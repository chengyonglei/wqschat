package com.wqs.wechat.dao;

import com.wqs.wechat.entity.Address;

import java.util.List;

/**
 * 地址
 *
 * @author zhou
 */
public class AddressDao {
    public List<Address> getAddressList() {
        return Address.listAll(Address.class);
    }

    public void saveAddress(Address address) {
        Address.save(address);
    }

    public void clearAddress() {
        Address.deleteAll(Address.class);
    }

    public Address getAddressByAddressId(String addressId) {
        List<Address> addressList = Address.find(Address.class, "address_id = ?", addressId);
        if (null != addressList && addressList.size() > 0) {
            return addressList.get(0);
        }
        return null;
    }

    public void deleteAddressByAddressId(String addressId) {
        String sql = "delete from address where address_id = ?";
        Address.executeQuery(sql, addressId);
    }
}
