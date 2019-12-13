package com.ysl.im.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "IM_MSG_CONTENT")
public class MessageContent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long mid;
    private Long senderId;
    private Long recipientId;
    private String content;
    private Integer msgType;
    private Date createTime;

}
