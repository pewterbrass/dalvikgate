package me.nov.dalvikgate.transform.instructions.translators;

import static org.objectweb.asm.Type.*;

import org.jf.dexlib2.builder.instruction.BuilderInstruction22b;
import org.objectweb.asm.tree.InsnNode;

import me.nov.dalvikgate.asm.ASMCommons;
import me.nov.dalvikgate.transform.instructions.*;
import me.nov.dalvikgate.transform.instructions.exception.UnsupportedInsnException;

public class IntMath8Translator extends AbstractInsnTranslator<BuilderInstruction22b> {

  public IntMath8Translator(InstructionTransformer it) {
    super(it);
  }

  @Override
  public void translate(BuilderInstruction22b i) {
    // Perform the indicated binary op on the indicated register (first argument) and literal value (second argument),
    // storing the result in the destination register.
    // A: destination register (8 bits)
    // B: source register (8 bits)
    // C: signed int constant (8 bits)
    addLocalGet(i.getRegisterB(), INT_TYPE);
    il.add(ASMCommons.makeIntPush(i.getNarrowLiteral()));
    switch (i.getOpcode()) {
    case ADD_INT_LIT8:
      il.add(new InsnNode(IADD));
      break;
    case RSUB_INT_LIT8:
      // reverse subtraction
      il.add(new InsnNode(SWAP));
      il.add(new InsnNode(ISUB));
      break;
    case MUL_INT_LIT8:
      il.add(new InsnNode(IMUL));
      break;
    case DIV_INT_LIT8:
      il.add(new InsnNode(IDIV));
      break;
    case REM_INT_LIT8:
      il.add(new InsnNode(IREM));
      break;
    case AND_INT_LIT8:
      il.add(new InsnNode(IAND));
      break;
    case OR_INT_LIT8:
      il.add(new InsnNode(IOR));
      break;
    case XOR_INT_LIT8:
      il.add(new InsnNode(IXOR));
      break;
    case SHL_INT_LIT8:
      il.add(new InsnNode(ISHL));
      break;
    case SHR_INT_LIT8:
      il.add(new InsnNode(ISHR));
      break;
    case USHR_INT_LIT8:
      il.add(new InsnNode(IUSHR));
      break;
    default:
      throw new UnsupportedInsnException(i);
    }
    addLocalSet(i.getRegisterA(), INT_TYPE);
  }
}