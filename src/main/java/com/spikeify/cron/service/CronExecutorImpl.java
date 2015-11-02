package com.spikeify.cron.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
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

@Singleton
public class CronExecutorImpl implements CronExecutor {

	private static final Logger log = Logger.getLogger(CronExecutorImpl.class.getSimpleName());

	@Inject
	public CronExecutorImpl() {

	}

	@Override
	public CronExecutorResult run(CronJob job, String rootUrl) {

		Assert.notNull(job, "Missing job to run!");

		if (!job.run()) {
			log.warning("Unable to run: " + job);
			return CronExecutorResult.fail(HttpURLConnection.HTTP_BAD_REQUEST, "Unable to run: " + job);
		}

		// if job can run, then target is set ... no need to check twice
		String target = job.getTarget(rootUrl);
		log.info("Running: " + job);

		return execute(target);
	}

	@Override
	public CronExecutorResult execute(String target) {

		return execute(target, null, null);
	}

	@Override
	public CronExecutorResult execute(String target, String cronUser, String cronPassword) {

		try {
			if (StringUtils.isNullOrEmptyTrimmed(target)) {
				return CronExecutorResult.fail(HttpURLConnection.HTTP_INTERNAL_ERROR, "No URL given, can't run!");
			}

			URL obj = new URL(target);
			HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

			// adding headers so target knows it's a cron job calling
			connection.setRequestProperty("Content-Type", "application/json");

			// simple basic auth if needed
			if (!StringUtils.isNullOrEmptyTrimmed(cronUser)) {
				String userCredentials = cronUser + ":" + cronPassword;
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
