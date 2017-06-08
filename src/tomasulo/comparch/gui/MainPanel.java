package tomasulo.comparch.gui;

import tomasulo.comparch.adaptor.Adaptor;
import tomasulo.comparch.util.multithread.SharedField;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Vector;

/**
 * Created by THU73 on 17/5/30.
 */
public class MainPanel {

    private static final String[][] defaultIns = {{"LD", "F6", "34", "R2"},
            {"LD", "F2", "45", "R3"},
            {"MULD", "F0", "F2", "F4"},
            {"SUBD", "F8", "F6", "F2"},
            {"DIVD", "F10", "F0", "F6"},
            {"ADDD", "F6", "F8", "F2"}};
    private static final String[][] defaultState = {};
    private static final String[][] defaultLoad = {{"load0", "NO", "", ""},
            {"load1", "NO", "", ""},
            {"load2", "NO", "", ""}};
    private static final String[][] defaultStore = {{"store0", "NO", "", ""},
            {"store1", "NO", "", ""},
            {"store2", "NO", "", ""}};
    private static final String[][] defaultReserve = {{"", "Add0", "NO", "", ""},
            {"", "Add1", "NO", "", ""},
            {"", "Add2", "NO", "", ""},
            {"", "Mul0", "NO", "", ""},
            {"", "Mul1", "NO", "", ""}};
    private static final String[][] defaultMem = {};
    private static final String[][] defaultFu = {{"F0", "", ""}, {"F1", "", ""}, {"F2", "", ""}, {"F3", "", ""},
            {"F4", "", ""}, {"F5", "", ""}, {"F6", "", ""}, {"F7", "", ""},
            {"F8", "", ""}, {"F9", "", ""}, {"F10", "", ""}, {"F11", "", ""},
            {"F12", "", ""}, {"F13", "", ""}, {"F14", "", ""}, {"F15", "", ""},
            {"F16", "", ""}, {"F17", "", ""}, {"F18", "", ""}, {"F19", "", ""},};
    private static final String[][] defaultRu = {{"R0", ""}, {"R1", ""}, {"R2", ""}, {"R3", ""},
            {"R4", ""}, {"R5", ""}, {"R6", ""}, {"R7", ""},
            {"R8", ""}, {"R9", ""}, {"R10", ""}, {"R11", ""},
            {"R12", ""}, {"R13", ""}, {"R14", ""}, {"R15", ""},
            {"R16", ""}, {"R17", ""}, {"R18", ""}, {"R19", ""},};

    private JFrame frame;

    public DataTable insTable;
    public DataTable stateTable;
    public DataTable reserveTable;
    public DataTable memTable;
    public DataTable loadTable;
    public DataTable storeTable;
    public DataTable ruTable;
    public DataTable fuTable;
    public Clock clock;

    private JButton addIns;
    private JButton delIns;
    private JButton addMem;
    private JButton delMem;
    private JButton stepButton;
    private JButton runButton;
    private JButton loadFile;
    private JButton init;
    private JButton setDefault;

    public Adaptor adaptor;


    public MainPanel() {
        frame = new JFrame("Tomasulo Demo");
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

        clock = new Clock();
        frame.getContentPane().add(clock);
        clock.update();
        clock.setBounds(600, 450, 50, 50);

        initTables();
        initButtons();

        frame.setVisible(true);
    }

    public void setAdaptor(Adaptor adaptor) {
        this.adaptor = adaptor;
    }

    private void initTables() {
        String[] insColumn = {"ins", "Des", "Src_j", "Src_k"};
        initTable(insTable, insColumn, defaultIns, "指令序列",
                25, 40, 200, 150);
        //set insTable

        String[] stateColumn = {"发射指令", "执行完毕", "写回结果"};
        initTable(stateTable, stateColumn, defaultState, "运行状态",
                300, 40, 200, 150);
        //set stateTable

        String[] loadColumn = {"name", "busy", "address", "cache"};
        initTable(loadTable, loadColumn, defaultLoad, "load队列",
                550, 20, 200, 80);
        //set loadTable

        String[] storeColumn = {"name", "busy", "address", "cache"};
        initTable(storeTable, storeColumn, defaultStore, "store队列",
                550, 120, 200, 80);
        //set storeTable

        String[] reserveColumn = {"time", "name", "busy", "Operation", "Vi", "Vk", "Qi", "Qk"};
        initTable(reserveTable, reserveColumn, defaultReserve, "保留站",
                250, 230, 500, 100);
        //set reserveTable

        String[] memColumn = {"Addr", "Data"};
        initTable(memTable, memColumn, defaultMem, "内存单元",
                25, 230, 200, 100);
        memTable.setListener(ml);
        //set memTable

        String[] ruColumn = {"寄存器号", "数据"};
        initTable(ruTable, ruColumn, defaultRu, "整型寄存器",
                25, 360, 150, 200);
        ruTable.setListener(ml);
        //set ruTable

        String[] fuColumn = {"寄存器号", "表达式", "数据"};
        initTable(fuTable, fuColumn, defaultFu, "浮点寄存器",
                200, 360, 180, 200);
        //set fuTable

        reserveTable.getTable().setEnabled(false);
        storeTable.getTable().setEnabled(false);
        loadTable.getTable().setEnabled(false);
        stateTable.getTable().setEnabled(false);
    }

    private void initTable(DataTable table, String[] column, String[][] data, String title,
                           int x, int y, int width, int height) {
        if (!table.isAdded()) {
            table.setHeader(column);
            JScrollPane sp = new JScrollPane(table.getTable());
            sp.setBounds(x, y, width, height);
            frame.getContentPane().add(sp);
            table.setText(title);
            table.setData(data);
            frame.getContentPane().add(table.getLabel());
            table.getLabel().setBounds(x + (int) (0.38 * width), y - 25, 100, 30);
            table.add();
            return;
        }
        table.setData(data);
    }

