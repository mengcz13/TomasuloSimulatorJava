package tomasulo.comparch.core;

import tomasulo.comparch.util.name.InstStateName;
import tomasulo.comparch.util.name.OperatorName;
import tomasulo.comparch.util.name.RegisterName;
import tomasulo.comparch.util.name.ReservationName;

import java.util.*;
import java.util.logging.Logger;

/**
 * Tomasulo模拟算法类, 实现了对Tomasulo算法的模拟, 并为外部UI提供了保留站/寄存器/内存单元的getter和setter.
 */
public class TomasuloSimulatorCore {

    /**
     * 默认内存大小为4096单元.
     */
    public static final int MEMSIZE = 4096;

    /**
     * 指令序列.
     */
    public List<Instruction> instList;

    /**
     * 4种Reservation Stations.
     */
    public Map<Integer, List<ReservationStation>> reservationStations;

    /**
     * 浮点寄存器+整数寄存器, 浮点寄存器实质上可以看做Reservation Station的引用标记, 而整数寄存器功能简单, 故统一用Reservation Station实现.
     */
    public Map<Integer, List<ReservationStation>> registers;

    /**
     * 内存单元.
     */
    public List<Double> mem;

    /**
     * 最近访问的内存单元.
     */
    public Set<Integer> ruMemAddr;

    /**
     * 指令指针.
     */
    public int pc;

    /**
     * 当前周期数.
     */
    public int clock;

    /**
     * 内存大小(默认为MEMSIZE).
     */
    public int memSize;

    /**
     * 表示当前指令序列是否可以运行, 从加载指令序列到全部执行完成期间始终为true.
     */
    public boolean runnable = false;

    /**
     * 当前周期中已经执行过操作的指令, 模拟过程中用于保证一条指令在一个周期中仅会执行Issue/Exec/Writeback中的一个.
     */
    private Set<Integer> pcsInCurrentClock;

    /**
     * 等待进入流水线的队列, FIFO.
     */
    private Map<Integer, LinkedList<ReservationStation>> waitingList;

    /**
     * 所有运算器流水线中的等待队列, FIFO.
     */
    private ALUPipeline aluPipeline;

    /**
     * 默认构造函数, 设置内存大小为默认值.
     */
    public TomasuloSimulatorCore() {
        this(new ArrayList<Instruction>(), MEMSIZE);
    }

    /**
     * 从给定指令序列构造模拟器.
     *
     * @param instList 给定的指令序列, 模拟器以此初始化自己的指令序列.
     */
    public TomasuloSimulatorCore(List<Instruction> instList) {
        this(instList, MEMSIZE);
    }

    /**
     * 根据指令序列和内存大小构造模拟器.
     *
     * @param instList 给定的指令序列, 模拟器以此初始化自己的指令序列.
     * @param memSize  给定的内存大小.
     */
    public TomasuloSimulatorCore(List<Instruction> instList, int memSize) {
        this.instList = instList;
        this.memSize = memSize;
        reset();
    }

    /**
     * 重置模拟器, 包括清除指令执行的历史记录, 清空所有寄存器, 清空内存, 清空内存最近访问记录, 重置pc和clock, 清空等待队列和流水线.
     */
    public void reset() {
        for (Instruction inst : this.instList) {
            inst.reset();
        }

        reservationStations = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : ReservationName.reservationItem.entrySet()) {
            ArrayList<ReservationStation> arrayList = new ArrayList<>(entry.getValue());
            for (int i = 0; i < entry.getValue(); ++i)
                arrayList.add(new ReservationStation(entry.getKey(), i));
            reservationStations.put(entry.getKey(), arrayList);
        }

