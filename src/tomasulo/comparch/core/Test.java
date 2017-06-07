package tomasulo.comparch.core;

import tomasulo.comparch.adaptor.Adaptor;
import tomasulo.comparch.gui.MainPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neozero on 17-6-1.
 */
public class Test {

    public static void main(String[] args) {
        MainPanel panel = new MainPanel();
        ArrayList<String> al = new ArrayList<>();
        Adaptor ad = new Adaptor(panel, al);

        Thread thread = new Thread(ad);
        thread.start();
    }
}
