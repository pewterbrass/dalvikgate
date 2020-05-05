package me.nov.dalvikgate.tests.runtime;

import static org.junit.jupiter.api.Assertions.*;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.*;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.*;

import me.nov.dalvikgate.tests.utils.Factory;

class NumberTests implements Opcodes {

  @Test
  void intConst32() {
    MutableMethodImplementation mmi = new MutableMethodImplementation(1);
    mmi.addInstruction(new BuilderInstruction31i(Opcode.CONST, 0, 1234));
    mmi.addInstruction(new BuilderInstruction11x(Opcode.RETURN, 0));

    assertEquals(1234, Factory.executeMethodAtRuntime(Factory.runDexToASM(Type.getMethodType(Type.INT_TYPE), mmi)));
  }

  @Test
  void floatConst32() {
    // unusual float construction
    MutableMethodImplementation mmi = new MutableMethodImplementation(1);
    mmi.addInstruction(new BuilderInstruction31i(Opcode.CONST, 0, Float.floatToIntBits(1.2f)));
    mmi.addInstruction(new BuilderInstruction11x(Opcode.RETURN, 0));

    assertEquals(1.2f, Factory.executeMethodAtRuntime(Factory.runDexToASM(Type.getMethodType(Type.FLOAT_TYPE), mmi)));
  }

  @Test
  void longConst64() {
    MutableMethodImplementation mmi = new MutableMethodImplementation(2);
    mmi.addInstruction(new BuilderInstruction51l(Opcode.CONST_WIDE, 0, 423456L));
    mmi.addInstruction(new BuilderInstruction11x(Opcode.RETURN, 0));

    assertEquals(423456L, Factory.executeMethodAtRuntime(Factory.runDexToASM(Type.getMethodType(Type.LONG_TYPE), mmi)));
  }

  @Test
  void doubleConst64() {
    // unusual double construction
    MutableMethodImplementation mmi = new MutableMethodImplementation(2);
    mmi.addInstruction(new BuilderInstruction51l(Opcode.CONST_WIDE, 0, Double.doubleToLongBits(363412)));
    mmi.addInstruction(new BuilderInstruction11x(Opcode.RETURN, 0));
    assertEquals(363412, Factory.executeMethodAtRuntime(Factory.runDexToASM(Type.getMethodType(Type.DOUBLE_TYPE), mmi)));
  }

  @Test
  void floatConstHigh16() {
    // normal float construction
    MutableMethodImplementation mmi = new MutableMethodImplementation(1);
    mmi.addInstruction(new BuilderInstruction21ih(Opcode.CONST_HIGH16, 0, Float.floatToIntBits(21.3f)));
    mmi.addInstruction(new BuilderInstruction11x(Opcode.RETURN, 0));

    assertEquals(21.3f, Factory.executeMethodAtRuntime(Factory.runDexToASM(Type.getMethodType(Type.FLOAT_TYPE), mmi)));
  }

  @Test
  void doubleConstWideHigh16() {
    // normal double construction
    MutableMethodImplementation mmi = new MutableMethodImplementation(2);
    mmi.addInstruction(new BuilderInstruction21lh(Opcode.CONST_WIDE_HIGH16, 0, Double.doubleToLongBits(363417)));
    mmi.addInstruction(new BuilderInstruction11x(Opcode.RETURN, 0));
    assertEquals(363417, Factory.executeMethodAtRuntime(Factory.runDexToASM(Type.getMethodType(Type.DOUBLE_TYPE), mmi)));
  }
}