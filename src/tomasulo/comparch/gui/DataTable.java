package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;

/**
 * Created by THU73 on 17/5/30.
 */
public class DataTable {

    private JTable table;
    private DefaultTableModel model;
    private String[] header;

    public DataTable() {
        table = new JTable();
        model = new DefaultTableModel();
        table.setModel(model);
    }

    public void setHeader(String[] header) {
        this.header = header;
        if (header != null) {
            for (int i = 0; i < header.length; ++i) {
                model.addColumn(header[i]);
            }
        }
    }

    public void setData(String[][] data) {
        clear();
        for (int i = 0; i < data.length; ++i) {
            model.addRow(data[i]);
        }
    }

    public void setData(Vector<Vector<String>> vec) {
        clear();
        for(int i = 0; i <vec.size(); ++i) {
            model.addRow(vec.elementAt(i));
        }
    }

    public Vector getData() {
        return model.getDataVector();
    }

    public void clear() {
        table.removeAll();
        model = new DefaultTableModel();
        table.setModel(model);
        setHeader(header);
    }

    public JTable table() {
        return this.table;
    }

    public void setBounds(int x, int y, int width, int height) {
        //table.setPreferredScrollableViewportSize(new Dimension(width,height));
        //table.setLocation(x, y);
        table.setBounds(x, y, width, height);
    }

    public void addRow(String[] data) {
        model.addRow(data);
    }
}
