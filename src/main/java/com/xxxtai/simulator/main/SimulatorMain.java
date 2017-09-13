package com.xxxtai.simulator.main;

import com.xxxtai.express.view.DrawingGui;
import com.xxxtai.express.view.SettingGui;
import com.xxxtai.simulator.view.SchedulingGui;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Component
@Slf4j(topic = "develop")
public class SimulatorMain extends JFrame {

    private static final long serialVersionUID = 1L;
    @Resource
    private SchedulingGui schedulingGui;
    @Resource
    private SettingGui settingGui;
    @Resource
    private DrawingGui graphingGui;

    public SimulatorMain() {
        super("模拟多AGV场景");

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setExtendedState(Frame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //exit();
            }
        });
    }

    private void init(ApplicationContext context) {
        graphingGui.getGuiInstance(SimulatorMain.this, schedulingGui, settingGui);
        settingGui.getGuiInstance(SimulatorMain.this, schedulingGui, graphingGui);
        schedulingGui.getGuiInstance(SimulatorMain.this, settingGui, graphingGui);
        schedulingGui.init(context);

        this.getContentPane().add(schedulingGui);
        this.repaint();
        this.validate();
    }


    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("/META-INF/spring/beans.xml");
        SimulatorMain simulatorMain = context.getBean(SimulatorMain.class);
        simulatorMain.init(context);
    }

    public void exit() {
        Object[] option = {"确认", "取消"};
        JOptionPane pane = new JOptionPane("确认关闭吗？", JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION, null, option, option[1]);
        JDialog dialog = pane.createDialog(this, "警告");
        dialog.setVisible(true);
        Object result = pane.getValue();
        if (result == null || result == option[1]) {
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        } else if (result == option[0]) {
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
    }
}
