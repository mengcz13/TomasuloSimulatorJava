package tomasulo.comparch.util.name;

import java.util.Map;
import java.util.HashMap;

/**
 * 模拟指令状态。
 */
public class InstStateName {

    /**
     * 发射。
     */
    public static final int ISSUE = ALUName.LSPipe + 1;

    /**
     * 执行。
     */
    public static final int EXECCOMP = ISSUE + 1;

    /**
     * 等待结果。
     */
    public static final int WRITERESULT = EXECCOMP + 1;

    /**
     * 映射表。
     */
    public static final Map<String, Integer> instStateNameMap;

    static {
        instStateNameMap = new HashMap<String, Integer>();
        instStateNameMap.put("ISSUE", ISSUE);
        instStateNameMap.put("EXECCOMP", EXECCOMP);
        instStateNameMap.put("WRITERESULT", WRITERESULT);
    }

    public static final int InstStateNameNum = instStateNameMap.keySet().size();
}
