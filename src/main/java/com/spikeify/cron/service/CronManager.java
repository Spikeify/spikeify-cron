package com.spikeify.cron.service;

import com.spikeify.cron.data.CronJobUpdater;
import com.spikeify.cron.data.json.CronJobJSON;
import com.spikeify.cron.entities.CronJob;
import com.spikeify.cron.exceptions.CronJobException;

import java.util.List;

/**
 * Takes care of storage and retrieval of cron jobs from to database
 */
public interface CronManager {

	/**
	 * Creates new cron job
	 *
	 * @param name of cron job
	 * @return created job
	 * @throws CronJobException in case of invalid data
	 */
	CronJob create(String name) throws CronJobException;

	/**
	 * Updates existing cron job
	 *
	 * @param job     to be updated
	 * @param updater class updating job
	 * @return updated job
	 * @throws CronJobException in case of invalid data
	 */
	CronJob update(CronJob job, CronJobUpdater updater) throws CronJobException;

	/**
	 * @param id returns job with id or throws exception if not found
	 * @return job or throws exception
	 * @throws CronJobException when job not found
	 */
	CronJob get(String id) throws CronJobException;

	/**
	 * @param id returns job with id or null if not found
	 * @return job or null
	 */
	CronJob find(String id);

	/**
	 * @param name returns job with name or null if not found
	 * @return job or null
	 */
	CronJob findByName(String name);

	/**
	 * Lists cron jobs
	 *
	 * @return list of cron jobs
	 */
	List<CronJob> list();

	/**
	 * Gets list of cron jobs to be executed at certain time of day
	 *
	 * @param time current time
	 * @return list of jobs or empty list if none found
	 */
	List<CronJob> list(long time);

	/**
	 * Removes job from database completely
	 *
	 * @param job to be removed
	 */
	void delete(CronJob job);

	/**
	 * Imports job into database
	 *
	 * @param job          to be imported
	 * @param lastModified time stamp of latest change, 0 to ignore (if job.lastModified is older than given time stamp job is imported)
	 * @param timeZone local time zone (to recalculate stored time to UTC)
	 * @throws CronJobException in case of invalid data
	 */
	void importJob(CronJobJSON job, long lastModified, int timeZone) throws CronJobException;
}
