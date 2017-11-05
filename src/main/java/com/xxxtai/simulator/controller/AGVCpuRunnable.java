package com.xxxtai.simulator.controller;

import com.xxxtai.express.model.Graph;
import com.xxxtai.simulator.model.AGVCar;
import com.xxxtai.express.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
@Slf4j(topic = "develop")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AGVCpuRunnable implements Runnable {
    @Resource
    private CommunicationModule communicationModule;

    @Resource
    private Graph graph;

    private AGVCar carModel;

    public AGVCpuRunnable() {}

    @Override
    public void run() {
        while (true) {
            if (System.currentTimeMillis() - this.carModel.getLastCommunicationTime() > 75000) {
                this.communicationModule.releaseSource();
                log.info("break//////////" + System.currentTimeMillis());
                this.carModel.setNewCpuRunnable();
                break;
            }

            String recMessage = communicationModule.inputStreamRead();
            if (recMessage != null && recMessage.length() > 0) {
                this.carModel.setLastCommunicationTime(System.currentTimeMillis());
                if (recMessage.startsWith(Constant.ROUTE_PREFIX)) {
                    this.carModel.setCardCommandMap(Constant.getContent(recMessage));
                }
                if (recMessage.endsWith(Constant.SUFFIX) && recMessage.startsWith(Constant.COMMAND_PREFIX)) {
                    String content = Constant.getContent(recMessage);
                    String[] c = content.split(Constant.SPLIT);
                    if (Integer.valueOf(c[0], 16) == 1) {
                        this.carModel.startTheAGV();
                    } else if (Integer.valueOf(c[0], 16) == 2) {
                        this.carModel.stopTheAGV();
                    }
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setCarModelToCpu(AGVCar car) {
        this.carModel = car;
    }

    public boolean connect() {
        return this.communicationModule.connect();
    }

    public void sendReadCardToSystem(int AGVNum, int cardNum) {
        if (Constant.USE_SERIAL) {
            communicationModule.write(Constant.CARD_PREFIX  +  graph.getCardNumMap().get(cardNum) + Constant.SUFFIX);
        } else {
            communicationModule.write(Constant.CARD_PREFIX  +  cardNum + Constant.SUFFIX);
        }
    }

    public void sendStateToSystem(int AGVNum, int state){
        communicationModule.write(Constant.STATE_PREFIX + Integer.toHexString(state) + Constant.SUFFIX);
    }

    public void heartBeat(int AGVNum){
        communicationModule.write(Constant.HEART_PREFIX + Integer.toHexString(AGVNum) + Constant.SUFFIX);
    }

}
