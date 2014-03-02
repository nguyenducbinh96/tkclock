package com.tkclock.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tkclock.dashboard.R;
import com.tkclock.dashboard.TkFragment;

public class FbSettingFragment extends TkFragment {
	final static String ARG_POSITION = "position";
    int mCurrentPosition = -1;
    TextView mContent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {

        // If activity recreated (such as from screen rotate), restore
        // the previous article selection set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_facebook_setting, container, false);
        
        mContent = (TextView) view.findViewById(R.id.textContent);
		
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // During startup, check if there are arguments passed to the fragment.
        // onStart is a good place to do this because the layout has already been
        // applied to the fragment at this point so we can safely call the method
        // below that sets the article text.
        Bundle args = getArguments();
        String content;
        if (args != null) {
            // Set article based on argument passed in
        	content = args.getString(ARG_POSITION);
        } else {
        	content = "large screen";
        }
        mContent.setText(content);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putInt(ARG_POSITION, mCurrentPosition);
    }
}
