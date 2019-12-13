package com.ysl.im.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.ysl.im.Constants;
import com.ysl.im.dao.MessageContactRepository;
import com.ysl.im.dao.MessageContentRepository;
import com.ysl.im.dao.MessageRelationRepository;
import com.ysl.im.dao.UserRepository;
import com.ysl.im.entity.*;
import com.ysl.im.service.MessageService;
import com.ysl.im.vo.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRelationRepository relationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageContentRepository contentRepository;

    @Autowired
    private MessageContactRepository contactRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<MessageVO> queryConversationMsg(long ownerUid, long otherUid) {
        List<MessageRelation> relationList = relationRepository.findAllByOwnerUidAndOtherUidOrderByMid(ownerUid, otherUid);
        return composeMessageVO(relationList, ownerUid, otherUid);
    }

    private List<MessageVO> composeMessageVO(List<MessageRelation> relationList, long ownerUid, long otherUid) {
        if (null == relationList || relationList.isEmpty()) return null;
        /** 拼接消息索引和内容 */
        List<MessageVO> msgList = Lists.newArrayList();
        User self = userRepository.findById(ownerUid).orElse(null);
        User other = userRepository.findById(otherUid).orElse(null);
        relationList.stream().forEach(relation -> {
            Long mid = relation.getMid();
            MessageContent contentVO = contentRepository.getOne(mid);
            if (null == contentVO) return;
            String content = contentVO.getContent();
            MessageVO messageVO = new MessageVO(mid, content, ownerUid, relation.getType(), otherUid, relation.getCreateTime(), self.getAvatar(), other.getAvatar(), self.getUsername(), other.getUsername());
            msgList.add(messageVO);
        });

        /** 变更未读数 */
        Object convUnreadObj = redisTemplate.opsForHash().get(ownerUid + "_C", otherUid);
        if (null != convUnreadObj) {
            long convUnread = Long.parseLong((String) convUnreadObj);
            redisTemplate.opsForHash().delete(ownerUid + "_C", otherUid);
            long afterCleanUnread = redisTemplate.opsForValue().increment(ownerUid + "_T", -convUnread);
            /** 修正总未读数 */
            if (afterCleanUnread < 0) {
                redisTemplate.delete(ownerUid + "_T");
            }
        }
        return msgList;
    }

    @Override
    public MessageVO sendNewMsg(long senderUid, long recipientUid, String content, int msgType) {
        Date currentTime = new Date();

        /** 存内容 */
        MessageContent messageContent = new MessageContent();
        messageContent.setSenderId(senderUid);
        messageContent.setRecipientId(recipientUid);
        messageContent.setContent(content);
        messageContent.setMsgType("".equals(msgType+"") ? 1 : msgType);
        messageContent.setCreateTime(currentTime);
        messageContent = contentRepository.saveAndFlush(messageContent);
        Long mid = messageContent.getMid();

        /** 存发件人的发件箱 */
        MessageRelation messageRelationSender = new MessageRelation();
        messageRelationSender.setMid(mid);
        messageRelationSender.setOwnerUid(senderUid);
        messageRelationSender.setOtherUid(recipientUid);
        messageRelationSender.setType(0);
        messageRelationSender.setCreateTime(currentTime);
        relationRepository.save(messageRelationSender);

        /** 存收件人的收件箱 */
        MessageRelation messageRelationRecipient = new MessageRelation();
        messageRelationRecipient.setMid(mid);
        messageRelationRecipient.setOwnerUid(recipientUid);
        messageRelationRecipient.setOtherUid(senderUid);
        messageRelationRecipient.setType(1);
        messageRelationRecipient.setCreateTime(currentTime);
        relationRepository.save(messageRelationRecipient);

        /** 更新发件人的最近联系人 */
        MessageContact messageContactSender = contactRepository.findById(new ContactMultiKeys(senderUid, recipientUid)).orElse(null);
        if (messageContactSender != null) {
            messageContactSender.setMid(mid);
        } else {
            messageContactSender = new MessageContact();
            messageContactSender.setMid(mid);
            messageContactSender.setOwnerUid(senderUid);
            messageContactSender.setOtherUid(recipientUid);
            messageContactSender.setCreateTime(currentTime);
            messageContactSender.setType(0);
        }
        contactRepository.save(messageContactSender);

        /** 更新收件人的最近联系人 */
        MessageContact messageContactRecipient = contactRepository.findById(new ContactMultiKeys(recipientUid, senderUid)).orElse(null);
        if (messageContactRecipient != null) {
            messageContactRecipient.setMid(mid);
        } else {
            messageContactRecipient = new MessageContact();
            messageContactRecipient.setMid(mid);
            messageContactRecipient.setOwnerUid(recipientUid);
            messageContactRecipient.setOtherUid(senderUid);
            messageContactRecipient.setCreateTime(currentTime);
            messageContactRecipient.setType(1);
        }
        contactRepository.save(messageContactRecipient);

        /** 更新未读数 */
        redisTemplate.opsForValue().increment(recipientUid+"_T", 1); // 总未读数
        redisTemplate.opsForHash().increment(recipientUid+"_C", senderUid, 1); // 会话未读数

        /** 推送消息发布到 redis */
        User self = userRepository.findById(senderUid).orElse(null);
        User other = userRepository.findById(recipientUid).orElse(null);
        MessageVO messageVO = new MessageVO(mid, content, senderUid, 0, recipientUid, currentTime, self.getAvatar(), other.getAvatar(), self.getUsername(), other.getUsername());
        redisTemplate.convertAndSend(Constants.WEBSOCKET_MSG_TOPIC, JSONObject.toJSONString(messageVO));

        return messageVO;
    }
}
