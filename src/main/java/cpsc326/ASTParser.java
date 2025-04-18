/**
 * CPSC 326, Spring 2025
 * The AST Parser implementation.
 * <p>
 * Orion Hess
 */

package cpsc326;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Simple recursive descent parser for checking program syntax.
 */
public class ASTParser {

  private final Lexer lexer;          // the lexer
  private Token currToken;      // the current token

  /**
   * Create a SimpleParser from the give lexer.
   *
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
   *
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
   *
   * @param targetTokenType The token type to check against.
   * @return True if the types match, false otherwise.
   */
  private boolean match(TokenType targetTokenType) {
    return currToken.tokenType == targetTokenType;
  }

  /**
   * Checks that the current token is contained in the given list of
   * token types.
   *
   * @param targetTokenTypes The token types ot check against.
   * @return True if the current type is in the given list, false
   * otherwise.
   */
  private boolean matchAny(List<TokenType> targetTokenTypes) {
    return targetTokenTypes.contains(currToken.tokenType);
  }

  /**
   * Advance to next token if current token matches the given token type.
   *
   * @param targetTokenType The token type to check against.
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
   *
   * @return the corresponding Program AST object
   */
  private Program program() {
    Program program = new Program();
    while (!match(TokenType.EOS)) {
      // Struct definition
      if (match(TokenType.STRUCT)) {
        program.structs.add(structDef());
      }
      // Function definition
      else {
        program.functions.add(funDef());
      }
    }
    return program;
  }

  /**
   * Parse structs
   *
   * @return the corresponding Struct AST object
   */
  private StructDef structDef() {
    StructDef structDef = new StructDef();
    structDef.fields = new ArrayList<>();
    // First already checked
    advance();
    if (match(TokenType.ID)) {
      structDef.structName = currToken;
      advance();
    } else {
      error("Expected ID");
    }
    eat(TokenType.LBRACE, "Expected LBRACE");
    if (match(TokenType.ID)) {
      structDef.fields.add(varDef());
      while (match(TokenType.COMMA)) {
        advance();
        structDef.fields.add(varDef());
      }
    }
    eat(TokenType.RBRACE, "Expected RBRACE");
    return structDef;
  }

  /**
   * Parse variable definitions
   *
   * @return the corresponding VarDef AST object
   */
  private VarDef varDef() {
    VarDef varDef = new VarDef();
    // ID already checked
    varDef.varName = currToken;
    advance();
    eat(TokenType.COLON, "Expected COLON");
    varDef.dataType = dataType();
    return varDef;
  }

  /**
   * Parse function definitions
   *
   * @return the corresponding FunDef AST object
   */
  private FunDef funDef() {
    FunDef funDef = new FunDef();
    funDef.returnType = dataType();
    if (match(TokenType.ID)) {
      funDef.funName = currToken;
      advance();
    } else {
      error("Expected ID");
    }
    eat(TokenType.LPAREN, "Expected LPAREN");
    funDef.params = params();
    eat(TokenType.RPAREN, "Expected RPAREN");
    funDef.stmts = stmts();
    return funDef;
  }

  /**
   * Parse statements
   *
   * @return a list of Stmt AST Objects
   */
  private List<Stmt> stmts() {
    List<Stmt> stmts = new ArrayList<>();
    eat(TokenType.LBRACE, "Expected LBRACE");
    while (!match(TokenType.RBRACE)) {
      stmts.add(stmt());
    }
    // Hit an RBRACE
    advance();
    return stmts;
  }

  /**
   * Parse parameters
   *
   * @return a list of VarDef AST objects
   */
  private List<VarDef> params() {
    List<VarDef> params = new ArrayList<>();
    if (match(TokenType.ID)) {
      params.add(param());
      while (match(TokenType.COMMA)) {
        advance();
        params.add(param());
      }
    }
    return params;
  }

  /**
   * Parse individual parameters
   *
   * @return a VarDef AST object for the parameter
   */
  private VarDef param() {
    VarDef varDef = new VarDef();
    if (match(TokenType.ID)) {
      varDef.varName = currToken;
      advance();
    } else {
      error("Expected ID");
    }
    eat(TokenType.COLON, "Expected COLON");
    varDef.dataType = dataType();
    return varDef;
  }

  /**
   * Parse data types
   *
   * @return the corresponding DataType AST object
   */
  private DataType dataType() {
    DataType dataType = new DataType();
    if (match(TokenType.LBRACKET)) {
      advance();
      dataType.isArray = true;
      if (match(TokenType.ID)) {
        dataType.type = currToken;
        advance();
      } else {
        dataType.type = baseType();
      }
      eat(TokenType.RBRACKET, "Expected RBRACKET for array");
    } else if (match(TokenType.ID)) {
      dataType.type = currToken;
      advance();
    } else {
      dataType.type = baseType();
    }
    return dataType;
  }

