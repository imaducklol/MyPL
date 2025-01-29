/**
 * CPSC 326, Spring 2025
 * MyPL Lexer Implementation.
 * <p>
 * PUT YOUR NAME HERE IN PLACE OF THIS TEXT
 */

package cpsc326;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;


/**
 * The Lexer class takes an input stream containing mypl source code
 * and transforms (tokenizes) it into a stream of tokens.
 */
public class Lexer {

    private BufferedReader buffer; // handle to the input stream
    private int line = 1;          // current line number
    private int column = 0;        // current column number

    /**
     * Creates a new Lexer object out of an input stream.
     */
    public Lexer(InputStream input) {
        buffer = new BufferedReader(new InputStreamReader(input));
    }

    /**
     * Helper function to read a single character from the input stream.
     *
     * @return A single character
     */
    private char read() {
        try {
            ++column;
            return (char) buffer.read();
        } catch (IOException e) {
            error("read error", line, column + 1);
        }
        return (char) -1;
    }

    /**
     * Helper function to look ahead one character in the input stream.
     *
     * @return A single character
     */
    private char peek() {
        int ch = -1;
        try {
            buffer.mark(1);
            ch = (char) buffer.read();
            buffer.reset();
            return (char) ch;
        } catch (IOException e) {
            error("read error", line, column + 1);
        }
        return (char) -1;
    }

    /**
     * Helper function to check if the given character is an end of line
     * symbol.
     *
     * @return True if the character is an end of line character and
     * false otherwise.
     */
    private boolean isEOL(char ch) {
        if (ch == '\n') return true;
        if (ch == '\r' && peek() == '\n') {
            read();
            return true;
        } else if (ch == '\r') return true;
        return false;
    }

    /**
     * Helper function to check if the given character is an end of file
     * symbol.
     *
     * @return True if the character is an end of file character and
     * false otherwise.
     */
    private boolean isEOF(char ch) {
        return ch == (char) -1;
    }

    /**
     * Print an error message and exit the program.
     */
    private void error(String msg, int line, int column) {
        String s = "[%d,%d] %s";
        MyPLException.lexerError(String.format(s, line, column, msg));
    }

