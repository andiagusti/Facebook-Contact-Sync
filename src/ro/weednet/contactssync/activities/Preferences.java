/*
 * Copyright (C) 2012 Danut Chereches
 *
 * Contact: Danut Chereches <admin@weednet.ro>
 *
 * This file is part of Facebook Contact Sync.
 * 
 * Facebook Contact Sync is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Facebook Contact Sync.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */
package ro.weednet.contactssync.activities;

import com.appbrain.AppBrain;

import ro.weednet.ContactsSync;
import ro.weednet.contactssync.Constants;
import ro.weednet.contactssync.R;
import ro.weednet.contactssync.authenticator.AuthenticatorActivity;
import ro.weednet.contactssync.client.RawContact;
import ro.weednet.contactssync.preferences.GlobalFragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;

public class Preferences extends Activity {
	public final static ContactsSync.SyncType DEFAULT_SYNC_TYPE = ContactsSync.SyncType.MEDIUM;
	public final static int DEFAULT_SYNC_FREQUENCY = 24;//hours
	public final static int DEFAULT_PICTURE_SIZE = RawContact.IMAGE_SIZES.MAX_SQUARE;
	public final static boolean DEFAULT_SYNC_ALL = false;
	public final static boolean DEFAULT_SYNC_WIFI_ONLY = false;
	public final static boolean DEFAULT_JOIN_BY_ID = false;
	public final static boolean DEFAULT_SYNC_BIRTHDAYS = false;
	public final static int DEFAULT_BIRTHDAY_FORMAT = RawContact.BIRTHDAY_FORMATS.GLOBAL;
	public final static boolean DEFAULT_SYNC_STATUSES = true;
	public final static boolean DEFAULT_SYNC_EMAILS = true;
	public final static boolean DEFAULT_SHOW_NOTIFICATIONS = false;
	public final static int DEFAULT_CONNECTION_TIMEOUT = 60;
	public final static boolean DEFAULT_DISABLE_ADS = false;
	
	private Dialog mAuthDialog;
	private GlobalFragment mFragment;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.preferences);
		
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		mFragment = new GlobalFragment();
		ft.replace(R.id.settings, mFragment);
		ft.commit();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		ContactsSync app = ContactsSync.getInstance();
		
		AccountManager am = AccountManager.get(this);
		Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
		
		if (accounts.length > 0) {
			//TODO: use current/selected account (not the first one)
			// Log.d("pref-bundle", icicle != null ? icicle.toString() : "null");
			mFragment.setAccount(accounts[0]);
			if (ContentResolver.getSyncAutomatically(accounts[0], ContactsContract.AUTHORITY)) {
				if (app.getSyncFrequency() == 0) {
					app.setSyncFrequency(Preferences.DEFAULT_SYNC_FREQUENCY);
					app.savePreferences();
					ContentResolver.addPeriodicSync(accounts[0], ContactsContract.AUTHORITY, new Bundle(), Preferences.DEFAULT_SYNC_FREQUENCY * 3600);
				}
			} else {
				if (app.getSyncFrequency() > 0) {
					app.setSyncFrequency(0);
					app.savePreferences();
				}
			}
		} else {
			if (mAuthDialog != null) {
				mAuthDialog.dismiss();
			}
			
			mAuthDialog = new Dialog(this);
			mAuthDialog.setContentView(R.layout.not_account_actions);
			mAuthDialog.setTitle("Select option");
			((Button) mAuthDialog.findViewById(R.id.add_account_button)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mAuthDialog.dismiss();
					Intent intent = new Intent(Preferences.this, AuthenticatorActivity.class);
					startActivity(intent);
				}
			});
			((Button) mAuthDialog.findViewById(R.id.exit_button)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mAuthDialog.dismiss();
					Preferences.this.finish();
				}
			});
			mAuthDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					mAuthDialog.dismiss();
					Preferences.this.finish();
				}
			});
			mAuthDialog.show();
		}
	}
	
	@Override
	public void onBackPressed() {
		ContactsSync app = ContactsSync.getInstance();
		
		if (!app.getDisableAds()) {
			AppBrain.getAds().maybeShowInterstitial(this);
		}
		
		finish();
	}
}
