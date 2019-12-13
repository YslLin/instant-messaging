package com.ysl.im.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(name = "IM_MSG_RELATION")
@IdClass(RelationMultiKeys.class)
public class MessageRelation {

    @Id
    private Long mid;
    @Id
    private Long ownerUid;
    private Integer type;
    private Long otherUid;
    private Date createTime;

}
