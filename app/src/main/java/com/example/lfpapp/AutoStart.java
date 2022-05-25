package com.example.lfpapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class AutoStart extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            SharedPreferences appData;
            appData = context.getSharedPreferences("appData", context.MODE_PRIVATE);
            boolean flag = appData.getBoolean("flag", false);
            boolean isSetting = appData.getBoolean("isSetting", false);

            if(flag && isSetting) {
                Intent t = new Intent(context, AlertService.class);
                context.startService(t);
                Toast.makeText(context, "Auto Start LFP App", Toast.LENGTH_LONG).show();
            }
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }
}