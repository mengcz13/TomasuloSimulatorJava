package tomasulo.comparch.gui;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;

/**
 * Created by THU73 on 17/5/30.
 */
public class DataTable {

    private JTable table;
    private DefaultTableModel model;
    private String[] header;
    private TableModelListener listener;
    private JLabel label;

    public DataTable() {
        table = new JTable();
        label = new JLabel();
        model = new DefaultTableModel();
        table.setModel(model);
    }

    public void setHeader(String[] header) {
        if (this.header == null && header != null) {
            this.header = header;
            for (int i = 0; i < header.length; ++i) {
                model.addColumn(header[i]);
            }
        }
    }

    public void setListener(TableModelListener ml) {
        this.listener = ml;
    }

    public void setData(String[][] data) {
        clear();
        for (int i = 0; i < data.length; ++i) {
            model.addRow(data[i]);
        }
    }

    public String[][] getData() {
        Vector<Vector<String>> vec = model.getDataVector();
        String[][] data = new String[vec.size()][];
        for (int i = 0; i < vec.size(); ++i) {
            String[] sec_data = new String[vec.elementAt(i).size()];
            for (int j = 0; j < vec.elementAt(i).size(); ++j) {
                sec_data[j] = vec.elementAt(i).elementAt(j);
            }
            data[i] = sec_data;
        }
        return data;

    }

    public void clear() {
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
    }

    public JTable table() {

        return this.table;
    }

    public void setBounds(int x, int y, int width, int height) {
        table.setBounds(x, y, width, height);
    }

    public void addRow(String[] data) {
        model.addRow(data);
    }


    public void setText(String title) {
        label.setText(title);
    }
}
