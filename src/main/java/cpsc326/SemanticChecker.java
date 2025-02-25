/**
 * CPSC 326, Spring 2025
 * The Semantic Checker implementation.
 * 
 * PUT YOUR NAME HERE IN PLACE OF THIS TEXT
 */


package cpsc326;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;


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
    for (StructDef s : node.structs) {
      s.accept(this);
    }
    // check each function
    for (FunDef f : node.functions) {
      f.accept(this);
    }
  }
  
  /**
   * Checks a function definition signature and body ensuring valid
   * data types and no duplicate parameters
   */
  public void visit(FunDef node) {
    String funName = node.funName.lexeme;
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
    // TODO ... 
  }
  
  // TODO: Finish the remaining visit functions
  
}
