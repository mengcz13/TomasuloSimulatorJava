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
     * 未初始化状态。
     */
	public static final int IDLE = 0;
	/**
	 * 初始化状态。
	 */
	public static final int INIT = 1;
	/**
	 * 单步调试状态。
	 */
	public static final int STEP = 2;
	/**
	 * 连续N步状态。
	 */
	public static final int STEPN = 3;
	/**
	 * 进行到底状态。
	 */
	public static final int RUN = 4;
	/**
	 * 运行完成状态。
	 */
	public static final int TERMINATE = 5;
	/**
	 * 设置内存状态。
	 */
	public static final int SET_MEM = 6;
	/**
	 * 设置寄存器状态。
	 */
	public static final int SET_REG = 7;

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