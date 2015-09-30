package edu.solarsystem.model;

import java.util.Timer;
import java.util.TimerTask;

public final class MyTime {
	private static volatile MyTime instance = null;
	private long starttime;
	private long offset;
	private Timer timer;

	private MyTime() {
		starttime = System.currentTimeMillis();
		offset = 0;
		timer = new Timer();
		timer.schedule(new TimerTask(){
			@Override
			public void run() {
				offset++;
			}	
		}, 0, 1);
	}
	
    public long getStarttime() {
		return starttime;
	}
	public long getOffset() {
		return offset;
	}

	public final static MyTime getInstance() {
        synchronized(MyTime.class) {
        	if (instance == null) {
        		instance = new MyTime();
        	}
        }
        return instance;
    }

	public long currentTimeMillis() {
		return starttime + offset;
	}
	
	public void pause() {
		timer.cancel();
	}
	public void resume() {
		timer = new Timer();
		timer.schedule(new TimerTask(){
			@Override
			public void run() {
				offset++;
			}	
		}, 0, 1);		
	}
}
