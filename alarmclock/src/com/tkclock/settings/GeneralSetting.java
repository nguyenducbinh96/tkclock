package com.tkclock.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;

import com.tkclock.dashboard.R;
import com.tkclock.dashboard.TkFragment;
import com.tkclock.dashboard.TkFragmentActivity;

public class GeneralSetting extends TkFragmentActivity implements 
NavigationSetting.OnNaviSelectedListener {
	
	String[] navigation_items;
	Fragment oldFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_setting);
		
		View frameLayout = findViewById(R.id.fragment_container);
		navigation_items = getResources().getStringArray(R.array.setting_items);
		// Checking whether current used layout is for small or large screen
		// Case of small screen --> add navigation programmatically
		if(frameLayout != null) {
			NavigationSetting navigation = new NavigationSetting();
			navigation.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, navigation).commit();
		}
	}

	@Override
	public void onArticleSelected(int position) {
		// TODO Auto-generated method stub
		// Capture the article fragment from the activity layout
		FrameLayout setting_part = (FrameLayout) findViewById(R.id.setting_fragment);

    	FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (setting_part != null) {
            // If setting part is available, we're in two-pane layout...
        	String option = navigation_items[position];
        	
    		TkFragment newFragment = null;
    		if(option.contains("Alarm")) {
	            newFragment = new AlarmSettingFragment();
            } else if(option.contains("Facebook")) {
            	newFragment = new FbSettingFragment();
            } else
            	return;
    		
    		Bundle args = new Bundle();
            args.putString(AlarmSettingFragment.ARG_POSITION, navigation_items[position]);
            newFragment.setArguments(args);
            
            // Replace old setting fragment with the new one
            if(oldFragment != null)
            	transaction.remove(oldFragment);
            transaction.add(R.id.setting_fragment, newFragment);
            
            oldFragment = newFragment;

            // Commit the transaction
            transaction.commit();

        } else {
            // we are in one-pane layout
        	transaction.setCustomAnimations(R.anim.appear_from_right, R.anim.exit_to_left);
            // Create fragment and give it an argument for the selected article
            String option = navigation_items[position];
            TkFragment newFragment = null;
            if(option.contains("Alarm")) {
	            newFragment = new AlarmSettingFragment();
            } else if(option.contains("Facebook")) {
            	newFragment = new FbSettingFragment();
            } else
            	return;
            
            Bundle args = new Bundle();
            args.putString(AlarmSettingFragment.ARG_POSITION, navigation_items[position]);
            newFragment.setArguments(args);
            
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, newFragment);

            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        }
	}

}
