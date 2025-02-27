/**
 * CPSC 326, Spring 2025
 * Pretty print visitor.
 * <p>
 * PUT YOUR NAME HERE IN PLACE OF THIS TEXT
 */


package cpsc326;

import java.util.List;

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
    write(indent());
    if (node.returnType.isArray) {
      write("[" + node.returnType.type.lexeme + "]");
    } else {
      write(node.returnType.type.lexeme);
    }
    write(" " + node.funName.lexeme + "(");
    List<VarDef> params = node.params;
    for (int i = 0; i < params.size(); i++) {
      params.get(i).accept(this);
      if (i + 1 < params.size()) {
        write(", ");
      }
    }
    write(") {");
    newline();
    incIndent();
    for (Stmt s : node.stmts) {
      write(indent());
      s.accept(this);
      newline();
    }
    decIndent();
    write("}");
    newline();
    newline();
  }

  public void visit(StructDef node) {
    write("struct " + node.structName.lexeme + " {");
    newline();
    incIndent();
    List<VarDef> fields = node.fields;
    for (int i = 0; i < fields.size(); i++) {
      VarDef v = fields.get(i);
      write(indent());
      v.accept(this);
      if (i + 1 < fields.size()) {
        write(", ");
      }
      newline();
    }
    decIndent();
    write("}");
    newline();
    newline();
  }

  public void visit(DataType node) {
    if (node.isArray) {
      write("[" + node.type.lexeme + "]");
    } else {
      write(node.type.lexeme);
    }
  }

  public void visit(VarDef node) {
    write(node.varName.lexeme + ": ");
    if (node.dataType.isArray) {
      write("[" + node.dataType.type.lexeme + "]");
    }
    else {
      write(node.dataType.type.lexeme);
    }
  }

  // statements
  public void visit(ReturnStmt node) {
    write("return ");
    node.expr.accept(this);
  }

  public void visit(VarStmt node) {
    write("var " + node.varName.lexeme);
    if (node.dataType.isPresent()) {
      if (node.dataType.get().isArray) {
        write(": [" + node.dataType.get().type.lexeme + "]");
      } else {
        write(": " + node.dataType.get().type.lexeme);
      }
    }
    if (node.expr.isPresent()) {
      write(" = ");
      node.expr.get().accept(this);
    }
  }

  public void visit(AssignStmt node) {
    List<VarRef> lvalue = node.lvalue;
    for (int i = 0; i < lvalue.size(); i++) {
      VarRef v = lvalue.get(i);
      write(v.varName.lexeme);
      if (v.arrayExpr.isPresent()) {
        write("[");
        v.arrayExpr.get().accept(this);
        write("]");
      }
      if (i + 1 < lvalue.size()) {
        write(".");
      }
    }
    write(" = ");
    node.expr.accept(this);
  }

  public void visit(WhileStmt node) {
    write("while ");
    node.condition.accept(this);
    write(" {");
    newline();
    incIndent();
    for (Stmt s : node.stmts) {
      write(indent());
      s.accept(this);
      newline();
    }
    decIndent();
    write(indent() + "}");
  }

  public void visit(ForStmt node) {
    write("for " + node.varName.lexeme + " from ");
    node.fromExpr.accept(this);
    write(" to ");
    node.toExpr.accept(this);
    write(" {");
    newline();
    incIndent();
    for (Stmt s : node.stmts) {
      write(indent());
      s.accept(this);
      newline();
    }
    decIndent();
    write(indent() + "}");
  }

  public void visit(IfStmt node) {
    write("if ");
    node.condition.accept(this);
    write(" {");
    newline();
    incIndent();
    for (Stmt s : node.ifStmts) {
      write(indent());
      s.accept(this);
      newline();
    }
    decIndent();
    write(indent() + "}");
    if (node.elseIf.isPresent()) {
      newline();
      write(indent() + "else ");
      node.elseIf.get().accept(this);
    }
    if (node.elseStmts.isPresent()) {
      newline();
      write(indent() + "else {");
      newline();
      incIndent();
      for (Stmt s : node.elseStmts.get()) {
        write(indent());
        s.accept(this);
        newline();
      }
      decIndent();
      write(indent() + "}");
    }
  }

  // expressions

  public void visit(BasicExpr node) {
    node.rvalue.accept(this);
  }

  public void visit(UnaryExpr node) {
    write("not (");
    node.expr.accept(this);
    write(")");
  }

  public void visit(BinaryExpr node) {
    write("(");
    node.lhs.accept(this);
    write(" " + node.binaryOp.lexeme + " ");
    node.rhs.accept(this);
    write(")");
  }

  public void visit(CallRValue node) {
    write(node.funName.lexeme + "(");
    List<Expr> args = node.args;
    for (int i = 0; i < args.size(); i++) {
      args.get(i).accept(this);
      if (i + 1 < args.size()) {
        write(", ");
      }
    }
    write(")");
  }

  public void visit(SimpleRValue node) {
    if (node.literal.tokenType.equals(TokenType.STRING_VAL)) {
      write("\"" + node.literal.lexeme + "\"");
    } else {
      write(node.literal.lexeme);
    }
  }

  public void visit(NewStructRValue node) {
    write("new " + node.structName.lexeme + "(");
    List<Expr> args = node.args;
    for (int i = 0; i < node.args.size(); i++) {
      args.get(i).accept(this);
      if (i + 1 < args.size()) {
        write(", ");
      }
    }
    write(")");
  }

  public void visit(NewArrayRValue node) {
    write("new " + node.type.lexeme + "[");
    node.arrayExpr.accept(this);
    write("]");
  }

  public void visit(VarRValue node) {
    List<VarRef> path = node.path;
    for (int i = 0; i < path.size(); i++) {
      VarRef v = path.get(i);
      write(v.varName.lexeme);
      if (v.arrayExpr.isPresent()) {
        write("[");
        v.arrayExpr.get().accept(this);
        write("]");
      }
      if (i + 1 < path.size()) {
        write(".");
      }
    }
  }
}
