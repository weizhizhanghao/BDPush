package com.example.think.emulatebd.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.think.emulatebd.R;
import com.example.think.emulatebd.app.PushApplication;
import com.example.think.emulatebd.bean.RecentItem;
import com.example.think.emulatebd.common.util.L;
import com.example.think.emulatebd.common.util.TimeUtil;
import com.example.think.emulatebd.db.MessageDB;
import com.example.think.emulatebd.db.RecentDB;
import com.example.think.emulatebd.swipelistview.SwipeListView;

import java.util.LinkedList;
import java.util.regex.Matcher;

/**
 * Created by HuangMei on 2016/12/27.
 */

public class RecentAdapter extends BaseAdapter{
    private LayoutInflater mInflater;
    private LinkedList<RecentItem> mData;
    private SwipeListView mListView;
    private MessageDB mMessageDB;
    private RecentDB mRecentDB;
    private Context mContext;

    public RecentAdapter(Context mContext, SwipeListView mListView, LinkedList<RecentItem> mData) {
        this.mContext = mContext;
        this.mListView = mListView;
        this.mData = mData;
        this.mInflater = LayoutInflater.from(mContext);
        mMessageDB = PushApplication.getInstance().getMessageDB();
        mRecentDB = PushApplication.getInstance().getRecentDB();
    }

    public void remove(int position){
        if (position < mData.size()){
            mData.remove(position);
            notifyDataSetChanged();
        }
    }

    public void removew(RecentItem item){
        if (mData.contains(item)){
            mData.remove(item);
            notifyDataSetChanged();
        }
    }

    public void addFirst(RecentItem item){
        if (mData.contains(item)){
            mData.remove(item);
        }
        mData.addFirst(item);
        L.i("addFirst:" + item);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final RecentItem item = mData.get(position);
        if (convertView == null) {
            convertView = mInflater
                    .inflate(R.layout.recent_listview_item, null);
        }
        TextView nickTV = (TextView) convertView
                .findViewById(R.id.recent_list_item_name);
        TextView msgTV = (TextView) convertView
                .findViewById(R.id.recent_list_item_msg);
        TextView numTV = (TextView) convertView.findViewById(R.id.unreadmsg);
        TextView timeTV = (TextView) convertView
                .findViewById(R.id.recent_list_item_time);
        ImageView headIV = (ImageView) convertView.findViewById(R.id.icon);
        Button deleteBtn = (Button) convertView
                .findViewById(R.id.recent_del_btn);
        nickTV.setText(item.getName());
        msgTV.setText(convertNormalStringToSpannableString(item.getMessage()),
                TextView.BufferType.SPANNABLE);
        timeTV.setText(TimeUtil.getChatTime(item.getTime()));
        headIV.setImageResource(PushApplication.heads[item.getHeadImg()]);
        int num = mMessageDB.getNewCount(item.getUserId());
        if (num > 0) {
            numTV.setVisibility(View.VISIBLE);
            numTV.setText(num + "");
        } else {
            numTV.setVisibility(View.GONE);
        }
        deleteBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mData.remove(position);
                mRecentDB.delRecent(item.getUserId());
                notifyDataSetChanged();
                if (mListView != null)
                    mListView.closeOpenedItems();
            }
        });
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

        Matcher localMatcher = MessageAdapter.EMOTION_URL.matcher(value);
        while (localMatcher.find()) {
            String str2 = localMatcher.group(0);
            int k = localMatcher.start();
            int m = localMatcher.end();
            // k = str2.lastIndexOf("[");
            // Log.i("way", "str2.length = "+str2.length()+", k = " + k);
            // str2 = str2.substring(k, m);
            if (m - k < 8) {
                if (PushApplication.getInstance().getFaceMap()
                        .containsKey(str2)) {
                    int face = PushApplication.getInstance().getFaceMap()
                            .get(str2);
                    Bitmap bitmap = BitmapFactory.decodeResource(
                            mContext.getResources(), face);
                    if (bitmap != null) {
                        int rawHeigh = bitmap.getHeight();
                        int rawWidth = bitmap.getHeight();
                        int newHeight = 30;
                        int newWidth = 30;
                        // 计算缩放因子
                        float heightScale = ((float) newHeight) / rawHeigh;
                        float widthScale = ((float) newWidth) / rawWidth;
                        // 新建立矩阵
                        Matrix matrix = new Matrix();
                        matrix.postScale(heightScale, widthScale);
                        // 设置图片的旋转角度
                        // matrix.postRotate(-30);
                        // 设置图片的倾斜
                        // matrix.postSkew(0.1f, 0.1f);
                        // 将图片大小压缩
                        // 压缩后图片的宽和高以及kB大小均会变化
                        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                rawWidth, rawHeigh, matrix, true);
                        ImageSpan localImageSpan = new ImageSpan(mContext,
                                newBitmap, ImageSpan.ALIGN_BASELINE);
                        value.setSpan(localImageSpan, k, m,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }
        return value;
    }
}
