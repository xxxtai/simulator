package com.xxxtai.model;

import com.xxxtai.controller.AGVCpuRunnable;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class AGVCar {
	@Resource
	private AGVCpuRunnable cpuRunnable;
	@Resource
	private Graph graph;
	private int AGVNum;
	public enum Orientation{RIGTH,DOWN,LEFT,UP}
	public enum State{STOP, FORWARD, BACKWARD, SHIPMENT, UNLOADING, NULL}
	private Orientation orientation = Orientation.RIGTH;
	private ExecutorService executor;
	private Point position;
	private boolean finishEdge;
	private State state = State.STOP;
	private Edge atEdge;
	private final int FORWARDPIX = 3;
	private boolean isFirstInquire = true;
	private int detectCardNum;
	private int lastDetectCardNum;
	private Map<Integer, Integer> cardCommandMap;
	private int stopCardNum;
	private boolean firstInit = true;
	private long lastCommunicationTime;
	private long count_3s;
	
	public AGVCar(){
		this.executor = Executors.newSingleThreadExecutor();
		this.position = new Point(-100,-100);
		this.lastCommunicationTime = System.currentTimeMillis();
	}

	
	public void init(int AGVNum){
		this.AGVNum = AGVNum;		
		this.cardCommandMap = new HashMap<>();
		this.cpuRunnable.setCarModelToCpu(this);		
		if(this.cpuRunnable.connect()){
			this.executor.execute(this.cpuRunnable);
			this.cpuRunnable.sendReadCardToSystem(AGVNum, 0, 2);
		}
		if(AGVNum == 1)
			setAtEdge(graph.getEdge(0));
		
		if(AGVNum == 2)
			setAtEdge(graph.getEdge(3));
		
		if(AGVNum == 3)
			setAtEdge(graph.getEdge(6));
	}
	
	public void setAtEdge(Edge edge){	
		this.atEdge = edge;
		this.position.x = this.atEdge.START_NODE.X;
		this.position.y = this.atEdge.START_NODE.Y;		
		this.finishEdge = false;
		judgeOrientation();
	}
	public void stepByStep(){			
		if(!finishEdge&& (state == State.FORWARD || state == State.BACKWARD)
				&& this.atEdge != null){				
			if(this.atEdge.START_NODE.X == this.atEdge.END_NODE.X){
				if(this.atEdge.START_NODE.Y < this.atEdge.END_NODE.Y ){
					if(this.position.y < this.atEdge.END_NODE.Y){
						this.position.y +=this.FORWARDPIX;
					}else{
						this.finishEdge = true;
					}						
				}else if(atEdge.START_NODE.Y > atEdge.END_NODE.Y ){
					if(this.position.y > this.atEdge.END_NODE.Y){
						this.position.y -= this.FORWARDPIX;
					}else{
						this.finishEdge = true;
					}
				}
			}else if(this.atEdge.START_NODE.Y == this.atEdge.END_NODE.Y){
				if(this.atEdge.START_NODE.X < this.atEdge.END_NODE.X ){
					if(this.position.x < this.atEdge.END_NODE.X){
						this.position.x += this.FORWARDPIX;
					}else{
						this.finishEdge = true;
					}
				}else if(this.atEdge.START_NODE.X > this.atEdge.END_NODE.X){
					if(this.position.x > this.atEdge.END_NODE.X){
						this.position.x -= this.FORWARDPIX;
					}else{
						this.finishEdge = true;
					}
				}
			}
		}
		
		int cardNum = detectRFIDCard();
		if(cardNum != 0 && cardNum != this.detectCardNum){
			this.lastDetectCardNum = this.detectCardNum;
			this.detectCardNum = cardNum;
			System.out.println(this.AGVNum + "AGV detectRFIDCard:"+cardNum);
			if(cardNum == this.stopCardNum){
				this.state = State.STOP;
				this.cpuRunnable.sendReadCardToSystem(this.AGVNum, cardNum, 2);//1、运行2、停止
			}else{
				this.cpuRunnable.sendReadCardToSystem(this.AGVNum, cardNum, 1);
			}
		}
		
		if(this.finishEdge && this.isFirstInquire && this.cardCommandMap.get(this.lastDetectCardNum)!= null){
			if(!swerve(this.cardCommandMap.get(this.lastDetectCardNum)))
				this.state = State.STOP;
			else
				this.cardCommandMap.remove(this.lastDetectCardNum);
		}
	}
	
	public void heartBeat(){
		if(this.count_3s == 60){
			this.count_3s = 0;
			this.cpuRunnable.sendReadCardToSystem(this.AGVNum, 0, 0);
		}else{
			this.count_3s++;
		}
	}
	
	public void judgeOrientation(){
		if(atEdge.START_NODE.X == atEdge.END_NODE.X){
			if(atEdge.START_NODE.Y < atEdge.END_NODE.Y){
				orientation = Orientation.DOWN;
			}else{
				orientation = Orientation.UP;
			} 	
		}else if(atEdge.START_NODE.Y == atEdge.END_NODE.Y){
			if(atEdge.START_NODE.X < atEdge.END_NODE.X){
				orientation = Orientation.RIGTH;
			}else{
				orientation = Orientation.LEFT;
			} 				
		}
	}
	
	private boolean patrolLine(Orientation orientation){		
		boolean isFound = false;
		for(Edge e : graph.getEdgeArray()){
			if(this.atEdge.END_NODE.CARD_NUM == e.START_NODE.CARD_NUM && this.atEdge.START_NODE.CARD_NUM != e.END_NODE.CARD_NUM){
				if((orientation == Orientation.RIGTH&& e.START_NODE.Y == e.END_NODE.Y && e.START_NODE.X < e.END_NODE.X)
					||(orientation == Orientation.DOWN&& e.START_NODE.X == e.END_NODE.X && e.START_NODE.Y < e.END_NODE.Y)
					||(orientation == Orientation.LEFT && e.START_NODE.Y == e.END_NODE.Y && e.START_NODE.X > e.END_NODE.X)
					||(orientation == Orientation.UP && e.START_NODE.X == e.END_NODE.X && e.START_NODE.Y > e.END_NODE.Y)){
					setAtEdge(e);
					isFound = true;
					break;					
				}				
			}else if(this.atEdge.END_NODE.CARD_NUM == e.END_NODE.CARD_NUM && this.atEdge.START_NODE.CARD_NUM != e.START_NODE.CARD_NUM){
				if((orientation == Orientation.RIGTH&& e.START_NODE.Y == e.END_NODE.Y && e.START_NODE.X > e.END_NODE.X)
					||(orientation == Orientation.DOWN&& e.START_NODE.X == e.END_NODE.X && e.START_NODE.Y > e.END_NODE.Y)
					||(orientation == Orientation.LEFT && e.START_NODE.Y == e.END_NODE.Y && e.START_NODE.X < e.END_NODE.X)
					||(orientation == Orientation.UP && e.START_NODE.X == e.END_NODE.X && e.START_NODE.Y < e.END_NODE.Y)){
					setAtEdge(new Edge(e.END_NODE, e.START_NODE, e.REAL_DISTANCE, e.CARD_NUM));
					isFound = true;
					break;					
				}
			}
		}
		return isFound;
	}
	
	public boolean swerve(int signal){//1、左转；2、右转；3、前进
		boolean isFound = false;
		this.isFirstInquire = false;
		if(signal == 1){
			switch(this.orientation){
			case RIGTH:	isFound = patrolLine(Orientation.UP);
				break;
			case LEFT:	isFound = patrolLine(Orientation.DOWN);
				break;
			case UP:	isFound = patrolLine(Orientation.LEFT);
				break;
			case DOWN: 	isFound = patrolLine(Orientation.RIGTH);
				break;
			}
		}else if(signal == 2){
			switch(this.orientation){
			case RIGTH:	isFound = patrolLine(Orientation.DOWN);
				break;
			case LEFT:  isFound = patrolLine(Orientation.UP);
				break;
			case UP:    isFound = patrolLine(Orientation.RIGTH);
				break;
			case DOWN:  isFound = patrolLine(Orientation.LEFT);
				break;
			}
		}else if(signal == 3){
			isFound = patrolLine(this.orientation);
		}
		if(isFound){
			this.isFirstInquire = true;
			this.state = State.FORWARD;
		}
		return isFound;		
	}
	
	public int detectRFIDCard(){
		int foundCard = 0;	
		if(Math.abs(this.position.x - this.atEdge.CARD_POSITION.x) < 4 && Math.abs(this.position.y - this.atEdge.CARD_POSITION.y) < 4)
			foundCard = this.atEdge.CARD_NUM;	
		
		if(Math.abs(this.position.x - this.atEdge.START_NODE.X) < 4 && Math.abs(this.position.y - this.atEdge.START_NODE.Y) < 4)
			foundCard = this.atEdge.START_NODE.CARD_NUM;	
		
		if(Math.abs(this.position.x - this.atEdge.END_NODE.X) < 4 && Math.abs(this.position.y - this.atEdge.END_NODE.Y) < 4)
			foundCard = this.atEdge.END_NODE.CARD_NUM;	

		return foundCard;
	}
	
	public void setCardCommandMap(String str){
		this.stopCardNum = Integer.parseInt(str.substring(str.length()-2, str.length()), 16);
		String commandString = str.substring(0, str.length()-2);
		String[] commandArray = commandString.split("FF");
		for(String s : commandArray){
			System.out.println(s);
			this.cardCommandMap.put(Integer.parseInt(s.substring(0, 2), 16), Integer.parseInt(s.substring(2, 4),16));
		}
		if(this.firstInit){
			swerve(this.cardCommandMap.get(this.lastDetectCardNum));
			this.firstInit = false;
		}else {
			this.state = State.FORWARD;
		}
		
	}
	public void changeState(){
		if(this.state == State.FORWARD || this.state == State.BACKWARD){
			this.cpuRunnable.sendReadCardToSystem(AGVNum, 0, 2);
			this.state = State.STOP;
		}else if(this.state == State.STOP){
			this.state = State.FORWARD;
			this.cpuRunnable.sendReadCardToSystem(AGVNum, 0, 1);
		}
	}
	
	public void stopTheAGV(){
		this.state = State.STOP;
		this.cpuRunnable.sendReadCardToSystem(AGVNum, 0, 2);
	}
	
	public void startTheAGV(){
		this.state = State.FORWARD;
		this.cpuRunnable.sendReadCardToSystem(AGVNum, 0, 1);
	}
	
	public int getNum(){
		return this.AGVNum;
	}
	
	public int getX(){
		return this.position.x;
	}
	
	public int getY(){
		return this.position.y;
	}
	
	public Orientation getOrientation(){
		return this.orientation;
	}
	
	public long getLastCommunicationTime(){
		return this.lastCommunicationTime;
	}
	
	public void setLastCommunicationTime(long time){
		this.lastCommunicationTime = time;
	}
	
	public void setNewCpuRunnable(){
		this.cpuRunnable = new AGVCpuRunnable();
		this.cpuRunnable.setCarModelToCpu(this);		
		if(this.cpuRunnable.connect()){
			this.executor.execute(this.cpuRunnable);
			this.cpuRunnable.sendReadCardToSystem(AGVNum, 0, 2);
		}
	}
}
