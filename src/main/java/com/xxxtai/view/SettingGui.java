package com.xxxtai.view;

import com.xxxtai.main.Main;
import com.xxxtai.myToolKit.City;
import com.xxxtai.myToolKit.Common;
import com.xxxtai.myToolKit.Constant;
import com.xxxtai.myToolKit.MyTextField;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Component
public class SettingGui extends JPanel {
    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private RoundButton schedulingGuiBtn;
    private RoundButton drawingGuiBtn;

    public SettingGui() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        schedulingGuiBtn = new RoundButton("调度界面");
        schedulingGuiBtn.setBounds(0, 0, screenSize.width / 3, screenSize.height / 20);

        RoundButton settingGuiBtn = new RoundButton("设置界面");
        settingGuiBtn.setBounds(screenSize.width / 3, 0, screenSize.width / 3, screenSize.height / 20);
        settingGuiBtn.setForeground(new Color(30, 144, 255));
        settingGuiBtn.setBackground(Color.WHITE);

        drawingGuiBtn = new RoundButton("制图界面");
        drawingGuiBtn.setBounds(2 * screenSize.width / 3, 0, screenSize.width / 3, screenSize.height / 20);

        JLabel stateLabel = new JLabel();
        stateLabel.setBounds(0, 22 * screenSize.height / 25, screenSize.width, screenSize.height / 25);
        stateLabel.setFont(new Font("宋体", Font.BOLD, 25));


        MyTextField entranceField = new MyTextField("        入口");
        entranceField.setBounds(5 * screenSize.width / 12, 3 * screenSize.height / 15, screenSize.width / 6, screenSize.height / 20);
        MyTextField exitField = new MyTextField("        出口");
        exitField.setBounds(5 * screenSize.width / 12, 4 * screenSize.height / 15, screenSize.width / 6, screenSize.height / 20);
        RoundButton confirmBtn = new RoundButton("确认");
        confirmBtn.setBounds(5 * screenSize.width / 12, 6 * screenSize.height / 15, screenSize.width / 6, screenSize.height / 20);
        confirmBtn.addActionListener(e -> {
            printWriter.println(Constant.PREFIX + Integer.toHexString(Integer.parseInt(entranceField.getText())) +
                    Constant.SPLIT + Long.toHexString(City.valueOfName(exitField.getText()).getCode()) + Constant.QR_SUFFIX);
            printWriter.flush();
        });

        this.setLayout(null);
        this.add(schedulingGuiBtn);
        this.add(settingGuiBtn);
        this.add(drawingGuiBtn);
        this.add(stateLabel);
        this.add(entranceField);
        this.add(exitField);
        this.add(confirmBtn);

    }

    public void init(){
        try {
            this.socket = new Socket("127.0.0.1", 8001);
            printWriter = new PrintWriter(socket.getOutputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter.println(Constant.PREFIX + 0 + Constant.SPLIT + 0 + Constant.QR_SUFFIX);
            printWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getGuiInstance(Main main, SchedulingGui schedulingGui, DrawingGui drawingGui) {
        schedulingGuiBtn.addActionListener(e -> Common.changePanel(main, schedulingGui));
        drawingGuiBtn.addActionListener(e -> Common.changePanel(main, drawingGui));
    }
}
