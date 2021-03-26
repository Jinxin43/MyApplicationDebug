package com.example.dingtu2.myapplication;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.DingTu.BaiduMap.BaiDuMap;
import com.DingTu.BaiduMap.BaiduLocate;
import com.DingTu.Base.DoEvent;
import com.DingTu.Base.HashValueObject;
import com.DingTu.Base.ICallback;
import com.DingTu.Base.PubVar;
import com.DingTu.Base.Tools;
import com.DingTu.Cargeometry.Coordinate;
import com.DingTu.Cargeometry.Envelope;
import com.DingTu.Cargeometry.Geometry;
import com.DingTu.Cargeometry.Point;
import com.DingTu.Cargeometry.Polygon;
import com.DingTu.Cargeometry.Polyline;
import com.DingTu.Data.RoundGPSLine;
import com.DingTu.Dataset.Dataset;
import com.DingTu.Dataset.SQLiteDataReader;
import com.DingTu.Dataset.Workspace;
import com.DingTu.Enum.lkDataCollectType;
import com.DingTu.Enum.lkGeoLayerType;
import com.DingTu.Enum.lkGeoLayersType;
import com.DingTu.Enum.lkGpsFixMode;
import com.DingTu.GPS.GPSLocate;
import com.DingTu.GPS.GPSMap;
import com.DingTu.GPS.LocationEx;
import com.DingTu.Layer.GeoLayer;
import com.DingTu.Map.Map;
import com.DingTu.Map.StaticObject;
import com.DingTu.Project.BKLayerExplorer;
import com.DingTu.Project.LayerExplorer;
import com.DingTu.Project.ProjectDB;
import com.DingTu.Project.ProjectExplorer;
import com.DingTu.ToolBar.Agent_Measure;
import com.DingTu.mapcontainer.MapControl;
import com.baidu.mapapi.NetworkUtil;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.baidu.tts.tools.SharedPreferencesUtils;
import com.example.dingtu2.myapplication.BlueTooth.BeiDouBaoWen;
import com.example.dingtu2.myapplication.RouteData.DriverRouteData;
import com.example.dingtu2.myapplication.View.PagingScrollHelper;
import com.example.dingtu2.myapplication.adapter.MyAdapter;
import com.example.dingtu2.myapplication.db.xEntity.PatrolEntity;
import com.example.dingtu2.myapplication.db.xEntity.PatrolPointEntity;
import com.example.dingtu2.myapplication.db.xEntity.TraceEntity;
import com.example.dingtu2.myapplication.http.Httpmodel.HttpTraceModel;
import com.example.dingtu2.myapplication.http.Httpmodel.RequestId;
import com.example.dingtu2.myapplication.http.RetrofitHttp;
import com.example.dingtu2.myapplication.manager.PatrolManager;
import com.example.dingtu2.myapplication.manager.ReuploadManager;
import com.example.dingtu2.myapplication.manager.TraceManager;
import com.example.dingtu2.myapplication.manager.UploadMananger;
import com.example.dingtu2.myapplication.manager.UserManager;
import com.example.dingtu2.myapplication.model.PointBean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static com.DingTu.mapcontainer.Tools.ZoomInOutPan;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainMapFragment extends Fragment implements OnGetRoutePlanResultListener, OnGetSuggestionResultListener, PagingScrollHelper.onPageChangeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static boolean unloadTraces = true;
    public TextView mapViewAreaView;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private MapControl mp;
    private View myView;
    //    private boolean mIsRounding = false;
    private boolean isStarting = false;
//    private int[] allDrawButtons = new int[]{R.id.bt_delete_op, R.id.bt_undo_op, R.id.bt_poly_drawline, R.id.bt_poly_gps,
//            R.id.bt_poly_create, R.id.bt_line_drawline, R.id.bt_line_gps, R.id.bt_line_create,
//            R.id.bt_point_draw, R.id.bt_point_coor, R.id.bt_point_gps};
//    private int[] polyButtons = new int[]{R.id.bt_poly_drawline, R.id.bt_poly_gps, R.id.bt_poly_create};
//    private int[] lineButtons = new int[]{R.id.bt_line_drawline, R.id.bt_line_gps, R.id.bt_line_create};
//    private int[] pointButtons = new int[]{R.id.bt_point_draw, R.id.bt_point_coor, R.id.bt_point_gps};

    private int[] m_SelectToolsBarItemIdList = {R.id.bt_poly_drawline, R.id.bt_poly_gps, R.id.bt_line_drawline, R.id.bt_line_gps,
            R.id.bt_point_draw, R.id.bt_point_gps, R.id.btnSelectDraw, R.id.btnStartRound, R.id.btnFullScreen,R.id.btnMoveScreen};
    //    private MapView mBaiduMap;
