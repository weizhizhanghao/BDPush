package com.example.think.emulatebd.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.think.emulatebd.R;
import com.example.think.emulatebd.activity.ChatActivity;
import com.example.think.emulatebd.activity.MainActivity;
import com.example.think.emulatebd.app.PushApplication;
import com.example.think.emulatebd.bean.User;
import com.example.think.emulatebd.common.util.L;
import com.example.think.emulatebd.common.util.SharePreferenceUtil;
import com.example.think.emulatebd.common.util.T;
import com.example.think.emulatebd.db.MessageDB;
import com.example.think.emulatebd.db.RecentDB;
import com.example.think.emulatebd.db.UserDB;
import com.example.think.emulatebd.pullrefresh.PullToRefreshBase;
import com.example.think.emulatebd.pullrefresh.PullToRefreshHorizontalScrollView;
import com.example.think.emulatebd.pullrefresh.SoundPullEventListener;
import com.example.think.emulatebd.quick_action_bar.QuickAction;
import com.example.think.emulatebd.quick_action_bar.QuickActionBar;
import com.example.think.emulatebd.quick_action_bar.QuickActionWidget;
import com.example.think.emulatebd.xlistview.IphoneTreeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HuangMei on 2016/12/26.
 */

public class LeftFragment extends Fragment {
    private static final String[] groups = { "未分组好友", "我的好友", "我的同学", "我的家人",
            "我的同事" };
    private QuickActionWidget mBar;
    private PushApplication mApplication;
    private UserDB mUserDB;
    private RecentDB mRecentDB;
    private MessageDB mMsgDB;
    private SharePreferenceUtil mSpUtil;
    private IphoneTreeView xListView;
    private LayoutInflater mInflater;
    private List<String> mGroup;// 组名
    private Map<Integer, List<User>> mChildren;// 每一组对应的child
    private MyExpandableListAdapter mAdapter;
    private int mLongPressGroupId, mLongPressChildId;
    private PullToRefreshHorizontalScrollView mPullRefreshScrollView;
    private SoundPullEventListener mSoundListener;

    public void updateAdapter(){
        if (xListView != null){
            L.i("update friend...");
            initUserData();
        }
    }

    public void onPullRefreshListener(boolean isChecked){
        if (isChecked && mSoundListener != null){
            mPullRefreshScrollView.setOnPullEventListener(mSoundListener);
        } else {
            mPullRefreshScrollView.setOnPullEventListener(null);
        }
    }
    private void showChildQuickActionBar(View view) {
        mBar = new QuickActionBar(getActivity());
        mBar.addQuickAction(new QuickAction(getActivity(),
                R.drawable.ic_action_share_pressed, R.string.open));
        mBar.addQuickAction(new QuickAction(getActivity(),
                R.drawable.ic_action_rename_pressed, R.string.rename));
        mBar.addQuickAction(new QuickAction(getActivity(),
                R.drawable.ic_action_move_pressed, R.string.move));
        mBar.addQuickAction(new QuickAction(getActivity(),
                R.drawable.ic_action_delete_pressed, R.string.delete));
        mBar.setOnQuickActionClickListener(mActionListener);
        mBar.show(view);
    }

    private void showGroupQuickActionBar(View view){
        mBar = new QuickActionBar(getActivity());
        mBar.addQuickAction(new QuickAction(getActivity(),
                R.drawable.ic_action_rename_pressed, R.string.rename));
        mBar.addQuickAction(new QuickAction(getActivity(),
                R.drawable.ic_action_delete_pressed, R.string.delete));
        mBar.setOnQuickActionClickListener(new QuickActionWidget.OnQuickActionClickListener() {
            @Override
            public void onQuickActionClicked(QuickActionWidget widget, int position) {
                switch (position) {
                    case 0:
                        T.showShort(getActivity(), "rename group "
                                + mLongPressGroupId);
                        break;
                    case 1:
                        T.showShort(getActivity(), "delete group "
                                + mLongPressGroupId);
                        break;
                    default:
                        break;
                }
            }
        });
        mBar.show(view);
    }

