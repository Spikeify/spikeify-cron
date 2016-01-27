package com.spikeify.cron.service;

import com.spikeify.cron.data.CronExecutorResult;
import com.spikeify.cron.entities.CronJob;

/**
 * Takes care of cron job execution
 */
public interface CronExecutor {

	/**
	 * Runs cron job
	 *
	 * @param job     to be run
	 * @param setting settings to support running / root url ...
	 * @return result of cron job
	 */
	CronExecutorResult run(CronJob job, CronSettings setting);

	/**
	 * Calls target url with basic auth info in header using user and password
	 *
	 * @param target       to invoke
	 * @param settings     for basic auth if any
	 * @return result of cron job
	 */
	CronExecutorResult execute(String target, CronSettings settings);
}
