package com.tkclock.worldclock;

import com.tkclock.dashboard.R;
import com.tkclock.dashboard.R.id;
import com.tkclock.dashboard.R.layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TkWorldClockFragment extends Fragment {
	static int count = 0;
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.worldclock_frag_layout, container, false);
		
		final TextView text = (TextView) view.findViewById(R.id.textContent);
		Button btnUpdate = (Button) view.findViewById(R.id.btnTest);
		btnUpdate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				text.setText(text.getText().toString() + ": " + count++);
			}
		});
		
		return view;
	}
}
