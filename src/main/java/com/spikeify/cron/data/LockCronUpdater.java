package com.spikeify.cron.data;

import com.spikeify.cron.entities.CronJob;

/**
 *
 */
public class LockCronUpdater implements CronJobUpdater {

	private final long time;

	public LockCronUpdater(long startTime) {
		super();
		time = startTime;
	}

	@Override
	public void update(CronJob job) {

		if (!job.isLocked()) {
			job.setStarted(time);
		}
	}
}
