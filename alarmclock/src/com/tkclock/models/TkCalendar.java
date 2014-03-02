package com.tkclock.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.tkclock.utils.DateTimeUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorJoiner;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;

public class TkCalendar {
	// The indices for the projection array.
	private static final int ID_INDEX = 0;
	private static final int ACC_NAME_INDEX = 1;
	private static final int ACC_TYPE_INDEX = 2;
	
	private static final int CAL_ID_INDEX = 0;
	private static final int TITLE_INDEX = 1;
	private static final int LOCALTION_INDEX = 2;
	private static final int DTSTART_INDEX = 3;
	private static final int DTEND_INDEX = 4;
	private static final int TZSTART_INDEX = 5;
	private static final int TZEND_INDEX = 6;
	
	private static final String _ID = "_id";                    		// 0
	private static final String _ACCOUNT_NAME = "account_name";			// 1
	private static final String _ACCOUNT_TYPE = "account_type";			// 2
	
	private static final String _CALENDAR_ID = "calendar_id";           // 0
	private static final String _TITLE = "title";                  		// 1
	private static final String _LOCATION = "eventLocation";         	// 2
	private static final String _DTSTART = "dtstart";					// 3
	private static final String _DTEND = "dtend";						// 4
	private static final String _EVENT_TIMEZONE = "eventTimezone";		// 5
	private static final String _EVENT_END_TIMEZONE	= "eventEndTimezone";// 6
	
	private static final String[] CAL_PROJECTION = new String[] {
		_ID, _ACCOUNT_NAME, _ACCOUNT_TYPE,
	};
	
	private static final String[] EVENT_PROJECTION = new String[] {
		_CALENDAR_ID, _TITLE, _LOCATION, _DTSTART, _DTEND, 
//	    _EVENT_TIMEZONE, _EVENT_END_TIMEZONE
	};
	  	
	private static final String[] JOIN_PROJECTION = new String[] {
		_ACCOUNT_NAME, _TITLE, _LOCATION, _DTSTART, _DTEND, 
//	    _EVENT_TIMEZONE, _EVENT_END_TIMEZONE
	};

	Context mContext;
	CalendarContract mCalendarManager;
	Uri mCalendarUri;
	Uri mEventUri;
	ContentResolver mCr;
	
	public TkCalendar(Context context) {
		mContext = context;
		// Run query
		mCr = mContext.getContentResolver();
		getContentUri();
	}
	
	public List<String> getAllAccounts(String account_type) {
		List<String> accounts = new ArrayList<String>();
		Cursor calCur = null;
		String account = "";
		String selection = null;
		String[] selectionArgs = null;
		
		if(account_type == null || account_type == "") {
			account_type = "com.google";
		}
		
		selection = "(" + _ACCOUNT_TYPE + " = ?)";
		selectionArgs = new String[]{account_type};
		
		calCur = mCr.query(mCalendarUri, CAL_PROJECTION, selection, selectionArgs, null);
		while(calCur.moveToNext()) {
			account = calCur.getString(ACC_NAME_INDEX);
			accounts.add(account);
		}
		
		return accounts;
	}
	
	public List<String> readEvents(Date startDate, Date endDate, List<String> acc_lists) {
		Cursor eventCur = null;
		Cursor calCur = null;
		List<String> eventDesc = new ArrayList<String>();
		
		long start = startDate.getTime();
		long end = endDate.getTime();
		
		String time_selection = "(" + _DTSTART + " >= ?) AND " +
								"(" + _DTEND + " <= ?)";
		String account_selection = "";
		for(int i = 0; i < acc_lists.size(); i++) {
			account_selection += (i != 0 ? " OR " : "") + "(" + _ACCOUNT_NAME + " = ?)";
		}
		
		String[] timeSelArgs = new String[] {Long.toString(start), Long.toString(end)};
		String[] accSelArgs = new String[0];
		accSelArgs = acc_lists.toArray(accSelArgs);

		// Submit the query and get a Cursor object back. 
		eventCur = mCr.query(mEventUri, EVENT_PROJECTION, time_selection, timeSelArgs, null);
		calCur = mCr.query(mCalendarUri, CAL_PROJECTION, account_selection, accSelArgs, null);
		
		CursorJoiner joiner = new CursorJoiner(	eventCur, 
													new String[] {_CALENDAR_ID}, 
													calCur, 
													new String[] {_ID});

		MatrixCursor cursor = new MatrixCursor(JOIN_PROJECTION);
		for (CursorJoiner.Result joinerResult : joiner) { 
            switch (joinerResult) { 
                case BOTH: 
                    String name = calCur.getString(ACC_NAME_INDEX); 
                    String title = eventCur.getString(TITLE_INDEX);
                    String location = eventCur.getString(LOCALTION_INDEX);
                    long start_time = eventCur.getLong(DTSTART_INDEX);
                    long end_time = eventCur.getLong(DTEND_INDEX);
                    cursor.addRow(new String[] {name, title, location, Long.toString(start_time), Long.toString(end_time)}); 
                    break; 
                default:
                	break;
            } 
		} 
		
		String desc = "";
		while(cursor.moveToNext()) {
			desc = genEventDesc(cursor);
			eventDesc.add(desc);
		}
		
		return eventDesc;
	}
	
	private String genEventDesc(Cursor cur) {
		String description = "";
		String title = "";
		String location = "";
		Date start_time, end_time;
		long start = 0;
		long end = 0;
		
		title = cur.getString(TITLE_INDEX);
		location = cur.getString(LOCALTION_INDEX);
		start = cur.getLong(DTSTART_INDEX);
		end = cur.getLong(DTEND_INDEX);
		
		start_time = DateTimeUtils.int2Date((long)start);
		end_time = DateTimeUtils.int2Date((long)end);
		Calendar calendar = Calendar.getInstance();
		
		calendar.setTime(start_time);
		start = (long)calendar.get(Calendar.HOUR);
		String start_am_pm = calendar.get(Calendar.AM_PM) == Calendar.PM ? " PM" : " AM";
		
		calendar.setTime(end_time);
		end = calendar.get(Calendar.HOUR);
		String end_am_pm = calendar.get(Calendar.AM_PM) == Calendar.PM ? " PM" : " AM";
		
		description = title + 
					" at " + location + 
					" from " + start + start_am_pm +
					" to " + end + end_am_pm;
		
		return description;
	}
	
	private void getContentUri() {
		String calendar_uri_path = "";
		String event_uri_path = "";
		
		calendar_uri_path = "content://com.android.calendar/calendars";
		event_uri_path = "content://com.android.calendar/events";
		
		mCalendarUri = Uri.parse(calendar_uri_path);
		mEventUri = Uri.parse(event_uri_path);
	}
}
