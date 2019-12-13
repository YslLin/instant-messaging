package com.ysl.im.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(name = "IM_MSG_CONTACT")
@IdClass(ContactMultiKeys.class)
public class MessageContact {

    @Id
    private Long ownerUid;
    @Id
    private Long otherUid;
    private Long mid;
    private Integer type;
    private Date createTime;

}
