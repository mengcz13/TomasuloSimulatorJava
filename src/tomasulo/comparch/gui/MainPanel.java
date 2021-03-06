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
 * 主面板类，负责大部分UI元件的维护与逻辑执行。
 */
public class MainPanel {

    /**
     * 当前UI面板的运行状态，方便进行一些UI交互的状态判定。
     */
    private int proState;
    /**
     * 未初始化。
     */
    public static final int SETTING = 0;
    /**
     * 已初始化。
     */
    public static final int INITTED = 1;
    /**
     * 正在连续运行。
     */
    public static final int RUNNING = 2;

    /**
     * 默认指令表头。
     */
    private static final String[][] defaultIns = {{"LD", "F6", "34", "R2"},
            {"LD", "F2", "45", "R3"},
            {"MULD", "F0", "F2", "F4"},
            {"SUBD", "F8", "F6", "F2"},
            {"DIVD", "F10", "F0", "F6"},
            {"ADDD", "F6", "F8", "F2"}};
    /**
     * 默认状态表头。
     */
    private static final String[][] defaultState = {};
    /**
     * 默认load表头。
     */
    private static final String[][] defaultLoad = {{"load0", "NO", "", ""},
            {"load1", "NO", "", ""},
            {"load2", "NO", "", ""}};
    /**
     * 默认store表头。
     */
    private static final String[][] defaultStore = {{"store0", "NO", "", ""},
            {"store1", "NO", "", ""},
            {"store2", "NO", "", ""}};
    /**
     * 默认保留站表头。
     */
    private static final String[][] defaultReserve = {{"", "Add0", "NO", "", ""},
            {"", "Add1", "NO", "", ""},
            {"", "Add2", "NO", "", ""},
            {"", "Mul0", "NO", "", ""},
            {"", "Mul1", "NO", "", ""}};
    /**
     * 默认内存表头。
     */
    private static final String[][] defaultMem = {};
    /**
     * 默认整数寄存器表头。
     */
    private static final String[][] defaultFu = {{"F0", "", ""}, {"F1", "", ""}, {"F2", "", ""}, {"F3", "", ""},
            {"F4", "", ""}, {"F5", "", ""}, {"F6", "", ""}, {"F7", "", ""},
            {"F8", "", ""}, {"F9", "", ""}, {"F10", "", ""}, {"F11", "", ""},
            {"F12", "", ""}, {"F13", "", ""}, {"F14", "", ""}, {"F15", "", ""},
            {"F16", "", ""}, {"F17", "", ""}, {"F18", "", ""}, {"F19", "", ""}};
    /**
     * 默认浮点寄存器表头。
     */
    private static final String[][] defaultRu = {{"R0", ""}, {"R1", ""}, {"R2", ""}, {"R3", ""},
            {"R4", ""}, {"R5", ""}, {"R6", ""}, {"R7", ""},
            {"R8", ""}, {"R9", ""}, {"R10", ""}, {"R11", ""},
            {"R12", ""}, {"R13", ""}, {"R14", ""}, {"R15", ""},
            {"R16", ""}, {"R17", ""}, {"R18", ""}, {"R19", ""}};

    /**
     * 主面板。
     */
    private JFrame frame;

    /**
     * 指令表格。
     */
    public DataTable insTable;
    /**
     * 状态表格。
     */
    public DataTable stateTable;
    /**
     * 保留站表格。
     */
    public DataTable reserveTable;
    /**
     * 内存表格。
     */
    public DataTable memTable;
    /**
     * load表格。
     */
    public DataTable loadTable;
    /**
     * store表格。
     */
    public DataTable storeTable;
    /**
     * 整数寄存器表格。
     */
    public DataTable ruTable;
    /**
     * 浮点寄存器表格。
     */
    public DataTable fuTable;

    /**
     * 计步器。
     */
    public Clock clock;

    /**
     * 添加指令按钮。
     */
    private JButton addIns;
    /**
     * 删除指令按钮。
     */
    private JButton delIns;
    /**
     * 添加内存按钮。
     */
    private JButton addMem;
    /**
     * 删除内存按钮。
     */
    private JButton delMem;
    /**
     * 单步执行按钮。
     */
    private JButton stepButton;
    /**
     * 执行到底按钮。
     */
    private JButton runButton;
    /**
     * 读取文件按钮。
     */
    private JButton loadFile;
    /**
     * 应用按钮。
     */
    private JButton init;
    /**
     * 使用默认参数按钮。
     */
    private JButton setDefault;
    /**
     * 连续执行n步按钮。
     */
    private JButton stepnButton;

