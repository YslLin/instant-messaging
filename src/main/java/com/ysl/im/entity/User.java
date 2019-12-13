package com.ysl.im.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "IM_USER")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;
    private String username;
    private String password;
    private String email;
    private String avatar;

}
