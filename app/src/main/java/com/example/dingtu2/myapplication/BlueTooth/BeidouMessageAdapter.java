package com.example.dingtu2.myapplication.BlueTooth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.DingTu.Base.PubVar;
import com.example.dingtu2.myapplication.R;

import java.util.List;

public class BeidouMessageAdapter extends BaseAdapter {

    private List<Msg> MsgList;
    private boolean IsSuccess;

    @Override
    public int getCount() {
        if (MsgList != null && MsgList.size() > 0) {
            return MsgList.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (MsgList != null && MsgList.size() > 0) {
            return MsgList.get(position);
        } else {
            return null;
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.msg_item_list, null);
            viewHolder.mTvCardNo = (TextView) convertView.findViewById(R.id.tv_card_no);
            viewHolder.mTvTime = (TextView) convertView.findViewById(R.id.tv_time);
            viewHolder.mMsgContent = (TextView) convertView.findViewById(R.id.tv_msg_content);
            viewHolder.mTvStatus=(TextView)convertView.findViewById(R.id.tv_send_status);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Msg msg = MsgList.get(position);
        if (msg.getMsgType().equals("RE")) {
            viewHolder.mTvCardNo.setText("From: " + msg.getFromId());
            if(IsSuccess){
                viewHolder.mTvStatus.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.receive_center_msg));
            }else{
                viewHolder.mTvStatus.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.receive_msg));
            }
        } else if (msg.getMsgType().equals("SE")) {
            viewHolder.mTvCardNo.setText("To: " + msg.getToId());
            if(IsSuccess) {
                viewHolder.mTvStatus.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.send_suceess));
            }else{
                viewHolder.mTvStatus.setText(PubVar.m_DoEvent.m_Context.getResources().getString(R.string.send_fail));
            }
        }

        viewHolder.mTvTime.setText(msg.getTime());
        viewHolder.mMsgContent.setText(msg.getMsg());
        return convertView;
    }

    public void setdata(List<Msg> mList, boolean isSuccess) {
        this.MsgList = mList;
        this.IsSuccess=isSuccess;
        notifyDataSetChanged();
    }

    class ViewHolder {
        public TextView mTvCardNo, mTvTime, mMsgContent,mTvStatus;
    }
}
