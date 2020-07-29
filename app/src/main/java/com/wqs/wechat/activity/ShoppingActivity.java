package com.wqs.wechat.activity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.qmuiteam.qmui.widget.webview.QMUIWebView;
import com.wqs.wechat.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShoppingActivity extends BaseActivity {

    @BindView(R.id.wb_shopping)
    QMUIWebView wbShopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);
        ButterKnife.bind(this);
        String url="http://www.hnhitao.com/";
        wbShopping.loadUrl(url);
    }
}