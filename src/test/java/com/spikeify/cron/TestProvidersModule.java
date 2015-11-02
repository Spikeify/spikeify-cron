package com.spikeify.cron;

import com.aerospike.client.Host;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.spikeify.Spikeify;
import com.spikeify.SpikeifyService;
import com.spikeify.cron.utils.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TestProvidersModule extends AbstractModule {

	private static final Logger log = Logger.getLogger(TestProvidersModule.class.getSimpleName());

	@Override
	protected void configure() {

	}

	@Provides
	public Spikeify getSpikeify() {

		// configure if needed
		if (SpikeifyService.getClient() == null) {

			HashMap<String, Integer> hosts = new HashMap<>();
			hosts.put("127.0.0.1", 3000);

			String namespace = "test";

			log.info("Initializing Aerospike/Spikeify: namespace = " + namespace);
			log.info("Found hosts: " + JsonUtils.toJson(hosts));
			List<Host> hostsData = hosts.entrySet().stream().map(stringIntegerEntry -> new Host(stringIntegerEntry.getKey(), stringIntegerEntry.getValue())).collect(Collectors.toList());

			SpikeifyService.globalConfig(namespace, hostsData.toArray(new Host[hostsData.size()]));

			log.info("Spikeify configured namespace = " + SpikeifyService.defaultNamespace);
		}

		return SpikeifyService.sfy();
	}

}
