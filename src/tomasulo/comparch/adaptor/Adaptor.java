package tomasulo.comparch.adaptor;

import tomasulo.comparch.core.Instruction;
import tomasulo.comparch.core.TomasuloSimulatorCore;
import tomasulo.comparch.gui.MainPanel;
import tomasulo.comparch.util.multithread.SharedField;

import java.util.*;


public class Adaptor implements Runnable {
    private MainPanel panelHandle;
    private TomasuloSimulatorCore engine;

    public SharedField operation;
    public ArrayList<String> ui_instruction;
    public ArrayList<Instruction> engine_instruction;


    public Adaptor(MainPanel handle, ArrayList<String> inst) {
        panelHandle = handle;
        operation = new SharedField();
        handle.setAdaptor(this);
        ui_instruction = inst;
        engine = new TomasuloSimulatorCore();
        engine_instruction = new ArrayList<Instruction>();
    }

    private void collectResult(TomasuloSimulatorCore core, MainPanel handle) {
        // wait for MCZ
        String[][] temp = core.getStateTable();
        handle.stateTable.setData(temp);
        temp = core.getReserveTable();
        handle.reserveTable.setData(temp);
        temp = core.getMemTable();
        handle.memTable.setData(temp);
        temp = core.getLoadTable();
        handle.loadTable.setData(temp);
        temp = core.getStoreTable();
        handle.storeTable.setData(temp);
        temp = core.getRuTable();
        handle.ruTable.setData(temp);
        temp = core.getFuTable();
        handle.fuTable.setData(temp);
        handle.clock.setTime(core.clock);
    }

    public void run() {
        int code;
        boolean flag = true;
        while (flag) {
            synchronized (operation) {
                while ((code = operation.get()) == SharedField.IDLE) {
                    try {
                        operation.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                switch (code) {
                    case SharedField.INIT:
                        engine.setInsTable(panelHandle.insTable.getData());
                        engine.setMemTable(panelHandle.memTable.getData());
                        break;
                    case SharedField.RUN:
                        if (!engine.runnable) {
                            break;
                        }
                        while (!engine.checkFinish()) {
                            engine.step();
                            collectResult(engine, panelHandle);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {

                            }
                        }
                        panelHandle.terminate();
                        break;
                    case SharedField.STEP:
                        if (!engine.runnable) {
                            break;
                        }
                        engine.step();
                        collectResult(engine, panelHandle);
                        if(!engine.checkFinish()) {
                            panelHandle.terminate();
                        }
                        break;
                    case SharedField.SET_MEM:
                        if (!engine.runnable) {
                            break;
                        }
                        engine.setMemTable(panelHandle.memTable.getData());
                        break;
                    case SharedField.SET_REG:
                        if(!engine.runnable) {
                            break;
                        }
                        engine.setRuTable(panelHandle.ruTable.getData());
                        break;
                    case SharedField.TERMINATE:
                        flag = false;
                        break;
                }
                operation.set(SharedField.IDLE);
            }
            code = 5;
            code++;
        }
    }
}