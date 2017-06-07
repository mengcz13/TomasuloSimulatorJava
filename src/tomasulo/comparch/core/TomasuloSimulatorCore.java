package tomasulo.comparch.core;

import tomasulo.comparch.util.name.InstStateName;
import tomasulo.comparch.util.name.OperatorName;
import tomasulo.comparch.util.name.RegisterName;
import tomasulo.comparch.util.name.ReservationName;

import java.util.*;

/**
 * Created by neozero on 17-5-31.
 */
public class TomasuloSimulatorCore {

    public static final int MEMSIZE = 4096;

    // 指令序列
    public List<Instruction> instList;

    // 4种Reservation Stations
    public Map<Integer, List<ReservationStation>> reservationStations;

    // 浮点寄存器+整数寄存器, 浮点寄存器实质上可以看做Reservation Station的引用标记, 而整数寄存器功能简单, 故统一用Reservation Station实现
    public Map<Integer, List<ReservationStation>> registers;

    // 内存单元
    public List<Double> mem;

    // 最近访问的内存单元
    public List<Integer> ruMemAddr;

    // 指令指针
    public int pc;

    // 当前周期数
    public int clock;

    // 内存大小(默认为MEMSIZE)
    public int memSize;

    public boolean runnable = false;

    // 当前周期中已经执行过操作的指令, 模拟过程中用于保证一条指令在一个周期中仅会执行Issue/Exec/Writeback中的一个
    private Set<Integer> pcsInCurrentClock;

    // 当前周期中浮点运算器流水线中的指令数量
    // private Map<Integer, Integer> arithInCurrentClock;

    // 等待进入流水线的队列, FIFO
    private Map<Integer, LinkedList<ReservationStation>> waitingList;

    // 所有运算器流水线中的等待队列, FIFO
    private ALUPipeline aluPipeline;

    public TomasuloSimulatorCore() {
        this(new ArrayList<Instruction>(), MEMSIZE);
    }

    public TomasuloSimulatorCore(List<Instruction> instList) {
        this(instList, MEMSIZE);
    }

    public TomasuloSimulatorCore(List<Instruction> instList, int memSize) {
        this.instList = instList;
        this.memSize = memSize;
        reset();
    }

    public void reset() {
        for (Instruction inst : this.instList) {
            inst.reset();
        }

        reservationStations = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : ReservationName.reservationItem.entrySet()) {
            ArrayList<ReservationStation> arrayList = new ArrayList<>(entry.getValue());
            for (int i = 0; i < entry.getValue(); ++i)
                arrayList.add(new ReservationStation(i));
            reservationStations.put(entry.getKey(), arrayList);
        }

        registers = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : RegisterName.registerItem.entrySet()) {
            ArrayList<ReservationStation> arrayList = new ArrayList<>(entry.getValue());
            for (int i = 0; i < entry.getValue(); ++i)
                arrayList.add(new ReservationStation(i));
            registers.put(entry.getKey(), arrayList);
        }

        mem = new ArrayList<>(Collections.nCopies(memSize, 0.0));

        this.ruMemAddr = new ArrayList<>();
        for (Instruction inst : this.instList) {
            if (inst.op == OperatorName.LD || inst.op == OperatorName.ST) {
                int addr = inst.regJ.intValue + this.registers.get(inst.regK.name).get(inst.regK.intValue).intValue;
                ruMemAddr.add(addr);
            }
        }

        pc = clock = 0;

        pcsInCurrentClock = new HashSet<>();

        waitingList = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : ReservationName.reservationItem.entrySet()) {
            waitingList.put(entry.getKey(), new LinkedList<>());
        }

        aluPipeline = new ALUPipeline();

        this.runnable = false;
    }

