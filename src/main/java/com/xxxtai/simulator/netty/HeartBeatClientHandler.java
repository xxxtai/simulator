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
        log.info(car.getAGVNum() + "AGV channel激活时间是："+new Date());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info(car.getAGVNum() + "AGV channel停止时间是："+new Date());
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        String msg = (String) message;
        log.debug(this.car.getAGVNum() + "AGV rec：" + msg);

        String[] contents = msg.split(Constant.SUFFIX);
        for (String content : contents) {
            if (content.startsWith(Constant.ROUTE_PREFIX)) {
                log.info(this.car.getAGVNum() + "AGV route:" + msg);
                ((AGVCar) car).setCardCommandMap(content.substring(Constant.FIX_LENGTH, content.length()));
            }
            if (content.startsWith(Constant.COMMAND_PREFIX)) {
                String[] c = content.substring(Constant.FIX_LENGTH, content.length()).split(Constant.SPLIT);
                if (Integer.valueOf(c[0], 16) == 1) {
                    this.car.setState(State.FORWARD);
                } else if (Integer.valueOf(c[0], 16) == 2) {
                    this.car.setState(State.STOP);
                }
            }
        }
        ReferenceCountUtil.release(msg);
    }
}
