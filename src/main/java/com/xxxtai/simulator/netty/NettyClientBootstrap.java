package com.xxxtai.simulator.netty;

import com.xxxtai.express.model.Car;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j(topic = "develop")
public class NettyClientBootstrap {
    protected final HashedWheelTimer timer = new HashedWheelTimer();
    private Bootstrap boot;
    private final ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();
    private int port;
    private String host;
    private Car car;
    private SocketChannel socketChannel;

    public NettyClientBootstrap(int port, String host, Car car) throws InterruptedException {
        this.port = port;
        this.host = host;
        this.car = car;
    }

    public SocketChannel connect() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        boot = new Bootstrap();
        boot.group(group).channel(NioSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO));

        final ConnectionWatchdog watchdog = new ConnectionWatchdog(boot, timer, port, host, car.getAGVNum(), true) {

            public ChannelHandler[] handlers() {
                return new ChannelHandler[]{
                        this,
                        new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS),
                        idleStateTrigger,
                        new StringDecoder(),
                        new StringEncoder(),
                        new HeartBeatClientHandler(car)
                };
            }
        };
        ChannelFuture future;
        //进行连接
        try {
            synchronized (boot) {
                boot.handler(new ChannelInitializer<Channel>() {

                    //初始化channel
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(watchdog.handlers());
                    }
                });
                future = boot.connect(host, port);
            }
            // 以下代码在synchronized同步块外面是安全的
            future.sync();
            if (future.isSuccess()) {
                socketChannel = (SocketChannel) future.channel();
                log.info(car.getAGVNum() + "AGV connect server  成功---------");
            }
            return socketChannel;
        } catch (Throwable t) {
            log.info(car.getAGVNum() + "AGV connect connects to  fails", t);
            throw new Exception("connects to  fails", t);
        }
    }
}