    /**
     * Obtains and returns the next token in the stream.
     *
     * @return The next token in the stream.
     */
    public Token nextToken() {
        // read the initial character
        char ch = read();

        // Get through whitespace, checking for EOLs
        while (Character.isWhitespace(ch)) {
            if (isEOL(ch)) {
                column = 0;
                line++;
            }
            ch = read();
        }

        // Check EOF
        if (isEOF(ch)) {
            return new Token(TokenType.EOS, "end-of-stream", line, column);
        }

        // Check single character tokens
        switch (ch) {
            // Punctuation Symbols
            case '.':
                return new Token(TokenType.DOT, ".", line, column);
            case ':':
                return new Token(TokenType.COLON, ":", line, column);
            case ',':
                return new Token(TokenType.COMMA, ",", line, column);
            case '(':
                return new Token(TokenType.LPAREN, "(", line, column);
            case ')':
                return new Token(TokenType.RPAREN, ")", line, column);
            case '[':
                return new Token(TokenType.LBRACKET, "[", line, column);
            case ']':
                return new Token(TokenType.RBRACKET, "]", line, column);
            case '{':
                return new Token(TokenType.LBRACE, "{", line, column);
            case '}':
                return new Token(TokenType.RBRACE, "}", line, column);

            // Arithmetic Operators
            case '+':
                return new Token(TokenType.PLUS, "+", line, column);
            case '-':
                return new Token(TokenType.MINUS, "-", line, column);
            case '*':
                return new Token(TokenType.TIMES, "*", line, column);
            case '/':
                return new Token(TokenType.DIVIDE, "/", line, column);
            default:
        }

        // Two character tokens
        switch (ch) {
            case '=':
                if (peek() == '=') {
                    read();
                    return new Token(TokenType.EQUAL, "==", line, column - 1);
                } else {
                    return new Token(TokenType.ASSIGN, "=", line, column);
                }
            case '<':
                if (peek() == '=') {
                    read();
                    return new Token(TokenType.LESS_EQ, "<=", line, column - 1);
                } else {
                    return new Token(TokenType.LESS, "<", line, column);
                }
            case '>':
                if (peek() == '=') {
                    read();
                    return new Token(TokenType.GREATER_EQ, ">=", line, column - 1);
                } else {
                    return new Token(TokenType.GREATER, ">", line, column);
                }
            case '!':
                if (peek() == '=') {
                    read();
                    return new Token(TokenType.NOT_EQUAL, "!=", line, column - 1);
                } else {
                    error("expecting !=", line, column);
                }
            default:
        }

        // Strings
        if (ch == '"') {
            StringBuilder string = new StringBuilder();
            ch = read();
            do {
                if (isEOL(ch)) error("non-terminated string", line, column);
                if (isEOF(ch)) error("non-terminated string", line, column);
                string.append(ch);
                ch = read();
            } while (ch != '"');
            return new Token(TokenType.STRING_VAL, string.toString(), line, column - string.length() - 1);
        }

        // Numbers
        if (Character.isDigit(ch)) {
            if (ch == '0' && Character.isDigit(peek())) {
                error("leading zero in number", line, column);
            }
            boolean isInteger = true;
            StringBuilder number = new StringBuilder();
            number.append(ch);

            while (true) {
                if (Character.isDigit(peek())) {
                    ch = read();
                    number.append(ch);
                } else if (peek() == '.' && isInteger) {
                    isInteger = false;
                    ch = read();
                    number.append(ch);
                    if (!Character.isDigit(peek())) {
                        error("missing digit after decimal", line, column + 1);
                    }
                } else {
                    return isInteger
                            ? new Token(TokenType.INT_VAL, number.toString(), line, column - number.length() + 1)
                            : new Token(TokenType.DOUBLE_VAL, number.toString(), line, column - number.length() + 1);
                }
            }
        }

        // Comments
        if (ch == '#') {
            StringBuilder comment = new StringBuilder();
            do {
                ch = read();
                comment.append(ch);
            } while (!isEOL(peek()) && !isEOF(peek()));

            return new Token(TokenType.COMMENT, comment.toString(), line, column - comment.length());
        }

        // Reserved words / Identifiers
        if (Character.isAlphabetic(ch)) {
            StringBuilder word = new StringBuilder();
            word.append(ch);
            while (Character.isAlphabetic(peek()) || Character.isDigit(peek()) || peek() == '_') {
                ch = read();
                word.append(ch);
            }

            return switch (word.toString()) {
                // Fancy values
                case "true", "false" -> new Token(TokenType.BOOL_VAL, word.toString(), line, column - word.length() + 1);
                case "null" -> new Token(TokenType.NULL_VAL, word.toString(), line, column - word.length() + 1);

                // Boolean operators
                case "and" -> new Token(TokenType.AND, word.toString(), line, column - word.length() + 1);
                case "or" -> new Token(TokenType.OR, word.toString(), line, column - word.length() + 1);
                case "not" -> new Token(TokenType.NOT, word.toString(), line, column - word.length() + 1);

                // Types
                case "int" -> new Token(TokenType.INT_TYPE, word.toString(), line, column - word.length() + 1);
                case "double" -> new Token(TokenType.DOUBLE_TYPE, word.toString(), line, column - word.length() + 1);
                case "string" -> new Token(TokenType.STRING_TYPE, word.toString(), line, column - word.length() + 1);
                case "bool" -> new Token(TokenType.BOOL_TYPE, word.toString(), line, column - word.length() + 1);
                case "void" -> new Token(TokenType.VOID_TYPE, word.toString(), line, column - word.length() + 1);

                // Reserved words
                case "struct" -> new Token(TokenType.STRUCT, word.toString(), line, column - word.length() + 1);
                case "var" -> new Token(TokenType.VAR, word.toString(), line, column - word.length() + 1);
                case "while" -> new Token(TokenType.WHILE, word.toString(), line, column - word.length() + 1);
                case "for" -> new Token(TokenType.FOR, word.toString(), line, column - word.length() + 1);
                case "from" -> new Token(TokenType.FROM, word.toString(), line, column - word.length() + 1);
                case "to" -> new Token(TokenType.TO, word.toString(), line, column - word.length() + 1);
                case "if" -> new Token(TokenType.IF, word.toString(), line, column - word.length() + 1);
                case "else" -> new Token(TokenType.ELSE, word.toString(), line, column - word.length() + 1);
                case "new" -> new Token(TokenType.NEW, word.toString(), line, column - word.length() + 1);
                case "return" -> new Token(TokenType.RETURN, word.toString(), line, column - word.length() + 1);

                default -> new Token(TokenType.ID, word.toString(), line, column - word.length() + 1);
            };
        }

        error("unrecognized symbol '" + ch + "'", line, column);

        return null;
    }

}
