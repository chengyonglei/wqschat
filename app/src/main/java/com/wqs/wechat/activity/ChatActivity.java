package com.wqs.wechat.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.wqs.wechat.R;
import com.wqs.wechat.adapter.EmojiAdapter;
import com.wqs.wechat.adapter.EmojiPagerAdapter;
import com.wqs.wechat.adapter.MessageAdapter;
import com.wqs.wechat.cons.Constant;
import com.wqs.wechat.dao.MessageDao;
import com.wqs.wechat.entity.Message;
import com.wqs.wechat.entity.User;
import com.wqs.wechat.entity.enums.MessageStatus;
import com.wqs.wechat.utils.AudioRecoderUtils;
import com.wqs.wechat.utils.CommonUtil;
import com.wqs.wechat.utils.EmojiUtil;
import com.wqs.wechat.utils.FileUtil;
import com.wqs.wechat.utils.JimUtil;
import com.wqs.wechat.utils.PopupWindowFactory;
import com.wqs.wechat.utils.PreferencesUtil;
import com.wqs.wechat.utils.TimeUtil;
import com.wqs.wechat.utils.TimeUtils;
import com.wqs.wechat.utils.VolleyUtil;
import com.wqs.wechat.widget.ConfirmDialog;
import com.wqs.wechat.widget.ExpandGridView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * 聊天
 *
 * @author zhou
 */
public class ChatActivity extends BaseActivity implements View.OnClickListener {
    public static final int REQUEST_CODE_VOICE = 5;
    public static final int REQUEST_CODE_IMAGE_ALBUM = 6;
    public static final int REQUEST_CODE_IMAGE_CAMERA = 7;
    public static final int REQUEST_CODE_LOCATION = 8;

    private InputMethodManager mManager;

    private TextView mFromNickNameTv;

    private LinearLayout mMoreLl;
    private LinearLayout mEmojiContainerLl;
    private RelativeLayout rl;
    // 语音和文字切换
    /**
     * 切换成语音
     */
    private Button mSetModeVoiceBtn;

    /**
     * 切换成文字
     */
    private Button mSetModeKeyboardBtn;

    /**
     * 按住说话
     */
    private LinearLayout mPressToSpeakLl;

    // 各种消息类型容器
    private LinearLayout mBtnContainerLl;
    // 发送图片-"相册"
    private LinearLayout mImageAlbumLl;
    // 发送图片-"拍照"
    private LinearLayout mImageCameraLl;
    // 位置
    private LinearLayout mChatLocationLl;


    private ImageView mEmojiNormalIv;
    private ImageView mEmojiCheckedIv;

    private Button mMoreBtn;
    private Button mSendBtn;

    private EditText mTextMsgEt;
    private RelativeLayout mTextMsgRl;

    private ImageView mSingleChatSettingIv;

    private ListView mMessageLv;
    private MessageAdapter mMessageAdapter;
    List<Message> mMessageList;

    // intent传值
    // 单聊
    private String targetType;
    private String fromUserId;
    private String fromUserNickName;
    private String fromUserAvatar;

    // 群聊
    private String groupId;
    private String groupDesc;
    private String memberNum;

    User mUser;
    private VolleyUtil mVolleyUtil;

    private int mMessageIndex;

    MessageDao mMessageDao;

    private String mImageName;

    private List<String> mEmojiList;
    private ViewPager mEmojiVp;

    //语音消息
    AudioRecoderUtils mAudioRecoderUtils = new AudioRecoderUtils();
    private ImageView mImageView;
    private TextView mTextView;
    private PopupWindowFactory mPop;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initStatusBar();
        mVolleyUtil = VolleyUtil.getInstance(this);
        JMessageClient.registerEventReceiver(this);
        PreferencesUtil.getInstance().init(this);
        mUser = PreferencesUtil.getInstance().getUser();
        mMessageDao = new MessageDao();
        initView();
        setUpView();
        initCamera();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        mFromNickNameTv = findViewById(R.id.tv_from_nick_name);

        mMessageLv = findViewById(R.id.lv_message);

        mMoreLl = findViewById(R.id.ll_more);
        mEmojiContainerLl = findViewById(R.id.ll_emoji_container);
        mBtnContainerLl = findViewById(R.id.ll_btn_container);

        mImageAlbumLl = findViewById(R.id.ll_image_album);
        mImageCameraLl = findViewById(R.id.ll_image_camera);
        mChatLocationLl = findViewById(R.id.ll_chat_location);
        rl = findViewById(R.id.root_layout);
        mSetModeVoiceBtn = findViewById(R.id.btn_set_mode_voice);
        mSetModeKeyboardBtn = findViewById(R.id.btn_set_mode_keyboard);

        mPressToSpeakLl = findViewById(R.id.ll_press_to_speak);

        mEmojiNormalIv = findViewById(R.id.iv_emoji_normal);
        mEmojiCheckedIv = findViewById(R.id.iv_emoji_checked);

        mMoreBtn = findViewById(R.id.btn_more);
        mSendBtn = findViewById(R.id.btn_send);

        mTextMsgEt = findViewById(R.id.et_text_msg);
        mTextMsgRl = findViewById(R.id.rl_text_msg);

        mSingleChatSettingIv = findViewById(R.id.iv_setting);


        mTextMsgEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    // 获取焦点
                    // 隐藏消息类型容器
                    mBtnContainerLl.setVisibility(View.GONE);
                    // 聊天页拉至最下
                    mMessageLv.setSelection(mMessageLv.getCount() - 1);

                    // 隐藏表情
                    mEmojiCheckedIv.setVisibility(View.GONE);
                    mEmojiNormalIv.setVisibility(View.VISIBLE);
                    mEmojiContainerLl.setVisibility(View.GONE);
                }
            }
        });


        mTextMsgEt.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                if (TextUtils.isEmpty(charSequence)) {
                    mMoreBtn.setVisibility(View.VISIBLE);
                    mSendBtn.setVisibility(View.GONE);
                } else {
                    mMoreBtn.setVisibility(View.GONE);
                    mSendBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mMessageLv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                KeyboardUtils.hideSoftInput(ChatActivity.this);

                // 隐藏更多
                if (mEmojiContainerLl.getVisibility() == View.VISIBLE) {
                    mEmojiContainerLl.setVisibility(View.GONE);
                    mBtnContainerLl.setVisibility(View.VISIBLE);
                    mEmojiNormalIv.setVisibility(View.VISIBLE);
                    mEmojiCheckedIv.setVisibility(View.GONE);
                } else {
                    mMoreLl.setVisibility(View.GONE);
                }

                return false;
            }
        });

        // 表情
        mEmojiVp = findViewById(R.id.vp_emoji);
        initEmojiList(40);
        List<View> emojiViews = new ArrayList<>();
        View emojiView1 = getGridChildView(1);
        View emojiView2 = getGridChildView(2);
        emojiViews.add(emojiView1);
        emojiViews.add(emojiView2);
        mEmojiVp.setAdapter(new EmojiPagerAdapter(emojiViews));

        mSingleChatSettingIv.setOnClickListener(this);
        mImageAlbumLl.setOnClickListener(this);
        mImageCameraLl.setOnClickListener(this);
        mChatLocationLl.setOnClickListener(this);

        mSetModeVoiceBtn.setOnClickListener(this);
        mSetModeKeyboardBtn.setOnClickListener(this);

        // 表情
        mEmojiNormalIv.setOnClickListener(this);
        mEmojiCheckedIv.setOnClickListener(this);

        mAudioRecoderUtils = new AudioRecoderUtils();

        //PopupWindow的布局文件
        final View view = View.inflate(this, R.layout.layout_microphone, null);

        mPop = new PopupWindowFactory(this,view);

        //PopupWindow布局文件里面的控件
        mImageView = (ImageView) view.findViewById(R.id.iv_recording_icon);
        mTextView = (TextView) view.findViewById(R.id.tv_recording_time);
        //录音回调
        mAudioRecoderUtils.setOnAudioStatusUpdateListener(new AudioRecoderUtils.OnAudioStatusUpdateListener() {

            @Override
            public void onUpdate(double db, long time) {
                mImageView.getDrawable().setLevel((int) (3000 + 6000 * db / 100));
                mTextView.setText(TimeUtils.long2String(time));
            }

            //录音结束，filePath为保存路径
            @Override
            public void onStop(String filePath) {
                final String messageId = preSendVoiceMsg(filePath);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<String> imageList = FileUtil.uploadFile(Constant.BASE_URL + "oss/file", filePath);
                        if (null != imageList && imageList.size() > 0) {
                            android.os.Message msg = new android.os.Message();
                            msg.what = REQUEST_CODE_VOICE;
                            Bundle bundle = new Bundle();
                            bundle.putString("video", imageList.get(0));
                            bundle.putString("messageId", messageId);
                            bundle.putString("localPath", filePath);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    }
                }).start();
                mTextView.setText(TimeUtils.long2String(0));
            }
        });
    }

    private void setUpView() {
        mManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        targetType = getIntent().getStringExtra("targetType");
        if (Constant.TARGET_TYPE_SINGLE.equals(targetType)) {
            fromUserId = getIntent().getStringExtra("fromUserId");
            fromUserNickName = getIntent().getStringExtra("fromUserNickName");
            fromUserAvatar = getIntent().getStringExtra("fromUserAvatar");
            mFromNickNameTv.setText(fromUserNickName);
            mMessageList = mMessageDao.getMessageListByUserId(fromUserId);
        } else if (Constant.TARGET_TYPE_GROUP.equals(targetType)) {
            groupId = getIntent().getStringExtra("groupId");
            groupDesc = getIntent().getStringExtra("groupDesc");
            memberNum = getIntent().getStringExtra("memberNum");
            if (TextUtils.isEmpty(groupDesc)) {
                mFromNickNameTv.setText("群聊(" + memberNum + ")");
            } else {
                mFromNickNameTv.setText(groupDesc + "(" + memberNum + ")");
            }

            mMessageList = mMessageDao.getMessageListByGroupId(groupId);
        }
        mMessageAdapter = new MessageAdapter(this, mMessageList);
        mMessageLv.setAdapter(mMessageAdapter);

        mMessageLv.setSelection(mMessageLv.getCount() - 1);
    }

    @Override
    public void onClick(View view) {
        String[] permissions;
        switch (view.getId()) {
            case R.id.btn_send:
                String content = mTextMsgEt.getText().toString();
                sendTextMsg(content);
                break;
            case R.id.iv_setting:
                if (Constant.TARGET_TYPE_SINGLE.equals(targetType)) {
                    // 单聊设置
                    Intent intent = new Intent(ChatActivity.this, ChatSingleSettingActivity.class);
                    intent.putExtra("userId", fromUserId);
                    intent.putExtra("userNickName", fromUserNickName);
                    intent.putExtra("userAvatar", fromUserAvatar);
                    startActivity(intent);
                } else {
                    // 群聊设置
                    Intent intent = new Intent(ChatActivity.this, ChatGroupSettingActivity.class);
                    intent.putExtra("groupId", groupId);
                    startActivity(intent);
                }
                break;
            case R.id.btn_set_mode_voice:
                // 切换成语音
                permissions = new String[]{"android.permission.RECORD_AUDIO"};
                requestPermissions(ChatActivity.this, permissions, REQUEST_CODE_VOICE);
                break;
            case R.id.btn_set_mode_keyboard:
                // 切换成文字
                // 输入框获取焦点
                // 显示软键盘
                mTextMsgEt.setFocusable(true);
                mTextMsgEt.setFocusableInTouchMode(true);
                mTextMsgEt.requestFocus();
                KeyboardUtils.showSoftInput();

                mPressToSpeakLl.setVisibility(View.GONE);
                mTextMsgRl.setVisibility(View.VISIBLE);

                mSetModeKeyboardBtn.setVisibility(View.GONE);
                mSetModeVoiceBtn.setVisibility(View.VISIBLE);
                break;
            case R.id.ll_press_to_speak:
                break;
            case R.id.ll_image_album:
                // 通过相册发送图片
                // 动态申请相册权限
                permissions = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};
                requestPermissions(ChatActivity.this, permissions, REQUEST_CODE_IMAGE_ALBUM);
                break;
            case R.id.ll_image_camera:
                // 通过拍照发送图片
                // 动态申请相机权限
                permissions = new String[]{"android.permission.CAMERA"};
                requestPermissions(ChatActivity.this, permissions, REQUEST_CODE_IMAGE_CAMERA);
                break;
            case R.id.ll_chat_location:
                // 动态申请定位权限
                permissions = new String[]{"android.permission.ACCESS_FINE_LOCATION"};
                requestPermissions(ChatActivity.this, permissions, REQUEST_CODE_LOCATION);
                break;
            case R.id.iv_emoji_normal:
                KeyboardUtils.hideSoftInput(this);
                mMoreLl.setVisibility(View.VISIBLE);

                mEmojiNormalIv.setVisibility(View.GONE);
                mEmojiCheckedIv.setVisibility(View.VISIBLE);
                mEmojiContainerLl.setVisibility(View.VISIBLE);

                mBtnContainerLl.setVisibility(View.GONE);

                // 切换成文字
                mPressToSpeakLl.setVisibility(View.GONE);
                mTextMsgRl.setVisibility(View.VISIBLE);

                mSetModeKeyboardBtn.setVisibility(View.GONE);
                mSetModeVoiceBtn.setVisibility(View.VISIBLE);
                break;

            case R.id.iv_emoji_checked:
                mEmojiNormalIv.setVisibility(View.VISIBLE);
                mEmojiCheckedIv.setVisibility(View.GONE);
                mEmojiContainerLl.setVisibility(View.GONE);

                mBtnContainerLl.setVisibility(View.GONE);
                break;
        }
    }

    public void back(View view) {
        finish();
    }

    /**
     * 显示或隐藏图标按钮页
     *
     * @param view
     */
    public void more(View view) {
        if (mMoreLl.getVisibility() == View.GONE) {
            KeyboardUtils.hideSoftInput(this);
            mMoreLl.setVisibility(View.VISIBLE);
            mBtnContainerLl.setVisibility(View.VISIBLE);
//            mEmojiContainerLl.setVisibility(View.GONE);

            // 切换成文字
            mPressToSpeakLl.setVisibility(View.GONE);
            mTextMsgRl.setVisibility(View.VISIBLE);

            mSetModeKeyboardBtn.setVisibility(View.GONE);
            mSetModeVoiceBtn.setVisibility(View.VISIBLE);
        } else {
            if (mEmojiContainerLl.getVisibility() == View.VISIBLE) {
                mEmojiContainerLl.setVisibility(View.GONE);
                mBtnContainerLl.setVisibility(View.VISIBLE);
                mEmojiNormalIv.setVisibility(View.VISIBLE);
                mEmojiCheckedIv.setVisibility(View.GONE);
            } else {
                mMoreLl.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 点击文字输入框
     *
     * @param v
     */
    public void editClick(View v) {
        mMessageLv.setSelection(mMessageLv.getCount() - 1);
        if (mMoreLl.getVisibility() == View.VISIBLE) {
            mMoreLl.setVisibility(View.GONE);
            mEmojiNormalIv.setVisibility(View.VISIBLE);
            mEmojiCheckedIv.setVisibility(View.INVISIBLE);
        }

    }



    /**
     * 发送文字消息
     *
     * @param content 消息内容
     */
    private void sendTextMsg(String content) {
        Message message = new Message();
        message.setMessageId(CommonUtil.generateId());
        message.setTargetType(targetType);
        message.setContent(content);
        message.setCreateTime(TimeUtil.getTimeStringAutoShort2(new Date().getTime(), true));
        message.setFromUserId(mUser.getUserId());
        message.setToUserId(fromUserId);
        message.setToUserName(fromUserNickName);
        message.setToUserAvatar(fromUserAvatar);
        message.setTimestamp(new Date().getTime());
        message.setStatus(MessageStatus.SENDING.value());

        // 群组
        message.setGroupId(groupId);

        mMessageList.add(message);
        mMessageIndex = mMessageList.size() - 1;
        message.setMessageType(Constant.MSG_TYPE_TEXT);

        Message.save(message);
        Map<String, Object> body = new HashMap<>();
        body.put("extras", new HashMap<>());
        body.put("text", content);
        if (Constant.TARGET_TYPE_SINGLE.equals(targetType)) {
            // 单聊
            sendMessage(targetType, fromUserId, mUser.getUserId(),
                    message.getMessageType(), JSON.toJSONString(body), mMessageIndex);
        } else {
            // 群聊
            sendMessage(targetType, groupId, mUser.getUserId(),
                    message.getMessageType(), JSON.toJSONString(body), mMessageIndex);
        }
        mMessageAdapter.notifyDataSetChanged();
        mMessageLv.setSelection(mMessageLv.getCount() - 1);
        mTextMsgEt.setText("");
    }

    /**
     * 预发送图片消息
     */
    private String preSendImageMsg(String localPath) {
        String messageId = CommonUtil.generateId();
        Message message = new Message();
        message.setMessageId(messageId);
        message.setTargetType(targetType);
        message.setCreateTime(TimeUtil.getTimeStringAutoShort2(new Date().getTime(), true));
        message.setFromUserId(mUser.getUserId());
        message.setToUserId(fromUserId);
        message.setToUserName(fromUserNickName);
        message.setToUserAvatar(fromUserAvatar);
        message.setTimestamp(new Date().getTime());
        message.setStatus(MessageStatus.SENDING.value());

        // 群组
        message.setGroupId(groupId);

        mMessageList.add(message);
        mMessageIndex = mMessageList.size() - 1;
        message.setMessageType(Constant.MSG_TYPE_IMAGE);

        Map<String, Object> body = new HashMap<>();
        body.put("extras", new HashMap<>());
        body.put("localPath", localPath);
        String messageBody = JSON.toJSONString(body);
        message.setMessageBody(messageBody);

        Message.save(message);

        mMessageAdapter.notifyDataSetChanged();
        mMessageLv.setSelection(mMessageLv.getCount() - 1);
        mTextMsgEt.setText("");

        // 隐藏底部功能栏
        mBtnContainerLl.setVisibility(View.GONE);

        return messageId;
    }
    /**
     * 预发送图片消息
     */
    private String preSendVoiceMsg(String localPath) {
        String messageId = CommonUtil.generateId();
        Message message = new Message();
        message.setMessageId(messageId);
        message.setTargetType(targetType);
        message.setCreateTime(TimeUtil.getTimeStringAutoShort2(new Date().getTime(), true));
        message.setFromUserId(mUser.getUserId());
        message.setToUserId(fromUserId);
        message.setToUserName(fromUserNickName);
        message.setToUserAvatar(fromUserAvatar);
        message.setTimestamp(new Date().getTime());
        message.setStatus(MessageStatus.SENDING.value());

        // 群组
        message.setGroupId(groupId);

        mMessageList.add(message);
        mMessageIndex = mMessageList.size() - 1;
        message.setMessageType(Constant.MSG_TYPE_VOICE);

        Map<String, Object> body = new HashMap<>();
        body.put("extras", new HashMap<>());
        body.put("localPath", localPath);
        String messageBody = JSON.toJSONString(body);
        message.setMessageBody(messageBody);

        Message.save(message);

        mMessageAdapter.notifyDataSetChanged();
        mMessageLv.setSelection(mMessageLv.getCount() - 1);
        mTextMsgEt.setText("");

        // 隐藏底部功能栏
        mBtnContainerLl.setVisibility(View.GONE);

        return messageId;
    }
    /**
     * 发送语音消息
     *
     * @param video 消息内容
     */
    private void sendVioceMsg(String video, String messageId, String localPath) {
        Message message = mMessageDao.getMessageByMessageId(messageId);
        Map<String, Object> body = new HashMap<>();
        body.put("type", Constant.MSG_TYPE_VOICE);
        body.put("extras", new HashMap<>());
        body.put("videoUrl", video);
        body.put("localPath", localPath);
        String messageBody = JSON.toJSONString(body);
        message.setMessageBody(messageBody);
        Message.save(message);

        if (Constant.TARGET_TYPE_SINGLE.equals(targetType)) {
            // 单聊
            sendMessage(targetType, fromUserId, mUser.getUserId(),
                    message.getMessageType(), JSON.toJSONString(body), mMessageIndex);
        } else {
            // 群聊
            sendMessage(targetType, groupId, mUser.getUserId(),
                    message.getMessageType(), JSON.toJSONString(body), mMessageIndex);
        }
        mMessageAdapter.notifyDataSetChanged();
        mMessageLv.setSelection(mMessageLv.getCount() - 1);
        mTextMsgEt.setText("");
    }
    /**
     * 发送图片消息
     *
     * @param imgUrl 消息内容
     */
    private void sendImageMsg(String imgUrl, String messageId, String localPath) {
        Message message = mMessageDao.getMessageByMessageId(messageId);
        Map<String, Object> body = new HashMap<>();
        body.put("type", Constant.MSG_TYPE_IMAGE);
        body.put("extras", new HashMap<>());
        body.put("imgUrl", imgUrl);
        body.put("localPath", localPath);
        String messageBody = JSON.toJSONString(body);
        message.setMessageBody(messageBody);
        Message.save(message);

        if (Constant.TARGET_TYPE_SINGLE.equals(targetType)) {
            // 单聊
            sendMessage(targetType, fromUserId, mUser.getUserId(),
                    message.getMessageType(), JSON.toJSONString(body), mMessageIndex);
        } else {
            // 群聊
            sendMessage(targetType, groupId, mUser.getUserId(),
                    message.getMessageType(), JSON.toJSONString(body), mMessageIndex);
        }
        mMessageAdapter.notifyDataSetChanged();
        mMessageLv.setSelection(mMessageLv.getCount() - 1);
        mTextMsgEt.setText("");
    }

    /**
     * 发送位置消息
     *
     * @param latitude      纬度
     * @param longitude     经度
     * @param address       地址
     * @param addressDetail 详细地址
     * @param path          地图截图http地址
     */
    private void sendLocationMsg(double latitude, double longitude, String address, String addressDetail, String path) {
        Message message = new Message();
        message.setMessageId(CommonUtil.generateId());
        message.setTargetType(targetType);
        message.setCreateTime(TimeUtil.getTimeStringAutoShort2(new Date().getTime(), true));
        message.setFromUserId(mUser.getUserId());
        message.setToUserId(fromUserId);
        message.setToUserName(fromUserNickName);
        message.setToUserAvatar(fromUserAvatar);
        message.setTimestamp(new Date().getTime());
        message.setStatus(MessageStatus.SENDING.value());

        // 群组
        message.setGroupId(groupId);

        mMessageList.add(message);
        mMessageIndex = mMessageList.size() - 1;
        message.setMessageType(Constant.MSG_TYPE_LOCATION);
        Map<String, Object> body = new HashMap<>();
        body.put("type", Constant.MSG_TYPE_LOCATION);
        body.put("latitude", latitude);
        body.put("longitude", longitude);
        body.put("address", address);
        body.put("addressDetail", addressDetail);
        body.put("path", path);
        String messageBody = JSON.toJSONString(body);
        message.setMessageBody(messageBody);
        Message.save(message);

        if (Constant.TARGET_TYPE_SINGLE.equals(targetType)) {
            // 单聊
            sendMessage(targetType, fromUserId, mUser.getUserId(),
                    message.getMessageType(), messageBody, mMessageIndex);
        } else {
            // 群聊
            sendMessage(targetType, groupId, mUser.getUserId(),
                    message.getMessageType(), messageBody, mMessageIndex);
        }
        mMessageAdapter.notifyDataSetChanged();
        mMessageLv.setSelection(mMessageLv.getCount() - 1);
        mTextMsgEt.setText("");
    }


    /*接收到的消息*/
    public void onEvent(MessageEvent event) {
        cn.jpush.im.android.api.model.Message newMessage = event.getMessage();//获取此次离线期间会话收到的新消息列表
        Log.d("接收到新消息", "onEvent: " + newMessage.getContentType());
        handleReceivedMessage(event.getMessage());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JMessageClient.unRegisterEventReceiver(this);
    }

    private void sendMessage(String targetType, String targetId, String fromId, String msgType, String body, final int messageIndex) {
        String url = Constant.BASE_URL + "messages";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("targetType", targetType);
        paramMap.put("targetId", targetId);
        paramMap.put("fromId", fromId);
        paramMap.put("msgType", msgType);
        paramMap.put("body", body);

        mVolleyUtil.httpPostRequest(url, paramMap, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Message message = mMessageList.get(messageIndex);
                message = mMessageDao.getMessageByMessageId(message.getMessageId());
                message.setStatus(MessageStatus.SEND_SUCCESS.value());
                mMessageList.set(messageIndex, message);
                Message.save(message);
                mMessageAdapter.setData(mMessageList);
                mMessageAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Message message = mMessageList.get(messageIndex);
                message = mMessageDao.getMessageByMessageId(message.getMessageId());
                message.setStatus(MessageStatus.SEND_FAIL.value());
                mMessageList.set(messageIndex, message);
                Message.save(message);
                mMessageAdapter.setData(mMessageList);
                mMessageAdapter.notifyDataSetChanged();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Constant.TARGET_TYPE_SINGLE.equals(targetType)) {
            mMessageList = mMessageDao.getMessageListByUserId(fromUserId);
        } else if (Constant.TARGET_TYPE_GROUP.equals(targetType)) {
            mMessageList = mMessageDao.getMessageListByGroupId(groupId);
        }

        mMessageAdapter = new MessageAdapter(this, mMessageList);
        mMessageLv.setAdapter(mMessageAdapter);

        mMessageLv.setSelection(mMessageLv.getCount() - 1);
    }

    /**
     * 监听极光收到的消息
     *
     * @param msg
     */
    private void handleReceivedMessage(cn.jpush.im.android.api.model.Message msg) {

        // 自己发送出的消息(接收方非自己)自己也能收到
        // 如果是自己发送的消息 则无需处理此条消息
        // 发送者
        UserInfo fromUserInfo = msg.getFromUser();
        if (fromUserInfo.getUserName().equals(mUser.getUserId())) {
            return;
        }

        Message message = new Message();
        message.setCreateTime(TimeUtil.getTimeStringAutoShort2(new Date().getTime(), true));

        // 消息发送者信息
        message.setFromUserId(fromUserInfo.getUserName());
        message.setFromUserName(fromUserInfo.getNickname());
        message.setFromUserAvatar(fromUserInfo.getAvatar());

        // 群发 or 单发
        if (msg.getTargetType().equals(ConversationType.single)) {
            message.setTargetType(Constant.TARGET_TYPE_SINGLE);

        } else {
            message.setTargetType(Constant.TARGET_TYPE_GROUP);
            GroupInfo groupInfo = (GroupInfo) msg.getTargetInfo();
            message.setGroupId(String.valueOf(groupInfo.getGroupID()));
        }

        // 消息接收者信息
        message.setToUserId(mUser.getUserId());

        // 消息类型
        message.setMessageType(JimUtil.getMessageType(msg));
        message.setTimestamp(new Date().getTime());

        if (Constant.MSG_TYPE_TEXT.equals(message.getMessageType())) {
            // 文字
            TextContent messageContent = (TextContent) msg.getContent();
            message.setContent(messageContent.getText());
        } else if (Constant.MSG_TYPE_IMAGE.equals(message.getMessageType())) {
            // 图片
            Map<String, String> messageMap = JSON.parseObject(msg.getContent().toJson(), Map.class);
            Map<String, Object> messageBodyMap = JSON.parseObject(messageMap.get("text"), Map.class);
            message.setMessageBody(JSON.toJSONString(messageBodyMap));

        } else if (Constant.MSG_TYPE_LOCATION.equals(message.getMessageType())) {
            // 位置
            Map<String, String> messageMap = JSON.parseObject(msg.getContent().toJson(), Map.class);
            Map<String, Object> messageBodyMap = JSON.parseObject(messageMap.get("text"), Map.class);
            message.setMessageBody(JSON.toJSONString(messageBodyMap));
        } else if (Constant.MSG_TYPE_VOICE.equals(message.getMessageType())) {
            // 语音
            Map<String, String> messageMap = JSON.parseObject(msg.getContent().toJson(), Map.class);
            Map<String, Object> messageBodyMap = JSON.parseObject(messageMap.get("text"), Map.class);
            message.setMessageBody(JSON.toJSONString(messageBodyMap));
        }

        Message.save(message);

        if (msg.getTargetType().equals(ConversationType.single)) {
            // 单聊
            // 如果是当前会话
            Conversation conversation = JMessageClient.getSingleConversation(fromUserInfo.getUserName());
            if (fromUserInfo.getUserName().equals(fromUserId)) {
                mMessageList.add(message);
                mMessageLv.post(new Runnable() {
                    @Override
                    public void run() {
                        mMessageAdapter.setData(mMessageList);
                        mMessageAdapter.notifyDataSetChanged();
                        mMessageLv.setSelection(mMessageLv.getBottom());
                    }
                });
                // 清除未读数
                conversation.resetUnreadCount();
            } else {
                // 未读数++
                PreferencesUtil.getInstance().setNewMsgsUnreadNumber(PreferencesUtil.getInstance().getNewMsgsUnreadNumber() + 1);
            }
        } else {
            Conversation conversation = JMessageClient.getGroupConversation(Long.valueOf(groupId));

            // 群聊
            // 如果是当前会话
            GroupInfo groupInfo = (GroupInfo) msg.getTargetInfo();
            if (String.valueOf(groupInfo.getGroupID()).equals(groupId) && !fromUserInfo.getUserName().equals(mUser.getUserId())) {
                mMessageList.add(message);
                mMessageLv.post(new Runnable() {
                    @Override
                    public void run() {
                        mMessageAdapter.setData(mMessageList);
                        mMessageAdapter.notifyDataSetChanged();
                        mMessageLv.setSelection(mMessageLv.getBottom());
                    }
                });
                // 清除未读数
                conversation.resetUnreadCount();
            } else {
                // 未读数++
                PreferencesUtil.getInstance().setNewMsgsUnreadNumber(PreferencesUtil.getInstance().getNewMsgsUnreadNumber() + 1);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case REQUEST_CODE_IMAGE_ALBUM:
                    if (data != null) {
                        Uri uri = data.getData();
                        final String filePath = FileUtil.getFilePathByUri(this, uri);
                        final String messageId = preSendImageMsg(filePath);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                List<String> imageList = FileUtil.uploadFile(Constant.BASE_URL + "oss/file", filePath);
                                if (null != imageList && imageList.size() > 0) {
                                    android.os.Message msg = new android.os.Message();
                                    msg.what = REQUEST_CODE_IMAGE_ALBUM;
                                    Bundle bundle = new Bundle();
                                    bundle.putString("imgUrl", imageList.get(0));
                                    bundle.putString("messageId", messageId);
                                    bundle.putString("localPath", filePath);
                                    msg.setData(bundle);
                                    handler.sendMessage(msg);
                                }
                            }
                        }).start();
                    }
                    break;
                case REQUEST_CODE_IMAGE_CAMERA:
                    final File file = new File(Environment.getExternalStorageDirectory(), mImageName);
                    final String messageId = preSendImageMsg(file.getPath());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<String> imageList = FileUtil.uploadFile(Constant.BASE_URL + "oss/file", file.getPath());
                            if (null != imageList && imageList.size() > 0) {
                                android.os.Message msg = new android.os.Message();
                                msg.what = REQUEST_CODE_IMAGE_ALBUM;
                                Bundle bundle = new Bundle();
                                bundle.putString("imgUrl", imageList.get(0));
                                bundle.putString("messageId", messageId);
                                bundle.putString("localPath", file.getPath());
                                msg.setData(bundle);
                                handler.sendMessage(msg);
                            }
                        }
                    }).start();
                    break;

                case REQUEST_CODE_LOCATION:
                    // 获取经纬度，发送位置消息
                    double latitude = data.getDoubleExtra("latitude", 0);
                    double longitude = data.getDoubleExtra("longitude", 0);
                    String address = data.getStringExtra("address");
                    String addressDetail = data.getStringExtra("addressDetail");
                    String path = data.getStringExtra("path");
                    sendLocationMsg(latitude, longitude, address, addressDetail, path);
                    break;
            }
        }
    }

    /**
     * 动态权限
     */
    public void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        // Android 6.0开始的动态权限，这里进行版本判断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> mPermissionList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(activity, permissions[i])
                        != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (mPermissionList.isEmpty()) {
                // 非初次进入App且已授权
                switch (requestCode) {
                    case REQUEST_CODE_LOCATION:
                        showMapPicker();
                        break;
                    case REQUEST_CODE_IMAGE_ALBUM:
                        showAlbum();
                        break;
                    case REQUEST_CODE_IMAGE_CAMERA:
                        showCamera();
                        break;
                    case REQUEST_CODE_VOICE:
                        StartListener();
                        showAudio();
                        break;
                }
            } else {
                // 请求权限方法
                String[] requestPermissions = mPermissionList.toArray(new String[mPermissionList.size()]);
                // 这个触发下面onRequestPermissionsResult这个回调
                ActivityCompat.requestPermissions(activity, requestPermissions, requestCode);
            }
        }
    }

    /**
     * requestPermissions的回调
     * 一个或多个权限请求结果回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasAllGranted = true;
        // 判断是否拒绝  拒绝后要怎么处理 以及取消再次提示的处理
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                hasAllGranted = false;
                break;
            }
        }
        if (hasAllGranted) {
            switch (requestCode) {
                case REQUEST_CODE_LOCATION:
                    showMapPicker();
                    break;
                case REQUEST_CODE_IMAGE_ALBUM:
                    showAlbum();
                    break;
                case REQUEST_CODE_IMAGE_CAMERA:
                    showCamera();
                    break;
                case REQUEST_CODE_VOICE:
                    if ((grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED) ) {
                        StartListener();
                    } else {
                        Toast.makeText(this, "已拒绝权限！", Toast.LENGTH_SHORT).show();
                    }
                    showAudio();
                    break;
            }
        } else {
            // 拒绝授权做的处理，弹出弹框提示用户授权
            handleRejectPermission(ChatActivity.this, permissions[0], requestCode);
        }
    }

    public void handleRejectPermission(final Activity context, String permission, int requestCode) {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
            String content = "";
            // 非初次进入App且已授权
            switch (requestCode) {
                case REQUEST_CODE_LOCATION:
                    content = getString(R.string.request_permission_location);
                    break;
                case REQUEST_CODE_IMAGE_ALBUM:
                    content = getString(R.string.request_permission_storage);
                    break;
                case REQUEST_CODE_IMAGE_CAMERA:
                    content = getString(R.string.request_permission_camera);
                    break;
                case REQUEST_CODE_VOICE:
                    content = getString(R.string.request_permission_record_audio);
                    break;
            }
            final ConfirmDialog mConfirmDialog = new ConfirmDialog(ChatActivity.this, getString(R.string.request_permission),
                    content,
                    getString(R.string.go_setting), getString(R.string.cancel), getColor(R.color.navy_blue));
            mConfirmDialog.setOnDialogClickListener(new ConfirmDialog.OnDialogClickListener() {
                @Override
                public void onOkClick() {
                    mConfirmDialog.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getApplicationContext().getPackageName(), null);
                    intent.setData(uri);
                    context.startActivity(intent);
                }

                @Override
                public void onCancelClick() {
                    mConfirmDialog.dismiss();
                }
            });
            // 点击空白处消失
            mConfirmDialog.setCancelable(false);
            mConfirmDialog.show();
        }
    }

    /**
     * 进入地图选择页面
     */
    private void showMapPicker() {
        Intent intent = new Intent(ChatActivity.this, MapPickerActivity.class);
        intent.putExtra("sendLocation", true);
        intent.putExtra("locationType", Constant.LOCATION_TYPE_MSG);
        startActivityForResult(intent, REQUEST_CODE_LOCATION);
    }

    /**
     * 跳转到相机
     */
    private void showCamera() {
        mImageName = CommonUtil.generateId() + ".png";
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(
                new File(Environment.getExternalStorageDirectory(), mImageName)));
        startActivityForResult(cameraIntent, REQUEST_CODE_IMAGE_CAMERA);
    }

    /**
     * 跳转到相册
     */
    private void showAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_CODE_IMAGE_ALBUM);
    }

    /**
     * 进入录音模式
     */
    private void showAudio() {
        // 切换成语音
        KeyboardUtils.hideSoftInput(this);
        // 隐藏消息类型容器
        mBtnContainerLl.setVisibility(View.GONE);

        // 隐藏表情
        mEmojiCheckedIv.setVisibility(View.GONE);
        mEmojiNormalIv.setVisibility(View.VISIBLE);
        mEmojiContainerLl.setVisibility(View.GONE);

        // 显示"按住说话"
        mPressToSpeakLl.setVisibility(View.VISIBLE);
        // 隐藏文本输入框
        mTextMsgRl.setVisibility(View.GONE);

        mSetModeVoiceBtn.setVisibility(View.GONE);
        mSetModeKeyboardBtn.setVisibility(View.VISIBLE);
    }

    /**
     * android 7.0系统解决拍照的问题
     */
    private void initCamera() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }

    /**
     * 初始化emoji列表
     *
     * @param emojiNum emoji数量
     */
    private void initEmojiList(int emojiNum) {
        mEmojiList = new ArrayList<>();
        for (int i = 1; i <= emojiNum; i++) {
            if (i < 10) {
                mEmojiList.add("emoji_0" + i);
            } else {
                mEmojiList.add("emoji_" + i);
            }

        }
    }

    private View getGridChildView(int i) {
        View view = View.inflate(this, R.layout.gridview_emoji, null);
        ExpandGridView expandGridView = view.findViewById(R.id.egv_emoji);
        List<String> emojiList = new ArrayList<>();
        if (i == 1) {
            emojiList.addAll(mEmojiList.subList(0, 21));
        } else {
            emojiList.addAll(mEmojiList.subList(21, mEmojiList.size()));
        }
        emojiList.addAll(mEmojiList);
        emojiList.add("delete_emoji");
        final EmojiAdapter emojiAdapter = new EmojiAdapter(this, 1, emojiList);
        expandGridView.setAdapter(emojiAdapter);
        expandGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String emoji = emojiAdapter.getItem(position);
                try {
                    // 文字输入框可见时，才可输入表情
                    // 按住说话可见，不让输入表情
                    if (mSetModeKeyboardBtn.getVisibility() != View.VISIBLE) {
                        if (emoji != "delete_emoji") {
                            // 不是删除键，显示表情
                            Class clz = Class.forName("com.wqs.wechat.utils.EmojiUtil");
                            Field field = clz.getField(emoji);
                            mTextMsgEt.append(EmojiUtil.getEmojisText(
                                    ChatActivity.this, (String) field.get(null)));
                        } else {
                            // 删除文字或者表情
                            if (!TextUtils.isEmpty(mTextMsgEt.getText())) {
                                int selectionStart = mTextMsgEt.getSelectionStart();// 获取光标的位置
                                if (selectionStart > 0) {
                                    String body = mTextMsgEt.getText().toString();
                                    String tempStr = body.substring(0, selectionStart);
                                    int i = tempStr.lastIndexOf("[");// 获取最后一个表情的位置
                                    if (i != -1) {
                                        CharSequence cs = tempStr.substring(i, selectionStart);
                                        if (EmojiUtil.containsKey(cs.toString()))
                                            mTextMsgEt.getEditableText().delete(i, selectionStart);
                                        else
                                            mTextMsgEt.getEditableText().delete(selectionStart - 1, selectionStart);
                                    } else {
                                        mTextMsgEt.getEditableText().delete(selectionStart - 1, selectionStart);
                                    }
                                }
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void StartListener(){
        //Button的touch监听
        mPressToSpeakLl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        mPop.showAtLocation(rl, Gravity.CENTER, 0, 0);
                        mAudioRecoderUtils.startRecord();
                        break;
                    case MotionEvent.ACTION_UP:
                        mAudioRecoderUtils.stopRecord();
                        mPop.dismiss();
                        break;
                }
                return true;
            }
        });
    }
    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            String messageId,localPath;
            switch (msg.what) {
                case REQUEST_CODE_IMAGE_ALBUM:
                    String imgUrl = msg.getData().getString("imgUrl");
                    messageId = msg.getData().getString("messageId");
                    localPath = msg.getData().getString("localPath");
                    sendImageMsg(imgUrl, messageId, localPath);
                    break;
                case REQUEST_CODE_VOICE:
                    String videoUrl = msg.getData().getString("video");
                    messageId = msg.getData().getString("messageId");
                    localPath = msg.getData().getString("localPath");
                    sendVioceMsg(videoUrl, messageId, localPath);
                    break;
            }
        }
    };
}
