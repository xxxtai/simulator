package com.xxxtai.model;

import java.awt.*;

public class Edge {
    public final Node START_NODE;
    public final Node END_NODE;
    final int REAL_DISTANCE;
    final int CARD_NUM;
    final Point CARD_POSITION;

    Edge(Node str, Node end, int dis, int CardNum) {
        START_NODE = str;
        END_NODE = end;
        REAL_DISTANCE = dis;
        CARD_NUM = CardNum;
        if (this.START_NODE.X == this.END_NODE.X) {
            this.CARD_POSITION = new Point(this.START_NODE.X, (this.START_NODE.Y + this.END_NODE.Y) / 2);
        } else {
            this.CARD_POSITION = new Point((this.START_NODE.X + this.END_NODE.X) / 2, this.START_NODE.Y);
        }
    }
}
