package tomasulo.comparch.core;

import tomasulo.comparch.adaptor.Adaptor;
import tomasulo.comparch.gui.MainPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * Main函数入口。
 */
public class Main {

    public static void main(String[] args) {
        MainPanel panel = new MainPanel();
        Adaptor ad = new Adaptor(panel);

        Thread thread = new Thread(ad);
        thread.start();
    }
}
