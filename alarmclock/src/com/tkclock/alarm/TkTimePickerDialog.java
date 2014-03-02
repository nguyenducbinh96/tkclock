package com.tkclock.alarm;

import android.app.TimePickerDialog;
import android.content.Context;

//Wrapper class to fix onStop() bug occur from android 4.1.2
public class TkTimePickerDialog extends TimePickerDialog {

	public TkTimePickerDialog(Context context, OnTimeSetListener callBack, int hourOfDay, int minute,
			boolean is24HourView) {
		super(context, callBack, hourOfDay, minute, is24HourView);
	}

	@Override
	protected void onStop() {
		// Doing nothing instead of involve super's function
	}
	
}
