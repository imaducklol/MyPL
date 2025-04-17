/**
 * CPSC 326, Spring 2025
 * 
 * PUT YOUR NAME HERE
 */


package cpsc326;

import java.util.*;


/**
 * Generates MyPL VM code from an AST.
 */
public class CodeGenerator implements Visitor {

  /* vm to add frames to */
  private VM vm;

  /* current frame template being generated */
  private VMFrameTemplate currTemplate;

  /* variable -> index mappings with respect to environments */
  private VarTable varTable = new VarTable();

  /* struct defs for field names */
  private Map<String,StructDef> structs = new HashMap<>();  


  /**
   * Create a new Code Generator given a virtual machine
   * @param vm the VM for storing generated frame templates
   */
  public CodeGenerator(VM vm) {
    this.vm = vm;
  }
  
  //----------------------------------------------------------------------
  // Helper functions
  //----------------------------------------------------------------------

  /**
   * Helper to add an instruction to the current frame.
   * @param instr the instruction to add
   */
  private void add(VMInstr instr) {
    currTemplate.add(instr);
  }
  
  /**
   * Helper to add an instruction to the current frame with a comment.
   * @param instr the instruction to add
   * @param comment the comment to assign to the instruction
   */
  private void add(VMInstr instr, String comment) {
    instr.comment = comment;
    currTemplate.add(instr);
  }

  /**
   * Helper to execute body statements that cleans up the stack for
   * single function call statements (whose returned values aren't
   * used).
   */
  private void execBody(List<Stmt> stmts) {
    for (var stmt : stmts) {
      stmt.accept(this);
      if (stmt instanceof CallRValue)
        add(VMInstr.POP(), "clean up call rvalue statement");
    }
  }
  
  //----------------------------------------------------------------------
  // Visitors for programs, functions, and structs
  //----------------------------------------------------------------------

  /**
   * Generates the IR for the program
   */
  public void visit(Program node) {
    // record each struct definitions and check for duplicate names
    for (StructDef s : node.structs)
      s.accept(this);
    // generate each function
    for (FunDef f : node.functions)
      f.accept(this);
  }

  
  /**
   * Generates a function definition
   */
  public void visit(FunDef node) {
    currTemplate = new VMFrameTemplate(node.funName.lexeme);

    varTable.pushEnvironment();

    // parameters
    for (var param : node.params) {
      varTable.add(param.varName.lexeme);
      add(VMInstr.STORE(varTable.get(param.varName.lexeme)));
    }

    // statements
    execBody(node.stmts);

    // add a return null if no other return statement
    if (node.stmts.isEmpty() || !(node.stmts.getLast() instanceof ReturnStmt)) {
      add(VMInstr.PUSH(VM.NULL));
      add(VMInstr.RET());
    }

    varTable.popEnvironment();

    vm.add(currTemplate);
  }

  
  /**
   * Adds the struct def to the list of structs.
   */
  public void visit(StructDef node) {
    structs.put(node.structName.lexeme, node);
  }

  
  /**
   * The visitor function for a variable definition, but this visitor
   * function is not used in code generation.
   */
  public void visit(VarDef node) {
    // nothing to do here
  }

  public void visit(ReturnStmt node) {
    node.expr.accept(this);
    add(VMInstr.RET());
  }

  public void visit(VarStmt node) {
    varTable.add(node.varName.lexeme);
    if (node.expr.isPresent()) {
      node.expr.get().accept(this);
      add(VMInstr.STORE(varTable.get(node.varName.lexeme)));
    } else {
      add(VMInstr.PUSH(VM.NULL));
      add(VMInstr.STORE(varTable.get(node.varName.lexeme)));
    }
  }

