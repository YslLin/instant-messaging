package com.ysl.im.controller;

import com.ysl.im.entity.User;
import com.ysl.im.exceptions.InvalidUserInfoException;
import com.ysl.im.exceptions.UserNotExistException;
import com.ysl.im.service.UserService;
import com.ysl.im.utils.ResultVOUtil;
import com.ysl.im.vo.MessageContactVO;
import com.ysl.im.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping(path = "/")
    public String welcomePage(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "index";
        } else {
            return "login";
        }
    }

    @RequestMapping(path = "/login")
    public ResultVO login(@RequestParam String email, @RequestParam String password, HttpSession session) {
        try {
            User loginUser = userService.login(email, password);
            session.setAttribute("user", loginUser);
            return ResultVOUtil.succes(loginUser);
        } catch (UserNotExistException e) {
            return ResultVOUtil.fail(-1, email + ": 该用户不存在！");
        } catch (InvalidUserInfoException e) {
            return ResultVOUtil.fail(-1, "密码输入错误！");
        }
    }

    @RequestMapping("/getContactList")
    public ResultVO getUsers(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            List<User> list = userService.getAllUsersExcept(user);
            return ResultVOUtil.succes(list);
        }
        return ResultVOUtil.fail(401, "登录超时！");
    }

    @RequestMapping("/getRecentContacts")
    public ResultVO getRecentContacts(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            MessageContactVO contactVo = userService.getRecentContacts(user);
            return ResultVOUtil.succes(contactVo);
        }
        return ResultVOUtil.fail(401, "登录超时！");
    }
}
