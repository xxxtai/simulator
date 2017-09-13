package com.xxxtai.simulator.view;

import com.xxxtai.express.model.Edge;
import com.xxxtai.simulator.model.AGVCar;
import com.xxxtai.simulator.model.Graph;
import com.xxxtai.express.constant.Orientation;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

@Component
public class DrawingGraph {
    private static Image leftImageG;
    private Image rightImageG;
    private Image upImageG;
    private Image downImageG;
    private Image leftImageR;
    private Image rightImageR;
    private Image upImageR;
    private Image downImageR;

    public DrawingGraph() {
        Toolkit tool = Toolkit.getDefaultToolkit();
        leftImageG = tool.createImage(getClass().getResource("/images/leftImage.png"));
        rightImageG = tool.createImage(getClass().getResource("/images/rightImage.png"));
        upImageG = tool.createImage(getClass().getResource("/images/upImage.png"));
        downImageG = tool.createImage(getClass().getResource("/images/downImage.png"));
        leftImageR = tool.createImage(getClass().getResource("/images/leftImage2.png"));
        rightImageR = tool.createImage(getClass().getResource("/images/rightImage2.png"));
        upImageR = tool.createImage(getClass().getResource("/images/upImage2.png"));
        downImageR = tool.createImage(getClass().getResource("/images/downImage2.png"));


    }

    void drawingMap(Graphics g, Graph graph) {
        ((Graphics2D) g).setStroke(new BasicStroke(6.0f));
        g.setFont(new Font("宋体", Font.BOLD, 20));

        for (Edge e : graph.getEdgeArray()) {
            g.setColor(Color.BLACK);
            g.drawLine(e.startNode.x, e.startNode.y, e.endNode.x, e.endNode.y);
        }

        for (int i = 0; i < graph.getNodeArraySize(); i++) {
            g.setColor(Color.YELLOW);
            g.fillRect(graph.getNode(i).x - 5, graph.getNode(i).y - 5, 10, 10);
            g.setColor(Color.BLUE);
            g.drawString(String.valueOf(graph.getNode(i).cardNum), graph.getNode(i).x + 10, graph.getNode(i).y + 25);
        }
    }


    void drawingCar(Graphics g, ArrayList<AGVCar> AGVArray, JPanel panel) {
        g.setColor(Color.black);
        g.setFont(new Font("Dialog", Font.BOLD, 25));
        for (int i = 0; i < AGVArray.size(); i++) {
            g.setColor(Color.black);
            if (AGVArray.get(i).getOrientation() == Orientation.LEFT) {
                g.drawImage(leftImageG, AGVArray.get(i).getX() - 20, AGVArray.get(i).getY() - 17, 40, 34, panel);
                g.drawString(String.valueOf(i + 1), AGVArray.get(i).getX(), AGVArray.get(i).getY() + 9);
            } else if (AGVArray.get(i).getOrientation() == Orientation.RIGHT) {
                g.drawImage(rightImageG, AGVArray.get(i).getX() - 20, AGVArray.get(i).getY() - 17, 40, 34, panel);
                g.drawString(String.valueOf(i + 1), AGVArray.get(i).getX() - 10, AGVArray.get(i).getY() + 9);
            } else if (AGVArray.get(i).getOrientation() == Orientation.UP) {
                g.drawImage(upImageG, AGVArray.get(i).getX() - 17, AGVArray.get(i).getY() - 20, 34, 40, panel);
                g.drawString(String.valueOf(i + 1), AGVArray.get(i).getX() - 10, AGVArray.get(i).getY() + 10);
            } else if (AGVArray.get(i).getOrientation() == Orientation.DOWN) {
                g.drawImage(downImageG, AGVArray.get(i).getX() - 17, AGVArray.get(i).getY() - 20, 34, 40, panel);
                g.drawString(String.valueOf(i + 1), AGVArray.get(i).getX() - 5, AGVArray.get(i).getY() + 5);
            }
        }
    }


}
