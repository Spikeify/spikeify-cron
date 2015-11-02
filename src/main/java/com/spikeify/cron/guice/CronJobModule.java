package com.spikeify.cron.guice;

import com.google.inject.AbstractModule;
import com.spikeify.cron.service.*;

public class CronJobModule extends AbstractModule {

	// module holding basic services for cron job use
	// Spikeify provider needs to be configured elsewhere
	@Override
	protected void configure() {

		bind(CronExecutor.class).to(CronExecutorImpl.class);
		bind(CronManager.class).to(CronManagerImpl.class);
		bind(CronService.class).to(CronServiceImpl.class);
	}
}
