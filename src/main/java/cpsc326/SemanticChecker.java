/**
 * CPSC 326, Spring 2025
 * The Semantic Checker implementation.
 * <p>
 * Orion Hess
 */


package cpsc326;

import java.util.*;


public class SemanticChecker implements Visitor {

  // for tracking function and struct definitions: 
  private Map<String, FunDef> functions = new HashMap<>();
  private Map<String, StructDef> structs = new HashMap<>();
  // for tracking variable types:
  private SymbolTable symbolTable = new SymbolTable();
  // for holding the last inferred type:
  private DataType currType;

  //----------------------------------------------------------------------
  // Helper functions
  //----------------------------------------------------------------------

  /**
   *
   */
  private boolean isBaseType(String type) {
    return List.of("int", "double", "bool", "string").contains(type);
  }

  /**
   *
   */
  private boolean isBuiltInFunction(String name) {
    return List.of("print", "println", "readln", "size", "get", "int_val", "dbl_val", "str_val").contains(name);
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
   *
   * @param fieldName the field name to check for
   * @param structDef the struct definition to check
   * @returns true if a match and false otherwise
   */
  private boolean isStructField(String fieldName, StructDef structDef) {
    for (var field : structDef.fields)
      if (field.varName.lexeme.equals(fieldName)) return true;
    return false;
  }


  /**
   * Obtains the data type for the field name in the struct
   * definition. This is a helper method for checking and inferring
   * assignment statement lvalues and var rvalue paths.
   *
   * @param fieldName the field name
   * @param structDef the struct definition
   * @returns the corresponding data type or null if no such field exists
   */
  private DataType getStructFieldType(String fieldName, StructDef structDef) {
    for (var field : structDef.fields)
      if (field.varName.lexeme.equals(fieldName)) return field.dataType;
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

    // map struct definitions
    for (StructDef s : node.structs) {
      // check if duplicate struct name
      if (structs.containsKey(s.structName.lexeme)) {
        error("Duplicate struct", s.structName);
      }
      // create mapping
      structs.put(s.structName.lexeme, s);
    }

    // map function definitions
    for (FunDef f : node.functions) {
      // check if remapping a builtin
      if (isBuiltInFunction(f.funName.lexeme)) {
        error("Redefinition of builtin function", f.funName);
      }
      // check if duplicate function name
      if (functions.containsKey(f.funName.lexeme)) {
        error("Duplicate function name", f.funName);
      }
      // create mapping
      functions.put(f.funName.lexeme, f);
    }

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
    // Checks for main
    if (funName.equals("main")) {
      if (!node.params.isEmpty()) {
        error("Main function cannot have parameters", node.funName);
      }
      if (node.returnType.type.tokenType != TokenType.VOID_TYPE || node.returnType.isArray) {
        error("Main function must be of return type 'void'", node.funName);
      }
    }
    // push environment for function context
    symbolTable.pushEnvironment();

    // add return type for matching in return statements
    symbolTable.add("return", node.returnType);

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
    // Check return type validity
    Token type = node.returnType.type;
    if (!(isBaseType(type.lexeme) || structs.containsKey(type.lexeme) || type.tokenType == TokenType.VOID_TYPE)) {
      error("Undefined return type", node.returnType.type);
    }


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
    // push environment
    symbolTable.pushEnvironment();
    // check fields
    for (var field : node.fields) {
      field.accept(this);
    }
    // pop environment
    symbolTable.popEnvironment();
  }

  public void visit(DataType node) {

  }

  public void visit(VarDef node) {
    String varName = node.varName.lexeme;
    if (symbolTable.exists(varName)) {
      error("Redeclaration of variable", node.varName);
    }
    if (!isBaseType(node.dataType.type.lexeme) && !structs.containsKey(node.dataType.type.lexeme)) {
      error("Undefined variable type", node.dataType.type);
    }
    symbolTable.add(varName, node.dataType);
  }

  // statements
  public void visit(ReturnStmt node) {
    node.expr.accept(this);
    if ((symbolTable.get("return").type.tokenType != currType.type.tokenType ||
            symbolTable.get("return").isArray != currType.isArray)
            && currType.type.tokenType != TokenType.VOID_TYPE) {
      error("Return type does not match");
    }
  }

  public void visit(VarStmt node) {
    String varName = node.varName.lexeme;
    if (symbolTable.existsInCurrEnv(varName)) {
      error("Duplicate variable", node.varName);
    }
    DataType type = null;
    if (node.dataType.isPresent() && node.expr.isPresent()) {
      node.expr.get().accept(this);
      if ((node.dataType.get().type.tokenType != currType.type.tokenType || node.dataType.get().isArray != currType.isArray) && currType.type.tokenType != TokenType.VOID_TYPE) {
        error("Mismatched types in variable statement", node.varName);
      }
      type = node.dataType.get();
    } else if (node.dataType.isPresent()) {
      type = node.dataType.get();
    } else if (node.expr.isPresent()) {
      node.expr.get().accept(this);
      type = currType;
    } else {
      error("Unable to infer variable type", node.varName);
    }

    assert type != null;
    if (!isBaseType(type.type.lexeme) && !structs.containsKey(type.type.lexeme)) {
      error("Undefined variable type", type.type);
    }
    symbolTable.add(varName, type);
  }

  public void visit(AssignStmt node) {
    DataType lval = symbolTable.get(node.lvalue.getLast().varName.lexeme);
    node.expr.accept(this);
    DataType rval = currType;
    if ((lval.type.tokenType != rval.type.tokenType || lval.isArray != rval.isArray) && rval.type.tokenType != TokenType.VOID_TYPE) {
      error("Mismatched types in variable statement", node.lvalue.getLast().varName);
    }

  }

  public void visit(WhileStmt node) {
    node.condition.accept(this);
    if (currType.type.tokenType != TokenType.BOOL_TYPE || currType.isArray) {
      error("While condition did not resolve to bool");
    }
    // push new environment
    symbolTable.pushEnvironment();
    // check statements
    for (Stmt stmt : node.stmts) {
      stmt.accept(this);
    }
  }

  public void visit(ForStmt node) {
    // push new environment
    symbolTable.pushEnvironment();
    // make an int type for the new var
    DataType intType = new DataType();
    intType.type = new Token(TokenType.INT_TYPE, "int", node.varName.line, node.varName.column);
    // add it
    symbolTable.add(node.varName.lexeme, intType);

    // check from and to
    node.fromExpr.accept(this);
    if (currType.type.tokenType != TokenType.INT_TYPE || currType.isArray) {
      error("For from condition did not resolve to int");
    }
    node.toExpr.accept(this);
    if (currType.type.tokenType != TokenType.INT_TYPE || currType.isArray) {
      error("For to condition did not resolve to int");
    }
    // check statements
    for (Stmt stmt : node.stmts) {
      stmt.accept(this);
    }
  }

  public void visit(IfStmt node) {
    node.condition.accept(this);
    if (currType.type.tokenType != TokenType.BOOL_TYPE || currType.isArray) {
      error("If condition did not resolve to bool");
    }
    // push new environment
    symbolTable.pushEnvironment();
    // check statements
    for (Stmt stmt : node.ifStmts) {
      stmt.accept(this);
    }
    // pop environment
    symbolTable.popEnvironment();
    // handle elseif if present
    if (node.elseIf.isPresent()) {
      node.elseIf.get().accept(this);
    }
    // handle else if present
    if (node.elseStmts.isPresent()) {
      symbolTable.pushEnvironment();
      for (Stmt stmt : node.elseStmts.get()) {
        stmt.accept(this);
      }
      symbolTable.popEnvironment();
    }
  }

  // expressions
  public void visit(BasicExpr node) {
    node.rvalue.accept(this);
  }

  public void visit(UnaryExpr node) {
    node.expr.accept(this);
    if (currType.type.tokenType != TokenType.BOOL_TYPE) {
      error("UnaryExpr expected to be bool", node.unaryOp);
    }
  }

  public void visit(BinaryExpr node) {
    // lhs
    node.lhs.accept(this);
    DataType lhsType = currType;
    node.rhs.accept(this);
    DataType rhsType = currType;
    // check for same type excluding void comparisons
    if ((lhsType.type.tokenType != rhsType.type.tokenType || lhsType.isArray != rhsType.isArray)
            && rhsType.type.tokenType != TokenType.VOID_TYPE && lhsType.type.tokenType != TokenType.VOID_TYPE) {
      error("Type across binary operator must be the same", node.binaryOp);
    }
    if (List.of("<", "<=", ">", ">=").contains(node.binaryOp.lexeme)) {
      DataType type = new DataType();
      type.type = new Token(TokenType.BOOL_TYPE, "bool", node.binaryOp.line, node.binaryOp.column);
      currType = type;
      if (lhsType.isArray || rhsType.isArray ||
              List.of(TokenType.BOOL_TYPE, TokenType.VOID_TYPE).contains(lhsType.type.tokenType) ||
              List.of(TokenType.BOOL_TYPE, TokenType.VOID_TYPE).contains(rhsType.type.tokenType)) {
        error("Arrays and booleans are not valid for this operator", node.binaryOp);
      }
    } else if (List.of("!=", "==").contains(node.binaryOp.lexeme)) {
      DataType type = new DataType();
      type.type = new Token(TokenType.BOOL_TYPE, "bool", node.binaryOp.line, node.binaryOp.column);
      currType = type;

    } else if (List.of("+", "-", "*", "/").contains(node.binaryOp.lexeme)) {
      if (lhsType.isArray || rhsType.isArray ||
              List.of(TokenType.BOOL_TYPE, TokenType.VOID_TYPE).contains(lhsType.type.tokenType) ||
              List.of(TokenType.BOOL_TYPE, TokenType.VOID_TYPE).contains(rhsType.type.tokenType)) {
        error("Arrays and booleans are not valid for this operator #2", node.binaryOp);
      }
    } else if (lhsType.isArray || rhsType.isArray) {
      error("Arrays are not valid for this operator", node.binaryOp);
    }
  }

  public void visit(CallRValue node) {
    // check if builtin
    if (isBuiltInFunction(node.funName.lexeme)) {
      switch (node.funName.lexeme) {
        case "int_val": {
          // check args count
          if (1 != node.args.size()) {
            error("Wrong number of arguments supplied, expected 1", node.funName);
          }
          // check arg types
          node.args.getFirst().accept(this);
          if (!List.of(TokenType.DOUBLE_TYPE, TokenType.STRING_TYPE).contains(currType.type.tokenType)){
            error("Argument has wrong type, expected double or string type", node.funName);
          }
          // process function return type
          currType = new DataType();
          currType.type = new Token(TokenType.INT_TYPE, "int", node.funName.line, node.funName.column);
          currType.isArray = false;
          break;
        }
        case "dbl_val": {
          // check args count
          if (1 != node.args.size()) {
            error("Wrong number of arguments supplied, expected 1", node.funName);
          }
          // check arg types
          node.args.getFirst().accept(this);
          if (!List.of(TokenType.INT_TYPE, TokenType.STRING_TYPE).contains(currType.type.tokenType)){
            error("Argument has wrong type, expected int or string type", node.funName);
          }
          // process function return type
          currType = new DataType();
          currType.type = new Token(TokenType.DOUBLE_TYPE, "double", node.funName.line, node.funName.column);
          currType.isArray = false;
          break;
        }
        case "str_val": {
          // check args count
          if (1 != node.args.size()) {
            error("Wrong number of arguments supplied, expected 1", node.funName);
          }
          // check arg types
          node.args.getFirst().accept(this);
          if (!List.of(TokenType.INT_TYPE, TokenType.DOUBLE_TYPE).contains(currType.type.tokenType)){
            error("Argument has wrong type, expected int or double type", node.funName);
          }
          // process function return type
          currType = new DataType();
          currType.type = new Token(TokenType.STRING_TYPE, "string", node.funName.line, node.funName.column);
          currType.isArray = false;
          break;
        }
        case "print": {
          // check args count
          if (1 != node.args.size()) {
            error("Wrong number of arguments supplied, expected 1", node.funName);
          }
          node.args.getFirst().accept(this);
          if (currType.isArray) {
            error("Print cannot accept array types", node.funName);
          }
          // process function return type
          currType = new DataType();
          currType.type = new Token(TokenType.VOID_TYPE, "null", node.funName.line, node.funName.column);
          currType.isArray = false;
          break;
        }
        case "println": {
          // check args count
          if (1 != node.args.size()) {
            error("Wrong number of arguments supplied, expected 1", node.funName);
          }
          node.args.getFirst().accept(this);
          if (currType.isArray) {
            error("Println cannot accept array types", node.funName);
          }
          // process function return type
          currType = new DataType();
          currType.type = new Token(TokenType.VOID_TYPE, "null", node.funName.line, node.funName.column);
          currType.isArray = false;
          break;
        }
        case "readln": {
          // check args count
          if (!node.args.isEmpty()) {
            error("Wrong number of arguments supplied, expected 0", node.funName);
          }
          // process function return type
          currType = new DataType();
          currType.type = new Token(TokenType.STRING_TYPE, "string", node.funName.line, node.funName.column);
          currType.isArray = false;
          break;
        }
        case "size": {
          // check args count
          if (1 != node.args.size()) {
            error("Wrong number of arguments supplied, expected 1", node.funName);
          }
          // check arg types
          node.args.getFirst().accept(this);
          if (currType.type.tokenType != TokenType.STRING_TYPE && !currType.isArray) {
            error("Argument has wrong type, expected string or array type", node.funName);
          }
          // process function return type
          currType = new DataType();
          currType.type = new Token(TokenType.INT_TYPE, "int", node.funName.line, node.funName.column);
          currType.isArray = false;
          break;
        }
        case "get": {
          // check args count
          if (2 != node.args.size()) {
            error("Wrong number of arguments supplied, expected 2", node.funName);
          }
          // check arg types
          node.args.getFirst().accept(this);
          if (currType.type.tokenType != TokenType.INT_TYPE) {
            error("First argument has wrong type, expected int type", node.funName);
          }
          node.args.get(1).accept(this);
          if (currType.type.tokenType != TokenType.STRING_TYPE && !currType.isArray) {
            error("Second argument has wrong type, expected string or array type", node.funName);
          }
          // process function return type
          currType.type = new Token(currType.type.tokenType, currType.type.lexeme, node.funName.line, node.funName.column);
          currType.isArray = false;
          break;
        }
      }
      return;
    }
    // check if function has been declared
    if (!functions.containsKey(node.funName.lexeme)) {
      error("Function not declared", node.funName);
    }
    FunDef function = functions.get(node.funName.lexeme);
    // check if proper args supplied
    if (function.params.size() != node.args.size()) {
      error("Wrong number of arguments supplied", node.funName);
    }
    for (int i = 0; i < function.params.size(); i++) {
      node.args.get(i).accept(this);
      DataType funType = function.params.get(i).dataType;
      if ((funType.type.tokenType != currType.type.tokenType || funType.isArray != currType.isArray) && currType.type.tokenType != TokenType.VOID_TYPE) {
        error("Type of argument does not match definition", currType.type);
      }
    }

    // process function return type
    DataType returnType = function.returnType;
    currType = new DataType();
    currType.type = new Token(returnType.type.tokenType, returnType.type.lexeme, node.funName.line, node.funName.column);
    currType.isArray = returnType.isArray;
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
  }

  public void visit(NewStructRValue node) {
    // validate struct exists
    if (!structs.containsKey(node.structName.lexeme)) {
      error("Struct not declared", node.structName);
    }
    StructDef struct = structs.get(node.structName.lexeme);
    // validate field count
    if (struct.fields.size() != node.args.size()) {
      error("Wrong number of arguments supplied", node.structName);
    }
    // validate field types
    for (int i = 0; i < struct.fields.size(); i++) {
      node.args.get(i).accept(this);
      DataType fieldType = struct.fields.get(i).dataType;
      if (fieldType.type.tokenType != currType.type.tokenType && currType.type.tokenType != TokenType.VOID_TYPE) {
        error("Type of argument does not match definition", currType.type);
      }
    }
    // process new struct return type
    currType = new DataType();
    currType.type = new Token(TokenType.STRUCT, struct.structName.lexeme, node.structName.line, node.structName.column);
  }

  public void visit(NewArrayRValue node) {
    node.arrayExpr.accept(this);
    currType.type.tokenType = node.type.tokenType;
    currType.type.lexeme = node.type.lexeme;
    currType.isArray = true;
  }

  public void visit(VarRValue node) {
    VarRef var = null;
    for (VarRef varRef : node.path) {
      var = varRef;
    }
    Token varToken = var.varName;
    DataType type = new DataType();
    if (!symbolTable.exists(varToken.lexeme)) {
      error("Variable is not defined in current context", varToken);
    }
    type.type = new Token(symbolTable.get(varToken.lexeme).type.tokenType, varToken.lexeme, varToken.line, varToken.column);
    type.isArray = symbolTable.get(varToken.lexeme).isArray;
    currType = type;
  }
}
