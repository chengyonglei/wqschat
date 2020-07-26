package com.wqs.wechat.net;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.request.RequestCall;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wqs.wechat.cons.Constant.BASE_URL;

public class FriendCircleManage {

    public static RequestCall sendFriendCircle(String userid, String content, List<String> photos){
        Map<String,String> params = new HashMap<>();
        params.put("content",content);
        params.put("photos",photos.toString());
        return OkHttpUtils.post().url(BASE_URL+"friendsCircle/"+userid).params(params).build();
    }
}
