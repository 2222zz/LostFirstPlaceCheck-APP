package com.example.lfpapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class loadThumb extends AsyncTask<String, Void, Bitmap> {

    String clientKey = "#########################";;
    private Bitmap bitmap;
    private String id = "";

    loadThumb(String id){
        this.id = id;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        URL url = null;
        HttpURLConnection conn = null;
        try {
            url = new URL("https://b.ppy.sh/thumb/" + id + ".jpg");

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            conn.setRequestProperty("x-waple-authorization", clientKey);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == conn.HTTP_OK) {
                InputStream is = conn.getInputStream(); // InputStream 값 가져오기
                bitmap = BitmapFactory.decodeStream(is);
                Log.e("load bitmap" , "Success");
                is.close();
            } else {
                Log.i("통신 결과", conn.getResponseCode() + "에러");
            }
        }catch(SocketTimeoutException e){
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            conn.disconnect();
            Log.e("http", "connection end");
        }
        return bitmap;
    }
}
