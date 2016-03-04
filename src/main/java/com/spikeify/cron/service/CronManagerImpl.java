package com.spikeify.cron.service;

import com.aerospike.client.AerospikeException;
import com.spikeify.Spikeify;
import com.spikeify.SpikeifyService;
import com.spikeify.cron.data.CronJobUpdater;
import com.spikeify.cron.data.CronJsonUpdater;
import com.spikeify.cron.data.json.CronJobJSON;
import com.spikeify.cron.entities.CronJob;
import com.spikeify.cron.exceptions.CronJobException;
import com.spikeify.cron.utils.Assert;
import com.spikeify.cron.utils.StringUtils;

import java.net.HttpURLConnection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CronManagerImpl implements CronManager {

	private static final Logger log = LoggerFactory.getLogger(CronManagerImpl.class);

	private final Spikeify sfy;

	public CronManagerImpl(Spikeify spikeify) {

		Assert.notNull(spikeify, "Missing spikeify!");
		sfy = spikeify;

		// register indexes for CronJob
		SpikeifyService.register(CronJob.class);
	}

	@Override
	public CronJob create(String name) throws CronJobException {

		if (findByName(name) != null) {
			throw new CronJobException("Job named: '" + name + "', already exists!", HttpURLConnection.HTTP_CONFLICT);
		}

		CronJob newJob = new CronJob(name);

		try {
			sfy.create(newJob).now();
			return newJob;
		}
		catch (AerospikeException e) {
			log.error("Failed to create cron job: " + newJob, e);
			throw new CronJobException("Failed to create cron job: " + newJob, HttpURLConnection.HTTP_BAD_REQUEST);
		}
	}

	@Override
	public CronJob update(CronJob job, CronJobUpdater updater) throws CronJobException {

		Assert.notNull(job, "Missing cron job to update!");
		Assert.notNull(updater, "Missing cron job updater!");

		try {
			return sfy.transact(5, () -> {

				CronJob original = sfy.get(CronJob.class).key(job.getId()).now();
				Assert.notNull(original, "Could not find cron job with id: " + job.getId());

				updater.update(original);
				sfy.update(original).now();
				return original;
			});
		}
		catch (AerospikeException e) {
			log.error("Failed to update cron job: " + job, e);
			throw new CronJobException("Failed to update cron job: " + job, HttpURLConnection.HTTP_BAD_REQUEST);
		}
	}

	@Override
	public CronJob get(String id) throws CronJobException {

		CronJob found = find(id);
		if (found == null) {
			throw new CronJobException("Job with id: '" + id + "', not found!", HttpURLConnection.HTTP_NOT_FOUND);
		}

		return found;
	}

	@Override
	public CronJob find(String id) {

		Assert.notNullOrEmptyTrimmed(id, "Missing job id!");
		id = StringUtils.trim(id);
		return sfy.get(CronJob.class).key(id).now();
	}

	@Override
	public CronJob findByName(String name) {

		Assert.notNullOrEmptyTrimmed(name, "Missing job name!");
		name = StringUtils.trim(name);
		return sfy.query(CronJob.class).filter("name", name).now().getFirst();
	}

	@Override
	public List<CronJob> list() {

		return sfy.scanAll(CronJob.class).now();
	}

	@Override
	public List<CronJob> list(long time) {

		Assert.isTrue(time >= 0, "Expecting time >= 0!");

		// list jobs that are candidates for running (nextRun is lower than time - is in the past)
		List<CronJob> list = sfy.query(CronJob.class).filter("nextRun", 0, time).now().toList();

		// remove jobs that can't run
		list.removeIf(job -> !job.run());
		return list;
	}

	@Override
	public void delete(CronJob job) {

		Assert.notNull(job, "Missing job to delete!");
		sfy.delete(job).now();
	}

	@Override
	public void importJob(CronJobJSON job, long lastModified, int timeZone) throws CronJobException {

		Assert.notNull(job, "Missing job to import!");
		Assert.notNullOrEmptyTrimmed(job.name, "Missing job name to import!");
		CronJob found = findByName(job.name); // find job by name

		if (found == null) { // job doesn't exists ... create new job ...  data
			log.info("Job not found, creating new job with name: '" + job.name + "'");
			found = create(job.name);
		}

		if (found.isOlder(lastModified)) {
			update(found, new CronJsonUpdater(job, timeZone));
		}
	}
}
