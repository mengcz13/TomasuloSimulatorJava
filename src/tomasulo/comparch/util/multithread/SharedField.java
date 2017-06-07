package tomasulo.comparch.util.multithread;

public class SharedField{
	private int value;
	public static final int IDLE = 0;
	public static final int INIT = 1;
	public static final int STEP = 2;
	public static final int RUN = 3;
	public static final int TERMINATE = 4;
	public static final int SET_MEM = 5;
	public static final int SET_REG = 6;
	public void set(int v){
		value = v;
	}
	public int get(){
		return value;
	}
}