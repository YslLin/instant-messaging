package com.ysl.im.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ContactMultiKeys implements Serializable {

    protected Long ownerUid;
    protected Long otherUid;

    public ContactMultiKeys(Long ownerUid, Long otherUid) {
        this.ownerUid = ownerUid;
        this.otherUid = otherUid;
    }

    public ContactMultiKeys() {
    }

}
