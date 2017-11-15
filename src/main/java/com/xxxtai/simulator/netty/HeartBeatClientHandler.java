package com.xxxtai.simulator.netty;

import com.xxxtai.express.constant.Constant;
import com.xxxtai.express.constant.State;
import com.xxxtai.express.model.Car;
import com.xxxtai.express.model.Graph;
import com.xxxtai.simulator.model.AGVCar;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Date;

@Sharable
@Slf4j(topic = "develop")
public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter {
    @Resource
    private Graph graph;
    private Car car;

    public HeartBeatClientHandler(Car car){
        this.car = car;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("激活时间是："+new Date());
        System.out.println("HeartBeatClientHandler channelActive");
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info(this.car.getAGVNum() + "AGV inactive!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("停止时间是："+new Date());
        System.out.println("HeartBeatClientHandler channelInactive");
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        String msg = (String) message;
        log.info(msg);

        if (msg != null && msg.length() > 0) {
            this.car.setLastCommunicationTime(System.currentTimeMillis());
            if (msg.startsWith(Constant.ROUTE_PREFIX)) {
                log.info(this.car.getAGVNum() + "AGV route:" + msg);
                ((AGVCar) car).setCardCommandMap(Constant.getContent(msg));
            }
            if (msg.endsWith(Constant.SUFFIX) && msg.startsWith(Constant.COMMAND_PREFIX)) {
                String content = Constant.getContent(msg);
                String[] c = content.split(Constant.SPLIT);
                if (Integer.valueOf(c[0], 16) == 1) {
                    this.car.setState(State.FORWARD);
                    log.info("让" + this.car.getAGVNum() + "AGV前进");
                } else if (Integer.valueOf(c[0], 16) == 2) {
                    this.car.setState(State.STOP);
                    log.info("让" + this.car.getAGVNum() + "AGV停止");
                }
            }
        }
        ReferenceCountUtil.release(msg);
    }
}
