package com.example.think.emulatebd.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.think.emulatebd.R;
import com.example.think.emulatebd.adapter.FaceAdapter;
import com.example.think.emulatebd.adapter.FacePageAdapter;
import com.example.think.emulatebd.adapter.MessageAdapter;
import com.example.think.emulatebd.app.PushApplication;
import com.example.think.emulatebd.baidupush.client.PushMessageReceiver;
import com.example.think.emulatebd.bean.Message;
import com.example.think.emulatebd.bean.MessageItem;
import com.example.think.emulatebd.bean.RecentItem;
import com.example.think.emulatebd.bean.User;
import com.example.think.emulatebd.common.util.HomeWatcher;
import com.example.think.emulatebd.common.util.L;
import com.example.think.emulatebd.common.util.SendMsgAsyncTask;
import com.example.think.emulatebd.common.util.SharePreferenceUtil;
import com.example.think.emulatebd.common.util.T;
import com.example.think.emulatebd.db.MessageDB;
import com.example.think.emulatebd.db.RecentDB;
import com.example.think.emulatebd.swipeback.SwipeBackActivity;
import com.example.think.emulatebd.view.CirclePageIndicator;
import com.example.think.emulatebd.view.JazzyViewPager;
import com.example.think.emulatebd.xlistview.MsgListView;
import com.example.think.emulatebd.xlistview.XExpandableListView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by HuangMei on 2016/12/27.
 */

