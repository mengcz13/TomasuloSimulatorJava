package tomasulo.comparch.core;

import tomasulo.comparch.util.name.OperatorName;
import tomasulo.comparch.util.name.RegisterName;
import tomasulo.comparch.util.pair.RegisterPair;
import tomasulo.comparch.util.name.InstStateName;

import java.util.*;

/**
 * Created by neozero on 17-5-31.
 *
 * Define the structure of a single instruction.
 */
public class Instruction {

    // 指令的运算种类
    public int op;

    // 寄存器种类+序号
    public RegisterPair reg0;

    public RegisterPair regJ;

    public RegisterPair regK;

    public String[] ins = new String[4];

    // 记录各阶段操作发生时的周期数
    public Map<Integer, Integer> record = new HashMap<Integer, Integer>();

    public Instruction() {
        reset();
    }

    public Instruction(String inst) {
        parseInst(inst);
    }

    public Instruction(String[] inst) {
        parseInstStrArray(inst);
    }

    // 解析指令字符串
    public void parseInst(String inst) {
        String[] inss = inst.split(" ");
        String[] ns = inss[1].split(",");
        String[] temp = new String[4];
        temp[0] = inss[0];
        temp[1] = ns[0];
        temp[2] = ns[1];
        temp[3] = ns[2];
        this.parseInstStrArray(temp);
    }

    public void parseInstStrArray(String[] inst) {
        op = OperatorName.nameOperatorMap.get(inst[0]);
        switch (op) {
            case OperatorName.ADDD:
            case OperatorName.SUBD:
            case OperatorName.MULTD:
            case OperatorName.DIVD:
                assert inst[1].charAt(0) == 'F';
                reg0 = new RegisterPair(RegisterName.FLOAT, Integer.parseInt(inst[1].substring(1)));
                regJ = new RegisterPair(RegisterName.FLOAT, Integer.parseInt(inst[2].substring(1)));
                regK = new RegisterPair(RegisterName.FLOAT, Integer.parseInt(inst[3].substring(1)));
                break;
            case OperatorName.LD:
            case OperatorName.ST:
                assert inst[1].charAt(0) == 'F';
                assert inst[3].charAt(0) == 'R';
                reg0 = new RegisterPair(RegisterName.FLOAT, Integer.parseInt(inst[1].substring(1)));
                regJ = new RegisterPair(RegisterName.INSTVALUE, Integer.parseInt(inst[2]));
                regK = new RegisterPair(RegisterName.INT, Integer.parseInt(inst[3].substring(1)));
                break;
        }
        this.ins = inst;
        reset();
    }

    // 重置指令, 这里指令内容不变, 仅清空历史记录
    public void reset() {
        for (Integer state : InstStateName.instStateNameMap.values()) {
            record.put(state, 0);
        }
    }

    public String[] getText() {
        return ins;
    }

    public String[] getState() {
        String[] stateTable = new String[3];
        stateTable[0] = record.get(InstStateName.ISSUE).toString();
        stateTable[1] = record.get(InstStateName.EXECCOMP).toString();
        stateTable[2] = record.get(InstStateName.WRITERESULT).toString();
        return stateTable;
    }
}