    private void initButtons() {
        addIns = new JButton("+");
        delIns = new JButton("-");
        addMem = new JButton("+");
        delMem = new JButton("-");
        stepButton = new JButton("单步执行");
        runButton = new JButton("连续执行");
        loadFile = new JButton("读取指令文本");
        init = new JButton("初始化数据");
        setDefault = new JButton("恢复默认参数");


        frame.getContentPane().add(addIns);
        frame.getContentPane().add(delIns);
        frame.getContentPane().add(addMem);
        frame.getContentPane().add(delMem);
        frame.getContentPane().add(stepButton);
        frame.getContentPane().add(runButton);
        frame.getContentPane().add(loadFile);
        frame.getContentPane().add(init);
        frame.getContentPane().add(setDefault);

        addIns.setBounds(160, 15, 18, 18);
        delIns.setBounds(180, 15, 18, 18);
        addMem.setBounds(160, 210, 18, 18);
        delMem.setBounds(180, 210, 18, 18);
        setDefault.setBounds(500, 400, 100, 40);
        stepButton.setBounds(500, 450, 100, 40);
        runButton.setBounds(500, 500, 100, 40);
        loadFile.setBounds(650, 400, 100, 40);
        init.setBounds(650, 450, 100, 40);

        addIns.addActionListener(al);
        delIns.addActionListener(al);
        addMem.addActionListener(al);
        delMem.addActionListener(al);
        stepButton.addActionListener(al);
        runButton.addActionListener(al);
        loadFile.addActionListener(al);
        init.addActionListener(al);
        setDefault.addActionListener(al);

        stepButton.setEnabled(false);
        runButton.setEnabled(false);
    }

    public ActionListener al = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == addIns) {
                String[] new_data = {"???", "???", "???", "???"};
                insTable.addRow(new_data);

            } else if (e.getSource() == delIns) {
                String[][] vec = insTable.getData();
                String[][] new_vec = new String[vec.length - 1][];
                for (int i = 0; i < vec.length - 1; ++i) {
                    new_vec[i] = vec[i];
                }
                insTable.setData(new_vec);

            } else if (e.getSource() == addMem) {
                String[] new_data = {"???", "???"};
                memTable.addRow(new_data);

            } else if (e.getSource() == delMem) {
                String[][] vec = memTable.getData();
                String[][] new_vec = new String[vec.length - 1][];
                for (int i = 0; i < vec.length - 1; ++i) {
                    new_vec[i] = vec[i];
                }
                memTable.setData(new_vec);

            } else if (e.getSource() == stepButton) {
                synchronized (adaptor.operation) {
                    adaptor.operation.set(SharedField.STEP);
                    adaptor.operation.notify();
                }
            } else if (e.getSource() == runButton) {
                synchronized (adaptor.operation) {
                    adaptor.operation.set(SharedField.RUN);
                    adaptor.operation.notify();
                }
            } else if (e.getSource() == loadFile) {
                try {
                    String[][] data = loadFileData();
                    if (data != null) {
                        insTable.setData(data);
                    }
                } catch (IOException error) {
                    JOptionPane.showMessageDialog(null,
                            "请选择正确的文件！",
                            "文件错误",
                            JOptionPane.ERROR_MESSAGE);
                    error.printStackTrace();
                }
            } else if (e.getSource() == init) {
                if (checkLegality()) {
                    clock.clear();
                    stepButton.setEnabled(true);
                    runButton.setEnabled(true);
                    synchronized (adaptor.operation) {
                        adaptor.operation.set(SharedField.INIT);
                        adaptor.operation.notify();
                    }
                }
            } else if(e.getSource() == setDefault) {
                initTables();
            }
        }
    };

    public TableModelListener ml = new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent e) {
            if (e.getSource() == memTable) {
                if (checkMemLegality(e)) {
                    synchronized (adaptor.operation) {
                        adaptor.operation.set(SharedField.SET_MEM);
                        adaptor.operation.notify();
                    }
                }
            } else if (e.getSource() == ruTable) {
                if (checkRegLegality(e)) {
                    synchronized (adaptor.operation) {
                        adaptor.operation.set(SharedField.SET_REG);
                        adaptor.operation.notify();
                    }
                }
            } else if(e.getSource() == fuTable) {
                
            }
        }
    };

    private boolean isGoodInstruction(String[] data) {
        if (data.length != 4) return false;
        if (data[0].equals("ADDD")
                || data[0].equals("SUBD")
                || data[0].equals("MULD")
                || data[0].equals("DIVD")
                || data[0].equals("LD")
                || data[0].equals("ST")) {
            return true;
        }
        return false;
    }

    private boolean checkLegality() {
        return true;
    }

    private boolean checkMemLegality(TableModelEvent e) {

        return true;
    }

    private boolean checkRegLegality(TableModelEvent e) {
        return true;
    }

    private boolean checkFuLegality(TableModelEvent e) {

    }

    private String[][] loadFileData() throws IOException {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("请选择指令文件");
        fc.setFileFilter(new FileNameExtensionFilter("文本文件(\".txt\")", "txt"));
        int retVal = fc.showOpenDialog(frame);

        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file));
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt;
                Vector<String[]> vec = new Vector<>();
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    String[] data = lineTxt.split(" ");
                    if (!isGoodInstruction(data)) {
                        throw new IOException();
                    }
                    vec.add(data);
                }
                String[][] data = new String[vec.size()][];
                for (int i = 0; i < vec.size(); ++i) {
                    data[i] = vec.elementAt(i);
                }
                read.close();
                return data;
            }
        }
        return null;
    }
}
