/**
 * CPSC 326, Spring 2025
 * The Semantic Checker implementation.
 * 
 * Orion Hess
 */


package cpsc326;

import java.util.*;


public class SemanticChecker implements Visitor {

  // for tracking function and struct definitions: 
  private Map<String,FunDef> functions = new HashMap<>();   
  private Map<String,StructDef> structs = new HashMap<>();  
  // for tracking variable types:
  private SymbolTable symbolTable = new SymbolTable();      
  // for holding the last inferred type:
  private DataType currType;                                

  //----------------------------------------------------------------------
  // Helper functions
  //----------------------------------------------------------------------
  
  /**
   */
  private boolean isBaseType(String type) {
    return List.of("int", "double", "bool", "string").contains(type); 
  }
  
  /**
   */
  private boolean isBuiltInFunction(String name) {
    return List.of("print", "println", "readln", "size", "get", "int_val",
                   "dbl_val", "str_val").contains(name);
  }
  
  /**
   * Create an error message
   */
  private void error(String msg) {
    MyPLException.staticError(msg);
  }
  
  /**
   * Creates an error 
   */
  private void error(String msg, Token token) {
    String s = "[%d,%d] %s";
    MyPLException.staticError(String.format(s, token.line, token.column, msg));
  }
  
  /**
   * Checks if the field name is a field in the struct
   * definition. This is a helper method for checking and inferring
   * assignment statement lvalues and var rvalue paths.
   * @param fieldName the field name to check for
   * @param structDef the struct definition to check
   * @returns true if a match and false otherwise
   */
  private boolean isStructField(String fieldName, StructDef structDef) {
    for (var field : structDef.fields) 
      if (field.varName.lexeme.equals(fieldName))
        return true;
    return false;
  }

  
  /**
   * Obtains the data type for the field name in the struct
   * definition. This is a helper method for checking and inferring
   * assignment statement lvalues and var rvalue paths.
   * @param fieldName the field name 
   * @param structDef the struct definition
   * @returns the corresponding data type or null if no such field exists
   */
  private DataType getStructFieldType(String fieldName, StructDef structDef) {
    for (var field : structDef.fields)
      if (field.varName.lexeme.equals(fieldName))
        return field.dataType;
    return null;
  }  
  
  //----------------------------------------------------------------------
  // Visit Functions
  //----------------------------------------------------------------------

  /**
   * Checks the program
   */
  public void visit(Program node) {
    // 1. record each struct definitions and check for duplicate names
    // 2. record each function definition and check for duplicate names
    // 3. check for a main function
    // 4. check each struct

    // check each struct
    for (StructDef s : node.structs) {
      s.accept(this);
    }
    // check each function
    for (FunDef f : node.functions) {
      f.accept(this);
    }
    // check for main
    if (!functions.containsKey("main")) {
      error("No function 'main' found");
    }
  }
  
  /**
   * Checks a function definition signature and body ensuring valid
   * data types and no duplicate parameters
   */
  public void visit(FunDef node) {
    String funName = node.funName.lexeme;
    // create the mapping
    functions.put(funName, node);
    // push environment for function context
    symbolTable.pushEnvironment();

    // process all the parameters
    for (VarDef param : node.params) {
      param.accept(this);
    }
    // process all the statements
    for (Stmt stmt : node.stmts) {
      stmt.accept(this);
    }
    // pop environment to leave function context
    symbolTable.popEnvironment();
    // set return type
    currType = node.returnType;

    // 1. check signature if it is main
    // 2. add an environment for params
    // 3. check and add the params (no duplicate param var names)
    // 4. add the return type
    // 5. check the body statements
  }

  /**
   * Checks structs for duplicate fields and valid data types
   */
  public void visit(StructDef node) {
    String structName = node.structName.lexeme;
    structs.put(structName, node);
    List<String> names = new ArrayList<>();
    for (var field : node.fields) {
      if (names.contains(field.varName.lexeme)) {
        error("Duplicate field in struct definition", node.structName);
      }
      names.add(field.varName.lexeme);
    }
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
    node.rvalue.accept(this);
  }

  public void visit(UnaryExpr node) {
    node.expr.accept(this);
    if (!currType.type.lexeme.equals("bool")) {
      error("UnaryExpr expected to be bool", node.unaryOp);
    }
  }
  public void visit(BinaryExpr node) {

  }
  public void visit(CallRValue node) {

  }

  /**
   * sets return type for right side
   */
  public void visit(SimpleRValue node) {
    TokenType literalType = node.literal.tokenType;
    int line = node.literal.line;
    int column = node.literal.column;
    Token typeToken = null;
    switch (literalType) {
      case TokenType.INT_VAL -> typeToken = new Token(TokenType.INT_TYPE, "int", line, column);
      case TokenType.DOUBLE_VAL -> typeToken = new Token(TokenType.DOUBLE_TYPE, "double", line, column);
      case TokenType.BOOL_VAL -> typeToken = new Token(TokenType.BOOL_TYPE, "bool", line, column);
      case TokenType.STRING_VAL -> typeToken = new Token(TokenType.STRING_TYPE, "string", line, column);
      case TokenType.NULL_VAL -> typeToken = new Token(TokenType.VOID_TYPE, "null", line, column);
    }
    currType = new DataType();
    currType.type = typeToken;
    // TODO Deal with arrays
  }

  public void visit(NewStructRValue node) {

  }
  public void visit(NewArrayRValue node) {

  }
  public void visit(VarRValue node) {

  }
}
