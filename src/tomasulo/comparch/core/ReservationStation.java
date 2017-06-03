package tomasulo.comparch.core;

import tomasulo.comparch.util.name.OperatorName;
import tomasulo.comparch.util.name.ReservationName;

/**
 * Created by neozero on 17-5-31.
 */
public class ReservationStation {

    // 装入指令的pc
    public int pc;

    // 运算种类
    public int operatorName;

    // 是否繁忙, busy==false表明当前Station中为具体的浮点数(来源为初始化或已经完成的运算)
    public boolean busy = false;

    // 当前操作完成还剩余的周期数
    public int busyCountDown;

    // 浮点运算结果or LOAD结果
    public double floatResult;

    // 源操作数(的引用), 是否执行完成要根据对应的busy判断
    public ReservationStation qJ;

    public ReservationStation qK;

    // 是否已进入运算器执行
    public boolean inArithm;

    // For load and store only

    // STORE指令的源寄存器
    public ReservationStation qStore;

    // 地址, 装入时可以确定(因不涉及对整数寄存器的修改)
    public int addr;

    // 作为整数寄存器使用时用于存储值
    public int intValue;

    public ReservationStation() {
        reset();
    }

    public void reset() {
        pc = -1;
        busy = false;
        busyCountDown = 0;
        operatorName = ReservationName.FLOATVALUE;
        floatResult = 0;
        qJ = qK = null;
        inArithm = false;
        qStore = null;
        addr = 0;
        intValue = 0;
    }
}
