package com.xxxtai.simulator.model;

import com.xxxtai.express.constant.*;
import com.xxxtai.express.controller.TrafficControl;
import com.xxxtai.express.model.Car;
import com.xxxtai.express.model.Edge;
import com.xxxtai.express.model.Exit;
import com.xxxtai.express.model.Graph;
import com.xxxtai.simulator.netty.NettyClientBootstrap;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j(topic = "develop")
public class AGVCar implements Car{
    private @Getter
    int AGVNum;
    private Orientation orientation = Orientation.RIGHT;
    private Point position;
    private boolean finishEdge;
    private @Getter
    State state = State.STOP;
    private @Getter
    Edge atEdge;
    private String destination;
    private boolean onDuty;
    private boolean isFirstInquire = true;
    private int detectCardNum;
    private int lastDetectCardNum;
    private Map<Integer, Integer> cardCommandMap;
    private int stopCardNum;
    private long lastCommunicationTime;
    private long count_3s;
    @Resource
    private Graph graph;

    private SocketChannel socketChannel;

    public AGVCar() {
        this.position = new Point(-100, -100);
        this.lastCommunicationTime = System.currentTimeMillis();
    }

    public void init(int AGVNum, int positionCardNum) {
        this.AGVNum = AGVNum;
        this.cardCommandMap = new HashMap<>();
        setAtEdge(graph.getEdgeMap().get(positionCardNum));

        try {
            NettyClientBootstrap bootstrap = new NettyClientBootstrap(8899, "127.0.0.1", this);
            socketChannel = bootstrap.start();
            heartBeat(AGVNum);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setReceiveCardNum(int i) {

    }

    private void setAtEdge(Edge edge) {
        this.atEdge = edge;
        this.position.x = this.atEdge.startNode.x;
        this.position.y = this.atEdge.startNode.y;
        this.finishEdge = false;
        judgeOrientation();
    }

    public void stepByStep() {
        if (!finishEdge && (state == State.FORWARD || state == State.BACKWARD)
                && this.atEdge != null) {
            int FORWARD_PIx = 3;
            if (this.atEdge.startNode.x == this.atEdge.endNode.x) {
                if (this.atEdge.startNode.y < this.atEdge.endNode.y) {
                    if (this.position.y < this.atEdge.endNode.y) {
                        this.position.y += FORWARD_PIx;
                    } else {
                        this.finishEdge = true;
                    }
                } else if (atEdge.startNode.y > atEdge.endNode.y) {
                    if (this.position.y > this.atEdge.endNode.y) {
                        this.position.y -= FORWARD_PIx;
                    } else {
                        this.finishEdge = true;
                    }
                }
            } else if (this.atEdge.startNode.y == this.atEdge.endNode.y) {
                if (this.atEdge.startNode.x < this.atEdge.endNode.x) {
                    if (this.position.x < this.atEdge.endNode.x) {
                        this.position.x += FORWARD_PIx;
                    } else {
                        this.finishEdge = true;
                    }
                } else if (this.atEdge.startNode.x > this.atEdge.endNode.x) {
                    if (this.position.x > this.atEdge.endNode.x) {
                        this.position.x -= FORWARD_PIx;
                    } else {
                        this.finishEdge = true;
                    }
                }
            }
        }

        int cardNum = detectRFIDCard();
        if (cardNum != 0 && cardNum != this.detectCardNum) {
            sendReadCardToSystem(this.AGVNum, cardNum);
            this.lastDetectCardNum = this.detectCardNum;
            this.detectCardNum = cardNum;
            log.info(this.AGVNum + "AGV detectRFIDCard:" + cardNum);
            if (cardNum == this.stopCardNum) {
                this.state = State.STOP;
                sendStateToSystem(this.AGVNum, State.STOP.getValue());
                sendStateToSystem(this.AGVNum, State.UNLOADED.getValue());
                this.finishedDuty();
            }
        }

        if (this.finishEdge && this.isFirstInquire && this.cardCommandMap.get(this.lastDetectCardNum) != null) {
            if (!swerve(this.cardCommandMap.get(this.lastDetectCardNum))) {
                this.state = State.STOP;
                sendStateToSystem(this.AGVNum, State.STOP.getValue());
            } else {
                this.cardCommandMap.remove(this.lastDetectCardNum);
            }
        }
    }

    public void heartBeat() {
        if (this.count_3s == 60) {
            this.count_3s = 0;
            heartBeat(this.AGVNum);
        } else {
            this.count_3s++;
        }
    }

    private void judgeOrientation() {
        if (atEdge.startNode.x == atEdge.endNode.x) {
            if (atEdge.startNode.y < atEdge.endNode.y) {
                orientation = Orientation.DOWN;
            } else {
                orientation = Orientation.UP;
            }
        } else if (atEdge.startNode.y == atEdge.endNode.y) {
            if (atEdge.startNode.x < atEdge.endNode.x) {
                orientation = Orientation.RIGHT;
            } else {
                orientation = Orientation.LEFT;
            }
        }
    }

    private boolean patrolLine(Orientation orientation) {
        boolean isFound = false;
        for (Edge e : graph.getEdgeArray()) {
            if (this.atEdge.endNode.cardNum.equals(e.startNode.cardNum) && !this.atEdge.startNode.cardNum.equals(e.endNode.cardNum)) {
                if ((orientation == Orientation.RIGHT && e.startNode.y == e.endNode.y && e.startNode.x < e.endNode.x)
                        || (orientation == Orientation.DOWN && e.startNode.x == e.endNode.x && e.startNode.y < e.endNode.y)
                        || (orientation == Orientation.LEFT && e.startNode.y == e.endNode.y && e.startNode.x > e.endNode.x)
                        || (orientation == Orientation.UP && e.startNode.x == e.endNode.x && e.startNode.y > e.endNode.y)) {
                    setAtEdge(e);
                    isFound = true;
                    break;
                }
            } else if (this.atEdge.endNode.cardNum.equals(e.endNode.cardNum) && !this.atEdge.startNode.cardNum.equals(e.startNode.cardNum)) {
                if ((orientation == Orientation.RIGHT && e.startNode.y == e.endNode.y && e.startNode.x > e.endNode.x)
                        || (orientation == Orientation.DOWN && e.startNode.x == e.endNode.x && e.startNode.y > e.endNode.y)
                        || (orientation == Orientation.LEFT && e.startNode.y == e.endNode.y && e.startNode.x < e.endNode.x)
                        || (orientation == Orientation.UP && e.startNode.x == e.endNode.x && e.startNode.y < e.endNode.y)) {
                    setAtEdge(new Edge(e.endNode, e.startNode, graph.getNodeMap().get(e.cardNum), e.realDistance));
                    isFound = true;
                    break;
                }
            }
        }
        return isFound;
    }

    private boolean swerve(Integer commandValue) {//1、左转；2、右转；3、前进
        boolean isFound = false;
        this.isFirstInquire = false;
        if (commandValue == Command.TURN_LEFT.getValue()) {
            switch (this.orientation) {
                case RIGHT:
                    isFound = patrolLine(Orientation.UP);
                    break;
                case LEFT:
                    isFound = patrolLine(Orientation.DOWN);
                    break;
                case UP:
                    isFound = patrolLine(Orientation.LEFT);
                    break;
                case DOWN:
                    isFound = patrolLine(Orientation.RIGHT);
                    break;
            }
        } else if (commandValue == Command.TURN_RIGHT.getValue()) {
            switch (this.orientation) {
                case RIGHT:
                    isFound = patrolLine(Orientation.DOWN);
                    break;
                case LEFT:
                    isFound = patrolLine(Orientation.UP);
                    break;
                case UP:
                    isFound = patrolLine(Orientation.RIGHT);
                    break;
                case DOWN:
                    isFound = patrolLine(Orientation.LEFT);
                    break;
            }
        } else if (commandValue == Command.GO_AHEAD.getValue()) {
            isFound = patrolLine(this.orientation);
        }
        if (isFound) {
            this.isFirstInquire = true;
            this.state = State.FORWARD;
            sendStateToSystem(this.AGVNum, State.FORWARD.getValue());
        }
        return isFound;
    }

    private int detectRFIDCard() {
        int foundCard = 0;
        if (Math.abs(this.position.x - this.atEdge.CARD_POSITION.x) < 4 && Math.abs(this.position.y - this.atEdge.CARD_POSITION.y) < 4)
            foundCard = this.atEdge.cardNum;

        if (Math.abs(this.position.x - this.atEdge.startNode.x) < 4 && Math.abs(this.position.y - this.atEdge.startNode.y) < 4)
            foundCard = this.atEdge.startNode.cardNum;

        if (Math.abs(this.position.x - this.atEdge.endNode.x) < 4 && Math.abs(this.position.y - this.atEdge.endNode.y) < 4)
            foundCard = this.atEdge.endNode.cardNum;

        return foundCard;
    }

    public void setCardCommandMap(String commandString) {
        String[] commandArray = commandString.split(Constant.SPLIT);
        String[] stopCardNumIsBackward = commandArray[commandArray.length - 1].split(Constant.SUB_SPLIT);
        if (Constant.USE_SERIAL) {
            stopCardNum = graph.getSerialNumMap().get(stopCardNumIsBackward[0]);
        } else {
            stopCardNum = Integer.parseInt(stopCardNumIsBackward[0]);
        }

        if (stopCardNumIsBackward[1].equals(Constant.BACKWARD)) {
            turnAround();
            this.atEdge = new Edge(this.atEdge.endNode, this.atEdge.startNode, graph.getNodeMap().get(this.atEdge.cardNum), this.atEdge.realDistance);
        }
        onDuty = true;
        for (Map.Entry<Long, List<Exit>> entry : graph.getExitMap().entrySet()) {
            for (Exit exit : entry.getValue()){
                for (int cardNum : exit.getExitNodeNums()){
                    if (cardNum == stopCardNum){
                        destination = City.valueOfCode(entry.getKey()).getName();
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < commandArray.length - 1; i++) {
            if (Constant.USE_SERIAL) {
                this.cardCommandMap.put(graph.getSerialNumMap().get(commandArray[i].substring(0, 8)),
                        Integer.parseInt(commandArray[i].substring(10, 12),16));
            } else {
                String[] c = commandArray[i].split(Constant.SUB_SPLIT);
                this.cardCommandMap.put(Integer.parseInt(c[0]), Integer.parseInt(c[1],16));
            }
        }
        this.state = State.FORWARD;
        sendStateToSystem(this.AGVNum, State.FORWARD.getValue());
    }

    private void turnAround(){
        if (this.orientation == Orientation.RIGHT) {
            this.orientation = Orientation.LEFT;
        } else if (this.orientation == Orientation.LEFT) {
            this.orientation = Orientation.RIGHT;
        } else if (this.orientation == Orientation.UP) {
            this.orientation = Orientation.DOWN;
        } else if (this.orientation == Orientation.DOWN) {
            this.orientation = Orientation.UP;
        }
    }

    public void finishedDuty(){
        onDuty = false;
        destination = null;
        stopCardNum = 0;
    }

    public Orientation getOrientation() {
        return this.orientation;
    }

    public long getLastCommunicationTime() {
        return this.lastCommunicationTime;
    }

    public void setLastCommunicationTime(long time) {
        this.lastCommunicationTime = time;
    }

    private void sendReadCardToSystem(int AGVNum, int cardNum) {
        if (Constant.USE_SERIAL) {
            this.socketChannel.writeAndFlush(Constant.CARD_PREFIX  +  graph.getCardNumMap().get(cardNum) + Constant.SUFFIX);
        } else {
            this.socketChannel.writeAndFlush(Constant.CARD_PREFIX  +  cardNum + Constant.SUFFIX);
        }
    }

    private void sendStateToSystem(int AGVNum, int state){
        this.socketChannel.writeAndFlush(Constant.STATE_PREFIX + Integer.toHexString(state) + Constant.SUFFIX);
    }

    private void heartBeat(int AGVNum){
        this.socketChannel.writeAndFlush(Constant.HEART_PREFIX + Integer.toHexString(AGVNum) + Constant.SUFFIX);
    }

    @Override
    public int getX(){
        return position.x;
    }

    @Override
    public int getY(){
        return position.y;
    }

    @Override
    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public SocketChannel getSocketChannel() {
        return this.socketChannel;
    }

    @Override
    public void setState(State state) {
        this.state = state;
        sendStateToSystem(AGVNum, this.state.getValue());
    }

    @Override
    public TrafficControl getTrafficControl() {
        return null;
    }

    @Override
    public int getReadCardNum() {
        return 0;
    }

    @Override
    public boolean isOnDuty() {
        return onDuty;
    }

    @Override
    public boolean isOnEntrance() {
        return false;
    }

    @Override
    public String getDestination() {
        return destination;
    }

    @Override
    public int getStopCardNum() {
        return 0;
    }

    @Override
    public void setExecutiveCommand(Command command) {}

    @Override
    public void sendMessageToAGV(String s) {}

    @Override
    public void setRouteNodeNumArray(List<Integer> list) {}
}