  public void visit(AssignStmt node) {

    VarRef first = node.lvalue.getFirst();
    var firstName = first.varName.lexeme;

    // single var assignment
    if (node.lvalue.size() == 1) {
      if (first.arrayExpr.isPresent()) {
        // oid
        add(VMInstr.LOAD(varTable.get(firstName)));
        // index
        first.arrayExpr.get().accept(this);
        // value
        node.expr.accept(this);
        add(VMInstr.SETI());
      } else {
        node.expr.accept(this);
        add(VMInstr.STORE(varTable.get(firstName)));
      }
      return;
    }

    // otherwise...

     add(VMInstr.LOAD(varTable.get(firstName)));

    // process arrayExpr if present for first
    if (first.arrayExpr.isPresent()) {
      first.arrayExpr.get().accept(this);
      add(VMInstr.GETI());
    }

    boolean firstDone = true;
    List<VarRef> lvalue = node.lvalue;
    for (int i = 0; i < lvalue.size(); i++) {
      VarRef var = lvalue.get(i);

      if (firstDone) { firstDone = false; continue; }


      if (i + 1 == lvalue.size()) {
        if (var.arrayExpr.isPresent()) {
          add(VMInstr.GETF(var.varName.lexeme));
          // oid already on stack from GETF
          // index
          var.arrayExpr.get().accept(this);
          // value
          node.expr.accept(this);
          add(VMInstr.SETI());
        } else {
          node.expr.accept(this);
          add(VMInstr.SETF(var.varName.lexeme));
        }
        return;
      }

      add(VMInstr.GETF(var.varName.lexeme));

      if (var.arrayExpr.isPresent()) {
        var.arrayExpr.get().accept(this);
        add(VMInstr.GETI());
      }
    }

  }

  public void visit(WhileStmt node) {
    int loopStart = currTemplate.instructions.size();
    node.condition.accept(this);
    int jumpfIndex = currTemplate.instructions.size();
    add(VMInstr.JMPF(-1));
    varTable.pushEnvironment();
    execBody(node.stmts);
    varTable.popEnvironment();
    add(VMInstr.JMP(loopStart));
    currTemplate.instructions.set(jumpfIndex, VMInstr.JMPF(currTemplate.instructions.size()));
  }

  public void visit(ForStmt node) {
    // new env for the forStmt var
    varTable.pushEnvironment();

    // populate stuff for the for stmt var
    varTable.add(node.varName.lexeme);
    node.fromExpr.accept(this);
    add(VMInstr.STORE(varTable.get(node.varName.lexeme)));

    // loop start for the final jump
    int loopStart = currTemplate.instructions.size();

    // populate the condition code
    add(VMInstr.LOAD(varTable.get(node.varName.lexeme)));
    node.toExpr.accept(this);
    add(VMInstr.CMPLE());
    int jumpfIndex = currTemplate.instructions.size();
    add(VMInstr.JMPF(-1));

    // populate the body stmts
    execBody(node.stmts);

    // inc the stmt var
    add(VMInstr.LOAD(varTable.get(node.varName.lexeme)));
    add(VMInstr.PUSH(1));
    add(VMInstr.ADD());
    add(VMInstr.STORE(varTable.get(node.varName.lexeme)));

    varTable.popEnvironment();

    // jump to the condition
    add(VMInstr.JMP(loopStart));
    // set the false jump to the end
    currTemplate.instructions.set(jumpfIndex, VMInstr.JMPF(currTemplate.instructions.size()));
  }

  public void visit(IfStmt node) {
    // populate the condition
    node.condition.accept(this);
    // jump variable to get to after the stmts
    int jumpfIndex = currTemplate.instructions.size();
    add(VMInstr.JMPF(-1));

    // populate the main stmts
    varTable.pushEnvironment();
    execBody(node.ifStmts);
    varTable.popEnvironment();

    int jumpToEndIndex = currTemplate.instructions.size();
    add(VMInstr.JMP(-1));

    // set that jump var to a proper val
    currTemplate.instructions.set(jumpfIndex, VMInstr.JMPF(currTemplate.instructions.size()));

    // populate elseIf if present
    node.elseIf.ifPresent(ifStmt -> ifStmt.accept(this));

    if (node.elseStmts.isPresent()) {
      // populate the else stmts
      varTable.pushEnvironment();
      execBody(node.elseStmts.get());
      varTable.popEnvironment();
    }

    // Fix the jump after successful if stmt
    currTemplate.instructions.set(jumpToEndIndex, VMInstr.JMP(currTemplate.instructions.size()));
  }

  public void visit(BasicExpr node) {
    node.rvalue.accept(this);
  }

  public void visit(UnaryExpr node) {
    node.expr.accept(this);
    add(VMInstr.NOT());
  }