public class ChatActivity extends SwipeBackActivity implements
        PushMessageReceiver.EventHandler, View.OnTouchListener, View.OnClickListener,
        HomeWatcher.OnHomePressedListener, MsgListView.XListViewListener {

    public static final int NEW_MESSAGE = 0x001;
    private JazzyViewPager.TransitionEffect mEffects[] = {JazzyViewPager.TransitionEffect.Standard,
            JazzyViewPager.TransitionEffect.Tablet, JazzyViewPager.TransitionEffect.CubeIn,
            JazzyViewPager.TransitionEffect.CubeOut, JazzyViewPager.TransitionEffect.FlipVertical,
            JazzyViewPager.TransitionEffect.FlipHorizontal, JazzyViewPager.TransitionEffect.Stack,
            JazzyViewPager.TransitionEffect.ZoomIn, JazzyViewPager.TransitionEffect.ZoomOut,
            JazzyViewPager.TransitionEffect.RotateUp, JazzyViewPager.TransitionEffect.RotateDown,
            JazzyViewPager.TransitionEffect.Accordion};
    private PushApplication mApplication;
    private static int MsgPagerNum;
    private MessageDB mMsgDB;
    private RecentDB mRecentDB;
    private int currentPage = 0;
    private boolean isFaceShow = false;
    private Button sendBtn;
    private ImageButton faceBtn;
    private EditText msgEt;
    private LinearLayout faceLinearLayout;
    private JazzyViewPager faceViewPager;
    private WindowManager.LayoutParams params;
    private InputMethodManager imm;
    private List<String> keys;
    private MessageAdapter adapter;
    private MsgListView mMsgListView;
    private SharePreferenceUtil mSpUtil;
    private User mFromUser;
    private TextView mTitle, mTitleLeftBtn, mTitleRightBtn;
    private HomeWatcher mHomeWatcher;
    private Gson mGson;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == NEW_MESSAGE){
                Message msgItem = (Message)msg.obj;
                String userId = msgItem.getUser_id();
                if (!userId.equals(mFromUser.getUserId())){
                    return;
                }

                int headId = msgItem.getHead_id();

                MessageItem item = new MessageItem(
                        MessageItem.MESSAGE_TYPE_TEXT, msgItem.getNick(),
                        System.currentTimeMillis(), msgItem.getMessage(),
                        headId, true, 0);
                adapter.upDateMsg(item);
                mMsgDB.saveMsg(msgItem.getUser_id(), item);
                RecentItem recentItem = new RecentItem(userId, headId,
                        msgItem.getNick(), msgItem.getMessage(), 0,
                        System.currentTimeMillis());
                mRecentDB.saveRecent(recentItem);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_main);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(this);
        mHomeWatcher.startWatch();
        PushMessageReceiver.ehList.add(this);
    }

    @Override
    protected void onPause() {
        imm.hideSoftInputFromInputMethod(msgEt.getWindowToken(), 0);
        faceLinearLayout.setVisibility(View.GONE);
        isFaceShow = false;
        super.onPause();
        mHomeWatcher.setOnHomePressedListener(null);
        mHomeWatcher.startWatch();
        PushMessageReceiver.ehList.remove(this);
    }

    private void initData(){
        mFromUser = (User) getIntent().getSerializableExtra("user");
        if (mFromUser == null) {// 如果为空，直接关闭
            finish();
        }
        mApplication = PushApplication.getInstance();
        mSpUtil = mApplication.getSpUtil();
        mGson = mApplication.getGson();
        mMsgDB = mApplication.getMessageDB();
        mRecentDB = mApplication.getRecentDB();
        Set<String> keySet = PushApplication.getInstance().getFaceMap()
                .keySet();
        keys = new ArrayList<String>();
        keys.addAll(keySet);
        MsgPagerNum = 0;
        adapter = new MessageAdapter(this, initMsgData());
    }

    private List<MessageItem> initMsgData(){
        List<MessageItem> list = mMsgDB.getMsg(mFromUser.getUserId(),
                MsgPagerNum);
        List<MessageItem> msgList = new ArrayList<>();
        if (list.size() > 0){
            for (MessageItem entity : list){
                if (entity.getName().equals("")){
                    entity.setName(mFromUser.getNick());
                }
                if (entity.getHeadImg() < 0){
                    entity.setHeadImg(mFromUser.getHeadIcon());
                }
                msgList.add(entity);
            }
        }
        return msgList;
    }


    private void initFacePage(){
        List<View> lv = new ArrayList<>();
        for (int i = 0; i < PushApplication.NUM_APGE; ++i){
            lv.add(getGridView(i));
        }
        FacePageAdapter adapter = new FacePageAdapter(lv, faceViewPager);
        faceViewPager.setAdapter(adapter);
        faceViewPager.setCurrentItem(currentPage);
        faceViewPager.setTransitionEffect(mEffects[mSpUtil.getFaceEffect()]);
        CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(faceViewPager);
        adapter.notifyDataSetChanged();
        faceLinearLayout.setVisibility(View.GONE);
        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private GridView getGridView(int i){
        GridView gv = new GridView(this);
        gv.setNumColumns(7);
        gv.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gv.setBackgroundColor(Color.TRANSPARENT);
        gv.setCacheColorHint(Color.TRANSPARENT);
        gv.setHorizontalSpacing(1);
        gv.setVerticalSpacing(1);
        gv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        gv.setGravity(Gravity.CENTER);
        gv.setAdapter(new FaceAdapter(this, i));
        gv.setOnTouchListener(forbidenScroll());
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == PushApplication.NUM){
                    int selection = msgEt.getSelectionStart();
                    String text = msgEt.getText().toString();
                    if (selection > 0){
                        String text2 = text.substring(selection - 1);
                        if ("]".equals(text2)){
                            int start = text.lastIndexOf("{");
                            int end = selection;
                            msgEt.getText().delete(start, end);
                            return;
                        }
                        msgEt.getText().delete(selection - 1, selection);
                    }
                } else {
                    int count = currentPage * PushApplication.NUM + position;

                    // 在EditText中显示表情
                    Bitmap bitmap = BitmapFactory.decodeResource(
                            getResources(), (Integer)PushApplication.getInstance().getFaceMap().values()
                            .toArray()[count]);
                    if (bitmap !=  null){
                        int rawHeigh = bitmap.getHeight();
                        int rawWidth = bitmap.getHeight();
                        int newHeight = 40;
                        int newWidth = 40;
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
                        ImageSpan imageSpan = new ImageSpan(ChatActivity.this,
                                newBitmap);
                        String emojiStr = keys.get(count);
                        SpannableString spannableString = new SpannableString(emojiStr);
                        spannableString.setSpan(imageSpan,
                                emojiStr.indexOf('['),
                                emojiStr.indexOf(']') + 1,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        msgEt.append(spannableString);
                    } else {
                        // 在EditText中显示字符串
                        String ori = msgEt.getText().toString();
                        int index = msgEt.getSelectionStart();
                        StringBuilder stringBuilder = new StringBuilder(ori);
                        stringBuilder.insert(index, keys.get(count));
                        msgEt.setText(stringBuilder.toString());
                        msgEt.setSelection(index + keys.get(count).length());
                    }
                }
            }
        });
        return null;
    }

    private void initView(){
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        params = getWindow().getAttributes();

        mMsgListView = (MsgListView) findViewById(R.id.msg_listView);
        // 触摸ListView隐藏表情和输入法
        mMsgListView.setOnTouchListener(this);
        mMsgListView.setPullLoadEnable(false);
        mMsgListView.setXListViewListener(this);
        mMsgListView.setAdapter(adapter);
        mMsgListView.setSelection(adapter.getCount() - 1);
        sendBtn = (Button) findViewById(R.id.send_btn);
        faceBtn = (ImageButton) findViewById(R.id.face_btn);
        msgEt = (EditText) findViewById(R.id.msg_et);
        faceLinearLayout = (LinearLayout) findViewById(R.id.face_ll);
        faceViewPager = (JazzyViewPager) findViewById(R.id.face_pager);
        msgEt.setOnTouchListener(this);
        mTitle = (TextView) findViewById(R.id.ivTitleName);
        mTitle.setText(mFromUser.getNick());
        mTitleLeftBtn = (TextView) findViewById(R.id.ivTitleBtnLeft);
        mTitleLeftBtn.setVisibility(View.VISIBLE);
        mTitleLeftBtn.setOnClickListener(this);
        msgEt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK){
                    if (params.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                            || isFaceShow){
                        faceLinearLayout.setVisibility(View.GONE);
                        isFaceShow = false;
                        return true;
                    }
                }
                return false;
            }
        });

        msgEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0){
                    sendBtn.setEnabled(true);
                } else {
                    sendBtn.setEnabled(false);
                }
            }
        });
        faceBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.face_btn:
                if (!isFaceShow){
                    imm.hideSoftInputFromInputMethod(msgEt.getWindowToken(), 0);
                    try{
                        Thread.sleep(80);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    faceLinearLayout.setVisibility(View.VISIBLE);
                    isFaceShow = true;
                } else {
                    faceLinearLayout.setVisibility(View.GONE);
                    isFaceShow = false;
                }
                break;
            case R.id.send_btn:
                String msg = msgEt.getText().toString();
                MessageItem item = new MessageItem(MessageItem.MESSAGE_TYPE_TEXT,
                        mSpUtil.getNick(), System.currentTimeMillis(), msg,
                        mSpUtil.getHeadIcon(), false, 0);
                adapter.upDateMsg(item);
                mMsgListView.setSelection(adapter.getCount() - 1);
                mMsgDB.saveMsg(mFromUser.getUserId(), item);
                msgEt.setText("");
                Message msgItem = new Message(System.currentTimeMillis(), msg, "");
                new SendMsgAsyncTask(mGson.toJson(msgItem), mFromUser.getUserId())
                .send();
                RecentItem recentItem = new RecentItem(mFromUser.getUserId(),
                        mFromUser.getHeadIcon(), mFromUser.getNick(), msg, 0,
                        System.currentTimeMillis());
                mRecentDB.saveRecent(recentItem);
                break;
            case R.id.ivTitleBtnLeft:
                finish();
                break;
            case R.id.ivTitleBtnRight:
                break;
            default:
                break;
        }
    }

    private View.OnTouchListener forbidenScroll(){
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()){
            case R.id.msg_listView:
                imm.hideSoftInputFromInputMethod(msgEt.getWindowToken(), 0);;
                faceLinearLayout.setVisibility(View.GONE);
                isFaceShow = false;
                break;
            case R.id.msg_et:
                imm.showSoftInput(msgEt, 0);
                faceLinearLayout.setVisibility(View.GONE);
                isFaceShow = false;
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onMessage(Message message) {
        android.os.Message handlerMsg = handler.obtainMessage(NEW_MESSAGE);
        handlerMsg.obj = message;
        handler.sendMessage(handlerMsg);
    }

    @Override
    public void onBind(String method, int errorCode, String content) {

    }

    @Override
    public void onNotify(String title, String content) {

    }

    @Override
    public void onNetChange(boolean isNetConnected) {
        if (!isNetConnected)
            T.showShort(this, "网络连接已断开");
    }

    @Override
    public void onNewFriend(User u) {

    }

    @Override
    public void onHomePressed() {
        mApplication.showNotification();
    }

    @Override
    public void onHomeLongPressed() {

    }

    @Override
    public void onRefresh() {
        MsgPagerNum ++;
        List<MessageItem> msgList = initMsgData();
        int position = adapter.getCount();
        adapter.setMessageList(msgList);
        mMsgListView.stopRefresh();
        mMsgListView.setSelection(adapter.getCount() - position - 1);
        L.i("MsgPagerNum = " + MsgPagerNum + ", adapter.getCount() = "
                + adapter.getCount());
    }

    @Override
    public void onLoadMore() {

    }


}

