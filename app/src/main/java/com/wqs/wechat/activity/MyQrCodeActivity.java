package com.wqs.wechat.activity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.wqs.wechat.R;
import com.wqs.wechat.cons.Constant;
import com.wqs.wechat.entity.User;
import com.wqs.wechat.utils.PreferencesUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.wqs.wechat.utils.QrcodeUtil;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

/**
 * 二维码名片
 *
 * @author zhou
 */
public class MyQrCodeActivity extends BaseActivity {
    private static final String TAG = "MyQrCodeActivity";
    private TextView mTitleTv;

    private SimpleDraweeView mAvatarSdv;
    private TextView mNickNameTv;
    private ImageView mSexIv;
    private SimpleDraweeView mQrCodeSdv;

    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_qr_code);
        initStatusBar();

        mUser = PreferencesUtil.getInstance().getUser();
        initView();
    }

    private void initView() {
        mTitleTv = findViewById(R.id.tv_title);
        TextPaint paint = mTitleTv.getPaint();
        paint.setFakeBoldText(true);

        mAvatarSdv = findViewById(R.id.sdv_avatar);
        mNickNameTv = findViewById(R.id.tv_nick_name);
        mSexIv = findViewById(R.id.iv_sex);
        mQrCodeSdv = findViewById(R.id.sdv_qr_code);

        if (!TextUtils.isEmpty(mUser.getUserAvatar())) {
            mAvatarSdv.setImageURI(Uri.parse(mUser.getUserAvatar()));
        }
        mNickNameTv.setText(mUser.getUserNickName());

        if (Constant.USER_SEX_MALE.equals(mUser.getUserSex())) {
            mSexIv.setImageResource(R.mipmap.icon_sex_male);
        } else if (Constant.USER_SEX_FEMALE.equals(mUser.getUserSex())) {
            mSexIv.setImageResource(R.mipmap.icon_sex_female);
        } else {
            mSexIv.setVisibility(View.GONE);
        }
        Bitmap img = QrcodeUtil.encode(mUser.getUserId(),900,900);
        mQrCodeSdv.setImageBitmap(img);
    }

    public void back(View view) {
        finish();
    }
}
