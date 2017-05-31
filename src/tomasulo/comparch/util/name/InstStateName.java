package tomasulo.comparch.util.name;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by neozero on 17-5-31.
 */
public class InstStateName {

    public static final int ISSUE = 0;

    public static final int EXECCOMP = ISSUE + 1;

    public static final int WRITERESULT = EXECCOMP + 1;

    public static final Map<String, Integer> instStateNameMap;

    static {
        instStateNameMap = new HashMap<String, Integer>();
        instStateNameMap.put("ISSUE", ISSUE);
        instStateNameMap.put("EXECCOMP", EXECCOMP);
        instStateNameMap.put("WRITERESULT", WRITERESULT);
    }

    public static final int InstStateNameNum = instStateNameMap.keySet().size();
}
