/**
 * CPSC 326, Spring 2025
 * The virtual machine implementation.
 * <p>
 * Orion Hess
 */

package cpsc326;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MyPL virtual machine for running MyPL programs (as VM
 * instructions).
 */
public class VM {

  /* special NULL value */
  public static final Object NULL = new Object() {
    public String toString() {
      return "null";
    }
  };

  /* the array heap as an oid to list mapping */
  private final Map<Integer, List<Object>> arrayHeap = new ConcurrentHashMap<>();

  /* the struct heap as an oid to object (field to value map) mapping */
  private final Map<Integer, Map<String, Object>> structHeap = new ConcurrentHashMap<>();

  /* the threads as a tid to Thread (field to thread) mapping */
  private final Map<Integer, ThreadProcessor> threads = new ConcurrentHashMap<>();

  /* the operand stack */
  private final Deque<Object> operandStack = new ArrayDeque<>();

  /* the function (frame) call stack */
  private final Deque<VMFrame> callStack = new ArrayDeque<>();

  /* the set of program function definitions (frame templates) */
  private final Map<String, VMFrameTemplate> templates = new HashMap<>();

  /* the next unused object id */
  private final AtomicInteger nextObjectId = new AtomicInteger(2025);

  /* the next unused thread id */
  private final AtomicInteger nextThreadId = new AtomicInteger(2025);

  /* debug flag for output debug info during vm execution (run) */
  private boolean debug = false;


  // helper functions

  /**
   * Create and throw an error.
   *
   * @param msg The error message.
   */
  private void error(String msg) {
    MyPLException.vmError(msg);
  }

  /**
   * Create and throw an error (for a specific frame).
   *
   * @param msg   The error message.
   * @param frame The frame where the error occurred.
   */
  // TODO: Put thread related info in the error messages
  private void error(String msg, VMFrame frame) {
    String s = "%s in %s at %d: %s";
    String name = frame.template.functionName;
    int pc = frame.pc - 1;
    VMInstr instr = frame.template.instructions.get(pc);
    MyPLException.vmError(String.format(s, msg, name, pc, instr));
  }

  /**
   * Add a frame template to the VM.
   *
   * @param template The template to add.
   */
  public void add(VMFrameTemplate template) {
    templates.put(template.functionName, template);
  }

