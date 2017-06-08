package tomasulo.comparch.util.multithread;

/**
 * SharedField类是一个信号量模型。
 * <p>位于两个或以上的线程中的对象同时持有指向同一个SharedField的引用，就可以通过SharedField对象传递信息和实现同步。
 * </p>
 */
public class SharedField{
    /**
     * value是当前携带的消息值
     */
	private int value;
    /**
     * 定义了一些可用的消息
     */
	public static final int IDLE = 0;
	public static final int INIT = 1;
	public static final int STEP = 2;
	public static final int RUN = 3;
	public static final int TERMINATE = 4;
	public static final int SET_MEM = 5;
	public static final int SET_REG = 6;

    /**
     * set: 基本的set函数
     * @param v 要设置的值
     */
	public void set(int v){
		value = v;
	}

    /**
     * get: 基本的get函数
     * @return 信号量当前携带的消息
     */
	public int get(){
		return value;
	}
}