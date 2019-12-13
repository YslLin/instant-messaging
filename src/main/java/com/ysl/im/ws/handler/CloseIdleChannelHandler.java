package com.ysl.im.ws.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@ChannelHandler.Sharable
@Component
public class CloseIdleChannelHandler extends ChannelDuplexHandler {
    @Autowired
    private WebSocketRouterHandler webSocketRouterHandler;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.ALL_IDLE) {
                System.out.println(String.format("connector no receive ping packet from client,will close.,channel:{0}", ctx.channel()));
                webSocketRouterHandler.cleanUserChannel(ctx.channel());
                ctx.close();
            }
        }
    }
}
