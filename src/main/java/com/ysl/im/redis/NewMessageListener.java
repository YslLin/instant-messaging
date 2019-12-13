package com.ysl.im.redis;

import com.alibaba.fastjson.JSONObject;
import com.ysl.im.ws.handler.WebSocketRouterHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@Component
public class NewMessageListener implements MessageListener {

    @Autowired
    private WebSocketRouterHandler webSocketRouterHandler;

    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    private static final RedisSerializer<String> valueSerializer = new GenericToStringSerializer(Object.class);

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String topic = stringRedisSerializer.deserialize(message.getChannel());
        String jsonMsg = valueSerializer.deserialize(message.getBody());
        System.out.println(String.format("Message Received --> pattern: {0}，topic:{1}，message: {2}", new String(pattern), topic, jsonMsg));
        JSONObject msgJson = JSONObject.parseObject(jsonMsg);
        long otherUid = msgJson.getLong("otherUid");
        JSONObject pushJson = new JSONObject();
        pushJson.put("type", 4);
        pushJson.put("data", msgJson);

        webSocketRouterHandler.pushMsg(otherUid, pushJson);
    }
}