  /**
   * Parse base types
   *
   * @return the Token of the type
   */
  private Token baseType() {
    Token token = null;
    if (matchAny(List.of(TokenType.INT_TYPE, TokenType.DOUBLE_TYPE, TokenType.STRING_TYPE, TokenType.BOOL_TYPE, TokenType.VOID_TYPE))) {
      token = currToken;
      advance();
    } else {
      error("Expected one of INT_TYPE, DOUBLE_TYPE, STRING_TYPE, BOOL_TYPE, VOID_TYPE");
    }
    return token;
  }

  /**
   * Parse statements
   *
   * @return the corresponding VarDef AST object
   */
  private Stmt stmt() {
    switch (currToken.tokenType) {
      case TokenType.VAR:
        return varStmt();
      case TokenType.WHILE:
        return whileStmt();
      case TokenType.IF:
        return ifStmt();
      case TokenType.FOR:
        return forStmt();
      case TokenType.RETURN:
        return returnStmt();
      case TokenType.ID:
        Token idToken = currToken;
        advance();
        if (match(TokenType.LPAREN)) {
          return funCall(idToken);
        } else {
          return assignStmt(idToken);
        }
      default:
        error("Expected statement");
        return null;
    }
  }

  /**
   * Parse variable statements
   *
   * @return the corresponding VarStmt AST object
   */
  private VarStmt varStmt() {
    VarStmt varStmt = new VarStmt();
    // Var already checked
    advance();
    if (match(TokenType.ID)) {
      varStmt.varName = currToken;
      advance();
    } else {
      error("Expected ID");
    }
    if (match(TokenType.ASSIGN)) {
      varStmt.expr = Optional.of(varInit());
    } else {
      varStmt.dataType = Optional.of(varType());

      if (match(TokenType.ASSIGN)) {
        varStmt.expr = Optional.of(varInit());
      } else {
        varStmt.expr = Optional.empty();
      }
    }
    return varStmt;
  }

  /**
   * Parse variable type
   *
   * @return the corresponding DataType AST object
   */
  private DataType varType() {
    eat(TokenType.COLON, "Expected COLON");
    return dataType();
  }

  /**
   * Parse variable initialization
   *
   * @return the corresponding Expr AST object
   */
  private Expr varInit() {
    // ASSIGN already checked
    advance();
    return expr();
  }

  /**
   * Parse while statement
   *
   * @return the corresponding WhileStmt AST object
   */
  private WhileStmt whileStmt() {
    WhileStmt whileStmt = new WhileStmt();
    // WHILE already checked
    advance();
    whileStmt.condition = expr();
    whileStmt.stmts = stmts();
    return whileStmt;
  }

  /**
   * Parse if statements
   *
   * @return the corresponding IfStmt AST object
   */
  private IfStmt ifStmt() {
    IfStmt ifStmt = new IfStmt();
    // IF already checked
    advance();
    ifStmt.condition = expr();
    ifStmt.ifStmts = stmts();
    if (match(TokenType.ELSE)) {
      advance();
      if (match(TokenType.IF)) {
        ifStmt.elseIf = Optional.of(ifStmt());
      } else {
        ifStmt.elseStmts = Optional.of(stmts());
      }
    }
    return ifStmt;
  }

  /**
   * Parse for statements
   *
   * @return the corresponding ForStmt AST object
   */
  private ForStmt forStmt() {
    ForStmt forStmt = new ForStmt();
    // FOR already checked
    advance();
    if (match(TokenType.ID)) {
      forStmt.varName = currToken;
      advance();
    } else {
      error("Expected ID");
    }
    eat(TokenType.FROM, "Expected FROM");
    forStmt.fromExpr = expr();
    eat(TokenType.TO, "Expected TO");
    forStmt.toExpr = expr();
    forStmt.stmts = stmts();
    return forStmt;
  }

  /**
   * Parse return statements
   *
   * @return the corresponding ReturnStmt AST object
   */
  private ReturnStmt returnStmt() {
    ReturnStmt returnStmt = new ReturnStmt();
    // RETURN already checked
    advance();
    returnStmt.expr = expr();
    return returnStmt;
  }

  /**
   * Parse assign statements
   *
   * @return the corresponding AssignStmt AST object
   */
  private AssignStmt assignStmt(Token idToken) {
    AssignStmt assignStmt = new AssignStmt();
    assignStmt.lvalue = lvalue(idToken);
    eat(TokenType.ASSIGN, "Expected ASSIGN");
    assignStmt.expr = expr();
    return assignStmt;
  }

  /**
   * Parse left value for assign statements
   *
   * @return a list of VarDef AST objects
   */
  private List<VarRef> lvalue(Token idToken) {
    List<VarRef> lvalue = new ArrayList<>();
    VarRef varRef = new VarRef();
    varRef.varName = idToken;
    // ID already checked and advanced (See stmt())
    if (match(TokenType.LBRACKET)) {
      advance();
      varRef.arrayExpr = Optional.of(expr());
      eat(TokenType.RBRACKET, "Expected RBRACKET for lvalue");
    }
    lvalue.add(varRef);
    while (match(TokenType.DOT)) {
      advance();
      varRef = new VarRef();
      if (match(TokenType.ID)) {
        varRef.varName = currToken;
        advance();
      } else {
        error("Expected ID");
      }
      if (match(TokenType.LBRACKET)) {
        advance();
        varRef.arrayExpr = Optional.of(expr());
        eat(TokenType.RBRACKET, "Expected RBRACKET for lvalue");
      }
      lvalue.add(varRef);
    }
    return lvalue;
  }

