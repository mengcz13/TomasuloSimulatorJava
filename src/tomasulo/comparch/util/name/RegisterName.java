package tomasulo.comparch.util.name;

import java.util.Map;
import java.util.HashMap;

/**
 * 模拟寄存器。
 */
public class RegisterName {

    /**
     * 浮点寄存器。
     */
    public static final int FLOAT = OperatorName.ST + 1;

    /**
     * 整数寄存器。
     */
    public static final int INT = FLOAT + 1;

    /**
     * 立即数。
     */
    public static final int INSTVALUE = INT + 1;

    public static final Map<String, Integer> registerNameMap;

    static {
        registerNameMap = new HashMap<String, Integer>();
        registerNameMap.put("FLOAT", FLOAT);
        registerNameMap.put("INT", INT);
        registerNameMap.put("INSTVALUE", INSTVALUE);
    }

    public static final int RegisterNameNum = registerNameMap.keySet().size();

    public static final Map<Integer, Integer> registerItem;

    static {
        registerItem = new HashMap<Integer, Integer>();
        registerItem.put(FLOAT, 20);
        registerItem.put(INT, 20);
    }
}
