package tomasulo.comparch.adaptor;

import tomasulo.comparch.core.TomasuloSimulatorCore;
import tomasulo.comparch.gui.MainPanel;
import tomasulo.comparch.util.multithread.SharedField;
import tomasulo.comparch.util.name

import java.util.*;



public class Adaptor implements Runnable{
	private MainPanel panelHandle;
	private TomasuloSimulatorCore engine;
	
	public SharedField operation;
	public ArrayList<String> ui_instruction;
	public ArrayList<Instruction> engine_instruction;
	
	
	public Adaptor(MainPanel handle, ArrayList<String> inst){
		panelHandle = handle;
		ui_instruction = inst;
		engine = new TomasuloSimulatorCore();
	}
	
	private void collectResult(TomasuloSimulatorCore core, MainPanel handle){
		// wait for MCZ
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
				case SharedField.INIT:
					engine_instruction.clear();
					for(int i = 0; i < ui_instruction.size(); ++i){
						engine_instruction.add(new Instruction(ui_instruction.get(i)));
					}
					engine.setInstList(engine_instruction);
					break;
				case SharedField.RUN:
					// if not initialized?
					while(!engine.checkFinish()){
						engine.step();
						collectResult(engine, panelHandle);
						try{
							Thread.sleep(1000);
						}
						catch(InterruptedException e){
							
						}
					}
					break;
				case SharedField.STEP:
					// if not initialized?
					engine.step();
					collectResult(engine, panelHandle);
					break;
				case SharedField.TERMINATE:
					flag = false;
					break;
			}
			synchronized(operation){
				operation.set(SharedField.IDLE);
			}
		}
	}
}