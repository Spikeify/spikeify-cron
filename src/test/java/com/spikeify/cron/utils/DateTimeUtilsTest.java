package com.spikeify.cron.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DateTimeUtilsTest {

	@Test
	public void getUtcHourTest() {

		assertEquals(0, DateTimeUtils.getUtcHour(0, 0));
		assertEquals(23, DateTimeUtils.getUtcHour(0, 1));
		assertEquals(12, DateTimeUtils.getUtcHour(0, 12));

		assertEquals(11, DateTimeUtils.getUtcHour(0, -11));
		assertEquals(1, DateTimeUtils.getUtcHour(0, -1));
	}

	@Test
	public void getTimezoneHourTest() {

		assertEquals(0, DateTimeUtils.getTimezoneHour(0, 0));
		assertEquals(1, DateTimeUtils.getTimezoneHour(0, 1));
		assertEquals(12, DateTimeUtils.getTimezoneHour(0, 12));

		assertEquals(13, DateTimeUtils.getTimezoneHour(0, -11));
		assertEquals(23, DateTimeUtils.getTimezoneHour(0, -1));
	}
}