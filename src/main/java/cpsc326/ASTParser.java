/**
 * CPSC 326, Spring 2025
 * The AST Parser implementation.
 *
 * PUT YOUR NAME HERE IN PLACE OF THIS TEXT
 */

package cpsc326;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
  

/**
 * Simple recursive descent parser for checking program syntax.
 */ 
public class ASTParser {

  private Lexer lexer;          // the lexer
  private Token currToken;      // the current token

  /**
   * Create a SimpleParser from the give lexer.
   * @param lexer The lexer for the program to parse.
   */ 
  public ASTParser(Lexer lexer) {
    this.lexer = lexer;
  }

  /**
   * Run the parser.
   */
  public Program parse() {
    advance();
    Program p = program();
    eat(TokenType.EOS, "expecting end of file");
    return p;
  }

  /**
   * Generate and throw a mypl parser exception.
   * @param msg The error message.
   */
  private void error(String msg) {
    String lexeme = currToken.lexeme;
    int line = currToken.line;
    int column = currToken.column;
    String s = "[%d,%d] %s found '%s'";
    MyPLException.parseError(String.format(s, line, column, msg, lexeme));
  }

  /**
   * Move to the next lexer token, skipping comments.
   */
  private void advance() {
    currToken = lexer.nextToken();
    while (match(TokenType.COMMENT))
      currToken = lexer.nextToken();
  }

  /**
   * Checks that the current token has the given token type.
   * @param targetTokenType The token type to check against.
   * @return True if the types match, false otherwise.
   */
  private boolean match(TokenType targetTokenType) {
    return currToken.tokenType == targetTokenType; 
  }

  /**
   * Checks that the current token is contained in the given list of
   * token types.
   * @param targetTokenTypes The token types ot check against.
   * @return True if the current type is in the given list, false
   * otherwise.
   */
  private boolean matchAny(List<TokenType> targetTokenTypes) {
    return targetTokenTypes.contains(currToken.tokenType);
  }

  /**
   * Advance to next token if current token matches the given token type.
   * @param targetType The token type to check against.
   */
  private void eat(TokenType targetTokenType, String msg) {
    if (!match(targetTokenType))
      error(msg);
    advance();
  }
  
  /**
   * Check if the current token is an allowed binary operator
   */
  private boolean isBinOp() {
    return matchAny(List.of(TokenType.PLUS, TokenType.MINUS, TokenType.TIMES,
                            TokenType.DIVIDE, TokenType.AND, TokenType.OR,
                            TokenType.EQUAL, TokenType.LESS, TokenType.GREATER,
                            TokenType.LESS_EQ, TokenType.GREATER_EQ,
                            TokenType.NOT_EQUAL));
  }

  /**
   * Check if the current token is a literal value
   */
  private boolean isLiteral() {
    return matchAny(List.of(TokenType.INT_VAL, TokenType.DOUBLE_VAL,
                            TokenType.STRING_VAL, TokenType.BOOL_VAL,
                            TokenType.NULL_VAL));
  }

  /**
   * Parse the program
   * @return the corresponding Program AST object
   */
  private Program program() {
    // TODO: implement this function
  }

  /**
   * Parse a struct definition
   * @return the corresponding StructDef AST object
   */
  private StructDef structDef() {
    // TODO: implement this function
  }

  /**
   * Parse a function definition
   * @return the corresponding FunDef AST object
   */
  private FunDef funDef() {
    // TODO: implement this function
  }


  // ... and so on ...   
  // TODO: implement the rest of the recursive descent functions 
  
}
