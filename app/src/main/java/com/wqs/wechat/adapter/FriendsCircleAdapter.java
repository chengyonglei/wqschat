package com.wqs.wechat.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lwkandroid.widget.ninegridview.NineGridBean;
import com.lwkandroid.widget.ninegridview.NineGridView;
import com.wqs.wechat.R;
import com.wqs.wechat.activity.FriendsCircleActivity;
import com.wqs.wechat.entity.FriendsCircle;
import com.wqs.wechat.entity.MyMedia;
import com.wqs.wechat.entity.RecyclerViewItem;
import com.wqs.wechat.utils.OssService;
import com.wqs.wechat.widget.FriendsCirclePhotoGridView;
import com.wqs.wechat.widget.GlideImageLoader;

import java.util.ArrayList;
import java.util.List;

import cn.edu.heuet.littlecurl.ninegridview.base.NineGridViewAdapter;
import cn.edu.heuet.littlecurl.ninegridview.bean.NineGridItem;
import cn.edu.heuet.littlecurl.ninegridview.preview.NineGridItemWrapperView;
import cn.edu.heuet.littlecurl.ninegridview.preview.NineGridViewGroup;

public class FriendsCircleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG = "FriendsCircleAdapter";
    Activity activity;
    private List<RecyclerViewItem> recyclerViewItemList;
    public FriendsCircleAdapter(ArrayList<RecyclerViewItem> list, FriendsCircleActivity friendsCircleActivity) {
        super();
        this.activity = friendsCircleActivity;
        this.recyclerViewItemList = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_friends_circle,parent,false);
        return new RecyclerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RecyclerViewItem recyclerViewItem = recyclerViewItemList.get(position);
        TextView nickname = holder.itemView.findViewById(R.id.tv_nick_name);
        TextView content = holder.itemView.findViewById(R.id.tv_content);
        TextView time = holder.itemView.findViewById(R.id.tv_create_time);
        ImageView avatar = holder.itemView.findViewById(R.id.sdv_avatar);
        LinearLayout pinglun = holder.itemView.findViewById(R.id.pinglun);

        NineGridViewGroup photos = holder.itemView.findViewById(R.id.gv_friends_circle_photo);
        ArrayList<MyMedia> mediaList = recyclerViewItem.getMediaList();
        if (mediaList  != null && mediaList.size() > 0) {
            ArrayList<NineGridItem> nineGridItemList = new ArrayList<>();
            for (MyMedia myMedia : mediaList) {
                String thumbnailUrl = myMedia.getImageUrl();
                String bigImageUrl = myMedia.getImageUrl();
                String videoUrl = myMedia.getVideoUrl();
                nineGridItemList.add(new NineGridItem(thumbnailUrl, bigImageUrl, videoUrl));
            }
            NineGridViewAdapter nineGridViewAdapter = new NineGridViewAdapter(nineGridItemList);
            photos.setAdapter(nineGridViewAdapter);
        }
        nickname.setText(recyclerViewItem.getNickName());
        Glide.with(avatar).load(recyclerViewItem.getHeadImageUrl()).into(avatar);
        content.setText(recyclerViewItem.getContent());
        time.setText(recyclerViewItem.getCreateTime());
    }

    @Override
    public int getItemCount() {
        return recyclerViewItemList.size();
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
