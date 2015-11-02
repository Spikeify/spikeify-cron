package com.spikeify.cron.data;

import com.spikeify.cron.entities.CronJob;

public class EnableDisableUpdater implements CronJobUpdater {

	private final boolean enable;

	public EnableDisableUpdater(boolean enableJob) {
		enable = enableJob;
	}

	public void update(CronJob job) {

		if (enable) {
			job.enable();
		}
		else {
			job.disable();
		}
	}
}
