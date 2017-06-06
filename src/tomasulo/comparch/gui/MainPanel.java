package gui;

import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;

import javax.swing.*;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * Created by THU73 on 17/5/30.
 */
public class MainPanel {

    private JFrame frame;

    public DataTable insTable;
    public DataTable stateTable;
    public DataTable reserveTable;
    public DataTable memTable;
    public DataTable loadTable;
    public DataTable storeTable;
    public DataTable ruTable;
    public DataTable fuTable;

    private JButton addIns;
    private JButton delIns;
    private JButton addMem;
    private JButton delMem;
    private JButton stepButton;
    private JButton runButton;

    public MainPanel() {
        frame = new JFrame("tomasulo demo");
        frame.setLayout(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        insTable = new DataTable();
        stateTable = new DataTable();
        reserveTable = new DataTable();
        memTable = new DataTable();
        loadTable = new DataTable();
        storeTable = new DataTable();
        ruTable = new DataTable();
        fuTable = new DataTable();

        initTables();
        initButtons();

        frame.setVisible(true);
    }

    private void initTables() {
        String[][] insData = {{"LD", "F6", "34", "R2"},
                {"LD", "F2", "45", "R3"},
                {"MULD", "F0", "F2", "F4"},
                {"SUBD", "F8", "F6", "F2"},
                {"DIVD", "F10", "F0", "F6"},
                {"ADDD", "F6", "F8", "F2"}};
        String[] insColumn = {"ins", "Des", "Src_j", "Src_k"};
        initTable(insTable, insColumn, insData, "指令序列", 25, 40, 200, 150);
        //set insTable

        String[] stateColumn = {"发射指令", "执行完毕", "写回结果"};
        String[][] stateData = {};
        initTable(stateTable, stateColumn, stateData, "运行状态", 300, 40, 200, 150);
        //set stateTable

        String[] loadColumn = {"name", "busy", "address", "cache"};
        String[][] loadData = {{"load0", "NO", "", ""},
                {"load1", "NO", "", ""},
                {"load2", "NO", "", ""}};
        initTable(loadTable, loadColumn, loadData, "load队列", 550, 20, 200, 80);
        //set loadTable

        String[] storeColumn = {"name", "busy", "address", "cache"};
        String[][] storeData = {{"store0", "NO", "", ""},
                {"store1", "NO", "", ""},
                {"store2", "NO", "", ""}};
        initTable(storeTable, storeColumn, storeData, "store队列", 550, 120, 200, 80);
        //set storeTable

        String[] reserveColumn = {"time", "name", "busy", "Operation", "Vi", "Vk", "Qi", "Qk"};
        String[][] reserveData = {{"", "Add0", "NO", "", ""},
                {"", "Add1", "NO", "", ""},
                {"", "Add2", "NO", "", ""},
                {"", "Mul0", "NO", "", ""},
                {"", "Mul1", "NO", "", ""}};
        initTable(reserveTable, reserveColumn, reserveData, "保留站", 250, 230, 500, 100);
        //set reserveTable

        String[] memColumn = {"Addr", "Data"};
        String[][] memData = {};
        initTable(memTable, memColumn, memData, "内存单元", 25, 230, 200, 100);
        //set memTable

        String[] ruColumn = {"寄存器号", "数据"};
        String[][] ruData = {{"R0", ""}, {"R1", ""}, {"R2", ""}, {"R3", ""},
                {"R4", ""}, {"R5", ""}, {"R6", ""}, {"R7", ""},
                {"R8", ""}, {"R9", ""}, {"R10", ""}};
        initTable(ruTable, ruColumn, ruData, "整型寄存器", 25, 360, 150, 200);
        //set ruTable

        String[] fuColumn = {"寄存器号", "表达式", "数据"};
        String[][] fuData = {{"F0", "", ""}, {"F1", "", ""}, {"F2", "", ""}, {"F3", "", ""},
                {"F4", "", ""}, {"F5", "", ""}, {"F6", "", ""}, {"F7", "", ""},
                {"F8", "", ""}, {"F9", "", ""}, {"F10", "", ""}};
        initTable(fuTable, fuColumn, fuData, "浮点寄存器", 200, 360, 180, 200);
        //set fuTable
    }

    private void initTable(DataTable table, String[] column, String[][] data, String title,
                           int x, int y, int width, int height) {
        table.setHeader(column);
        table.setData(data);
        JScrollPane sp = new JScrollPane(table.table());
        sp.setBounds(x, y, width, height);
        frame.getContentPane().add(sp);

        JLabel label = new JLabel();
        label.setText(title);
        frame.getContentPane().add(label);
        label.setBounds(x + (int) (0.38 * width), y - 25, 100, 30);
    }

    private void initButtons() {
        addIns = new JButton("+");
        delIns = new JButton("-");
        addMem = new JButton("+");
        delMem = new JButton("-");
        stepButton = new JButton("单步执行");
        runButton = new JButton("连续执行");


        frame.getContentPane().add(addIns);
        frame.getContentPane().add(delIns);
        frame.getContentPane().add(addMem);
        frame.getContentPane().add(delMem);
        frame.getContentPane().add(stepButton);
        frame.getContentPane().add(runButton);

        addIns.setBounds(160, 15, 18, 18);
        delIns.setBounds(180, 15, 18, 18);
        addMem.setBounds(160, 210, 18, 18);
        delMem.setBounds(180, 210, 18, 18);
        stepButton.setBounds(500, 400, 100, 40);
        runButton.setBounds(650, 400, 100, 40);

        addIns.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] new_data = {"???", "???", "???", "???"};
                insTable.addRow(new_data);
            }
        });

        delIns.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Vector<Vector<String>> vec = insTable.getData();
                if (vec.size() > 0) {
                    vec.remove(vec.lastElement());
                    insTable.setData(vec);
                }

            }
        });

        addMem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] new_data = {"???", "???"};
                memTable.addRow(new_data);
            }
        });

        delMem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Vector<Vector<String>> vec = memTable.getData();
                if (vec.size() > 0) {
                    vec.remove(vec.lastElement());
                    memTable.setData(vec);
                }
            }
        });

        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
            }
        });
    }

}
