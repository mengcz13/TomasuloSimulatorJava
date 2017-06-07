package tomasulo.comparch.gui;


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
    private JButton loadFile;
    private JButton init;

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
        memTable.setListener(ml);
        //set memTable

        String[] ruColumn = {"寄存器号", "数据"};
        String[][] ruData = {{"R0", ""}, {"R1", ""}, {"R2", ""}, {"R3", ""},
                {"R4", ""}, {"R5", ""}, {"R6", ""}, {"R7", ""},
                {"R8", ""}, {"R9", ""}, {"R10", ""}};
        initTable(ruTable, ruColumn, ruData, "整型寄存器", 25, 360, 150, 200);
        ruTable.setListener(ml);
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

    public TableModelListener ml = new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent e) {
            if (e.getSource() == memTable) {

            } else if (e.getSource() == ruTable) {

            }
        }
    };

    private void initButtons() {
        addIns = new JButton("+");
        delIns = new JButton("-");
        addMem = new JButton("+");
        delMem = new JButton("-");
        stepButton = new JButton("单步执行");
        runButton = new JButton("连续执行");
        loadFile = new JButton("读取指令文本");
        init = new JButton("初始化数据");


        frame.getContentPane().add(addIns);
        frame.getContentPane().add(delIns);
        frame.getContentPane().add(addMem);
        frame.getContentPane().add(delMem);
        frame.getContentPane().add(stepButton);
        frame.getContentPane().add(runButton);
        frame.getContentPane().add(loadFile);
        frame.getContentPane().add(init);

        addIns.setBounds(160, 15, 18, 18);
        delIns.setBounds(180, 15, 18, 18);
        addMem.setBounds(160, 210, 18, 18);
        delMem.setBounds(180, 210, 18, 18);
        stepButton.setBounds(500, 400, 100, 40);
        runButton.setBounds(650, 400, 100, 40);
        loadFile.setBounds(500, 500, 100, 40);
        init.setBounds(650, 500, 100, 40);

        addIns.addActionListener(al);
        delIns.addActionListener(al);
        addMem.addActionListener(al);
        delMem.addActionListener(al);
        stepButton.addActionListener(al);
        runButton.addActionListener(al);
        loadFile.addActionListener(al);
        init.addActionListener(al);
    }

    public ActionListener al = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == addIns) {
                String[] new_data = {"???", "???", "???", "???"};
                insTable.addRow(new_data);

            } else if (e.getSource() == delIns) {
                Vector<Vector<String>> vec = insTable.getData();
                if (vec.size() > 0) {
                    vec.remove(vec.lastElement());
                    insTable.setData(vec);
                }

            } else if (e.getSource() == addMem) {
                String[] new_data = {"???", "???"};
                memTable.addRow(new_data);

            } else if (e.getSource() == delMem) {
                Vector<Vector<String>> vec = memTable.getData();
                if (vec.size() > 0) {
                    vec.remove(vec.lastElement());
                    memTable.setData(vec);
                }

            } else if (e.getSource() == stepButton) {

            } else if (e.getSource() == runButton) {

            } else if (e.getSource() == loadFile) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("请选择指令文件");
                fc.setFileFilter(new FileNameExtensionFilter("文本文件(\".txt\")", "txt"));
                int retVal = fc.showOpenDialog(frame);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    if (file.isFile() && file.exists()) {
                        try {
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
                            insTable.setData(data);
                            read.close();
                        } catch (IOException error) {
                            JOptionPane.showMessageDialog(null,
                                    "请选择正确的文件！",
                                    "文件错误",
                                    JOptionPane.ERROR_MESSAGE);

                            error.printStackTrace();
                        }

                    }
                }
            } else if(e.getSource() == init) {

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
}
