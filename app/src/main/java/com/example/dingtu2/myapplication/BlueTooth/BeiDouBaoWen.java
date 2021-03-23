package com.example.dingtu2.myapplication.BlueTooth;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.DingTu.Base.PubVar;
import com.DingTu.Base.Tools;
import com.DingTu.Cargeometry.Coordinate;
import com.DingTu.Controls.ListViewForScrollView;
import com.DingTu.Map.StaticObject;
import com.bdsdk.dto.CardLocationDto;
import com.example.dingtu2.myapplication.R;
import com.example.dingtu2.myapplication.http.RetrofitHttp;
import com.example.dingtu2.myapplication.model.BDBean;
import com.example.dingtu2.myapplication.model.BindModel;
import com.example.dingtu2.myapplication.model.UnBindModel;
import com.hailiao.hailiaosdk.constant.MsgType;
import com.pancoit.bdboxsdk.bdsdk.AgentListener;
import com.pancoit.bdboxsdk.bdsdk.BeidouHandler;
import com.pancoit.bdboxsdk.constant.BdBoxParams;
import com.pancoit.bdboxsdk.entity.CardMessage;
import com.pancoit.bdboxsdk.entity.ReceiptType;
import com.pancoit.bdboxsdk.entity.UserMessage;
import com.pancoit.bdboxsdk.util.ContentConvertTOBoxMessage;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Looper.getMainLooper;
import static com.baidu.platform.comapi.util.UiThreadUtil.runOnUiThread;

public class BeiDouBaoWen implements AgentListener {

    private static final int UNBIND = 0x001;
    private static final int BIND = 0x002;
    private final TextView mTvMessageContent, mTvDeviceStatus;
    private final ImageView boxSignalImg1, boxSignalImg2, boxSignalImg3, boxSignalImg4, boxSignalImg5, boxSignalImg6, boxSignalImg7, boxSignalImg8, boxSignalImg9, boxSignalImg10;
    private final LinearLayout mSign;
    private final ListViewForScrollView mLvSendList, mLvReciveList;
    private final BeidouMessageAdapter mSendAdpter, mReciveAdpter;
    private final LinearLayout mMessageList;
    private MsgDB mDB;
    private v1_FormTemplate _Dialog = null;
    private TextView tvMessageOut;
    private Button btnSendMsg;
    private Button btnSearchBle;
    //
//    private LinearLayoutManager mLinearLayoutManager;
//    private BluetoothAdapter mBluetoothAdapter;
    // 控制循环,当值为1时，表示是第一个设备
//    private BluetoothDeviceAdapter mDeviceAdapter;
//    private RecyclerView ryDevice;
//    private ArrayList<BluetoothDeviceEntity> mDeviceList = new ArrayList<BluetoothDeviceEntity>();
//    private boolean mScanning = false;
    private static ProgressDialog progressDialog;
    private String mCardId;
    private EditText msgContent, toId;
    private List<Msg> mSendList, mReciveList;

