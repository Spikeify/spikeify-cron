package com.spikeify.cron;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.spikeify.cron.guice.CronJobModule;

public class TestHelper {

	public static Injector inject(Object clazz) {

		Injector injector = Guice.createInjector(new CronJobModule(),
												 new TestProvidersModule());
		injector.injectMembers(clazz);
		return injector;
	}
}
