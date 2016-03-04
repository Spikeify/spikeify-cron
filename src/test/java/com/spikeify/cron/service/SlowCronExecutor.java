package com.spikeify.cron.service;

import com.spikeify.cron.data.CronExecutorResult;

/**
 *
 */
public class SlowCronExecutor extends CronExecutorImpl {

	@Override
	public CronExecutorResult execute(String target, CronSettings settings) {

		// simulates long executions
		try {
			Thread.sleep(4000);
		}
		catch (InterruptedException e) {
			return CronExecutorResult.fail(500, "interrupted");
		}

		return CronExecutorResult.ok(200);
	}
}
