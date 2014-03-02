package com.tkclock.adapters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.facebook.Response;

public class TkNoficationMng {
	private static final String TAG = "Notification Manager";
	private static String KEY_OBJ_TYPE = "object_type";
	private static String KEY_CREATED_TIME = "created_time";
	private static String KEY_UPDATED_TIME = "updated_time";
	private static String KEY_TITLE_TEXT = "title_text";
	private static String KEY_TITLE_HTML = "title_html";
	private static String KEY_BODY_TEXT = "body_text";
	private static String KEY_BODY_HTML = "body_html";
	
	private static String NOFICATION_TABLE = "notification";

	public static String getQueryFql () {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DATE, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		String keys = 	KEY_OBJ_TYPE + "," +
						KEY_CREATED_TIME + "," +
						KEY_UPDATED_TIME + "," +
						KEY_TITLE_TEXT + "," +
						KEY_BODY_TEXT;
		String condition = "recipient_id = me() AND updated_time > " + (long)calendar.getTimeInMillis()/1000;
		
		String fqlQuery = "SELECT " + keys +" FROM " + NOFICATION_TABLE +
				" WHERE " + condition + "ORDER BY " + KEY_UPDATED_TIME;
		
		Log.d(TAG, "Query: " + fqlQuery);
		return fqlQuery;
	}
	
	public static List<TkNoficationMng.NotifycationInfo> parseResponse(Response response) {
		JSONArray jResults;
		String object_type;
		long created_time;
		long updated_time;
		String title_text;
		String title_html;
		String body_text;
		String body_html;
		
		List<TkNoficationMng.NotifycationInfo> nofitications = new ArrayList<TkNoficationMng.NotifycationInfo>();
		try {
			jResults = response.getGraphObject().getInnerJSONObject().getJSONArray("data");
			JSONObject jObj;
			for(int i = 0; i < jResults.length(); i++) {
				jObj = jResults.getJSONObject(i);
				object_type = jObj.getString(KEY_OBJ_TYPE);
				created_time = jObj.getLong(KEY_CREATED_TIME);
				updated_time = jObj.getLong(KEY_UPDATED_TIME);
				title_text = jObj.getString(KEY_TITLE_TEXT);
				body_text = jObj.getString(KEY_BODY_TEXT);
				
				nofitications.add(new TkNoficationMng.NotifycationInfo(object_type, title_text, body_text, created_time, updated_time));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return nofitications;
	}
	
	public static class NotifycationInfo {
		String m_object_type;
		long m_created_time;
		long m_updated_time;
		String m_title_text;
		String m_title_html;
		String m_body_text;
		String m_body_html;
		
		public NotifycationInfo(String obj_type, String title_text, String body_text, long created_time, long updated_time) {
			m_object_type = obj_type;
			m_title_text = title_text;
			m_body_text = body_text;
			m_created_time = created_time;
			m_updated_time = updated_time;
		}
		
		public String getTitle() {
			return m_title_text;
		}
		
		public String getContent() {
			// TODO: Setting which information will be spoken
			return m_title_text;
		}
	}
}
