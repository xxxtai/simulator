package com.xxxtai.simulator.controller;

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

    private AGVCar carModel;

    public AGVCpuRunnable() {}

    @Override
    public void run() {
        while (true) {
            if (System.currentTimeMillis() - this.carModel.getLastCommunicationTime() > 4500) {
                this.communicationModule.releaseSource();
                log.info("break//////////" + System.currentTimeMillis());
                this.carModel.setNewCpuRunnable();
                break;
            }

            String recMessage;
            if ((recMessage = this.communicationModule.read()) != null) {
                this.carModel.setLastCommunicationTime(System.currentTimeMillis());
//				log.info("AGV receive message:"+recMessage);
                if (recMessage.endsWith(Constant.ROUTE_SUFFIX)) {
                    this.carModel.setCardCommandMap(Constant.getContent(recMessage));
                }
                if (recMessage.startsWith(Constant.PREFIX) && recMessage.endsWith(Constant.COMMAND_SUFFIX)) {
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
        communicationModule.write(Constant.PREFIX + Integer.toHexString(AGVNum) + "/" + Integer.toHexString(cardNum) + Constant.CARD_SUFFIX);
    }

    public void sendStateToSystem(int AGVNum, int state){
        communicationModule.write(Constant.PREFIX + Integer.toHexString(AGVNum) + "/" + Integer.toHexString(state) + Constant.STATE_SUFFIX);
    }

    public void heartBeat(int AGVNum){
        communicationModule.write(Constant.PREFIX + Integer.toHexString(AGVNum) + Constant.HEART_SUFFIX);
    }

}
