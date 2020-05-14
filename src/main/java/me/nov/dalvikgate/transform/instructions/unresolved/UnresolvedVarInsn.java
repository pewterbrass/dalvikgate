package me.nov.dalvikgate.transform.instructions.unresolved;

import static me.nov.dalvikgate.asm.ASMCommons.*;

import java.util.*;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Frame;

import me.coley.analysis.util.FrameUtil;
import me.coley.analysis.value.AbstractValue;
import me.nov.dalvikgate.DexToASM;
import me.nov.dalvikgate.transform.instructions.IUnresolvedInstruction;
import me.nov.dalvikgate.transform.instructions.exception.UnresolvedInsnException;
import me.nov.dalvikgate.utils.UnresolvedUtils;

public class UnresolvedVarInsn extends VarInsnNode implements IUnresolvedInstruction, Opcodes {
  private final boolean store;
  private final Type initialType;
  private boolean resolvedOp;
  private boolean resolvedVar;
  private boolean wide;

  /**
   * Create new unresolved variable instruction.
   *
   * @param store       {@code true} for storage insns.
   * @param initialType Initial type indicated by android instruction. Will be {@code null} if ambiguous.
   */
  public UnresolvedVarInsn(boolean wide, boolean store, Type initialType) {
    super(UnresolvedUtils.getDefaultVarOp(store), -1);
    this.wide = wide;
    this.store = store;
    this.initialType = initialType;
  }

  @Override
  public void accept(final MethodVisitor methodVisitor) {
    validate();
    super.accept(methodVisitor);
  }

  @Override
  public AbstractInsnNode clone(final Map<LabelNode, LabelNode> clonedLabels) {
    validate();
    return super.clone(clonedLabels);
  }

  /**
   * Validate the opcode and variable index are set.
   */
  public void validate() {
    if (DexToASM.noResolve)
      setType(initialType == null ? Type.INT_TYPE : initialType);
    if (opcode < 0)
      throw new UnresolvedInsnException("Variable opcode has not been resolved!");
    if (var < 0)
      throw new UnresolvedInsnException("Variable index has not been resolved!");
  }

  /**
   * Update the instruction's variable index.
   *
   * @param local Variable index.
   */
  public void setLocal(int local) {
    this.var = local;
    resolvedVar = true;
  }

  public void setType(Type type) {
    if (resolvedOp) {
      return;
    }
    switch (type.getSort()) {
    case Type.OBJECT:
    case Type.ARRAY:
      setOpcode(store ? ASTORE : ALOAD);
      break;
    case Type.BOOLEAN:
    case Type.INT:
    case Type.BYTE:
    case Type.SHORT:
    case Type.CHAR:
      setOpcode(store ? ISTORE : ILOAD);
      break;
    case Type.FLOAT:
      setOpcode(store ? FSTORE : FLOAD);
      break;
    case Type.LONG:
      setOpcode(store ? LSTORE : LLOAD);
      break;
    case Type.DOUBLE:
      setOpcode(store ? DSTORE : DLOAD);
      break;
    default:
      throw new IllegalArgumentException("Unsupported var type: " + type.getDescriptor());
    }
    resolvedOp = true;
  }

  public boolean isStore() {
    return store;
  }

  @Override
  public boolean isResolved() {
    return resolvedVar && resolvedOp;
  }

  @Override
  public boolean tryResolve(int index, MethodNode method, Frame<AbstractValue>[] frames) {
    if (var == -1)
      throw new IllegalArgumentException();

    Type type = tryResolve(method.instructions.get(index).getNext());
    visited.clear();
    if (type != null) {
      setType(type);
    } else {
      if (store) {
        // if it cannot be resolved, try to the stack to resolve it.
        AbstractValue value = FrameUtil.getTopStack(frames[index]);
        if (!UnresolvedUtils.containsUnresolved(value.getInsns())) {
          setType(value.getType());
          return true;
        }
      }
      // type is unknown, just guess the type, as the local is never used, and it doesn't matter. only the size must be right.
      setType(wide ? Type.LONG_TYPE : Type.INT_TYPE);
      // why int and not object? well, imagine this scenario:

      // unresolved ldc 5
      // unresolved var store 3
      // unresolved var load 3
      // unresolved jump ifeq/null label1

      // variable will never get resolved. type will be set to int and cause no problem (jump and number will get resolved properly too).
    }
    return true;
  }

  private ArrayList<AbstractInsnNode> visited = new ArrayList<>();

  private Type tryResolve(AbstractInsnNode ain) {
    while (ain != null && !isReturn(ain)) {
      if (visited.contains(ain)) {
        // prevent infinite loops
        return null;
      }
      visited.add(ain);
      int op = ain.getOpcode();
      if (op == ATHROW) {
        // block end
        return null;
      }
      if (ain.getType() == AbstractInsnNode.VAR_INSN) {
        VarInsnNode vin = (VarInsnNode) ain;
        boolean canBeReferencePoint = !(vin instanceof UnresolvedVarInsn) || ((UnresolvedVarInsn) vin).isResolved();
        if (vin.var == var && canBeReferencePoint) {
          if (isVarStore(op)) {
            // variable is set before it is loaded, we don't know the type, as the register could now store something else.
            // TODO test if in dalvik the same is possible as in java, reusing locals with different type sorts, otherwise this can be removed
            return null;
          } else {
            switch (op) {
            case ALOAD:
              return OBJECT_TYPE;
            case ILOAD:
              return Type.INT_TYPE;
            case FLOAD:
              return Type.FLOAT_TYPE;
            case DLOAD:
              return Type.DOUBLE_TYPE;
            case LLOAD:
              return Type.LONG_TYPE;
            }
          }
        }
      }
      if (op == GOTO) {
        ain = ((JumpInsnNode) ain).label;
      } else if (ain.getType() == AbstractInsnNode.JUMP_INSN) {
        Type subroutineType = tryResolve(((JumpInsnNode) ain).label);
        if (subroutineType != null) {
          return subroutineType;
        }
        // else continue
      } else if (ain.getType() == AbstractInsnNode.TABLESWITCH_INSN || ain.getType() == AbstractInsnNode.LOOKUPSWITCH_INSN) {
        // try resolve all targets
        throw new IllegalArgumentException("unimplemented");
      }
      ain = ain.getNext();
    }
    return null;
  }
}
