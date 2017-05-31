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

    public int op;

    public RegisterPair reg0;

    public RegisterPair regJ;

    public RegisterPair regK;

    public Map<Integer, Integer> record = new HashMap<Integer, Integer>();

    public Instruction() {
        reset();
    }

    public Instruction(String inst) {
        parseInst(inst);
    }

    public void parseInst(String inst) {
        String[] inss = inst.split(" ");
        op = OperatorName.operatorNameMap.get(inss[0]);
        String[] ns = inss[1].split(",");
        switch (op) {
            case OperatorName.ADDD:
            case OperatorName.SUBD:
            case OperatorName.MULTD:
            case OperatorName.DIVD:
                assert ns[0].charAt(0) == 'F';
                reg0 = new RegisterPair(RegisterName.FLOAT, Integer.parseInt(ns[0].substring(1)));
                regJ = new RegisterPair(RegisterName.FLOAT, Integer.parseInt(ns[1].substring(1)));
                regK = new RegisterPair(RegisterName.FLOAT, Integer.parseInt(ns[2].substring(1)));
                break;
            case OperatorName.LD:
            case OperatorName.ST:
                assert ns[0].charAt(0) == 'F';
                assert ns[2].charAt(0) == 'R';
                reg0 = new RegisterPair(RegisterName.FLOAT, Integer.parseInt(ns[0].substring(1)));
                regJ = new RegisterPair(RegisterName.INSTVALUE, Integer.parseInt(ns[1]));
                regK = new RegisterPair(RegisterName.INT, Integer.parseInt(ns[2].substring(1)));
                break;
        }
        reset();
    }

    public void reset() {
        for (Integer state : InstStateName.instStateNameMap.values()) {
            record.put(state, 0);
        }
    }
}
