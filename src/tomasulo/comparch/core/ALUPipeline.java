package tomasulo.comparch.core;

import java.security.KeyException;
import java.util.*;

import tomasulo.comparch.util.name.ALUName;
import tomasulo.comparch.util.name.OperatorName;

/**
 * 流水线结构的实现. 流水线中的操作使用链表组织, FIFO.
 */
public class ALUPipeline {

    private Map<Integer, LinkedList<ReservationStation>> aluList;

    public ALUPipeline() {
        aluList = new HashMap<>();
        for (Integer aluname : ALUName.pipeStruct.keySet()) {
            aluList.put(aluname, new LinkedList<>());
        }
    }

    public void add(ReservationStation res) {
        assert res.busyCountDown == OperatorName.busyCountDownMap.get(res.operatorName);
        res.stage = 0;
        aluList.get(ALUName.operALUMap.get(res.operatorName)).addLast(res);
    }

    /**
     * 模拟一个clock中的流水线行为.
     *
     * 由于流水线中的保留站采用FIFO组织, 故对于每一条流水线从前到后扫描一次即可完成处理, 具体方法如下:
     * 尝试执行当前的保留站一拍, 若所处stage不变则继续, 若改变则判断下一个stage是否被占据, 不被占据时才能前进, 否则恢复原状态.
     * 具体实现中, 链表内除了进入流水线的保留站外还有等待进入者, 因此需要根据流水线段数控制循环, 同时注意每条流水线每次最多新进入一条指令,
     *
     * 对于未设置流水线的情况, 直接将countdown-1即可.
     *
     * @return 本clock中完成的所有Reservation Station.
     */
    public List<ReservationStation> process() {
        LinkedList<ReservationStation> outress = new LinkedList<>();
        for (Map.Entry<Integer, LinkedList<ReservationStation>> entry : aluList.entrySet()) {
            int pipename = entry.getKey();
            LinkedList<ReservationStation> pipeline = entry.getValue();
            List<Integer> pipeStruct = ALUName.pipeStruct.get(pipename);

            if (pipeStruct == null) {   // 无流水线
                Iterator<ReservationStation> iter = pipeline.iterator();
                while (iter.hasNext()) {
                    ReservationStation curr = iter.next();
                    int hasbeen = OperatorName.busyCountDownMap.get(curr.operatorName) - curr.busyCountDown;
                    boolean fresh = (hasbeen == 0);
                    ++hasbeen;
                    --curr.busyCountDown;
                    if (curr.busyCountDown == 0) {
                        ++curr.stage;
                    }
                    if (curr.busyCountDown == 0) {
                        outress.addLast(curr);
                        iter.remove();
                    }
                    if (fresh) {
                        curr.inArithm = true;
                        break;
                    }
                }
            } else {
                int laststage = pipeStruct.size();
                Iterator<ReservationStation> iter = pipeline.iterator();
                int numInPipe = 0;
                while (iter.hasNext() && numInPipe < pipeStruct.size()) {
                    ReservationStation curr = iter.next();
                    int maxcurr = pipeStruct.get(curr.stage);
                    int hasbeen = OperatorName.busyCountDownMap.get(curr.operatorName) - curr.busyCountDown;
                    boolean fresh = (curr.stage == 0);  // 这里保证流水线第一阶段每次最多处理1条指令, 避免出现第一阶段需要多周期完成时2条指令进入同一段流水的情况.
                    ++hasbeen;
                    --curr.busyCountDown;
                    ++numInPipe;
                    if (hasbeen >= maxcurr) {
                        int nextstage = curr.stage + 1;
                        if (nextstage == pipeStruct.size()) {
                            curr.stage = nextstage;
                            outress.addLast(curr);
                            iter.remove();
                            --numInPipe;
                        } else if (laststage != nextstage) {
                            curr.stage = nextstage;
                        } else {
                            --hasbeen;
                            ++curr.busyCountDown;
                        }
                    } else {
                        break;
                    }
                    laststage = curr.stage;
                    if (fresh) {
                        curr.inArithm = true;
                        break;
                    }
                }
            }
        }
        return outress;
    }
}
