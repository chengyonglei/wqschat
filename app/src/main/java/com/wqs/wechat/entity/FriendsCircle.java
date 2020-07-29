package com.wqs.wechat.entity;

import com.orm.SugarRecord;

import java.util.List;

public class FriendsCircle extends SugarRecord {

    /**
     * momentsContent :
     * momentsId : ce041bd089e74abe994df0dea4a6b2c6
     * createTime : 2020-07-27 16:48:37
     * likeUserList : [{"userPassword":null,"userFriendDesc":null,"userPhone":null,"userAvatar":"https://erp-wd-com.oss-cn-hangzhou.aliyuncs.com/1bfd92dc8c3548589cdfb5562841f1d9.jpg","userLastestCirclePhotos":null,"userQrCode":null,"userImPassword":null,"isOwner":null,"userFriendRemark":null,"userWxId":null,"userEmail":null,"userQqId":null,"isFriend":null,"userNickName":"程永磊","friendList":null,"userSex":null,"userFriendPhone":null,"userIsEmailLinked":null,"userWxIdModifyFlag":null,"userId":"b5b149d4423c4e0eb7fc0c74acaf3fb7","userIsQqLinked":null,"isStarFriend":null,"userSign":null,"userQqPassword":null,"userHeader":null,"friendSource":null}]
     * userAvatar : https://erp-wd-com.oss-cn-hangzhou.aliyuncs.com/1bfd92dc8c3548589cdfb5562841f1d9.jpg
     * userNickName : 程永磊
     * momentsCommentList : []
     * momentsPhotos : ["https://imwechat.oss-cn-beijing.aliyuncs.com/2020/7/27/friendcircle2020072735750.jpg","https://imwechat.oss-cn-beijing.aliyuncs.com/2020/7/27/friendcircle2020072735763.jpg"]
     * userId : b5b149d4423c4e0eb7fc0c74acaf3fb7
     * timestamp : 1595839717873
     */
    private String momentsContent;
    private String momentsId;
    private String createTime;
    private List<LikeUserList> likeUserList;
    private String userAvatar;
    private String userNickName;
    private List<FriendsCircleComment> momentsCommentList;
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

    public void setUserNickName(String userNickName) {
        this.userNickName = userNickName;
    }

    public void setMomentsCommentList(List<FriendsCircleComment> momentsCommentList) {
        this.momentsCommentList = momentsCommentList;
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

    public String getUserNickName() {
        return userNickName;
    }

    public List<FriendsCircleComment> getMomentsCommentList() {
        return momentsCommentList;
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
