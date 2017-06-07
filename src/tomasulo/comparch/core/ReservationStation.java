package tomasulo.comparch.core;

import tomasulo.comparch.util.name.OperatorName;
import tomasulo.comparch.util.name.ReservationName;

/**
 * Created by neozero on 17-5-31.
 */
public class ReservationStation {

    // 在RS队列中的rank
    public int rank;

    // 装入指令的pc
    public int pc;

    // 运算种类
    public int operatorName;

    // 进入的运算器/读写器的种类
    public int reservationName;

    // 是否繁忙, busy==false表明当前Station中为具体的浮点数(来源为初始化或已经完成的运算)
    public boolean busy = false;

    // 当前操作完成还剩余的周期数
    public int busyCountDown;

    // 当前操作在ALU中的stage
    public int stage;

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

    public ReservationStation(int rank) {
        this.rank = rank;
        reset();
    }

    public void reset() {
        pc = -1;
        busy = false;
        busyCountDown = 0;
        stage = 0;
        operatorName = ReservationName.FLOATVALUE;
        reservationName = -1;
        floatResult = 0;
        qJ = qK = null;
        inArithm = false;
        qStore = null;
        addr = 0;
        intValue = 0;
    }

    public String[] getNormalRSText() {
        String[] text = new String[8];
        if (busy) {
            text[0] = Integer.toString(busyCountDown);
            text[1] = ReservationName.reservationNameMap.get(reservationName) + Integer.toString(rank);
            text[2] = "YES";
            text[3] = OperatorName.operatorNameMap.get(operatorName);
            if (qJ.busy) {
                text[6] = ReservationName.reservationNameMap.get(qJ.reservationName) + Integer.toString(qJ.rank);
            } else {
                text[4] = Double.toString(qJ.floatResult);
            }
            if (qK.busy) {
                text[7] = ReservationName.reservationNameMap.get(qK.reservationName) + Integer.toString(qK.rank);
            } else {
                text[5] = Double.toString(qK.floatResult);
            }
        } else {
            text[1] = ReservationName.reservationNameMap.get(reservationName) + Integer.toString(rank);
            text[2] = "NO";
        }
        return text;
    }

    public String[] getLoadText() {
        String[] text = new String[4];
        text[0] = ReservationName.reservationNameMap.get(reservationName) + Integer.toString(rank);
        if (busy) {
            text[1] = "YES";
            text[2] = Integer.toString(addr);
            text[3] = "";
        } else {
            text[1] = "NO";
            text[3] = Double.toString(floatResult);
        }
        return text;
    }

    public String[] getStoreText() {
        String[] text = new String[4];
        text[0] = ReservationName.reservationNameMap.get(reservationName) + Integer.toString(rank);
        if (busy) {
            text[1] = "YES";
            text[2] = Integer.toString(addr);
            text[3] = (qStore.busy) ? ReservationName.reservationNameMap.get(qStore.reservationName) + Integer.toString(qStore.rank) : Double.toString(qStore.floatResult);
        } else {
            text[1] = "NO";
        }
        return text;
    }
}
