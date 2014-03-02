package com.tkclock.settings;

import java.util.ArrayList;
import java.util.List;

import com.tkclock.dashboard.R;
import com.tkclock.models.TkCalendar;
import com.tkclock.utils.StringUtils;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;

import static com.tkclock.dashboard.TkApplication.KEY_PREFERENCE_CAL_ACCS;

public class SelectAccDialog extends DialogFragment {
	public static int REQ_CODE = 1;
	private boolean m_change_activity = false;
	
	private NoticeDialogListener mListener;
	List<String> mAccs_selected;
	
	SharedPreferences mPref;
	SharedPreferences.Editor mPrefEditor;
	
	public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogStop(boolean change_activity);
    }

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		
		// Get share preference
		mPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		mPrefEditor = mPref.edit();

		// Load shared value
		String selected_accs = mPref.getString(KEY_PREFERENCE_CAL_ACCS, "");
		mAccs_selected = StringUtils.SplitUsingToken(selected_accs, ";");
		TkCalendar calendar = new TkCalendar(getActivity().getApplicationContext());
		final List<String> all_accounts = calendar.getAllAccounts("com.google");
		
		// Convert to pass to dialog
		final String[] account_name = all_accounts.toArray(new String[0]);
		final boolean[] selection = new boolean[all_accounts.size()];
		List<String> accounts_tmp = new ArrayList<String>(all_accounts);
		
		for(int i = 0; i < all_accounts.size(); i++) {
			if(mAccs_selected.contains(all_accounts.get(i))) {
				selection[i] = true;
			} else {
				selection[i] = false;
				accounts_tmp.set(i, "");
			}
		}
		mAccs_selected = accounts_tmp;
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.calendar_acc_opt)
	           .setMultiChoiceItems(account_name, selection, new DialogInterface.OnMultiChoiceClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						String acc = isChecked ? all_accounts.get(which) : "";
						mAccs_selected.set(which, acc);
					}
				})	
               .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // FIRE ZE MISSILES!
                	   List<String> accs = mAccs_selected;
                	   for(int i = 0; i < accs.size(); i++) {
                		   if(accs.get(i) == "") {
                			   accs.remove(i);
                			   i--;
                		   }
                	   }
                	   String seleted = StringUtils.list2String(accs, null);
                	   mPrefEditor.putString(KEY_PREFERENCE_CAL_ACCS, seleted);
                	   mPrefEditor.commit();
                	   mListener.onDialogPositiveClick(SelectAccDialog.this);
                   }
               })
               .setNeutralButton(R.string.add_more_acc, new DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO If support multiple calendar account types
//						Intent i = new Intent(Settings.ACTION_ADD_ACCOUNT);
//						i.putExtra("account_types", "com.google");
//						getActivity().startActivityForResult(i, REQ_CODE);
						
						// Support google account only
						String account_type = "com.google";
						AccountManager acm = AccountManager.get(getActivity().getApplicationContext());
						acm.addAccount(account_type, null, null, null, getActivity(), null, null);
						
						// Switching activity flag
						m_change_activity = true;
					}
				})
               .setNegativeButton(android.R.string.cancel, null);
        // Create the AlertDialog object and return it
        return builder.create();
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
	}
	

	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		super.onCancel(dialog);
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// TODO Auto-generated method stub
		super.onDismiss(dialog);
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(arg0);
	}

	@Override
	public void onStop() {
		super.onStop();
		
		mListener.onDialogStop(m_change_activity);
		m_change_activity = false;
	}

	public boolean noAccSeleted() {
		return mAccs_selected.size() == 0;
	}
}