//    public void setInstList(List<Instruction> instList) {
//        this.instList = instList;
//        this.reset();
//    }
//
//    public List<Instruction> getInstList() {
//        return instList;
//    }

    // 单步执行
    public void step() {
        if (!checkFinish()) {
            ++clock;
            pcsInCurrentClock.clear();

            issue();
            compExec();
            writeBack();
        } else {
            this.runnable = false;
        }
    }

    // 连续执行直到全部完成
    public void run() {
        while (!checkFinish()) {
            step();
            for (int i = 0; i < instList.size(); ++i) {
                System.out.printf("%d %d %d %d\n", i,
                        instList.get(i).record.get(InstStateName.ISSUE),
                        instList.get(i).record.get(InstStateName.EXECCOMP),
                        instList.get(i).record.get(InstStateName.WRITERESULT));
            }
            System.out.println();
        }
        this.runnable = false;
    }

    // 每条指令执行完成后会记录完成时的周期数, 以此检查所有指令是否全部完成
    public boolean checkFinish() {
        for (Instruction inst : this.instList) {
            for (Integer rec : inst.record.values()) {
                if (rec == 0)
                    return false;
            }
        }
        return true;
    }

    // 修改内存
    private void setMem(int addr, double newvalue) {
        this.mem.set(addr, newvalue);
    }

    // 与UI的交互
    public String[][] getInsTable() {
        int insnum = this.instList.size();
        String[][] insTable = new String[insnum][];
        for (int i = 0; i < insnum; ++i) {
            insTable[i] = this.instList.get(i).getText();
        }
        return insTable;
    }

    public void setInsTable(String[][] insTable) {
        List<Instruction> inst = new ArrayList<>();
        for (String[] anInsTable : insTable) {
            inst.add(new Instruction(anInsTable));
        }
        this.instList = inst;
        this.reset();
        this.runnable = true;
    }

    public String[][] getStateTable() {
        String[][] stateTable = new String[this.instList.size()][];
        for (int i = 0; i < this.instList.size(); ++i) {
            stateTable[i] = this.instList.get(i).getState();
        }
        return stateTable;
    }

    public String[][] getReserveTable() {
        int addnum = ReservationName.reservationItem.get(ReservationName.ADD);
        int mulnum = ReservationName.reservationItem.get(ReservationName.MULT);
        String[][] reserveTable = new String[addnum + mulnum][];
        for (int i = 0; i < addnum; ++i) {
            reserveTable[i] = this.reservationStations.get(ReservationName.ADD).get(i).getNormalRSText();
        }
        for (int i = addnum; i < addnum + mulnum; ++i) {
            reserveTable[i] = this.reservationStations.get(ReservationName.MULT).get(i - addnum).getNormalRSText();
        }
        return reserveTable;
    }

    public String[][] getMemTable() {
        String[][] memTable = new String[ruMemAddr.size()][2];
        for (int i = 0; i < ruMemAddr.size(); ++i) {
            memTable[i][0] = Integer.toHexString(ruMemAddr.get(i));
            memTable[i][1] = Double.toString(mem.get(ruMemAddr.get(i)));
        }
        return memTable;
    }

    public void setMemTable(String[][] memTable) {
        for (String[] aMemTable : memTable) {
            assert aMemTable[0].substring(0, 2).equals("0x");
            int addr = Integer.parseInt(aMemTable[0].substring(2), 16);
            int newvalue = Integer.parseInt(aMemTable[1]);
            this.setMem(addr, newvalue);
        }
    }

    public String[][] getLoadTable() {
        List<ReservationStation> loads = reservationStations.get(ReservationName.LOAD);
        String[][] loadTable = new String[loads.size()][];
        for (int i = 0; i < loads.size(); ++i) {
            loadTable[i] = loads.get(i).getLoadText();
        }
        return loadTable;
    }

    public String[][] getStoreTable() {
        List<ReservationStation> stores = reservationStations.get(ReservationName.STORE);
        String[][] storeTable = new String[stores.size()][];
        for (int i = 0; i < stores.size(); ++i) {
            storeTable[i] = stores.get(i).getStoreText();
        }
        return storeTable;
    }

    public String[][] getRuTable() {
        List<ReservationStation> rus = registers.get(RegisterName.INT);
        String[][] ruTable = new String[rus.size()][2];
        for (int i = 0; i < rus.size(); ++i) {
            ruTable[i][0] = "R" + Integer.toString(rus.get(i).rank);
            ruTable[i][1] = Integer.toString(rus.get(i).intValue);
        }
        return ruTable;
    }

    public void setRuTable(String[][] ruTable) {
        for (String[] anRuTable : ruTable) {
            assert anRuTable[0].charAt(0) == 'R';
            int rank = Integer.parseInt(anRuTable[0].substring(1));
            int newvalue = Integer.parseInt(anRuTable[1]);
            this.registers.get(RegisterName.INT).get(rank).intValue = newvalue;
        }
    }

    public String[][] getFuTable() {
        List<ReservationStation> fus = registers.get(RegisterName.FLOAT);
        String[][] fuTable = new String[fus.size()][3];
        for (int i = 0; i < fus.size(); ++i) {
            ReservationStation fu = fus.get(i);
            fuTable[i][0] = "F" + Integer.toString(i);
            if (fu.busy) {
                fuTable[i][1] = ReservationName.reservationNameMap.get(fu.reservationName) + Integer.toString(fu.rank);
            } else {
                fuTable[i][2] = Double.toString(fu.floatResult);
            }
        }
        return fuTable;
    }

    // 这里利用了Java对于复杂结构传递引用的特性, 每个Reservation Station中的源操作数均为对其他Station的引用
    private void issue() {
        if (pc < instList.size()) {
            Instruction inst = this.instList.get(pc);
            int reservationName = OperatorName.operatorReservationMap.get(inst.op);
            List<ReservationStation> lrs = this.reservationStations.get(reservationName);
            ReservationStation res = null;
            // 寻找空闲的Reservation Station, 找不到则stall, 不再继续issue
            for (ListIterator<ReservationStation> iter = lrs.listIterator(); iter.hasNext(); ) {
                int rank = iter.nextIndex();
                ReservationStation temp = iter.next();
                if (!temp.busy) {   // 对于计算完成的Station, 存放的实际为具体的浮点数, 这里可以直接将其替换为新的Station, 若有其他位置引用了该浮点数则引用关系不变, 否则该浮点数会被垃圾回收
                    res = new ReservationStation(rank);
                    iter.set(res);
                    break;
                }
            }
            if (res != null) {
                res.reservationName = reservationName;
                this.pcsInCurrentClock.add(pc);
                // 解析指令, 将源操作数和目的操作数分配到Reservation Station的对应位置
                switch (inst.op) {
                    case OperatorName.ADDD:
                    case OperatorName.SUBD:
                    case OperatorName.MULTD:
                    case OperatorName.DIVD:
                        res.qJ = this.registers.get(inst.regJ.name).get(inst.regJ.intValue);
                        res.qK = this.registers.get(inst.regK.name).get(inst.regK.intValue);
                        this.registers.get(inst.reg0.name).set(inst.reg0.intValue, res);
                        break;
                    case OperatorName.LD:
                        assert inst.regJ.name == RegisterName.INSTVALUE;
                        assert inst.regK.name == RegisterName.INT;
                        res.addr = inst.regJ.intValue + this.registers.get(inst.regK.name).get(inst.regK.intValue).intValue;
                        this.registers.get(inst.reg0.name).set(inst.reg0.intValue, res);
                        break;
                    case OperatorName.ST:
                        res.qStore = this.registers.get(inst.reg0.name).get(inst.reg0.intValue);
                        res.addr = inst.regJ.intValue + this.registers.get(inst.regK.name).get(inst.regK.intValue).intValue;
                        break;
                    default:
                        break;
                }
                res.pc = pc;
                res.busy = true;
                res.busyCountDown = OperatorName.busyCountDownMap.get(inst.op);
                res.operatorName = inst.op;

                this.waitingList.get(res.reservationName).addLast(res);

                inst.record.put(InstStateName.ISSUE, clock);
                ++pc;
            }
        }
    }

    private void compExec() {
        // 对于所有的等待队列, 从前到后检测操作数是否已经准备好
        for (Map.Entry<Integer, LinkedList<ReservationStation>> entry : this.waitingList.entrySet()) {
            LinkedList<ReservationStation> wl = entry.getValue();
            Iterator<ReservationStation> iter = wl.iterator();
            while (iter.hasNext()) {
                ReservationStation res = iter.next();
                if (!pcsInCurrentClock.contains(res.pc)) {
                    boolean ready = false;
                    switch (OperatorName.operatorReservationMap.get(res.operatorName)) {    // 判断源操作数是否都已经计算完成
                        case ReservationName.ADD:
                        case ReservationName.MULT:
                            ready = !(res.qJ.busy || res.qK.busy);
                            break;
                        case ReservationName.LOAD:
                            ready = true;
                            break;
                        case ReservationName.STORE:
                            ready = !(res.qStore.busy);
                            break;
                        default:
                            break;
                    }
                    if (ready) {
                        aluPipeline.add(res);
                        iter.remove();
                    }
                }
            }
        }
        List<ReservationStation> outress = aluPipeline.process();
        for (ReservationStation res : outress) {
            switch (res.operatorName) {
                case OperatorName.ADDD:
                    res.floatResult = res.qJ.floatResult + res.qK.floatResult;
                    break;
                case OperatorName.SUBD:
                    res.floatResult = res.qJ.floatResult - res.qK.floatResult;
                    break;
                case OperatorName.MULTD:
                    res.floatResult = res.qJ.floatResult * res.qK.floatResult;
                    break;
                case OperatorName.DIVD:
                    if (res.qK.floatResult == 0.0) {
                        res.floatResult = 0.0;
                    } else {
                        res.floatResult = res.qJ.floatResult / res.qK.floatResult;
                    }
                    break;
                case OperatorName.LD:
                    res.floatResult = this.mem.get(res.addr);
                    break;
                case OperatorName.ST:
                    this.mem.set(res.addr, res.qStore.floatResult);
                    break;
                default:
                    break;
            }
            this.pcsInCurrentClock.add(res.pc);
            this.instList.get(res.pc).record.put(InstStateName.EXECCOMP, clock);
        }
        /*
        for (Map.Entry<Integer, List<ReservationStation>> entry : this.reservationStations.entrySet()) {
            int resname = entry.getKey();
            List<ReservationStation> list = entry.getValue();
            for (ReservationStation res : list) {
                if (!pcsInCurrentClock.contains(res.pc) && res.busy) {  // 这里跳过了本周期中刚刚issue的指令占用的单元和非busy(实际上为计算完毕的常数)的单元
                    // boolean justIssued = (res.busyCountDown == OperatorName.busyCountDownMap.get(res.operatorName));    // 表示是否为上一个周期刚刚issue的指令
                    // boolean arithReady = this.arithInCurrentClock.get(resname) < ReservationName.maxArithmicItem.get(resname);  // 需要的运算器是否可以继续接收指令(由于流水线级数有限)
                    boolean ready = false;
                    switch (OperatorName.operatorReservationMap.get(res.operatorName)) {    // 判断源操作数是否都已经计算完成
                        case ReservationName.ADD:
                        case ReservationName.MULT:
                            ready = !(res.qJ.busy || res.qK.busy);
                            break;
                        case ReservationName.LOAD:
                            ready = true;
                            break;
                        case ReservationName.STORE:
                            ready = !(res.qStore.busy);
                            break;
                        default:
                            break;
                    }
                    if (ready) {
                        if (justIssued) {   // 对于上个clock刚刚issue的指令, 这里要判断本周期内是否被送入了运算器
                            if (arithReady) {
                                res.inArithm = true;
                                this.arithInCurrentClock.put(resname, this.arithInCurrentClock.get(resname) + 1);
                            }
                            else {
                                continue;
                            }
                        }
                        if (res.busyCountDown > 0) {    // 对于所有运算器内的指令, 执行运算
                            --res.busyCountDown;
                            this.pcsInCurrentClock.add(res.pc);
                            if (res.busyCountDown == 0) {   // 设定最后一个周期给出结果
                                switch (res.operatorName) {
                                    case OperatorName.ADDD:
                                        res.floatResult = res.qJ.floatResult + res.qK.floatResult;
                                        break;
                                    case OperatorName.SUBD:
                                        res.floatResult = res.qJ.floatResult - res.qK.floatResult;
                                        break;
                                    case OperatorName.MULTD:
                                        res.floatResult = res.qJ.floatResult * res.qK.floatResult;
                                        break;
                                    case OperatorName.DIVD:
                                        if (res.qK.floatResult == 0.0) {
                                            res.floatResult = 0.0;
                                        } else {
                                            res.floatResult = res.qJ.floatResult / res.qK.floatResult;
                                        }
                                        break;
                                    case OperatorName.LD:
                                        res.floatResult = this.mem.get(res.addr);
                                        break;
                                    case OperatorName.ST:
                                        this.mem.set(res.addr, res.qStore.floatResult);
                                        break;
                                    default:
                                        break;
                                }
                                Instruction inst = this.instList.get(res.pc);
                                inst.record.put(InstStateName.EXECCOMP, clock);
                            }
                        }
                    }
                }
            }
        }
        */
    }

    private void writeBack() {  // 写回操作只需要修改Reservation Station的状态即可, 由于Java传递引用的特性, 对应位置的值会自动变化
        for (Map.Entry<Integer, List<ReservationStation>> entry : this.reservationStations.entrySet()) {
            int resname = entry.getKey();
            List<ReservationStation> list = entry.getValue();
            for (Iterator<ReservationStation> iter = list.iterator(); iter.hasNext(); ) {
                ReservationStation res = iter.next();
                if (!pcsInCurrentClock.contains(res.pc) && res.busy && res.busyCountDown == 0) {
                    res.inArithm = false;
                    res.busy = false;
                    res.operatorName = ReservationName.FLOATVALUE;
                    this.instList.get(res.pc).record.put(InstStateName.WRITERESULT, clock);
                }
            }
        }
    }
}