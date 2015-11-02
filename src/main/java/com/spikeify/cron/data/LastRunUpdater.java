package com.spikeify.cron.data;

import com.spikeify.cron.entities.CronJob;
import com.spikeify.cron.entities.enums.CronJobResult;

public class LastRunUpdater implements CronJobUpdater {

	private final long runTime;
	private final CronJobResult runResult;
	private final String runMessage;

	public LastRunUpdater(long timeMillis, CronJobResult result, String message) {

		runTime = timeMillis;
		runResult = result;
		runMessage = message;
	}

	public void update(CronJob job) {

		job.setLastRun(runTime, runResult, runMessage);
	}
}
