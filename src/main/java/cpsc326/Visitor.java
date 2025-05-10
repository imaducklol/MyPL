/**
 * CPSC 326, Spring 2025
 * Basic Visitor API.
 */

package cpsc326;

public interface Visitor {
  // general AST classes
  void visit(Program node);

  void visit(FunDef node);

  void visit(StructDef node);

  void visit(DataType node);

  void visit(VarDef node);

  // statements
  void visit(ReturnStmt node);

  void visit(VarStmt node);

  void visit(AssignStmt node);

  void visit(WhileStmt node);

  void visit(ForStmt node);

  void visit(IfStmt node);

  // expressions
  void visit(BasicExpr node);

  void visit(UnaryExpr node);

  void visit(BinaryExpr node);

  void visit(CallRValue node);

  void visit(SimpleRValue node);

  void visit(NewStructRValue node);

  void visit(NewArrayRValue node);

  void visit(VarRValue node);
}
