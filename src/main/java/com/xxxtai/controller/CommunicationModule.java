package com.xxxtai.controller;

import com.xxxtai.myToolKit.ReaderWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class CommunicationModule {
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public CommunicationModule() {
        System.out.println("communicationModule");
    }

    boolean connect() {
        boolean isSuccess = false;
        try {
            System.out.println("ready to connect");
            this.socket = new Socket("127.0.0.1", 8001);
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
            isSuccess = true;
            System.out.println("connect success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSuccess;
    }

    String read() {
        boolean readSuccess = false;
        boolean foundStart = false;
        StringBuilder message = new StringBuilder();
        try {
            if (inputStream.available() > 0) {
                byte[] endCode = new byte[1];
                inputStream.read(endCode);
                message.append(ReaderWriter.bytes2HexString(endCode));
//					System.out.println("found start:"+message);
                if (message.toString().equals("CC") || message.toString().equals("AA")) {
                    foundStart = true;
                }
                if (foundStart) {
                    while (true) {
                        if (inputStream.available() > 0) {
                            byte[] buff = new byte[1];
                            inputStream.read(buff);
                            String str = ReaderWriter.bytes2HexString(buff);
                            if (!str.equals("BB") && !str.equals("DD")) {
                                message.append(str);
                            } else if (str.equals("BB")) {
                                message.append("BB");
                                readSuccess = true;
                                break;
                            } else if (str.equals("DD")) {
                                message.append("DD");
                                readSuccess = true;
                                break;
                            } else if (str.equals("AA")) {
                                readSuccess = false;
                                break;
                            }
                        } else {
                            readSuccess = false;
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (readSuccess) {
            return message.toString();
        } else {
            return null;
        }
    }

    boolean write(String sendMessage) throws SocketException {
        //System.out.println("ready to send to system :"+ sendMessage);
        boolean isSuccess = false;
        try {
            this.outputStream.write(ReaderWriter.hexString2Bytes(sendMessage));//ReaderWriter.hexString2Bytes(sendMessage)
            isSuccess = true;
        } catch (Exception e) {
            if (e instanceof SocketException)
                throw new SocketException();
            else
                e.printStackTrace();
        }
        //	if(isSuccess)
        //	System.out.println("success to send to system :"+ sendMessage);
        return isSuccess;
    }

    void releaseSource() {
        try {
            this.inputStream.close();
            this.outputStream.close();
            this.socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
