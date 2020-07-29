package com.wqs.wechat.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.android.volley.NetworkError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.lwkandroid.imagepicker.ImagePicker;
import com.lwkandroid.imagepicker.data.ImageBean;
import com.lwkandroid.imagepicker.data.ImagePickType;
import com.qmuiteam.qmui.layout.QMUIFrameLayout;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.simple.SimpleMultiListener;
import com.scwang.smart.refresh.layout.util.SmartUtil;
import com.wqs.wechat.R;
import com.wqs.wechat.adapter.FriendsCircleAdapter;
import com.wqs.wechat.adapter.FriendsCircleCommentAdapter;
import com.wqs.wechat.cons.Constant;
import com.wqs.wechat.dao.ContactsDao;
import com.wqs.wechat.dao.FriendsCircleDao;
import com.wqs.wechat.entity.FriendsCircle;
import com.wqs.wechat.entity.FriendsCircleComment;
import com.wqs.wechat.entity.User;
import com.wqs.wechat.entity.UserFriendsCircle;
import com.wqs.wechat.utils.PreferencesUtil;
import com.wqs.wechat.utils.VolleyUtil;
import com.wqs.wechat.widget.ImagePickerLoader;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FriendsCircleActivity extends BaseActivity {
    private static final String TAG = "FriendsCircleActivity";
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.v_cover)
    LinearLayout cover;
    @BindView(R.id.iv_back)
    ImageView back;
    @BindView(R.id.parallax)
    ImageView parallax;
    @BindView(R.id.scrollView)
    NestedScrollView scrollView;
    @BindView(R.id.rv_list)
    RecyclerView rvList;
    @BindView(R.id.qtb_topbar)
    Toolbar topbar;
    @BindView(R.id.iv_camera)
    ImageView ivCamera;
    @BindView(R.id.tv_nickname)
    TextView tvNickname;
    @BindView(R.id.sdv_avatar)
    QMUIRadiusImageView sdvAvatar;
    @BindView(R.id.fl_root)
    FrameLayout flRoot;
    @BindView(R.id.et_text_msg)
    EditText etTextMsg;
    @BindView(R.id.btn_send)
    QMUIRoundButton btnSend;
    @BindView(R.id.qfl_tool)
    QMUIFrameLayout qflTool;
    private int mOffset = 0;
    private int mScrollY = 0;
    int time = 0;
    private User mUser;
    FriendsCircleAdapter mAdapter;
    LinearLayoutManager linearLayoutManager;
    private final int REQUEST_CODE_PICKER = 100;
    List<FriendsCircle> friendsCircles;
    VolleyUtil mVolleyUtil;
    List<FriendsCircleComment> comments;
    String momentsID;
    String endTimestamp = String.valueOf(Calendar.getInstance().getTimeInMillis());
    //弹窗
    private PopupWindow mPopupWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendcircle);
        ButterKnife.bind(this);
        mVolleyUtil = VolleyUtil.getInstance(this);
        PreferencesUtil.getInstance().init(this);
        mUser = PreferencesUtil.getInstance().getUser();
        QMUIStatusBarHelper.translucent(this);
        QMUIStatusBarHelper.setStatusBarLightMode(this);
        initView();
        loadData(5, endTimestamp, mUser.getUserId());
    }
    private void initView() {
        //处理EditView在键盘上面的问题
        View decorView = getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(getGlobalLayoutListener(decorView, qflTool));
        //判断输入内容来确定按钮是否能够使用
        etTextMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    btnSend.setEnabled(true);
                    btnSend.setBackgroundColor(getResources().getColor(R.color.wechat_btn_green));
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        Glide.with(sdvAvatar).load(mUser.getUserAvatar()).into(sdvAvatar);
        Glide.with(parallax).load(getDrawable(R.drawable.image_weibo_home_1)).into(sdvAvatar);
        tvNickname.setText(mUser.getUserNickName());
        //下拉刷新
        refreshLayout.setOnMultiListener(new SimpleMultiListener() {
            @Override
            public void onHeaderMoving(RefreshHeader header, boolean isDragging, float percent, int offset, int headerHeight, int maxDragHeight) {
                super.onHeaderMoving(header, isDragging, percent, offset, headerHeight, maxDragHeight);
                mOffset = offset / 1;
                parallax.setTranslationY(mOffset - mScrollY);
                cover.setTranslationX(mOffset-mScrollY);
                topbar.setAlpha(1 - Math.min(percent, 1));
            }
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                endTimestamp = String.valueOf(Calendar.getInstance().getTimeInMillis());
                loadData(10,endTimestamp,mUser.getUserId());
            }
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                loadMoreData(5, endTimestamp, mUser.getUserId());
            }
        });
        //检测页面滚动
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            private int lastScrollY = 0;
            private int h = SmartUtil.dp2px(300);
            private int color = ContextCompat.getColor(getApplicationContext(), R.color.design_default_color_on_secondary) & 0x00ffffff;
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (lastScrollY < h) {
                    scrollY = Math.min(h, scrollY);
                    mScrollY = scrollY > h ? h : scrollY;
                    parallax.setTranslationY(mOffset - mScrollY);
                    cover.setTranslationX(mOffset-mScrollY);
                    QMUIStatusBarHelper.translucent(FriendsCircleActivity.this);
                    topbar.setBackgroundColor(getColor(R.color.gray_normal));
                }
                if (scrollY < 300) {
                    topbar.setBackgroundColor(0);
                }
                lastScrollY = scrollY;
            }
        });

    }
    private void loadData(int limit, String timestamp, String userId) {
        String url = Constant.BASE_URL + "moments?userId=" + userId + "&timestamp=" + timestamp + "&pageSize=" + limit;
        mVolleyUtil.httpGetRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                refreshLayout.finishRefresh();
                friendsCircles = JSON.parseArray(s, FriendsCircle.class);
                if (friendsCircles.size() == 0) {
                    ToastUtils.showShort("没有数据哦");
                } else {
                    mAdapter = new FriendsCircleAdapter(friendsCircles, FriendsCircleActivity.this);
                    linearLayoutManager = new LinearLayoutManager(FriendsCircleActivity.this);
                    endTimestamp = String.valueOf(friendsCircles.get(friendsCircles.size() - 1).getTimestamp());
                    rvList.setLayoutManager(linearLayoutManager);
                    rvList.getLayoutManager().setAutoMeasureEnabled(false);
                    rvList.setAdapter(mAdapter);
                    mAdapter.setmItemOnClickListener(new FriendsCircleAdapter.ItemOnClickListener() {
                        @Override
                        public void itemOnClickListener(List<FriendsCircleComment> commentlist,String momentsId) {
                            comments = commentlist;
                            momentsID = momentsId;
                        }
                    });
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (volleyError instanceof NetworkError) {
                    Toast.makeText(FriendsCircleActivity.this, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                } else if (volleyError instanceof TimeoutError) {
                    Toast.makeText(FriendsCircleActivity.this, R.string.network_time_out, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
    private void loadMoreData(int limit, String timestamp, String userId) {
        String url = Constant.BASE_URL + "moments?userId=" + userId + "&timestamp=" + timestamp + "&pageSize=" + limit;
        mVolleyUtil.httpGetRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                refreshLayout.finishLoadMore();
                refreshLayout.finishRefresh();
                List<FriendsCircle> newDatas = JSON.parseArray(s, FriendsCircle.class);
                if (friendsCircles.size() == 0) {
                    ToastUtils.showShort("没有更多数据了");
                } else {
                    for (int i = 0; i < newDatas.size(); i++) {
                        friendsCircles.add(newDatas.get(i));
                    }
                    endTimestamp = String.valueOf(friendsCircles.get(friendsCircles.size() - 1).getTimestamp());
                    mAdapter.notifyDataSetChanged();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (volleyError instanceof NetworkError) {
                    Toast.makeText(FriendsCircleActivity.this, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                } else if (volleyError instanceof TimeoutError) {
                    Toast.makeText(FriendsCircleActivity.this, R.string.network_time_out, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
    private void addComment(String content,String userId){
        String url = Constant.BASE_URL + "moments/" + momentsID + "/comment";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("content", content);
        paramMap.put("userId", String.valueOf(userId));
        mVolleyUtil.httpPostRequest(url, paramMap,new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (s!=null){
                    FriendsCircleComment comment = JSON.parseObject(s,FriendsCircleComment.class);
                    comment.setCommentUserNickName(mUser.getUserNickName());
                    qflTool.setVisibility(View.GONE);
                    etTextMsg.setText(null);
                    btnSend.setEnabled(false);
                    comments.add(comment);
                    FriendsCircleCommentAdapter fccAdapter = new FriendsCircleCommentAdapter(comments,FriendsCircleActivity.this);
                    fccAdapter.setHasStableIds(false);
                    fccAdapter.notifyDataSetChanged();

                    KeyboardUtils.hideSoftInput(FriendsCircleActivity.this);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (volleyError instanceof NetworkError) {
                    Toast.makeText(FriendsCircleActivity.this, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                } else if (volleyError instanceof TimeoutError) {
                    Toast.makeText(FriendsCircleActivity.this, R.string.network_time_out, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
    @OnClick({R.id.sdv_avatar, R.id.iv_camera, R.id.btn_send,R.id.v_cover,R.id.iv_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sdv_avatar:
                ToastUtils.showShort("点击了头像");
                break;
            case R.id.iv_camera:
                showPop(view);
                break;
            case R.id.v_cover:
                new ImagePicker()
                        .cachePath(Environment.getExternalStorageDirectory().getAbsolutePath())
                        .pickType(ImagePickType.MULTI)
                        .displayer(new ImagePickerLoader())
                        .maxNum(1)
                        .start(FriendsCircleActivity.this, REQUEST_CODE_PICKER);
                break;
            case R.id.btn_send:
                addComment(etTextMsg.getText().toString(),mUser.getUserId());
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        endTimestamp = String.valueOf(Calendar.getInstance().getTimeInMillis());
        loadData(10,endTimestamp,mUser.getUserId());
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            List<ImageBean> list = data.getParcelableArrayListExtra(ImagePicker.INTENT_RESULT_DATA);
            for (ImageBean imageBean : list) {
                Glide.with(parallax).load(imageBean.getImagePath()).into(parallax);
            }
        }
    }

    private void hidePop(View view) {
        mPopupWindow.dismiss();
    }

    //点击相机弹出底部选择框
    private void showPop(View view) {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.popup_window_add_friends_circle, null);
        // 给popwindow加上动画效果
        LinearLayout mPopRootLl = view.findViewById(R.id.ll_pop_root);
        view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
        mPopRootLl.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_bottom_in));
        // 设置popwindow的宽高
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mPopupWindow = new PopupWindow(view, dm.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT);

        // 使其聚集
        mPopupWindow.setFocusable(true);
        // 设置允许在外点击消失
        mPopupWindow.setOutsideTouchable(true);

        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        backgroundAlpha(0.5f);  //透明度

        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
            }
        });
        // 弹出的位置
        mPopupWindow.showAtLocation(flRoot, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

        // 取消
        RelativeLayout mCancelRl = view.findViewById(R.id.rl_cancel);
        RelativeLayout mAddByAlbum = view.findViewById(R.id.rl_add_by_album);
        mCancelRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopupWindow.dismiss();
            }
        });
        mAddByAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendsCircleActivity.this, FriendsCircleSendActivity.class);
                startActivity(intent);
                hidePop(v);
            }
        });
    }

    /**
     * 设置添加屏幕的背景透明度
     * 1.0完全不透明，0.0f完全透明
     *
     * @param bgAlpha 透明度值
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        // 0.0-1.0
        lp.alpha = bgAlpha;
        getWindow().setAttributes(lp);
    }

    //处理键盘覆盖EDITTEXT
    private ViewTreeObserver.OnGlobalLayoutListener getGlobalLayoutListener(final View decorView, final View contentView) {
        return new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                decorView.getWindowVisibleDisplayFrame(r);

                int height = decorView.getContext().getResources().getDisplayMetrics().heightPixels;
                int diff = height - r.bottom + 80;

                if (diff != 0) {
                    if (contentView.getPaddingBottom() != diff) {
                        contentView.setPadding(0, 0, 0, diff);
                    }
                } else {
                    if (contentView.getPaddingBottom() != 0) {
                        contentView.setPadding(0, 0, 0, 0);
                    }
                }
            }
        };
    }

}