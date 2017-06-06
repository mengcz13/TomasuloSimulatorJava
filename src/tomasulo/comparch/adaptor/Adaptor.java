package tomasulo.comparch.adaptor;

import tomasulo.comparch.core.TomasuloSimulatorCore;
import tomasulo.comparch.UI.MainPanel;	// assumed path

import java.util.*;

class SharedField{
	private int value;
	public static final int IDLE = 0;
	public static final int TERMINATE = 1;
	public static final int STEP = 2;
	public static final int RUN = 3;
	public void set(int v){
		value = v;
	}
	public int get(){
		return value;
	}
}

public class Adaptor implements Runnable{
	private MainPanel panelHandle;
	private TomasuloSimulatorCore engine;
	
	public SharedField operation;
	
	
	public Adaptor(MainPanel handle){
		panelHandle = handle;
	}
	
	public void run(){
		int code;
		boolean flag = true;
		while(flag){
			synchronized(operation){
				while((code = operation.get()) == SharedField.IDLE){
					operation.wait();
				}
			}
			switch(code){
				case SharedField.RUN:
					break;
				case SharedField.STEP:
					break;
				case SharedField.TERMINATE:
					flag = false;
					break;
			}
		}
	}
}