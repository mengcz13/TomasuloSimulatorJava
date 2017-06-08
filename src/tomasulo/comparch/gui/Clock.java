package tomasulo.comparch.gui;

import javax.swing.*;

/**
 * Created by THU73 on 17/6/8.
 */
public class Clock extends JLabel {
    private int count;
    public Clock() {
        super();
        count = 0;
    }
    public void setTime(int time) {
        count = time;
        update();
    }

    public void clear() {
        count = 0;
        update();
    }
    public void update() {
        setText("周期计数：" + Integer.toString(count));
    }
}