  /**
   * For turning on debug mode to help with debugging the VM.
   *
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
   *
   * @param x     the value to check
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
      return (int) x + (int) y;
    else if (x instanceof Double)
      return (double) x + (double) y;
    else
      return x + (String) y;
  }

  /**
   * Helper to subtract two objects
   */
  private Object subHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int) x - (int) y;
    else
      return (double) x - (double) y;
  }

  /**
   * Helper to multiply two objects
   */
  private Object mulHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int) x * (int) y;
    else
      return (double) x * (double) y;
  }

  /**
   * Helper to divide two objects
   */
  private Object divHelper(Object x, Object y, VMFrame f) {
    if (x instanceof Integer && (int) y != 0)
      return (int) x / (int) y;
    else if (x instanceof Double && (double) y != 0.0)
      return (double) x / (double) y;
    else
      error("division by zero error", f);
    return null;
  }

  /**
   * Helper to compare if first object less than second
   */
  private Object cmpltHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int) x < (int) y;
    else if (x instanceof Double)
      return (double) x < (double) y;
    else
      return ((String) x).compareTo((String) y) < 0;
  }

  /**
   * Helper to compare if first object less than or equal second
   */
  private Object cmpleHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int) x <= (int) y;
    else if (x instanceof Double)
      return (double) x <= (double) y;
    else
      return ((String) x).compareTo((String) y) <= 0;
  }

  // the main run method

  /**
   * Execute the program
   */
  // TODO: modify run to be able to work on a different call and op stack, should just be able to make them inputs
  public void run() {
    process("main", operandStack, callStack);
  }

  public void process(String startingFunc, Deque<Object> operandStack, Deque<VMFrame> callStack) {
    // grab the main frame and "instantiate" it
    if (!templates.containsKey(startingFunc))
      error("No " + startingFunc + " function");
    VMFrame frame = new VMFrame(templates.get(startingFunc));
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

      switch (instr.opcode) {
        //----------------------------------------------------------------------
        // Literals and Variables
        //----------------------------------------------------------------------

        // push operand A
        case PUSH -> operandStack.push(instr.operand);
        // pop x
        case POP -> operandStack.pop();
        // push value at memory address (operand) A
        case LOAD -> operandStack.push(frame.memory.get((int) instr.operand));
        // pop x, store x at memory address (operand) A
        case STORE -> {
          Object val = operandStack.pop();
          if (frame.memory.size() <= (int) instr.operand) frame.memory.add(VM.NULL);
          if (frame.memory.size() <= (int) instr.operand) error("Invalid store index", frame);
          frame.memory.set((int) instr.operand, val);
        }

        //----------------------------------------------------------------------
        // arithmetic, relational, and logical operators
        //----------------------------------------------------------------------

        // pop x, pop y, push (y + x)
        case ADD -> {
          Object x = operandStack.pop();
          Object y = operandStack.pop();
          if ((x.equals(VM.NULL) || y.equals(VM.NULL))) {
            error("ADD called with null operand", frame);
          } else {
            operandStack.push(addHelper(y, x));
          }
        }
        // pop x, pop y, push (y - x)
        case SUB -> {
          Object x = operandStack.pop();
          Object y = operandStack.pop();
          if ((x.equals(VM.NULL) || y.equals(VM.NULL))) {
            error("SUB called with null operand", frame);
          } else {
            operandStack.push(subHelper(y, x));
          }
        }
        // pop x, pop y, push (y * x)
        case MUL -> {
          Object x = operandStack.pop();
          Object y = operandStack.pop();
          if ((x.equals(VM.NULL) || y.equals(VM.NULL))) {
            error("MUL called with null operand", frame);
          } else {
            operandStack.push(mulHelper(y, x));
          }
        }
        // pop x, pop y, push (y // x) or (y / x)
        case DIV -> {
          Object x = operandStack.pop();
          Object y = operandStack.pop();
          if ((x.equals(VM.NULL) || y.equals(VM.NULL))) {
            error("DIV called with null operand", frame);
          } else {
            operandStack.push(divHelper(y, x, frame));
          }
        }
        // pop x, pop y, push (y < x)
        case CMPLT -> {
          Object x = operandStack.pop();
          Object y = operandStack.pop();
          if ((x.equals(VM.NULL) || y.equals(VM.NULL))) {
            error("CMPLT called with null operand", frame);
          } else {
            operandStack.push(cmpltHelper(y, x));
          }
        }
        // pop x, pop y, push (y <= x)
        case CMPLE -> {
          Object x = operandStack.pop();
          Object y = operandStack.pop();
          if ((x.equals(VM.NULL) || y.equals(VM.NULL))) {
            error("CMPLE called with null operand", frame);
          } else {
            operandStack.push(cmpleHelper(y, x));
          }
        }
        // pop x, pop y, push (y == x)
        case CMPEQ -> {
          Object x = operandStack.pop();
          Object y = operandStack.pop();
          operandStack.push(x.equals(y));
        }
        // pop x, pop y, push (y != x)
        case CMPNE -> {
          Object x = operandStack.pop();
          Object y = operandStack.pop();
          operandStack.push(!x.equals(y));
        }
        // pop x, pop y, push (y and x)
        case AND -> {
          Object x = operandStack.pop();
          Object y = operandStack.pop();
          if (x instanceof Boolean && y instanceof Boolean) {
            operandStack.push((Boolean) x && (Boolean) y);
          } else {
            error("AND called on non-boolean types", frame);
          }
        }
        // pop x, pop y, push (y or x)
        case OR -> {
          Object x = operandStack.pop();
          Object y = operandStack.pop();
          if (x instanceof Boolean && y instanceof Boolean) {
            operandStack.push((Boolean) x || (Boolean) y);
          } else {
            error("OR called on non-boolean types", frame);
          }
        }
        // pop x, push (not x)
        case NOT -> {
          Object x = operandStack.pop();
          if (x instanceof Boolean) {
            operandStack.push(!(Boolean) x);
          } else {
            error("NOT called on non-boolean type", frame);
          }
        }

        //----------------------------------------------------------------------
        // jump and branch
        //----------------------------------------------------------------------

        // jump to given instruction offset A
        case JMP -> frame.pc = (int) instr.operand;
        // pop x, if x is False jump to instruction offset A
        case JMPF -> {
          Object x = operandStack.pop();
          if (!(Boolean) x) {
            frame.pc = (int) instr.operand;
          }
        }

        //----------------------------------------------------------------------
        // functions
        //----------------------------------------------------------------------

        // call function A (pop and push arguments)
        case CALL -> {
          VMFrame callFrame = new VMFrame(templates.get((String) instr.operand));
          frame = callFrame;
          callStack.push(callFrame);
        }
        // return from current function
        case RET -> {
          callStack.pop();
          frame = callStack.peek();
        }

        //----------------------------------------------------------------------
        // built ins
        //----------------------------------------------------------------------

        // pop x, print x to standard output
        case WRITE -> System.out.print(operandStack.pop());
        // read standard input, push result onto stack
        case READ -> {
          BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
          try {
            String input = reader.readLine();
            operandStack.push(input);
          } catch (IOException e) {
            error("Error reading input", frame);
          }
        }
        // pop string x, push length(x) if str, else push obj(x).length
        case LEN -> {
          Object x = operandStack.pop();
          if (x.equals(VM.NULL)) error("LEN called with null argument", frame);
          if (x instanceof String) {
            operandStack.push(((String) x).length());
          } else {
            operandStack.push(arrayHeap.get((int) x).size());
          }
        }
        // pop int x, pop string y, push y[x]
        case GETC -> {
          Object x = operandStack.pop();
          Object y = operandStack.pop();
          if (y.equals(VM.NULL) || x.equals(VM.NULL)) error("GETC called with null argument", frame);
          if ((int) x < 0 || (int) x >= ((String) y).length()) error("GETC called with oob index", frame);
          operandStack.push(Character.toString(((String) y).charAt((int) x)));
        }
        // pop x, push int(x)
        case TOINT -> {
          Object x = operandStack.pop();
          if (x.equals(VM.NULL)) error("TOINT called with null argument", frame);
          if (x instanceof String) {
            try {
              operandStack.push(Integer.parseInt((String) x));
            } catch (Exception e) {
              error("TOINT called on bad string");
            }
          } else if (x instanceof Double) operandStack.push(((Double) x).intValue());
          else error("TOINT called with non String/Double type", frame);
        }
        // pop x, push double(x)
        case TODBL -> {
          Object x = operandStack.pop();
          if (x.equals(VM.NULL)) error("TODBL called with null argument", frame);
          if (x instanceof String) {
            try {
              operandStack.push(Double.parseDouble((String) x));
            } catch (Exception e) {
              error("TODBL called on bad string");
            }
          } else if (x instanceof Integer) operandStack.push(((Integer) x).doubleValue());
          else error("TODBL called with non String/Integer type", frame);
        }
        // pop x, push str(x)
        case TOSTR -> {
          Object x = operandStack.pop();
          if (x.equals(VM.NULL)) error("TOSTR called with null argument", frame);
          if (x instanceof Double || x instanceof Integer) operandStack.push(String.valueOf(x));
          else error("TOSTR called with non Double/Integer type", frame);
        }

        //----------------------------------------------------------------------
        // threading
        //----------------------------------------------------------------------

        // pop x function, pop y arguments similar to call, starts a new thread of function x - pushes arguments y onto the new op stack
        case THREAD -> {
          ThreadProcessor thread = new ThreadProcessor(nextThreadId.get(), this, (String) operandStack.pop(), operandStack.pop());
          threads.put(nextThreadId.get(), thread);
          operandStack.push(nextThreadId.getAndIncrement());
        }
        // pop x, wait for/join tid x, push return of threaded func
        case WAIT -> {
          Object x = operandStack.pop();
          if (x.equals(VM.NULL)) error("WAIT called with null operand", frame);
          Integer tid = (Integer) x;
          if (!threads.containsKey(tid)) error("WAIT called on non-existent thread", frame);
          Optional<Integer> returnVal = threads.get(tid).returnVal;
          while (returnVal.isEmpty()) {
            try {
              wait();
            } catch (InterruptedException e) {
              error("Something has gone terribly wrong in WAIT", frame);
              throw new RuntimeException(e);
            }
          }
          operandStack.push(returnVal.get());
        }

        //----------------------------------------------------------------------
        // heap
        //----------------------------------------------------------------------

        // allocate struct object, push oid x
        case ALLOCS -> {
          structHeap.put(nextObjectId.get(), new HashMap<>());
          operandStack.push(nextObjectId.getAndIncrement());
        }
        // pop value x, pop oid y, set obj(y)[A] = x
        case SETF -> {
          Object x = operandStack.pop();
          Object y = operandStack.pop();
          if (y.equals(VM.NULL)) error("SETF called with null OID", frame);
          structHeap.get((Integer) y).put((String) instr.operand, x);
        }
        // pop oid x, push obj(x)[A] onto stack
        case GETF -> {
          Object x = operandStack.pop();
          if (x.equals(VM.NULL)) error("GETF called with null OID", frame);
          operandStack.push(structHeap.get((Integer) x).get((String) instr.operand));
        }
        // pop int x, allocate array object with x None values, push oid
        case ALLOCA -> {
          Object x = operandStack.pop();
          List<Object> array = new ArrayList<>();
          if (x.equals(VM.NULL) || (int) x < 0) error("ALLOCA called with bad length ( < 0 or null)", frame);
          for (int i = 0; i < (Integer) x; i++) {
            array.add(VM.NULL);
          }
          arrayHeap.put(nextObjectId.get(), array);
          operandStack.push(nextObjectId.getAndIncrement());
        }
        // pop value x, pop index y, pop oid z, set array obj(z)[y] = x
        case SETI -> {
          Object x = operandStack.pop();
          Object y = operandStack.pop();
          Object z = operandStack.pop();
          if (z.equals(VM.NULL) || !arrayHeap.containsKey((int) z))
            error("SETI called on non-existent or null array", frame);
          var array = arrayHeap.get((int) z);
          if (y.equals(VM.NULL) || (int) y >= array.size() || (int) y < 0)
            error("SETI called with out of bounds or null index", frame);
          array.set((int) y, x);
        }
        // pop index x, pop oid y, push obj(y)[x] onto stack
        case GETI -> {
          Object x = operandStack.pop();
          Object y = operandStack.pop();
          if (y.equals(VM.NULL) || !arrayHeap.containsKey((int) y))
            error("GETI called on non-existent or null array", frame);
          var array = arrayHeap.get((int) y);
          if (x.equals(VM.NULL) || (int) x >= array.size() || (int) x < 0)
            error("GETI called with out of bounds index", frame);
          operandStack.push(array.get((int) x));
        }

        //----------------------------------------------------------------------
        // Special Instructions
        //----------------------------------------------------------------------

        // pop x, push x, push x
        case DUP -> {
          Object val = operandStack.pop();
          operandStack.push(val);
          operandStack.push(val);
        }
        // do nothing
        case NOP -> {
        }

        default -> error("Unsupported operation: " + instr);
      }
    }
  }
}
