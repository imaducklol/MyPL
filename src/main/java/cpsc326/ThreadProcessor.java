/**
 * CPSC 326, Spring 2025
 * The virtual machine implementation.
 * <p>
 * Orion Hess
 */

package cpsc326;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * MyPL thread class
 */
public class ThreadProcessor {
  /* thread identification int */
  private int tid;

  /* reference to main vm */
  private VM vm;

  /* the operand stack */
  private final Deque<Object> operandStack = new ArrayDeque<>();

  /* the function (frame) call stack */
  private final Deque<VMFrame> callStack = new ArrayDeque<>();

  /* the result of the function, filled when it is completed */
  public Optional<Integer> returnVal = Optional.empty();

  /**
   * Create a new Thread given a virtual machine
   *
   * @param tid the thread ID
   * @param vm the VM for referencing
   */
  public ThreadProcessor(int tid, VM vm, String funcName, Object argumentStruct) {
    this.tid = tid;
    this.vm = vm;

    operandStack.push(argumentStruct);

    new Thread(() -> {
      vm.process(funcName, operandStack, callStack);
      returnVal = Optional.of((Integer) operandStack.pop());
    });
  }
}
