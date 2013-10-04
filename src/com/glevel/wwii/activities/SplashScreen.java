package com.glevel.wwii.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.glevel.wwii.R;
import com.glevel.wwii.WWApplication.FONTS;
import com.glevel.wwii.utils.ApplicationUtils;
import com.glevel.wwii.utils.WWActivity;

public class SplashScreen extends WWActivity {

	private static final int DELAY_AFTER_ANIMATION = 300,
			DELAY_BEFORE_ANIMATION = 200;
	private ViewGroup mTitleLayout;
	private Animation mLetterAnimation, mBounceAnimation;
	private int mCurrentAnimationPlaying = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// increase number of launches
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		int nbLaunches = prefs.getInt(ApplicationUtils.PREFS_NB_LAUNCHES, 0);
		prefs.edit().putInt(ApplicationUtils.PREFS_NB_LAUNCHES, ++nbLaunches);

		// do not show splashscreen after a few launches
		if (true || nbLaunches >= ApplicationUtils.NB_LAUNCHES_SPLASHSCREEN_APPEARS) {
			goToHomeScreen();
		}

		setContentView(R.layout.activity_splashscreen);
		setupUI();

		prepareSplashScreenTitle();

	}

	@Override
	protected void onStart() {
		super.onStart();
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				startNextFallingLetterAnimation();
			}
		}, DELAY_BEFORE_ANIMATION);
	}

	private void setupUI() {
		mTitleLayout = (ViewGroup) findViewById(R.id.title);

		mBounceAnimation = AnimationUtils.loadAnimation(this,
				R.anim.bounce_effect);

		mLetterAnimation = AnimationUtils.loadAnimation(this,
				R.anim.falling_letter);
		mLetterAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// start next letter animation and the general bounce effect
				startNextFallingLetterAnimation();
				mTitleLayout.startAnimation(mBounceAnimation);
			}
		});
	}

	private void prepareSplashScreenTitle() {
		// create text views for each letter of the title
		String appMaker = getString(R.string.app_maker);
		for (int n = 0; n < appMaker.length(); n++) {
			char c = appMaker.charAt(n);
			TextView letterTV = new TextView(this);
			letterTV.setTextAppearance(this, R.style.SplashScreenTitle);
			letterTV.setText("" + c);
			letterTV.setVisibility(View.GONE);
			letterTV.setTypeface(FONTS.splash);
			mTitleLayout.addView(letterTV);
		}
	}

	private void startNextFallingLetterAnimation() {
		if (mCurrentAnimationPlaying < mTitleLayout.getChildCount()) {
			// reset previous view's animation
			if (mCurrentAnimationPlaying > 0) {
				mTitleLayout.getChildAt(mCurrentAnimationPlaying - 1)
						.setAnimation(null);
			}
			// play next animation
			mTitleLayout.getChildAt(mCurrentAnimationPlaying).setVisibility(
					View.VISIBLE);
			mTitleLayout.getChildAt(mCurrentAnimationPlaying).startAnimation(
					mLetterAnimation);
			mCurrentAnimationPlaying++;
		} else {
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					goToHomeScreen();
				}
			}, DELAY_AFTER_ANIMATION);
		}
	}

	private void goToHomeScreen() {
		Intent intent = new Intent(SplashScreen.this, Home.class);
		startActivity(intent);
		finish();
	}

}
