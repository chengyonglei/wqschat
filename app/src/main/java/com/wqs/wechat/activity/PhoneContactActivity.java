package com.wqs.wechat.activity;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.wqs.wechat.R;
import com.wqs.wechat.adapter.PhoneContactAdapter;
import com.wqs.wechat.cons.Constant;
import com.wqs.wechat.entity.PhoneContact;
import com.wqs.wechat.entity.User;
import com.wqs.wechat.utils.CommonUtil;
import com.wqs.wechat.utils.PinyinComparator;
import com.wqs.wechat.utils.PreferencesUtil;
import com.wqs.wechat.utils.VolleyUtil;
import com.wqs.wechat.widget.LoadingDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 手机通讯录
 *
 * @author zhou
 */
public class PhoneContactActivity extends BaseActivity {
    private PhoneContactAdapter mPhoneContactAdapter;
    private ListView mPhoneContactLv;

    private VolleyUtil mVolleyUtil;
    private User mUser;
    private LoadingDialog mDialog;

    private Map<String, String> mContactNameMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_contact);
        initStatusBar();

        initView();
        mVolleyUtil = VolleyUtil.getInstance(this);
        mUser = PreferencesUtil.getInstance().getUser();
        mDialog = new LoadingDialog(this);

        List<PhoneContact> phoneContactList = getPhoneContant();
        List<String> phoneList = new ArrayList<>();
        for (PhoneContact phoneContact : phoneContactList) {
            String phone = phoneContact.getPhoneNumber().replaceAll(" ", "");
            phoneList.add(phone);
            mContactNameMap.put(phone, phoneContact.getDisplayName());
        }
        getPhoneContactList(mUser.getUserId(), JSON.toJSONString(phoneList));
    }

    private void initView() {
        mPhoneContactLv = findViewById(R.id.lv_phone_contact);
    }

    public void back(View view) {
        finish();
    }

    /**
     * 获取通讯录里的微信好友列表
     *
     * @param userId 用户ID
     * @param phones 手机号列表(json格式)
     * @return 通讯录里的微信好友列表
     */
    private void getPhoneContactList(String userId, String phones) {
        String url = Constant.BASE_URL + "users/" + userId + "/phoneContact";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("phones", phones);

        mDialog.setMessage("正在获取朋友信息");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        mVolleyUtil.httpPostRequest(url, paramMap, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                final List<User> userList = JSONArray.parseArray(response, User.class);
                for (User user : userList) {
                    user.setUserHeader(CommonUtil.setUserHeader(mContactNameMap.get(user.getUserPhone())));
                }
                // 对list进行排序
                Collections.sort(userList, new PinyinComparator() {
                });

                mPhoneContactAdapter = new PhoneContactAdapter(PhoneContactActivity.this, userList, mContactNameMap);
                mPhoneContactLv.setAdapter(mPhoneContactAdapter);

                mDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                mDialog.dismiss();
            }
        });
    }

    /**
     * 获取手机所有联系人
     *
     * @return 手机所有联系人列表
     */
    public List<PhoneContact> getPhoneContant() {
        // 取得ContentResolver
        List<PhoneContact> phoneContactList = new ArrayList<>();
        ContentResolver content = getContentResolver();

        // 联系人的URI
        Cursor cursor = content
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // id下标
                int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                // 名称下标
                int displayNameColumn = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                // 电话号码列
                int numberColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                String contactId = cursor.getString(idColumn);
                String displayName = cursor.getString(displayNameColumn);
                String phoneNumber = cursor.getString(numberColumn);

                PhoneContact phoneContact = new PhoneContact(contactId, displayName, phoneNumber);
                phoneContactList.add(phoneContact);
            }
        }
        return phoneContactList;
    }
}
