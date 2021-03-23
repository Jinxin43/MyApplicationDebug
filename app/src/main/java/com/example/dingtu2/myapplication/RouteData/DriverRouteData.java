package com.example.dingtu2.myapplication.RouteData;



import com.DingTu.Cargeometry.Coordinate;
import com.DingTu.Map.StaticObject;
import com.DingTu.util.Gps;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import java.util.ArrayList;
import java.util.List;

import static com.DingTu.util.PositionUtil.gcj_To_Gps84;

public class DriverRouteData {

    private DrivingRouteLine mDriverRouteLine;
    private List<LatLng> mDriverLatList;
    private List<Coordinate> mDriverList;
    private LatLng mLat;

    public void setData(DrivingRouteLine driverRouteLine) {
        this.mDriverRouteLine = driverRouteLine;
        mDriverLatList = new ArrayList<LatLng>();
        mDriverList = new ArrayList<Coordinate>();
    }

    public List<Coordinate> getData() {
        if(mDriverLatList!=null&&mDriverLatList.size()>0){
            mDriverLatList.clear();
        }
        if(mDriverList!=null&&mDriverList.size()>0){
            mDriverList.clear();
        }


        if (mDriverRouteLine == null) {
            return null;
        }
        //起点标注
        if (mDriverRouteLine.getStarting() != null) {
            mLat = mDriverRouteLine.getStarting().getLocation();
            mDriverLatList.add(mLat);
        }

        if (mDriverRouteLine.getAllStep() != null
                && mDriverRouteLine.getAllStep().size() > 0) {
                List<DrivingRouteLine.DrivingStep> steps = mDriverRouteLine.getAllStep();
                int stepNum = steps.size();
                List<LatLng> points = new ArrayList<LatLng>();
                ArrayList<Integer> traffics = new ArrayList<Integer>();
                int totalTraffic = 0;
                for (int i = 0; i < stepNum; i++) {
                    if (i == stepNum - 1) {
                        points.addAll(steps.get(i).getWayPoints());
                    } else {
                        points.addAll(steps.get(i).getWayPoints().subList(0, steps.get(i).getWayPoints().size() - 1));
                    }

                    totalTraffic += steps.get(i).getWayPoints().size() - 1;
                    if (steps.get(i).getTrafficList() != null && steps.get(i).getTrafficList().length > 0) {
                        for (int j = 0; j < steps.get(i).getTrafficList().length; j++) {
                            traffics.add(steps.get(i).getTrafficList()[j]);
                        }
                    }
                }
                boolean isDotLine = false;

                if (traffics != null && traffics.size() > 0) {
                    isDotLine = true;
                }
                mDriverLatList.addAll(points);
//                PolylineOptions option = new PolylineOptions().points(points).textureIndex(traffics)
//                        .width(7).dottedLine(isDotLine).focus(true)
//                        .color(getLineColor() != 0 ? getLineColor() : Color.argb(178, 0, 78, 255)).zIndex(0);
//                if (isDotLine) {
//                    option.customTextureList(getCustomTextureList());
//                }
//                overlayOptionses.add(option);



        }


        //终点标注
        if (mDriverRouteLine.getTerminal() != null) {
            mLat = mDriverRouteLine.getTerminal().getLocation();
            mDriverLatList.add(mLat);
        }

        for (int i = 0; i < mDriverLatList.size(); i++) {
            Gps gps = gcj_To_Gps84(mDriverLatList.get(i).latitude, mDriverLatList.get(i).longitude);
            Coordinate coord = StaticObject.soProjectSystem.WGS84ToXY(gps.getWgLon(), gps.getWgLat(), 0);
            mDriverList.add(coord);
        }

        return mDriverList;

    }

}