//    private BaiduMap mBazidumap;
    private RoutePlanSearch mSearch;
    int nodeIndex = -1; // 节点索引,供浏览节点时使用
    private Button mBtnPre, mBtnNext;
    boolean hasShownDialogue = false;
    RouteLine route = null;
    MassTransitRouteLine massroute = null;
    private MassTransitRouteResult nowResultmass = null;
    private BikingRouteResult nowResultbike = null;
    private TextView popupText = null; // 泡泡view
    int nowSearchType = -1; // 当前进行的检索，供判断浏览节点时结果使用。
    private AutoCompleteTextView mStart, mEnd;
    private LinearLayout mNavgationLayout;
    private ArrayAdapter<String> mKeyAdapter;
    private SuggestionSearch mSuggestionSearch = null;
    private boolean isStart = false;
    private PlanNode stNode;
    private PlanNode enNode;

    private MyAdapter mRouteLineAdapter;
    private RecyclerView mHorizionRecyclerview;
    private LinearLayoutManager hLinearLayoutManager;
    PagingScrollHelper scrollHelper = new PagingScrollHelper();
    private List<DrivingRouteLine> mDrivingRouteLine;
    private List<HashMap<String, Object>> navaPoints = new ArrayList<HashMap<String, Object>>();
    private static final int NORMAL = 0;
    private double mStartLatitude;
    private double mSatrtLontitude;
    private double mEndLatitude;
    private double mEndLontitude;
    private final int MSG_SUCCESS_FLAG = 1;
    private final int MSG_FAIL_FLAG = 2;
    private List<PointBean> mListPoints;
    private List<Coordinate> m_trackList;
    private Map mMap;
    //百度地图导航地址偏差
    private Double LatitudeY = -0.001618888888;
    private Double LongtitudeX =0.0046081388892;
    private String mSDCardPath;
    private static final String APP_FOLDER_NAME = "MyBaiduMap";
    private LinearLayout mLLExit;
    private boolean hasInitSuccess = false;
    //主线程中的handler
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case MSG_SUCCESS_FLAG:
                    startDraw();
                    break;
                case MSG_FAIL_FLAG:
                    Toast.makeText(getActivity(), "获取巡护点信息失败", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };
    private boolean isLocate = false;
    private boolean isClick = false;
    private List<LatLng> mResult;


    private void startDraw() {
        if (PubVar.m_DoEvent.m_ProjectDB.saveProjectInfo(PubVar.m_DoEvent.m_ProjectDB.GetProjectExplorer().GetCoorSystem().GetName(), String.valueOf(PubVar.m_DoEvent.m_ProjectDB.GetProjectExplorer().GetCoorSystem().GetCenterMeridian()))) {
            MainActivity.mMapSettingCallbak.OnClick("", null);
        }
        if (mListPoints != null && mListPoints.size() > 0) {
            m_trackList = new ArrayList<Coordinate>();
            if (m_trackList != null && m_trackList.size() > 0) {
                m_trackList.clear();
            }
            for (int i = 0; i < mListPoints.size(); i++) {
                Coordinate coord = new Coordinate(Double.parseDouble(mListPoints.get(i).getX()), Double.parseDouble(mListPoints.get(i).getY()));
                m_trackList.add(coord);
            }
            PubVar.mBaseLine.UpdateData(m_trackList);
            PubVar.mBaseLine.startCanvas();
        }


    }


    public MainMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainMapFragment newInstance(String param1, String param2) {
        MainMapFragment fragment = new MainMapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private static String GetDDMMSS(double DDD) {
        //DD°MM'SS.SSSS″
        int dd = (int) Math.floor(DDD);
        double MM = (DDD - dd) * 60;
        int mm = (int) Math.floor(MM);

        double SS = (MM - mm) * 60;
        String ss = Tools.ConvertToDigi(SS + "", 4);
        return dd + "°" + mm + "'" + ss + "″";

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_main_map, container, false);
        ButterKnife.bind(this, myView);
        addMapViewOnUI();
        initPubVar();
        initWorkDirectionary();
        startGPS();
        initBusiness();
        if (mParam1 != null) {
            mListPoints = new ArrayList<PointBean>();
            if (NetworkUtil.isNetworkAvailable(getActivity())) {
                getDataPointDraw(mParam1);
            } else {
                List<TraceEntity> mEntity = TraceManager.getInstance().getUnloadTracesByPatrolId(mParam1);
                if (mEntity != null && mEntity.size() > 0) {
                    for (int i = 0; i < mEntity.size(); i++) {
                        PointBean bean = new PointBean();
                        bean.setX(String.valueOf(mEntity.get(i).getX()));
                        bean.setY(String.valueOf(mEntity.get(i).getY()));
                        bean.setLatitude(String.valueOf(mEntity.get(i).getLatitude()));
                        bean.setLongitude(String.valueOf(mEntity.get(i).getLongitude()));
                        bean.setCreatetime(String.valueOf(mEntity.get(i).getGpsTime()));
                        bean.setUserid(String.valueOf(mEntity.get(i).getUserID()));
                        bean.setId(String.valueOf(mEntity.get(i).getId()));
                        bean.setRoundId(String.valueOf(mEntity.get(i).getRoundID()));
                        bean.setHigh(String.valueOf(mEntity.get(i).getHeight()));
                        mListPoints.add(bean);
                    }
                }
                startDraw();
            }

        }

        return myView;
    }

    private void getDataPointDraw(String mParam1) {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        RequestId requestId = new RequestId();
        requestId.setRequestId(mParam1);
        Call<ResponseBody> newPointCall = RetrofitHttp.getRetrofit(builder.build()).GetPointLine("GetTracksFromPatrolId", requestId);
        newPointCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.body() == null) {
                        return;
                    }
                    JSONObject result = new JSONObject(response.body().string());
                    String data = result.getString("data");
                    JSONArray jsonarray = new JSONArray(data);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject object = (JSONObject) new JSONArray(data).get(i);
                        PointBean bean = new PointBean();
                        bean.setX(object.getString("x"));
                        bean.setY(object.getString("y"));
                        bean.setLatitude(object.getString("Latitude"));
                        bean.setLongitude(object.getString("Longitude"));
                        bean.setCreatetime(object.getString("Createtime"));
                        bean.setUserid(object.getString("Userid"));
                        bean.setId(object.getString("Id"));
                        bean.setRoundId(object.getString("roundId"));
                        bean.setHigh(object.getString("high"));
                        mListPoints.add(bean);
                    }

                    Message msg = mHandler.obtainMessage();
                    msg.arg1 = MSG_SUCCESS_FLAG;
                    mHandler.sendMessage(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Message msg = mHandler.obtainMessage();
                msg.arg1 = MSG_FAIL_FLAG;
                mHandler.sendMessage(msg);
            }
        });

    }

    private void startGPS() {
        PubVar.m_GPSLocate.OpenGPS();
        PubVar.m_GPSLocate.SetGpsSetCallback(new ICallback() {
            @Override
            public void OnClick(String Str, Object ExtraStr) {


                if (ExtraStr == null || ((LocationEx) ExtraStr).GetGpsFixMode() != lkGpsFixMode.en3DFix) {
                    ((TextView) myView.findViewById(R.id.status_location)).setText("GPS信号弱");
                } else {
                    if (((LocationEx) ExtraStr).GetGpsLongitude() > 0 && (((LocationEx) ExtraStr).GetGpsLatitude()) > 0) {
                        String coor = GetDDMMSS(((LocationEx) ExtraStr).GetGpsLongitude()) + ", " + GetDDMMSS((((LocationEx) ExtraStr).GetGpsLatitude())) + ", " + Tools.ConvertToDigi(((LocationEx) ExtraStr).GetGpsAltitude() + "", 1);
                        ((TextView) myView.findViewById(R.id.status_location)).setText(coor);
                    } else {
                        ((TextView) myView.findViewById(R.id.status_location)).setText("GPS信号弱");
                    }

                }
            }
        });

    }

    private void initPubVar() {
        PubVar.m_DoEvent = new DoEvent(this.getActivity());
        PubVar.m_BaiduMap = new BaiDuMap(PubVar.m_MapControl);
        PubVar.m_BaiduLocate = new BaiduLocate(PubVar.m_MapControl);

        PubVar.m_GPSMap = new GPSMap(PubVar.m_MapControl);
        PubVar.m_DoEvent.mGPSSatus.SetStatusView((ImageView) myView.findViewById(R.id.iv_status));
        PubVar.m_DoEvent.mGPSSatus.setAreaView((TextView) myView.findViewById(R.id.status_area));
        PubVar.m_DoEvent.mGPSSatus.setLocationView((TextView) myView.findViewById(R.id.status_location));
        PubVar.m_DoEvent.mGPSSatus.setStatusView((TextView) myView.findViewById(R.id.status_status));

        PubVar.m_GPSLocate = new GPSLocate(PubVar.m_MapControl);
        PubVar.m_DoEvent.mRoundLinePresenter = new RoundGPSLine();
        PubVar.m_GPSMap.SetRoundGpsLine(PubVar.m_DoEvent.mRoundLinePresenter);
        PubVar.m_SysDictionaryName = this.getResources().getString(R.string.app_name);


    }


    private void addMapViewOnUI() {
        mLLExit = (LinearLayout) myView.findViewById(R.id.ll_exit);
        mHorizionRecyclerview = (RecyclerView) myView.findViewById(R.id.m_horizion_recyclerview);
        hLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mHorizionRecyclerview.setLayoutManager(hLinearLayoutManager);
        mRouteLineAdapter = new MyAdapter();
        mHorizionRecyclerview.setAdapter(mRouteLineAdapter);
        scrollHelper.setUpRecycleView(mHorizionRecyclerview);
        scrollHelper.setOnPageChangeListener(this);
        scrollHelper.updateLayoutManger();
        scrollHelper.scrollToPosition(0);
        mHorizionRecyclerview.setHorizontalScrollBarEnabled(true);
        mRouteLineAdapter.setOnCancelItemClick(new MyAdapter.CancelItemClick() {
            @Override
            public void onCancelItemClick(View view, int position) {
                mNavgationLayout.setVisibility(GONE);
                mHorizionRecyclerview.setVisibility(GONE);
                PubVar.mBaseLine.clearPaint();
            }
        });


        // 百度初始化搜索模块，注册事件监听
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
        mStart = (AutoCompleteTextView) myView.findViewById(R.id.et_my_start);
        mStart.setText(getResources().getString(R.string.my_location));
        mKeyAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line);
        mStart.setAdapter(mKeyAdapter);
        mStart.setThreshold(1);
        mStart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
                isStart = true;
                if (cs.length() <= 0) {
                    return;
                }
                /* 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新 */
                mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                        .keyword(cs.toString())
                        .city("西安"));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mEnd = (AutoCompleteTextView) myView.findViewById(R.id.et_my_end);
        mKeyAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line);
        mEnd.setAdapter(mKeyAdapter);
        mEnd.setThreshold(1);
        mEnd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
                isStart = false;
                if (cs.length() <= 0) {
                    return;
                }
                /* 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新 */
                mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                        .keyword(cs.toString())
                        .city("西安"));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mNavgationLayout = (LinearLayout) myView.findViewById(R.id.ll_navgation_line);
        mp = new MapControl(this.getActivity());
        RelativeLayout linearLayout = (RelativeLayout) myView.findViewById(R.id.mMainContainer);
        linearLayout.addView(mp, 0);

        RelativeLayout.LayoutParams para = new RelativeLayout.LayoutParams(0, 0);
        para.height = RelativeLayout.LayoutParams.FILL_PARENT;
        para.width = RelativeLayout.LayoutParams.FILL_PARENT;
        para.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        para.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        para.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        para.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        mp.setLayoutParams(para);
        PubVar.m_MapControl = mp;
        mp.invalidate();

    }


    private void initWorkDirectionary() {
        HashMap<String, String> resultHM = SystemSetup.CheckSystemFile(this.getActivity());
        if (resultHM.get("Result").equals("系统主目录缺失")) {
            SelectSystemPath sst = new SelectSystemPath(this.getActivity());
            sst.SetCallback(new ICallback() {
                @Override
                public void OnClick(String Str, Object ExtraStr) {
                    if (Str.equals("退出")) {
                        ActivityManager am = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                        String PackName = getActivity().getPackageName();
                        am.killBackgroundProcesses(PackName);
                        System.exit(0);
                    }
                    if (Str.equals("工作目录")) {
                        SystemSetup.CheckSystemFile(PubVar.m_DoEvent.m_Context);
                        HashMap<String, String> resultHM = SystemSetup.CheckSystemFile(getActivity());
                        if ((resultHM.get("Result").equals("OK"))) {
                            PubVar.m_SysAbsolutePath = resultHM.get("Path");
                            AppSetting.photoPath = PubVar.m_SysAbsolutePath + "/Photo";
                            AppSetting.smallPhotoPath = AppSetting.photoPath + "/samllPhoto";
                            openProject();
                        }
                    }

                }
            });
            sst.ShowDialog();
            return;
        } else {
            if ((resultHM.get("Result").equals("OK"))) {
                PubVar.m_SysAbsolutePath = resultHM.get("Path");
                AppSetting.photoPath = PubVar.m_SysAbsolutePath + "/Photo";
                AppSetting.smallPhotoPath = AppSetting.photoPath + "/samllPhoto";
                openProject();

            } else {

                Tools.ShowMessageBox(this.getActivity(), resultHM.get("Result"), new ICallback() {
                    @Override
                    public void OnClick(String Str, Object ExtraStr) {
                        ActivityManager am = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                        String PackName = getActivity().getPackageName();
                        am.killBackgroundProcesses(PackName);   // API Level至少为8才能使用
                        System.exit(0);
                    }
                });
            }
        }
    }

    private void initBusiness() {

        if (AppSetting.curUser == null) {
            AppSetting.curUser = UserManager.getInstance().getLoginUser(AppSetting.curUserKey);
        }

        if (AppSetting.curUser == null) {
            return;
        }

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                settingOnGoingPatrol();
                //数据补传
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Toast.makeText(getActivity(), "开始补传数据", Toast.LENGTH_LONG).show();
                        new ReuploadManager().reloadAll();
                    }
                }).start();
            }
        });


    }

