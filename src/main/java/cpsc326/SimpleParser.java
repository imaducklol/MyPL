/**
 * CPSC 326, Spring 2025
 * The Simple Parser implementation.
 *
 * PUT YOUR NAME HERE IN PLACE OF THIS TEXT
 */

package cpsc326;

import java.util.List;


/**
 * Simple recursive descent parser for checking program syntax.
 */ 
public class SimpleParser {

  private Lexer lexer;          // the lexer
  private Token currToken;      // the current token

  /**
   * Create a SimpleParser from the give lexer.
   * @param lexer The lexer for the program to parse.
   */ 
  public SimpleParser(Lexer lexer) {
    this.lexer = lexer;
  }

  /**
   * Run the parser.
   */
  public void parse() {
    advance();
    program();
    eat(TokenType.EOS, "expecting end of file");
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
   * @param targetTokenType The token type to check against.
   */
  private void eat(TokenType targetTokenType, String msg) {
    if (!match(targetTokenType))
      error(msg);
    advance();
  }
  
  /**
   * Helper to check that the current token is a binary operator.
   */
  private boolean isBinOp() {
    return matchAny(List.of(TokenType.PLUS, TokenType.MINUS, TokenType.TIMES,
                            TokenType.DIVIDE, TokenType.AND, TokenType.OR,
                            TokenType.EQUAL, TokenType.LESS, TokenType.GREATER,
                            TokenType.LESS_EQ, TokenType.GREATER_EQ,
                            TokenType.NOT_EQUAL));
  }

  /**
   * Helper to check that the current token is a literal value.
   */
  private boolean isLiteral() {
    return matchAny(List.of(TokenType.INT_VAL, TokenType.DOUBLE_VAL,
                            TokenType.STRING_VAL, TokenType.BOOL_VAL,
                            TokenType.NULL_VAL));
  }

  /**
   * Checks for a valid program.
   */ 
  private void program() {
    while (!match(TokenType.EOS)) {
      // Struct definition
      if (match(TokenType.STRUCT)) {
        advance();
        structDef();

      }
    }
  }

  /**
   * Checks for a valid struct definition.
   */ 
  private void structDef() {
    // TODO: implement this function
  }

  /**
   * Checks for valid fields
   */
  private void fields() {

  }

  /**
   * Checks for a valid field
   */
  private void field() {

  }

  /**
   * Checks for a valid function definition.
   */ 
  private void funDef() {
    // TODO: implement this function
  }

  /**
   * Checks for a valid block
   */
  private void block() {

  }

  /**
   * Checks for a valid returnType
   */
  private void returnType() {

  }

  /**
   * Checks for valid params
   */
  private void params() {

  }

  /**
   * Checks for a valid param
   */
  private void param() {

  }

  /**
   * Checks for a valid dataType
   */
  private void dataType() {

  }

  /**
   * Checks for a valid blockType
   */
  private void blockType() {

  }

  /**
   * Checks for a valid stmt
   */
  private void stmt() {

  }

  /**
   * Checks for a valid varStmt
   */
  private void varStmt() {

  }

  /**
   * Checks for a valid varType
   */
  private void varType() {

  }

  /**
   * Checks for a valid varInit
   */
  private void varInit() {

  }

  /**
   * Checks for a valid whileStmt
   */
  private void whileStmt() {

  }

  /**
   * Checks for a valid ifStmt
   */
  private void ifStmt() {

  }

  /**
   * Checks for a valid forStmt
   */
  private void forStmt() {

  }

  /**
   * Checks for a valid returnStmt
   */
  private void returnStmt() {

  }

  /**
   * Checks for a valid assignStmt
   */
  private void assignStmt() {

  }

  /**
   * Checks for a valid lvalue
   */
  private void lvalue() {

  }

  /**
   * Checks for a valid funCall
   */
  private void funCall() {

  }

  /**
   * Checks for a valid args
   */
  private void args() {

  }

  /**
   * Checks for a valid expr
   */
  private void expr() {

  }

  /**
   * Checks for a valid binOp
   */
  private void binOp() {

  }

  /**
   * Checks for a valid rvalue
   */
  private void rvalue() {

  }

  /**
   * Checks for a valid newRvalue
   */
  private void newRvalue() {

  }

  /**
   * Checks for a valid literal
   */
  private void literal() {

  }

  /**
   * Checks for a valid varRvalue
   */
  private void varRvalue() {

  }
}
