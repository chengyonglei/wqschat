package com.wqs.wechat.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.lwkandroid.imagepicker.ImagePicker;
import com.lwkandroid.imagepicker.data.ImageBean;
import com.lwkandroid.imagepicker.data.ImagePickType;
import com.lwkandroid.widget.ninegridview.NineGirdImageContainer;
import com.lwkandroid.widget.ninegridview.NineGridBean;
import com.lwkandroid.widget.ninegridview.NineGridView;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.wqs.wechat.R;
import com.wqs.wechat.entity.User;
import com.wqs.wechat.utils.OssService;
import com.wqs.wechat.utils.PreferencesUtil;

import com.wqs.wechat.widget.GlideImageLoader;
import com.wqs.wechat.widget.ImagePickerLoader;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FriendsCircleSendActivity extends BaseActivity {
    @BindView(R.id.content)
    EditText content;
    @BindView(R.id.ninegridview)
    NineGridView mNineGridView;
    @BindView(R.id.qtb_topbar)
    QMUITopBar topbar;
    int num = 9;

    List<NineGridBean> resultList = new ArrayList<>();
    List<String> images = new ArrayList<>();
    private final int REQUEST_CODE_PICKER = 100;
    OssService ossService = new OssService(this);
    User mUser;
    private static final String TAG = "FriendsCircleSendActivi";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendcirclesend);
        ButterKnife.bind(this);
        PreferencesUtil.getInstance().init(this);
        mUser = PreferencesUtil.getInstance().getUser();
        initView();
    }

    private void initView() {
        resultList.clear();
        new ImagePicker()
                .cachePath(Environment.getExternalStorageDirectory().getAbsolutePath())
                .pickType(ImagePickType.MULTI)
                .displayer(new ImagePickerLoader())
                .maxNum(num)
                .start(FriendsCircleSendActivity.this, REQUEST_CODE_PICKER);
        topbar.addLeftImageButton(R.drawable.ic_back_topbar, 0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultList.clear();
                finish();
            }
        });
        @SuppressLint("InflateParams") View root = LayoutInflater.from(this).inflate(R.layout.send_viw_right, null);
        topbar.addRightView(root, 1);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        QMUIStatusBarHelper.translucent(this);
        QMUIStatusBarHelper.setStatusBarLightMode(this);

        mNineGridView.setImageLoader(new GlideImageLoader());
        mNineGridView.setColumnCount(4);
        mNineGridView.setIsEditMode(true);
        mNineGridView.setSingleImageSize(150);
        mNineGridView.setSingleImageRatio(0.8f);
        mNineGridView.setMaxNum(num);
        mNineGridView.setSpaceSize(1);
        mNineGridView.setIcDeleteResId(R.drawable.ic_baseline_delete_sweep_24);
        mNineGridView.setRatioOfDeleteIcon(0);
        mNineGridView.setIcAddMoreResId(R.drawable.ic_ngv_add_pic);
        mNineGridView.setOnItemClickListener(new NineGridView.onItemClickListener() {
            @Override
            public void onNineGirdAddMoreClick(int cha) {
                new ImagePicker()
                        .cachePath(Environment.getExternalStorageDirectory().getAbsolutePath())
                        .pickType(ImagePickType.MULTI)
                        .displayer(new ImagePickerLoader())
                        .maxNum(cha)
                        .start(FriendsCircleSendActivity.this, REQUEST_CODE_PICKER);
            }

            @Override
            public void onNineGirdItemClick(int position, NineGridBean gridBean, NineGirdImageContainer imageContainer) {
                //点击图片的监听
            }

            @Override
            public void onNineGirdItemDeleted(int position, NineGridBean gridBean, NineGirdImageContainer imageContainer) {
                //编辑模式下，某张图片被删除后回调这里
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            List<ImageBean> list = data.getParcelableArrayListExtra(ImagePicker.INTENT_RESULT_DATA);
            for (ImageBean imageBean : list) {
                resultList.clear();
                NineGridBean nineGirdData = new NineGridBean(imageBean.getImagePath());
                resultList.add(nineGirdData);
                ossService.initOSSClient();
                ossService.beginupload(this, imageBean.getImageId(), imageBean.getImagePath());
                ossService.setProgressCallback(new OssService.ProgressCallback() {
                    @Override
                    public void onProgressCallback(double progress) {

                    }
                    public void onSuccess(String objectKey){
                        Log.d(TAG, "onSuccess: " +objectKey);
                        images.add(objectKey);
                    }
                });
                mNineGridView.addDataList(resultList);
            }
        }
    }
}