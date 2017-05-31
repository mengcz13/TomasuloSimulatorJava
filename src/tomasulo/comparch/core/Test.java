package tomasulo.comparch.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neozero on 17-6-1.
 */
public class Test {

    public static void main(String[] args) {
        List<Instruction> instructionList = new ArrayList<Instruction>();
        instructionList.add(new Instruction("LD F6,34,R2"));
        instructionList.add(new Instruction("LD F2,45,R3"));
        instructionList.add(new Instruction("MULTD F0,F2,F2"));
        instructionList.add(new Instruction("SUBD F8,F6,F2"));
        instructionList.add(new Instruction("DIVD F10,F0,F6"));
        instructionList.add(new Instruction("ADDD F6,F8,F2"));

        TomasuloSimulatorCore tsc = new TomasuloSimulatorCore(instructionList);
        for (int i = 0; i < 10; ++i) {
            tsc.run();
            tsc.reset();
        }
    }
}
