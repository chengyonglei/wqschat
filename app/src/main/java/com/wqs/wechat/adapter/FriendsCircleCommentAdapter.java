package com.wqs.wechat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.wqs.wechat.R;
import com.wqs.wechat.entity.FriendsCircleComment;
import com.wqs.wechat.utils.TextParser;

import java.util.List;

/**
 * 朋友圈评论
 *
 * @author zhou
 */
public class FriendsCircleCommentAdapter extends RecyclerView.Adapter<FriendsCircleCommentAdapter.RecyclerHolder> {
    List<FriendsCircleComment> momentsCommentList;
    Context context;
    int color = Color.rgb(87, 107, 149);
    public FriendsCircleCommentAdapter(List<FriendsCircleComment> momentsCommentList, Context context) {
        this.momentsCommentList = momentsCommentList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friends_circle_comment, parent, false);
        return new RecyclerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerHolder holder, int position) {
        FriendsCircleComment data = momentsCommentList.get(position);
        holder.nickname.setText(data.getCommentUserNickName()+":");
        holder.comment.setText(data.getCommentContent());
        holder.llcomment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtils.showShort("点击了会话");
            }
        });
    }


    @Override
    public int getItemCount() {
        return momentsCommentList.size();
    }

    class RecyclerHolder extends RecyclerView.ViewHolder {
        TextView comment,nickname;
        LinearLayout llcomment;
        public RecyclerHolder(@NonNull View itemView) {
            super(itemView);
            nickname = itemView.findViewById(R.id.tv_comment_nickname);
            comment = itemView.findViewById(R.id.tv_comment);
            llcomment = itemView.findViewById(R.id.ll_commnet);
        }
    }
}
