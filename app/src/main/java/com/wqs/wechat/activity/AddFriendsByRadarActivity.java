package com.wqs.wechat.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.wqs.wechat.R;
import com.wqs.wechat.entity.User;
import com.wqs.wechat.utils.PreferencesUtil;
import com.facebook.drawee.view.SimpleDraweeView;

public class AddFriendsByRadarActivity extends FragmentActivity {
    private SimpleDraweeView mAvatarSdv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends_by_radar);

        mAvatarSdv = findViewById(R.id.sdv_avatar);
        User user = PreferencesUtil.getInstance().getUser();
        mAvatarSdv.setImageURI(Uri.parse(user.getUserAvatar()));
    }

    public void back(View view) {
        finish();
    }
}
