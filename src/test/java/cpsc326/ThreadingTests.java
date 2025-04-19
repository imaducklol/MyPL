package cpsc326;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ThreadingTests {

  /**
   * Helper to build an input string.
   */
  InputStream istream(String str) {
    try {
      return new ByteArrayInputStream(str.getBytes("UTF-8"));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Test
  void functionDecl() {
    String p = """
            struct input {
              x: int,
              y: bool,
              z: int
            }
            int test(input: input) {
              println(input.x)
              println(input.y)
              return input.z
            }
            void main() {
              var i1 = new input(1, true, 3)
              var i2 = new input(2, false, 4)
              var tid1 = thread_create("test", i1)
              var tid2 = thread_create("test", i2)
              println(thread_wait(tid2))
              println(thread_wait(tid1))
            }
            """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }
}
