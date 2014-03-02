package com.tkclock.dashboard;

import static com.tkclock.dashboard.TkApplication.KEY_PREFERENCE_CAL_ACCS;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.facebook.SessionState;
import com.tkclock.dashboard.R;
import com.tkclock.models.TkCalendar;
import com.tkclock.models.TkFacebook;
import com.tkclock.models.TkFacebook.OnSessionStateChange;
import com.tkclock.onalarm.OnAlarmReceiver;
import com.tkclock.onalarm.TkAlarmNotify;
import com.tkclock.settings.GeneralSetting;
import com.tkclock.settings.TkSettings;
import com.tkclock.stopwatch.TkStopWatchFragment;
import com.tkclock.timer.TkTimerFragment;
import com.tkclock.utils.StringUtils;
import com.tkclock.voice.notify_manager.TkVoiceCmd;
import com.tkclock.worldclock.TkWorldClockFragment;
import com.tkclock.adapters.TkPageFragAdapter;
import com.tkclock.alarm.TkAlarmFragment;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;

public class MainActivity extends FragmentActivity 
	implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener, OnSessionStateChange{
	
	private static final String TAG = "TkClock MainActivity";
	private static int NUMBER_TABS = 4;
	private TkPageFragAdapter mPagerAdapter;
	ViewPager mViewPager;
	LayoutInflater mInflater;
	private TabHost mTabHost;
	
	TkFacebook mFbService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
		// Initialise the TabHost
        initialiseTabHost(savedInstanceState);
        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
        }
        // Intialise ViewPager
        intialiseViewPager();
        
        mFbService = TkFacebook.getService(this);
        mFbService.onCreate(savedInstanceState);
        
        TkCalendar calendar = new TkCalendar(this);
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.DAY_OF_MONTH, 16);
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        
        Date start = instance.getTime();
        
        instance.set(Calendar.HOUR_OF_DAY, 23);
        instance.set(Calendar.MINUTE, 59);
        Date end = instance.getTime();
        
        SharedPreferences preferences =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String selected_accs = preferences.getString(KEY_PREFERENCE_CAL_ACCS, "");
        List<String> acc_lists = StringUtils.SplitUsingToken(selected_accs, ";");
        if(calendar != null) {
        	calendar.readEvents(start, end, acc_lists);
        }
        
//        createNotification();
	}
	
	protected void onSaveInstanceState(Bundle outState) {
        outState.putString("tab", mTabHost.getCurrentTabTag()); //save the tab selected
        super.onSaveInstanceState(outState);
        mFbService.onSaveInstanceState(outState);
    }
	@Override
	public void onPause() {
	    super.onPause();
	    mFbService.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		mFbService.onResume();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    mFbService.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		mFbService.onActivityResult(requestCode, resultCode, data);
	}

	private void initialiseTabHost(Bundle args) {
		mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();
        
        Resources res = getResources();
        addTab(mTabHost, mTabHost.newTabSpec("Tab1").setIndicator(res.getText(R.string.tab_alarm_title)));
        addTab(mTabHost, mTabHost.newTabSpec("Tab2").setIndicator(res.getText(R.string.tab_worldclock_title)));
        addTab(mTabHost, mTabHost.newTabSpec("Tab3").setIndicator(res.getText(R.string.tab_stopwatch_title)));
        addTab(mTabHost, mTabHost.newTabSpec("Tab4").setIndicator(res.getText(R.string.tab_timer_title)));
        
        mTabHost.setOnTabChangedListener(this);
	}

	private void intialiseViewPager() {
		List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(Fragment.instantiate(this, TkAlarmFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, TkWorldClockFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, TkStopWatchFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, TkTimerFragment.class.getName()));
        
        mPagerAdapter  = new TkPageFragAdapter(getSupportFragmentManager(), fragments);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(NUMBER_TABS);
        mViewPager.setOnPageChangeListener(this);
	}
	
	private void addTab(TabHost mTabHost2, TabSpec setIndicator) {
		// Attach a Tab view factory to the spec
		setIndicator.setContent(new TabFactory(this));
        mTabHost2.addTab(setIndicator);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()) {
		case R.id.action_login:
			mFbService = TkFacebook.getService(this);
			if(mFbService.isLogin() == false) {
	        	mFbService.login();
	        }
			break;
		case R.id.action_logout:
			mFbService = TkFacebook.getService(this);
			if(mFbService.isLogin() == true) {
				mFbService.logout();
			}
			break;
		case R.id.action_setting:
			Intent i = new Intent(MainActivity.this, TkSettings.class);
			startActivity(i);
			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageSelected(int position) {
		mTabHost.setCurrentTab(position);
	}

	@Override
	public void onTabChanged(String tag) {
        int pos = mTabHost.getCurrentTab();
        mViewPager.setCurrentItem(pos);
    }
	
	class TabFactory implements TabContentFactory {
		 
        private final Context mContext;
 
        /**
         * @param context
         */
        public TabFactory(Context context) {
            mContext = context;
        }
 
        /** (non-Javadoc)
         * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
         */
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }

	@Override
	public void onSessionStateChange(SessionState state) {
		// TODO Auto-generated method stub
		if(state.isOpened()) {
			Log.d(TAG, "Logged in");
			((TkApplication)getApplication()).setFbLoginState(true);
		}
	}
	
	private void createNotification() {
		Intent i = new Intent(this, TkAlarmNotify.class);
		PendingIntent p = PendingIntent.getActivity(this, 0, i, 0);
		
		Notification noti = new NotificationCompat.Builder(this)
	        .setContentTitle("New mail from " + "test@gmail.com")
	        .setContentText("Subject").setSmallIcon(android.R.drawable.btn_plus)
	        .setContentIntent(p)
	        .addAction(R.drawable.ic_action_add_alarm, "Call", p)
	        .addAction(R.drawable.ic_action_add_alarm, "More", p)
	        .addAction(R.drawable.ic_action_add_alarm, "And more", p).build();
	    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	    // hide the notification after its selected
	    noti.flags |= Notification.FLAG_AUTO_CANCEL;
	
	    notificationManager.notify(0, noti);	
	}
}
