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
	 * @param rootUrl root url to prefix on target (if needed)
	 * @return result of cron job
	 */
	CronExecutorResult run(CronJob job, String rootUrl);

	/**
	 * Calls target url
	 *
	 * @param target to invoke
	 * @return result of cron job
	 */
	CronExecutorResult execute(String target);

	/**
	 * Calls target url with basic auth info in header using user and password
	 *
	 * @param target       to invoke
	 * @param cronUser     basic auth user
	 * @param cronPassword basic auth password
	 * @return result of cron job
	 */
	CronExecutorResult execute(String target, String cronUser, String cronPassword);
}
