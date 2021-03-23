package com.example.dingtu2.myapplication.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.DingTu.Base.PubVar;

public class NetworkReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager)PubVar.m_DoEvent.m_Context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            PubVar.mIsNetworkAvaliable = true;
        } else {
            PubVar.mIsNetworkAvaliable  = false;
        }
    }
}
