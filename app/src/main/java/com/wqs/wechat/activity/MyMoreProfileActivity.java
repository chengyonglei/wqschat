package com.wqs.wechat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.NetworkError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.wqs.wechat.R;
import com.wqs.wechat.cons.Constant;
import com.wqs.wechat.entity.User;
import com.wqs.wechat.utils.PreferencesUtil;
import com.wqs.wechat.utils.VolleyUtil;
import com.wqs.wechat.widget.LoadingDialog;

import java.util.HashMap;
import java.util.Map;


/**
 * 更多信息
 *
 * @author zhou
 */
public class MyMoreProfileActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "MyMoreProfileActivity";
    private TextView mTitleTv;

    private RelativeLayout mSexRl;
    private RelativeLayout mSignRl;
    private RelativeLayout mRegionRl;
    private TextView mSexTv;
    private TextView mSignTv;
    private TextView mRegionTv;

    private User mUser;
    LoadingDialog mDialog;
    VolleyUtil mVolleyUtil;
    String mRegion="";

    private static final int UPDATE_USER_SIGN = 5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_more_profile);
        initStatusBar();

        PreferencesUtil.getInstance().init(this);
        mUser = PreferencesUtil.getInstance().getUser();
        initView();
    }

    private void initView() {
        mTitleTv = findViewById(R.id.tv_title);
        TextPaint paint = mTitleTv.getPaint();
        paint.setFakeBoldText(true);

        mSexRl = findViewById(R.id.rl_sex);
        mSexTv = findViewById(R.id.tv_sex);

        mSignRl = findViewById(R.id.rl_sign);
        mSignTv = findViewById(R.id.tv_sign);

        mRegionRl = findViewById(R.id.rl_region);
        mRegionTv = findViewById(R.id.tv_region);

        String userSex = mUser.getUserSex();

        if (Constant.USER_SEX_MALE.equals(userSex)) {
            mSexTv.setText(getString(R.string.sex_male));
        } else if (Constant.USER_SEX_FEMALE.equals(userSex)) {
            mSexTv.setText(getString(R.string.sex_female));
        }
        mSignTv.setText(mUser.getUserSign());
        mRegionTv.setText(mUser.getUserRegion());

        mSexRl.setOnClickListener(this);
        mSignRl.setOnClickListener(this);
        mRegionRl.setOnClickListener(this);
    }

    public void back(View view) {
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_sex:
                startActivity(new Intent(this, SetGenderActivity.class));
                break;
            case R.id.rl_region:
                startActivity(new Intent(MyMoreProfileActivity.this, PickProvinceActivity.class));
                break;
            case R.id.rl_sign:
                // 签名
                startActivityForResult(new Intent(this, EditSignActivity.class), UPDATE_USER_SIGN);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            final User user = PreferencesUtil.getInstance().getUser();
            switch (requestCode) {
                case UPDATE_USER_SIGN:
                    // 个性签名
                    mSignTv.setText(user.getUserSign());
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //返回地区信息，没有保存处理；只做界面布局
        String pickedProvince = PreferencesUtil.getInstance().getPickedProvince();
        String pickedCity = PreferencesUtil.getInstance().getPickedCity();
        String pickedDistrict = PreferencesUtil.getInstance().getPickedDistrict();
        if (!TextUtils.isEmpty(pickedProvince)
                && !TextUtils.isEmpty(pickedCity)
                && !TextUtils.isEmpty(pickedDistrict)) {
            StringBuffer addressInfoBuffer = new StringBuffer();
            addressInfoBuffer.append(pickedProvince).append(" ")
                    .append(pickedCity).append(" ")
                    .append(pickedDistrict);
            if (addressInfoBuffer.toString().length()>0){
                mRegion = addressInfoBuffer.toString();
            }
            mRegionTv.setText(mRegion);
            mUser.setUserRegion(mRegion);
            PreferencesUtil.getInstance().setUser(mUser);
        }
        User user = PreferencesUtil.getInstance().getUser();
        if (Constant.USER_SEX_MALE.equals(user.getUserSex())) {
            mSexTv.setText(getString(R.string.sex_male));
        } else if (Constant.USER_SEX_FEMALE.equals(user.getUserSex())) {
            mSexTv.setText(getString(R.string.sex_female));
        } else {
            mSexTv.setText("");
        }
    }
    /**
     * 修改用户地区
     *
     * @param userId  用户ID
     * @param userRegion 用户地区
     */
    private void updateUserRegion(String userId, final String userRegion){
        String url = Constant.BASE_URL + "users/" + userId + "/userRegion";
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("userRegion", userRegion);
        mVolleyUtil.httpPutRequest(url, paramMap, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                mDialog.dismiss();
                mUser.setUserRegion(userRegion);
                PreferencesUtil.getInstance().setUser(mUser);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mDialog.dismiss();
                if (volleyError instanceof NetworkError) {
                    Toast.makeText(MyMoreProfileActivity.this, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
                } else if (volleyError instanceof TimeoutError) {
                    Toast.makeText(MyMoreProfileActivity.this, R.string.network_time_out, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
