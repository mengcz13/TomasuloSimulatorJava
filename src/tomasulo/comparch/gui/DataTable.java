package tomasulo.comparch.gui;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;

/**
 * DataTable类负责存储当前数据表的数据。
 * 同时对外提供一些方便显示在主面板上的可能需要用到的函数。
 */
public class DataTable {

    /**
     * 数据表格。
     */
    private JTable table;

    /**
     * 数据模型对象。
     */
    private DefaultTableModel model;

    /**
     * 表头文字。
     */
    private String[] header;

    /**
     * 动作监听者。
     */
    private TableModelListener listener;

    /**
     * 表格标签文字。
     */
    private JLabel label;

    /**
     * 是否已显示在面板上。
     */
    private boolean added;


    /**
     * 构造函数。
     */
    public DataTable() {
        table = new JTable();
        label = new JLabel();
        model = new DefaultTableModel();
        table.setModel(model);
        added = false;
    }

    /**
     * 显示在面板上调用一次进行状态登记。
     */
    public void add() {
        added = true;
    }

    /**
     * 查询本表格是否已经显示在面板上。
     * @return 是否已经显示的boolean值。
     */
    public boolean isAdded() {
        return added;
    }

    /**
     * 设置表格表头。
     *
     * @param header 一个String数组指针，里面装有表头。
     */
    public void setHeader(String[] header) {
        if (this.header == null && header != null) {
            this.header = header;
            for (int i = 0; i < header.length; ++i) {
                model.addColumn(header[i]);
            }
        }
    }

    /**
     * 设置本表格的动作监听者。
     *
     * @param ml 一个TableModelListener，负责监听表格相应的变化。
     */
    public void setListener(TableModelListener ml) {
        if (this.listener == null) {
            this.listener = ml;
            model.addTableModelListener(ml);
        }
    }

    /**
     * 设置数据。
     *
     * @param data 一个String[][]，里面装有与表头对应的数据。
     */
    public void setData(String[][] data) {
        clear();
        for (int i = 0; i < data.length; ++i) {
            model.addRow(data[i]);
        }
    }

    /**
     * 获取table对象。
     *
     * @return 本表格的表格对象。
     */
    public JTable getTable() {
        return this.table;
    }

    /**
     * 获取label对象。
     *
     * @return 本表格的标签对象。
     */
    public JLabel getLabel() {
        return this.label;
    }

    /**
     * 获取数据。
     *
     * @return 表格中的数据。
     */
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

    /**
     * 清空数据。
     */
    public void clear() {
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
    }

    /**
     * 增加一行新的数据。
     *
     * @param data 要添加的新行的数据。
     */
    public void addRow(String[] data) {
        model.addRow(data);
    }

    /**
     * 设置标签内容。
     *
     * @param title 设置标签的title。
     */
    public void setText(String title) {
        label.setText(title);
    }
}
