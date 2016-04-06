package com.spikeify.cron.entities;

import com.spikeify.annotations.Generation;
import com.spikeify.annotations.Indexed;
import com.spikeify.annotations.UserKey;
import com.spikeify.cron.entities.enums.CronJobResult;
import com.spikeify.cron.entities.enums.RunEvery;
import com.spikeify.cron.utils.Assert;
import com.spikeify.cron.utils.DateTimeUtils;
import com.spikeify.cron.utils.StringUtils;
import com.spikeify.cron.utils.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Entity in database holding information about cron task
 */
public class CronJob {

	private static final Logger log = LoggerFactory.getLogger(CronJob.class);

	private static final long ONE_DAY_IN_MILLISECONDS = 24L * 60L * 60L * 1000L;

	// simple measure to ease filtering out enabled and disabled jobs until they are run for the first time
	private static final long RUN_DISABLED = -1L;

	private static final long RUN_ENABLED = 1L;

	private static final long START_LOCK_DURATION = 60L * 1000L; // 60 seconds

	/**
	 * Uniquely generated id
	 */
	@UserKey(generate = true)
	protected String id;

	@Generation
	protected int generation;

	/**
	 * description of cron job - should be unique
	 */
	@Indexed
	protected String name;

	/*
	 * time stamp cron job was last modified (schedule)
	 */
	protected long lastModified;

	/**
	 * target URL to call: GET http://some/url
	 */
	protected String target;

	/**
	 * Time stamp when job should run for the first time
	 * null = as soon as possible
	 */
	protected Long firstRun;

	/**
	 * Time stamp of last run
	 * if null no run has been performed - start as soon as possible
	 */
	protected Long lastRun;

	/**
	 * Time job was started ... to prevent other thread starting the same cron job
	 */
	protected Long startTime;

	/**
	 * Last execution result
	 */
	protected CronJobResult lastResult;

	/**
	 * Last run massage if any
	 */
	protected String lastMessage;

	/**
	 * Time job should run next
	 * if nextRun is lower than current time
	 * - is calculated when job is run ...
	 * - to be available on next run
	 * -1 - default / disabled ... until interval is set
	 */
	@Indexed
	protected long nextRun;


	/**
	 * SCHEDULE definition
	 */

	/**
	 * run every, every second, every five (minute, hour, day ...)
	 * 0 = don't run
	 */
	protected int interval;

	/**
	 * run every minute, hour, day
	 */
	protected RunEvery intervalUnit;

	/**
	 * run from hour/minute in current day, null - don't care
	 */
	protected Integer runFromHour;

	protected Integer runFromMinute;

	/**
	 * run to hour/minute in current day, null - don't care
	 * run to can be lower than run from to enable running for instance from: 23:20 until: 1:20
	 */
	protected Integer runToHour;

	protected Integer runToMinute;

	private boolean locked;

	protected CronJob() {
		// Aerospike only
	}

	public CronJob(String jobName) {

		Assert.notNullOrEmptyTrimmed(jobName, "Missing job name!");

		name = jobName.trim();
		nextRun = RUN_ENABLED;
		lastModified = 0;
		startTime = null;
	}

	public void setTarget(String newTarget) {

		Assert.notNullOrEmptyTrimmed(newTarget, "Missing target!");

		try {
			new URI(newTarget); // check if correct
			target = newTarget.trim();

			calculateNextRun();
		}
		catch (URISyntaxException e) {
			log.error("Invalid target URI: " + newTarget, e);
			throw new IllegalArgumentException("Invalid target URI: " + newTarget);
		}
	}

	public boolean run() {

		// not started ... and can be run
		return (canRun() && !isLocked() && nextRun <= getTime());
	}


	public boolean canRun() {

		return !(isDisabled() || target == null || intervalUnit == null);

	}

	public String getTarget(String rootUrl) {

		if (StringUtils.isNullOrEmpty(rootUrl)) {
			return target;
		}

		// build url from root url and target
		return UrlUtils.getFullUrl(rootUrl, target);
	}

