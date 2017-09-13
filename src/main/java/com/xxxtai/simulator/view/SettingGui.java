package com.xxxtai.simulator.view;

import com.xxxtai.simulator.main.SimulatorMain;
import com.xxxtai.express.toolKit.Common;
import com.xxxtai.express.toolKit.MyTextField;
import com.xxxtai.express.view.RoundButton;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class SettingGui extends JPanel {
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
        confirmBtn.addActionListener(e -> {});

        this.setLayout(null);
        this.add(schedulingGuiBtn);
        this.add(settingGuiBtn);
        this.add(drawingGuiBtn);
        this.add(stateLabel);
        this.add(entranceField);
        this.add(exitField);
        this.add(confirmBtn);

    }

    public void getGuiInstance(SimulatorMain simulatorMain, SchedulingGui schedulingGui, DrawingGui drawingGui) {
        schedulingGuiBtn.addActionListener(e -> Common.changePanel(simulatorMain, schedulingGui));
        drawingGuiBtn.addActionListener(e -> Common.changePanel(simulatorMain, drawingGui));
    }
}
