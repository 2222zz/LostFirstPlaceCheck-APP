package com.example.lfpapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {
    public static Context context;
    private SharedPreferences appData;
    private String UID = "";
    private String API = "";
    private boolean isSetting;
    private boolean flag;

    String jsonResult;
    String name;
    String country;

    FloatingActionButton fab;
    ToggleButton btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        Intent serviceIntent = new Intent(this, AlertService.class);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        btn = (ToggleButton) findViewById(R.id.btn);



        appData = getSharedPreferences("appData", MODE_PRIVATE);
        UID = appData.getString("UID", "");
        API = appData.getString("API", "");
        flag = appData.getBoolean("flag", false);
        isSetting = appData.getBoolean("isSetting", false);

        // log
        Log.e("Start", "Start App");
        Log.e("Start", "UID : " + UID);
        Log.e("Start", "API : " + API);
        Log.e("Start", "Last Check : " + appData.getString("lastCheckTime", ""));
        if(flag)
            Log.e("Start", "Current State : ON" );
        else
            Log.e("Start", "Current State : OFF");
        if(!isSetting)
            Log.e("Start", "Not Setting Key");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent t = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(t);
            }
        });
//        SharedPreferences.Editor editor = appData.edit();
//        editor.putString("lastCheckTime", "2022-05-16 09:35:11");
//        editor.apply();

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean WhiteCheck = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            WhiteCheck = powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
            if(!WhiteCheck){
                Log.d("WhiteList","Entry WhiteList");
                Intent intent  = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:"+ context.getPackageName()));
                context.startActivity(intent);
            }
            else Log.d("WhiteList","Already WhiteList");
        }
        /*
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo rsi : am.getRunningServices(Integer.MAX_VALUE)){
            if(!AlertService.class.getName().equals(rsi.service.getClassName()) && isSetting && flag){
                startForegroundService(serviceIntent);
                Toast.makeText(context, "AutoStart LFP Service", Toast.LENGTH_SHORT).show();
            }
        }*/

        btn.setChecked(flag);
        btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    if(isSetting){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(serviceIntent);
                        } else {
                            startService(serviceIntent);
                        }
                        //startService(serviceIntent);
                        Log.e("Service", "Start");
                        SharedPreferences.Editor editor = appData.edit();
                        editor.putBoolean("flag", true);
                        editor.apply();
                    }
                    else{
                        Log.e("Service", "Not Start");
                        final AlertDialog.Builder alt_bld = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
                        alt_bld.setTitle("Warning").setMessage("Setting is not complete. Please Set API Key and User UID.").setCancelable(false).setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                        final AlertDialog alert = alt_bld.create();
                        alert.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialogInterface) {
                                alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black));
                            }
                        });
                        alert.show();
                        btn.setChecked(false);
                    }
                }
                else{
                    stopService(serviceIntent);
                    Log.e("Service", "End");
                    SharedPreferences.Editor editor = appData.edit();
                    editor.putBoolean("flag", false);
                    editor.apply();
                }
            }
        });
    }

    public void setValue(){
        final EditText et = new EditText(this);
        final EditText et2 = new EditText(this);
        et.setTextColor(Color.parseColor("#000000"));
        et2.setTextColor(Color.parseColor("#000000"));
        et.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.black));
        et2.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.black));
        et.setHintTextColor(getResources().getColor(R.color.gray));
        et2.setHintTextColor(getResources().getColor(R.color.gray));
        et.setText(API);
        et2.setText(UID);
        et.setHint("Enter Your API Key.");
        et2.setHint("Enter User UID.");
        et.setMaxLines(1);
        et2.setMaxLines(1);
        et.setSingleLine(true);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        //container.setBackgroundColor(Color.parseColor("#FFFFFF"));
        LinearLayout.LayoutParams params = new  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        et.setLayoutParams(params);
        et2.setLayoutParams(params);
        container.addView(et);
        container.addView(et2);
        final AlertDialog.Builder alt_bld = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        alt_bld.setTitle("Basic Setting").setMessage("Enter Information").setCancelable(false).setView(container).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String newKey = et.getText().toString();
                        String newUID = et2.getText().toString();

                        checkValue(newUID, newKey);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        final AlertDialog alert = alt_bld.create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black));
                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black));
            }
        });
        alert.show();
    }
    public void checkValue(String newUID, String newKey){
        try{
            jsonResult = new loadJson(newUID, newKey, 1, 0).execute().get();
            if(jsonResult == null){
                showSimpleDialog("Warning", "Invalid API-Key Value. Please check your API Key.");
                return;
            }
            else if(jsonResult.equals("[]")){
                showSimpleDialog("Warning", "Cannot Found User. Please Check User UID Value.");
                return;
            }
        } catch(InterruptedException e){
            e.printStackTrace();
        } catch (ExecutionException e){
            e.printStackTrace();
        }
        name = getJsonParser(jsonResult, 0);
        country = getJsonParser(jsonResult, 1);

        final TextView textUser = new TextView(this);
        final TextView textKey = new TextView(this);
        textUser.setTextColor(Color.parseColor("#000000"));
        textKey.setTextColor(Color.parseColor("#000000"));
        textUser.setText("User : " + name);
        textKey.setText("Country : " + country);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        //container.setBackgroundColor(Color.parseColor("#FFFFFF"));
        LinearLayout.LayoutParams params = new  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        textUser.setLayoutParams(params);
        textKey.setLayoutParams(params);
        container.addView(textUser);
        container.addView(textKey);
        final AlertDialog.Builder alt_bld = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        alt_bld.setTitle("Check").setMessage("Is this information correct?").setCancelable(false).setView(container).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        API = newKey;
                        UID = newUID;
                        isSetting = true;

                        SharedPreferences.Editor editor = appData.edit();
                        editor.putString("API", API);
                        editor.putString("UID", UID);
                        editor.putBoolean("isSetting", isSetting);
                        editor.apply();

                        showSimpleDialog("Success", "Complete Setting.");
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        final AlertDialog alert = alt_bld.create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black));
                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black));
            }
        });
        alert.show();
    }
    private String getJsonParser(String jsonString, int type){
        String userName = null;
        String country = null;
        ArrayList<String> eventArray = new ArrayList<>();

        try{
            JSONArray initArray = new JSONArray(jsonString);
            JSONObject initObject = initArray.getJSONObject(0);

            userName = initObject.optString("username");
            country = initObject.optString("country");

        } catch (JSONException e){
            e.printStackTrace();
        }
        if(type == 0)
            return userName;
        else
            return country;
    }
    private void showSimpleDialog(String title, String msg){
        final AlertDialog.Builder alt_bld = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
        alt_bld.setTitle(title).setMessage(msg).setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        final AlertDialog alert = alt_bld.create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black));
            }
        });
        alert.show();
    }
}