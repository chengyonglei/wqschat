package com.wqs.wechat.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.webview.QMUIWebView;
import com.wqs.wechat.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShoppingActivity extends BaseActivity {

    @BindView(R.id.wb_shopping)
    QMUIWebView wbShopping;
    @BindView(R.id.topbar)
    QMUITopBar topbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);
        ButterKnife.bind(this);
        initView();
    }
    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {

        topbar.setTitleGravity(Gravity.LEFT);
        String url = "http://www.hnhitao.com/";
        wbShopping.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                topbar.setTitle(title);
            }
        });
        wbShopping.getSettings().setJavaScriptEnabled(true);
        wbShopping.loadUrl(url);
    }
}