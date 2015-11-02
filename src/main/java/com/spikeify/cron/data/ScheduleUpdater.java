package com.spikeify.cron.data;

import com.spikeify.cron.entities.CronJob;
import com.spikeify.cron.entities.enums.RunEvery;
import com.spikeify.cron.utils.DateTimeUtils;

public class ScheduleUpdater implements CronJobUpdater {

	private final String jobTarget;
	private final int runInterval;
	private final RunEvery runIntervalUnit;

	private final boolean runFromTo;
	private final boolean runOnce;

	private final Integer runFromHour;
	private final Integer runFromMinute;
	private final Integer runToHour;
	private final Integer runToMinute;


	public ScheduleUpdater(String target,
						   int interval,
						   RunEvery intervalUnit) {

		runFromTo = false;
		runOnce = false;

		jobTarget = target;
		runInterval = interval;
		runIntervalUnit = intervalUnit;

		runFromHour = null;
		runFromMinute = null;
		runToHour = null;
		runToMinute = null;
	}

	public ScheduleUpdater(String target,
						   int interval, RunEvery intervalUnit,
						   int fromHour, int fromMinute,
						   int toHour, int toMinute,
						   int timezone) {

		if (timezone < -11 || timezone > 12) {
			throw new IllegalArgumentException("Expected time zone > -12 and <= 12!");
		}

		runFromTo = true;
		runOnce = false;

		jobTarget = target;
		runInterval = interval;
		runIntervalUnit = intervalUnit;

		runFromHour = DateTimeUtils.getUtcHour(fromHour, timezone);
		runFromMinute = fromMinute;
		runToHour = DateTimeUtils.getUtcHour(toHour, timezone);
		runToMinute = toMinute;
	}

	public ScheduleUpdater(String target,
						   int atHour, int atMinute,
						   int timezone) {

		if (timezone < -11 || timezone > 12) {
			throw new IllegalArgumentException("Expected time zone > -12 and <= 12!");
		}

		runFromTo = false;
		runOnce = true;

		jobTarget = target;
		runInterval = 1;
		runIntervalUnit = RunEvery.day;

		runFromHour = DateTimeUtils.getUtcHour(atHour, timezone);
		runFromMinute = atMinute;
		runToHour = null;
		runToMinute = null;
	}

	public void update(CronJob job) {

		job.setRunInterval(runInterval, runIntervalUnit);
		job.setTarget(jobTarget);

		job.clearRunFromTo();

		if (runFromTo) {
			job.runFromTo(runFromHour, runFromMinute, runToHour, runToMinute);
		}

		if (runOnce) {
			job.runExactlyAt(runFromHour, runFromMinute);
		}
	}
}
