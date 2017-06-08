package tomasulo.comparch.adaptor;

import tomasulo.comparch.core.TomasuloSimulatorCore;
import tomasulo.comparch.gui.MainPanel;
import tomasulo.comparch.util.multithread.SharedField;

import java.util.*;

/**
 * Adaptor类扮演适配器的角色，在用户界面和后端引擎之间搭起桥梁，并承担线程通信的任务。<br>
 * Adaptor对象持有一个MainPanel指针和一个TomasuloSimulatorCore指针，用于实现信息传递。<br>
 * Adaptor类继承Runnable，因此可以独立作为一个线程运行，实现后台计算与前台显示的分离。<br>
 * 成员变量operation是一个SharedField对象，由Adaptor的对象与MainPanel类的对象共享。<br>
 * 通过SharedField的设置和复位，实现由前台对后台的控制指令传递。
 */
public class Adaptor implements Runnable {
    /**
     * UI主面板。
     */
    private MainPanel panelHandle;
    /**
     * Tomasulo算法核心类。
     */
    private TomasuloSimulatorCore engine = new TomasuloSimulatorCore();

    /**
     * UI线程和计算线程的共享信号量。
     */
    public SharedField operation = new SharedField();

    /**
     * Adaptor构造函数，完成与UI界面所对应的类(MainPanel)的相互绑定，即二者均持有对方的引用。
     * @param handle 要绑定的UI界面
     */
    public Adaptor(MainPanel handle) {
        panelHandle = handle;
        handle.setAdaptor(this);
    }

    /**
     * 收集模拟器的输出信息，并显示在界面上。<br>
     * 要显示的内容包括：
     * <ul>
     *     <li>指令状态信息</li>
     *     <li>保留站信息</li>
     *     <li>内存信息</li>
     *     <li>Load表信息</li>
     *     <li>Store表信息</li>
     *     <li>整数寄存器信息</li>
     *     <li>浮点寄存器信息</li>
     * </ul>
     * @param core Tomasulo核心类
     * @param handle 主面板类MainPanel
     */
    private void collectResult(TomasuloSimulatorCore core, MainPanel handle) {
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

    /**
     * 线程的主函数。
     * 主要逻辑如下：
     * 不断轮询operation中的值，根据不同的值执行不同操作。
     * <ul>
     *     <li>IDLE: 当前线程进入wait状态</li>
     *     <li>INIT: 装载指令列表</li>
     *     <li>STEP: 执行一步，并将输出信息显示在界面上</li>
     *     <li>RUN: 连续执行，并将输出信息显示在界面上</li>
     *     <li>SET_MEM / SET_REG: 修改内存/整数寄存器</li>
     *     <li>TERMINATE: 退出</li>
     * </ul>
     */
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
                    case SharedField.STEPN:
                        if (!engine.runnable) {
                            break;
                        }
                        int count = 0;
                        int stepVal = Integer.parseInt(panelHandle.textField.getText());
                        while (count < stepVal && !engine.checkFinish()) {
                            engine.step();
                            collectResult(engine, panelHandle);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                            count++;
                        }
                        if(engine.checkFinish()) {
                            panelHandle.terminate();
                        } else {
                            panelHandle.restoreFree();
                        }
                        break;
                    case SharedField.STEP:
                        if (!engine.runnable) {
                            break;
                        }
                        engine.step();
                        collectResult(engine, panelHandle);
                        if (engine.checkFinish()) {
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
                        if (!engine.runnable) {
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