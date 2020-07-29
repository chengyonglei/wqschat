package com.wqs.wechat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ActivityUtils;
import com.bumptech.glide.Glide;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.wqs.wechat.R;
import com.wqs.wechat.activity.UserInfoActivity;
import com.wqs.wechat.entity.FriendsCircle;
import com.wqs.wechat.entity.LikeUserList;

import java.util.List;

public class FriendLikeUserListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<LikeUserList> list;
    public FriendLikeUserListAdapter(List<LikeUserList> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friends_circle_like,parent,false);
        return new RecyclerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TextView nickname = holder.itemView.findViewById(R.id.nickname);
        nickname.setText(list.get(position).getUserNickName());
        nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UserInfoActivity.class);
                intent.putExtra("userId",list.get(position).getUserId());
                ActivityUtils.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class RecyclerHolder extends RecyclerView.ViewHolder {

        public RecyclerHolder(View view) {
            super(view);

        }
    }
}
