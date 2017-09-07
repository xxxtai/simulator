package com.xxxtai.view;

import com.xxxtai.main.Main;
import com.xxxtai.model.AGVCar;
import com.xxxtai.model.Graph;
import com.xxxtai.myToolKit.Common;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;


@Component
public class SchedulingGui extends JPanel{
	private static final long serialVersionUID = 1L;

	private RoundButton settingGuiBtn;

	private RoundButton drawingGuiBtn;

	@Resource
	private Graph graph;

	@Resource
	private DrawingGraph drawingGraph;
	
	private ArrayList<AGVCar> AGVArray;
	
	private Timer timer;

	private SchedulingGui(){
		System.out.println("SchedulingGui");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		RoundButton schedulingGuiBtn = new RoundButton("调度界面");
		schedulingGuiBtn.setBounds(0, 0, screenSize.width/3, screenSize.height/20);
		schedulingGuiBtn.setForeground(new Color(30, 144, 255));
		schedulingGuiBtn.setBackground(Color.WHITE);
		
		settingGuiBtn = new RoundButton("设置界面");
		settingGuiBtn.setBounds(screenSize.width/3, 0, screenSize.width/3, screenSize.height/20);

		drawingGuiBtn = new RoundButton("制图界面");
		drawingGuiBtn.setBounds(2*screenSize.width/3, 0, screenSize.width/3, screenSize.height/20);

		JLabel stateLabel = new JLabel();
		stateLabel.setBounds(0, 22*screenSize.height/25, screenSize.width, screenSize.height/25);
		stateLabel.setFont(new Font("宋体", Font.BOLD, 25));

		this.setLayout(null);
		this.add(schedulingGuiBtn);
		this.add(settingGuiBtn);
		this.add(drawingGuiBtn);
		this.add(stateLabel);
		
		this.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if(e.getButton() == MouseEvent.BUTTON1){
					int x = e.getX();
					int y = e.getY();
					for(AGVCar car: AGVArray){
						if(Math.abs(x - car.getX()) < 40 && Math.abs(y - car.getY()) < 40){
							car.changeState();
						}
					}
				}
			}
		});
		AGVArray = new ArrayList<>();
		timer = new Timer(50, new RepaintTimerListener());
	}
	
	public void init(ApplicationContext context){
		
		for(int i = 0; i < 3; i++){
			AGVArray.add(context.getBean(AGVCar.class));
			AGVArray.get(i).init(i+1);
		}		
		timer.start();
		
	}
	@Override
	public void paint(Graphics g){
		super.paint(g);
		drawingGraph.drawingMap(g, graph);
		drawingGraph.drawingCar(g, graph, AGVArray, this);	
	}
	
	class RepaintTimerListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			repaint();
			for(AGVCar car : AGVArray){
				car.stepByStep();
				car.heartBeat();
			}
		}
	}
	
	public void getGuiInstance(Main main, SettingGui settingGui, DrawingGui drawingGui){
		settingGuiBtn.addActionListener(e -> Common.changePanel(main, settingGui));
		drawingGuiBtn.addActionListener(e -> Common.changePanel(main, drawingGui));
	}
}