    /**
     * 文本框。（用来输入连续执行的步数）
     */
    public JTextField textField;

    /**
     * Adaptor
     */
    public Adaptor adaptor;

    /**
     * 构造函数。
     */
    public MainPanel() {
        frame = new JFrame("Tomasulo Demo");
        frame.setLayout(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        proState = SETTING;

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
        clock.setBounds(650, 400, 100, 50);

        initTables(false);
        initButtons();
        initLabels();
        initTextField();

        updateState();

        frame.setVisible(true);
    }

    /**
     * 设置Adaptor
     *
     * @param adaptor 要设置的Adaptor
     */
    public void setAdaptor(Adaptor adaptor) {
        this.adaptor = adaptor;
    }

    /**
     * 终止函数。
     */
    public void terminate() {
        proState = SETTING;
        updateState();
    }

    /**
     * 单步终止。
     */
    public void restoreFree() {
        proState = INITTED;
        updateState();
    }

    /**
     * 初始化各个表格。
     *
     * @param isDefault true:表示第一次初始化, false:表示之后重复初始化
     */
    private void initTables(boolean isDefault) {
        String[] insColumn = {"ins", "Des", "Src_j", "Src_k"};
        String[][] noDefault = {};
        initTable(insTable, insColumn, isDefault ? defaultIns : noDefault, "指令序列",
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
        //set ruTable

        String[] fuColumn = {"寄存器号", "表达式", "数据"};
        initTable(fuTable, fuColumn, defaultFu, "浮点寄存器",
                200, 360, 180, 200);
        //set fuTable

        reserveTable.getTable().setEnabled(false);
        storeTable.getTable().setEnabled(false);
        loadTable.getTable().setEnabled(false);
        stateTable.getTable().setEnabled(false);
        ruTable.getTable().setEnabled(false);
        fuTable.getTable().setEnabled(false);
        memTable.getTable().setEnabled(false);
        insTable.getTable().setEnabled(false);
    }

    /**
     * 初始化单个表格。
     *
     * @param table  要初始化的表格
     * @param column 要初始化的表头
     * @param data   要初始化的数据
     * @param title  初始化的表格标题
     * @param x      横坐标
     * @param y      纵坐标
     * @param width  宽度
     * @param height 高度
     */
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

    /**
     * 初始化按钮。
     */
    private void initButtons() {
        addIns = new JButton("+");
        delIns = new JButton("-");
        addMem = new JButton("+");
        delMem = new JButton("-");
        stepButton = new JButton("单步执行");
        runButton = new JButton("执行到底");
        loadFile = new JButton("读取指令文本");
        init = new JButton("应用");
        setDefault = new JButton("使用默认数据");
        stepnButton = new JButton("连续执行n步");


        frame.getContentPane().add(addIns);
        frame.getContentPane().add(delIns);
        frame.getContentPane().add(addMem);
        frame.getContentPane().add(delMem);
        frame.getContentPane().add(stepButton);
        frame.getContentPane().add(runButton);
        frame.getContentPane().add(loadFile);
        frame.getContentPane().add(init);
        frame.getContentPane().add(setDefault);
        frame.getContentPane().add(stepnButton);

        addIns.setBounds(160, 15, 18, 18);
        delIns.setBounds(180, 15, 18, 18);
        addMem.setBounds(160, 210, 18, 18);
        delMem.setBounds(180, 210, 18, 18);
        setDefault.setBounds(400, 350, 100, 40);
        stepButton.setBounds(400, 510, 100, 40);
        runButton.setBounds(510, 510, 100, 40);
        loadFile.setBounds(510, 350, 100, 40);
        init.setBounds(450, 430, 100, 40);
        stepnButton.setBounds(620, 510, 100, 40);

        addIns.addActionListener(al);
        delIns.addActionListener(al);
        addMem.addActionListener(al);
        delMem.addActionListener(al);
        stepButton.addActionListener(al);
        runButton.addActionListener(al);
        loadFile.addActionListener(al);
        init.addActionListener(al);
        setDefault.addActionListener(al);
        stepnButton.addActionListener(al);
    }

    /**
     * 初始化标签。
     */
    private void initLabels() {
        JLabel[] labels = new JLabel[4];
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                labels[i * 2 + j] = new JLabel("↓");
                frame.getContentPane().add(labels[i * 2 + j]);
                labels[i * 2 + j].setBounds(460 + j * 70, 400 + i * 80, 20, 20);
            }
        }
    }

    /**
     * 初始化文本框。
     */
    private void initTextField() {
        textField = new JTextField();
        frame.getContentPane().add(textField);
        textField.setBounds(650, 480, 40, 20);
    }

