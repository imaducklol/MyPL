/**
 * CPSC 326, Spring 2025
 * The virtual machine implementation.
 */

package cpsc326;

import java.util.List;
import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * MyPL virtual machine for running MyPL programs (as VM
 * instructions).
 */
public class VM {

  /* special NULL value */
  public static final Object NULL = new Object() {
      public String toString() {return "null";}
    };
  
  /* the array heap as an oid to list mapping */
  private Map<Integer,List<Object>> arrayHeap = new HashMap<>();

  /* the struct heap as an oid to object (field to value map) mapping */
  private Map<Integer,Map<String,Object>> structHeap = new HashMap<>();

  /* the operand stack */
  private Deque<Object> operandStack = new ArrayDeque<>();

  /* the function (frame) call stack */
  private Deque<VMFrame> callStack = new ArrayDeque<>();

  /* the set of program function definitions (frame templates) */
  private Map<String,VMFrameTemplate> templates = new HashMap<>();

  /* the next unused object id */
  private int nextObjectId = 2025;

  /* debug flag for output debug info during vm execution (run) */
  private boolean debug = false;

  
  // helper functions

  /**
   * Create and throw an error.
   * @param msg The error message.
   */
  private void error(String msg) {
    MyPLException.vmError(msg);
  }

  /**
   * Create and throw an error (for a specific frame).
   * @param msg The error message.
   * @param frame The frame where the error occurred.
   */
  private void error(String msg, VMFrame frame) {
    String s = "%s in %s at %d: %s";
    String name = frame.template.functionName;
    int pc = frame.pc - 1;
    VMInstr instr = frame.template.instructions.get(pc);
    MyPLException.vmError(String.format(s, msg, name, pc, instr));
  }

  /**
   * Add a frame template to the VM.
   * @param template The template to add.
   */
  public void add(VMFrameTemplate template) {
    templates.put(template.functionName, template);
  }

  /**
   * For turning on debug mode to help with debugging the VM.
   * @param on Set to true to turn on debugging, false to turn it off.
   */
  public void debugMode(boolean on) {
    debug = on;
  }

  /**
   * Pretty-print the VM frames.
   */
  public String toString() {
    String s = "";
    for (var funName : templates.keySet()) {
      s += String.format("\nFrame '%s'\n", funName);
      VMFrameTemplate template = templates.get(funName);
      for (int i = 0; i < template.instructions.size(); ++i) 
        s += String.format("  %d: %s\n", i, template.instructions.get(i));
    }
    return s;
  }

  // Additional helpers for implementing the VM instructions
  
  /**
   * Helper to ensure the given value isn't NULL
   * @param x the value to check
   * @param frame the current stack frame
   */
  private void ensureNotNull(Object x, VMFrame frame) {
    if (x == NULL)
      error("null value error", frame);
  }
  
  /**
   * Helper to add two objects
   */
  private Object addHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int)x + (int)y;
    else if (x instanceof Double)
      return (double)x + (double)y;
    else
      return (String)x + (String)y;
  }

  /**
   * Helper to subtract two objects
   */
  private Object subHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int)x - (int)y;
    else
      return (double)x - (double)y;   
  }
  
  /**
   * Helper to multiply two objects
   */
  private Object mulHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int)x * (int)y;
    else
      return (double)x * (double)y;
  }

  /**
   * Helper to divide two objects
   */
  private Object divHelper(Object x, Object y, VMFrame f) {
    if (x instanceof Integer && (int)y != 0) 
      return (int)((int)x / (int)y);
    else if (x instanceof Double && (double)y != 0.0) 
      return (double)x / (double)y;
    else
      error("division by zero error", f);
    return null;
  }

  /**
   * Helper to compare if first object less than second
   */
  private Object cmpltHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int)x < (int)y;
    else if (x instanceof Double)
      return (double)x < (double)y;
    else
      return ((String)x).compareTo((String)y) < 0;
  }

  /**
   * Helper to compare if first object less than or equal second
   */
  private Object cmpleHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int)x <= (int)y;
    else if (x instanceof Double)
      return (double)x <= (double)y;
    else
      return ((String)x).compareTo((String)y) <= 0;
  }
  
  // the main run method

  /**
   * Execute the program
   */
  public void run() {
    // grab the main frame and "instantiate" it
    if (!templates.containsKey("main"))
      error("No 'main' function");
    VMFrame frame = new VMFrame(templates.get("main"));
    callStack.push(frame);

    // run loop until out of call frames or instructions in the frame
    while (!callStack.isEmpty() && frame.pc < frame.template.instructions.size()) {
      // get the next instruction
      VMInstr instr = frame.template.instructions.get(frame.pc);

      // for debugging:
      if (debug) {
        System.out.println();
        System.out.println("\t FRAME.........: " + frame.template.functionName);
        System.out.println("\t PC............: " + frame.pc);
        System.out.println("\t INSTRUCTION...: " + instr);
        Object val = operandStack.isEmpty() ? null : operandStack.peek();
        System.out.println("\t NEXT OPERAND..: " + val);
      }

      // increment the pc
      ++frame.pc;

      //----------------------------------------------------------------------
      // Literals and Variables
      //----------------------------------------------------------------------

      if (instr.opcode == OpCode.PUSH) {
        operandStack.push(instr.operand);
      }

      else if (instr.opcode == OpCode.POP) {
        operandStack.pop();
      }

      else if (instr.opcode == OpCode.LOAD) {
        operandStack.push(frame.memory.get((int)instr.operand));
      }


      
      // TODO: Implement the remaining instructions (except for DUP and NOP, see below) ...
      //   -- see lecture notes for hints and tips
      //
      // Additional Hints: 
      //   -- use ensureNotNull(v, frame) if operand can't be null
      //   -- Deque supports pop(), peek(), isEmpty()
      //   -- for WRITE, use System.out.print(...)
      //   -- for READ, use: new BufferedReader(new InputStreamReader(System.in)) and readLine()
      //   -- for LEN, can check type via: if (value instanceof String) ...
      //   -- for GETC, can use String charAt() function
      //   -- for TOINT, can use intValue() on Double
      //   -- for TOINT, can use Integer.parseInt(...) for String (in try-catch block)
      //   -- similarly for TODBL (but with corresponding Double versions)
      //   -- for TOSTR, can use String.valueOf(...)
      //   -- in a number of places, can cast if type known, e.g., ((int)length)


      
      //----------------------------------------------------------------------
      // Special Instructions
      //----------------------------------------------------------------------

      else if (instr.opcode == OpCode.DUP) {
        Object val = operandStack.pop();
        operandStack.push(val);
        operandStack.push(val);
      }

      else if (instr.opcode == OpCode.NOP) {
        // do nothing
      }

      else
        error("Unsupported operation: " + instr);
    }

  }

  
}
