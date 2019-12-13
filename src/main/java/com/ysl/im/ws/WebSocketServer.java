package com.ysl.im.ws;

import com.ysl.im.ws.handler.CloseIdleChannelHandler;
import com.ysl.im.ws.handler.WebSocketRouterHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Component
public class WebSocketServer {

    private ServerBootstrap bootstrap;
    private ChannelFuture channelFuture;

//    private EventExecutorGroup eventExecutorGroup;

    @Autowired
    private WebSocketRouterHandler webSocketRouterHandler;

    @Autowired
    private CloseIdleChannelHandler closeIdleChannelHandler;

    @PostConstruct
    public void start() throws InterruptedException {
        ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpObjectAggregator(65536));
                pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
                pipeline.addLast(new WebSocketServerProtocolHandler("/", null, true));

                pipeline.addLast(new IdleStateHandler(0, 0, 360));
                pipeline.addLast(closeIdleChannelHandler);

                pipeline.addLast(webSocketRouterHandler);
            }
        };

        bootstrap = newServerBootstrap();
        bootstrap.childHandler(initializer);

        channelFuture = bootstrap.bind(4040).sync();

        Runtime.getRuntime().addShutdownHook(new ShutdownThread());

        new Thread(() -> {
            try {
                channelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    ServerBootstrap newServerBootstrap() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        return new ServerBootstrap().group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
    }

    class ShutdownThread extends Thread {
        @Override
        public void run() {
            close();
        }

        void close() {
            if (bootstrap == null) return;
            if (channelFuture != null) {
                channelFuture.channel().close().awaitUninterruptibly(10, TimeUnit.SECONDS);
                channelFuture = null;
            }
            if (bootstrap != null && bootstrap.config().group() != null) {
                bootstrap.config().group().shutdownGracefully();
            }
            if (bootstrap != null && bootstrap.config().childGroup() != null) {
                bootstrap.config().childGroup().shutdownGracefully();
            }
            bootstrap = null;
        }
    }
}