    //创建一个Handler
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UNBIND) {
                tvMessageOut.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.dis_connect));
                tvMessageOut.setVisibility(View.GONE);
                btnSearchBle.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.scan_blooth_device));
                mTvDeviceStatus.setVisibility(View.GONE);
                mSign.setVisibility(View.GONE);
            } else if (msg.what == BIND) {
                hideProgressDialog();
                tvMessageOut.setVisibility(View.VISIBLE);
                btnSearchBle.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.device_disconnect));
                tvMessageOut.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.now_connect_success));
                mTvDeviceStatus.setVisibility(View.VISIBLE);
                mSign.setVisibility(View.VISIBLE);
            }

        }
    };


    public BeiDouBaoWen() {
        _Dialog = new v1_FormTemplate(PubVar.m_DoEvent.m_Context);
        _Dialog.SetOtherView(R.layout.dialog_beidoubaowen);
        _Dialog.ReSetSize(0.8f, 0.6f);

        // 设置标题
//        _Dialog.SetCaption(Tools.ToLocale("北斗设备"));
        _Dialog.SetCaption(_Dialog.getContext().getResources().getString(R.string.beidou_txt));

        Tools.ToLocale(_Dialog.findViewById(R.id.tvLocaleText));

        tvMessageOut = (TextView) _Dialog.findViewById(R.id.tv_Message_status);
        mTvMessageContent = (TextView) _Dialog.findViewById(R.id.tv_Message_content);
        mTvDeviceStatus = (TextView) _Dialog.findViewById(R.id.tv_device_status);
        btnSendMsg = (Button) _Dialog.findViewById(R.id.btnSendMsg);
        mSign = (LinearLayout) _Dialog.findViewById(R.id.ll_layout_sign);
        mMessageList = (LinearLayout) _Dialog.findViewById(R.id.ll_message_list);
        boxSignalImg1 = (ImageView) _Dialog.findViewById(R.id.box_signal_img1);
        boxSignalImg2 = (ImageView) _Dialog.findViewById(R.id.box_signal_img2);
        boxSignalImg3 = (ImageView) _Dialog.findViewById(R.id.box_signal_img3);
        boxSignalImg4 = (ImageView) _Dialog.findViewById(R.id.box_signal_img4);
        boxSignalImg5 = (ImageView) _Dialog.findViewById(R.id.box_signal_img5);
        boxSignalImg6 = (ImageView) _Dialog.findViewById(R.id.box_signal_img6);
        boxSignalImg7 = (ImageView) _Dialog.findViewById(R.id.box_signal_img7);
        boxSignalImg8 = (ImageView) _Dialog.findViewById(R.id.box_signal_img8);
        boxSignalImg9 = (ImageView) _Dialog.findViewById(R.id.box_signal_img9);
        boxSignalImg10 = (ImageView) _Dialog.findViewById(R.id.box_signal_img10);
        BeidouHandler.agentListeners.add(this);

        btnSearchBle = (Button) _Dialog.findViewById(R.id.searchBluetooth);
        btnSearchBle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //   searchBluetoothDevice();
                final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    PubVar.m_DoEvent.m_Context.startActivity(enableBleIntent);
                    return;
                }
                if (BdBoxParams.isBoxConnectNormal) {
                    BeidouHandler.getInstance().disConnectBluetooth();
                    btnSendMsg.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.send_mesage));
                    btnSendMsg.setEnabled(true);
                } else {
                    BluetoothDialog bluetoothDialog = new BluetoothDialog();
                    bluetoothDialog.setData((Activity) v.getContext());
                    bluetoothDialog.show(((Activity) v.getContext()).getFragmentManager(), "");
                }

            }
        });
        _Dialog.findViewById(R.id.btnSendMsg).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String txt = tvMessageOut.getText().toString().trim();
                if (txt.equals(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.now_connect_success)) || txt.equals(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.send_fail))) {
                    {
                        AlertDialog.Builder customizeDialog = new AlertDialog.Builder(PubVar.m_DoEvent.m_Context);
                        final View dialogView = LayoutInflater.from(PubVar.m_DoEvent.m_Context)
                                .inflate(R.layout.dialog_sendbeidoumsg, null);
                        customizeDialog.setTitle(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.send_beidou_message));
                        customizeDialog.setView(dialogView);
                        final Spinner spMsgType = (Spinner) dialogView.findViewById(R.id.spMsgType);
                        String strMsgType = "北斗短报文,短信,系统中心";
                        ArrayAdapter<String> bhyyAdapter = new ArrayAdapter<String>(PubVar.m_DoEvent.m_Context,
                                android.R.layout.simple_spinner_item, strMsgType.split(","));
                        spMsgType.setAdapter(bhyyAdapter);
                        customizeDialog.setPositiveButton(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.sure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 获取EditView中的输入内容
                                int msgType = spMsgType.getSelectedItemPosition();
                                msgContent = (EditText) dialogView.findViewById(R.id.etMsgContent);
                                toId = (EditText) dialogView.findViewById(R.id.etReciverNo);
                                if (toId.getText().toString().length() == 0) {
                                    Toast.makeText(PubVar.m_DoEvent.m_Context, PubVar.m_DoEvent.m_Context.getResources().getString(R.string.send_numer_empty), Toast.LENGTH_LONG).show();
                                } else {
                                    if (msgContent.getText().length() == 0) {
                                        Toast.makeText(PubVar.m_DoEvent.m_Context, PubVar.m_DoEvent.m_Context.getResources().getString(R.string.send_content_empty), Toast.LENGTH_LONG).show();
                                    } else if (msgContent.getText().length() > 50) {
                                        Toast.makeText(PubVar.m_DoEvent.m_Context, PubVar.m_DoEvent.m_Context.getResources().getString(R.string.must_send_message_limit), Toast.LENGTH_LONG).show();
                                    } else {
                                        if (msgType == 0) {
                                            //封装成北斗消息实体类
                                            CardMessage card1 = ContentConvertTOBoxMessage.getInstance().castUserMessageTo0x12(toId.getText().toString(), msgContent.getText().toString() + "|" + mCardId + "-盒子中心" + "-" + toId.getText().toString());
                                            //发送北斗短报文
                                            BeidouHandler.getInstance().sendBeidouMessage(card1);
                                            //封装成北斗消息实体类
                                            CardMessage card2 = ContentConvertTOBoxMessage.getInstance().castUserMessageTo0x12(toId.getText().toString(), msgContent.getText().toString() + "|" + mCardId + "-" + toId.getText().toString());
                                            //发送北斗短报文
                                            BeidouHandler.getInstance().sendBeidouMessage(card2);
                                        } else if (msgType == 1) {
                                            //发短信
                                            CardMessage card1 = ContentConvertTOBoxMessage.getInstance().castUserMessageTo0x13(toId.getText().toString(), msgContent.getText().toString());
                                            BeidouHandler.getInstance().sendBeidouMessage(card1);
                                        } else if (msgType == 2) {
                                            //封装成北斗消息实体类
                                            CardMessage card2 = ContentConvertTOBoxMessage.getInstance().castUserMessageTo0x12("0313985", msgContent.getText().toString() + "|" + mCardId + "-0313985");
                                            //发送北斗短报文
                                            BeidouHandler.getInstance().sendBeidouMessage(card2);
                                        }
                                    }
                                }
//						if (msgType == 0) {
//							sendBDMsg(toId.getText().toString(), MsgType.MSG_TOBD, msgContent.getText().toString(),
//									null);
//						} else if (msgType == 1) {
//							sendBDMsg(toId.getText().toString(), MsgType.MSG_TOSMS, msgContent.getText().toString(),
//									null);
//						} else if (msgType == 2) {
//							sendBDMsg(toId.getText().toString(), MsgType.MSG_TOSYSTEM, msgContent.getText().toString(),
//									null);
//						}

                            }
                        });
                        customizeDialog.setNegativeButton(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        customizeDialog.show();
                    }

                } else {
                    Toast.makeText(PubVar.m_DoEvent.m_Context, PubVar.m_DoEvent.m_Context.getResources().getString(R.string.first_bind_blutooth), Toast.LENGTH_LONG).show();
                }
            }
        });
