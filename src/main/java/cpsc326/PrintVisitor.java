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

  
}
