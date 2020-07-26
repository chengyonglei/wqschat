package com.wqs.wechat.utils;

import com.alibaba.fastjson.JSON;
import com.wqs.wechat.cons.Constant;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.im.android.api.model.Conversation;

public class JimUtil {
    final static String DEFAULT_LATEST_MESSAGE = "";

    public static String getLatestMessage(Conversation conversation) {
        Map<String, String> latestMessageMap;
        // 如果消息被清除
        // conversation.getLastestMessage() == null
        if (null == conversation.getLatestMessage()) {
            return DEFAULT_LATEST_MESSAGE;
        } else {
            String messageType = conversation.getLatestMessage().getContentType().name();
            try {
                latestMessageMap = JSON.parseObject(conversation.getLatestMessage().getContent().toJson(), Map.class);
            } catch (Exception e) {
                latestMessageMap = new HashMap<>();
            }
            if (latestMessageMap.size() == 0) {
                return DEFAULT_LATEST_MESSAGE;
            }

            if (Constant.MSG_TYPE_TEXT.equals(messageType)) {
                return latestMessageMap.get("text");
            } else if (Constant.MSG_TYPE_IMAGE.equals(messageType)) {
                return "[图片]";
            } else if (Constant.MSG_TYPE_CUSTOM.equals(messageType)) {
                try {
                    Map<String, Object> messageBodyMap = JSON.parseObject(latestMessageMap.get("text"), Map.class);
                    String type = String.valueOf(messageBodyMap.get("type"));
                    if (Constant.MSG_TYPE_LOCATION.equals(type)) {
                        return "[位置]";
                    } else if (Constant.MSG_TYPE_IMAGE.equals(type)) {
                        return "[图片]";
                    } else {
                        return DEFAULT_LATEST_MESSAGE;
                    }
                } catch (Exception e) {
                    return DEFAULT_LATEST_MESSAGE;
                }
            } else {
                return DEFAULT_LATEST_MESSAGE;
            }
        }
    }

    public static String getMessageType(cn.jpush.im.android.api.model.Message msg) {
        String messageType = msg.getContentType().name();
        if (Constant.MSG_TYPE_CUSTOM.equals(messageType)) {
            Map<String, String> messageMap = JSON.parseObject(msg.getContent().toJson(), Map.class);
            Map<String, Object> messageBodyMap = JSON.parseObject(messageMap.get("text"), Map.class);
            String type = String.valueOf(messageBodyMap.get("type"));
            return type;
        } else {
            return messageType;
        }
    }
}
