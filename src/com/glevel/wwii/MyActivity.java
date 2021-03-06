package com.glevel.wwii;

import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.glevel.wwii.utils.MusicManager;
import com.glevel.wwii.utils.MusicManager.Music;
import com.google.analytics.tracking.android.EasyTracker;

public abstract class MyActivity extends FragmentActivity {

	protected boolean mContinueMusic = false;
	protected Music mMusic = Music.MUSIC_MENU;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// allow user to change the music volume with the phone's hardware
		// buttons
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// analytics
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// analytics
		EasyTracker.getInstance(this).activityStop(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!mContinueMusic) {
			MusicManager.pause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mContinueMusic = false;
		if (mMusic != null) {
			MusicManager.start(this, mMusic);
		}
	}

}
