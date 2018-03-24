package com.xxxtai.simulator.netty;

import com.xxxtai.express.constant.Constant;
import com.xxxtai.express.model.Car;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 重连检测狗，当发现当前的链路不稳定关闭之后，进行12次重连
 */
@Sharable
@Slf4j(topic = "develop")
public abstract class ConnectionWatchdog extends ChannelInboundHandlerAdapter implements TimerTask, ChannelHandlerHolder {
    private final Bootstrap bootstrap;
    private final Timer timer;
    private final int port;
    private final String host;
    private final int agvNum;
    private volatile boolean reconnect = true;
    private int attempts;


    public ConnectionWatchdog(Bootstrap bootstrap, Timer timer, int port, String host, int agvNum, boolean reconnect) {
        this.bootstrap = bootstrap;
        this.timer = timer;
        this.port = port;
        this.host = host;
        this.reconnect = reconnect;
        this.agvNum = agvNum;
    }

    /**
     * channel链路每次active的时候，将其连接的次数重新☞ 0
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info(this.agvNum + "AGV 当前链路已经激活了，重连尝试次数为:" + attempts);
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (reconnect) {
            log.info(this.agvNum + "AGV 链接关闭，将进行重连");
            if (attempts < 12) {
                attempts++;
                //重连的间隔时间会越来越长
                int timeout = 2 << attempts;
                timer.newTimeout(this, timeout, TimeUnit.MILLISECONDS);
            }
        }
        ctx.fireChannelInactive();
    }


    public void run(Timeout timeout) throws Exception {
        ChannelFuture future;
        //bootstrap已经初始化好了，只需要将handler填入就可以了
        synchronized (bootstrap) {
            bootstrap.handler(new ChannelInitializer<Channel>() {

                @Override
                protected void initChannel(Channel ch) throws Exception {

                    ch.pipeline().addLast(handlers());
                }
            });
            future = bootstrap.connect(host, port);
        }
        //future对象
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture f) throws Exception {
                boolean succeed = f.isSuccess();
                //如果重连失败，则调用ChannelInactive方法，再次出发重连事件，一直尝试12次，如果失败则不再重连
                if (!succeed) {
                    log.info(agvNum + "AGV 重连失败");
                    f.channel().pipeline().fireChannelInactive();
                } else {
                    log.info(agvNum + "AGV 重连成功");
                    f.channel().writeAndFlush(Constant.HEART_PREFIX + Integer.toHexString(agvNum) + Constant.SUFFIX);
                }
            }
        });
    }
}
