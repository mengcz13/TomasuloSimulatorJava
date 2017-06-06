package tomasulo.comparch.util.name;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by neozero on 17-5-31.
 *
 * Define all kinds of reservation stations.
 */
public class ReservationName {

    public static final int ADD = RegisterName.INSTVALUE + 1;

    public static final int MULT = ADD + 1;

    public static final int LOAD = MULT + 1;

    public static final int STORE = LOAD + 1;

    public static final int FLOATVALUE = STORE + 1;

    public static final Map<String, Integer> reservationNameMap;

    static {
        reservationNameMap = new HashMap<String, Integer>();
        reservationNameMap.put("ADD", ADD);
        reservationNameMap.put("MULT", MULT);
        reservationNameMap.put("LOAD", LOAD);
        reservationNameMap.put("STORE", STORE);
        reservationNameMap.put("FLOATVALUE", FLOATVALUE);
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
