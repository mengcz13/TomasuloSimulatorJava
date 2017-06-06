package tomasulo.comparch.util.name;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by neozero on 17-6-6.
 */
public class ALUName {

    public static final int AddPipe = 0;

    public static final int MulPipe = AddPipe + 1;

    public static final int DivPipe = MulPipe + 1;

    public static final int LSPipe = DivPipe + 1;

    public static final Map<Integer, Integer> operALUMap;

    static {
        operALUMap = new HashMap<>();
        operALUMap.put(OperatorName.ADDD, AddPipe);
        operALUMap.put(OperatorName.SUBD, AddPipe);
        operALUMap.put(OperatorName.MULTD, MulPipe);
        operALUMap.put(OperatorName.DIVD, DivPipe);
        operALUMap.put(OperatorName.LD, LSPipe);
        operALUMap.put(OperatorName.ST, LSPipe);
    }

    public static final Map<Integer, List<Integer>> pipeStruct;

    static {
        pipeStruct = new HashMap<>();
        pipeStruct.put(AddPipe, new ArrayList<>(Arrays.asList(1, 1)));
        pipeStruct.put(MulPipe, new ArrayList<>(Arrays.asList(1, 1, 1, 1, 1, 5)));
        pipeStruct.put(DivPipe, new ArrayList<>(Arrays.asList(40)));
        pipeStruct.put(LSPipe, null);

        for (Map.Entry<Integer, List<Integer>> entry : pipeStruct.entrySet()) {
            List<Integer> accu = entry.getValue();
            if (accu != null) {
                int accumulate = 0;
                ListIterator<Integer> iter = accu.listIterator();
                while (iter.hasNext()) {
                    int curr = iter.next();
                    accumulate += curr;
                    iter.set(accumulate);
                }
            }
        }
    }
}