//    private void getUserDutyArea(String departmentId) {
//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//        RequestId requestId = new RequestId();
//        requestId.setRequestId(departmentId);
//        Call<ResponseBody> getDutyAreaCall = RetrofitHttp.getRetrofit(builder.build()).GetDutyArea("GetDutyArea", requestId);
//        getDutyAreaCall.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                try {
//
//                    if (response.body() == null) {
//                        Log.e("getDutyAreaCall", "response.body() is null ");
//                        return;
//                    }
//
//                    JSONObject result = new JSONObject(response.body().string());
//                    Log.d("DutyAreaString", response.body().string());
//                    if (result.get("success").equals(Boolean.TRUE)) {
//                        JSONArray dataArray = new JSONArray(result.get("data").toString());
//                        if (AppSetting.myDutyArea == null) {
//                            AppSetting.myDutyArea = new HashMap<String, String>();
//                        } else {
//                            AppSetting.myDutyArea.clear();
//                        }
//                        for (int i = 0; i < dataArray.length(); i++) {
//                            try {
//                                JSONObject jsonObject = dataArray.getJSONObject(i);
//                                DutyAreaEntity daEntity = new DutyAreaEntity();
//                                daEntity.setDepId(jsonObject.getString("DepId"));
//                                daEntity.setId(jsonObject.getString("Id"));
//                                daEntity.setName(jsonObject.getString("Name"));
//                                daEntity.setDutyUserNames(jsonObject.getString("DutyUserNames"));
//                                daEntity.setDutymanager(jsonObject.getString("Dutymanager"));
//                                daEntity.setDescription(jsonObject.getString("Description"));
//                                daEntity.setUpdateTime(new Date());
//                                AppSetting.myDutyArea.put(daEntity.getId(), daEntity.getName());
//                                UserManager.getInstance().saveDutyArea(daEntity);
//                            } catch (Exception ex) {
//                                Log.e("ParseAreaDutyJson", ex.getMessage());
//                            }
//                        }
//                    }
//
//                } catch (IOException io) {
//                    Log.e("getDutyArea IO", io.getMessage());
//                } catch (Exception ex) {
//
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Log.e("getUserDutyArea", t.getMessage());
//            }
//        });
//    }

    private void settingOnGoingPatrol() {

        AppSetting.curRound = PatrolManager.getInstance().getOnGoingPatrol(AppSetting.curUserKey);
        if (AppSetting.curRound == null) {
            return;
        }
        Tools.ShowYesOrOtherMessage(getActivity(), "是否继续您上次未完成的巡护？", new ICallback() {
            @Override
            public void OnClick(String Str, Object ExtraStr) {
                if (Str == "YES") {
                    Log.d("巡护", "继续上次未完成的巡护");
                    Log.i("curRound", "round id：" + AppSetting.curRound.getId());
                    Log.i("curRound", "round server id：" + AppSetting.curRound.getServerId());
//                    if (AppSetting.curRound.getServerId() == null || AppSetting.curRound.getServerId().isEmpty()) {
//                                UploadMananger.getInstance().uploadRound(AppSetting.curRound, new ICallback() {
//                                    @Override
//                                    public void OnClick(String Str, Object ExtraStr) {
//
//                                    }
//                        });
//                    }

                    PubVar.m_DoEvent.mRoundLinePresenter.Start(lkDataCollectType.enGps_T, AppSetting.curRound.getStartTime());

                    try {
                        List<TraceEntity> traces = TraceManager.getInstance().getTracesByPatrolId(AppSetting.curRound.getId());
                        Log.d("PatrolId", AppSetting.curRound.getId());
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                        if (traces != null) {
                            Log.d("TraceCount", traces.size() + "");
                            for (TraceEntity trace : traces) {
                                if (trace.getLatitude() != 0 && trace.getLongitude() != 0) {
                                    LocationEx location = new LocationEx();
                                    Log.d("trace", "lon:" + trace.getLongitude() + " lat:" + trace.getLatitude());
                                    location.SetGpsLongitude(trace.getLongitude());
                                    location.SetGpsLatitude(trace.getLatitude());
                                    location.SetGpsAltitude(trace.getHeight());
                                    location.SetGpsDate(dateFormat.format(trace.getGpsTime()));
                                    location.SetGpsTime(timeFormat.format(trace.getGpsTime()));
                                    PubVar.m_DoEvent.mRoundLinePresenter.UpdateGpsPosition(location, false);
                                }


                            }
                        } else {
                            Log.d("NO Trace", "NO Trace");
                        }

                    } catch (Exception ex) {
                        Log.d("GetTrace", ex.getMessage());
                        Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    SharedPreferencesUtils.putBoolean(getActivity(), "mIsRounding", true);
                    initRounding();
                } else {
//                    mIsRounding = false;
                    stopRound();
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Toast.makeText(getActivity(), "开始补传数据", Toast.LENGTH_LONG).show();
                        new ReuploadManager().reloadAll();
                    }
                }).start();

            }
        });
    }

    private void openProject() {
        HashValueObject hvo = new HashValueObject();
        hvo.Value = "巡护系统";
        PubVar.m_HashMap.Add("Project", hvo);


        PubVar.m_DoEvent.m_ProjectDB = new ProjectDB();

        PubVar.m_DoEvent.m_ProjectDB.CreateProject(hvo.Value);

        PubVar.m_DoEvent.m_ProjectDB.OpenProject(hvo.Value, false);


        StaticObject.soProjectSystem.SetCoorSystem(PubVar.m_DoEvent.m_ProjectDB.GetProjectExplorer().GetCoorSystem());
        //在PorjectDB.v1_BKLayerExplorer,底图数据图层
        BKLayerExplorer _BKLayerExplorer = PubVar.m_DoEvent.m_ProjectDB.GetBKLayerExplorer();

        //在PorjectDB.PorjectDB.v1_LayerExplorer,采集数据图层
        LayerExplorer _LayerExplorer = PubVar.m_DoEvent.m_ProjectDB.GetLayerExplorer();

//			//图层渲染器
//			v1_LayerRenderExplorer _LayerRenderExplorer = PubVar.m_DoEvent.m_ProjectDB.GetLayerRenderExplorer();

        //工程浏览器
        ProjectExplorer _ProjectExplorer = PubVar.m_DoEvent.m_ProjectDB.GetProjectExplorer();

        //创建工程空间实例
        if (PubVar.m_Workspace != null) {
            PubVar.m_Workspace.FreeWorkSpace();
        }

        Workspace pWorkspace = new Workspace();
        PubVar.m_Workspace = pWorkspace;

        //设置工程空间坐标系统，工程坐标系统存储于StaticObject.soProjectSystem.
        pWorkspace.SetCoorSystemInfo(StaticObject.soProjectSystem.GetCoorSystem());
        //创建MAP对象，在此处赋最大范围this.setFullExtend(StaticObject.soMapCellIndex.GetCellExtend());
        mMap = new Map(PubVar.m_MapControl);
        mMap.SetScaleBar(PubVar.m_DoEvent.mScaleBar);
        mMap.setEmpty();
        PubVar.m_Map = mMap;
        ImageView iv = (ImageView) myView.findViewById(R.id.iv_scalebar);
        PubVar.m_DoEvent.mScaleBar.SetImageView(iv);

//        PubVar.m_Map.getOverMapLayer().SetOverMapType(lkOverMapType.enGoogle_Satellite);

        //不需要比例尺
        //map.SetScaleBar(PubVar.m_DoEvent.m_ScaleBar);

        //打开采集数据源，其中有渲染底图图层 ，也就是创建GeoLayer
        _LayerExplorer.OpenDataSource(_ProjectExplorer.GetProjectDataFileName());

        //打开底图数据源，其中有渲染底图图层 ，也就是创建GeoLayer
        _BKLayerExplorer.OpenBKDataSource();

        //读取工程的上次视图范围，如果没有则全图显示
        Envelope pEnv = _ProjectExplorer.ReadShowExtend();
//        if (pEnv != null) {
//            PubVar.m_MapControl.setActiveTool(com.DingTu.mapcontainer.Tools.FullScreenSize);
//            PubVar.m_Map.setExtend(pEnv);
////            PubVar.m_Map.Refresh();
//        } else {
//            PubVar.m_MapControl.setActiveTool(com.DingTu.mapcontainer.Tools.FullScreen);
//        }
        PubVar.m_MapControl.setActiveTool(com.DingTu.mapcontainer.Tools.FullScreen);
        PubVar.m_Map.Refresh();
        PubVar.m_MapControl.setActiveTool(ZoomInOutPan);

        Dataset pDataset = PubVar.m_Workspace.GetDatasetById("T5A37395CA75F49F2B0A017DEE983D4EF");
        PubVar.m_DoEvent.mRoundLinePresenter.SetDataset(pDataset);


    }

    @OnClick({R.id.btnFullScreen, R.id.btnStartRound, R.id.btnSelectDraw, R.id.btnReportAlarm, R.id.btnReportEvent, R.id.btnDraw, R.id.btnFinishRound, R.id.btnMoveScreen,R.id.btn_navigate, R.id.btn_beidou, R.id.btn_object, R.id.btn_measure, R.id.btn_search_way, R.id.btn_voice, R.id.btn_clear, R.id.ll_exit})
    public void clickBtn(View view) {

        switch (view.getId()) {
            case R.id.btnMoveScreen:
                PubVar.m_MapControl.setActiveTool(com.DingTu.mapcontainer.Tools.ZoomInOutPan);
                break;
            case R.id.btnFullScreen:
                PubVar.m_MapControl.setActiveTool(com.DingTu.mapcontainer.Tools.FullScreen);
                break;
            case R.id.btnStartRound:
                startNormalPatrol();
                break;
            case R.id.btnReportAlarm:
                break;
            case R.id.btnReportEvent:
                reportEvent();
                break;
            case R.id.btnDraw:
//                isDrawing = !isDrawing;
//                if(isDrawing)
//                {
//                    selectDrawType();
//                }
//                else
//                {
//                    exitDraw();
//                }
                if (AppSetting.curRound != null) {
                    addPatrolPoint();
                } else {
                    Toast.makeText(getActivity(), "请先开始巡护！", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btnFinishRound:
                stopRound();
                break;
            case R.id.btnSelectDraw:
//                PubVar.m_MapControl.setActiveTool(com.DingTu.mapcontainer.Tools.Select);
                if (PubVar.m_DoEvent.m_Agent_Measure == null) {
                    PubVar.m_DoEvent.m_Agent_Measure = new Agent_Measure();
                    PubVar.m_DoEvent.m_Agent_Measure.OnPrepare();
                }
                PubVar.m_DoEvent.m_Agent_Measure.SetZHMode();

                break;

//            case R.id.btn_impot_sharp:
//                // 打开工程图层管理
//                Tools.OpenDialog(new ICallback() {
//
//                    @Override
//                    public void OnClick(String Str, Object ExtraStr) {
//                        LayerManger vpl = new LayerManger();
//                        vpl.SetCallback(new ICallback() {
//                            @Override
//                            public void OnClick(String Str, Object ExtraStr) {
//                                if (ExtraStr == null)
//                                    return;
//                                Layer player = (Layer) ExtraStr;
//                                Dataset pDataset = PubVar.m_Workspace.GetDatasetById(player.GetLayerID());
//                                if (pDataset == null)
//                                    return;
//                                // PubVar.m_Workspace.SetAllGeoLayerNoSelectable();
//                                PubVar.m_Map.ClearSelection();
//                                pDataset.getBindGeoLayer().setSelectable(true);
//                                // 处理界面
//
//                                if (player.GetLayerTypeName().equals("点"))
//                                    PubVar.m_DoEvent.m_GPSPoint.SetDataset(pDataset);
//                                if (player.GetLayerTypeName().equals("线"))
//                                    PubVar.m_DoEvent.m_GPSLine.SetDataset(pDataset);
//                                if (player.GetLayerTypeName().equals("面"))
//                                    PubVar.m_DoEvent.m_GPSPoly.SetDataset(pDataset);
////                                m_MainBottomToolBar.LoadBottomToolBarByType("全部", false);
////                                if (Str.equals("Vector")) {
////                                    m_MainBottomToolBar.LoadBottomToolBarByType("Vector", true);
////                                    m_EditToolbar.ShowToolsItem(player.GetLayerTypeName() + "工具", true);
////                                } else {
////                                    m_MainBottomToolBar.LoadBottomToolBarByType(player.GetLayerTypeName(), true);
////                                    m_EditToolbar.ShowToolsItem(player.GetLayerTypeName() + "工具", false);
////                                }
//
//                                // v1_Layer pLayer =
//                                // PubVar.m_DoEvent.m_ProjectDB.GetLayerExplorer().GetLayerByID(pDataset.getId());
////                                m_GpsInfoManage.SetCurrentLayerName(player);
//
//                            }
//                        });
//                        vpl.ShowDialog();
//
//                    }
//
//                });
//                break;
            case R.id.btn_navigate:
                mNavgationLayout.setVisibility(View.VISIBLE);
                mHorizionRecyclerview.setVisibility(GONE);
                break;

            case R.id.btn_beidou:

                new BeiDouBaoWen().ShowDialog();

                break;
            case R.id.btn_object:
                collectSelectedOjd();
                if (navaPoints != null && navaPoints.size() > 0) {
                    Double x = (Double) navaPoints.get(0).get("D2");
                    Double y = (Double) navaPoints.get(0).get("D3");
                    mEnd.setText(x + "," + y);
                }
                break;
            case R.id.btn_measure:
                if (PubVar.m_DoEvent.m_Agent_Measure != null) {
                    Coordinate coor = PubVar.m_DoEvent.m_Agent_Measure.getFirstPoint();
                    if (coor != null) {
                        mEnd.setText(coor.getX() + "," + coor.getY());
                    } else {
                        Tools.ShowMessageBox("请先用测量工具获取基准点");
                    }
                }

                break;


            case R.id.btn_search_way:
//                PubVar.m_BaiduLocate.OpenGPS();
                // 重置浏览节点的路线数据
                route = null;
                // 处理搜索按钮响应
                String startText = mStart.getText().toString().trim();
                String endText = mEnd.getText().toString().trim();
                mResult = new ArrayList<LatLng>();
                if (!TextUtils.isEmpty(startText) && !TextUtils.isEmpty(endText)) {

                    if (startText.equals(getResources().getString(R.string.my_location))) {
                        Log.d("TAG", "哈哈");
//                        if (PubVar.m_BaiduLocate != null && PubVar.m_BaiduLocate.m_LocationEx != null && PubVar.m_BaiduLocate.m_LocationEx.GetGpsLatitude() > 0.000001 && PubVar.m_BaiduLocate.m_LocationEx.GetGpsLongitude() > 0.000001) {
//                            stNode = PlanNode.withLocation(new LatLng(PubVar.m_BaiduLocate.m_LocationEx.GetGpsLatitude() + LatitudeY, PubVar.m_BaiduLocate.m_LocationEx.GetGpsLongitude() + LongtitudeX));
//                        } else if (PubVar.m_GPSLocate != null) {
//                            if (PubVar.m_GPSLocate.m_LocationEx != null) {
//                                if (PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() > 0.000001 && PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() > 0.000001) {
//                                    stNode = PlanNode.withLocation(new LatLng(PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() + LatitudeY, PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() + LongtitudeX));
//                                    Log.d("TAG", "GPS:" + PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() + LatitudeY + "---" + PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() + LongtitudeX);
//                                }
//                            }
//                        }
                        if (PubVar.m_GPSLocate != null) {
                            if (PubVar.m_GPSLocate.m_LocationEx != null) {
                                if (PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() > 0.000001 && PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() > 0.000001) {
                                    stNode = PlanNode.withLocation(new LatLng(PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude()+LatitudeY, PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude()+LongtitudeX));
                                    mResult.add(new LatLng(PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude()+LatitudeY, PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude()+LongtitudeX));
                                    Log.d("TAG", "GPS:" + PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() + LatitudeY + "---" + PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() + LongtitudeX);
                                }
                            } else {
                                stNode = null;
                            }
                        } else {
                            stNode = null;
                        }
                    } else {
                        stNode = PlanNode.withCityNameAndPlaceName("西安", startText);
                        mResult.add(stNode.getLocation());
                    }

                    if (endText.contains(",")) {
                        String[] EndStr = endText.split(",");
                        double X = Tools.ConvertToDouble(EndStr[0]);
                        double Y = Tools.ConvertToDouble(EndStr[1]);
                        if ((X > 0 && X < 180) && (Y > 0 && Y < 90)) {
                            mEndLatitude = Y + LatitudeY;
                            mEndLontitude = X + LongtitudeX;
                        } else {
                            Coordinate Coor = StaticObject.soProjectSystem.XYToWGS84(X, Y, 0);
                            mEndLatitude = Coor.getY() + LatitudeY;
                            mEndLontitude = Coor.getX() + LongtitudeX;
                        }
                        mResult.add(new LatLng(mEndLatitude, mEndLontitude));
                        enNode = PlanNode.withLocation(new LatLng(mEndLatitude, mEndLontitude));
                    } else {
                        enNode = PlanNode.withCityNameAndPlaceName("西安", endText);
                        mResult.add(enNode.getLocation());
                    }
                    if (stNode != null && enNode != null) {
                        mSearch.drivingSearch((new DrivingRoutePlanOption())
                                .from(stNode).to(enNode).policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_DIS_FIRST)
                                .trafficPolicy(DrivingRoutePlanOption.DrivingTrafficPolicy.ROUTE_PATH_AND_TRAFFIC));
                        nowSearchType = 1;
                    } else {
                        Toast.makeText(getActivity(), "起点或者终点未定位!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "请输入起点或者终点位置", Toast.LENGTH_SHORT).show();
                }
                InputMethodManager manager = ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null) {
                    manager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                break;

            case R.id.btn_voice:

                String start = mStart.getText().toString().trim();
                String end = mEnd.getText().toString().trim();
                if (!TextUtils.isEmpty(start) && !TextUtils.isEmpty(end)) {

                    if (start.equals(getResources().getString(R.string.my_location))) {

//                        if (PubVar.m_BaiduLocate != null && PubVar.m_BaiduLocate.m_LocationEx != null){
//
//                            if(PubVar.m_BaiduLocate.m_LocationEx.GetGpsLatitude() > 0.000001 && PubVar.m_BaiduLocate.m_LocationEx.GetGpsLongitude() > 0.000001){
//                                mStartLatitude = PubVar.m_BaiduLocate.m_LocationEx.GetGpsLatitude();
//                                mSatrtLontitude = PubVar.m_BaiduLocate.m_LocationEx.GetGpsLongitude();
//                            }else if(PubVar.m_GPSLocate != null){
//                                if (PubVar.m_GPSLocate.m_LocationEx != null) {
//                                    if (PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() > 0.000001 || PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() > 0.000001) {
//                                        mStartLatitude = PubVar.m_BaiduLocate.m_LocationEx.GetGpsLatitude();
//                                        mSatrtLontitude = PubVar.m_BaiduLocate.m_LocationEx.GetGpsLongitude();
//                                    }
//                                }
//                            }
//                        }

                        if (PubVar.m_GPSLocate != null && PubVar.m_GPSLocate.m_LocationEx != null) {
                            if (PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() > 0.000001 || PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() > 0.000001) {
                                mStartLatitude = PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude();
                                mSatrtLontitude = PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude();
                            }
                        }
                    } else {
                        stNode = PlanNode.withCityNameAndPlaceName("西安", end);
                    }

                    if (end.contains(",")) {
                        String[] EndStr = end.split(",");
                        double X = Tools.ConvertToDouble(EndStr[0]);
                        double Y = Tools.ConvertToDouble(EndStr[1]);
                        if ((X > 0 && X < 180) && (Y > 0 && Y < 90)) {
                            mEndLatitude = Y;
                            mEndLontitude = X;
                        } else {
                            Coordinate Coor = StaticObject.soProjectSystem.XYToWGS84(X, Y, 0);
                            mEndLatitude = Coor.getY();
                            mEndLontitude = Coor.getX();
                        }
                    } else {
                        enNode = PlanNode.withCityNameAndPlaceName("西安", end);
                    }
                    if (initDirs()) {
                        initNavi();
                    }

                    if (mStartLatitude != 0 && mSatrtLontitude != 0 && mEndLatitude != 0 && mEndLontitude != 0) {
                        BNRoutePlanNode sNode = new BNRoutePlanNode(mSatrtLontitude, mStartLatitude, "西安", "西安", BNRoutePlanNode.CoordinateType.WGS84);
                        BNRoutePlanNode eNode = new BNRoutePlanNode(mEndLontitude, mEndLatitude, "西安", "西安", BNRoutePlanNode.CoordinateType.WGS84);
                        routePlanToNavi(sNode, eNode, NORMAL);
                    }

                } else {
                    Toast.makeText(getActivity(), "请输入起点和终点位置", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btn_clear:
                if (PubVar.mBaseLine != null) {
                    PubVar.mBaseLine.clearPaint();
                }
                break;

//            case R.id.btn_swip:
//                PubVar.m_MapControl.setActiveTool(Shutter);
//                break;
            case R.id.ll_exit:
                mNavgationLayout.setVisibility(GONE);
                break;


        }
        setSelectStatus(view);
    }

    private void routePlanToNavi(BNRoutePlanNode sNode, BNRoutePlanNode eNode, final int from) {
        List<BNRoutePlanNode> list = new ArrayList<>();
        list.add(sNode);
        list.add(eNode);

        BaiduNaviManagerFactory.getCommonSettingManager().setCarNum(getActivity(), "");
        BaiduNaviManagerFactory.getRoutePlanManager().routeplanToNavi(
                list,
                IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                null,
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                                Toast.makeText(getActivity(),
                                        "算路开始", Toast.LENGTH_SHORT).show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                                Toast.makeText(getActivity(),
                                        "算路成功", Toast.LENGTH_SHORT).show();
                                // 躲避限行消息
//                                Bundle infoBundle = (Bundle) msg.obj;
//                                if (infoBundle != null) {
//                                    String info = infoBundle.getString(
//                                            BNaviCommonParams.BNRouteInfoKey.TRAFFIC_LIMIT_INFO
//                                    );
//                                    Log.d("OnSdkDemo", "info = " + info);
//                                }
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                                Toast.makeText(getActivity(), "计算语音导航路线失败!", Toast.LENGTH_SHORT).show();
                                BaiduNaviManagerFactory.getRoutePlanManager()
                                        .removeRequestByHandler(this);
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                                Toast.makeText(getActivity(),
                                        "算路成功准备进入导航！", Toast.LENGTH_SHORT).show();

//                                Intent intent = null;
                                if (from == NORMAL) {
                                    Intent intent = new Intent(getActivity(), VoiceGuideActivity.class);
                                    startActivity(intent);

                                }

                                BaiduNaviManagerFactory.getRoutePlanManager().removeRequestByHandler(this);
                                break;

                        }
                    }
                });
    }


    private void initNavi() {
        if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
            hasInitSuccess = true;
            return;
        }

        BaiduNaviManagerFactory.getBaiduNaviManager().init(getActivity(),
                mSDCardPath, APP_FOLDER_NAME, new IBaiduNaviManager.INaviInitListener() {

                    @Override
                    public void onAuthResult(int status, String msg) {
                        String result;
                        if (0 == status) {
                            result = "key校验成功!";
                        } else {
                            result = "key校验失败, " + msg;
                        }
                    }

                    @Override
                    public void initStart() {
                        Toast.makeText(getActivity(),
                                "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void initSuccess() {
                        Toast.makeText(getActivity(),
                                "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                        hasInitSuccess = true;
                        // 初始化tts
                        initTTS();
                    }

                    @Override
                    public void initFailed() {
                        Toast.makeText(getActivity(),
                                "百度导航引擎初始化失败 ", Toast.LENGTH_SHORT).show();
                    }

//                    @Override
//                    public void initFailed(int errCode) {
//
//                    }
                });
    }

    private void initTTS() {
        // 使用内置TTS
        BaiduNaviManagerFactory.getTTSManager().initTTS(getActivity(), getSdcardDir(), APP_FOLDER_NAME, "17362211");
    }


    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }


    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }


    private void collectSelectedOjd() {
        int pointIndex = 0;
        int lineIndex = 0;
        int polyIndex = 0;

        for (int i = 0; i < PubVar.m_MapControl.getMap().getGeoLayers(lkGeoLayersType.enAll).size(); i++) {
            GeoLayer pGeoLayer = PubVar.m_MapControl.getMap().getGeoLayers(lkGeoLayersType.enAll).GetLayerByIndex(i);
            for (int j = 0; j < pGeoLayer.getSelSelection().getCount(); j++) {
                Geometry geo = pGeoLayer.getSelSelection().getDataset().GetGeometry(pGeoLayer.getSelSelection().getGeometryIndexList().get(j));
                if (geo.GetType() == lkGeoLayerType.enPoint) {
                    Point point = (Point) geo;
                    HashMap<String, Object> hm = new HashMap<String, Object>();
                    pointIndex++;
                    hm.put("D1", "点" + pointIndex);//名称

                    addPointToNavList("点" + pointIndex, point.getCoordinate());
                }

                if (geo.GetType() == lkGeoLayerType.enPolyline) {
                    Polyline line = (Polyline) geo;
                    HashMap<String, Object> hm = new HashMap<String, Object>();
                    lineIndex++;
                    hm.put("D1", "线" + lineIndex);//名称
                    if (line.getCenterPoint() != null) {
                        addPointToNavList("线" + lineIndex, line.getCenterPoint());
                    }
                }

                if (geo.GetType() == lkGeoLayerType.enPolygon) {
                    Polygon poly = (Polygon) geo;
                    HashMap<String, Object> hm = new HashMap<String, Object>();
                    polyIndex++;
                    hm.put("D1", "面" + polyIndex);//名称
                    if (poly.getCenterPoint() != null) {
                        addPointToNavList("面" + polyIndex, poly.getCenterPoint());
                    }
                }
            }


        }
    }

    private void addPointToNavList(String pointName, Coordinate coord) {
        HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put("D1", pointName);//名称
        hm.put("D2", coord.getX());//x坐标
        hm.put("D3", coord.getY());//y坐标
        hm.put("D4", 0);//直线距离
        hm.put("D5", " ");//方位1
        hm.put("D6", 0);//方位1距离
        hm.put("D7", " ");//方位2
        hm.put("D8", 0);//方位2距离
        hm.put("D9", " ");//方位角
        navaPoints.add(hm);
    }


    private void setSelectStatus(View view) {
        boolean isNone = false;
        for (int id : m_SelectToolsBarItemIdList) {
            if (view.getId() == id) {
                view.setSelected(true);
                isNone = true;
            } else {
                View buttonView = myView.findViewById(id);
                buttonView.setSelected(false);
            }
        }


        if (!isNone) {
            PubVar.m_MapControl.setActiveTool(com.DingTu.mapcontainer.Tools.ZoomInOutPan);
        }
    }

    private void reportEvent() {
        if (AppSetting.curRound != null) {
            startActivity(new Intent(getActivity(), EventActivity.class));
        } else {
            Toast.makeText(getActivity(), "请先开始巡护！", Toast.LENGTH_LONG).show();
        }

    }

//    private void takePhoto() {
//        if (AppSetting.curRound == null) {
//            Toast.makeText(getActivity(), "没有正在巡护的路线", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        startActivity(new Intent(getActivity(), RoundActivity.class));
//    }

    private void stopRound() {
        if (AppSetting.curRound == null) {
            Toast.makeText(getActivity(), "没有正在巡护的路线", Toast.LENGTH_SHORT).show();
            return;
        }

        RoundActivity.mFinishCallbak = new ICallback() {
            @Override
            public void OnClick(String Str, Object ExtraStr) {
                if (Str.equals("Finish")) {
                    try {
                        int geometryID = PubVar.m_DoEvent.mRoundLinePresenter.Stop();
                        AppSetting.curRound.setGeometryId(geometryID);
                        try {
                            PatrolManager.getInstance().savePatrol(AppSetting.curRound);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        PubVar.m_DoEvent.mRoundLinePresenter.setGPSTrackList(new ArrayList<Coordinate>());
                        AppSetting.curRound = null;
                        SharedPreferencesUtils.putBoolean(getActivity(), "mIsRounding", false);
                        unloadTraces = true;

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }

//                    PubVar.m_DoEvent.mRoundLinePresenter = new RoundGPSLine();

//                    initRounding();
                }
            }
        };
        startActivity(new Intent(getActivity(), RoundActivity.class));
    }


    private void startNormalPatrol() {
        if (SharedPreferencesUtils.getBoolean(getActivity(), "mIsRounding")) {
            Tools.ShowMessageBox("正在进行巡护，请完成正在进行的巡护后再开始新的巡护！");
            return;
        }

        AlertDialog.Builder startRoundDialog = new AlertDialog.Builder(getActivity());
        startRoundDialog.setCancelable(false);
        startRoundDialog.setTitle("开始巡护");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_startround, null);
        final Spinner spRoundType = (Spinner) layout.findViewById(R.id.spRoundType);
        startRoundDialog.setView(layout);

        String[] arrRoundType = "常规巡护、稽查巡护、责任区巡护".split("、");
        ArrayAdapter<String> roundTypeAdapter = new ArrayAdapter<String>(PubVar.m_DoEvent.m_Context,
                android.R.layout.simple_spinner_item,
                arrRoundType);
        roundTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRoundType.setAdapter(roundTypeAdapter);
        spRoundType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    layout.findViewById(R.id.layoutZeRenqu).setVisibility(View.VISIBLE);
                } else {
                    layout.findViewById(R.id.layoutZeRenqu).setVisibility(GONE);
                }
                if (position == 2) {
                    ((TextView) layout.findViewById(R.id.tvXuHuyuan)).setText("责任人");
                } else {
                    ((TextView) layout.findViewById(R.id.tvXuHuyuan)).setText("巡护人");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String currenetDate = format.format(now);
        ((TextView) layout.findViewById(R.id.etRoundDate)).setText(currenetDate);

        final Spinner spRoundWeather = (Spinner) layout.findViewById(R.id.spWeather);
        String[] arrRoundWeather = "晴、多云、 阴、雾、阵雨、雷阵雨、小雨、中雨、大雨、雨夹雪、暴雨、阵雪、小雪、中雪、大雪、暴雪、冻雨、浮尘、霾".split("、");
        ArrayAdapter<String> roundWeatherAdapter = new ArrayAdapter<String>(PubVar.m_DoEvent.m_Context,
                android.R.layout.simple_spinner_item,
                arrRoundWeather);
        roundWeatherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRoundWeather.setAdapter(roundWeatherAdapter);


        try {

//            if (PubVar.m_BaiduLocate != null && PubVar.m_BaiduLocate.m_LocationEx != null) {
//
//                if (PubVar.m_BaiduLocate.m_LocationEx.GetGpsLatitude() > 0.000001 && PubVar.m_BaiduLocate.m_LocationEx.GetGpsLongitude() > 0.000001) {
//                    Log.d("TAG", "使用百度定位!");
//                    isLocate = true;
//                    ((TextView) layout.findViewById(R.id.tvLon)).setText(Tools.ConvertToDigi(PubVar.m_BaiduLocate.m_LocationEx.GetGpsLongitude() + "", 7));
//                    ((TextView) layout.findViewById(R.id.tvLat)).setText(Tools.ConvertToDigi(PubVar.m_BaiduLocate.m_LocationEx.GetGpsLatitude() + "", 7));
//                    ((TextView) layout.findViewById(R.id.tvHigh)).setText(PubVar.m_BaiduLocate.m_LocationEx.GetGpsAltitude() + "");
//                } else if (PubVar.m_GPSLocate != null) {
//                    if (PubVar.m_GPSLocate.m_LocationEx != null) {
//                        Log.d("TAG", "使用Gps定位!");
//                        isLocate = true;
//                        ((TextView) layout.findViewById(R.id.tvLon)).setText(Tools.ConvertToDigi(PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() + "", 7));
//                        ((TextView) layout.findViewById(R.id.tvLat)).setText(Tools.ConvertToDigi(PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() + "", 7));
//                        ((TextView) layout.findViewById(R.id.tvHigh)).setText(PubVar.m_GPSLocate.m_LocationEx.GetGpsAltitude() + "");
//                    }
//                }
//            } else {
//                isLocate = false;
//                layout.findViewById(R.id.layoutNoLocation).setVisibility(View.VISIBLE);
//                layout.findViewById(R.id.layoutLocation).setVisibility(GONE);
//            }


            if (PubVar.m_GPSLocate != null && PubVar.m_GPSLocate.m_LocationEx != null && PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() > 0.000001 && PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() > 0.000001) {
                isLocate = true;

                ((TextView) layout.findViewById(R.id.tvLon)).setText(Tools.ConvertToDigi(PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() + "", 7));
                ((TextView) layout.findViewById(R.id.tvLat)).setText(Tools.ConvertToDigi(PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() + "", 7));
                ((TextView) layout.findViewById(R.id.tvHigh)).setText(PubVar.m_GPSLocate.m_LocationEx.GetGpsAltitude() + "");

            } else {
                isLocate = false;
                layout.findViewById(R.id.layoutNoLocation).setVisibility(View.VISIBLE);
                layout.findViewById(R.id.layoutLocation).setVisibility(GONE);
            }
        } catch (Exception ex) {
            //TODO:record and upload error log
        }

//        startRoundDialog.setMessage("您要现在开始新的巡护？");
        startRoundDialog.setPositiveButton("开始", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isLocate) {
                    if (isStarting) {
                        return;
                    } else {
                        isStarting = true;
                    }

                    try {

                        PatrolEntity roundEntity = new PatrolEntity();
                        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                        String date = sDateFormat.format(new Date());
                        String userId = AppSetting.curUserKey;
                        roundEntity.setUserID(userId);
                        String id = userId + date.toString();
                        roundEntity.setId(id);
                        roundEntity.setStartTime(new Date());
                        String strRoundType = spRoundType.getSelectedItem().toString();
                        roundEntity.setRoundName(((TextView) ((AlertDialog) dialog).findViewById(R.id.etRoundName)).getText() + "");
                        roundEntity.setUserNames(((TextView) ((AlertDialog) dialog).findViewById(R.id.etXunHuYuan)).getText() + "");
                        roundEntity.setRoundStatus(0);
                        roundEntity.setWeather(spRoundWeather.getSelectedItem().toString());
                        if (strRoundType.equals("常规巡护")) {
                            roundEntity.setRoundType(21);
                        } else if (strRoundType.equals("稽查巡护")) {
                            roundEntity.setRoundType(22);
                            roundEntity.setDutyId(((TextView) layout.findViewById(R.id.et_DutyArea)).getText() + "");

                        } else if (strRoundType.equals("责任区巡护")) {
                            roundEntity.setRoundType(23);
                            roundEntity.setDutyId(((TextView) layout.findViewById(R.id.et_DutyArea)).getText() + "");
                        } else {
                            roundEntity.setRoundType(1);
                        }

                        PatrolManager.getInstance().savePatrol(roundEntity);

                        try {
                            //保存起始点坐标

                            double longitude = Double.valueOf(((TextView) ((AlertDialog) dialog).findViewById(R.id.tvLon)).getText().toString());
                            double latitude = Double.valueOf(((TextView) ((AlertDialog) dialog).findViewById(R.id.tvLat)).getText().toString());
                            double altitude = Double.valueOf(((TextView) ((AlertDialog) dialog).findViewById(R.id.tvHigh)).getText().toString());

                            if (longitude > 0.0001 && latitude > 0.0001) {

                                PatrolPointEntity patrolPointEntity = new PatrolPointEntity();
                                patrolPointEntity.setUserID(AppSetting.curUser.getUserID());
                                patrolPointEntity.setRoundID(roundEntity.getId());
                                patrolPointEntity.setLatitude(latitude);
                                patrolPointEntity.setLongitude(longitude);
                                patrolPointEntity.setHeight(altitude);
                                patrolPointEntity.setGpsTime(new Date());
                                Log.e("起始点", "Log:" + longitude + "Lat:" + latitude + "alt:" + altitude);
                                Coordinate coordinate = StaticObject.soProjectSystem.WGS84ToXY(longitude, latitude, altitude);
                                NumberFormat nf = NumberFormat.getInstance();
                                nf.setGroupingUsed(false);
                                patrolPointEntity.setX(nf.format(coordinate.getX()));
                                patrolPointEntity.setY(nf.format(coordinate.getY()));
                                String name = StaticObject.soProjectSystem.GetCoorSystem().GetName();
                                if (name.equals("西安80坐标")) {
                                    patrolPointEntity.setSrid("2381");
                                } else if (name.equals("北京54坐标")) {
                                    patrolPointEntity.setSrid("2433");
                                } else if (name.equals("2000国家大地坐标系")) {
                                    patrolPointEntity.setSrid("4545");
                                } else if (name.equals("WGS-84坐标")) {
                                    patrolPointEntity.setSrid("4326");
                                }
                                patrolPointEntity.setPointType("0");
                                roundEntity.setStartPoint(patrolPointEntity);
                                PatrolManager.getInstance().savePatrolPoint(patrolPointEntity);
                            }
                        } catch (Exception ex) {
                            Toast.makeText(getActivity(), "保存起始点坐标失败" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        SharedPreferencesUtils.putBoolean(getActivity(), "mIsRounding", true);
                        initRounding();
                        AppSetting.curRound = roundEntity;
                        PubVar.m_DoEvent.mRoundLinePresenter.Start(lkDataCollectType.enGps_T, AppSetting.curRound.getStartTime());

                        dialog.dismiss();

                        try {
                            UploadMananger.getInstance().uploadRound(roundEntity, new ICallback() {
                                @Override
                                public void OnClick(String Str, Object ExtraStr) {
                                    if (Str.equals("success")) {
//                                AppSetting.curRound.setServerId(AppSetting.curRound.getServerId());
                                        try {
                                            if (AppSetting.curRound.getStartPoint() != null) {
                                                uploadStartPoint(AppSetting.curRound.getStartPoint());
                                            }
                                        } catch (Exception ex) {
                                            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
                                        }

                                        Toast.makeText(getActivity(), "上传成功", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } catch (Exception ex) {

                        }

                        isStarting = false;

                    } catch (Exception ex) {
                        isStarting = false;
                        ex.printStackTrace();
                    }
                } else {
                    Tools.ShowMessageBox("请定位成功后，开始巡护！");
                }
            }
        });

        startRoundDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        startRoundDialog.show();
    }

    private void initRounding() {
        if (SharedPreferencesUtils.getBoolean(getActivity(), "mIsRounding")) {
            PubVar.m_DoEvent.mRoundLinePresenter.setTraceCallback(new ICallback() {
                @Override
                public void OnClick(String Str, final Object ExtraStr) {

                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            LocationEx location = (LocationEx) ExtraStr;
                            TraceEntity traceEntity = new TraceEntity();
                            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            if (location.GetGpsDate() == null || location.GetGpsDate().isEmpty() || location.GetGpsDate() == null || location.GetGpsTime().isEmpty()) {
                                try {
                                    traceEntity.setGpsTime(sd.parse(location.GetGpsDate() + " " + location.GetGpsTime()));
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                    Log.d("SaveTrace", "Time is Null");
                                    traceEntity.setGpsTime(new Date());
                                }
                            } else {
                                Log.d("SaveTrace", "Time Null");
                                traceEntity.setGpsTime(new Date());
                            }

                            if (AppSetting.curRound != null) {
                                traceEntity.setRoundID(AppSetting.curRound.getId());
                            } else {
                                Toast.makeText(getActivity(), "Current round is null", Toast.LENGTH_SHORT).show();
                            }

                            try {
                                final Coordinate coord = StaticObject.soProjectSystem.WGS84ToXY(location.GetGpsLongitude(), location.GetGpsLatitude(), location.GetGpsAltitude());

                                traceEntity.setUserID(AppSetting.curUserKey);
                                if (location.GetGpsLatitude() > 0 && location.GetGpsLongitude() > 0) {
                                    traceEntity.setHeight(location.GetGpsAltitude());
                                    traceEntity.setLatitude(location.GetGpsLatitude());
                                    traceEntity.setLongitude(location.GetGpsLongitude());
                                    NumberFormat nf = NumberFormat.getInstance();
                                    nf.setGroupingUsed(false);
                                    traceEntity.setX(nf.format(coord.getX()));
                                    traceEntity.setY(nf.format(coord.getY()));
                                }
                                String name = StaticObject.soProjectSystem.GetCoorSystem().GetName();
                                if (name.equals("西安80坐标")) {
                                    traceEntity.setSrid("2381");
                                } else if (name.equals("北京54坐标")) {
                                    traceEntity.setSrid("2433");
                                } else if (name.equals("2000国家大地坐标系")) {
                                    traceEntity.setSrid("4545");
                                } else if (name.equals("WGS-84坐标")) {
                                    traceEntity.setSrid("4326");
                                }
                                traceEntity.setUploadStatus(0);
                                traceEntity.setSaveTime(new Date());

                                TraceManager.getInstance().SaveTrace(traceEntity);

                                AppSetting.curRound.setLastTrace(traceEntity);

                                try {
                                    String curArea = findCurrentArea(coord);
                                    if (mapViewAreaView != null) {
                                        mapViewAreaView.setText(curArea);
                                    }

                                    ((TextView) myView.findViewById(R.id.status_area)).setText(curArea);

                                } catch (Exception ex) {
                                    Log.e("curArea", ex.getMessage());
                                }


                                Log.d("轨迹存储", "traceEntity:" + traceEntity.getId() + " lat: " + traceEntity.getLatitude() + " lon:" + traceEntity.getLongitude() + " time:" + traceEntity.getGpsTime().toString());

                                if (AppSetting.curRound.getServerId() == null || AppSetting.curRound.getServerId().isEmpty()) {
                                    UploadMananger.getInstance().uploadRound(AppSetting.curRound, new ICallback() {
                                        @Override
                                        public void OnClick(String Str, Object ExtraStr) {
                                            if (ExtraStr != null) {
                                                AppSetting.curRound.setServerId(ExtraStr + "");
                                            }
                                        }
                                    });
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


                                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                                    Call<ResponseBody> newTraceCall = RetrofitHttp.getRetrofit(builder.build()).uploadTrace("InsertTrackData", httpTraceModel);
                                    final TraceEntity trace = traceEntity;
                                    newTraceCall.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            if (response.body() == null) {
                                                Log.e("upload trace", "response.body() is null");
                                                return;
                                            }

                                            try {
                                                if (response.body().string().contains("true")) {
                                                    Log.d("上传轨迹", trace.getGpsTime().toString());
                                                    trace.setUploadStatus(1);
//                                                    DatabaseCreator.getInstance(mContext).getDatabase().traceDao().insertTraces(trace);
                                                    TraceManager.getInstance().SaveTrace(trace);
                                                } else {
                                                    Log.e("上传轨迹", response.body().string());

                                                }
                                            } catch (IOException io) {
                                                Log.e("上传轨迹失败", io.getMessage());
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

                                Log.e("存储迹失败：", ex.getMessage());

                            }
                        }
                    });
                }
            });


        } else {
            PubVar.m_DoEvent.mRoundLinePresenter.Stop();
            PubVar.m_DoEvent.mRoundLinePresenter.setTraceCallback(null);
        }
    }

    private void uploadStartPoint(final PatrolPointEntity patrolPointEntity) {
        UploadMananger.getInstance().uploadPatrolPoint(patrolPointEntity, AppSetting.curRound.getServerId(), new ICallback() {
            @Override
            public void OnClick(String Str, Object ExtraStr) {
                if (Str.equals("success")) {
                    try {
                        patrolPointEntity.setUploadStatus(1);
                        PatrolManager.getInstance().savePatrolPoint(patrolPointEntity);
                    } catch (Exception ex) {
                        Toast.makeText(getActivity(), "上传起始点" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getActivity(), "起始点已上传", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getActivity(), "上传起始点" + Str, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void saveStartPoint(LocationEx locationEx, boolean patrolUploaded) {
        if (locationEx == null || locationEx.GetGpsLatitude() < 0.000001 || locationEx.GetGpsLongitude() < 0.000001) {
            Log.e("nostartpoint", "起始点数值为0");
            return;
        }

        final PatrolPointEntity patrolPointEntity = new PatrolPointEntity();
        patrolPointEntity.setUserID(AppSetting.curUser.getUserID());
        patrolPointEntity.setRoundID(AppSetting.curRound.getId());
        patrolPointEntity.setLatitude(locationEx.GetGpsLatitude());
        patrolPointEntity.setLongitude(locationEx.GetGpsLongitude());
        patrolPointEntity.setGpsTime(new Date());
        patrolPointEntity.setHeight(locationEx.GetGpsAltitude());
        Coordinate coordinate = StaticObject.soProjectSystem.WGS84ToXY(locationEx.GetGpsLongitude(),
                locationEx.GetGpsLatitude(),
                locationEx.GetGpsAltitude());
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        patrolPointEntity.setX(nf.format(coordinate.getX()));
        patrolPointEntity.setY(nf.format(coordinate.getY()));
//            TODO:auto get srid;
        String name = StaticObject.soProjectSystem.GetCoorSystem().GetName();
        if (name.equals("西安80坐标")) {
            patrolPointEntity.setSrid("2381");
        } else if (name.equals("北京54坐标")) {
            patrolPointEntity.setSrid("2433");
        } else if (name.equals("2000国家大地坐标系")) {
            patrolPointEntity.setSrid("4545");
        } else if (name.equals("WGS-84坐标")) {
            patrolPointEntity.setSrid("4326");
        }
        patrolPointEntity.setPointType("0");
        patrolPointEntity.setPointName("起始点");

        try {
            PatrolManager.getInstance().savePatrolPoint(patrolPointEntity);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(getActivity(), "起始点" + ex.getMessage(), Toast.LENGTH_SHORT);
            return;
        }

        if (patrolUploaded) {
//            if (AppSetting.curRound.getServerId() != null && !(AppSetting.curRound.getServerId().isEmpty())) {
            UploadMananger.getInstance().uploadPatrolPoint(patrolPointEntity, AppSetting.curRound.getServerId(), new ICallback() {
                @Override
                public void OnClick(String Str, Object ExtraStr) {
                    if (Str.equals("success")) {
                        try {
                            patrolPointEntity.setUploadStatus(1);
                            PatrolManager.getInstance().savePatrolPoint(patrolPointEntity);
                        } catch (Exception ex) {
                            Toast.makeText(getActivity(), "上传起始点异常！", Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(getActivity(), "起始点已上传", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getActivity(), "上传起始点" + Str, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
//        }

    }

    private void addPatrolPoint() {
        AlertDialog.Builder addPointDialog = new AlertDialog.Builder(getActivity());
        addPointDialog.setCancelable(true);
        addPointDialog.setTitle("记录巡点");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_addpatrolpoint, null);
        addPointDialog.setView(layout);

        if (PubVar.m_GPSLocate != null && PubVar.m_GPSLocate.m_LocationEx != null && PubVar.m_GPSLocate.m_LocationEx.GetGpsFixMode() == lkGpsFixMode.en3DFix) {
            try {
                ((TextView) layout.findViewById(R.id.etPointLon)).setText(Tools.ConvertToDigi(PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() + "", 7));
                ((TextView) layout.findViewById(R.id.etPointLat)).setText(Tools.ConvertToDigi(PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() + "", 7));
                ((TextView) layout.findViewById(R.id.etPointAlt)).setText(PubVar.m_GPSLocate.m_LocationEx.GetGpsAltitude() + "");
            } catch (Exception ex) {

            }
        }

        ((TextView) layout.findViewById(R.id.etPointLon)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PubVar.m_GPSLocate != null && PubVar.m_GPSLocate.m_LocationEx != null && PubVar.m_GPSLocate.m_LocationEx.GetGpsFixMode() == lkGpsFixMode.en3DFix) {
                    try {
                        ((TextView) layout.findViewById(R.id.etPointLon)).setText(Tools.ConvertToDigi(PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() + "", 7));
                        ((TextView) layout.findViewById(R.id.etPointLat)).setText(Tools.ConvertToDigi(PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() + "", 7));
                        ((TextView) layout.findViewById(R.id.etPointAlt)).setText(PubVar.m_GPSLocate.m_LocationEx.GetGpsAltitude() + "");
                    } catch (Exception ex) {

                    }
                } else {
                    Toast.makeText(v.getContext(), "请开启GPS并在开阔地带精确定位,然后刷新位置!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addPointDialog.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {
                    if (PubVar.m_GPSLocate != null && PubVar.m_GPSLocate.m_LocationEx != null
                            && PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude() > 0
                            && PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude() > 0) {
                        PatrolPointEntity pointEntity = new PatrolPointEntity();
                        pointEntity.setHeight(PubVar.m_GPSLocate.m_LocationEx.GetGpsAltitude());
                        pointEntity.setLatitude(PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude());
                        pointEntity.setLongitude(PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude());
//                       pointEntity.setWarningtime(PubVar.m_GPSLocate.m_LocationEx.GetGpsTime());
                        pointEntity.setGpsTime(new Date());
                        Coordinate coord = StaticObject.soProjectSystem.WGS84ToXY(PubVar.m_GPSLocate.m_LocationEx.GetGpsLongitude(),
                                PubVar.m_GPSLocate.m_LocationEx.GetGpsLatitude(),
                                PubVar.m_GPSLocate.m_LocationEx.GetGpsAltitude());

                        pointEntity.setPointType("1");
                        String name = StaticObject.soProjectSystem.GetCoorSystem().GetName();
                        if (name.equals("西安80坐标")) {
                            pointEntity.setSrid("2381");
                        } else if (name.equals("北京54坐标")) {
                            pointEntity.setSrid("2433");
                        } else if (name.equals("2000国家大地坐标系")) {
                            pointEntity.setSrid("4545");
                        } else if (name.equals("WGS-84坐标")) {
                            pointEntity.setSrid("4326");
                        }
                        NumberFormat nf = NumberFormat.getInstance();
                        nf.setGroupingUsed(false);
                        pointEntity.setX(nf.format(coord.getX()));
                        pointEntity.setY(nf.format(coord.getY()));
                        pointEntity.setRoundID(AppSetting.curRound.getId());
                        //TODO:
//                       pointEntity.setUserID(AppSetting.curUser.getUserID());
                        pointEntity.setUserID(AppSetting.curUserKey);
                        try {
                            PatrolManager.getInstance().savePatrolPoint(pointEntity);
                        } catch (Exception ex) {
                            Toast.makeText(getActivity(), "保存失败", Toast.LENGTH_SHORT);
                            return;
                        }

                        if (AppSetting.curRound.getServerId() != null && !(AppSetting.curRound.getServerId().isEmpty())) {
                            UploadMananger.getInstance().uploadPatrolPoint(pointEntity, AppSetting.curRound.getServerId(), new ICallback() {
                                @Override
                                public void OnClick(String Str, Object ExtraStr) {
                                    if (Str.equals("success")) {
                                        Toast.makeText(getActivity(), "中间点已上传", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                        dialog.dismiss();

                    } else {
                        Toast.makeText(getActivity(), "请开启GPS并在开阔地带精确定位!", Toast.LENGTH_SHORT).show();
                        return;
                    }


                } catch (Exception ex) {
                    Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

        addPointDialog.setNegativeButton("返回", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        addPointDialog.show();
    }

//    public void UpdateRoundStatus(double len, Date startTime, LocationEx location) {
//        String mStatusString;
//        String mLocationString;
//        String mAreaString;
//
//        if (startTime == null || len < 0.01) {
//            mStatusString = "未开始巡护";
//        } else {
//            mStatusString = "巡护中," + Tools.ReSetDistance(len, true);
//        }
//
//
//        if (location != null) {
//            mLocationString = Tools.ConvertToDigi(location.GetGpsLatitude(), 7) + "," + Tools.ConvertToDigi(location.GetGpsLongitude(), 7) + "," + Tools.ConvertToDigi(location.GetGpsAltitude(), 1);
//            mAreaString = "安家岐保护站(实验区)";
//        } else {
//            mLocationString = "未定位";
//            mAreaString = "未定位";
//        }
//    }

    private String findCurrentArea(Coordinate curPoint) {
        //选择容忍距离
        double Tolerance = PubVar.m_MapControl.getMap().ToMapDistance(Tools.DPToPix(10));

        String strBaoHuQu = "";
        String strGongnengqu = "";
        //1-在采集数据层内选择
        int LayerCount = PubVar.m_MapControl.getMap().getGeoLayers(lkGeoLayersType.enVectorBackground).size();
        Log.d("LayerCount", LayerCount + "");
        for (int i = LayerCount - 1; i >= 0; i--) {
            GeoLayer pGeoLayer = PubVar.m_MapControl.getMap().getGeoLayers(lkGeoLayersType.enVectorBackground).GetLayerByIndex(i);

            for (Geometry StGeometry : pGeoLayer.getDataset().GetGeometryList()) {
                try {
                    if (pGeoLayer.getDataset().getType() == lkGeoLayerType.enPolygon &&
                            pGeoLayer.GetAliasName().contains("保护站")) {
                        Polygon StPolygon = (Polygon) StGeometry;
                        Log.d("PolygonID", StPolygon.getSysId() + "");
                        if (StPolygon.HitTest(curPoint, Tolerance)) {
                            Log.d("SelectID", StPolygon.getSysId() + "");
                            String SQL = "select F3 from " + pGeoLayer.getDataset().getDataTableName() + " where SYS_ID = " + StPolygon.getSysId();
                            SQLiteDataReader DR = pGeoLayer.getDataset().getDataSource().Query(SQL);
                            if (DR != null) {
                                if (DR.Read()) {
                                    strBaoHuQu = DR.GetString("F3");

                                }

                            }
                            DR.Close();
                            break;
                        }
                    }

                } catch (Exception ex) {

                }

                try {
                    if (pGeoLayer.getDataset().getType() == lkGeoLayerType.enPolygon &&
                            pGeoLayer.GetAliasName().contains("功能区")) {
                        Polygon StPolygon = (Polygon) StGeometry;
                        if (StPolygon.HitTest(curPoint, Tolerance)) {
                            String SQL = "select F2 from " + pGeoLayer.getDataset().getDataTableName() + " where SYS_ID = " + StPolygon.getSysId();
                            SQLiteDataReader DR = pGeoLayer.getDataset().getDataSource().Query(SQL);
                            if (DR != null) {
                                if (DR.Read()) {
                                    strGongnengqu = DR.GetString("F2");
                                    Log.d("SelectID", StPolygon.getSysId() + "");
                                }

                            }
                            DR.Close();
                            break;
                        }
                    }
                } catch (Exception ex) {

                }

            }

        }
        if (strGongnengqu == null || strGongnengqu.isEmpty()) {
            return strBaoHuQu;
        } else {
            return strBaoHuQu + "(" + strBaoHuQu + ")";
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {


    }


    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult result) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getActivity(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            if (result.getRouteLines().size() > 0) {
                mNavgationLayout.setVisibility(GONE);
                mHorizionRecyclerview.setVisibility(View.VISIBLE);
                mRouteLineAdapter.updata(result.getRouteLines());
                scrollHelper.scrollToPosition(0);
                mDrivingRouteLine = result.getRouteLines();
                DriverRouteData data = new DriverRouteData();
                data.setData(result.getRouteLines().get(0), mResult);
                PubVar.mBaseLine.UpdateData(data.getData());
                PubVar.mBaseLine.startCanvas();
            } else {
                Log.d("route result", "结果数<0");
                return;
            }


        }
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult result) {

    }


    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            return;
        }

        List<String> suggest = new ArrayList<>();
        for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
            if (info.key != null) {
                suggest.add(info.key);
            }
        }

        mKeyAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line,
                suggest);
        if (isStart) {
            mStart.setAdapter(mKeyAdapter);
        } else {
            mEnd.setAdapter(mKeyAdapter);
        }
        mKeyAdapter.notifyDataSetChanged();


    }

    @Override
    public void onPageChange(int index) {
        if (mDrivingRouteLine != null && mDrivingRouteLine.size() > 0) {
            DriverRouteData data = new DriverRouteData();
            data.setData(mDrivingRouteLine.get(index), mResult);
            PubVar.mBaseLine.UpdateData(data.getData());
            PubVar.mBaseLine.startCanvas();
        }


    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mMap != null) {
            mMap = null;
        }
    }
}
