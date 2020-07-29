package com.wqs.wechat.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.android.volley.NetworkError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.qmuiteam.qmui.layout.QMUIFrameLayout;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopups;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.qmuiteam.qmui.widget.textview.QMUISpanTouchFixTextView;
import com.wqs.wechat.R;
import com.wqs.wechat.activity.UserInfoActivity;
import com.wqs.wechat.cons.Constant;
import com.wqs.wechat.entity.FriendsCircle;
import com.wqs.wechat.entity.FriendsCircleComment;
import com.wqs.wechat.entity.LikeUserList;
import com.wqs.wechat.entity.User;
import com.wqs.wechat.utils.PreferencesUtil;
import com.wqs.wechat.utils.TextParser;
import com.wqs.wechat.utils.TimeUtil;
import com.wqs.wechat.utils.VolleyUtil;
import com.wqs.wechat.widget.LoadingDialog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.heuet.littlecurl.ninegridview.base.NineGridViewAdapter;
import cn.edu.heuet.littlecurl.ninegridview.bean.NineGridItem;
import cn.edu.heuet.littlecurl.ninegridview.preview.NineGridViewGroup;

import static com.baidu.mapapi.BMapManager.getContext;

public class FriendsCircleAdapter extends RecyclerView.Adapter<FriendsCircleAdapter.RecyclerHolder> {
    private static final String TAG = "FriendsCircleAdapter";
    Activity activity;
    private VolleyUtil mVolleyUtil;
    LoadingDialog mDialog;
    User mUser;
    private ItemOnClickListener item;
    private List<FriendsCircle> friendsCircles;
    private QMUIPopup mNormalPopup;
    int color = Color.rgb(87, 107, 149);
    RecyclerHolder current_holder;
    public FriendsCircleAdapter(List<FriendsCircle> list, Activity friendsCircleActivity) {
        super();
        this.activity = friendsCircleActivity;
        this.friendsCircles = list;
        this.mVolleyUtil = VolleyUtil.getInstance(friendsCircleActivity);
        PreferencesUtil.getInstance().init(friendsCircleActivity);
        this.mUser = PreferencesUtil.getInstance().getUser();
        this.mDialog = new LoadingDialog(friendsCircleActivity);
    }
    @NonNull
    @Override
    public RecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_friends_circle, parent, false);
        return new RecyclerHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerHolder holder, int position) {
        //默认没有点过赞
        Boolean isLike = false;
        FriendsCircle data = friendsCircles.get(position);
        List<LikeUserList> likeUserLists = data.getLikeUserList();
        LikeUserList user = new LikeUserList();
        TextParser textParser = new TextParser();
        View popuwind = LayoutInflater.from(activity).inflate(R.layout.layout_popu_view, null);
        TextView tvLike = popuwind.findViewById(R.id.tv_like);
        TextView tvComment = popuwind.findViewById(R.id.tv_comment);
        List<FriendsCircleComment> commentlist = data.getMomentsCommentList();
        JSONArray photosData = JSON.parseArray(data.getMomentsPhotos());
        ArrayList<NineGridItem> nineGridItemList = new ArrayList<>();
        //显示头像、昵称、时间、地址
        holder.tvNickName.setText(data.getUserNickName());
        Glide.with(holder.avatar).load(data.getUserAvatar()).into(holder.avatar);
        holder.time.setText(TimeUtil.getTimeStringAutoShort2(data.getTimestamp(), true));
        //点击操作按钮
        holder.ibComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNormalPopup = QMUIPopups.popup(getContext(),
                        QMUIDisplayHelper.dp2px(getContext(), 150),
                        QMUIDisplayHelper.dp2px(getContext(), 40))
                        .preferredDirection(QMUIPopup.DIRECTION_BOTTOM)
                        .view(popuwind)
                        .offsetX(-250)
                        .offsetYIfBottom(-105)
                        .edgeProtection(QMUIDisplayHelper.dp2px(getContext(), 20))
                        .animStyle(QMUIPopup.ANIM_GROW_FROM_RIGHT)
                        .skinManager(QMUISkinManager.defaultInstance(getContext()))
                        .show(view);
            }
        });
        //第一加载，判断我是否给这个朋友圈点赞过
        for (int i = 0; i < likeUserLists.size(); i++) {
            if (likeUserLists.get(i).getUserId().equals(mUser.getUserId())){
                isLike = true;
            }
        }
        setTvLike(tvLike,isLike);
        //当点赞按钮被点击的时候处理
        tvLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean isLike = false;
                //判断是否被点赞过
                for (int i = 0; i < likeUserLists.size(); i++) {
                    if (likeUserLists.get(i).getUserId().equals(mUser.getUserId())){
                        isLike = true;
                    }
                }
                if (isLike){
                    tvLike.setText("  点赞");
                    //如果被点过，则取消点赞,并把弹窗设为 “点赞”
                    //先找出点赞集中，我的点赞记录并移除
                    for (int i = 0; i < likeUserLists.size(); i++) {
                        if (likeUserLists.get(i).getUserId().equals(mUser.getUserId())) {
                            likeUserLists.remove(i);
                        }
                    }
                    //执行取消点赞
                    unLike(data.getMomentsId(),holder,likeUserLists,textParser);
                }else{
                    //如果未被点过，则点赞,并把弹窗设为 “取消”
                    tvLike.setText("  取消");
                    user.setUserId(mUser.getUserId());
                    user.setUserNickName(mUser.getUserNickName());
                    likeUserLists.add(user);
                    addLike(holder,data.getMomentsId(),data,textParser,likeUserLists);
                }
            }
        });
        //点击评论按钮
        tvComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTools(holder,data);
            }
        });
        //判断是否有图片数据，有则执行
        if (photosData != null && photosData.size() > 0) {
            for (int i = 0; i < photosData.size(); i++) {
                String thumbnailUrl = photosData.get(i).toString();
                String bigImageUrl = photosData.get(i).toString();
                nineGridItemList.add(new NineGridItem(thumbnailUrl, bigImageUrl));
            }
            NineGridViewAdapter nineGridViewAdapter = new NineGridViewAdapter(nineGridItemList);
            holder.photos.setAdapter(nineGridViewAdapter);
        }
        //判断是否有文字内容，有则执行
        if (data.getMomentsContent().length() > 0) {
            holder.content.setText(data.getMomentsContent());
        }
        //首次加载判断是否有点赞信息，有则执行
        if (data.getLikeUserList().size() > 0) {
            showLike(holder,data,textParser);
        }
        //首次加载判断是否有评论，有则执行
        if (commentlist != null && commentlist.size() > 0) {
            holder.llPingLun.setVisibility(View.VISIBLE);
            holder.rlcomment.setVisibility(View.VISIBLE);
            if (data.getLikeUserList().size()>0){
                holder.rlcomment.setTop(0);
                holder.line.setVisibility(View.VISIBLE);
            }
            FriendsCircleCommentAdapter mFriendsCircleCommentAdapter = new FriendsCircleCommentAdapter(data.getMomentsCommentList(),getContext());
            LinearLayoutManager mLinerLayoutMange = new LinearLayoutManager(getContext());
            holder.rlcomment.setLayoutManager(mLinerLayoutMange);
            holder.rlcomment.setAdapter(mFriendsCircleCommentAdapter);
        }
    }
    @Override
    public int getItemCount() {
        return friendsCircles.size();
    }
    public void setmItemOnClickListener(ItemOnClickListener listener) {
        this.item = listener;
    }
    public interface ItemOnClickListener {
        public void itemOnClickListener(List<FriendsCircleComment> commentlist,String momentsId);
    }
    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
    class RecyclerHolder extends RecyclerView.ViewHolder {
        LinearLayout llPingLun;
        QMUISpanTouchFixTextView likesUsernickname ;
        TextView tvNickName;
        TextView content;
        TextView time;
        QMUIRadiusImageView avatar;
        ImageButton ibComment ;
        NineGridViewGroup photos;
        RelativeLayout rlLike;
        QMUIFrameLayout qfltool;
        TextView etTextMsg;
        QMUIRoundButton btnsend;
        RecyclerView rlcomment;
        View line;
        public RecyclerHolder(View view) {
            super(view);
            llPingLun = view.findViewById(R.id.pinglun);
            likesUsernickname = view.findViewById(R.id.praise_content);
            tvNickName = view.findViewById(R.id.tv_nick_name);
            content = view.findViewById(R.id.tv_content);
            time = view.findViewById(R.id.tv_create_time);
            avatar = view.findViewById(R.id.sdv_avatar);
            ibComment = view.findViewById(R.id.ib_comment);
            photos = view.findViewById(R.id.gv_friends_circle_photo);
            rlLike = view.findViewById(R.id.rl_like);
            qfltool = activity.findViewById(R.id.qfl_tool);
            etTextMsg = activity.findViewById(R.id.et_text_msg);
            btnsend = activity.findViewById(R.id.btn_send);
            rlcomment = view.findViewById(R.id.rl_comment);
            line = view.findViewById(R.id.line);
        }
    }
    private void sendMomentLike(String userId, String momentsId) {
        String url = Constant.BASE_URL + "moments/" + momentsId + "/like";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("userId", userId);
        mVolleyUtil.httpPostRequest(url, paramMap, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (s.equals("LIKE_MOMENTS_SUCCESS")) {
                    ToastUtils.showShort("点赞成功");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mDialog.dismiss();
                if (volleyError instanceof NetworkError) {
                    ToastUtils.showShort(R.string.network_unavailable);
                    return;
                } else if (volleyError instanceof TimeoutError) {
                    ToastUtils.showShort(R.string.network_time_out);
                    return;
                }
            }
        });
    }
    private void sendUnMomentLike(String userId, String momentsId) {
        String url = Constant.BASE_URL + "moments/" + momentsId + "/like?userId="+userId;
        mVolleyUtil.httpDeleteRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (s.equals("UNLIKE_MOMENTS_SUCCESS")) {
                    ToastUtils.showShort("取消成功");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mDialog.dismiss();
                if (volleyError instanceof NetworkError) {
                    ToastUtils.showShort(R.string.network_unavailable);
                    return;
                } else if (volleyError instanceof TimeoutError) {
                    ToastUtils.showShort(R.string.network_time_out);
                    return;
                }
            }
        });
    }
    private void addLike(RecyclerHolder holder, String momnetsId, FriendsCircle data, TextParser textParser, List<LikeUserList> likeUserLists){
        //显示View
        holder.likesUsernickname.setText(null);
        holder.llPingLun.setVisibility(View.VISIBLE);
        holder.rlLike.setVisibility(View.VISIBLE);
        holder.likesUsernickname.setVisibility(View.VISIBLE);
        for (int i = 0; i < likeUserLists.size(); i++) {
            if (i==0){
                textParser.append(likeUserLists.get(i).getUserNickName(), 20, color);
            }else{
                textParser.append(",  " + likeUserLists.get(i).getUserNickName(), 20, color);
            }

        }
        textParser.parse(holder.likesUsernickname);
        sendMomentLike(mUser.getUserId(), momnetsId);
    }
    private void unLike(String momentsId, RecyclerHolder holder, List<LikeUserList> likeUserLists, TextParser textParser) {
        //首先把值清空
        holder.likesUsernickname.setText(null);
        //缓存清空
        textParser.setNull();
        //如果清空后，列表为空，则隐藏该布局
        if (likeUserLists.size()==0){
            holder.rlLike.setVisibility(View.GONE);
        }else{
            //如果数据不为空，则循环并赋值给该布局
            for (int i = 0; i < likeUserLists.size(); i++) {
                if (i==0){
                    textParser.append(likeUserLists.get(i).getUserNickName(),20,color);
                }else{
                    textParser.append(","+likeUserLists.get(i).getUserNickName(),20,color);
                }
            }
            textParser.parse(holder.likesUsernickname);
        }
        sendUnMomentLike(mUser.getUserId(),momentsId);
    }
    private void showTools(RecyclerHolder holder,FriendsCircle data) {
        item.itemOnClickListener(data.getMomentsCommentList(),data.getMomentsId());
        current_holder = holder;
        KeyboardUtils.showSoftInput(activity);
        holder.qfltool.setVisibility(View.VISIBLE);
        holder.btnsend.setBackgroundColor(activity.getResources().getColor(R.color.btn_gray_pressed_status));
        holder.btnsend.setEnabled(false);
        holder.etTextMsg.setFocusable(true);
        holder.etTextMsg.setFocusableInTouchMode(true);
        holder.etTextMsg.requestFocus();
        mNormalPopup.dismiss();
    }
    private void showLike(RecyclerHolder holder,FriendsCircle data,TextParser textParser){
        holder.llPingLun.setVisibility(View.VISIBLE);
        holder.rlLike.setVisibility(View.VISIBLE);
        //显示点赞用户
        for (int i = 0; i < data.getLikeUserList().size(); i++) {
            String userId = data.getLikeUserList().get(i).getUserId();
            if (i == data.getLikeUserList().size() - 1) {
                textParser.append(data.getLikeUserList().get(i).getUserNickName(), 20, color, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(activity, UserInfoActivity.class);
                        intent.putExtra("userId", userId);
                        activity.startActivity(intent);
                    }
                });
            } else {
                textParser.append(data.getLikeUserList().get(i).getUserNickName() + ", ", 20, color, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(activity, UserInfoActivity.class);
                        intent.putExtra("userId", userId);
                        activity.startActivity(intent);
                    }
                });
            }
        }
        textParser.parse(holder.likesUsernickname);
    }
    private void setTvLike(TextView tvLike,Boolean isLike){
        tvLike.setText(isLike?"  取消":"  点赞");
    }
}
