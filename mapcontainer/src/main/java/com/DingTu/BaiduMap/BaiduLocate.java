package com.DingTu.BaiduMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

import com.DingTu.Base.ICallback;
import com.DingTu.Base.PubVar;
import com.DingTu.Cargeometry.Coordinate;
import com.DingTu.GPS.LocationEx;
import com.DingTu.Map.StaticObject;
import com.DingTu.mapcontainer.MapControl;
import com.DingTu.util.Gps;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.text.DecimalFormat;

import static com.DingTu.util.PositionUtil.gcj_To_Gps84;

public class BaiduLocate {

    public Context m_Context = null;
    public MapControl m_MapControl = null;
    public BaiDuMap m_BaiduMap = null;
    public LocationManager m_LTManager = null;
    private LocationClient mLocClient;
    public LocationEx m_LocationEx = null;
    //百度地图导航地址偏差
    private Double LatitudeY = -0.0016245277777;
    private Double LongtitudeX = 0.0045826388892;
    //巡护页面位置状态回调
    private ICallback m_GPSPositionCallback = null;
    public void SetGpsSetCallback(ICallback callback)
    {
        this.m_GPSPositionCallback = callback;
    }


    public BaiduLocate(MapControl mapControl) {
        this.m_Context = PubVar.m_DoEvent.m_Context;
        this.m_MapControl = mapControl;
        this.m_BaiduMap = new BaiDuMap(this.m_MapControl);
        PubVar.m_BaiduMap = this.m_BaiduMap;
        m_LocationEx =new LocationEx();
    }

    public boolean GPS_OpenClose = false;   //GPS的开关状态，true-开，close-关

    public boolean OpenGPS() {
        this.m_LTManager = (LocationManager) this.m_Context.getSystemService(Context.LOCATION_SERVICE);

        if (!this.m_LTManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // 创建AlertDialog
            AlertDialog.Builder menuDialog = new AlertDialog.Builder(PubVar.m_DoEvent.m_Context);
            menuDialog.setTitle("系统提示");
            menuDialog.setMessage("获取精确的位置服务，需在位置设置中打开GPS，是否需要打开设置界面？");
            menuDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    PubVar.m_DoEvent.m_Context.startActivity(myIntent);
                    dialog.dismiss();
                }
            });
            menuDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            menuDialog.show();
            this.GPS_OpenClose = false;
            this.m_BaiduMap.UpdateGPSStatus(null);
            return false;

        }

        initLocation();
        if (this.m_LocationEx != null) {
            this.m_BaiduMap.UpdateGPSStatus(this.m_LocationEx);
        }
        GPS_OpenClose = true;
        this.m_BaiduMap.UpdateGPSStatus(null);


        return true;
    }

    private void initLocation() {
        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        LocationClient locationClient = new LocationClient(m_Context);
        //声明LocationClient类实例并配置定位参数
        LocationClientOption locationOption = new LocationClientOption();
        MyLocationListener myLocationListener = new MyLocationListener();
        //注册监听函数
        locationClient.registerLocationListener(myLocationListener);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        locationOption.setCoorType("gcj02");
        //可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        locationOption.setScanSpan(1000);
        //可选，设置是否需要地址信息，默认不需要
        locationOption.setIsNeedAddress(false);
        //可选，设置是否需要地址描述
        locationOption.setIsNeedLocationDescribe(false);
        //可选，设置是否需要设备方向结果
        locationOption.setNeedDeviceDirect(false);
        //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        locationOption.setLocationNotify(true);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locationOption.setIgnoreKillProcess(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        locationOption.setIsNeedLocationDescribe(false);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        locationOption.setIsNeedLocationPoiList(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        locationOption.SetIgnoreCacheException(false);
        //可选，默认false，设置是否开启Gps定位
        locationOption.setOpenGps(true);
        //可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        locationOption.setIsNeedAltitude(true);
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        locationOption.setOpenAutoNotifyMode();
//       //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        locationOption.setOpenAutoNotifyMode(3000, 1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        locationClient.setLocOption(locationOption);
        //开始定位
        locationClient.start();
    }

    /**
     * 得取GPS平面坐标
     * @return
     */
    public Coordinate getGPSCoordinate()
    {
        return StaticObject.soProjectSystem.WGS84ToXY(this.m_LocationEx.GetGpsLongitude(), this.m_LocationEx.GetGpsLatitude(),this.m_LocationEx.GetGpsAltitude());
    }

    /**
     * 得取GPS经纬度坐标
     * @return
     */
    public String getJWGPSCoordinate()
    {
        DecimalFormat df = new DecimalFormat("#.000000");
        return df.format(this.m_LocationEx.GetGpsLongitude())+","+ df.format(this.m_LocationEx.GetGpsLatitude());
    }

    /**
     * 实现定位回调
     */
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            // m_LocationEx.SetInGpsLocate(location);
            if(location!=null&&location.getLatitude()>0.01&&location.getLongitude()>0.01) {
                Gps gps = gcj_To_Gps84(location.getLatitude(), location.getLongitude());

                m_LocationEx.SetGpsLongitude(gps.getWgLon() + LongtitudeX);
                m_LocationEx.SetGpsLatitude(gps.getWgLat() + LatitudeY);
                Log.d("TAG", m_LocationEx.GetGpsLongitude() + "****" + m_LocationEx.GetGpsLatitude());
                m_LocationEx.SetGpsAltitude(location.getAltitude());
                m_LocationEx.SetGpsSpeed(location.getSpeed());
                m_BaiduMap.UpdateGPSStatus(m_LocationEx);
                    if (m_GPSPositionCallback != null) {
                        m_GPSPositionCallback.OnClick("", m_LocationEx);
                    }
            }

        }
    }

}
