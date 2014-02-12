package com.easemob.demo.domain;

import org.usergrid.java.client.utils.JsonUtils;


import org.usergrid.java.client.entities.Entity;


import com.easemob.user.domain.EMUserBase;

public class DemoUser extends EMUserBase {
    public final static String PROPERTY_MOBILE = "mobile";
    public final static String PROPERTY_WORKPHONE = "workPhone";
    public final static String PROPERTY_ADDRESS = "address";
    
    public DemoUser(Entity entity) {
        super(entity);
    }
    
    public DemoUser() {
        super();
    }   

    public String getMobile() {
        return JsonUtils.getStringProperty(properties, PROPERTY_MOBILE);
    }
    public void setMobile(String mobile) {
        JsonUtils.setStringProperty(properties, PROPERTY_MOBILE, mobile);
    }
    public String getWorkPhone() {
        return JsonUtils.getStringProperty(properties, PROPERTY_WORKPHONE);
    }
    public void setWorkPhone(String workPhone) {
        JsonUtils.setStringProperty(properties, PROPERTY_WORKPHONE,workPhone);
    }
    public String getAddress() {
        return JsonUtils.getStringProperty(properties, PROPERTY_ADDRESS);
    }
    public void setAddress(String address) {
        JsonUtils.setStringProperty(properties, PROPERTY_ADDRESS, address);
    }
}
