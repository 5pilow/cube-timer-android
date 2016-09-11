package com.avelsoft.cubetimer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CubeTimerActivity extends Activity {

	private static final int MENU_DELETE_SCORES = 0;
	private static final int MENU_DELETE_LAST_SCORE = 1;

	private static String TAG = "Cube Timer";

	private Handler handler;
	private Runnable loop;
	private Runnable timerStartRunnable;
	private long initialTime;
	private int finalTime;
	private TextView timer;
	private TextView scramble;
	private TextView bestView;
	private TextView average5View;
	private TextView average12View;
	private LinearLayout timeList;
	private boolean running = false;

	private boolean canStart = false;

	private final List<Integer> times = new ArrayList<Integer>();
	private int best = 0;
	private int average5 = 0;
	private int average12 = 0;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, TAG);

		// Fullscreen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Keep screen on
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.main);

		// Get views
		timer = (TextView) findViewById(R.id.time);
		scramble = (TextView) findViewById(R.id.scramble);
		bestView = (TextView) findViewById(R.id.best);
		average5View = (TextView) findViewById(R.id.average5);
		average12View = (TextView) findViewById(R.id.average12);
		timeList = (LinearLayout) findViewById(R.id.timelist);

		resetTimer();
		displayScramble();

		handler = new Handler();

		loop = new Runnable() {
			@Override
			public void run() {

				setTime();
				handler.postDelayed(loop, 63);
			}
		};

		timerStartRunnable = new Runnable() {
			@Override
			public void run() {
				timer.setTextColor(0xff00A803);
				canStart = true;
			}
		};
	}

	private void start() {
		initialTime = System.nanoTime();
		handler.post(loop);
	}

	private void stop() {
		// Set exact time
		setTime();

		// Add new time
		times.add(finalTime);

		// Add time to times list
		final TextView timeView = new TextView(this);
		timeView.setTextSize(20);
		timeView.setTextColor(Color.BLACK);
		timeView.setText(timeToString(finalTime));
		timeList.addView(timeView);

		// Compute averages and best
		computeAveragesAndBest();

		setBestView();
		setAverage5View();
		setAverage12View();

		displayScramble();

		handler.removeCallbacks(loop);
	}

	private void setTime() {
		finalTime = (int) ((System.nanoTime() - initialTime) / 10000000);

		timer.setText(timeToString(finalTime));
	}

	private void resetTimer() {
		timer.setText(timeToString(0));
		timer.setTextColor(Color.BLACK);
	}

	private void displayScramble() {
		scramble.setText(ScrambleGenerator.generate(3));
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (!running) {
				resetTimer();
				timer.setTextColor(Color.RED);

				canStart = false;

				handler.postDelayed(timerStartRunnable, 400);

			} else {
				stop();
				timer.setTextColor(Color.BLACK);
			}
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (!running) {

				if (canStart) {
					timer.setTextColor(Color.BLACK);
					start();
					running = true;
				} else {
					handler.removeCallbacks(timerStartRunnable);
					timer.setTextColor(Color.BLACK);
				}

			} else {
				running = false;
			}
		}

		return super.onTouchEvent(event);
	}

	private void computeAveragesAndBest() {

		// Best
		if (times.size() > 0) {
			int best = Integer.MAX_VALUE;
			for (int i = 0; i < times.size(); ++i) {
				if (times.get(i) < best)
					best = times.get(i);
			}
			this.best = best;
		} else {
			this.best = 0;
		}

		// Average 5
		int sum = 0;
		if (times.size() >= 5) {
			// On recupere les 5 derniers
			final List<Integer> timesList = new ArrayList<Integer>();
			for (int i = times.size() - 5; i < times.size(); i++) {
				timesList.add(times.get(i));
			}
			// On trie
			Collections.sort(timesList);
			// On tronque
			timesList.remove(4);
			timesList.remove(0);
			// On calcule la moyenne
			sum = 0;
			for (int i = 0; i < 3; i++) {
				sum += timesList.get(i);
			}
			average5 = sum / 3;
		}

		// Average 12
		sum = 0;
		if (times.size() >= 12) {
			final List<Integer> timesList = new ArrayList<Integer>();
			for (int i = times.size() - 12; i < times.size(); i++) {
				timesList.add(times.get(i));
			}
			Collections.sort(timesList);
			timesList.remove(11);
			timesList.remove(0);
			sum = 0;
			for (int i = 0; i < 10; i++) {
				sum += timesList.get(i);
			}
			average12 = sum / 10;
		}
	}

	private String timeToString(final int time) {

		String minuts = String.valueOf(time / 6000);
		if (minuts.length() == 1) {
			minuts = "0" + minuts;
		}
		String millis = String.valueOf(time % 100);
		if (millis.length() == 1)
			millis = "0" + millis;
		String seconds = String.valueOf(time / 100 % 60);
		if (seconds.length() == 1)
			seconds = "0" + seconds;

		if (minuts.equals("00")) {
			return seconds + "." + millis;
		} else {
			return minuts + ":" + seconds + "." + millis;
		}
	}

	private void deleteLastTime() {

		resetTimer();

		if (times.size() > 0) {
			times.remove(times.size() - 1);
		}
		if (timeList.getChildCount() > 0) {
			timeList.removeViewAt(timeList.getChildCount() - 1);
		}

		computeAveragesAndBest();

		setBestView();
		setAverage5View();
		setAverage12View();

		handler.removeCallbacks(loop);
		running = false;
	}

	private void deleteTimes() {

		resetTimer();

		timeList.removeAllViews();
		times.clear();

		best = 0;
		average5 = 0;
		average12 = 0;

		setBestView();
		setAverage5View();
		setAverage12View();

		handler.removeCallbacks(loop);
		running = false;
	}

	private void setBestView() {
		if (best != 0)
			bestView.setText(timeToString(best));
		else
			bestView.setText("-");
	}

	private void setAverage5View() {
		if (average5 != 0)
			average5View.setText(timeToString(average5));
		else
			average5View.setText("-");
	}

	private void setAverage12View() {
		if (average12 != 0)
			average12View.setText(timeToString(average12));
		else
			average12View.setText("-");
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		menu.add(0, MENU_DELETE_SCORES, 0, "Delete scores").setIcon(android.R.drawable.ic_menu_delete);
		menu.add(0, MENU_DELETE_LAST_SCORE, 0, "Delete last score").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case (MENU_DELETE_SCORES):
			deleteTimes();
			return true;

		case (MENU_DELETE_LAST_SCORE):
			deleteLastTime();
			return true;
		}
		return false;
	}
}