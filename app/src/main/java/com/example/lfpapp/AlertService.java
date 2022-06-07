package com.example.lfpapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class AlertService extends Service {
    private SharedPreferences appData;
    private String UID = "";
    private String API = "";
    private String lastCheckTime = "";
    private Boolean loadDataSeq = true;

    NotificationManager manager;
    ServiceThread thread;

    String jsonResult;
    ArrayList<UserEventData> data;
    ScoreData bData;

    loadJson getUserEventData;
    loadJson getScoreData;
    loadThumb getThumbnail;

    public AlertService() {
    }
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String strId = getString(R.string.noti_channel_id);
            final String strTitle = getString(R.string.app_name);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = notificationManager.getNotificationChannel(strId);
            if (channel == null) {
                channel = new NotificationChannel(strId, strTitle, NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }

            Notification notification = new NotificationCompat.Builder(this, strId).build();
            startForeground(1, notification);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
        //throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Service", "StartCommand");
        appData = getSharedPreferences("appData", MODE_PRIVATE);
        UID = appData.getString("UID", "");
        API = appData.getString("API", "");
        lastCheckTime = appData.getString("lastCheckTime", "");

        manager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        myServiceHandler handler = new myServiceHandler();
        thread = new ServiceThread(handler, getApplicationContext());
        thread.start();
        return START_STICKY;
    }
    //서비스가 종료될 때 할 작업
    public void onDestroy() {
        Log.e("Service", "Destroy");
        if(thread != null)
            thread.stopForever();
        thread = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

    /*public void start() {
        Log.e("aaaaa", "service start");
        myServiceHandler handler = new myServiceHandler();
        thread = new ServiceThread(handler);
        thread.start();
    }
    public void stop() {
        myServiceHandler handler = new myServiceHandler();
        thread = new ServiceThread(handler);
        thread.stopForever();
    }*/

    public class myServiceHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String curTime = mFormat.format(date);
            int request = appData.getInt("request", 0);
            loadDataSeq = true;

            NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP );
            PendingIntent pendingIntent = PendingIntent.getActivity( AlertService.this, 0, intent, PendingIntent.FLAG_IMMUTABLE );
            Uri soundUri = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION );

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                @SuppressLint("WrongConstant")
                NotificationChannel notificationChannel = new NotificationChannel( "my_notification", "n_channel", NotificationManager.IMPORTANCE_MAX );
                notificationChannel.setDescription( "description" );
                notificationChannel.setName( "ALERT Channel" );
                assert notificationManager != null;
                notificationManager.createNotificationChannel( notificationChannel );
            }

            if(lastCheckTime.equals("")){
                lastCheckTime = curTime;
                SharedPreferences.Editor editor = appData.edit();
                editor.putString("lastCheckTime", lastCheckTime);
                editor.apply();

                NotificationCompat.Builder firstBuilder = new NotificationCompat.Builder( AlertService.this )
                        .setSmallIcon( R.mipmap.ic_icon_gray )
                        .setContentTitle( "Start Detection" )
                        //.setContentText( "abcd" )
                        .setContentText( "This notification is generated only on first run." )
                        .setAutoCancel( true )
                        .setSound( soundUri )
                        .setContentIntent( pendingIntent )
                        .setDefaults( Notification.DEFAULT_ALL )
                        .setOnlyAlertOnce( true )
                        .setChannelId( "my_notification" )
                        .setColor( Color.parseColor( "#ffffff" ) );
                //.setProgress(100,50,false);
                assert notificationManager != null;
                int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
                notificationManager.notify( m, firstBuilder.build() );
                Log.e("Service", "first execute");
            }

            // get user data
            try{
                Log.e("AsyncTask", "Start getUserEventData");
                getUserEventData = new loadJson(UID, API, 1, 0);
                jsonResult = getUserEventData.execute().get();
            } catch(InterruptedException e){
                e.printStackTrace();
            } catch (ExecutionException e){
                e.printStackTrace();
            }
            Log.e("Service", "UserData : " + jsonResult);
            if(jsonResult == null){
                Log.e("Service", "No Data");
            }
            else {
                data = findItem(jsonResult);
                Log.e("Service", "DataSize : " + data.size());

                for(int i = 0; i < data.size(); i++){
                    String scoreJsonData;
                    String itemTime = data.get(i).getDate();

                    try {
                        Log.e("Service", "CurrentTime : " + curTime);
                        Log.e("Service", "lastCheckTime : " + lastCheckTime);
                        Log.e("Service", "itemTime : " + itemTime);

                        itemTime = convertLocalTime(itemTime);
                        Date checkTimeDate = mFormat.parse(lastCheckTime);
                        Date itemTimeDate = mFormat.parse(itemTime);
                        Log.e("Service", "change itemTime : " + itemTime);

                        long diff = checkTimeDate.getTime() - itemTimeDate.getTime();
                        Log.e("Service", "time : " + diff);
                        if(diff >= 0)
                            continue;
                    } catch (ParseException e){
                        e.printStackTrace();
                    }

                    //Log.e("tag", curTime);
                    //Log.e("tag", itemTime);
                    //Log.e("tag", data.get(i).getTitle());

                    try{
                        Log.e("AsyncTask", "Start getScoreData");
                        getScoreData = new loadJson(data.get(i).getBeatmapID(), API, 2, data.get(i).getMode());
                        getThumbnail = new loadThumb(data.get(i).getBeatmapSetID());
                        scoreJsonData = getScoreData.execute().get();
                        Bitmap bitmap = getThumbnail.execute().get();
                        if(scoreJsonData != null && bitmap != null) {
                            bData = getData(scoreJsonData);
                            String detailsScore = "(" + bData.getX320() + " / " + bData.getX300() + " / " + bData.getX200() + " / " + (bData.getX100() + bData.getX50() + bData.getX0()) + ")";

                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://osu.ppy.sh/beatmapsets/" + data.get(i).getBeatmapSetID() + "#" + intToStringMode(data.get(i).getMode()) + "/" + data.get(i).getBeatmapID()));
                            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP );
                            pendingIntent = PendingIntent.getActivity( AlertService.this, 0, intent, PendingIntent.FLAG_IMMUTABLE );
                            NotificationCompat.Builder alertBuilder = new NotificationCompat.Builder(AlertService.this)
                                    .setSmallIcon( R.mipmap.ic_icon_gray )
                                    .setLargeIcon(bitmap)
                                    .setContentTitle(data.get(i).getTitle())
                                    //.setContentText( "abcd" )
                                    .setContentText(bData.getPlayer() + " " + detailsScore)
                                    .setAutoCancel(true)
                                    .setSound(soundUri)
                                    .setContentIntent(pendingIntent)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setOnlyAlertOnce(true)
                                    .setChannelId("my_notification")
                                    .setColor(Color.parseColor("#ffffff"));
                            //.setProgress(100,50,false);
                            assert notificationManager != null;
                            int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
                            notificationManager.notify(m, alertBuilder.build());
                        }
                        else {
                            loadDataSeq = false;
                        }

                    } catch(InterruptedException e){
                        e.printStackTrace();
                    } catch (ExecutionException e){
                        e.printStackTrace();
                    }
                }
                if(data.size() > 0 && loadDataSeq == true){
                    if(data.get(0).getDate() != lastCheckTime) {
                        lastCheckTime = convertLocalTime(data.get(0).getDate());
                        Log.e("Service", "update lastCheckTime : " + lastCheckTime);
                        SharedPreferences.Editor editor = appData.edit();
                        editor.putString("lastCheckTime", lastCheckTime);
                        editor.apply();
                    }
                }

                SharedPreferences.Editor editor = appData.edit();
                request += 1;
                editor.putInt("request", request);
                editor.apply();

                Log.e("Service", "Total Request : " + request);
                Log.e("Service", "Last Check Time : " + lastCheckTime);
                Log.e("Service", "Message Complete");
            }
        }
    }

    private ArrayList<UserEventData> findItem(String jsonString){
        String beatmapID = null;
        String beatmapSetID = null;
        String date = null;
        String title = null;
        String userName = null;
        int mode = 0;
        ArrayList<UserEventData> eventArray = new ArrayList<>();

        try{
            JSONArray initArray = new JSONArray(jsonString);
            JSONObject initObject = initArray.getJSONObject(0);
            userName = initObject.optString("username");

            JSONArray jsonArray = initObject.getJSONArray("events");

            for (int i=0; i < jsonArray.length(); i++){
                HashMap map = new HashMap<>();
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                beatmapID = jsonObject.optString("beatmap_id");
                beatmapSetID = jsonObject.optString("beatmapset_id");
                date = jsonObject.optString("date");

                title = jsonObject.optString("display_html");
                title = title.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
                if(title.indexOf(userName + " has lost first place on ") != -1){
                    title = title.replace(userName + " has lost first place on ", "");
                    if(title.indexOf("(osu!)")!= -1)
                        mode = 0;
                    else if(title.indexOf("(osu!taiko)")!= -1)
                        mode = 1;
                    else if(title.indexOf("(osu!catch)")!= -1)
                        mode = 2;
                    else if(title.indexOf("(osu!mania)")!= -1)
                        mode = 3;
                    UserEventData index = new UserEventData(beatmapID, beatmapSetID, date, title, userName, mode);
                    eventArray.add(index);
                }
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        return eventArray;
    }
    private ScoreData getData(String jsonString){
        ScoreData data = new ScoreData();
        try{
            JSONArray initArray = new JSONArray(jsonString);
            JSONObject initObject = initArray.getJSONObject(0);

            data.setPlayer(initObject.optString("username"));
            data.setX320(Integer.parseInt(initObject.optString("countgeki")));
            data.setX300(Integer.parseInt(initObject.optString("count300")));
            data.setX200(Integer.parseInt(initObject.optString("countkatu")));
            data.setX100(Integer.parseInt(initObject.optString("count100")));
            data.setX50(Integer.parseInt(initObject.optString("count50")));
            data.setX0(Integer.parseInt(initObject.optString("countmiss")));
        } catch (JSONException e){
            e.printStackTrace();
        }
        return data;
    }
    private String intToStringMode(int mode){
        switch(mode){
            case 0:
                return "osu";
            case 1:
                return "taiko";
            case 2:
                return "fruits";
            case 3:
                return "mania";
            default:
                return "";
        }
    }
    private String convertLocalTime(String utcTime){
        String localTime = "";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            Date dateUtcTime = dateFormat.parse(utcTime);
            long longUtcTime = dateUtcTime.getTime();

            TimeZone zone = TimeZone.getDefault();
            int offset = zone.getOffset(longUtcTime);
            long longLocalTime = longUtcTime + offset;

            Date dateLocalTime = new Date();
            dateLocalTime.setTime(longLocalTime);

            localTime = dateFormat.format(dateLocalTime);
        } catch (ParseException e){
            e.printStackTrace();
        }

        return localTime;
    }

}