package com.neurosky.mindwavemobiledemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.EEGPower;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;
import com.neurosky.connection.DataType.MindDataType;


public class GameActivity extends Activity implements SensorEventListener {

    private static final String TAG = "GameActivity";
    private TgStreamReader tgStreamReader;

    private BluetoothAdapter mBluetoothAdapter;

    private int badPacketCount = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_game);


        //  判斷藍芽 有沒有開啟

        try {
            // (1) Make sure that the device supports Bluetooth and Bluetooth is on
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "若要繼續進行遊戲，請開啟藍芽！！", Toast.LENGTH_LONG).show();
                finish();
//				return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.getMessage());
            return;
        }

        //  開啟後  開始設定

        // Example of constructor public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
        tgStreamReader = new TgStreamReader(mBluetoothAdapter,callback);
        // (2) Demo of setGetDataTimeOutTime, the default time is 5s, please call it before connect() of connectAndStart()
        tgStreamReader.setGetDataTimeOutTime(6);
        // (3) Demo of startLog, you will get more sdk log by logcat if you call this function
        tgStreamReader.startLog();

        init();

        start(null);




    }

    private TextView txv_signal,txv_medation,txv_attenation,txv_coderow;
    private LinearLayout wave_layout;



    LinearLayout game_layout;

    DrawGameView dgv;

    SoundPool sp;
    int sp_jump,sp_gameover,sp_fire;

    public int attenaion = 26;

    public int jumpcount = 15;

    public int medation_power = 0;

    public void init(){
        initview();
        setUpDrawWaveView();
        initSensor();
        initSoundPool();
    }
    public void initview(){

        //  遊戲的layout
        game_layout = (LinearLayout)findViewById(R.id.game_lin_gameVIew);

        dgv = new DrawGameView(getApplicationContext(),this);

        game_layout.addView(dgv, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        dgv.post(dgv.updateRunnable);


        // 腦波儀的顯示text
        txv_signal = (TextView)findViewById(R.id.game_txv_signal);
        txv_medation = (TextView)findViewById(R.id.game_txv_medation);
        txv_attenation = (TextView)findViewById(R.id.game_txv_attenation);
        txv_coderow = (TextView)findViewById(R.id.game_txv_coderow);

        // 波形layout
        wave_layout = (LinearLayout) findViewById(R.id.wave_layout);

        //
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);


        Bitmap cloud = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cloud3);


        cloud = zoomImage(cloud,metrics.widthPixels,metrics.heightPixels);
        //cloud = zoomImage(cloud,100,200);

        //Drawable d = new BitmapDrawable(getResources(), cloud);


    }
    public void initSoundPool(){
        sp = new SoundPool(3, AudioManager.STREAM_MUSIC,5);
        sp_jump = sp.load(this,R.raw.jump,1);
        sp_gameover = sp.load(this,R.raw.gameover,1);
        sp_fire = sp.load(this,R.raw.fire,1);
    }


    /////////test

    public static Bitmap zoomImage(Bitmap bgimage, double newWidth, double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int)width, (int)height,matrix, true);

        return bitmap;
    }

    public void go(View v){
        dgv.role.jump(jumpcount);
    }

    public void start(View v){
        badPacketCount = 0;

        // (5) demo of isBTConnected
        if(tgStreamReader != null && tgStreamReader.isBTConnected()){

            // Prepare for connecting
            tgStreamReader.stop();
            tgStreamReader.close();
        }

        // (4) Demo of  using connect() and start() to replace connectAndStart(),
        // please call start() when the state is changed to STATE_CONNECTED
        tgStreamReader.connect();
    }

    /////////

    public void stop() {
        if(tgStreamReader != null){
            tgStreamReader.stop();
            tgStreamReader.close();
        }
    }

    @Override
    protected void onDestroy() {
        //(6) use close() to release resource
        if(tgStreamReader != null){
            tgStreamReader.close();
            tgStreamReader = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stop();
    }

    // 設定 波形圖
    DrawWaveView waveView = null;

    public void setUpDrawWaveView() {
        waveView = new DrawWaveView(getApplicationContext());
        wave_layout.addView(waveView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        waveView.setValue(100, 100, 0);
    }

    public void updateWaveView(int data) {
        if (waveView != null) {
            waveView.updateData(data);
        }
    }
    public void updateWaveViewForMedation(int data){
        if (waveView != null) {
            waveView.updateDataformediation(data);
        }
    }
    // (7) demo of TgStreamHandler
    private TgStreamHandler callback = new TgStreamHandler() {

        @Override
        public void onStatesChanged(int connectionStates) {
            // TODO Auto-generated method stub
            Log.d(TAG, "connectionStates change to: " + connectionStates);
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTING:
                    // Do something when connecting
                    break;
                case ConnectionStates.STATE_CONNECTED:
                    // Do something when connected
                    tgStreamReader.start();
                    showToast("Connected", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_WORKING:
                    // Do something when working

                    //(9) demo of recording raw data , stop() will call stopRecordRawData,
                    //or you can add a button to control it.
                    //You can change the save path by calling setRecordStreamFilePath(String filePath) before startRecordRawData
                    tgStreamReader.startRecordRawData();

                    break;
                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    // Do something when getting data timeout

                    //(9) demo of recording raw data, exception handling
                    tgStreamReader.stopRecordRawData();

                    showToast("Get data time out!", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_STOPPED:
                    // Do something when stopped
                    // We have to call tgStreamReader.stop() and tgStreamReader.close() much more than
                    // tgStreamReader.connectAndstart(), because we have to prepare for that.

                    break;
                case ConnectionStates.STATE_DISCONNECTED:
                    // Do something when disconnected
                    break;
                case ConnectionStates.STATE_ERROR:
                    // Do something when you get error message
                    break;
                case ConnectionStates.STATE_FAILED:
                    // Do something when you get failed message
                    // It always happens when open the BluetoothSocket error or timeout
                    // Maybe the device is not working normal.
                    // Maybe you have to try again
                    break;
            }
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_STATE;
            msg.arg1 = connectionStates;
            LinkDetectedHandler.sendMessage(msg);
        }

        @Override
        public void onRecordFail(int flag) {
            // You can handle the record error message here
            Log.e(TAG,"onRecordFail: " +flag);

        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
            // You can handle the bad packets here.
            badPacketCount ++;
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_BAD_PACKET;
            msg.arg1 = badPacketCount;
            LinkDetectedHandler.sendMessage(msg);

        }

        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            // You can handle the received data here
            // You can feed the raw data to algo sdk here if necessary.

            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = datatype;
            msg.arg1 = data;
            msg.obj = obj;
            LinkDetectedHandler.sendMessage(msg);

            //Log.i(TAG,"onDataReceived");
        }

    };

    private boolean isPressing = false;
    private static final int MSG_UPDATE_BAD_PACKET = 1001;
    private static final int MSG_UPDATE_STATE = 1002;

    int raw;
    private Handler LinkDetectedHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // (8) demo of MindDataType
            switch (msg.what) {
                case MindDataType.CODE_RAW:
                    //txv_coderow.setText("Row:" + msg.arg1 + "");
                    if(msg.arg1<-300) {
                        go(null);
                    }
                    break;
                case MindDataType.CODE_MEDITATION:
                    //Log.d(TAG, "HeadDataType.CODE_MEDITATION " + msg.arg1);
                    updateWaveViewForMedation(msg.arg1);
                    if(msg.arg1>80){
                        medation_power+=1;
                    }
                    if(medation_power>3){
                        get(null);
                        medation_power = 0;
                    }
                    txv_medation.setText("Medition:"+msg.arg1+" " + " Medition_power:"+medation_power);
                    break;
                case MindDataType.CODE_ATTENTION:
                    //Log.d(TAG, "CODE_ATTENTION " + msg.arg1);
                    updateWaveView(msg.arg1);
                    //txv_attenation.setText("Attenation:" + msg.arg1 + " ");
                    attenaion = msg.arg1;

                    changerolefire();

                    break;
                case MindDataType.CODE_POOR_SIGNAL://
                    int poorSignal = msg.arg1;
                    //Log.d(TAG, "poorSignal:" + poorSignal);
                    txv_signal.setText("Signal:"+msg.arg1+" ");
                    if(msg.arg1 >26) dgv.isGame = false;
                    else dgv.isGame = true;
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void showToast(final String msg,final int timeStyle){
        GameActivity.this.runOnUiThread(new Runnable()
        {
            public void run()
            {
                Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }

        });
    }
    public void changerolefire(){
        if(attenaion<35) {
            dgv.role.whichfirebmp = 1;
            jumpcount = 10;
        }
        else if(attenaion<50){
            dgv.role.whichfirebmp =2;
            jumpcount = 15;
        }
        else if(attenaion < 65){
            dgv.role.whichfirebmp =3;
            jumpcount = 20;
        }
        else if(attenaion<80){
            dgv.role.whichfirebmp =4;
            jumpcount = 25;
        }
        else{
            dgv.role.whichfirebmp =5;
            jumpcount = 30;
        }

    }


    //  設定 sensor  三軸加速度
    private SensorManager sm;
    Sensor sr;

    private void initSensor() {
        sm = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        sr = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        if(dgv.isGame) {
            if (values[0] > 0) {
                dgv.role.isgoRight = false;
            } else {
                dgv.role.isgoRight = true;
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onResume() {
        super.onResume();

        sm.registerListener(this, sr, SensorManager.SENSOR_DELAY_GAME);


    }

    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);
    }



    //  儲存最高紀錄到手機

    public void savePreference(int result){
        String PREFS_NAME = "mindwavemobiledemo";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("Height", result);
        editor.commit();
    }

    public int getPreference(){
        String PREFS_NAME = "mindwavemobiledemo";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("Height", 0);
    }


    public void get(View v){
        dgv.role.bonusCount += 150;
        dgv.role.jumpCount = dgv.role.bonusCount;
        sp.play(sp_fire,1,1,0,1,(float)1.2);
    }

    public AlertDialog getAlertDialog(String title,String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title);

        builder.setMessage(message);

        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sp.stop(sp_gameover);
                sp.stop(sp_fire);
                finish();

            }
        });
        builder.setNegativeButton("Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dgv.reset();
                medation_power = 0;

            }
        });
        return builder.create();
    }

}
