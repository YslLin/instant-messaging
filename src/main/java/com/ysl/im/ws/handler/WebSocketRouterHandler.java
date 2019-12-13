package com.ysl.im.ws.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@ChannelHandler.Sharable
@Component
public class WebSocketRouterHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final ConcurrentHashMap<Long, Channel> userChannel = new ConcurrentHashMap<>(15000);
    private static final ConcurrentHashMap<Channel, Long> channelUser = new ConcurrentHashMap<>(15000);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            String msg = ((TextWebSocketFrame) frame).text();
            JSONObject msgJson = JSONObject.parseObject(msg);
            int type = msgJson.getIntValue("type");
            JSONObject data = msgJson.getJSONObject("data");
            switch (type) {
                case 0:
                    break;
                case 1: // 上线消息
                    long loginUid = data.getLong("uid");
                    userChannel.put(loginUid, ctx.channel());
                    channelUser.put(ctx.channel(), loginUid);
//                    ctx.channel().attr()

                    ctx.writeAndFlush(new TextWebSocketFrame("{\"type\":1,\"status\":\"success\"}"));
                    break;
                case 2:
                    break;
                case 3: // 发送消息
                    long senderUid = data.getLong("senderUid");
                    long recipientUid = data.getLong("recipientUid");
                    String content = data.getString("content");
//                    int msgType = data.getIntValue("msgType");
//                    MessageVO messageContent = message
                    break;
            }
        }
    }

    /**
     * 向客户端推送消息
     *
     * @param recipientUid
     * @param message
     */
    public void pushMsg(long recipientUid, JSONObject message) {
        Channel channel = userChannel.get(recipientUid);
        if (channel != null && channel.isActive() && channel.isWritable()) {
//            AtomicLong generator
            channel.writeAndFlush(new TextWebSocketFrame(message.toJSONString())).addListener(future -> {
                if (future.isCancelled()) {
                    System.out.println(String.format("future has been cancelled. {0}, channel: {1}", message, channel));
                } else if (future.isSuccess()) {
//                    addMsgToAckBuffer(channel, message);
                    System.out.println(String.format("future has been successfully pushed. {0}, channel: {1}", message, channel));
                } else {
                    System.out.println(String.format("message write fail, {0}, channel: {1}", message, channel, future.cause()));
                }
            });
        }
    }

    /**
     * 清除用户和 socket 映射的相关信息
     *
     * @param channel
     */
    void cleanUserChannel(Channel channel) {
        long uid = channelUser.remove(channel);
        userChannel.remove(uid);
    }
}
