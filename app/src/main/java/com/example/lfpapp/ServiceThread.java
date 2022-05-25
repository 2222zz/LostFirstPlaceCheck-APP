package com.example.lfpapp;

import static android.content.Context.CONNECTIVITY_SERVICE;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.logging.Handler;

public class ServiceThread extends Thread{
    AlertService.myServiceHandler handler;
    Context context;
    boolean isRun = true;

    public ServiceThread(AlertService.myServiceHandler handler, Context context) {
        this.handler = handler;
        this.context = context;
    }
    public void stopForever() {
        synchronized (this) {
            this.isRun = false;
        }
    }
    public void run() {
        while (isRun) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if(networkInfo != null){
                Log.e("Thread", "Network Connection Success");
                handler.sendEmptyMessage(0);
                try {
                    Thread.sleep( 90000 ); //90초씩 쉰다
                } catch (Exception e) {
                }
            }
            else{
                try {
                    Log.e("Thread", "Network Connection Failed");
                    Thread.sleep( 20000 ); //10초씩 쉰다
                } catch (Exception e) {
                }
            }
        }
    }
}
