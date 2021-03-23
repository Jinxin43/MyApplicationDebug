package com.example.dingtu2.myapplication.BlueTooth;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import com.example.dingtu2.myapplication.R;
import com.pancoit.bdboxsdk.bdsdk.BeidouHandler;

import java.util.ArrayList;
import java.util.List;


/**
 * - @Description:  蓝牙连接弹框
 * - @Author:  LXJ
 * - @Time:  2018/12/6 18:20
 */

public class BluetoothDialog extends DialogFragment implements BluetoothDeviceAdapter.OnItemClickListener, View.OnClickListener {
    private Activity mContext;
    //设备列表
    private RecyclerView deviceListView;
    //设备列表数据
    private List<BluetoothDevice> deviceList;
    //设备列表适配器
    private BluetoothDeviceAdapter adapter;
    // 蓝牙设备适配器
    private BluetoothAdapter mBluetoothAdapter;


    public BluetoothDialog() {

    }

    public void setData(Activity context) {
        this.mContext = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            dialog.getWindow().setLayout((int) (dm.widthPixels * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viewContent = inflater.inflate(R.layout.dialog_bluetooth, container, false);
        //去掉dialog默认标题
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设备列表
        deviceListView = (RecyclerView) viewContent.findViewById(R.id.device_list);
        initFindBluetooth();
        //确定按钮
        Button cancelBtn = (Button) viewContent.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(this);
        return viewContent;
    }

    /**
     * 初始化设备列表
     */
    private void initFindBluetooth() {
        deviceList = new ArrayList<>();
        adapter = new BluetoothDeviceAdapter(getActivity(), deviceList, this);
        deviceListView.setAdapter(adapter);
        deviceListView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        // 获取蓝牙管理器
        BluetoothManager bm = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        // 获取默认适配器
        mBluetoothAdapter = bm.getAdapter();
        findBluetoothDevice();
    }

    /**
     * 搜索蓝牙设备
     */
    private void findBluetoothDevice() {
        deviceList.clear();
        //时时的扫描蓝牙的状态
        //开启蓝牙搜索
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }


    /**
     * 蓝牙适配器BluetoothAdapter
     * 如果你只需要搜索指定UUID的外设，你可以调用 startLeScan(UUID[], BluetoothAdapter.LeScanCallback)方法。
     * 其中UUID数组指定你的应用程序所支持的GATT Services的UUID。
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        //开始搜索蓝牙设备
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (device.getName() == null) return;
                    if (deviceList.size() == 0) {
                        adapter.addListItem(device);
                    }
                    boolean isExist = false;
                    for (int i = 0; i < deviceList.size(); i++) {
                        if (device.getAddress().equals(deviceList.get(i).getAddress())) {
                            isExist = true;
                        }
                    }
                    if (!isExist) {
                        adapter.addListItem(device);
                    }
                }
            });
        }
    };

    @Override
    public void OnItemClickListener(View view, int postion, BluetoothDevice device) {
        //注册BeidouContentService,调用系统的方法，得到地址
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Toast.makeText(mContext, "请打开蓝牙", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }
        //开始连接蓝牙
        BeidouHandler.getInstance().startConnectBluetooth(device);
        BeiDouBaoWen.showProgressDialog(getActivity().getString(R.string.connect_bluetooth_load));
        dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_btn:
                dismiss();
                break;
        }
    }
}