	public void disable() {

		if (!isDisabled()) {
			disableNextRun();
		}
	}

	public void enable() {

		if (isDisabled()) {
			nextRun = RUN_ENABLED;
			calculateNextRun();
		}
	}

	public boolean isDisabled() {

		return nextRun == RUN_DISABLED;
	}

	public String getId() {

		return id;
	}

	public String getName() {

		return name;
	}

	public Long getFirstRun() {

		return firstRun;
	}

	public Long getLastRun() {

		return lastRun;
	}

	public Long getLastRun(int timezone) {

		if (lastRun == null) {
			return null;
		}

		return DateTimeUtils.getTimezoneTime(lastRun, timezone);
	}

	public CronJobResult getLastResult() {

		return lastResult;
	}

	public String getLastResultMessage() {

		return lastMessage;
	}

	public long getNextRun() {

		return nextRun;
	}

	public long getNextRun(int timeZone) {

		if (isDisabled()) {
			return nextRun;
		}

		return DateTimeUtils.getTimezoneTime(nextRun, timeZone);
	}

	public int getInterval() {

		return interval;
	}

	public RunEvery getIntervalUnit() {

		return intervalUnit;
	}

	public Integer getRunFromHour() {

		return runFromHour;
	}

	public Integer getRunFromHour(int timeZone) {

		return runFromHour != null ? DateTimeUtils.getTimezoneHour(runFromHour, timeZone) : null;
	}

	public Integer getRunFromMinute() {

		return runFromMinute;
	}

	public Integer getRunToHour() {

		return runToHour;
	}

	public Integer getRunToHour(int timeZone) {

		return runToHour != null ? DateTimeUtils.getTimezoneHour(runToHour, timeZone) : null;
	}

	public Integer getRunToMinute() {

		return runToMinute;
	}

	public void setFirstRun(long startTime) {

		Assert.isTrue(startTime >= 0, "Start time must be >= 0!");

		if (startTime < getTime()) {
			firstRun = null;
		}
		else {
			firstRun = startTime;
		}

		// clear last run
		lastRun = null;

		calculateNextRun();
	}

	public void setLastRun(long runTime, CronJobResult result, String message) {

		Assert.isTrue(runTime <= getTime(), "Last run time can't be in the future!");
		Assert.notNull(result, "Missing cron job result!");

		lastRun = runTime;
		lastResult = result;
		lastMessage = message != null ? message.trim() : null;
		startTime = null; // unlock

		calculateNextRun();
	}

	public void setRunInterval(int number, RunEvery unit) {

		Assert.isTrue(number > 0, "Interval must be > 0, but was: " + number + "!");
		Assert.notNull(unit, "Missing interval unit!");

		interval = number;
		intervalUnit = unit;

		calculateNextRun();
	}

	public void runExactlyAt(int hour, int minute) {

		checkHour(hour);
		checkMinutes(minute);

		interval = 1;
		intervalUnit = RunEvery.day;
		runFromHour = hour;
		runFromMinute = minute;

		runToHour = null;
		runToMinute = null;

		calculateNextRun();
	}

	public void runFromTo(int fromHour, int fromMinute, int toHour, int toMinute) {

		checkHour(fromHour);
		checkMinutes(fromMinute);

		checkHour(toHour);
		checkMinutes(toMinute);

		runFromHour = fromHour;
		runFromMinute = fromMinute;

		runToHour = toHour;
		runToMinute = toMinute;

		calculateNextRun();
	}

	public void clearRunFromTo() {

		runFromHour = null;
		runFromMinute = null;
		runToHour = null;
		runToMinute = null;

		calculateNextRun();
	}

	private void checkMinutes(int fromMinute) {

		Assert.isTrue(fromMinute >= 0 && fromMinute < 60, "Expected minute: 0 - 59, but was: " + fromMinute);
	}

	private void checkHour(int fromHour) {

		Assert.isTrue(fromHour >= 0 && fromHour < 24, "Expected hour: 0 - 23, but was: " + fromHour);
	}

