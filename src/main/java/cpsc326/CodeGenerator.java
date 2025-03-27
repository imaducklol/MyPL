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
    // TODO: see lecture notes

    // NOTE: to generate the body code (for this and other
    // statements), you can use: execBody(node.stmts)
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

  
  /**
   * The visitor function for data types, but not used in code generation.
   */
  public void visit(DataType node) {
    // nothing to do here
  }

  
  // TODO: Finish the remaining visit functions ... 

  
}
