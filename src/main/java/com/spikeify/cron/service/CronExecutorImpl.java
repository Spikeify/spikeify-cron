package com.spikeify.cron.service;

import com.spikeify.cron.data.CronExecutorResult;
import com.spikeify.cron.entities.CronJob;
import com.spikeify.cron.entities.enums.CronJobResult;
import com.spikeify.cron.utils.Assert;
import com.spikeify.cron.utils.StringUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CronExecutorImpl implements CronExecutor {

	private static final Logger log = Logger.getLogger(CronExecutorImpl.class.getSimpleName());

	public CronExecutorImpl() {

	}

	@Override
	public CronExecutorResult run(CronJob job, CronSettings settings) {

		Assert.notNull(job, "Missing job to run!");

		if (!job.canRun()) {
			log.warning("Unable to run: " + job + ",  - no schedule defined (job will not run)!");
			return CronExecutorResult.fail(HttpURLConnection.HTTP_BAD_REQUEST, "Unable to run: " + job);
		}

		// if job can run, then target is set ... no need to check twice
		String target = job.getTarget(settings.getRootUrl());
		log.info("Running: " + job);

		return execute(target, settings);
	}

	@Override
	public CronExecutorResult execute(String target, CronSettings settings) {

		try {
			if (StringUtils.isNullOrEmptyTrimmed(target)) {
				return CronExecutorResult.fail(HttpURLConnection.HTTP_INTERNAL_ERROR, "No URL given, can't run!");
			}

			URL obj = new URL(target);
			HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

			// adding headers so target knows it's a cron job calling
			connection.setRequestProperty("Content-Type", "application/json");

			// simple basic auth if needed
			if (!StringUtils.isNullOrEmptyTrimmed(settings.getCronUser())) {

				String userCredentials = settings.getCronUser() + ":" + settings.getCronPassword();
				String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes(Charset.forName("UTF-8"))));
				connection.setRequestProperty("Authorization", basicAuth);
			}

			int status = connection.getResponseCode();

			if (status == HttpURLConnection.HTTP_OK ||
				status == HttpURLConnection.HTTP_NO_CONTENT) {
				log.info("Successfully triggered: " + target);
				return CronExecutorResult.ok(status);
			}

			log.severe("Http GET: " + target + ", returned: " + status);
			return new CronExecutorResult(CronJobResult.fail, status, "");
		}
		catch (Exception e) {
			log.log(Level.SEVERE, "Failed to execute HTTP request to: " + target, e);
			return CronExecutorResult.fail(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage());
		}
	}
}
