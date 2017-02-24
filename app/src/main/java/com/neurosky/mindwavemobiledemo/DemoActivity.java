package com.neurosky.mindwavemobiledemo;


import com.neurosky.connection.TgStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * This activity is the man entry of this app. It demonstrates the usage of 
 * (1) TgStreamReader.redirectConsoleLogToDocumentFolder()
 * (2) TgStreamReader.stopConsoleLog()
 * (3) demo of getVersion
 */
public class DemoActivity extends Activity {
	private static final String TAG = DemoActivity.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//不要標題
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 螢幕不要關閉
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main_view);

		initView();
		// (1) Example of redirectConsoleLogToDocumentFolder()
		// Call redirectConsoleLogToDocumentFolder at the beginning of the app, it will record all the log.
		// Don't forget to call stopConsoleLog() in onDestroy() if it is the end point of this app.
		// If you can't find the end point of the app , you don't have to call stopConsoleLog()
		TgStreamReader.redirectConsoleLogToDocumentFolder();
		// (3) demo of getVersion
		Log.d(TAG,"lib version: " + TgStreamReader.getVersion());
	}

	private Button btn_game;
	private TextView txv_show;
	private ImageView igv_start,igv_story,igv_rule;
	public Boolean is_story = true;
	private ScrollView scv;

	private void initView() {

//		btn_game = (Button)findViewById(R.id.main_btn_game);
//
//		btn_game.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent it = new Intent(DemoActivity.this,GameActivity.class);
//				startActivity(it);
//			}
//		});
		scv = (ScrollView)findViewById(R.id.scv_show);

		scv.setVisibility(View.INVISIBLE);

		txv_show = (TextView)findViewById(R.id.main_txv_show);

		igv_start = (ImageView)findViewById(R.id.igv_Start);
		igv_start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = new Intent(DemoActivity.this,GameActivity.class);
				startActivity(it);
			}
		});

		igv_story = (ImageView)findViewById(R.id.igv_story);

		igv_story.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if(is_story) changeVisibility();
				else if(!is_story) {
					if(scv.getVisibility()==View.INVISIBLE){
						scv.setVisibility(View.VISIBLE);
						//動畫路徑設定(x1,x2,y1,y2)
						Animation am = new TranslateAnimation(0, 0, 500, 0);
						//動畫開始到結束的時間，2秒
						am.setDuration(500);
						// 動畫重覆次數 (-1表示一直重覆，0表示不重覆執行，所以只會執行一次)
						am.setRepeatCount(0);
						//將動畫寫入ImageView
						scv.setAnimation(am);
						//開始動畫
						am.startNow();
					}
					txv_show.setText(getResources().getString(R.string.story));
					is_story = true;
				}

			}
		});

		igv_rule = (ImageView)findViewById(R.id.igv_rule);

		igv_rule.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if(is_story) {
					if(scv.getVisibility()==View.INVISIBLE){
						scv.setVisibility(View.VISIBLE);
						//動畫路徑設定(x1,x2,y1,y2)
						Animation am = new TranslateAnimation(0, 0, 500, 0);
						//動畫開始到結束的時間，2秒
						am.setDuration(500);
						// 動畫重覆次數 (-1表示一直重覆，0表示不重覆執行，所以只會執行一次)
						am.setRepeatCount(0);
						//將動畫寫入ImageView
						scv.setAnimation(am);
						//開始動畫
						am.startNow();
					}
					txv_show.setText(getResources().getString(R.string.rule));
					is_story = false;
				}
				else{
					changeVisibility();
				}

			}
		});

	}
	public void changeVisibility(){

			if(scv.getVisibility() == View.INVISIBLE) {
				scv.setVisibility(View.VISIBLE);
				//動畫路徑設定(x1,x2,y1,y2)
				Animation am = new TranslateAnimation(0, 0, 500, 0);
				//動畫開始到結束的時間，2秒
				am.setDuration(500);
				// 動畫重覆次數 (-1表示一直重覆，0表示不重覆執行，所以只會執行一次)
				am.setRepeatCount(0);
				//將動畫寫入ImageView
				scv.setAnimation(am);
				//開始動畫
				am.startNow();
			}
			else if(scv.getVisibility() == View.VISIBLE) {


				//動畫路徑設定(x1,x2,y1,y2)
				Animation am = new TranslateAnimation(0, 0, 0, 500);
				//動畫開始到結束的時間，2秒
				am.setDuration(500);
				// 動畫重覆次數 (-1表示一直重覆，0表示不重覆執行，所以只會執行一次)
				am.setRepeatCount(0);
				//將動畫寫入ImageView
				scv.setAnimation(am);
				//開始動畫
				am.startNow();
				scv.setVisibility(View.INVISIBLE);
			}

	}



	@Override
	protected void onDestroy() {
		
		// (2) Example of stopConsoleLog()
		TgStreamReader.stopConsoleLog();
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}


}
