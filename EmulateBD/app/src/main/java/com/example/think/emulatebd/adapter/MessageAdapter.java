package com.example.think.emulatebd.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.think.emulatebd.R;
import com.example.think.emulatebd.app.PushApplication;
import com.example.think.emulatebd.bean.MessageItem;
import com.example.think.emulatebd.common.util.L;
import com.example.think.emulatebd.common.util.SharePreferenceUtil;
import com.example.think.emulatebd.common.util.TimeUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by HuangMei on 2016/12/27.
 */

public class MessageAdapter extends BaseAdapter{

    public static final Pattern EMOTION_URL = Pattern.compile("\\[(\\S+?)\\]");

    private Context mContext;
    private LayoutInflater mInflater;
    private List<MessageItem> mMsgList;
    private SharePreferenceUtil mSpUtil;

    public MessageAdapter(Context mContext, List<MessageItem> mMsgList) {
        this.mContext = mContext;
        this.mMsgList = mMsgList;
        mInflater = LayoutInflater.from(mContext);
        mSpUtil = PushApplication.getInstance().getSpUtil();
    }

    public void removeHeadMsg(){
        L.i("before remove mMsgList.size() = " + mMsgList.size());
        if (mMsgList.size() - 10 > 10){
            for (int i = 0; i < 10; i ++){
                mMsgList.remove(i);
            }
            notifyDataSetChanged();
        }
        L.i("after remove mMsgList.size() = " + mMsgList.size());
    }

    public void setMessageList(List<MessageItem> msgList){
        mMsgList = msgList;
        notifyDataSetChanged();
    }

    public void upDateMsg(MessageItem msg){
        mMsgList.add(msg);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMsgList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMsgList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageItem item = mMsgList.get(position);
        boolean isComMsg = item.isComMeg();
        ViewHolder holder;
        if (convertView == null
                || convertView.getTag(R.drawable.ic_launcher + position) == null){
            holder = new ViewHolder();
            if (isComMsg){
                convertView = mInflater.inflate(R.layout.chat_item_left, null);
            } else {
                convertView = mInflater.inflate(R.layout.chat_item_right, null);
            }

            holder.head = (ImageView) convertView.findViewById(R.id.icon);
            holder.time = (TextView) convertView.findViewById(R.id.datetime);
            holder.msg = (TextView) convertView.findViewById(R.id.textView2);
            holder.progressBar = (ProgressBar) convertView
                    .findViewById(R.id.progressBar1);
            convertView.setTag(R.drawable.ic_launcher + position);
        } else {
            holder = (ViewHolder) convertView.getTag(R.drawable.ic_launcher
                    + position);
        }

        holder.time.setText(TimeUtil.getChatTime(item.getTime()));
        holder.time.setVisibility(View.VISIBLE);
        holder.head.setBackgroundResource(PushApplication.heads[item
                .getHeadImg()]);
        if (!isComMsg && !mSpUtil.getShowHead()) {
            holder.head.setVisibility(View.GONE);
        }

        holder.msg.setText(
                convertNormalStringToSpannableString(item.getMessage()),
                TextView.BufferType.SPANNABLE);
        holder.progressBar.setVisibility(View.GONE);
        holder.progressBar.setProgress(50);
        return convertView;
    }

    private CharSequence convertNormalStringToSpannableString(String message){
        String hackTxt;
        if (message.startsWith("[") && message.endsWith("]")){
            hackTxt = message + " ";
        } else {
            hackTxt = message;
        }
        SpannableString value = SpannableString.valueOf(hackTxt);

        Matcher localMatcher = EMOTION_URL.matcher(value);
        while (localMatcher.find()){
            String str2 = localMatcher.group(0);
            int k = localMatcher.start();
            int m = localMatcher.end();

            if (m - k < 8){
                if (PushApplication.getInstance().getFaceMap()
                        .containsKey(str2)){
                    int face = PushApplication.getInstance().getFaceMap()
                            .get(str2);
                    Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), face);
                    if (bitmap != null){
                        ImageSpan localImageSpan = new ImageSpan(mContext,
                                bitmap, ImageSpan.ALIGN_BASELINE);
                        value.setSpan(localImageSpan, k, m,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }
        return value;
    }

    static class ViewHolder{
        ImageView head;
        TextView time;
        TextView msg;
        ImageView imageView;
        ProgressBar progressBar;
    }
}
