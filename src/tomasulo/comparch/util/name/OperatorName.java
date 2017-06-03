package tomasulo.comparch.util.name;

import java.util.Map;
import java.util.HashMap;

import tomasulo.comparch.util.name.ReservationName;

/**
 * Created by neozero on 17-5-31.
 *
 * Define all operators.
 */
public class OperatorName {

    public static final int ADDD = InstStateName.WRITERESULT + 1;

    public static final int SUBD = ADDD + 1;

    public static final int MULTD = SUBD + 1;

    public static final int DIVD = MULTD + 1;

    public static final int LD = DIVD + 1;

    public static final int ST = LD + 1;

    public static final Map<String, Integer> operatorNameMap;

    static {
        operatorNameMap = new HashMap<String, Integer>();
        operatorNameMap.put("ADDD", ADDD);
        operatorNameMap.put("SUBD", SUBD);
        operatorNameMap.put("MULTD", MULTD);
        operatorNameMap.put("DIVD", DIVD);
        operatorNameMap.put("LD", LD);
        operatorNameMap.put("ST", ST);
    }

    public static final int OperatorNameNum = operatorNameMap.keySet().size();

    public static final Map<Integer, Integer> busyCountDownMap;

    static {
        busyCountDownMap = new HashMap<Integer, Integer>();
        busyCountDownMap.put(ADDD, 2);
        busyCountDownMap.put(SUBD, 2);
        busyCountDownMap.put(MULTD, 10);
        busyCountDownMap.put(DIVD, 40);
        busyCountDownMap.put(LD, 2);
        busyCountDownMap.put(ST, 2);
    }

    public static final Map<Integer, Integer> operatorReservationMap;

    static {
        operatorReservationMap = new HashMap<Integer, Integer>();
        operatorReservationMap.put(ADDD, ReservationName.ADD);
        operatorReservationMap.put(SUBD, ReservationName.ADD);
        operatorReservationMap.put(MULTD, ReservationName.MULT);
        operatorReservationMap.put(DIVD, ReservationName.MULT);
        operatorReservationMap.put(LD, ReservationName.LOAD);
        operatorReservationMap.put(ST, ReservationName.STORE);
    }
}
