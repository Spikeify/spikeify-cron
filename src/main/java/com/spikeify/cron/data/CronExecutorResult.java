package com.spikeify.cron.data;

import com.spikeify.cron.entities.enums.CronJobResult;

/**
 * Class holding info about last cron job execution
 */
public class CronExecutorResult {

	private final CronJobResult jobResult;
	private final int httpStatus;

	private final String message;

	public CronExecutorResult(CronJobResult result, int statusCode, String infoMessage) {
		jobResult = result;
		httpStatus = statusCode;
		message = infoMessage;
	}


	public String getMessage() {

		return message;
	}

	public int getHttpStatus() {

		return httpStatus;
	}

	public CronJobResult getJobResult() {

		return jobResult;
	}

	public static CronExecutorResult ok(int status) {
		return new CronExecutorResult(CronJobResult.ok, status, null);
	}

	public static CronExecutorResult fail(int status, String message) {
		return new CronExecutorResult(CronJobResult.fail, status, message);
	}
}
