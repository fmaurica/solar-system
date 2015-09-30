package edu.solarsystem.model;

import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

public class Movable extends Observable {	
	
	private enum Type {CIRCULAR, ELLIPTIC}
	private Type type;
	
	private double r;
	private double a, b;
	private double rotangle;

	private double w0;
	private double theta, theta0;
	private double x, y;
	
	private double centerx, centery;
	
	private static final int UPDATE_INTERVAL = 100; //ms
	
	public Movable(double centerx, double centery, double r, double theta0, double w0) {
		super();
		this.type = Type.CIRCULAR;
		this.centerx = centerx;
		this.centery = centery;
		this.r = r;
		this.theta0 = theta0;
		this.w0 = w0;
	}
	public Movable(double centerx, double centery, double a, double b, double rotangle, double theta0, double w0) {
		super();
		this.type = Type.ELLIPTIC;
		this.centerx = centerx;
		this.centery = centery;
		this.a = a;
		this.b = b;
		this.rotangle = rotangle;
		this.theta0 = theta0;
		this.w0 = w0;
	}

	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}

	private void setXY(double x, double y) {
		this.x = x;
		this.y = y;
		
		setChanged();
		notifyObservers();
	}

	public void move() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){
			@Override
			public void run() {
				double t = (double)MyTime.getInstance().currentTimeMillis() / 1000;
				theta = (-w0 * t + theta0) ;
				
				double x = 0, y = 0;
				if (type == Type.CIRCULAR) {
					x = r * Math.cos(theta);
					y = r * Math.sin(theta);
				} else if (type == Type.ELLIPTIC) {
					double nonrotx = a * Math.cos(theta);
					double nonroty = b * Math.sin(theta);
					x = nonrotx * Math.cos(rotangle) + nonroty * Math.sin(rotangle);
					y = -nonrotx * Math.sin(rotangle) + nonroty * Math.cos(rotangle);					
				}
				x += centerx;
				y += centery;
				setXY(x, y);				
			}			
		}, 0, UPDATE_INTERVAL);
	}

	public static void main(String[] args) {

	}
}