package com.spikeify.cron.service;

/**
 *
 */
public class DefaultCronSettings implements CronSettings {

	private final String rootUrl;

	private final String cronUser;

	private final String cronPassword;

	public DefaultCronSettings(String url) {
		this(url, null, null);
	}

	public DefaultCronSettings(String url, String username, String password) {
		rootUrl = url;
		cronUser = username;
		cronPassword = password;
	}

	@Override
	public String getRootUrl() {

		return rootUrl;
	}

	@Override
	public String getCronUser() {

		return cronUser;
	}

	@Override
	public String getCronPassword() {

		return cronPassword;
	}
}
