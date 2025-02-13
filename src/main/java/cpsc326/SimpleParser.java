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
        structDef();
      }
      // Function definition
      else {
        funDef();
      }
    }
  }

  /**
   * Checks for a valid struct definition.
   */ 
  private void structDef() {
    // First already checked
    advance();
    eat(TokenType.ID, "Expected ID");
    eat(TokenType.LBRACE, "Expected LBRACE");
    fields();
    eat(TokenType.RBRACE, "Expected RBRACE");
  }

  /**
   * Checks for valid fields
   */
  private void fields() {
    if (match(TokenType.ID)) {
      field();
      while (match(TokenType.COMMA)) {
        advance();
        field();
      }
    }
  }

  /**
   * Checks for a valid field
   */
  private void field() {
    // ID already checked
    advance();
    eat(TokenType.COLON, "Expected COLON");
    dataType();
  }

  /**
   * Checks for a valid function definition.
   */ 
  private void funDef() {
    returnType();
    eat(TokenType.ID, "Expected ID");
    eat(TokenType.LPAREN, "Expected LPAREN");
    params();
    eat(TokenType.RPAREN, "Expected RPAREN");
    block();
  }

  /**
   * Checks for a valid block
   */
  private void block() {
    eat(TokenType.LBRACE, "Expected LBRACE");
    while (!match(TokenType.RBRACE)) {
      stmt();
    }
    // Hit an RBRACE
    advance();
  }

  /**
   * Checks for a valid returnType
   */
  private void returnType() {
    if (match(TokenType.VOID_TYPE)) {
      advance();
    } else {
      dataType();
    }
  }

  /**
   * Checks for valid params
   */
  private void params() {
    if (match(TokenType.ID)) {
      param();
      while (match(TokenType.COMMA)) {
        advance();
        param();
      }
    }
  }

  /**
   * Checks for a valid param
   */
  private void param() {
    eat(TokenType.ID, "Expected ID");
    eat(TokenType.COLON, "Expected COLON");
    dataType();
  }

  /**
   * Checks for a valid dataType
   */
  private void dataType() {
    if (match(TokenType.LBRACKET)) {
      advance();
      if (match(TokenType.ID)) {
        advance();
      } else {
        baseType();
      }
      eat(TokenType.RBRACKET, "Expected RBRACKET");
    } else if (match(TokenType.ID)) {
      advance();
    } else {
      baseType();
    }
  }

  /**
   * Checks for a valid baseType
   */
  private void baseType() {
    if (matchAny(List.of(TokenType.INT_TYPE, TokenType.DOUBLE_TYPE, TokenType.STRING_TYPE, TokenType.BOOL_TYPE))) {
      advance();
    } else {
      error("Expected one of INT_TYPE, DOUBLE_TYPE, STRING_TYPE, BOOL_TYPE");
    }
  }

  /**
   * Checks for a valid stmt
   */
  private void stmt() {
    switch (currToken.tokenType) {
      case TokenType.VAR:
        varStmt();
        break;
      case TokenType.WHILE:
        whileStmt();
        break;
      case TokenType.IF:
        ifStmt();
        break;
      case TokenType.FOR:
        forStmt();
        break;
      case TokenType.RETURN:
        returnStmt();
        break;
      case TokenType.ID:
        advance();
        if (match(TokenType.LPAREN)) {
          funCall();
        } else {
          assignStmt();
        }
        break;
      default:
        error("Expected any of VAR, WHILE, IF, FOR, RETURN, ID");
    }
  }

  /**
   * Checks for a valid varStmt
   */
  private void varStmt() {
    // Var already checked
    advance();
    eat(TokenType.ID, "Expected ID");
    if (match(TokenType.ASSIGN)) {
      varInit();
    } else {
      varType();
      if (match(TokenType.ASSIGN)) {
        varInit();
      }
    }
  }

  /**
   * Checks for a valid varType
   */
  private void varType() {
    eat(TokenType.COLON, "Expected COLON");
    dataType();
  }

  /**
   * Checks for a valid varInit
   */
  private void varInit() {
    // ASSIGN already checked
    advance();
    expr();
  }

  /**
   * Checks for a valid whileStmt
   */
  private void whileStmt() {
    // WHILE already checked
    advance();
    expr();
    block();
  }

  /**
   * Checks for a valid ifStmt
   */
  private void ifStmt() {
    // IF already checked
    advance();
    expr();
    block();
    if (match(TokenType.ELSE)) {
      advance();
      if (match(TokenType.IF)) {
        ifStmt();
      } else {
        block();
      }
    }
  }

  /**
   * Checks for a valid forStmt
   */
  private void forStmt() {
    // FOR already checked
    advance();
    eat(TokenType.ID, "Expected ID");
    eat(TokenType.FROM, "Expected FROM");
    expr();
    eat(TokenType.TO, "Expected TO");
    expr();
    block();
  }

  /**
   * Checks for a valid returnStmt
   */
  private void returnStmt() {
    // RETURN already checked
    advance();
    expr();
  }

  /**
   * Checks for a valid assignStmt
   */
  private void assignStmt() {
    lvalue();
    eat(TokenType.ASSIGN, "Expected ASSIGN");
    expr();
  }

  /**
   * Checks for a valid lvalue
   */
  private void lvalue() {
    // ID already checked and advanced (See stmt())
    if (match(TokenType.LBRACKET)) {
      advance();
      expr();
      eat(TokenType.RBRACKET, "Expected RBRACKET");
    }
    while (match(TokenType.DOT)) {
      advance();
      eat(TokenType.ID, "Expected ID");
      if (match(TokenType.LBRACKET)) {
        advance();
        expr();
        eat(TokenType.RBRACKET, "Expected RBRACKET");
      }
    }
  }

  /**
   * Checks for a valid funCall
   */
  private void funCall() {
    // ID already checked and advanced (See stmt())
    eat(TokenType.LPAREN, "Expected LPAREN");
    args();
    eat(TokenType.RPAREN, "Expected RPAREN");
  }

  /**
   * Checks for a valid args
   */
  private void args() {
    // If there aren't args, there will be an RPAREN
    if  (!match(TokenType.RPAREN)) {
      expr();
    }
    while (match(TokenType.COMMA)) {
      advance();
      expr();
    }
  }

  /**
   * Checks for a valid expr
   */
  private void expr() {
    if (match(TokenType.NOT)) {
      advance();
      expr();
    } else if (match(TokenType.LPAREN)) {
      advance();
      args();
      eat(TokenType.RPAREN, "Expected RPAREN");
    } else {
      rvalue();
    }
    if (isBinOp()) {
      binOp();
      expr();
    }
  }

  /**
   * Checks for a valid binOp
   */
  private void binOp() {
    // isBinOp() already checked
    advance();
  }

  /**
   * Checks for a valid rvalue
   */
  private void rvalue() {
    if (isLiteral()) {
      literal();
    } else if (match(TokenType.NEW)) {
      newRvalue();
    } else if (match(TokenType.ID)) {
      // Must look further ahead
      advance();
      if (match(TokenType.LPAREN)) {
        funCall();
      } else {
        varRvalue();
      }
    } else {
      error("Expected one of LITERAL, NEW, ID");
    }
  }

  /**
   * Checks for a valid newRvalue
   */
  private void newRvalue() {
    // NEW already checked
    advance();
    if (match(TokenType.ID)) {
      advance();
    } else {
      baseType();
    }
    if (match(TokenType.LPAREN)) {
      advance();
      args();
      eat(TokenType.RPAREN, "Expected RPAREN");
    } else {
      eat(TokenType.RBRACKET, " Expected RBRACKET");
      expr();
      eat(TokenType.RBRACKET, "Expected RBRACKET");
    }
  }

  /**
   * Checks for a valid literal
   */
  private void literal() {
    // isLiteral() already checked
    advance();
  }

  /**
   * Checks for a valid varRvalue
   */
  private void varRvalue() {
    // ID already checked and advanced
    if (match(TokenType.LBRACKET)) {
      advance();
      expr();
      eat(TokenType.RBRACKET, "Expected RBRACKET");
    }
    while (match(TokenType.DOT)) {
      advance();
      eat(TokenType.ID, "Expected ID");
      if (match(TokenType.LBRACKET)) {
        advance();
        expr();
        eat(TokenType.RBRACKET, "Expected RBRACKET");
      }
    }
  }
}
