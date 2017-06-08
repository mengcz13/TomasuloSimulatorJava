package tomasulo.comparch.gui;

import javax.swing.*;

/**
 * Clock类是一个计步器。
 * 该类负责存储并显示当前Tomasulo算法的已用周期数。
 */
public class Clock extends JLabel {
    /**
     * 当前已经过去的周期数。
     */
    private int count;

    /**
     * Clock构造函数。
     */
    public Clock() {
        super();
        count = 0;
    }

    /**
     * 设置周期。
     * @param time 要设定的周期值
     */
    public void setTime(int time) {
        count = time;
        update();
    }

    /**
     * 计步器归零。
     */
    public void clear() {
        count = 0;
        update();
    }
    /**
     * 刷新UI。
     */
    public void update() {
        setText("周期计数：" + Integer.toString(count));
    }
}
