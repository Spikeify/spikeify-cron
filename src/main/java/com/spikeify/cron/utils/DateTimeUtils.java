package com.spikeify.cron.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public final class DateTimeUtils {

	private DateTimeUtils() {
		// hiding constructor
	}

	private static ThreadLocal<SimpleDateFormat> simpleDateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {

			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};

	private static ThreadLocal<SimpleDateFormat> simpleTimeFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {

			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
		}
	};

	public static SimpleDateFormat getDateFormat() {

		return simpleDateFormatThreadLocal.get();
	}

	public static SimpleDateFormat getTimeFormat() {

		return simpleTimeFormatThreadLocal.get();
	}

	public static Calendar getCalendar() {

		return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	}

	public static Calendar getCalendar(long time) {

		Calendar calendar = getCalendar();
		calendar.setTimeInMillis(time);
		return calendar;
	}

	public static String formatDateTime(long time) {

		SimpleDateFormat format = getTimeFormat();
		Calendar calendar = getCalendar(time);

		return format.format(calendar.getTime());
	}

	public static String formatDate(long time) {

		SimpleDateFormat format = getDateFormat();
		Calendar calendar = getCalendar(time);

		return format.format(calendar.getTime());
	}

	public static String format(long time, SimpleDateFormat format) {

		if (format == null) {
			return formatDateTime(time);
		}

		Calendar calendar = getCalendar(time);
		return format.format(calendar.getTime());
	}

	/**
	 * Converts UTC hour to time zone hour
	 * @param hour UTC hour
	 * @param timezone time zone
	 * @return hour as seen in the given time zone
	 */
	public static int getTimezoneHour(int hour, int timezone) {

		hour = (hour + timezone) % 24;
		if (hour < 0) {
			return 24 + hour;
		}

		return hour;
	}

	/**
	 * Converts local hour back to UTC hour
	 * @param hour local hour in time zone
	 * @param timezone current time zone
	 * @return UTC hour
	 */
	public static int getUtcHour(int hour, int timezone) {

		hour = (hour - timezone) % 24;
		if (hour < 0) {
			return 24 + hour;
		}

		return hour;
	}
}
