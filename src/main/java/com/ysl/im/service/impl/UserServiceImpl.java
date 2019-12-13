package com.ysl.im.service.impl;

import com.ysl.im.dao.MessageContactRepository;
import com.ysl.im.dao.MessageContentRepository;
import com.ysl.im.dao.UserRepository;
import com.ysl.im.entity.MessageContact;
import com.ysl.im.entity.MessageContent;
import com.ysl.im.entity.User;
import com.ysl.im.exceptions.InvalidUserInfoException;
import com.ysl.im.exceptions.UserNotExistException;
import com.ysl.im.service.UserService;
import com.ysl.im.vo.MessageContactVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageContactRepository contactRepository;

    @Autowired
    private MessageContentRepository contentRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public User login(String email, String password) {
        List<User> users = userRepository.findByEmail(email);
        if (null == users || users.isEmpty()) {
            System.out.println("用户不存在");
            throw new UserNotExistException("该用户不存在:" + email);
        } else {
            User user = users.get(0);
            if (user.getPassword().equals(password)) {
                System.out.println("登录成功");
                return user;
            } else {
                System.out.println("密码不正确");
                throw new InvalidUserInfoException("密码不正确");
            }
//            long afterCleanUnread = redisTemplate.opsForValue().increment(user.getUid() + "_T", 10);
//            System.out.println("settttttt"+afterCleanUnread);
//            Object totalUnreadObj = redisTemplate.opsForValue().get(user.getUid() + "_T");
//            long convUnread = Long.parseLong((String) totalUnreadObj);
//            System.out.println("gettttttt"+convUnread);
        }
    }

    @Override
    public List<User> getAllUsersExcept(User exceptUser) {
        return userRepository.findUsersByUidIsNot(exceptUser.getUid());
    }

    @Override
    public MessageContactVO getRecentContacts(User ownerUser) {
        List<MessageContact> contacts = contactRepository.findMessageContactsByOwnerUidOrderByMidDesc(ownerUser.getUid());
        if (contacts == null) return null;
        long totalUnread = 0;
        Object totalUnreadObj = redisTemplate.opsForValue().get(ownerUser.getUid() + "_T");
        if (null != totalUnreadObj) {
            totalUnread = Long.parseLong((String) totalUnreadObj);
        }

        final MessageContactVO contactVo = new MessageContactVO(ownerUser.getUid(), ownerUser.getUsername(), ownerUser.getAvatar(), totalUnread);
        contacts.stream().forEach(contact -> {
            Long mid = contact.getMid();
            MessageContent content = contentRepository.findById(mid).orElse(null);
            User otherUser = userRepository.findById(contact.getOtherUid()).orElse(null);

            if (null == content) return;
            long convUnread = 0;
            Object convUnreadObj = redisTemplate.opsForHash().get(ownerUser.getUid() + "_C", otherUser.getUid());
            if (null != convUnreadObj) {
                convUnread = Long.parseLong((String) convUnreadObj);
            }

            MessageContactVO.ContactInfo contactInfo = contactVo.new ContactInfo(otherUser.getUid(), otherUser.getUsername(), otherUser.getAvatar(), mid, contact.getType(), content.getContent(), convUnread, content.getCreateTime());
            contactVo.appendContact(contactInfo);
        });
        return contactVo;
    }
}
