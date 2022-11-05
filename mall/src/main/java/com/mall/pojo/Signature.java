package com.mall.pojo;

import java.util.Date;

public class Signature {
    private String username;

    private String signature;

    private String privatekey;

    private String publickey;

    private Date creatTime;

    private Date updateTime;

    public Signature(String username, String signature, String privatekey, String publickey, Date creatTime, Date updateTime) {
        this.username = username;
        this.signature = signature;
        this.privatekey = privatekey;
        this.publickey = publickey;
        this.creatTime = creatTime;
        this.updateTime = updateTime;
    }

    public Signature() {
        super();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature == null ? null : signature.trim();
    }

    public String getPrivatekey() {
        return privatekey;
    }

    public void setPrivatekey(String privatekey) {
        this.privatekey = privatekey == null ? null : privatekey.trim();
    }

    public String getPublickey() {
        return publickey;
    }

    public void setPublickey(String publickey) {
        this.publickey = publickey == null ? null : publickey.trim();
    }

    public Date getCreatTime() {
        return creatTime;
    }

    public void setCreatTime(Date creatTime) {
        this.creatTime = creatTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}