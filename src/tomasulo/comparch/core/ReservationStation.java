package tomasulo.comparch.core;

/**
 * Created by neozero on 17-5-31.
 */
public class ReservationStation {

    public int pc;

    public boolean busy = false;

    public int busyCountDown;

    public int operatorName = -1;

    public double floatResult;

    public double vJ;

    public double vK;

    public ReservationStation qJ;

    public ReservationStation qK;

    // For load and store only

    public double vStore;

    public ReservationStation qStore;

    public int addr;

    public int intValue;

    public ReservationStation() {
        reset();
    }

    public void reset() {
        pc = -1;
        busy = false;
        busyCountDown = 0;
        operatorName = -1;
        floatResult = vJ = vK = 0;
        qJ = qK = null;
        vStore = 0;
        qStore = null;
        addr = 0;
        intValue = 0;
    }
}
