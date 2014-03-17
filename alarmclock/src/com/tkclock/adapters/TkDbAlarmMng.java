package com.tkclock.adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import com.tkclock.utils.DateTimeUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings;
import android.text.format.DateFormat;

public class TkDbAlarmMng {
	
	public static final String TABLE_NAME = "alarm";
    
    public static final String KEY_DATE_TIME = "date_time"; 
    public static final String KEY_ROWID = "_id";
    public static final String KEY_ENABLE = "enable";
    public static final String KEY_REPEAT = "repeat";
    public static final String KEY_REPEAT_DAY = "repeat_day";
    public static final String KEY_RINGTONE_URI ="ringtones";
    
    public static final String DAY_SEPERATOR = ";";
    
    public static final String TABLE_CREATE = 
    "CREATE TABLE " + TABLE_NAME + " ("
            + KEY_ROWID + " INTEGER primary key,"
            + KEY_DATE_TIME + " TEXT not null, "
            + KEY_ENABLE + " INTEGER not null, " 
            + KEY_REPEAT + " INTEGER not null, "
            + KEY_REPEAT_DAY + " TEXT not null, "
            + KEY_RINGTONE_URI + " TEXT not null);";
	
	protected static final String LOG = "TkClock - AlarmDbMng";
	
	public static long DEFAULT_OPTION = -1;

	private final Context mContext;
	private SQLiteDatabase mDb;
	private DbHelper mDbHelper;
	
	public TkDbAlarmMng(Context context) {
		// Use the manger from global -> using global context
		mContext = context.getApplicationContext();
		mDbHelper = new DbHelper(mContext);
	}
	
	public DbHelper open() throws android.database.SQLException {
        mDbHelper = new DbHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();
        return mDbHelper;
    }
    
    public void close() {
		mDb.close();
	}
	
	public static class TkDbAlarm {
		public long m_id;
		public boolean m_repeat;
		public boolean m_enable;
		public String m_ringtone;
		public Date m_alarmtime;
		public List<String> m_repeat_days;
		
		public TkDbAlarm() {
			m_repeat = false;
			m_enable = true;
			m_repeat_days = new ArrayList<String>();
			m_ringtone = Settings.System.DEFAULT_ALARM_ALERT_URI.toString();
			m_alarmtime = Calendar.getInstance().getTime();
		}
	}
	
	public List<TkDbAlarm> getAlarms(long id) {
		List<TkDbAlarm> alarms = new ArrayList<TkDbAlarm>();
		Cursor cur = null;
		if(id == DEFAULT_OPTION)
			cur = fetAllAlarms();
		else
			cur = fetAlarm(id);
		
		if (cur != null && cur.moveToFirst()) {
	        do {
	        	TkDbAlarm alarm = new TkDbAlarm();
	        	
	        	alarm.m_id = cur.getLong(0);
	        	try {
					alarm.m_alarmtime = DateTimeUtils.str2Date(cur.getString(1));
				} catch (ParseException e) {
					e.printStackTrace();
				}
	        	alarm.m_enable = cur.getInt(2) == 1 ? true : false;
	        	alarm.m_repeat = cur.getInt(3) == 1 ? true : false;
	        	alarm.m_repeat_days = parseString(cur.getString(4), ";");
	        	alarm.m_ringtone = cur.getString(5);
	            
	            alarms.add(alarm);
	        } while (cur.moveToNext());
	    }
		
		return alarms;
	}
	
	private List<String> parseString(String src, String pattern) {
		List<String> options = new ArrayList<String>();
		
		Pattern p = Pattern.compile(pattern);
		p.split(src);
		
		return options;
	}
    
    public long createAlarm(TkDbAlarm alarm) {
		ContentValues content = new ContentValues();
		content.put(KEY_ENABLE, alarm.m_enable ? 1 : 0);
		content.put(KEY_REPEAT, alarm.m_repeat ? 1 : 0);
		String days ="";
		for(int i = 0; i < alarm.m_repeat_days.size(); i++)
			days = days + alarm.m_repeat_days.get(i) + DAY_SEPERATOR; 
		content.put(KEY_REPEAT_DAY, days);
		content.put(KEY_RINGTONE_URI, alarm.m_ringtone);
		content.put(KEY_DATE_TIME, DateTimeUtils.date2Str(alarm.m_alarmtime));
		long ret = mDb.insert(TABLE_NAME, null, content);
		alarm.m_id = ret;
		
		return ret;
	}
	public boolean deleteAlarm(long id){
		return mDb.delete(TABLE_NAME, KEY_ROWID + "=" + id, null) > 0;
	}
	public Cursor fetAllAlarms(){
		Cursor mCursor = mDb.query(TABLE_NAME, new String[]{KEY_ROWID, KEY_DATE_TIME, KEY_ENABLE, KEY_REPEAT, KEY_REPEAT_DAY, KEY_RINGTONE_URI}, 
				null, null, null, null, null);
		mCursor.moveToFirst();
		return mCursor;
	}
	public Cursor fetAlarm(long id){
		Cursor mCursor = mDb.query(TABLE_NAME, new String[]{KEY_ROWID, KEY_DATE_TIME, KEY_ENABLE, KEY_REPEAT, KEY_REPEAT_DAY, KEY_RINGTONE_URI}, 
				KEY_ROWID + "=" + id, null, null, null, null);
		if(mCursor != null)
			mCursor.moveToFirst();
		return mCursor;
	}
	public boolean updateAlarm(TkDbAlarm alarm){
		ContentValues content = new ContentValues();
		content.put(KEY_ENABLE, alarm.m_enable ? 1 : 0);
		content.put(KEY_REPEAT, alarm.m_repeat ? 1 : 0);
		
		String days ="";
		for(int i = 0; i < alarm.m_repeat_days.size(); i++)
			days = days + alarm.m_repeat_days.get(i) + DAY_SEPERATOR; 
		content.put(KEY_REPEAT_DAY, days);
		
		content.put(KEY_RINGTONE_URI, alarm.m_ringtone);
		content.put(KEY_DATE_TIME, DateTimeUtils.date2Str(alarm.m_alarmtime));
		return mDb.update(TABLE_NAME, content, KEY_ROWID + "=" + alarm.m_id, null) > 0;
	}
}