    private QuickActionWidget.OnQuickActionClickListener mActionListener = new QuickActionWidget.OnQuickActionClickListener() {
        @Override
        public void onQuickActionClicked(QuickActionWidget widget, int position) {
            User u = mChildren.get(mLongPressGroupId).get(mLongPressChildId);
            if (u != null)
                switch (position) {
                    case 0:
                        mMsgDB.clearNewCount(u.getUserId());// 新消息置空
                        Intent toChatIntent = new Intent(getActivity(),
                                ChatActivity.class);
                        toChatIntent.putExtra("user", u);
                        startActivity(toChatIntent);
                        // T.showShort(mApplication, "open");
                        break;
                    case 1:
                        T.showShort(mApplication, "rename");
                        break;
                    case 2:
                        T.showShort(mApplication, "move");
                        break;
                    case 3:
                        mUserDB.delUser(u);
                        updateAdapter();
                        mRecentDB.delRecent(u.getUserId());
                        ((MainActivity) getActivity()).upDateList();
                        T.showShort(mApplication, "删除成功！");
                        break;
                    default:
                        break;
                }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_left_fragment, container,
                false);
        initView(view);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = PushApplication.getInstance();
        mUserDB = mApplication.getUserDB();
        mMsgDB = mApplication.getMessageDB();
        mRecentDB = mApplication.getRecentDB();
        mSpUtil = mApplication.getSpUtil();
        mSoundListener = new SoundPullEventListener(getActivity());
        mSoundListener.addSoundEvent(PullToRefreshBase.State.PULL_TO_REFRESH, R.raw.pull_event);
        mSoundListener.addSoundEvent(PullToRefreshBase.State.RESET, R.raw.reset_sound);
        mSoundListener.addSoundEvent(PullToRefreshBase.State.REFRESHING, R.raw.refreshing_sound);
    }

    @Override
    public void onResume() {
        super.onResume();
        initUserData();
    }

