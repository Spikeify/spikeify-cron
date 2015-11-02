package com.spikeify.cron.data;

import com.spikeify.cron.entities.CronJob;

public class FirstRunUpdater implements CronJobUpdater {

	private final long startTime;

	public FirstRunUpdater(Long start) {

		startTime = start == null ? 0 : start;
	}

	public void update(CronJob job) {

		job.setFirstRun(startTime);
	}
}
