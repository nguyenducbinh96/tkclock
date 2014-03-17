package com.tkclock.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String TIME_FORMAT = "HH:mm";
	
	public static String date2Str(Date date) {
    	SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
    	return sdf.format(date);
	}
	public static Date str2Date(String strDate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
		return sdf.parse(strDate);
	}
	public static String time2Str(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.US);
    	return sdf.format(date);
	}
	public static Date int2Date(long date) {
		return new Date(date);
	}
}
