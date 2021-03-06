package com.spikeify.cron.service;

import com.spikeify.Spikeify;

/**
 * Simplifies cron service initialization
 */
public class DefaultCronService extends CronServiceImpl {

	public DefaultCronService(Spikeify spikeify) {

		super(new CronManagerImpl(spikeify), new CronExecutorImpl(), null);
	}

	public DefaultCronService(Spikeify spikeify, CronSettings cronSettings) {

		super(new CronManagerImpl(spikeify), new CronExecutorImpl(), cronSettings);
	}
}
