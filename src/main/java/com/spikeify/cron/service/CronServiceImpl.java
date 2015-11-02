package com.spikeify.cron.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.spikeify.cron.data.CronExecutorResult;
import com.spikeify.cron.data.CronJobUpdater;
import com.spikeify.cron.data.LastRunUpdater;
import com.spikeify.cron.data.json.CronJobJSON;
import com.spikeify.cron.entities.CronJob;
import com.spikeify.cron.exceptions.CronJobException;
import com.spikeify.cron.utils.Assert;
import com.spikeify.cron.utils.JsonUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Singleton
public class CronServiceImpl implements CronService {

	private static final Logger log = Logger.getLogger(CronServiceImpl.class.getSimpleName());

	private final CronManager manager;
	private final CronExecutor executor;

	@Inject
	public CronServiceImpl(CronManager cronManager,
						   CronExecutor cronExecutor) {

		manager = cronManager;
		executor = cronExecutor;
	}

	@Override
	public CronJob create(String name) throws CronJobException {

		return manager.create(name);
	}

	@Override
	public CronJob update(CronJob job, CronJobUpdater updater) throws CronJobException {

		return manager.update(job, updater);
	}

	@Override
	public CronJob find(String id) {

		return manager.find(id);
	}

	@Override
	public List<CronJob> list() {

		return manager.list();
	}

	@Override
	public void delete(CronJob job) {
		manager.delete(job);
	}

	@Override
	public int run(String rootUrl) {

		List<CronJob> list = manager.list(System.currentTimeMillis());

		int count = 0;
		for (CronJob job : list) {

			try {
				// execute
				CronExecutorResult result = executor.run(job, rootUrl);

				// set last run result and calculate next execution and store changes to database
				manager.update(job, new LastRunUpdater(System.currentTimeMillis(), result.getJobResult(), result.getMessage()));
				count ++;
			}
			catch (CronJobException e) {
				// should not happen ... but anyhow ... let's catch it
				log.severe("Failed to update cron job: " + job);
			}
		}

		return count;
	}

	@Override
	public CronExecutorResult run(CronJob job, String rootUrl) {

		String target = job.getTarget(rootUrl);
		return executor.execute(target);
	}

	@Override
	public List<CronJobJSON> exportJobs(int timeZone) {

		List<CronJob> jobs = list();
		List<CronJobJSON> output = new ArrayList<>();

		for (CronJob job: jobs) {
			output.add(new CronJobJSON(job, timeZone));
		}

		return output;
	}

	@Override
	public void importJobs(List<CronJobJSON> data, int timeZone) throws CronJobException {

		Assert.notNull(data, "Missing jobs to import!");

		for (CronJobJSON job: data) {
			manager.importJob(job, 0, timeZone);
		}
	}

	@Override
	public void importJobs(String resource, boolean checkTimestamp, int timeZone) throws CronJobException {

		try {
			URL url = Resources.getResource(resource);
			String json = Resources.toString(url, Charsets.UTF_8);

			long lastModified = 0;
			if (checkTimestamp) {
				lastModified = url.openConnection().getLastModified(); // get last modified date of resource
			}

			List<CronJobJSON> jobs = JsonUtils.fromJson(json, new TypeReference<List<CronJobJSON>>() {});

			for (CronJobJSON job: jobs) {
				manager.importJob(job, lastModified, timeZone);
			}
		}
		catch (IOException e) {
			throw new CronJobException("Resource: " + resource + ", not found!", HttpURLConnection.HTTP_NOT_FOUND);
		}
	}
}
