package tomasulo.comparch.util.pair;

/**
 * 基于KindValuePair实现的寄存器值映射类。
 */
public class RegisterPair extends KindValuePair {
    public RegisterPair() {
        super();
    }

    public RegisterPair(int n, int i) {
        super(n, i);
    }
}
