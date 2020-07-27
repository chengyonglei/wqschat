package com.wqs.wechat.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.NetworkError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.qmuiteam.qmui.layout.QMUIButton;
import com.qmuiteam.qmui.layout.QMUIFrameLayout;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;

import com.qmuiteam.qmui.widget.popup.QMUIPopups;
import com.qmuiteam.qmui.widget.popup.QMUIQuickAction;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.wqs.wechat.R;
import com.wqs.wechat.activity.FriendsCircleActivity;
import com.wqs.wechat.activity.FriendsCircleSendActivity;
import com.wqs.wechat.cons.Constant;
import com.wqs.wechat.entity.FriendsCircle;

import com.wqs.wechat.entity.FriendsCircleComment;
import com.wqs.wechat.entity.LikeUserList;
import com.wqs.wechat.entity.User;
import com.wqs.wechat.utils.PreferencesUtil;
import com.wqs.wechat.utils.TimeUtil;
import com.wqs.wechat.utils.VolleyUtil;
import com.wqs.wechat.widget.FriendsCircleCommentListView;
import com.wqs.wechat.widget.LoadingDialog;
import com.xujiaji.happybubble.BubbleDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.heuet.littlecurl.ninegridview.base.NineGridViewAdapter;
import cn.edu.heuet.littlecurl.ninegridview.bean.NineGridItem;
import cn.edu.heuet.littlecurl.ninegridview.preview.NineGridViewGroup;
import cn.jmessage.support.qiniu.android.utils.Json;

import static com.baidu.mapapi.BMapManager.getContext;

public class FriendsCircleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG = "FriendsCircleAdapter";
    Activity activity;
    private VolleyUtil mVolleyUtil;
    LoadingDialog mDialog;
    User mUser;
    private ItemOnClickListener item;
    FriendLikeUserListAdapter likeUserListAdapter;
    FriendsCircleCommentAdapter commentAdapter;
    private List<FriendsCircle> friendsCircles;
    private QMUIPopup mNormalPopup;
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_friends_circle,parent,false);
        return new RecyclerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        QMUIFrameLayout qfltool = activity.findViewById(R.id.qfl_tool);
        EditText etTextMsg = activity.findViewById(R.id.et_text_msg);
        QMUIRoundButton btnsend = activity.findViewById(R.id.btn_send);
        FriendsCircle data = friendsCircles.get(position);
        TextView nickname = holder.itemView.findViewById(R.id.tv_nick_name);
        TextView content = holder.itemView.findViewById(R.id.tv_content);
        TextView time = holder.itemView.findViewById(R.id.tv_create_time);
        ImageView avatar = holder.itemView.findViewById(R.id.sdv_avatar);
        LinearLayout pinglun = holder.itemView.findViewById(R.id.pinglun);
        ImageButton ibComment = holder.itemView.findViewById(R.id.ib_comment);
        NineGridViewGroup photos = holder.itemView.findViewById(R.id.gv_friends_circle_photo);
        View popuwind = LayoutInflater.from(activity).inflate(R.layout.layout_popu_view, null);
        ibComment.setOnClickListener(new View.OnClickListener() {
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
        TextView tvLike = popuwind.findViewById(R.id.tv_like);
        TextView tvComment = popuwind.findViewById(R.id.tv_comment);
        tvLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMomentLike(mUser.getUserId(),data.getMomentsId());

            }
        });
        tvComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item.itemOnClickListener(data.getMomentsId());
                KeyboardUtils.showSoftInput(activity);
                qfltool.setVisibility(View.VISIBLE);
                btnsend.setBackgroundColor(activity.getResources().getColor(R.color.btn_gray_pressed_status));
                btnsend.setEnabled(false);
                etTextMsg.setFocusable(true);
                etTextMsg.setFocusableInTouchMode(true);
                etTextMsg.requestFocus();
                mNormalPopup.dismiss();
            }
        });
        JSONArray photosData = JSON.parseArray(data.getMomentsPhotos());
        if (photosData  != null && photosData.size() > 0) {
            ArrayList<NineGridItem> nineGridItemList = new ArrayList<>();
            for (int i = 0; i <photosData.size() ; i++) {
                String thumbnailUrl = photosData.get(i).toString();
                String bigImageUrl = photosData.get(i).toString();
                nineGridItemList.add(new NineGridItem(thumbnailUrl, bigImageUrl));
            }
            NineGridViewAdapter nineGridViewAdapter = new NineGridViewAdapter(nineGridItemList);
            photos.setAdapter(nineGridViewAdapter);
        }
        nickname.setText(data.getUserNickName());
        Glide.with(avatar).load(data.getUserAvatar()).into(avatar);
        if (data.getMomentsContent().length()==0){
            content.setVisibility(View.GONE);
        }else{
            content.setText(data.getMomentsContent());
        }
        time.setText(TimeUtil.getTimeStringAutoShort2(data.getTimestamp(),true));
        RelativeLayout rlLike = holder.itemView.findViewById(R.id.rl_like);
        RecyclerView rvlikelist = holder.itemView.findViewById(R.id.rv_likelist);
        List<LikeUserList> likelist = data.getLikeUserList();
        if (likelist.size()>0){
            pinglun.setVisibility(View.VISIBLE);
            rlLike.setVisibility(View.VISIBLE);
            likeUserListAdapter = new FriendLikeUserListAdapter(likelist,getContext());
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),10);
            rvlikelist.setLayoutManager(gridLayoutManager);
            rvlikelist.setAdapter(likeUserListAdapter);
        }
        FriendsCircleCommentListView mFriendsCirleCommentListView  = holder.itemView.findViewById(R.id.lv_comment_list);
        List<FriendsCircleComment> commentlist = data.getFriendsCircleCommentList();
        Log.d(TAG, "onBindViewHolder: " + commentlist.size());
        if (commentlist.size()>0){
            pinglun.setVisibility(View.VISIBLE);
            commentAdapter = new FriendsCircleCommentAdapter(commentlist,getContext());
            mFriendsCirleCommentListView.setAdapter(commentAdapter);
        }
    }

    @Override
    public int getItemCount() {
        return friendsCircles.size();
    }


    public void setmItemOnClickListener(ItemOnClickListener listener){
        this.item = listener;
    }

    public interface ItemOnClickListener{
        /**
         * 传递点击的view
         * @param circleId
         */
        public void itemOnClickListener(String circleId);
    }
    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    private static class RecyclerHolder extends RecyclerView.ViewHolder {
        TextView nickname;
        public RecyclerHolder(View view) {
            super(view);
            nickname = view.findViewById(R.id.tv_nick_name);
        }
    }
    private void sendMomentLike(String userId, String momentsId){
        String url = Constant.BASE_URL + "moments/" + momentsId +"/like";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("userId",userId);
        mVolleyUtil.httpPostRequest(url, paramMap, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (s.equals("LIKE_MOMENTS_SUCCESS")){
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
}
