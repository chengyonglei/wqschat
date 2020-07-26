package com.wqs.wechat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.wqs.wechat.R;
import com.wqs.wechat.entity.FriendsCircle;
import com.wqs.wechat.entity.User;
import com.wqs.wechat.utils.PreferencesUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 设置
 *
 * @author zhou
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.rl_title)
    RelativeLayout rlTitle;
    @BindView(R.id.rl_account_security)
    RelativeLayout rlAccountSecurity;
    @BindView(R.id.rl_notifications)
    RelativeLayout rlNotifications;
    @BindView(R.id.rl_do_not_disturb)
    RelativeLayout rlDoNotDisturb;
    @BindView(R.id.rl_chats)
    RelativeLayout rlChats;
    @BindView(R.id.rl_privacy)
    RelativeLayout rlPrivacy;
    @BindView(R.id.rl_general)
    RelativeLayout rlGeneral;
    @BindView(R.id.rl_about)
    RelativeLayout rlAbout;
    @BindView(R.id.rl_help_feedback)
    RelativeLayout rlHelpFeedback;
    @BindView(R.id.rl_wechat_services)
    RelativeLayout rlWechatServices;
    @BindView(R.id.rl_switch_account)
    RelativeLayout rlSwitchAccount;
    @BindView(R.id.rl_log_out)
    RelativeLayout rlLogOut;
    /**
     * 标题
     */
    private TextView mTitleTv;

    /**
     * 账号与安全
     */
    private RelativeLayout mAccountSecurityRl;

    /**
     * 退出
     */
    private RelativeLayout mLogOutRl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        initStatusBar();
        PreferencesUtil.getInstance().init(this);
        initView();
    }

    private void initView() {

    }

    public void back(View view) {
        finish();
    }

    @OnClick({R.id.rl_account_security, R.id.rl_about, R.id.rl_log_out})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_account_security:
                ActivityUtils.startActivity(AccountSecurityActivity.class);
                break;
            case R.id.rl_about:
                ActivityUtils.startActivity(AboutActivity.class);
                break;
            case R.id.rl_log_out:
                // 清除sharedpreferences中存储信息
                PreferencesUtil.getInstance().setLogin(false);
                PreferencesUtil.getInstance().setUser(null);

                // 清除通讯录
                User.deleteAll(User.class);
                // 清除朋友圈
                FriendsCircle.deleteAll(FriendsCircle.class);

                // 跳转至登录页
                Intent intent = new Intent(this, SplashActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}
