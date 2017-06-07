package tomasulo.comparch.core;

import java.security.KeyException;
import java.util.*;

import tomasulo.comparch.util.name.ALUName;
import tomasulo.comparch.util.name.OperatorName;

/**
 * Created by neozero on 17-6-6.
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
                while (iter.hasNext() && numInPipe <= pipeStruct.size()) {
                    ReservationStation curr = iter.next();
                    int maxcurr = pipeStruct.get(curr.stage);
                    int hasbeen = OperatorName.busyCountDownMap.get(curr.operatorName) - curr.busyCountDown;
                    boolean fresh = (hasbeen == 0);
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
