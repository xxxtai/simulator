package com.xxxtai.simulator.view;

import com.xxxtai.express.constant.Orientation;
import com.xxxtai.express.model.Car;
import com.xxxtai.express.model.Edge;
import com.xxxtai.express.model.Graph;
import com.xxxtai.express.view.DrawingGraph;
import com.xxxtai.simulator.model.AGVCar;
import com.xxxtai.express.constant.State;
import com.xxxtai.express.view.RoundButton;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j(topic = "develop")
@Component
public class SchedulingGui extends JPanel {
    private static final long serialVersionUID = 1L;

    private int safeDistance;

    private JLabel stateLabel;

    private Timer heartBeat;

    private boolean stopAGVS;

    private boolean showNums = true;

    private int entranceBorder1;

    private int entranceBorder2;

    @Resource
    private DrawingGraph drawingGraph;

    @Resource
    private Graph graph;

    private List<Car> AGVArray;

    private SchedulingGui() {
        AGVArray = new ArrayList<>();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        RoundButton startAGVSBtn = new RoundButton("启动AGVS");
        startAGVSBtn.setBounds(0, 0, screenSize.width / 4, screenSize.height / 22);
        startAGVSBtn.addActionListener(e -> {
            for (Car car : AGVArray) {
                car.setState(State.FORWARD);
            }
        });

        RoundButton stopAGVSBtn = new RoundButton("停止AGVS");
        stopAGVSBtn.setBounds(screenSize.width / 4, 0, screenSize.width / 4, screenSize.height / 22);
        stopAGVSBtn.addActionListener(e -> stopAGVS = true);

        RoundButton drawingGuiBtn = new RoundButton("重新启动AGVS");
        drawingGuiBtn.setBounds(2 * screenSize.width / 4, 0, screenSize.width / 4, screenSize.height / 22);
        drawingGuiBtn.addActionListener(e -> stopAGVS = false);

        RoundButton showNumBtn = new RoundButton("显示卡号");
        showNumBtn.setBounds(3 * screenSize.width / 4, 0, screenSize.width / 4, screenSize.height / 22);
        showNumBtn.addActionListener(e -> showNums = !showNums);

        stateLabel = new JLabel();
        stateLabel.setBounds(0, 24 * screenSize.height / 26, screenSize.width, screenSize.height / 26);
        stateLabel.setFont(new Font("宋体", Font.BOLD, 20));
        stateLabel.setForeground(Color.RED);

        this.setLayout(null);
        this.add(startAGVSBtn);
        this.add(stopAGVSBtn);
        this.add(drawingGuiBtn);
        this.add(showNumBtn);
        this.add(stateLabel);
    }

    public void init(ApplicationContext context) {
        this.safeDistance = (graph.getNodeMap().get(2).x - graph.getNodeMap().get(1).x)/2 - 20;
        log.info("safeDistance:" + safeDistance);
        if (!graph.getEntranceMap().isEmpty()) {
            Edge edge = graph.getEdgeMap().get(graph.getEntranceMap().keySet().iterator().next());
            if (edge.startNode.x > edge.endNode.x) {
                this.entranceBorder1 = edge.endNode.x;
                this.entranceBorder2 = edge.startNode.x;
            } else {
                this.entranceBorder1 = edge.startNode.x;
                this.entranceBorder2 = edge.endNode.x;
            }
        }
        for (Map.Entry<Integer, Integer> entry : graph.getAGVSPosition().entrySet()) {
            AGVArray.add(context.getBean(AGVCar.class));
            AGVArray.get(AGVArray.size() - 1).init(entry.getKey(), entry.getValue());
        }
        heartBeat = new Timer(50, new RepaintTimerListener());
        heartBeat.start();
        new Timer(100, new DetectCollideTimerListener()).start();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        drawingGraph.drawingMap(g, DrawingGraph.Style.SIMULATOR, showNums);
        drawingGraph.drawingAGV(g, AGVArray, this);
    }

    class RepaintTimerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            repaint();
            for (Car car : AGVArray) {
                if (car.getState().equals(State.FORWARD)) {
                    if (!stopAGVS) {
                        car.stepByStep();
                    }
                }
            }
        }
    }

    class DetectCollideTimerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < AGVArray.size(); i++) {
                AGVCar car1 = (AGVCar) AGVArray.get(i);
                if (!car1.isOnDuty() || (car1.getX() > entranceBorder1 && car1.getX() < entranceBorder2)) {
                    continue;
                }
                for (int j = i + 1; j < AGVArray.size(); j++) {
                    AGVCar car2 = (AGVCar) AGVArray.get(j);
                    if (!car2.isOnDuty() || (car2.getX() > entranceBorder1 && car2.getX() < entranceBorder2)) {
                        continue;
                    }
                    if ((Math.abs(car1.getX() - car2.getX()) + Math.abs(car1.getY() - car2.getY())) < safeDistance) {
                        car1.setState(State.COLLIED);
                        car2.setState(State.COLLIED);
                        stateLabel.setText(car1.getAGVNum() + "AGV 和 " + car2.getAGVNum() + "AGV相撞");
                        stopAGVS = true;
                    }
                }
            }

            for (int i = 0; i < AGVArray.size(); i++) {
                boolean detected = false;
                AGVCar car1 = (AGVCar) AGVArray.get(i);
                if (car1.getX() < entranceBorder1 && car1.getX() > entranceBorder2) {
                    continue;
                }
                for (int j = 0; j < AGVArray.size(); j++) {
                    AGVCar car2 = (AGVCar) AGVArray.get(j);
                    if (i == j || (car2.getX() < entranceBorder1 && car2.getX() > entranceBorder2) ||
                            car2.getY() != car1.getY()) {
                        continue;
                    }
                    if (Math.abs(car1.getX() - car2.getX()) < safeDistance) {
                        if (car1.getOrientation().equals(Orientation.RIGHT) && car1.getX() < car2.getX()) {
                            detected = true;
                        } else if(car1.getOrientation().equals(Orientation.LEFT) && car1.getX() > car2.getX()){
                            detected = true;
                        }
                    }
                }
                if (detected && car1.getState().equals(State.FORWARD)) {
                    car1.setState(State.INFRARED_ANOMALY);
                    stateLabel.setText(car1.getAGVNum() + "AGV红外异常");
                } else if (!detected && car1.getState().equals(State.INFRARED_ANOMALY)){
                    car1.setState(State.FORWARD);
                }
            }
        }
    }
}
