package com.spikeify.cron.exceptions;

public class CronJobException extends Exception {

	private final int errorCode;

	public CronJobException(String message) {
		this(message, 500);
	}

	public CronJobException(String message, int code) {
		super(message);
		errorCode = code;
	}

	public int getErrorCode() {
		return errorCode;
	}
}