  public void visit(BinaryExpr node) {
    if (node.binaryOp.tokenType == TokenType.GREATER || node.binaryOp.tokenType == TokenType.GREATER_EQ) {
      node.rhs.accept(this);
      node.lhs.accept(this);
    } else {
      node.lhs.accept(this);
      node.rhs.accept(this);
    }
    switch (node.binaryOp.tokenType) {
      // arithmetic
      case PLUS -> add(VMInstr.ADD());
      case MINUS -> add(VMInstr.SUB());
      case TIMES -> add(VMInstr.MUL());
      case DIVIDE -> add(VMInstr.DIV());
      // comparator
      case EQUAL -> add(VMInstr.CMPEQ());
      case NOT_EQUAL -> {
        add(VMInstr.CMPEQ());
        add(VMInstr.NOT());
      }
      case LESS, GREATER -> add(VMInstr.CMPLT());
      case LESS_EQ, GREATER_EQ -> add(VMInstr.CMPLE());
      // boolean
      case AND -> add(VMInstr.AND());
      case OR -> add(VMInstr.OR());
    }
  }

  public void visit(CallRValue node) {
    for (int i = node.args.size() - 1; i >= 0; i--) {
      node.args.get(i).accept(this);
    }

    // handle builtins
    switch (node.funName.lexeme) {
      case "print" -> {
        add(VMInstr.WRITE());
        add(VMInstr.PUSH(VM.NULL));
        return;
      }
      case "println" -> {
        add(VMInstr.WRITE());
        add(VMInstr.PUSH("\n"));
        add(VMInstr.WRITE());
        add(VMInstr.PUSH(VM.NULL));
        return;
      }
      case "readln" -> {
        add(VMInstr.READ());
        return;
      }
      case "size" -> {
        add(VMInstr.LEN());
        return;
      }
      case "get" -> {
        add(VMInstr.GETC());
        return;
      }
      case "int_val" -> {
        add(VMInstr.TOINT());
        return;
      }
      case "dbl_val" -> {
        add(VMInstr.TODBL());
        return;
      }
      case "str_val" -> {
        add(VMInstr.TOSTR());
        return;
      }
    }

    // handle user defined functions
    add(VMInstr.CALL(node.funName.lexeme));
    // TODO: Maybe deal with null returns?
  }

  public void visit(SimpleRValue node) {
    switch (node.literal.tokenType) {
      case INT_VAL -> add(VMInstr.PUSH(Integer.parseInt(node.literal.lexeme)));
      case DOUBLE_VAL -> add(VMInstr.PUSH(Double.parseDouble(node.literal.lexeme)));
      case BOOL_VAL -> add(VMInstr.PUSH(Boolean.parseBoolean(node.literal.lexeme)));
      case STRING_VAL -> add(VMInstr.PUSH(node.literal.lexeme));
      case NULL_VAL -> add(VMInstr.PUSH(VM.NULL));
    }
  }

  public void visit(NewStructRValue node) {
    add(VMInstr.ALLOCS());
    for (int i = 0; i < node.args.size(); i++) {
      add(VMInstr.DUP());
      node.args.get(i).accept(this);
      add(VMInstr.SETF(structs.get(node.structName.lexeme).fields.get(i).varName.lexeme));
    }
  }

  public void visit(NewArrayRValue node) {
    node.arrayExpr.accept(this);
    add(VMInstr.ALLOCA());

  }

  public void visit(VarRValue node) {
    VarRef first = node.path.getFirst();
    var firstName = first.varName.lexeme;
    add(VMInstr.LOAD(varTable.get(firstName)));

    // process arrayExpr if present for first
    if (first.arrayExpr.isPresent()) {
      first.arrayExpr.get().accept(this);
      add(VMInstr.GETI());
    }

    boolean firstDone = true;
    for (VarRef var : node.path) {
      if (firstDone) { firstDone = false; continue;}

      add(VMInstr.GETF(var.varName.lexeme));

      if (var.arrayExpr.isPresent()) {
        var.arrayExpr.get().accept(this);
        add(VMInstr.GETI());
      }
    }
  }


  /**
   * The visitor function for data types, but not used in code generation.
   */
  public void visit(DataType node) {
    // nothing to do here
  }



  
}
