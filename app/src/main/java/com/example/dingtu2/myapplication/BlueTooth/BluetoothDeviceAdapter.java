package com.example.dingtu2.myapplication.BlueTooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.dingtu2.myapplication.R;

import java.util.List;

public class BluetoothDeviceAdapter extends Adapter<BluetoothDeviceAdapter.MyViewHolder> {

    private final Context mContext;
    private final List<BluetoothDevice> mList;
    private OnItemClickListener mOnItemClickListener;
    
    public BluetoothDeviceAdapter(Context context, List<BluetoothDevice> list, OnItemClickListener onItemClickListener) {
        this.mContext = context;
        this.mList = list;
        this.mOnItemClickListener = onItemClickListener;
    }



    @Override
    public int getItemCount() {
        // TODO Auto-generated method stub
        if(mList!=null&&mList.size()>0){
            return mList.size();
        }else{
            return 0;
        }

    }

    public interface OnItemClickListener {
        void OnItemClickListener(View view, int postion, BluetoothDevice device);
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final BluetoothDevice device = mList.get(position);
        if (position % 2 == 0) {
            holder.itemLayout.setBackground(mContext.getResources().getDrawable(R.drawable.bluetooth_list_cilckable_white));
        } else {
            holder.itemLayout.setBackground(mContext.getResources().getDrawable(R.drawable.bluetooth_list_cilckable_gray));
        }
        holder.deviceName.setText(device.getName());
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.OnItemClickListener(view,position,device);
            }
        });
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_device_list_item, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view,mOnItemClickListener);
        return viewHolder;
    }


    /**
     * 添加设备
     *
     * @param device
     */
    public void addListItem(BluetoothDevice device) {
        mList.add(device);
        notifyDataSetChanged();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        //整个布局
        private RelativeLayout itemLayout;
        //蓝牙名称
        private TextView deviceName;
        OnItemClickListener mOnItemClickListener;

        public MyViewHolder(View view, OnItemClickListener onItemClickListener) {
            super(view);
            mOnItemClickListener = onItemClickListener;
            itemLayout = (RelativeLayout) itemView.findViewById(R.id.item_layout);
            deviceName = (TextView) itemView.findViewById(R.id.device_name);
        }
    }


}
