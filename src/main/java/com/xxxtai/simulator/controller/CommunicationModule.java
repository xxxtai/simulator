package com.xxxtai.simulator.controller;

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

        } catch (IOException e) {
            log.error("exception:", e);
        }
        return revMsg;
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