//        _Dialog.findViewById(R.id.btnGetDevice).setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        mLvSendList = (ListViewForScrollView) _Dialog.findViewById(R.id.lv_send_list);
        mSendAdpter = new BeidouMessageAdapter();
        mLvSendList.setAdapter(mSendAdpter);
        mLvReciveList = (ListViewForScrollView) _Dialog.findViewById(R.id.lv_recive_list);
        mReciveAdpter = new BeidouMessageAdapter();
        mLvReciveList.setAdapter(mReciveAdpter);
        initdata(true);

    }

    private void initdata(boolean IsSuccess) {
        mDB = new MsgDB();
        mReciveList = mDB.readReDB();
        mReciveAdpter.setdata(mReciveList, IsSuccess);
        mSendList = mDB.readSeDB();
        mSendAdpter.setdata(mSendList, IsSuccess);
    }


    /**
     * 显示加载框
     *
     * @param text
     */
    public static void showProgressDialog(String text) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(PubVar.m_DoEvent.m_Context);
        }
        progressDialog.setTitle(text);
        progressDialog.show();
    }


    /**
     * 隐藏加载框
     */
    private void hideProgressDialog() {
        if (progressDialog == null) return;
        progressDialog.dismiss();
    }

//    private void initView() {
//        mDeviceAdapter = new BluetoothDeviceAdapter(mDeviceList);
//        ryDevice = (RecyclerView) _Dialog.findViewById(R.id.ryDevice);
//        ryDevice.setAdapter(mDeviceAdapter);
//        mLinearLayoutManager = new LinearLayoutManager(PubVar.m_DoEvent.m_Context, LinearLayout.VERTICAL, false);
//        ryDevice.setLayoutManager(mLinearLayoutManager);
//        mDeviceAdapter.notifyDataSetChanged();
//        mDeviceAdapter.setOnItemClickListener(new BluetoothDeviceAdapter.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(View view, int position) {
//                if (mDeviceList != null && mDeviceList.size() > 0) {
//                    BluetoothDeviceEntity device = mDeviceList.get(position);
//                    BeidouParams.bleaddress = device.getDeviceAddress();
//                    BeidouSDK.getInstance(PubVar.m_DoEvent.m_Context).startConnectBle();
//                }
//
////		 HailiaoManager.getHailiaoManager().connectBle(device.getDeviceAddress(),
////		 new HlConnectCallBack() {
////
////		 @Override
////		 public void onFailed(String arg0) {
////		 Toast.makeText(PubVar.m_DoEvent.m_Context, "连接失败" + arg0,
////		 Toast.LENGTH_LONG).show();
////
////		 }
////
////		 @Override
////		 public void onDisconnect(String arg0) {
////		 Toast.makeText(PubVar.m_DoEvent.m_Context, "断开连接" + arg0,
////		 Toast.LENGTH_LONG).show();
////
////		 }
////
////		 @Override
////		 public void onConnectSuccess(String arg0) {
////		 Toast.makeText(PubVar.m_DoEvent.m_Context, "连接成功" + arg0,
////		 Toast.LENGTH_LONG).show();
////
////		 new Handler().post(new Runnable() {
////		 public void run() {
////		 tvMessageOut.setText(tvMessageOut.getText() + "设备连接成功");
////		 }
////		 });
////		 }
////		 });
//            }
//        });
//
//    }

