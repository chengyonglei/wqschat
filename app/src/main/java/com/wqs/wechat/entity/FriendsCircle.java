package com.wqs.wechat.entity;

import com.orm.SugarRecord;

import java.util.List;

public class FriendsCircle extends SugarRecord {


    /**
     * momentsContent : 层经济管理吉里吉里里
     * momentsId : cf9f8ea7807b411785445fcef8d73a55
     * createTime : 2020-07-27 15:46:56
     * likeUserList : []
     * userAvatar : https://erp-wd-com.oss-cn-hangzhou.aliyuncs.com/1095fe8064ae423db093c98af6a30283.jpg
     * friendsCircleCommentList : []
     * userNickName : 程永磊
     * momentsPhotos : ["https://imwechat.oss-cn-beijing.aliyuncs.com/2020/7/27/friendcircle2020072751081.jpg","https://imwechat.oss-cn-beijing.aliyuncs.com/2020/7/27/friendcircle2020072751116.jpg"]
     * userId : c5baddef5a7642b7a8d849d10482380f
     * timestamp : 1595836016512
     */
    private String momentsContent;
    private String momentsId;
    private String createTime;
    private List<LikeUserList> likeUserList;
    private String userAvatar;
    private List<FriendsCircleComment> friendsCircleCommentList;
    private String userNickName;
    private String momentsPhotos;
    private String userId;
    private long timestamp;

    public void setMomentsContent(String momentsContent) {
        this.momentsContent = momentsContent;
    }

    public void setMomentsId(String momentsId) {
        this.momentsId = momentsId;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public void setLikeUserList(List<LikeUserList> likeUserList) {
        this.likeUserList = likeUserList;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public void setFriendsCircleCommentList(List<FriendsCircleComment> friendsCircleCommentList) {
        this.friendsCircleCommentList = friendsCircleCommentList;
    }

    public void setUserNickName(String userNickName) {
        this.userNickName = userNickName;
    }

    public void setMomentsPhotos(String momentsPhotos) {
        this.momentsPhotos = momentsPhotos;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMomentsContent() {
        return momentsContent;
    }

    public String getMomentsId() {
        return momentsId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public List<LikeUserList> getLikeUserList() {
        return likeUserList;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public List<FriendsCircleComment> getFriendsCircleCommentList() {
        return friendsCircleCommentList;
    }

    public String getUserNickName() {
        return userNickName;
    }

    public String getMomentsPhotos() {
        return momentsPhotos;
    }

    public String getUserId() {
        return userId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
