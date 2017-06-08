package tomasulo.comparch.util.name;

import java.util.Map;
import java.util.HashMap;

/**
 * 模拟保留站。
 */
public class ReservationName {

    /**
     * 加法保留站。
     */
    public static final int ADD = RegisterName.INSTVALUE + 1;

    /**
     * 乘法保留站。
     */
    public static final int MULT = ADD + 1;

    /**
     * Load保留站。
     */
    public static final int LOAD = MULT + 1;

    /**
     * Store保留站。
     */
    public static final int STORE = LOAD + 1;

    /**
     * 浮点数立即数。
     */
    public static final int FLOATVALUE = STORE + 1;

    /**
     * 映射表。
     */
    public static final Map<Integer, String> reservationNameMap;

    static {
        reservationNameMap = new HashMap<Integer, String>();
        reservationNameMap.put(ADD, "ADD");
        reservationNameMap.put(MULT, "MULT");
        reservationNameMap.put(LOAD, "LOAD");
        reservationNameMap.put(STORE, "STORE");
        reservationNameMap.put(FLOATVALUE, "FLOATVALUE");
    }

    public static final int ReservationNameNum = reservationNameMap.keySet().size();

    public static final Map<Integer, Integer> reservationItem;

    static {
        reservationItem = new HashMap<Integer, Integer>();
        reservationItem.put(ADD, 3);
        reservationItem.put(MULT, 2);
        reservationItem.put(LOAD, 3);
        reservationItem.put(STORE, 3);
    }
}