//    @SuppressLint("NewApi")
//    private void searchBluetoothDevice() {
//        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//        if (!mBluetoothAdapter.isEnabled()) {
//            Intent enableBleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            PubVar.m_DoEvent.m_Context.startActivity(enableBleIntent);
//            return;
//        }
//
//        if (mScanning) {
//            mScanning = false;
//            mBluetoothAdapter.stopLeScan(leScanCallback);
//
//        } else {
//
//            btnSearchBle.setText("停止搜索");
//            mDeviceList.clear();
//            mDeviceAdapter.notifyDataSetChanged();
//            new Handler().postDelayed(new Runnable() {
//
//                @Override
//                public void run() {
//                    mScanning = false;
//                    mBluetoothAdapter.stopLeScan(leScanCallback);
//                    btnSearchBle.setText("开始搜索");
//                }
//            }, 5 * 1000);
//            mScanning = true;
//            mBluetoothAdapter.startLeScan(leScanCallback);
//        }
//
//    }

//    @SuppressLint("NewApi")
//    private LeScanCallback leScanCallback = new LeScanCallback() {
//
//        @Override
//        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//            boolean isRepeat = false;
//            String name = device.getName();
//            String address = device.getAddress();
//            if (name != null) {
//                if (mDeviceList != null && mDeviceList.size() > 0) {
//                    Iterator<BluetoothDeviceEntity> iteerator = mDeviceList.iterator();
//                    while (iteerator.hasNext()) {
//                        BluetoothDeviceEntity entity = iteerator.next();
//                        if (entity.getDeviceName().equals(name)) {
//                            isRepeat = true;
//                            break;
//                        }
//                    }
//                    if (!isRepeat) {
//                        mDeviceList.add(new BluetoothDeviceEntity(name, address));
//                    }
//                } else {
//                    mDeviceList.add(new BluetoothDeviceEntity(name, address));
//                }
//                mDeviceAdapter.notifyDataSetChanged();
//            }
//
//        }
//    };


    public void ShowDialog() {
        _Dialog.show();

    }

    @Override
    public void onBeidouDisconnectSuccess() {
        UnbindServer();
        mMessageList.setVisibility(View.GONE);
    }


    @Override
    public void onBeidouConnectSuccess() {
        Message message = Message.obtain();
        message.what = BIND;
        handler.sendMessage(message);
    }


    @Override
    public void sendStatusReceipt(final ReceiptType receiptType) {
        //北斗指令回执
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (receiptType.getValue() == 0) {
//                    tvMessageOut.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.send_suceess));
                    SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String format = date.format(new Date(System.currentTimeMillis()));
                    if (toId != null) {
                        new MsgDB().SaveMsgDB(toId.getText().toString().trim(), msgContent.getText().toString(), "SE", format);
                        initdata(true);
                        new Handler().post(new Runnable() {
                            public void run() {
                                btnSendMsg.setEnabled(false);
                            }
                        });
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                btnSendMsg.setEnabled(true);
                            }
                        }, 60 * 1000);
                    }
                } else if (receiptType.getValue() == 1) {
//                    tvMessageOut.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.send_fail));
                    SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String format = date.format(new Date(System.currentTimeMillis()));
                    if (toId != null) {
                        new MsgDB().SaveMsgDB(toId.getText().toString().trim(), msgContent.getText().toString(), "SE", format);
                        initdata(false);
                    }
                } else if (receiptType.getValue() == 2) {
                    tvMessageOut.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.state_unlock));
                } else if (receiptType.getValue() == 3) {
                    tvMessageOut.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.send_message_unlock));
                } else if (receiptType.getValue() == 4) {
                    tvMessageOut.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.send_message_hz));
                } else if (receiptType.getValue() == 5) {
                    tvMessageOut.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.lock_unlock_error));
                } else if (receiptType.getValue() == 6) {
                    tvMessageOut.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.crc_error));
                } else if (receiptType.getValue() == 7) {
                    tvMessageOut.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.device_unlock));
                } else if (receiptType.getValue() == 8) {
                    tvMessageOut.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.unlock_cancel));
                }
            }
        });

    }

    @Override
    public void onGetOfflineStateChange(int i, int i1) {

    }

    @Override
    public void onBeidouCardMessageReceivedOffline(UserMessage userMessage) {

    }

    @Override
    public void onReceiptMessages(String s, long l) {

    }

    @Override
    public void onReceiveMessage(final String fromNumber, final String content, final String createdTime) {
        String[] mContents = content.split("|");
        String[] carID = mContents[1].split("-");
        if (mContents[1].contains("盒子中心")) {
            //封装成北斗消息实体类
            CardMessage card1 = ContentConvertTOBoxMessage.getInstance().castUserMessageTo0x12(carID[1], msgContent.getText().toString() + "|" + "0313985-" + carID[1]);
            //发送北斗短报文
            BeidouHandler.getInstance().sendBeidouMessage(card1);
        } else {
            if (carID[1].equals("0313985")) {
                //系统消息
                new MsgDB().SaveMsgDB(fromNumber, mContents[0], "RE", createdTime);
                initdata(true);
            } else {
                new MsgDB().SaveMsgDB(fromNumber, mContents[0], "RE", createdTime);
                initdata(false);
            }
        }
//        if(mCardId.equals("313985")){
//
//        }else{
//            new MsgDB().SaveMsgDB(fromNumber, content, "RE", createdTime);
//            initdata(true);
//        }

//        new Handler(getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                mTvMessageContent.append("\n" + createdTime + "\n" + content);
//            }
//        });

    }

    @Override
    public void onReceiveBeidouUserId(String s) {

    }

    @Override
    public void onBeidouCardLocationReceived(CardLocationDto cardLocationDto) {

    }

    @Override
    public void onBeidouLocationReceived(String s, String s1) {
    }

    @Override
    public void onReceiveBeidouSOSCS(String s, String s1) {

    }

    @Override
    public void onReceiveBeidouPassword(String s) {

    }

    @Override
    public void onBleDataReceive(String s, String s1) {

    }

    @Override
    public void onBleBDPWXReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDBSIReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDDWRReceived(String s, final String[] strings) {

    }

    @Override
    public void onBleBDFKIReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDTXAReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDPOSReceived(String s, String[] strings) {

    }

    @Override
    public void onBleCCTXAReceived(String s, final String[] strings) {
        Log.d("TAG", "纬度:" + strings[4] + "\n经度:" + strings[5] + "\n高度:" + strings[6]);
        sendLocateMesage(strings[4],strings[5],strings[6]);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PubVar.m_DoEvent.m_Context, "纬度:" + strings[4] + "\n经度:" + strings[5] + "\n高度:" + strings[6], Toast.LENGTH_LONG).show();
            }
        });
    }



    @Override
    public void onBleBDMDXReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDMSHReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDPRXReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDVRXReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDZTXReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDHMXReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDOKXReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDQZXReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDZZXReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDQDXReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDMSXReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDFRXReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDZDXReceived(String s, final String[] strings) {
        new Handler(getMainLooper()).post(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                mCardId = strings[1];
                String number = BdBoxParams.beidouSignal1 + "," + BdBoxParams.beidouSignal2 + "," + BdBoxParams.beidouSignal3 + "," + BdBoxParams.beidouSignal4 + "," + BdBoxParams.beidouSignal5 + "," + BdBoxParams.beidouSignal6 + "," + BdBoxParams.beidouSignal7 + "," + BdBoxParams.beidouSignal8 + "," + BdBoxParams.beidouSignal9 + "," + BdBoxParams.beidouSignal10;
                TelephonyManager tm = (TelephonyManager) PubVar.m_DoEvent.m_Context.getSystemService(Context.TELEPHONY_SERVICE);
                if (isPad(PubVar.m_DoEvent.m_Context)) {
                    bindServer(mCardId, number, strings[2] + "%", strings[13], "平板", tm.getDeviceId());
                } else {
                    bindServer(mCardId, number, strings[2] + "%", strings[13], "手机", tm.getDeviceId());
                }

                if (BdBoxParams.cardType == null) {
                    mTvDeviceStatus.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.device_version) + strings[1] + "\n" + PubVar.m_DoEvent.m_Context.getResources().getString(R.string.power) + strings[2] + "%" + "\n" + PubVar.m_DoEvent.m_Context.getResources().getString(R.string.service_hz) + strings[13] + "\n" + PubVar.m_DoEvent.m_Context.getResources().getString(R.string.card_level) + strings[14] + "\n" + PubVar.m_DoEvent.m_Context.getResources().getString(R.string.connect_length) + strings[15] + "\n" + PubVar.m_DoEvent.m_Context.getResources().getString(R.string.connect_level) + "未知");
                } else {
                    mTvDeviceStatus.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.device_version) + strings[1] + "\n" + PubVar.m_DoEvent.m_Context.getResources().getString(R.string.power) + strings[2] + "%" + "\n" + PubVar.m_DoEvent.m_Context.getResources().getString(R.string.service_hz) + strings[13] + "\n" + PubVar.m_DoEvent.m_Context.getResources().getString(R.string.card_level) + strings[14] + "\n" + PubVar.m_DoEvent.m_Context.getResources().getString(R.string.connect_length) + strings[15] + "\n" + PubVar.m_DoEvent.m_Context.getResources().getString(R.string.connect_level) + BdBoxParams.cardType.str());
                }
                int a1 = Math.abs(BdBoxParams.beidouSignal1);
                int a2 = Math.abs(BdBoxParams.beidouSignal2);
                int a3 = Math.abs(BdBoxParams.beidouSignal3);
                int a4 = Math.abs(BdBoxParams.beidouSignal4);
                int a5 = Math.abs(BdBoxParams.beidouSignal5);
                int a6 = Math.abs(BdBoxParams.beidouSignal6);
                int a7 = Math.abs(BdBoxParams.beidouSignal7);
                int a8 = Math.abs(BdBoxParams.beidouSignal8);
                int a9 = Math.abs(BdBoxParams.beidouSignal9);
                int a10 = Math.abs(BdBoxParams.beidouSignal10);
                if (a1 >= 3 || a2 >= 3 || a3 >= 3 || a4 >= 3 || a5 >= 3 || a6 >= 3 || a7 >= 3 || a8 >= 3 || a9 >= 3 || a10 > 3) {
                    btnSendMsg.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.send_mesage));
                    btnSendMsg.setEnabled(true);
                } else {
                    btnSendMsg.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.sign_error_message));
                    btnSendMsg.setEnabled(false);
                }
                mSign.setVisibility(View.VISIBLE);
                boxSignalImg1.setBackgroundResource(R.drawable.signal_icon_0 + BdBoxParams.beidouSignal1);
                boxSignalImg2.setBackgroundResource(R.drawable.signal_icon_0 + BdBoxParams.beidouSignal2);
                boxSignalImg3.setBackgroundResource(R.drawable.signal_icon_0 + BdBoxParams.beidouSignal3);
                boxSignalImg4.setBackgroundResource(R.drawable.signal_icon_0 + BdBoxParams.beidouSignal4);
                boxSignalImg5.setBackgroundResource(R.drawable.signal_icon_0 + BdBoxParams.beidouSignal5);
                boxSignalImg6.setBackgroundResource(R.drawable.signal_icon_0 + BdBoxParams.beidouSignal6);
                boxSignalImg7.setBackgroundResource(R.drawable.signal_icon_0 + BdBoxParams.beidouSignal7);
                boxSignalImg8.setBackgroundResource(R.drawable.signal_icon_0 + BdBoxParams.beidouSignal8);
                boxSignalImg9.setBackgroundResource(R.drawable.signal_icon_0 + BdBoxParams.beidouSignal9);
                boxSignalImg10.setBackgroundResource(R.drawable.signal_icon_0 + BdBoxParams.beidouSignal10);
                mMessageList.setVisibility(View.VISIBLE);
            }
        });


    }

    @Override
    public void onBleBDDLXReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDDLCReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDIDXReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDRSXReceived(String s) {

    }

    @Override
    public void onBleBDRNXReceived(String s, String[] strings) {

    }

    @Override
    public void onBleGGAReceived(String s, String[] strings) {

    }

    @Override
    public void onBleGLLReceived(String s, String[] strings) {

    }

    @Override
    public void onBleGSAReceived(String s, String[] strings) {

    }

    @Override
    public void onBleGSVReceived(String s, String[] strings) {

    }

    @Override
    public void onBleRMCReceived(String s, String[] strings) {

    }

    @Override
    public void onBleZDAReceived(String s, String[] strings) {

    }

    @Override
    public void onBleBDICIReceived(String s, String[] strings) {

    }

    @Override
    public void onBleStatusLog(String s) {

    }

    @Override
    public void onDataReceived(String s) {

    }

    private void sendLocateMesage(String latitude, String longtitude, String altitude) {
        BDBean bean=new BDBean();
        bean.setLatitude(latitude);
        bean.setLongitude(longtitude);
        bean.setHeight(altitude);
        Coordinate coord = StaticObject.soProjectSystem.WGS84ToXY(Double.valueOf(longtitude),Double.valueOf(latitude),Double.valueOf(altitude));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        bean.setX(nf.format(coord.getX()));
        bean.setY(nf.format(coord.getY()));
        bean.setGpstime(0);
        String name = StaticObject.soProjectSystem.GetCoorSystem().GetName();
        if (name.equals("西安80坐标")) {
            bean.setSrid("2381");
        } else if (name.equals("北京54坐标")) {
            bean.setSrid("2433");
        } else if (name.equals("2000国家大地坐标系")) {
            bean.setSrid("4545");
        } else if (name.equals("WGS-84坐标")) {
            bean.setSrid("4326");
        }
        bean.setBdcode(mCardId);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        RetrofitHttp.getRetrofit(builder.build()).AddBDLocation("AddBDLocation",bean).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String body = response.body().string();
                    Log.d("TAG", body);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable arg1) {
                Log.d("TAG", arg1.getMessage());
            }
        });



    }

    /**
     * 判断当前设备是手机还是平板，代码来自 Google I/O App for Android
     *
     * @param context
     * @return 平板返回 True，手机返回 False
     */
    public static boolean isPad(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private void bindServer(String cardId, String xinhaoStr, final String battery, String frequency, String deviceType, String deviceId) {
        try {
            BindModel mBindModel = new BindModel();
            mBindModel.setCardId(cardId);
            mBindModel.setStatus(xinhaoStr);
            mBindModel.setElectricity(battery);
            mBindModel.setFrequency(frequency);
            mBindModel.setDeviceType(deviceType);
            mBindModel.setDeviceId(deviceId);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            RetrofitHttp.getRetrofit(builder.build()).BindDevice("BindBDDevice", mBindModel)
                    .enqueue(new Callback<ResponseBody>() {

                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                String body = response.body().string();
                                Log.d("TAG", body);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable arg1) {
                            Log.d("TAG", arg1.getMessage());
                        }
                    });
        } catch (Exception ex) {
            Tools.ShowMessageBox(ex.getMessage());
        }
    }

    private void UnbindServer() {
        UnBindModel unBindModel = new UnBindModel();
        unBindModel.setRequestId(mCardId);
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            RetrofitHttp.getRetrofit(builder.build()).UnBindDevice("UnBindBDDevice", unBindModel)
                    .enqueue(new Callback<ResponseBody>() {

                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                String body = response.body().string();
                                Log.d("TAG", body);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable arg1) {
                            Log.d("TAG", arg1.getMessage());
                        }
                    });
        } catch (Exception ex) {
            Tools.ShowMessageBox(ex.getMessage());
        }
        Message message = Message.obtain();
        message.what = UNBIND;
        handler.sendMessage(message);
    }


}
