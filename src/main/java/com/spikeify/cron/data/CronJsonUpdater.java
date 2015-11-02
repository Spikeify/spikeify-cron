package com.spikeify.cron.data;

import com.spikeify.cron.data.json.CronJobJSON;
import com.spikeify.cron.entities.CronJob;

import java.util.ArrayList;
import java.util.List;

public class CronJsonUpdater implements CronJobUpdater {

	private final CronJobJSON data;
	private final int timeZone;

	public CronJsonUpdater(CronJobJSON json, int localTimeZone) {

		data = json;
		timeZone = localTimeZone;
	}

	public void update(CronJob job) {

		List<CronJobUpdater> updaters = new ArrayList<>();

		updaters.add(new FirstRunUpdater(data.firstRun));

		if (data.target != null && data.target.trim().length() > 0) {

			if (data.intervalUnits != null &&
				data.interval > 0 &&
				data.startHour != null && data.startMinute != null &&
				data.endHour != null && data.endMinute != null) {

				// repeat only in range of hours
				updaters.add(new ScheduleUpdater(data.target, data.interval, data.intervalUnits, data.startHour, data.startMinute, data.endHour, data.endMinute, timeZone));
			}
			else if (data.startHour != null && data.startMinute != null &&
					 data.endHour == null && data.endMinute == null) {

				// once a day at certain hour
				updaters.add(new ScheduleUpdater(data.target, data.startHour, data.startMinute, timeZone));
			}
			else if (data.intervalUnits != null &&
					 data.interval > 0) {

				// repeat by interval
				updaters.add(new ScheduleUpdater(data.target, data.interval, data.intervalUnits));
			}
		}

		// enable / disable
		if (job.isDisabled() != data.disabled) {
			updaters.add(new EnableDisableUpdater(!data.disabled));
		}

		// call all added updaters
		for (CronJobUpdater updater : updaters) {
			updater.update(job);
		}
	}
}
