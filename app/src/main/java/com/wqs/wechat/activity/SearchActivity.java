package com.wqs.wechat.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.alibaba.fastjson.JSONArray;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.wqs.wechat.R;
import com.wqs.wechat.adapter.SearchNewsAdapter;
import com.wqs.wechat.cons.Constant;
import com.wqs.wechat.entity.SearchHistory;
import com.wqs.wechat.utils.VolleyUtil;

import java.util.List;

/**
 * 搜一搜
 *
 * @author zhou
 */
public class SearchActivity extends BaseActivity {

    private EditText mSearchEt;
    private ListView mSearchNewsLv;
    private SearchNewsAdapter mSearchNewsAdapter;
    private VolleyUtil mVolleyUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initStatusBar();

        mVolleyUtil = VolleyUtil.getInstance(this);

        initView();
    }

    private void initView() {
        mSearchEt = findViewById(R.id.et_search);
        mSearchNewsLv = findViewById(R.id.lv_search_news);

        getHotSearchHistoryList();

        mSearchEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SearchActivity.this, SearchContentActivity.class));
            }
        });
    }

    public void back(View view) {
        finish();
    }


    /**
     * 获取搜索热词
     */
    private void getHotSearchHistoryList() {
        String url = Constant.BASE_URL + "searchHistory/hot";

        mVolleyUtil.httpGetRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                final List<SearchHistory> searchHistoryList = JSONArray.parseArray(response, SearchHistory.class);
                mSearchNewsAdapter = new SearchNewsAdapter(SearchActivity.this, searchHistoryList);
                mSearchNewsLv.setAdapter(mSearchNewsAdapter);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        });
    }
}
