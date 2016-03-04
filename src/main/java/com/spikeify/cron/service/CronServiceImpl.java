package com.spikeify.cron.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.spikeify.cron.data.CronExecutorResult;
import com.spikeify.cron.data.CronJobUpdater;
import com.spikeify.cron.data.LastRunUpdater;
import com.spikeify.cron.data.LockCronUpdater;
import com.spikeify.cron.data.json.CronJobJSON;
import com.spikeify.cron.entities.CronJob;
import com.spikeify.cron.exceptions.CronJobException;
import com.spikeify.cron.utils.Assert;
import com.spikeify.cron.utils.JsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class CronServiceImpl implements CronService {

	private static final Logger log = Logger.getLogger(CronServiceImpl.class.getSimpleName());
	private static final long DELTA = 10 * 1000L; // 10 seconds

	private final CronManager manager;
	private final CronExecutor executor;
	private final CronSettings settings;

	public CronServiceImpl(CronManager cronManager,
						   CronExecutor cronExecutor,
	                       CronSettings cronSetting) {

		manager = cronManager;
		executor = cronExecutor;

		// make sure settings are present ... event if empty
		if (cronSetting == null) {
			settings = new DefaultCronSettings(null);
		}
		else {
			settings = cronSetting;
		}
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
	public int run() {

		List<CronJob> list = manager.list(System.currentTimeMillis());

		int count = 0;
		for (CronJob job : list) {

			try {
				// refresh job
				job = manager.get(job.getId());

				// can we run the job?
				if (job.run()) {
					// remember time job was started
					long startTime = job.getNextRun(); // take next run as start time if not smaller than 10s from current time
					if (startTime <= System.currentTimeMillis() - DELTA) {
						startTime = System.currentTimeMillis();
					}

					// execute
					// update and lock cron job before executing so other threads will not start the job ( ... )
					long lockTime = System.nanoTime();
					job = manager.update(job, new LockCronUpdater(lockTime));

					if (job.getStartedTime() == lockTime) {

						CronExecutorResult result = executor.run(job, settings);

						// set last run result and calculate next execution and store changes to database
						manager.update(job, new LastRunUpdater(startTime, result.getJobResult(), result.getMessage()));
						count++;
					}
				}
			}
			catch (CronJobException e) {
				// should not happen ... but anyhow ... let's catch it
				log.severe("Failed to update cron job: " + job);
			}
		}

		return count;
	}

	@Override
	public CronExecutorResult run(CronJob job) {

		String target = job.getTarget(settings.getRootUrl());
		return executor.execute(target, settings);
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
			InputStream stream = getClass().getResourceAsStream(resource);
			if (stream == null) {
				throw new CronJobException("Missing resource: '" + resource + "'", HttpURLConnection.HTTP_BAD_REQUEST);
			}

			Scanner s = new Scanner(stream).useDelimiter("\\A");
			String json = s.hasNext() ? s.next() : "";

			long lastModified = 0;
			if (checkTimestamp) {
				URL url = getClass().getResource(resource);
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
