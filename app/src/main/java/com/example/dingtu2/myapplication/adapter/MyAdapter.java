package com.example.dingtu2.myapplication.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.example.dingtu2.myapplication.R;

import java.util.List;

/**
 * Created by zhuguohui on 2016/11/8.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {



    private List<? extends RouteLine> mRouteLines;
    private CancelItemClick mOnCancelItemClick;

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.activity_transit_item, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        DrivingRouteLine drivingRouteLine = (DrivingRouteLine) mRouteLines.get(position);
        holder.mName.setText("当前线路" );
        holder.mLightNum.setText("红绿灯数：" + drivingRouteLine.getLightNum());
        holder.mDis.setText("拥堵距离为：" + drivingRouteLine.getCongestionDistance() + "米");
        holder.mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnCancelItemClick!=null){
                    mOnCancelItemClick.onCancelItemClick(view,position);
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        if (mRouteLines != null && mRouteLines.size() > 0) {
            return mRouteLines.size();
        } else {
            return 0;
        }

    }

    public void updata(List<? extends RouteLine> routeLines) {
        this.mRouteLines = routeLines;
        notifyDataSetChanged();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView mName;
        private final TextView mLightNum;
        private final TextView mDis;
        private final Button mBtnCancel;

        public MyViewHolder(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.transitName);
            mLightNum = (TextView) itemView.findViewById(R.id.lightNum);
            mDis = (TextView) itemView.findViewById(R.id.dis);
           mBtnCancel=(Button)itemView.findViewById(R.id.btn_cancel);

        }
    }

    public interface CancelItemClick{
        public void onCancelItemClick(View view, int position);
    }

    public void setOnCancelItemClick( CancelItemClick onCancelItemClick)
    {
        this.mOnCancelItemClick = onCancelItemClick;
    }


}
