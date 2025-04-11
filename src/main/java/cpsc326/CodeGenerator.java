/**
 * CPSC 326, Spring 2025
 * 
 * PUT YOUR NAME HERE
 */


package cpsc326;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;


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
    if (!(node.stmts.getLast() instanceof ReturnStmt)) {
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
    }
  }

  public void visit(AssignStmt node) {

  }

  public void visit(WhileStmt node) {
    int loopStart = currTemplate.instructions.size();
    node.condition.accept(this);
    VMInstr jump = VMInstr.JMPF(-1);
    add(jump);
    varTable.pushEnvironment();
    execBody(node.stmts);
    varTable.popEnvironment();
    jump = VMInstr.JMPF(currTemplate.instructions.size());
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
    VMInstr jump = VMInstr.JMPF(-1);
    add(jump);

    // populate the body stmts
    execBody(node.stmts);
    varTable.popEnvironment();

    // jump to the condition
    add(VMInstr.JMP(loopStart));
    // set the false jump to the end
    jump = VMInstr.JMPF(currTemplate.instructions.size());
  }

  public void visit(IfStmt node) {
    // populate the condition
    node.condition.accept(this);
    // jump variable to get to after the stmts
    VMInstr jump = VMInstr.JMPF(-1);
    add(jump);

    // populate the main stmts
    varTable.pushEnvironment();
    execBody(node.ifStmts);
    varTable.popEnvironment();

    // set that jump var to a proper val
    jump = VMInstr.JMPF(currTemplate.instructions.size());

    // populate elseIf if present
    node.elseIf.ifPresent(ifStmt -> ifStmt.accept(this));

    // exit if there are no else stmts
    if (!node.elseStmts.isPresent()) return;

    // populate the else stmts
    varTable.pushEnvironment();
    execBody(node.elseStmts.get());
    varTable.popEnvironment();
  }

  public void visit(BasicExpr node) {
    node.rvalue.accept(this);
  }

  public void visit(UnaryExpr node) {
    node.expr.accept(this);
    add(VMInstr.NOT());
  }

  public void visit(BinaryExpr node) {

  }

  public void visit(CallRValue node) {

  }

  public void visit(SimpleRValue node) {

  }

  public void visit(NewStructRValue node) {

  }

  public void visit(NewArrayRValue node) {

  }

  public void visit(VarRValue node) {

  }


  /**
   * The visitor function for data types, but not used in code generation.
   */
  public void visit(DataType node) {
    // nothing to do here
  }



  
}
