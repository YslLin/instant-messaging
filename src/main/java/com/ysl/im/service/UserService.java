package com.ysl.im.service;

import com.ysl.im.entity.User;
import com.ysl.im.vo.MessageContactVO;

import java.util.List;

public interface UserService {

    User login(String email, String password);

    List<User> getAllUsersExcept(User exceptUser);

    MessageContactVO getRecentContacts(User ownerUser);
}
