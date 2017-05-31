package tomasulo.comparch.core;

/**
 * Created by neozero on 17-5-31.
 */
public class ReservationStation {

    public int pc;

    public int operatorName = -1;

    public boolean busy = false;

    public int busyCountDown;

    public double floatResult;

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
        floatResult = 0;
        qJ = qK = null;
        vStore = 0;
        qStore = null;
        addr = 0;
        intValue = 0;
    }
}
