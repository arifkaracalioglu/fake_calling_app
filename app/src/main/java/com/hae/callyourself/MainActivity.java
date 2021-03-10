package com.hae.callyourself;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import static com.hae.callyourself.SettingsActivity.mSharedPreferencesSettings;

public class MainActivity extends AppCompatActivity {
    private LinearLayout mCallLayout;
    private TextView mTextViewSettings, mTextViewCall;
    private ImageView mImageViewCall;
    private Bitmap myPicture;
    public static SharedPreferences mSharedPreferences;
    public static SharedPreferences.Editor editor;
    private int CALL_PERMISSION_CODE = 1;
    private int delayTime;
    private View decorView;
    private Boolean isThereAPhoto = false;
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //With this codes activity always work, even if screen locked.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_main);
        //Check permissions
        checkPermissions();
        //START onCreate
        //if API is greater or equal to 27, show on locked screen boolean must be true.
        if (Build.VERSION.SDK_INT >= 27){
            setTurnScreenOn(true);
            setShowWhenLocked(true);
        }
        //Get SettingsActivity sharedPreferences and get delay time that choosen by user
        mSharedPreferencesSettings = getSharedPreferences("settings_activity",MODE_PRIVATE);
        delayTime = mSharedPreferencesSettings.getInt("delay",0);
        //View Settings
        mTextViewSettings = findViewById(R.id.mTextViewSettings);
        mImageViewCall = findViewById(R.id.mImageViewCall);
        mTextViewCall = findViewById(R.id.mTextViewCall);
        mCallLayout = findViewById(R.id.mCallLayout);
        //Create drawable and set background of mCallLayout
        readFile();
        Drawable drawable = new BitmapDrawable(getResources(),myPicture);
        if (isThereAPhoto){
            //Set Text and ImageView sources
            mTextViewCall.setText(null);
            mTextViewCall.setVisibility(View.GONE);
            mImageViewCall.setImageDrawable(drawable);
            //Call layout listener
            mCallLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, getString(R.string.hsssh), Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            readFile();
                            playring();
                            unlock();
                        }
                    },delayTime*1000);
                }
            });
        }else{
            Toast.makeText(MainActivity.this, getString(R.string.there_is_no_photo), Toast.LENGTH_SHORT).show();
        }
        myPicture = BitmapFactory.decodeResource(getResources(),R.drawable.main_background);
        //Preferences Settings
        mSharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
        editor = mSharedPreferences.edit();
        if (getPreferencesBool("isthisfirst",this)){
            Toast.makeText(this, getString(R.string.first_settings), Toast.LENGTH_SHORT).show();
        }
        //Change delay test
        mTextViewSettings.setText(getString(R.string.time_out)+delayTime);
        //Get DecorView for hide navigation bar
        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0){
                    decorView.setSystemUiVisibility(hideSystemBars());
                }
            }
        });
    }// EOF onCreate
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            decorView.setSystemUiVisibility(hideSystemBars());
        }
    }
    private int hideSystemBars(){
        return View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                |View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    }
    public void goSettingsClick(View view){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }
    public static boolean getPreferencesBool(String setting, Context context){
        return mSharedPreferences.getBoolean(setting,false);
    }
    public static String getPreferencesString(String setting, Context context){
        return mSharedPreferences.getString(setting,context.getString(R.string.empty));
    }
    //Read image from saved_images file from external storage
    private void readFile(){
        try {
            File file = new File(Environment.getExternalStorageDirectory()+"/saved_images/profile.jpg");
            myPicture = BitmapFactory.decodeFile(file.getAbsolutePath());
            isThereAPhoto = true;
        }catch (Exception e){
            myPicture = BitmapFactory.decodeResource(getResources(),R.drawable.ic_baseline_call_24);
            isThereAPhoto = false;
        }
    }
    //Play default ringtone of phone
    private void playring(){
        Uri ringtone = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(),RingtoneManager.TYPE_RINGTONE);
        Ringtone defaultRingtone = RingtoneManager.getRingtone(getApplicationContext(), ringtone);
        defaultRingtone.play();
    }
    //Show screen even if phone is sleeping
    public void unlock(){
       LayoutInflater mInflater = this.getLayoutInflater();
       // WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        View mView = (View) mInflater.inflate(R.layout.activity_fake_calling_screen , null);
        Drawable mDrawable = new BitmapDrawable(getResources(),myPicture);
        mView.setBackground(mDrawable);

        //Finish view settings
        setContentView(mView);
        View theScreen = findViewById(R.id.mBtnViewFinish);
        theScreen.setBackground(mDrawable);

        WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 0,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                /* | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON */,
                PixelFormat.RGBA_8888);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else{
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
        wakeLock.acquire();
      //  mWindowManager.addView(mView, mLayoutParams);
    }
    //Check permissions for write and read data
    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            requestPermissionStorage();
        }
    }
    //Request Permissions Method for Storage Permissions
    private void requestPermissionStorage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.permission_needed_title));
            builder.setMessage(getString(R.string.permission_needed));
            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},CALL_PERMISSION_CODE);
                }
            });
            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    exitApp();
                }
            }).create().show();
        }else{
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},CALL_PERMISSION_CODE);
        }
    }
    //Permission granted or denied results are checks on that method
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PERMISSION_CODE){
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show();
                }
        }
    }
    //Finish activity and exit from app method
    private void exitApp() {
        finish();
        System.exit(0);
    }
}