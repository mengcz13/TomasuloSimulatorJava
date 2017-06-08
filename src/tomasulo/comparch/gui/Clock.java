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
    public void step() {
        count++;
        show();
    }
    public void clear() {
        count = 0;
        show();
    }
    public void show() {
        setText(Integer.toString(count));
    }
}