	private void disableNextRun() {

		if (nextRun >= RUN_ENABLED) {
			nextRun = RUN_DISABLED;
		}
	}

	public void setStarted(long time) {

		startTime = time;
	}

	public long getStartedTime() {

		if (startTime == null) {
			return 0;
		}
		return startTime;
	}

	public boolean isLocked() {

		return startTime != null &&
			nextRun + START_LOCK_DURATION > System.currentTimeMillis(); // 1 minute lock at the most
	}

	@Override
	public String toString() {

		return id + " [" + name + "] " + getDescription(true, 0);
	}

	public String getDescription(boolean withTarget, int timeZone) {

		StringBuilder builder = new StringBuilder();

		if (firstRun != null && firstRun > getTime()) {

			builder.append("first run: ");
			builder.append(formatDateTime(firstRun, timeZone));
		}

		if (intervalUnit == null) {
			if (builder.length() > 0) {
				builder.append(" ");
			}

			builder.append("- no schedule defined (job will not run)!");
			return builder.toString();
		}

		if (builder.length() > 0) {
			builder.append(", ");
		}

		builder.append("runs every ");

		int lastDigit = interval % 10;

		if (lastDigit != 1) {
			builder.append(interval);
		}

		switch (lastDigit) {
			case 1:
				break;

			case 2:
				builder.append("nd ");
				break;

			case 3:
				builder.append("rd ");
				break;

			default:
				builder.append("th ");
				break;
		}

		builder.append(intervalUnit.name());

		if (runFromHour != null) {

			if (runToHour != null) {
				builder.append(", from: ");
			}
			else {
				builder.append(" at: ");
			}
			builder.append(getRunTimeFormatted(runFromHour, runFromMinute, timeZone));

			if (runToHour != null) {
				builder.append(", until: ");
				builder.append(getRunTimeFormatted(runToHour, runToMinute, timeZone));
			}
		}

		if (withTarget) {
			if (target == null || target.trim().length() == 0) {
				builder.append(", missing target (job will not run)");
			}
			else {
				builder.append(", target: ").append(target);
			}
		}

		if (isDisabled()) {
			builder.append(", next run: disabled");
		}
		else if (nextRun >= getTime()) {
			builder.append(", next run: ").append(formatDateTime(nextRun, timeZone));
		}

		return builder.toString();
	}

	private String getRunTimeFormatted(Integer hour, Integer minute, int timezone) {

		if (hour == null || minute == null) {
			return "";
		}

		hour = DateTimeUtils.getTimezoneHour(hour, timezone);

		String out = (hour < 10) ? "0" + hour : "" + hour;
		out = out + ":";
		return (minute < 10) ? out + "0" + minute : out + minute;
	}

	private String formatDateTime(long time, int timezone) {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		format.setCalendar(getCalendar(timezone));
		return DateTimeUtils.format(time, format);
	}

