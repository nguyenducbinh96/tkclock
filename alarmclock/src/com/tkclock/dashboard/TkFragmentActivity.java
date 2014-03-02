package com.tkclock.dashboard;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;

public class TkFragmentActivity extends FragmentActivity {
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		this.overridePendingTransition(R.anim.appear_from_left, R.anim.exit_to_right);
	}
	
	@Override
	public void startActivity(Intent intent) {
		// TODO Auto-generated method stub
		// tranfer activity
		super.startActivity(intent);
		this.overridePendingTransition(R.anim.appear_from_right, R.anim.exit_to_left);
	}
}
