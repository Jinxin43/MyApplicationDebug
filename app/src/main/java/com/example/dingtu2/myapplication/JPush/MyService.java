package com.example.dingtu2.myapplication.JPush;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.DingTu.Base.ICallback;
import com.DingTu.Base.PubVar;
import com.DingTu.Cargeometry.Coordinate;
import com.DingTu.GPS.LocationEx;
import com.DingTu.Map.StaticObject;
import com.baidu.tts.tools.SharedPreferencesUtils;
import com.example.dingtu2.myapplication.AppSetting;
import com.example.dingtu2.myapplication.MainActivity;
import com.example.dingtu2.myapplication.R;
import com.example.dingtu2.myapplication.db.xEntity.PatrolEntity;
import com.example.dingtu2.myapplication.db.xEntity.PatrolPointEntity;
import com.example.dingtu2.myapplication.db.xEntity.TraceEntity;
import com.example.dingtu2.myapplication.http.Httpmodel.HttpTraceModel;
import com.example.dingtu2.myapplication.http.RetrofitHttp;
import com.example.dingtu2.myapplication.manager.PatrolManager;
import com.example.dingtu2.myapplication.manager.TraceManager;
import com.example.dingtu2.myapplication.manager.UploadMananger;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Notification.PRIORITY_MIN;

