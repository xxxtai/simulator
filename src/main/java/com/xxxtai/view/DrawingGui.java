package com.xxxtai.view;

import com.xxxtai.main.Main;
import com.xxxtai.myToolKit.Common;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class DrawingGui extends JPanel {

    private RoundButton schedulingGuiBtn;
    private RoundButton settingGuiBtn;

    public DrawingGui() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        schedulingGuiBtn = new RoundButton("调度界面");
        schedulingGuiBtn.setBounds(0, 0, screenSize.width / 3, screenSize.height / 20);

        settingGuiBtn = new RoundButton("设置界面");
        settingGuiBtn.setBounds(screenSize.width / 3, 0, screenSize.width / 3, screenSize.height / 20);

        RoundButton drawingGuiBtn = new RoundButton("制图界面");
        drawingGuiBtn.setBounds(2 * screenSize.width / 3, 0, screenSize.width / 3, screenSize.height / 20);
        drawingGuiBtn.setForeground(new Color(30, 144, 255));
        drawingGuiBtn.setBackground(Color.WHITE);

        JLabel stateLabel = new JLabel();
        stateLabel.setBounds(0, 22 * screenSize.height / 25, screenSize.width, screenSize.height / 25);
        stateLabel.setFont(new Font("宋体", Font.BOLD, 25));


        this.setLayout(null);
        this.add(schedulingGuiBtn);
        this.add(settingGuiBtn);
        this.add(drawingGuiBtn);
        this.add(stateLabel);


    }


    public void getGuiInstance(Main main, SchedulingGui schedulingGui, SettingGui settingGui) {
        schedulingGuiBtn.addActionListener(e -> Common.changePanel(main, schedulingGui));
        settingGuiBtn.addActionListener(e -> Common.changePanel(main, settingGui));
    }

}
