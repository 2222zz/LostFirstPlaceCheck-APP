package com.example.lfpapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class SettingActivity extends AppCompatActivity {
    Context context;
    private SharedPreferences appData;
    private int device_width = 0;
    private int device_height = 0;
    private boolean isOpen = false;

    private String UID = "";
    private String API = "";
    private int REQ = 0;

    FloatingActionButton mainBtn, keyBtn, settingBtn, logBtn;
    TextView keyText, settingText, logText;
    Animation fab_open, fab_close, rotate_forward, rotate_backward;

    View.OnClickListener mListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        context = getApplicationContext();

        appData = getSharedPreferences("appData", MODE_PRIVATE);
        UID = appData.getString("UID", "");
        API = appData.getString("API", "");
        REQ = appData.getInt("request", 0);

        DisplayMetrics display = getApplicationContext().getResources().getDisplayMetrics();
        device_width = display.widthPixels;
        device_height = display.heightPixels;

        mainBtn = (FloatingActionButton) findViewById(R.id.fab_main);
        keyBtn = (FloatingActionButton) findViewById(R.id.fab_key);
        settingBtn = (FloatingActionButton) findViewById(R.id.fab_setting);
        logBtn = (FloatingActionButton) findViewById(R.id.fab_log);
        keyText = (TextView) findViewById(R.id.fab_key_txt);
        settingText = (TextView) findViewById(R.id.fab_setting_txt);
        logText = (TextView) findViewById(R.id.fab_log_txt) ;

        fab_open = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(this, R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(this, R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(this, R.anim.rotate_backward);

        rotate_forward.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                finish();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                action();
            }
        });
        mListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()){
                    case R.id.fab_key:
                        finish();
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://osu.ppy.sh/p/api"));
                        startActivity(intent);
                        // open browser
                        break;
                    case R.id.fab_setting:
                        // open dialog
                        finish();
                        ((MainActivity)MainActivity.context).setValue();
                        break;
                    case R.id.fab_log:
                        finish();
                        Toast.makeText((MainActivity)MainActivity.context, "request : " + Integer.toString(REQ), Toast.LENGTH_LONG).show();
                        break;

                }
            }
        };
        keyBtn.setOnClickListener(mListener);
        settingBtn.setOnClickListener(mListener);
        logBtn.setOnClickListener(mListener);

        action();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0,0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int)event.getX();
            int y = (int)event.getY();
            Bitmap bitmapScreen = Bitmap.createBitmap(device_width, device_height, Bitmap.Config.ARGB_8888);
            if(x < 0 || y < 0)
                return false;
            int ARGB = bitmapScreen.getPixel(x, y);
            if(Color.alpha(ARGB) == 0) {
                action();
            }
            return true;
        }
        return false;
    }

    private void action(){
        if(isOpen){
            mainBtn.startAnimation(rotate_forward);
            keyBtn.startAnimation(fab_close);
            keyBtn.setClickable(false);
            settingBtn.startAnimation(fab_close);
            settingBtn.setClickable(false);
            logBtn.startAnimation(fab_close);
            logBtn.setClickable(false);
            keyText.startAnimation(fab_close);
            settingText.startAnimation(fab_close);
            logText.startAnimation(fab_close);
            isOpen = false;
        }
        else{
            mainBtn.startAnimation(rotate_backward);
            keyBtn.startAnimation(fab_open);
            keyBtn.setClickable(true);
            settingBtn.startAnimation(fab_open);
            settingBtn.setClickable(true);
            logBtn.startAnimation(fab_open);
            logBtn.setClickable(true);
            keyText.startAnimation(fab_open);
            settingText.startAnimation(fab_open);
            logText.startAnimation(fab_open);
            isOpen = true;
        }
    }
}