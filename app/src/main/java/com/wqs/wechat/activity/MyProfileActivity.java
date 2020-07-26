package com.wqs.wechat.activity;

import android.Manifest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.wqs.wechat.R;
import com.wqs.wechat.cons.Constant;
import com.wqs.wechat.entity.User;
import com.wqs.wechat.utils.FileUtil;
import com.wqs.wechat.utils.OssUtil;
import com.wqs.wechat.utils.PreferencesUtil;
import com.wqs.wechat.utils.VolleyUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.nereo.multi_image_selector.MultiImageSelector;

/**
 * 个人信息
 *
 * @author zhou
 */
public class MyProfileActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "MyProfileActivity";
    // 头像
    private RelativeLayout mAvatarRl;
    // 昵称
    private RelativeLayout mNickNameRl;
    // 微信号
    private RelativeLayout mWxIdRl;
    // 二维码
    private RelativeLayout mQrCodeRl;
    // 更多
    private RelativeLayout mMoreRl;
    // 我的地址
    private RelativeLayout mAddressRl;

    private TextView mTitleTv;

    private TextView mNickNameTv;
    private TextView mWxIdTv;
    private SimpleDraweeView mAvatarSdv;
    private ImageView mWxIdIv;

    private VolleyUtil mVolleyUtil;


    private static final int UPDATE_USER_NICK_NAME = 3;
    private static final int UPDATE_USER_WX_ID = 4;
    private static final int REQUEST_IMAGE = 2;
    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    private User mUser;
    private ArrayList<String> mSelectPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        initStatusBar();
        mVolleyUtil = VolleyUtil.getInstance(this);
        PreferencesUtil.getInstance().init(this);
        mUser = PreferencesUtil.getInstance().getUser();
        initView();
    }

    private void initView() {
        mTitleTv = findViewById(R.id.tv_title);
        TextPaint paint = mTitleTv.getPaint();
        paint.setFakeBoldText(true);

        mAvatarRl = findViewById(R.id.rl_avatar);

        mNickNameRl = findViewById(R.id.rl_nick_name);
        mNickNameTv = findViewById(R.id.tv_nick_name);

        mWxIdRl = findViewById(R.id.rl_wx_id);
        mWxIdTv = findViewById(R.id.tv_wx_id);
        mWxIdIv = findViewById(R.id.iv_wx_id);

        mQrCodeRl = findViewById(R.id.rl_qr_code);

        mMoreRl = findViewById(R.id.rl_more);

        mAvatarSdv = findViewById(R.id.sdv_avatar);

        mAddressRl = findViewById(R.id.rl_address);

        mNickNameTv.setText(mUser.getUserNickName());

        String userAvatar = mUser.getUserAvatar();
        if (!TextUtils.isEmpty(userAvatar)) {
            String resizeAvatarUrl = OssUtil.resize(userAvatar);
            mAvatarSdv.setImageURI(resizeAvatarUrl);
        }

        mAvatarRl.setOnClickListener(this);
        mNickNameRl.setOnClickListener(this);

        mQrCodeRl.setOnClickListener(this);
        mMoreRl.setOnClickListener(this);
        mAvatarSdv.setOnClickListener(this);
        mAddressRl.setOnClickListener(this);

        renderWxId(mUser);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_avatar:
                pickImage();
                break;
            case R.id.sdv_avatar:
                Intent intent = new Intent(this, BigImageActivity.class);
                intent.putExtra("imgUrl", mUser.getUserAvatar());
                startActivity(intent);
                break;
            case R.id.rl_nick_name:
                // 昵称
                startActivityForResult(new Intent(this, EditNameActivity.class), UPDATE_USER_NICK_NAME);
                break;
            case R.id.rl_wx_id:
                startActivityForResult(new Intent(this, EditWeChatIdActivity.class), UPDATE_USER_WX_ID);
                break;
            case R.id.rl_qr_code:
                startActivity(new Intent(this, MyQrCodeActivity.class));
                break;
            case R.id.rl_more:
                startActivity(new Intent(this, MyMoreProfileActivity.class));
                break;
            case R.id.rl_address:
                startActivity(new Intent(this, MyAddressActivity.class));
                break;
        }
    }

    public void back(View view) {
        finish();
    }

    /**
     * 渲染微信ID
     */
    private void renderWxId(User user) {
        mWxIdTv.setText(user.getUserWxId());
        // 微信号只能修改一次
        if (Constant.USER_WX_ID_MODIFY_FLAG_TRUE.equals(user.getUserWxIdModifyFlag())) {
            mWxIdIv.setVisibility(View.GONE);
            mWxIdRl.setClickable(false);
        } else {
            mWxIdRl.setOnClickListener(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            final User user = PreferencesUtil.getInstance().getUser();
            switch (requestCode) {
                case REQUEST_IMAGE:
                    if (resultCode == RESULT_OK) {
                        mSelectPath = data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT);
                        PreferencesUtil.getInstance().setUser(user);
                        mAvatarSdv.setImageBitmap(BitmapFactory.decodeFile(mSelectPath.get(0)));
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                List<String> imageList = FileUtil.uploadFile(Constant.BASE_URL + "oss/file", mSelectPath.get(0));
                                if (null != imageList && imageList.size() > 0) {
                                    Log.d(TAG, "run: " + imageList.get(0));
                                    updateUserAvatar(user.getUserId(), imageList.get(0));
                                }
                            }
                        }).start();
                    }
                    break;
                case UPDATE_USER_NICK_NAME:
                    // 昵称
                    mNickNameTv.setText(user.getUserNickName());
                    break;
                case UPDATE_USER_WX_ID:
                    renderWxId(user);
                    break;
            }
        }
    }

    /**
     * 修改用户头像
     *
     * @param userId     用户ID
     * @param userAvatar 用户头像
     */
    private void updateUserAvatar(String userId, final String userAvatar) {
        String url = Constant.BASE_URL + "users/" + userId + "/userAvatar";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("userAvatar", userAvatar);

        mVolleyUtil.httpPutRequest(url, paramMap, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                mUser.setUserAvatar(userAvatar);
                PreferencesUtil.getInstance().setUser(mUser);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (volleyError instanceof NetworkError) {
                    Toast.makeText(MyProfileActivity.this, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                } else if (volleyError instanceof TimeoutError) {
                    Toast.makeText(MyProfileActivity.this, R.string.network_time_out, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    private void pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.mis_permission_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            int maxNum = 1;
            MultiImageSelector.create().count(maxNum).single().showCamera(true).origin(mSelectPath).start(MyProfileActivity.this, REQUEST_IMAGE);
        }
    }

    private void requestPermission(final String permission, String rationale, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.mis_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.mis_permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MyProfileActivity.this, new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.mis_permission_dialog_cancel, null)
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_READ_ACCESS_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