        registers = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : RegisterName.registerItem.entrySet()) {
            ArrayList<ReservationStation> arrayList = new ArrayList<>(entry.getValue());
            for (int i = 0; i < entry.getValue(); ++i)
                arrayList.add(new ReservationStation(-1, i));
            registers.put(entry.getKey(), arrayList);
        }

        mem = new ArrayList<>(Collections.nCopies(memSize, 0.0));

        this.ruMemAddr = new HashSet<>();
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

    /**
     * 设定指令序列.
     *
     * @param instList 指令序列.
     */
    public void setInstList(List<Instruction> instList) {
        this.instList = instList;
        this.reset();
    }

    /**
     * 获取指令序列
     *
     * @return 模拟器当前使用的指令序列.
     */
    public List<Instruction> getInstList() {
        return instList;
    }

    /**
     * 模拟器进行一次单步执行.
     * <p>
     * 执行前检查是否已经完成, 若完成则修改runnable标记.
     * <p>
     * 每次执行均增加clock, 重置当前clock内执行的指令的历史记录.
     */
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

    /**
     * 连续执行指令直到全部完成.
     */
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

    /**
     * 每条指令执行完成后会记录完成时的周期数, 以此检查所有指令是否全部完成.
     *
     * @return 若全部指令已经执行完成则返回True.
     */
    public boolean checkFinish() {
        for (Instruction inst : this.instList) {
            for (Integer rec : inst.record.values()) {
                if (rec == 0)
                    return false;
            }
        }
        return true;
    }

    /**
     * 修改指定位置的内存值.
     *
     * @param addr     修改位置的内存地址.
     * @param newvalue 需要写入的新内存值.
     */
    private void setMem(int addr, double newvalue) {
        this.mem.set(addr, newvalue);
    }

    /**
     * 返回指令序列.
     *
     * @return 指令序列的二维字符串数组. 格式为{{"OP", "R1", "R2", "R3"}}.
     */
    public String[][] getInsTable() {
        int insnum = this.instList.size();
        String[][] insTable = new String[insnum][];
        for (int i = 0; i < insnum; ++i) {
            insTable[i] = this.instList.get(i).getText();
        }
        return insTable;
    }

    /**
     * 设置指令序列.
     *
     * @param insTable 指令序列的二维字符串数组. 格式为{{"OP", "R1", "R2", "R3"}}.
     */
    public void setInsTable(String[][] insTable) {
        List<Instruction> inst = new ArrayList<>();
        for (String[] anInsTable : insTable) {
            inst.add(new Instruction(anInsTable));
        }
        this.instList = inst;
        this.reset();
        this.runnable = true;
    }

    /**
     * 返回指令执行历史记录.
     *
     * @return 指令执行历史记录的二维字符串数组. 格式为{{"ISSUE", "EXEC", "WRITEBACK"}}.
     */
    public String[][] getStateTable() {
        String[][] stateTable = new String[this.instList.size()][];
        for (int i = 0; i < this.instList.size(); ++i) {
            stateTable[i] = this.instList.get(i).getState();
        }
        return stateTable;
    }

    /**
     * 返回Add和Mul操作的Reservation Station.
     *
     * @return Reservation Station的二维字符串数组. 格式为{{"time", "name", "busy", "operation", "Vi", "Vj", "Qi", "Qj"}}.
     */
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

    /**
     * 返回指令序列中涉及到的内存地址的信息.
     *
     * @return 内存地址和数据的字符串二维数组. 格式为{{"16进制地址", "浮点数"}}.
     */
    public String[][] getMemTable() {
        String[][] memTable = new String[ruMemAddr.size()][2];
        int rank = 0;
        for (Integer addr : ruMemAddr) {
            memTable[rank][0] = Integer.toHexString(addr);
            memTable[rank][1] = Double.toString(mem.get(addr));
            ++rank;
        }
        return memTable;
    }

    /**
     * 设置指定位置的内存值.
     *
     * @param memTable 内存地址和数据的字符串二维数组. 格式为{{"16进制地址", "浮点数"}}.
     */
    public void setMemTable(String[][] memTable) {
        for (String[] aMemTable : memTable) {
            int addr = -1;
            if (aMemTable[0].startsWith("0x")) {
                addr = Integer.parseInt(aMemTable[0].substring(2), 16);
            } else {
                addr = Integer.parseInt(aMemTable[0], 16);
            }
            double newvalue = Double.parseDouble(aMemTable[1]);
            this.setMem(addr, newvalue);
            System.out.println(newvalue);
        }
    }

    /**
     * 返回Load操作的Reservation Station.
     *
     * @return Reservation Station的二维字符串数组. 格式为{{"name", "busy", "address", "cache"}}.
     */
    public String[][] getLoadTable() {
        List<ReservationStation> loads = reservationStations.get(ReservationName.LOAD);
        String[][] loadTable = new String[loads.size()][];
        for (int i = 0; i < loads.size(); ++i) {
            loadTable[i] = loads.get(i).getLoadText();
        }
        return loadTable;
    }

    /**
     * 返回Store操作的Reservation Station.
     *
     * @return Reservation Station的二维字符串数组. 格式为{{"name", "busy", "address", "cache"}}.
     */
    public String[][] getStoreTable() {
        List<ReservationStation> stores = reservationStations.get(ReservationName.STORE);
        String[][] storeTable = new String[stores.size()][];
        for (int i = 0; i < stores.size(); ++i) {
            storeTable[i] = stores.get(i).getStoreText();
        }
        return storeTable;
    }

    /**
     * 返回整数寄存器.
     *
     * @return 二维字符串数组. 格式为{{"name", "value"}}.
     */
    public String[][] getRuTable() {
        List<ReservationStation> rus = registers.get(RegisterName.INT);
        String[][] ruTable = new String[rus.size()][2];
        for (int i = 0; i < rus.size(); ++i) {
            ruTable[i][0] = "R" + Integer.toString(rus.get(i).rank);
            ruTable[i][1] = Integer.toString(rus.get(i).intValue);
        }
        return ruTable;
    }

    /**
     * 设置整数寄存器.
     *
     * @param ruTable 二维字符串数组. 格式为{{"name", "value"}}.
     */
    public void setRuTable(String[][] ruTable) {
        for (String[] anRuTable : ruTable) {
            assert anRuTable[0].charAt(0) == 'R';
            int rank = Integer.parseInt(anRuTable[0].substring(1));
            int newvalue = Integer.parseInt(anRuTable[1], 16);
            this.registers.get(RegisterName.INT).get(rank).intValue = newvalue;
        }
    }

    /**
     * 返回浮点寄存器.
     *
     * @return 二维字符串数组. 格式为{{"name", "value"}}.
     */
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

    /**
     * Issue部分模拟。
     * 具体流程如下:<br>
     * <br>
     * 1. 对于每条指令, 寻找对应的Reservation Station中是否有空闲位置, 没有则返回, 否则继续.<br>
     * 2. 解析指令, 将源操作数和目的操作数分配到Reservation Station的对应位置, 设置好Reservation Station的信息.<br>
     * 3. 将该Reservation Station加入waitingList.<br>
     * <br>
     * 这里利用了Java对于复杂结构传递引用的特性, 每个Reservation Station中的源操作数均为对其他Station的引用.<br>
     * 对于计算完成的Station, 存放的实际为具体的浮点数, 实现时, 若有新的指令需要使用该位置, 可以直接将其替换为新构造的Station, 即使有其他位置引用了该浮点数, 引用关系也不变.<br>
     */
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
                    res = new ReservationStation(reservationName, rank);
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

    /**
     * EXEC部分模拟, 具体流程如下:
     * <p>
     * 1. 对于所有的等待队列, 从前到后检测操作数是否已经准备好. 该队列按照指令Issue的顺序排列, FIFO.<br>
     * 2. 将Reservation Station送入流水线.<br>
     * 3. 取出流水线计算完成的Reservation Station, 实际计算出具体数值, 并对其对应的指令标记"本clock中已执行过".<br>
     */
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
    }

    /**
     * WRITEBACK部分模拟, 具体流程如下:
     * <p>
     * 只需要修改Reservation Station的状态即可, 由于Java传递引用的特性, 对应位置的值会自动变化.
     */
    private void writeBack() {
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