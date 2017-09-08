package com.xxxtai.controller;

import com.xxxtai.model.AGVCar;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.SocketException;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AGVCpuRunnable implements Runnable {
    @Resource
    private CommunicationModule communicationModule;

    private AGVCar carModel;

    public AGVCpuRunnable() {
    }

    @Override
    public void run() {
        while (true) {
            if (System.currentTimeMillis() - this.carModel.getLastCommunicationTime() > 4500) {
                this.communicationModule.releaseSource();
                System.out.println("break//////////" + System.currentTimeMillis());
                this.carModel.setNewCpuRunnable();
                break;
            }

            String recMessage = null;
            if ((recMessage = this.communicationModule.read()) != null) {
                this.carModel.setLastCommunicationTime(System.currentTimeMillis());
//				System.out.println("AGV receive message:"+recMessage);
                if (recMessage.endsWith("BB")) {
                    this.carModel.setCardCommandMap(recMessage.substring(2, recMessage.length() - 2));
                }
                if (recMessage.startsWith("CC") && recMessage.endsWith("DD")) {
                    if (Integer.valueOf(recMessage.substring(2, 4), 16) == 1) {
                        this.carModel.startTheAGV();
                    } else if (Integer.valueOf(recMessage.substring(2, 4), 16) == 2) {
                        this.carModel.stopTheAGV();
                    }
                }
            }
//			try {
//				this.communicationModule.write("AA222222BB");
//			} catch (SocketException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//				this.communicationModule.releaseSource();
//				break;
//			}
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
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

    public boolean sendReadCardToSystem(int AGVNum, int cardNum, int state) {
        boolean isSuccess = false;
        String sendMessage = "AA";
        if (AGVNum < 16)
            sendMessage = sendMessage + "0" + Integer.toHexString(AGVNum);
        else
            sendMessage += Integer.toHexString(AGVNum);

        if (cardNum < 16)
            sendMessage = sendMessage + "0" + Integer.toHexString(cardNum);
        else
            sendMessage = sendMessage + Integer.toHexString(cardNum);

        sendMessage = sendMessage + "0" + state + "BB";

        try {
            isSuccess = this.communicationModule.write(sendMessage);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return isSuccess;
    }

}
