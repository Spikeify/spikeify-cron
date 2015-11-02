package com.spikeify.cron.data.json;

import com.spikeify.cron.NotNullAndIgnoreUnknowns;
import com.spikeify.cron.data.CronExecutorResult;
import com.spikeify.cron.entities.enums.CronJobResult;
import com.spikeify.cron.utils.Assert;

/**
 * Used to report cron result via REST call
 */
@NotNullAndIgnoreUnknowns
public class CronExecutorResultJSON {

	public int status;
	public CronJobResult result;
	public String message;

	public CronExecutorResultJSON(CronExecutorResult data) {

		Assert.notNull(data, "Missing cron result!");
		status = data.getHttpStatus();
		result = data.getJobResult();
		message = data.getMessage();
	}
}
