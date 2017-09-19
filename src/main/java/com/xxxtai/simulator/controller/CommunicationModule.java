package com.xxxtai.simulator.controller;

import com.xxxtai.express.constant.Constant;
import com.xxxtai.express.toolKit.ReaderWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.io.*;
import java.net.Socket;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j(topic = "develop")
public class CommunicationModule {
    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private InputStream inputStream;

    public CommunicationModule() {
        log.info("communicationModule");
    }

    boolean connect() {
        boolean isSuccess = false;
        try {
            log.info("ready to connect");
            this.socket = new Socket("127.0.0.1", 8899);
            printWriter = new PrintWriter(socket.getOutputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            inputStream = socket.getInputStream();
            isSuccess = true;
            log.info("connect success");
        } catch (Exception e) {
            log.error("exception:", e);
        }
        return isSuccess;
    }

    String read() {
        String revMsg = null;
        try {
            revMsg= bufferedReader.readLine();
            log.info("receive message:" + revMsg);
        } catch (IOException e) {
            log.error("exception:", e);
        }
        return revMsg;
    }

    String inputStreamRead() {
        boolean found = false;
        StringBuilder revMsg = new StringBuilder();
        byte[] b = new byte[1];
        try {
            while (true) {
                if (inputStream.available() <= 0) {
                    break;
                }
                inputStream.read(b);
                if (!found) {
                    String temp = ReaderWriter.bytes2HexString(b);
                    if (temp.equals(Constant.COMMAND_PREFIX) || temp.equals(Constant.HEART_PREFIX) || temp.equals(Constant.ROUTE_PREFIX)){
                        found = true;
                        revMsg.append(temp);
                    }
                } else {
                    String temp = ReaderWriter.bytes2HexString(b);
                    revMsg.append(temp);
                    if (temp.equals(Constant.SUFFIX)) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            log.error("exception:", e);
        }
        return revMsg.toString();
    }

    boolean write(String sendMessage) {
        boolean isSuccess = false;
        try {
            printWriter.println(sendMessage);
            printWriter.flush();
            isSuccess = true;
        } catch (Exception e) {
            log.error("exception:", e);
        }
        return isSuccess;
    }

    void releaseSource() {
        try {
            this.bufferedReader.close();
            this.printWriter.close();
            this.socket.close();
        } catch (IOException e) {
            log.error("exception:", e);
        }

    }
}