    private void initView(View view){
        mInflater = LayoutInflater.from(getActivity());
        // prepareQuickActionBar();
        // title
        view.findViewById(R.id.ivTitleBtnLeft).setVisibility(View.GONE);
        view.findViewById(R.id.ivTitleBtnRigh).setVisibility(View.GONE);
        TextView title = (TextView) view.findViewById(R.id.ivTitleName);
        title.setText(R.string.left_title_name);

        xListView = (IphoneTreeView) view.findViewById(R.id.friend_xlistview);

        xListView.setGroupIndicator(null);
        xListView.setHeaderView(mInflater.inflate(
                R.layout.contact_buddy_list_group, xListView, false));

        xListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                User u = (User)mAdapter.getChild(groupPosition, childPosition);
                mMsgDB.clearNewCount(u.getUserId());
                Intent toChatIntent = new Intent(getActivity(),
                        ChatActivity.class);
                toChatIntent.putExtra("user", u);
                startActivity(toChatIntent);
                return false;
            }
        });

        xListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int groupPos = (Integer) view.getTag(R.id.xxx01); // 参数值是在setTag时使用的对应资源id号
                int childPos = (Integer) view.getTag(R.id.xxx02);
                mLongPressGroupId = groupPos;
                mLongPressChildId = childPos;
                if (childPos == -1){
                    showGroupQuickActionBar(view.findViewById(R.id.group_indicator));
                    T.showShort(getActivity(), "LongPress group position = "
                            + groupPos);
                } else {
                    showChildQuickActionBar(view.findViewById(R.id.icon));
                }
                return false;
            }
        });

        xListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mBar != null && mBar.isShowing()){
                    mBar.clearAllQucikActions();
                    mBar.dismiss();
                }
            }
        });
        mPullRefreshScrollView = (PullToRefreshHorizontalScrollView) view
                .findViewById(R.id.pull_refresh_horizontalscrollview);
        mPullRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<HorizontalScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<HorizontalScrollView> refreshView) {
                new GetDataTask().execute();
            }
        });
        if (mSpUtil.getPullRefreshSound() && mSoundListener != null){
            mPullRefreshScrollView.setOnPullEventListener(mSoundListener);
        }
    }

    private class GetDataTask extends AsyncTask<Void, Void, String[]>{
        @Override
        protected String[] doInBackground(Void... params) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e){

            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            initUserData();
            xListView.setAdapter(mAdapter);
            mPullRefreshScrollView.onRefreshComplete();
            super.onPostExecute(strings);
        }
    }

    private void initUserData(){
        mGroup = new ArrayList<>();
        mChildren = new HashMap<>();
        List<User> dbUsers = mUserDB.getUser();

        for (int i = 0; i < groups.length; ++ i){
            mGroup.add(groups[i]);
            List<User> childUsers = new ArrayList<>();
            mChildren.put(i, childUsers);
        }

        for (User u : dbUsers){
            for (int i = 0; i < mGroup.size(); ++i){
                if (u.getGroup() == i){
                    mChildren.get(i).add(u);
                }
            }
        }
        mAdapter = new MyExpandableListAdapter(mGroup, mChildren);
        xListView.setAdapter(mAdapter);
    }

    public class MyExpandableListAdapter extends BaseExpandableListAdapter
        implements IphoneTreeView.IphoneTreeHeaderAdapter{

        private List<String> mGroup;
        private Map<Integer, List<User>> mChildren;

        public MyExpandableListAdapter(List<String> mGroup, Map<Integer, List<User>> mChildren) {
            this.mGroup = mGroup;
            this.mChildren = mChildren;
        }

        public void addUser(User u){
            int groupId = u.getGroup();
            if (groupId < mGroup.size()){
                mChildren.get(groupId).add(u);
                notifyDataSetChanged();
            }
        }

        @Override
        public int getGroupCount() {
            return mGroup.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mChildren.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mGroup.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mChildren.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(
                        R.layout.contact_buddy_list_group, null);
            }
            TextView groupName = (TextView) convertView
                    .findViewById(R.id.group_name);
            groupName.setText(getGroup(groupPosition).toString());
            TextView onlineNum = (TextView) convertView
                    .findViewById(R.id.online_count);
            onlineNum.setText(getChildrenCount(groupPosition) + "/"
                    + getChildrenCount(groupPosition));
            ImageView indicator = (ImageView) convertView
                    .findViewById(R.id.group_indicator);
            if (isExpanded)
                indicator.setImageResource(R.drawable.indicator_expanded);
            else
                indicator.setImageResource(R.drawable.indicator_unexpanded);
            // 必须使用资源Id当key（不是资源id会出现运行时异常），android本意应该是想用tag来保存资源id对应组件。
            // 将groupPosition，childPosition通过setTag保存,在onItemLongClick方法中就可以通过view参数直接拿到了
            convertView.setTag(R.id.xxx01, groupPosition);
            convertView.setTag(R.id.xxx02, -1);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(
                        R.layout.contact_list_item_for_buddy, null);
            }
            TextView nick = (TextView) convertView
                    .findViewById(R.id.contact_list_item_name);
            final User u = (User) getChild(groupPosition, childPosition);
            nick.setText(u.getNick());
            TextView state = (TextView) convertView
                    .findViewById(R.id.cpntact_list_item_state);
            state.setText(u.getUserId());
            ImageView head = (ImageView) convertView.findViewById(R.id.icon);
            head.setImageResource(PushApplication.heads[u.getHeadIcon()]);
            convertView.setTag(R.id.xxx01, groupPosition);
            convertView.setTag(R.id.xxx02, childPosition);

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public int getTreeHeaderState(int groupPosition, int childPosition) {
            final int childCount = getChildrenCount(groupPosition);
            if (childPosition == childCount - 1){
                return PINNED_HEADER_PUSHED_UP;
            } else if (childPosition == -1
                    && !xListView.isGroupExpanded(groupPosition)){
                return PINNED_HEADER_GONE;
            } else {
                return PINNED_HEADER_VISIBLE;
            }
        }

        @Override
        public void configureTreeHeader(View header, int groupPosition, int childPosition, int alpha) {
            ((TextView)header.findViewById(R.id.group_name))
                    .setText(groups[groupPosition]);
            ((TextView) header.findViewById(R.id.online_count))
                    .setText(getChildrenCount(groupPosition) + "/"
                            + getChildrenCount(groupPosition));
        }

        private HashMap<Integer, Integer> groupStatusMap = new HashMap<Integer, Integer>();

        @Override
        public void onHeadViewClick(int groupPosition, int status) {
            groupStatusMap.put(groupPosition, status);
        }

        @Override
        public int getHeadViewClickStatus(int groupPosition) {
            if (groupStatusMap.containsKey(groupPosition)) {
                return groupStatusMap.get(groupPosition);
            } else {
                return 0;
            }
        }
    }
}
