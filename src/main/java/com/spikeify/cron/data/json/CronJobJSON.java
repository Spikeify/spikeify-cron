package com.spikeify.cron.data.json;

import com.spikeify.cron.NotNullAndIgnoreUnknowns;
import com.spikeify.cron.entities.CronJob;
import com.spikeify.cron.entities.enums.RunEvery;

/**
 * For REST usage
 */
@NotNullAndIgnoreUnknowns
public class CronJobJSON {

	public boolean disabled;

	public String id;
	public String name;
	public String target;
	public String description;

	public Long firstRun;
	public Long nextRun;
	public Long lastRun;

	public int interval;
	public RunEvery intervalUnits;

	public Integer startHour;
	public Integer startMinute;

	public Integer endHour;
	public Integer endMinute;

	public CronJobJSON() {

	}

	public CronJobJSON(CronJob job) {

		this(job, 0);
	}

	public CronJobJSON(CronJob job, int timeZone) {
		id = job.getId();
		name = job.getName();

		target = job.getTarget(null);

		disabled = (job.isDisabled());

		firstRun = job.getFirstRun();

		if (!disabled) {
			nextRun = job.getNextRun();
		}
		else {
			nextRun = null;
		}

		lastRun = job.getLastRun();

		startHour = job.getRunFromHour(timeZone);
		startMinute = job.getRunFromMinute();

		endHour = job.getRunToHour(timeZone);
		endMinute = job.getRunToMinute();

		intervalUnits = job.getIntervalUnit();
		interval = job.getInterval();

		description = job.getDescription(false, timeZone);
	}
}