public class MyService extends Service {
    private Notification notification;
    private NotificationManager mManager;
    private NotificationCompat.Builder notificationBuilder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notifyIntent = new Intent();
        notifyIntent.setClass(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {//8.0系统之上
            String channelId = createNotificationChannel("my_service", "My Background Service");
            notificationBuilder = new NotificationCompat.Builder(this, channelId);
            notification = notificationBuilder.setAutoCancel(true)
                    .setContentText("“巡护系统”正在运行")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(PRIORITY_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(101, notification);

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        mManager.createNotificationChannel(chan);
        return channelId;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (SharedPreferencesUtils.getBoolean(getApplicationContext(), "mIsRounding")) {
            if(PubVar.m_GPSLocate!=null&&  PubVar.m_DoEvent.mRoundLinePresenter!=null) {
                PubVar.m_DoEvent.mRoundLinePresenter.setTraceCallback(new ICallback() {
                    @Override
                    public void OnClick(String Str, final Object ExtraStr) {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("TAG", "开始上传数据！");
                                try {
                                    LocationEx location = (LocationEx) ExtraStr;
                                    TraceEntity traceEntity = new TraceEntity();
                                    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    if (location.GetGpsDate() == null || location.GetGpsDate().isEmpty() || location.GetGpsDate() == null || location.GetGpsTime().isEmpty()) {
                                        try {
                                            traceEntity.setGpsTime(sd.parse(location.GetGpsDate() + " " + location.GetGpsTime()));
                                        } catch (ParseException ex) {
                                            ex.printStackTrace();
                                            traceEntity.setGpsTime(new Date());
                                        }
                                    } else {
                                        traceEntity.setGpsTime(new Date());
                                    }


                                    if (AppSetting.curRound != null) {
                                        traceEntity.setRoundID(AppSetting.curRound.getId());
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Current round is null", Toast.LENGTH_SHORT).show();
                                    }

                                    Coordinate coord = StaticObject.soProjectSystem.WGS84ToXY(location.GetGpsLongitude(), location.GetGpsLatitude(), location.GetGpsAltitude());
                                    String name=StaticObject.soProjectSystem.GetCoorSystem().GetName();
                                    if(name.equals("西安80坐标")){
                                        traceEntity.setSrid("2381");
                                    }else if(name.equals("北京54坐标")){
                                        traceEntity.setSrid("2433");
                                    }else if(name.equals("2000国家大地坐标系")){
                                        traceEntity.setSrid("4545");
                                    }else if(name.equals("WGS-84坐标")){
                                        traceEntity.setSrid("4326");
                                    }
                                    traceEntity.setGpsTime(traceEntity.getGpsTime());
                                    traceEntity.setUserID(AppSetting.curUserKey);
                                    if(location.GetGpsLatitude()>0&&location.GetGpsLongitude()>0) {
                                        traceEntity.setHeight(location.GetGpsAltitude());
                                        traceEntity.setLatitude(location.GetGpsLatitude());
                                        traceEntity.setLongitude(location.GetGpsLongitude());
                                        NumberFormat nf = NumberFormat.getInstance();
                                        nf.setGroupingUsed(false);
                                        traceEntity.setX(nf.format(coord.getX()));
                                        traceEntity.setY(nf.format(coord.getY()));
                                    }
                                    traceEntity.setUploadStatus(0);
                                    traceEntity.setSaveTime(new Date());
                                    traceEntity.setId(traceEntity.getGpsTime().getTime());
                                    TraceManager.getInstance().SaveTrace(traceEntity);
                                    if (AppSetting.curRound != null && AppSetting.curRound.getStartPoint() == null) {
                                        PatrolPointEntity patrolPointEntity = new PatrolPointEntity();
                                        patrolPointEntity.setUserID(AppSetting.curUser.getUserID());
                                        patrolPointEntity.setRoundID(AppSetting.curRound.getId());
                                        patrolPointEntity.setLatitude(location.GetGpsAltitude());
                                        patrolPointEntity.setLongitude(location.GetGpsLongitude());
                                        patrolPointEntity.setHeight(location.GetGpsAltitude());
                                        patrolPointEntity.setGpsTime(new Date());
                                        NumberFormat nfs = NumberFormat.getInstance();
                                        nfs.setGroupingUsed(false);
                                        patrolPointEntity.setX(nfs.format(coord.getX()));
                                        patrolPointEntity.setY(nfs.format(coord.getY()));
                                        if(name.equals("西安80坐标")){
                                            traceEntity.setSrid("2381");
                                        }else if(name.equals("北京54坐标")){
                                            traceEntity.setSrid("2433");
                                        }else if(name.equals("2000国家大地坐标系")){
                                            traceEntity.setSrid("4545");
                                        }else if(name.equals("WGS-84坐标")){
                                            traceEntity.setSrid("4326");
                                        }
                                        patrolPointEntity.setPointType("0");
                                        AppSetting.curRound.setStartPoint(patrolPointEntity);

                                        if (AppSetting.curRound.getServerId() == null) {
                                            saveStartPoint(AppSetting.curRound, false);
                                        } else {
                                            saveStartPoint(AppSetting.curRound, true);
                                        }

                                    }

                                    if (AppSetting.curRound == null || AppSetting.curRound.getServerId() == null || AppSetting.curRound.getServerId().isEmpty()) {
//                                    if(!AppSetting.isReUpload)
//                                    {
//                                        UploadMananger.getInstance().uploadRound(AppSetting.curRound, new ICallback() {
//                                            @Override
//                                            public void OnClick(String Str, Object ExtraStr) {
//                                                //TODO:setting server id to all entities;
//
//                                            }
//                                        });
//                                    }
                                    } else {
                                        HttpTraceModel httpTraceModel = new HttpTraceModel();
                                        httpTraceModel.setUserId(traceEntity.getUserID());
                                        httpTraceModel.setRoundId(AppSetting.curRound.getServerId());
                                        httpTraceModel.setLatitude(traceEntity.getLatitude() + "");
                                        httpTraceModel.setLongitude(traceEntity.getLongitude() + "");
                                        httpTraceModel.setGpsTime(traceEntity.getGpsTime().getTime());
                                        httpTraceModel.setHeight(traceEntity.getHeight() + "");
                                        httpTraceModel.setX(traceEntity.getX());
                                        httpTraceModel.setY(traceEntity.getY());
                                        httpTraceModel.setSrid(traceEntity.getSrid());

                                        Log.d("轨迹存储", "ID:" + traceEntity.getId() + " lat: " + traceEntity.getLatitude() + " lon:" + traceEntity.getLongitude() + " time:" + traceEntity.getGpsTime().toString());
                                        OkHttpClient.Builder builder = new OkHttpClient.Builder();
                                        Call<ResponseBody> newTraceCall = RetrofitHttp.getRetrofit(builder.build()).uploadTrace("InsertTrackData", httpTraceModel);
                                        final TraceEntity trace = traceEntity;
                                        newTraceCall.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                if (response.body() == null) {
                                                    Log.d("upload trace", "response.body() is null");
                                                    return;
                                                }

                                                try {
                                                    if (response.body().string().contains("true")) {
                                                        Log.e("上传轨迹", trace.getGpsTime().toString());
                                                        trace.setUploadStatus(1);
//                                                    DatabaseCreator.getInstance(mContext).getDatabase().traceDao().insertTraces(trace);
                                                        TraceManager.getInstance().SaveTrace(trace);
                                                    } else {
                                                        Log.e("上传轨迹", response.body().string());

                                                    }
                                                } catch (IOException io) {
                                                    io.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                Log.e("上传轨迹失败：", t.getMessage());
                                            }
                                        });
                                    }

                                } catch (Exception ex) {
                                    Log.e("保存轨迹", ex.getMessage());
                                }
                            }
                        }).start();
                    }
                });
            }


        } else {
            if(PubVar.m_GPSLocate!=null&&  PubVar.m_DoEvent.mRoundLinePresenter!=null) {
                PubVar.m_GPSLocate.CloseGPS();
                PubVar.m_DoEvent.mRoundLinePresenter.Stop();
                PubVar.m_DoEvent.mRoundLinePresenter.setTraceCallback(null);
            }
        }

        Intent intentService = new Intent(this, MyService.class);
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intentService);
        } else {
            startService(intentService);
        }

        return START_STICKY;
    }


    private void saveStartPoint(PatrolEntity patrolEntity, boolean patrolUploaded) {

        if (patrolEntity.getStartPoint() == null) {
            return;
        }

        try {
            PatrolManager.getInstance().savePatrolPoint(patrolEntity.getStartPoint());
        } catch (Exception ex) {
            //如果起点保存失败，则清空
            patrolEntity.setStartPoint(null);
            return;
        }

        if (patrolUploaded) {
            if (patrolEntity.getServerId() != null && !patrolEntity.getServerId().isEmpty()) {
                UploadMananger.getInstance().uploadPatrolPoint(patrolEntity.getStartPoint(), AppSetting.curRound.getServerId(), new ICallback() {
                    @Override
                    public void OnClick(String Str, Object ExtraStr) {

                    }
                });
            }
        }


    }



}
