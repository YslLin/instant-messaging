package com.ysl.im.service;

import com.ysl.im.vo.MessageVO;

import java.util.List;

public interface MessageService {

    /**
     * 查询两人的历史消息
     * @param ownerUid
     * @param otherUid
     * @return
     */
    List<MessageVO> queryConversationMsg(long ownerUid, long otherUid);

    /**
     * 发送新消息
     * @param senderUid
     * @param recipientUid
     * @param content
     * @param msgType
     * @return
     */
    MessageVO sendNewMsg(long senderUid, long recipientUid, String content, int msgType);
}