  /**
   * Parse function call
   *
   * @return the corresponding CallRValue AST object
   */
  private CallRValue funCall(Token idToken) {
    CallRValue funCall = new CallRValue();
    funCall.funName = idToken;
    // ID already checked and advanced (See stmt())
    eat(TokenType.LPAREN, "Expected LPAREN");
    funCall.args = args();
    eat(TokenType.RPAREN, "Expected RPAREN");
    return funCall;
  }

  /**
   * Parse arguments for a function call
   *
   * @return a list of the corresponding Expr AST objects
   */
  private List<Expr> args() {
    List<Expr> args = new ArrayList<>();
    // If there aren't args, there will be an RPAREN
    if (!match(TokenType.RPAREN)) {
      args.add(expr());
    }
    while (match(TokenType.COMMA)) {
      advance();
      args.add(expr());
    }
    return args;
  }

  /**
   * Parse expressions
   *
   * @return the corresponding Expr AST object
   */
  private Expr expr() {
    Expr expr;
    if (match(TokenType.NOT)) {
      UnaryExpr unaryExpr = new UnaryExpr();
      unaryExpr.unaryOp = currToken;
      advance();
      unaryExpr.expr = expr();
      expr = unaryExpr;
    } else if (match(TokenType.LPAREN)) {
      advance();
      expr = expr();
      eat(TokenType.RPAREN, "Expected RPAREN");
    } else {
      BasicExpr basicExpr = new BasicExpr();
      basicExpr.rvalue = rvalue();
      expr = basicExpr;
    }
    if (isBinOp()) {
      BinaryExpr binaryExpr = new BinaryExpr();
      binaryExpr.lhs = expr;
      binaryExpr.binaryOp = binOp();
      binaryExpr.rhs = expr();
      expr = binaryExpr;
    }
    return expr;
  }

  /**
   * Parse a binary operator
   *
   * @return the corresponding Token
   */
  private Token binOp() {
    // isBinOp() already checked
    Token token = currToken;
    advance();
    return token;
  }

  /**
   * Parse right values
   *
   * @return the corresponding RValue AST object
   */
  private RValue rvalue() {
    if (isLiteral()) {
      return literal();
    } else if (match(TokenType.NEW)) {
      return newRvalue();
    } else if (match(TokenType.ID)) {
      Token idToken = currToken;
      // Must look further ahead
      advance();
      if (match(TokenType.LPAREN)) {
        return funCall(idToken);
      } else {
        return varRvalue(idToken);
      }
    }
    error("Expected one of LITERAL, NEW, ID");
    return null;
  }

  /**
   * Parse new right value
   *
   * @return the corresponding NewRValue AST object
   */
  private NewRValue newRvalue() {
    NewRValue newRValue;
    Token token = null;
    // NEW already checked
    advance();
    if (match(TokenType.ID)) {
      token = currToken;
      advance();
    } else {
      token = baseType();
    }
    if (match(TokenType.LPAREN)) {
      advance();
      NewStructRValue newStructRValue = new NewStructRValue();
      newStructRValue.structName = token;
      newStructRValue.args = args();
      newRValue = newStructRValue;
      eat(TokenType.RPAREN, "Expected RPAREN");
    } else {
      eat(TokenType.LBRACKET, " Expected RBRACKET");
      NewArrayRValue newArrayRValue = new NewArrayRValue();
      newArrayRValue.type = token;
      newArrayRValue.arrayExpr = expr();
      newRValue = newArrayRValue;
      eat(TokenType.RBRACKET, "Expected RBRACKET");
    }
    return newRValue;
  }

  /**
   * Parse literal
   *
   * @return the corresponding SimpleRValue AST object
   */
  private SimpleRValue literal() {
    SimpleRValue literal = new SimpleRValue();
    literal.literal = currToken;
    // isLiteral() already checked
    advance();
    return literal;
  }

  /**
   * Parse variable
   *
   * @return the corresponding VarRValue AST object
   */
  private VarRValue varRvalue(Token idToken) {
    VarRValue varRValue = new VarRValue();
    VarRef varRef = new VarRef();
    varRef.varName = idToken;
    // ID already checked and advanced
    if (match(TokenType.LBRACKET)) {
      advance();
      varRef.arrayExpr = Optional.of(expr());
      eat(TokenType.RBRACKET, "Expected RBRACKET for varRvalue");
    }
    varRValue.path.add(varRef);

    while (match(TokenType.DOT)) {
      advance();
      varRef = new VarRef();
      if (match(TokenType.ID)) {
        varRef.varName = currToken;
        advance();
      } else {
        error("Expected ID");
      }
      if (match(TokenType.LBRACKET)) {
        advance();
        varRef.arrayExpr = Optional.of(expr());
        eat(TokenType.RBRACKET, "Expected RBRACKET for varRvalue");
      }
      varRValue.path.add(varRef);
    }
    return varRValue;
  }

}
