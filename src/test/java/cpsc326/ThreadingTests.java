package cpsc326;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

public class ThreadingTests {

  /** For dealing with program output **/
  private PrintStream stdout = System.out;
  private ByteArrayOutputStream output = new ByteArrayOutputStream();

  /*@BeforeEach
  public void changeSystemOut() {
    // redirect System.out to output
    System.setOut(new PrintStream(output));
  }

  @AfterEach
  public void restoreSystemOut() {
    // reset System.out to standard out
    System.setOut(stdout);
  }*/

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

  /**
   * Helper to generate a VM to run
   */
  VM build(String program) {
    Lexer lexer = new Lexer(istream(program));
    ASTParser parser = new ASTParser(lexer);
    Program p = parser.parse();
    p.accept(new SemanticChecker());
    VM vm = new VM();
    p.accept(new CodeGenerator(vm));
    return vm;
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
              println(thread_wait(tid1))
              println(thread_wait(tid2))
            }
            """;
    build(p).run();
  }
}
