package com.wqs.wechat.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.qmuiteam.qmui.layout.QMUIFrameLayout;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;

import com.wqs.wechat.R;
import com.wqs.wechat.entity.FriendsCircle;

import com.wqs.wechat.utils.TimeUtil;
import com.xujiaji.happybubble.BubbleDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.edu.heuet.littlecurl.ninegridview.base.NineGridViewAdapter;
import cn.edu.heuet.littlecurl.ninegridview.bean.NineGridItem;
import cn.edu.heuet.littlecurl.ninegridview.preview.NineGridViewGroup;
import cn.jmessage.support.qiniu.android.utils.Json;

public class FriendsCircleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG = "FriendsCircleAdapter";
    Activity activity;
    private QMUIPopup mNormalPopup;
    private List<FriendsCircle> friendsCircles;
    public FriendsCircleAdapter(List<FriendsCircle> list, Activity friendsCircleActivity) {
        super();
        this.activity = friendsCircleActivity;
        this.friendsCircles = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_friends_circle,parent,false);
        return new RecyclerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        QMUIFrameLayout qmuiFrameLayout = activity.findViewById(R.id.qfl_tool);
        FriendsCircle data = friendsCircles.get(position);
        Log.d(TAG, "onBindViewHolder: " + data);
        TextView nickname = holder.itemView.findViewById(R.id.tv_nick_name);
        TextView content = holder.itemView.findViewById(R.id.tv_content);
        TextView time = holder.itemView.findViewById(R.id.tv_create_time);
        ImageView avatar = holder.itemView.findViewById(R.id.sdv_avatar);
        LinearLayout pinglun = holder.itemView.findViewById(R.id.pinglun);
        ImageButton ibComment = holder.itemView.findViewById(R.id.ib_comment);
        NineGridViewGroup photos = holder.itemView.findViewById(R.id.gv_friends_circle_photo);
        View popuwind = LayoutInflater.from(activity).inflate(R.layout.layout_popu_view, null);
        BubbleDialog bubbleDialog = new BubbleDialog(activity);
        ibComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bubbleDialog
                        .setPosition(BubbleDialog.Position.LEFT)
                        .addContentView(popuwind)
                        .setTransParentBackground()
                        .setClickedView(view)
                        .show();
            }
        });
        TextView tvLike = popuwind.findViewById(R.id.tv_like);
        TextView tvComment = popuwind.findViewById(R.id.tv_comment);
        tvLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtils.showShort("点击点赞按钮");
                bubbleDialog.dismiss();
            }
        });
        tvComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qmuiFrameLayout.setVisibility(View.VISIBLE);
                KeyboardUtils.showSoftInput(activity);
            }
        });
        JSONArray photosData = JSON.parseArray(data.getMomentsPhotos());
        if (photosData  != null && photosData.size() > 0) {
            ArrayList<NineGridItem> nineGridItemList = new ArrayList<>();
            for (int i = 0; i <photosData.size() ; i++) {
                String thumbnailUrl = photosData.get(i).toString();
                String bigImageUrl = photosData.get(i).toString();
//                String videoUrl = photosData.get(i);
                nineGridItemList.add(new NineGridItem(thumbnailUrl, bigImageUrl));
            }
            NineGridViewAdapter nineGridViewAdapter = new NineGridViewAdapter(nineGridItemList);
            photos.setAdapter(nineGridViewAdapter);
        }
        nickname.setText(data.getUserNickName());
        Glide.with(avatar).load(data.getUserAvatar()).into(avatar);
        content.setText(data.getMomentsContent());
        time.setText(TimeUtil.getTimeStringAutoShort2(data.getTimestamp(),true));
    }

    @Override
    public int getItemCount() {
        return friendsCircles.size();
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
}
