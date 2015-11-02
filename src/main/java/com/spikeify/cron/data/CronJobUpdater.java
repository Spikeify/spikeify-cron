package com.spikeify.cron.data;

import com.spikeify.cron.entities.CronJob;

public interface CronJobUpdater {

	void update(CronJob job);
}
