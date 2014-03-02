package com.tkclock.alarm;

import java.util.List;

import com.tkclock.adapters.TkDbAlarmMng;
import com.tkclock.dashboard.R;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class TkAlarmListAdapter extends BaseAdapter {
	List<TkDbAlarmMng.TkDbAlarm> mData;
	Fragment mContainer;
	LayoutInflater mInflater;
	
	public TkAlarmListAdapter(Fragment container, List<TkDbAlarmMng.TkDbAlarm> data) {
		mContainer = container;
		mData = data;
		mInflater = (LayoutInflater) mContainer.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		if(mData != null)
			return mData.size();
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TkDbAlarmMng.TkDbAlarm alarm = mData.get(position);
		
		if(convertView == null) {
			View alarm_content = mInflater.inflate(R.layout.alarm_info, parent, false);
			
			final CheckBox check_enable = (CheckBox) alarm_content.findViewById(R.id.alarm_enable);
			final CheckBox check_repeat = (CheckBox) alarm_content.findViewById(R.id.alarm_repeat);
			final LinearLayout repeat_opt = (LinearLayout) alarm_content.findViewById(R.id.repeat_opt);
			
			check_enable.setChecked(alarm.m_enable);
			check_repeat.setChecked(alarm.m_repeat);
			repeat_opt.setVisibility(alarm.m_repeat ? View.VISIBLE : View.GONE);
			
			check_repeat.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean checked) {
					repeat_opt.setVisibility(checked ? View.VISIBLE : View.GONE);
				}
			});
			
			check_enable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				}
			});
		}
		return convertView;
	}
}
