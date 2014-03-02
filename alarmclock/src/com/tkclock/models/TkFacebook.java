package com.tkclock.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.tkclock.adapters.TkNoficationMng;
import com.tkclock.adapters.TkNoficationMng.NotifycationInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import static com.tkclock.dashboard.TkApplication.MSG_TYPE_FB_NOTIFICATION;

public class TkFacebook {
	protected static final String TAG = "TkFacebook";
	private static TkFacebook mService;
	private Session mSession;
	private UiLifecycleHelper uiHelper;
	private Activity mContext;
	
	List<NotifycationInfo> mNoficationResult;
	
	private OnSessionStateChange mListener;
	
	private Handler mWaitResHandler = null;
		
	public interface OnSessionStateChange {
		public void onSessionStateChange(SessionState state);
	}
	
	private TkFacebook(Context context) {
		mContext = (Activity) context;
		if(context instanceof OnSessionStateChange)
			mListener = (OnSessionStateChange) context;
		else
			mListener = null;
		uiHelper = new UiLifecycleHelper(mContext, stateCallback);
		mNoficationResult = null;
	}
	
	public static TkFacebook getService(Context context) {
		if(mService == null) {
			mService = new TkFacebook(context);
		}
		
		return mService;
	}
	
	public static boolean releaseService(Context context) {
		if(context != mService.mContext)
			return false;
		else {
			mService = null;
			return true;
		}
	}
	
	public void setHandler(Handler handler) {
		mWaitResHandler = handler;
	}
	
	public static boolean getLoginState() {
		if(mService != null)
			return mService.isLogin();
		else
			return false;
	}
	
	public boolean isLogin() {
		mSession = Session.getActiveSession();
		if(mSession == null)
			return false;
		else
			return mSession.isOpened();
	}
	
	public void login() {
		mSession = Session.getActiveSession();
		if(mSession == null) {
			mSession = new Session(mContext);
			Session.setActiveSession(mSession);
		}
		if(mSession.isOpened() == false)
			mSession.openForPublish(createOpenReq());
	}
	
	public void logout() {
	    if(mSession != null && mSession.isOpened()) {
			mSession.closeAndClearTokenInformation();
			Session.setActiveSession(null);
			mSession = null;
		}	
	}
	
	public void requestNotification() {
		queryFql(TkNoficationMng.getQueryFql());
	}
	
	public List<NotifycationInfo> getNofications() {
		return mNoficationResult;
	}
	
	public List<String> getNotificationString() {
		if(mNoficationResult == null)
			return null;
		
		String sentence = "";
		List<String> result = new ArrayList<String>();
		for(int i = 0; i < mNoficationResult.size(); i++) {
			sentence = mNoficationResult.get(i).getContent();
			result.add(sentence);
		}
		
		return result;
	}
	
	private void queryFql(String fqlQuery) {
        Bundle params = new Bundle();
        params.putString("q", fqlQuery);
        Session session = Session.getActiveSession();
        Request request = new Request(session, "/fql", params, HttpMethod.GET, queryCallback); 
        Request.executeBatchAsync(request); 
	}
	
	private Session.OpenRequest createOpenReq() {
		/* 
		 * Create a publish permission request
		 */
		Session.OpenRequest publishRequest = new Session.OpenRequest(mContext);
		publishRequest.setCallback(openReqCallback);
		List<String> permission = Arrays.asList("status_update", "manage_notifications");
		publishRequest.setPermissions(permission);

		return publishRequest;
	}
	
	/*
	 * Used by uiHelper to manage session state
	 */
	private Session.StatusCallback stateCallback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	    	if (state.isOpened()) {
		        Log.i(TAG, "Logged in...");
		    } else if (state.isClosed()) {
		        Log.i(TAG, "Logged out...");
		    }
	    	
	    	/*
	    	 *  Notify to GUI about the changed state
	    	 */
	    	if(mListener != null)
	    		mListener.onSessionStateChange(state);
	    }
	};
	
	/*
	 *  Used to handle request response from GUI
	 */
	private Session.StatusCallback openReqCallback = new Session.StatusCallback() {
		/*
		 * @see com.facebook.Session.StatusCallback#call(com.facebook.Session, com.facebook.SessionState, java.lang.Exception)
		 */
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			if(exception != null) {
				Log.d("error", exception.getMessage());
				return;
			}
			if (session.isOpened()) {
				
			} else if(session.isClosed()) {
				
			}
		}
	};
	
	private Request.Callback queryCallback = new Request.Callback()
	{         
        public void onCompleted(Response response) {
        	/* Parse response */
        	Calendar calendar = Calendar.getInstance();
        	Log.d(TAG, "Get Notification Completed: " + calendar.getTimeInMillis());
            mNoficationResult = TkNoficationMng.parseResponse(response);
            
            /* Inform completed status to listener */
            if(mWaitResHandler != null) {
            	Message msg = Message.obtain();
            	msg.arg1 = MSG_TYPE_FB_NOTIFICATION;
            	mWaitResHandler.sendMessage(msg);
            }
        }
	};
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}
	
	public void onCreate(Bundle savedInstanceState) {
		uiHelper.onCreate(savedInstanceState);
	}
	
	public void onPause() {
	    uiHelper.onPause();
	}

	public void onDestroy() {
	    uiHelper.onDestroy();
	}

	public void onSaveInstanceState(Bundle outState) {
	    uiHelper.onSaveInstanceState(outState);
	}
}
