package com.easemob.demo;

import static org.usergrid.java.client.utils.JsonUtils.getStringProperty;
import static org.usergrid.java.client.utils.JsonUtils.setStringProperty;


import org.usergrid.java.client.entities.Entity;


import com.easemob.chat.domain.EMUserBase;

public class DemoUser extends EMUserBase {
    
    //this is the bare jid on XMPP server. It takes a format of appkey-companykey-username@domain, eg 12345-12345-jliu@ac2
    public final static String PROPERTY_SEX = "sex";
    public final static String PROPERTY_DEPARTEMENT = "department";
    public final static String PROPERTY_MOBILE = "mobile";
    public final static String PROPERTY_WORKPHONE = "workPhone";
    public final static String PROPERTY_ADDRESS = "address";
    public final static String PROPERTY_COMPANYKEY = "companykey";
    
    public DemoUser(Entity entity) {
        super(entity);
    }
    
    public DemoUser() {
        super();
    }
    
    private String note;
    
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
    
    public String getSex() {
        return getStringProperty(properties, PROPERTY_SEX);
    }
    public void setSex(String sex) {
        setStringProperty(properties, PROPERTY_SEX, sex);
    }
    public String getDepartment() {
        return getStringProperty(properties, PROPERTY_DEPARTEMENT);
    }
    public void setDepartment(String department) {
        setStringProperty(properties, PROPERTY_DEPARTEMENT, department);
    }
    public String getMobile() {
        return getStringProperty(properties, PROPERTY_MOBILE);
    }
    public void setMobile(String mobile) {
        setStringProperty(properties, PROPERTY_MOBILE, mobile);
    }
    public String getWorkPhone() {
        return getStringProperty(properties, PROPERTY_WORKPHONE);
    }
    public void setWorkPhone(String workPhone) {
        setStringProperty(properties, PROPERTY_WORKPHONE,workPhone);
    }
    public String getAddress() {
        return getStringProperty(properties, PROPERTY_ADDRESS);
    }
    public void setAddress(String address) {
        setStringProperty(properties, PROPERTY_ADDRESS, address);
    }
    public String getCompanyKey() {
        return getStringProperty(properties, PROPERTY_COMPANYKEY);
    }
    public void setCompanyKey(String companyKey) {
        setStringProperty(properties, PROPERTY_COMPANYKEY, companyKey);
    }
}
