package me.nov.dalvikgate.transform.instructions;

import org.objectweb.asm.Type;

public interface IUnresolvedInstruction {
  /**
   * Validate the opcode is set. Should throw an exception if setType hasn't been invoked.
   */
  void validate();

  /**
   * Update the instruction's type to determine its opcode and value.
   *
   * @param type Type discovered by analyzer.
   */
  void setType(Type type);
}