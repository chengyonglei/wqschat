package com.wqs.wechat.dao;

import com.alibaba.fastjson.JSON;
import com.wqs.wechat.cons.Constant;
import com.wqs.wechat.entity.Message;
import com.wqs.wechat.utils.JimUtil;
import com.wqs.wechat.utils.TimeUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * 消息
 *
 * @author zhou
 */
public class MessageDao {
    public Message getMessageByMessageId(String messageId) {
        List<Message> messageList = Message.find(Message.class, "message_id = ?", messageId);
        if (null != messageList && messageList.size() > 0) {
            return messageList.get(0);
        }
        return null;
    }

    /**
     * 根据用户ID获取消息列表
     *
     * @param userId 用户ID
     * @return 消息列表
     */
    public List<Message> getMessageListByUserId(String userId) {
        return Message.findWithQuery(Message.class,
                "select * from message where (from_user_id = ? or to_user_id = ?) and target_type = ? order by timestamp asc",
                userId, userId, Constant.TARGET_TYPE_SINGLE);
    }

    /**
     * 根据群组ID获取消息列表
     *
     * @param groupId 群组ID
     * @return 消息列表
     */
    public List<Message> getMessageListByGroupId(String groupId) {
        return Message.findWithQuery(Message.class, "select * from message where group_id = ? order by timestamp asc", groupId);
    }

    /**
     * 根据群组ID删除消息
     * 使用场景: 群会话中清空聊天记录
     *
     * @param groupId 群组ID
     */
    public void deleteMessageByGroupId(String groupId) {
        String sql = "delete from message where group_id = ?";
        Message.executeQuery(sql, groupId);
    }

    /**
     * 根据用户ID删除消息
     *
     * @param userId 用户ID
     */
    public void deleteMessageByUserId(String userId) {
        String sql = "delete from message where (from_user_id = ? or to_user_id = ?) and target_type = ?";
        Message.executeQuery(sql, userId, userId, Constant.TARGET_TYPE_SINGLE);
    }

    public long getMessageCountByGroupId(String groupId) {
        long count = Message.count(Message.class, "group_id = ?", new String[]{groupId});
        return count;
    }

    public void saveMessageByImMessage(cn.jpush.im.android.api.model.Message imMessage, String userId) {
        UserInfo fromUserInfo = imMessage.getFromUser();
        if (fromUserInfo.getUserName().equals(userId)) {
            return;
        }
        Message message = new Message();
        message.setCreateTime(TimeUtil.getTimeStringAutoShort2(new Date().getTime(), true));

        // 消息发送者信息
        message.setFromUserId(fromUserInfo.getUserName());
        message.setFromUserName(fromUserInfo.getNickname());
        message.setFromUserAvatar(fromUserInfo.getAvatar());

        // 群发 or 单发
        if (imMessage.getTargetType().equals(ConversationType.single)) {
            message.setTargetType(Constant.TARGET_TYPE_SINGLE);

        } else {
            message.setTargetType(Constant.TARGET_TYPE_GROUP);
            GroupInfo groupInfo = (GroupInfo) imMessage.getTargetInfo();
            message.setGroupId(String.valueOf(groupInfo.getGroupID()));
        }

        // 消息接收者信息
        message.setToUserId(userId);

        // 消息类型
        message.setMessageType(JimUtil.getMessageType(imMessage));
        message.setTimestamp(new Date().getTime());

        if (Constant.MSG_TYPE_TEXT.equals(message.getMessageType())) {
            // 文字
            TextContent messageContent = (TextContent) imMessage.getContent();
            message.setContent(messageContent.getText());
        } else if (Constant.MSG_TYPE_IMAGE.equals(message.getMessageType())) {
            // 图片
            ImageContent imageContent = ((ImageContent) imMessage.getContent());
            String imageUrl = imageContent.getLocalThumbnailPath();
            message.setImageUrl(imageUrl);
        } else if (Constant.MSG_TYPE_LOCATION.equals(message.getMessageType())) {
            // 位置
            Map<String, String> messageMap = JSON.parseObject(imMessage.getContent().toJson(), Map.class);
            Map<String, Object> messageBodyMap = JSON.parseObject(messageMap.get("text"), Map.class);
            message.setMessageBody(JSON.toJSONString(messageBodyMap));
        }
        Message.save(message);
    }
}
