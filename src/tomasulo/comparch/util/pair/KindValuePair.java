package tomasulo.comparch.util.pair;

/**
 * Created by neozero on 17-5-31.
 */
public class KindValuePair {

    public int name;

    public int intValue;

    public KindValuePair(int n, int i) {
        name = n;
        intValue = i;
    }

    public KindValuePair() {
        reset();
    }

    public void reset() {
        name = 0;
        intValue = 0;
    }
}
