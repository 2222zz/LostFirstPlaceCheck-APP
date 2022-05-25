package com.example.lfpapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class loadJson extends AsyncTask<String, Void, String> {

    String clientKey = "#########################";;
    private String str, receiveMsg;
    private String UID = "";
    private String key = "";
    private int flag;
    private int mode;

    // flag 1 : get user
    // flag 2 : get score
    loadJson(String UID, String key, int flag, int mode){
        this.UID = UID;
        this.key = key;
        this.flag = flag;
        this.mode = mode;
    }

    @Override
    protected String doInBackground(String... params) {
        URL url = null;
        HttpURLConnection conn = null;
        try {
            if(flag == 1)
                url = new URL("https://osu.ppy.sh/api/get_user?k=" + key + "&u=" + UID + "&m=" + mode + "&event_days=1");
            else if(flag == 2)
                url = new URL("https://osu.ppy.sh/api/get_scores?k=" + key + "&b=" + UID + "&m=" + mode);

            Log.e("http", "connection start");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod("GET");
            //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            //conn.setRequestProperty("x-waple-authorization", clientKey);

            if (conn.getResponseCode() == conn.HTTP_OK) {
                Log.e("http", "load start");
                InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuffer buffer = new StringBuffer();
                while ((str = reader.readLine()) != null) {
                    buffer.append(str);
                }
                receiveMsg = buffer.toString();
                Log.i("receiveMsg : ", receiveMsg);
                Log.e("http", "load end");

                reader.close();
                tmp.close();
            } else {
                Log.e("Connection Result", conn.getResponseCode() + "Error");
            }
        } catch (MalformedURLException e) {
            Log.e("http", "protocol invalid");
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            Log.e("http", "time out");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            conn.disconnect();
            Log.e("http", "connection end");
        }
        return receiveMsg;
    }
}
