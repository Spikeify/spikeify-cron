package com.spikeify.cron.entities.enums;

/**
 * Defines run interval.
 * First run happens as soon as possible, next run is then calculated upon the interval
 */
public enum RunEvery {

	minute, // will run every X minutes (according to runEvery)
	hour,	// will run every X hours (according to runEvery)
	day,	// will run every X days (according to runEvery)
	week
}
