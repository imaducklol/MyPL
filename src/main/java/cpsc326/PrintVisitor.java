/**
 * CPSC 326, Spring 2025
 * Pretty print visitor.
 *
 * PUT YOUR NAME HERE IN PLACE OF THIS TEXT 
 */


package cpsc326;

public class PrintVisitor implements Visitor {

  private int indent = 0;

  /**
   * Prints message without ending newline
   */
  private void write(String s) {
    System.out.print(s);
  }

  /**
   * Increase the indent level by one
   */
  private void incIndent() {
    indent++;
  }

  /**
   * Decrease the indent level by one
   */
  private void decIndent() {
    indent--;
  }
  
  /**
   * Print an initial indent string
   */
  private String indent() {
    return "  ".repeat(indent);
  }

  /**
   * Prints a newline
   */
  private void newline() {
    System.out.println();
  }
  
  /**
   * Prints the program
   */
  public void visit(Program node) {
    // always one blank line at the "top"
    newline();
    for (StructDef s : node.structs)
      s.accept(this);
    for (FunDef f : node.functions)
      f.accept(this);
  }

  // TODO: Complete the rest of the visit functions.

  // Use the above helper functions to write, deal with indentation,
  // and print newlines as part of your visit functions.

  // general AST classes
  public void visit(FunDef node) {

  }
  public void visit(StructDef node) {

  }
  public void visit(DataType node) {

  }
  public void visit(VarDef node) {

  }
  // statements
  public void visit(ReturnStmt node) {

  }
  public void visit(VarStmt node) {

  }
  public void visit(AssignStmt node) {

  }
  public void visit(WhileStmt node) {

  }
  public void visit(ForStmt node) {

  }
  public void visit(IfStmt node) {

  }
  // expressions
  public void visit(BasicExpr node) {

  }
  public void visit(UnaryExpr node) {

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

  
}