    /**
     * 一个监听按钮变化事件的监听类。
     */
    private ActionListener al = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == addIns) {
                String str = JOptionPane.showInputDialog("输入内存地址和对应的值，中间用空格分开。\n" +
                        "如： MULD F1 F2 R2");
                String[] new_data = str.split(" ");
                if (isGoodInstruction(new_data)) {
                    insTable.addRow(new_data);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "请输入正确的指令！",
                            "指令错误",
                            JOptionPane.ERROR_MESSAGE);
                }

            } else if (e.getSource() == delIns) {
                String[][] vec = insTable.getData();
                String[][] new_vec = new String[vec.length - 1][];
                for (int i = 0; i < vec.length - 1; ++i) {
                    new_vec[i] = vec[i];
                }
                insTable.setData(new_vec);

            } else if (e.getSource() == addMem) {
                String str = JOptionPane.showInputDialog("输入内存地址和对应的值，中间用空格分开。\n" +
                        "如： 0xfff(3位16进制地址) 1.22(任意数字)");
                String[] new_data = str.split(" ");
                if (isGoodMemory(new_data)) {
                    memTable.addRow(new_data);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "请输入正确的内存数据！",
                            "数据错误",
                            JOptionPane.ERROR_MESSAGE);
                }
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
                proState = RUNNING;
                updateState();
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
                clock.clear();
                proState = INITTED;
                updateState();
                synchronized (adaptor.operation) {
                    adaptor.operation.set(SharedField.INIT);
                    adaptor.operation.notify();
                }

            } else if (e.getSource() == setDefault) {
                initTables(true);
            } else if (e.getSource() == stepnButton) {
                String text = textField.getText();
                try {
                    int stepVal = Integer.parseInt(text);
                    if (stepVal <= 0) throw new Exception();
                    proState = RUNNING;
                    updateState();
                    synchronized (adaptor.operation) {
                        adaptor.operation.set(SharedField.STEPN);
                        adaptor.operation.notify();
                    }
                } catch (Exception error) {
                    JOptionPane.showMessageDialog(null,
                            "请输入正确的步数！",
                            "步数错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    };

    /**
     * 一个监听按钮变化事件的监听类。
     */
    private TableModelListener ml = new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent e) {
            if (proState == INITTED) {
                synchronized (adaptor.operation) {
                    System.out.println("set mem");
                    adaptor.operation.set(SharedField.SET_MEM);
                    adaptor.operation.notify();
                }
            }
        }
    };

    /**
     * 测试一条指令是否是合法的指令。
     *
     * @param data 指令分割后的数组。
     * @return 是否合法的boolean值。
     */
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

    /**
     * 测试一项内存数据是否合法。
     *
     * @param data 数据分割后的数组。
     * @return 是否合法的boolean值。
     */
    private boolean isGoodMemory(String[] data) {
        if (data.length != 2) return false;
        if (data[0].length() > 3) return false;
        for (int i = 0; i < data[0].length(); ++i) {
            char cc = data[0].charAt(i);
            if (cc != 'x' && cc != '0' && cc != '1' && cc != '2' && cc != '3' && cc != '4' &&
                    cc != '5' && cc != '6' && cc != '7' && cc != '8' && cc != '9' && cc != 'a' &&
                    cc != 'b' && cc != 'c' && cc != 'd' && cc != 'e' && cc != 'f' && cc != 'A' &&
                    cc != 'B' && cc != 'C' && cc != 'D' && cc != 'E' && cc != 'F') {
                return false;
            }
        }
        try {
            float res = Float.parseFloat(data[1]);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 读取文件。
     *
     * @return String[][] 如果读取成功，返回文件指令内容。
     * @throws IOException 如果出错，抛出异常，在上一层进行处理。
     */
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

    /**
     * 根据当前的state更新各按钮的可用情况。
     */
    private void updateState() {
        if (proState == SETTING) {
            stepButton.setEnabled(false);
            runButton.setEnabled(false);
            stepnButton.setEnabled(false);
            setDefault.setEnabled(true);
            loadFile.setEnabled(true);
            init.setEnabled(true);
        } else if (proState == INITTED) {
            stepButton.setEnabled(true);
            runButton.setEnabled(true);
            stepnButton.setEnabled(true);
            setDefault.setEnabled(false);
            loadFile.setEnabled(false);
            init.setEnabled(false);
        } else if (proState == RUNNING) {
            stepnButton.setEnabled(false);
            stepButton.setEnabled(false);
            runButton.setEnabled(false);
            setDefault.setEnabled(false);
            loadFile.setEnabled(false);
            init.setEnabled(false);
        }
    }

}