	/**
	 * Calculates when job should run next time
	 */
	protected void calculateNextRun() {

		lastModified = System.currentTimeMillis();

		if (isDisabled()) {
			return; // nothing to calculate
		}

		long currentTime = getTime();

		long start = currentTime;
		if (firstRun != null && firstRun >= currentTime) {
			start = firstRun;
		}

		if (lastRun != null &&
			(firstRun == null || firstRun < lastRun) &&
			lastRun <= currentTime) {
			start = lastRun;
			// calculate next run ...
			start = getNextRunFor(intervalUnit, interval, start);
		}

		// check if from / to limits are met
		Calendar calendar = getCalendar(0);
		calendar.setTimeInMillis(start);
		int startHour = calendar.get(Calendar.HOUR_OF_DAY);
		int startMinute = calendar.get(Calendar.MINUTE);

		if (hasRunFrom() &&
			runFromBeforeRunTo() &&
			isBefore(startHour, startMinute, runFromHour, runFromMinute)) {

			// start at run from hour:minute
			calendar.setTimeInMillis(start);
			calendar.set(Calendar.HOUR_OF_DAY, runFromHour);
			calendar.set(Calendar.MINUTE, runFromMinute);
			start = calendar.getTimeInMillis();

			// recalculate
			startHour = calendar.get(Calendar.HOUR_OF_DAY);
			startMinute = calendar.get(Calendar.MINUTE);
		}

		if (hasRunTo() &&
			runFromBeforeRunTo() &&
			isAfter(startHour, startMinute, runToHour, runToMinute)) {

			// + ONE DAY
			calendar.setTimeInMillis(start + ONE_DAY_IN_MILLISECONDS);
			calendar.set(Calendar.HOUR_OF_DAY, runFromHour);
			calendar.set(Calendar.MINUTE, runFromMinute);
			start = calendar.getTimeInMillis();
		}


		// opposite ... run from is after run to
		if (hasRunTo() &&
			!runFromBeforeRunTo() &&
			isAfter(startHour, startMinute, runToHour, runToMinute) &&
			isBefore(startHour, startMinute, runFromHour, runFromMinute)) {

			// start at run to hour:minute
			calendar.setTimeInMillis(start);
			calendar.set(Calendar.HOUR_OF_DAY, runFromHour);
			calendar.set(Calendar.MINUTE, runFromMinute);
			start = calendar.getTimeInMillis();
		}

		nextRun = start;
	}

	private boolean isBefore(int hour, int minute, int compareHour, int compareMinute) {

		return hour < compareHour || hour == compareHour && minute <= compareMinute;
	}

	private boolean isAfter(int hour, int minute, int compareHour, int compareMinute) {

		return hour > compareHour || hour == compareHour && minute >= compareMinute;
	}

	private boolean hasRunFrom() {

		return runFromMinute != null && runFromHour != null;
	}

	private boolean hasRunTo() {

		return runToMinute != null && runToHour != null;
	}

	private boolean runFromBeforeRunTo() {

		return hasRunFrom() && (!hasRunTo() || isBefore(runFromHour, runFromMinute, runToHour, runToMinute));
	}

	/**
	 * needed for time simulation in unit tests
	 *
	 * @return current system time (for test mocking purposes only)
	 */
	protected long getTime() {

		return System.currentTimeMillis();
	}

	public boolean isOlder(long timeStamp) {

		return timeStamp == 0 ||
			lastModified <= timeStamp;
	}

	private long getNextRunFor(RunEvery interval, long intervalUnits, long start) {

		// calculate next interval
		switch (interval) {
			case minute:
				start = start + (60L * 1000L) * intervalUnits;
				break;

			case hour:
				start = start + (60L * 60L * 1000L) * intervalUnits;
				break;

			case day:
				start = start + ONE_DAY_IN_MILLISECONDS * intervalUnits;
				break;

			case week:
				start = start + (ONE_DAY_IN_MILLISECONDS * 7L * intervalUnits);
				break;
		}

		// new calculated time is in the past ...
		if (start < getTime()) {
			return getTime();
		}

		return start;
	}

	private Calendar getCalendar(int timezone) {

		TimeZone zone = TimeZone.getTimeZone("UTC");
		zone.setRawOffset(60 * 60 * 1000 * timezone);

		return Calendar.getInstance(zone);
	}

	@Override
	public boolean equals(Object o) {

		if (o == this) { return true; }

		if (!(o instanceof CronJob)) { return false; }

		CronJob compare = (CronJob) o;
		return StringUtils.equals(compare.name, name) &&
			StringUtils.equals(compare.target, target) &&
			Objects.equals(compare.firstRun, firstRun) &&
			Objects.equals(compare.runFromHour, runFromHour) &&
			Objects.equals(compare.runFromMinute, runFromMinute) &&
			Objects.equals(compare.runToHour, runToHour) &&
			Objects.equals(compare.runToMinute, runToMinute);
	}

	@Override
	public int hashCode() {

		return getDescription(true, 0).hashCode();
	}
}
