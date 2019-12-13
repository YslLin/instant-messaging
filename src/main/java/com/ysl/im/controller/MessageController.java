package com.ysl.im.controller;

import com.ysl.im.service.MessageService;
import com.ysl.im.utils.ResultVOUtil;
import com.ysl.im.vo.MessageVO;
import com.ysl.im.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MessageController {

    @Autowired
    MessageService messageService;

    @GetMapping(path = "/queryMsg")
    public ResultVO queryMsg(@RequestParam Long ownerUid, @RequestParam Long otherUid) {
        List<MessageVO> messageVOList = messageService.queryConversationMsg(ownerUid, otherUid);
        if (messageVOList != null) {
            return ResultVOUtil.succes(messageVOList);
        }
        return ResultVOUtil.fail();
    }

    @PostMapping(path = "/sendMsg")
    public ResultVO sendMsg(@RequestBody Map<String, Object> paramMap) {
        Long senderUid = Long.parseLong(paramMap.get("senderUid").toString());
        Long recipientUid = Long.parseLong(paramMap.get("recipientUid").toString());
        String content = (String) paramMap.get("content");
        MessageVO messageVO = messageService.sendNewMsg(senderUid, recipientUid, content, 1);
        if (messageVO != null) {
            return ResultVOUtil.succes(messageVO);
        }
        return ResultVOUtil.fail();
    }
}
