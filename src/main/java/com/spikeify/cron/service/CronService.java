package com.spikeify.cron.service;

import com.spikeify.cron.data.CronExecutorResult;
import com.spikeify.cron.data.CronJobUpdater;
import com.spikeify.cron.data.json.CronJobJSON;
import com.spikeify.cron.entities.CronJob;
import com.spikeify.cron.exceptions.CronJobException;

import java.util.List;

/**
 * Wraps multiple services into one
 */
public interface CronService {

	/**
	 * Creates new cron job
	 *
	 * @param name cron job name
	 * @return created cron job
	 * @throws CronJobException in case of invalid data
	 */
	CronJob create(String name) throws CronJobException;

	/**
	 * Updates existing cron job
	 *
	 * @param original to update
	 * @param updater  performing update on original
	 * @return updated cron job
	 * @throws CronJobException in case of invalid data
	 */
	CronJob update(CronJob original, CronJobUpdater updater) throws CronJobException;


	/**
	 * Returns job by id
	 *
	 * @param id of job
	 * @return job or null if not found
	 */
	CronJob find(String id);

	/**
	 * Lists all configured cron jobs by filter
	 *
	 * @return list of cron jobs
	 */
	List<CronJob> list();

	/**
	 * deletes given cron job
	 * @param job with id
	 */
	void delete(CronJob job);

	/**
	 * Finds tasks to be executed (run) on given moment and triggers them
	 *
	 * @return number of jobs run
	 * @throws CronJobException in case of invalid data
	 */
	int run(String rootUrl) throws CronJobException;

	/**
	 * Test runs given cron job
	 *
	 * @param job to be run
	 * @return result of run
	 */
	CronExecutorResult run(CronJob job);

	/**
	 * Exports all configured jobs as JSON
	 *
	 * @param timeZone local time zone to recalculate time info
	 * @return list of jobs (JSON)
	 */
	List<CronJobJSON> exportJobs(int timeZone);

	/**
	 * Imports cron jobs from external
	 *
	 * @param data     to be imported
	 * @param timeZone local time zone to take into account when setting UTC time
	 * @throws CronJobException in case of invalid data
	 */
	void importJobs(List<CronJobJSON> data, int timeZone) throws CronJobException;

	/**
	 * Imports jobs from a resource file
	 *
	 * @param resource       to load jobs from
	 * @param checkTimestamp true check if given resource has changed and is newer that the state in the database, false overwrite
	 * @param timeZone       local time zone to take into account when setting UTC timeC
	 * @throws CronJobException in case of invalid data
	 */
	void importJobs(String resource, boolean checkTimestamp, int timeZone) throws CronJobException;
}
