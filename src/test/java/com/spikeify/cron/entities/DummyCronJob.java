package com.spikeify.cron.entities;

import com.spikeify.cron.data.json.CronJobJSON;
import com.spikeify.cron.entities.enums.RunEvery;

/**
 *
 */
public class DummyCronJob extends CronJob {

	public DummyCronJob(String jobName) {

		name = jobName;
		interval = 1;
		intervalUnit = RunEvery.minute;
		target = "bla";
	}
}
