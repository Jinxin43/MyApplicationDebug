package com.DingTu.BaiduMap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.DingTu.Base.PubVar;
import com.DingTu.Cargeometry.Coordinate;
import com.DingTu.Data.GpsLine;
import com.DingTu.Data.GpsPoly;
import com.DingTu.Data.RoundGPSLine;
import com.DingTu.GPS.LocationEx;
import com.DingTu.Map.StaticObject;
import com.DingTu.mapcontainer.IOnPaint;
import com.DingTu.mapcontainer.MapControl;
import com.DingTu.mapcontainer.R;

public class BaiDuMap implements IOnPaint {

    private MapControl m_MapControl = null;

    public BaiDuMap(MapControl mapControl) {
        this.m_MapControl = mapControl;
        this.m_MapControl._GPSMapPaint = this;
    }


    //采集百度定位线的实例
    private RoundGPSLine m_CRoundGpsLine = null;

    public void SetRoundGpsLine(RoundGPSLine gpsLine) {
        this.m_CRoundGpsLine = gpsLine;
    }

    private GpsLine m_CGpsLine = null;




    //采集GPS面的实例
    private GpsPoly m_CGpsPoly = null;


    //定时更新经纬度
    private LocationEx m_LocationEx = null;

    public void UpdateGPSStatus(LocationEx locationEx) {

        if (locationEx != null) {
            this.m_LocationEx = locationEx;
            //分采集类更新GPS位置信息

            try {


                Coordinate newCoor = StaticObject.soProjectSystem.WGS84ToXY(locationEx.GetGpsLongitude(), locationEx.GetGpsLatitude(), locationEx.GetGpsAltitude());


                if (newCoor != null) {
                    if (this.m_CRoundGpsLine != null)
                        this.m_CRoundGpsLine.UpdateGpsPosition(locationEx, true);
                    if (this.m_CGpsLine != null) {
                        this.m_CGpsLine.UpdateGpsPosition(newCoor);
                    }
                    if (this.m_CGpsPoly != null)
                        this.m_CGpsPoly.getGPSLine().UpdateGpsPosition(newCoor);


                } else {
                    Log.d("m_LocationEx", "GPS not fix");
                }


            } catch (Exception ex) {
                ex.printStackTrace();
            }


        }

        this.m_MapControl.invalidate();


    }

    private Bitmap _GpsPointICON = null;  //GPS当前位置的显示图标

    @Override
    public void OnPaint(Canvas canvas) {
        //1-此处为移屏时不显示更新状态
        if (PubVar.m_Map == null) return;
        if (PubVar.m_Map.getInvalidMap()) return;

        //为采集类刷新显示
        if (this.m_CRoundGpsLine != null) this.m_CRoundGpsLine.OnPaint(canvas);
        if (this.m_CGpsPoly != null) this.m_CGpsPoly.OnPaint(canvas);

//        //如果GPS已经关闭退出
//        if (!PubVar.m_BaiduLocate.GPS_OpenClose) return;

        //4-画当前的定位状态，也就是当前定位点

        Coordinate CurrentGPSCoor = null;
        if(this.m_LocationEx!=null) {
            CurrentGPSCoor = StaticObject.soProjectSystem.WGS84ToXY(this.m_LocationEx.GetGpsLongitude(), this.m_LocationEx.GetGpsLatitude(), this.m_LocationEx.GetGpsAltitude());
            if (CurrentGPSCoor == null) {
                return;
            }
        }else{
            return;
        }
        Point PT = this.m_MapControl.getMap().getViewConvert().MapToScreen(CurrentGPSCoor);

        //GPS状态图片资源
        if (this._GpsPointICON == null)
            this._GpsPointICON = ((BitmapDrawable) (PubVar.m_DoEvent.m_Context.getResources().getDrawable(R.drawable.gpspointer))).getBitmap();

        float PointX = PT.x - this._GpsPointICON.getWidth() / 2;
        float PointY = PT.y - this._GpsPointICON.getHeight() / 2;
        canvas.drawBitmap(this._GpsPointICON, PointX, PointY, null);

        if (PubVar.AutoPan) {
            //判断是否已经超出了当前显示范围
            if (!PubVar.m_Map.getExtend().ContainsPoint(CurrentGPSCoor)) {
                this.m_MapControl._Pan.SetNewCenter(CurrentGPSCoor);
            }
        }
    }


}
