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

    private List<Instruction> instList;

    private Map<Integer, List<ReservationStation>> reservationStations;

    private Map<Integer, List<ReservationStation>> registers;

    private List<Double> mem;

    private int pc;

    private int clock;

    private int memSize;

    private Set<Integer> pcsInCurrentClock;

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
                arrayList.add(new ReservationStation());
            reservationStations.put(entry.getKey(), arrayList);
        }

        registers = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : RegisterName.registerItem.entrySet()) {
            ArrayList<ReservationStation> arrayList = new ArrayList<>(entry.getValue());
            for (int i = 0; i < entry.getValue(); ++i)
                arrayList.add(new ReservationStation());
            registers.put(entry.getKey(), arrayList);
        }

        mem = new ArrayList<>(Collections.nCopies(memSize, 0.0));

        pc = clock = 0;

        pcsInCurrentClock = new HashSet<>();
    }

    public void setInstList(List<Instruction> instList) {
        this.instList = instList;
        this.reset();
    }

    public List<Instruction> getInstList() {
        return instList;
    }

    public void step() {
        ++clock;
        pcsInCurrentClock.clear();

        issue();
        compExec();
        writeBack();
    }

    public void run() {
        while (!checkFinish()) {
            step();
            for (int i = 0; i < instList.size(); ++i) {
                System.out.printf("%d %d %d %d\n", i,
                        instList.get(i).record.get(0),
                        instList.get(i).record.get(1),
                        instList.get(i).record.get(2));
            }
            System.out.println();
        }
    }

    public boolean checkFinish() {
        for (Instruction inst : this.instList) {
            for (Integer rec : inst.record.values()) {
                if (rec == 0)
                    return false;
            }
        }
        return true;
    }

    private void issue() {
        if (pc < instList.size()) {
            Instruction inst = this.instList.get(pc);
            int reservationName = OperatorName.operatorReservationMap.get(inst.op);
            List<ReservationStation> lrs = this.reservationStations.get(reservationName);
            ReservationStation res = null;
            for (ListIterator<ReservationStation> iter = lrs.listIterator(); iter.hasNext(); ) {
                ReservationStation temp = iter.next();
                if (!temp.busy) {
                    res = new ReservationStation();
                    iter.set(res);
                    break;
                }
            }
            if (res != null) {
                this.pcsInCurrentClock.add(pc);

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

                inst.record.put(InstStateName.ISSUE, clock);
                ++pc;
            }
        }
    }

    private void compExec() {
        for (Map.Entry<Integer, List<ReservationStation>> entry : this.reservationStations.entrySet()) {
            List<ReservationStation> list = entry.getValue();
            for (ReservationStation res : list) {
                if (!pcsInCurrentClock.contains(res.pc) && res.busy) {
                    boolean ready = false;
                    switch (OperatorName.operatorReservationMap.get(res.operatorName)) {
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
                        if (res.busyCountDown > 0) {
                            --res.busyCountDown;
                            this.pcsInCurrentClock.add(res.pc);
                            if (res.busyCountDown == 0) {
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
    }

    private void writeBack() {
        for (Map.Entry<Integer, List<ReservationStation>> entry : this.reservationStations.entrySet()) {
            List<ReservationStation> list = entry.getValue();
            for (Iterator<ReservationStation> iter = list.iterator(); iter.hasNext(); ) {
                ReservationStation res = iter.next();
                if (!pcsInCurrentClock.contains(res.pc) && res.busy && res.busyCountDown == 0) {
                    res.busy = false;
                    this.instList.get(res.pc).record.put(InstStateName.WRITERESULT, clock);
                }
            }
        }
    }
}
