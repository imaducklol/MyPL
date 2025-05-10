/**
 * CPSC 326, Spring 2025
 * Example program 2 for HW-5.
 */

package cpsc326;

/**
 * Class for HW5 program 2.
 */
public class HW5Prog2 {

  // Implement the following MyPL program: 
  // 
  // struct Team {
  //   name: string, 
  //   wins: int, 
  //   games: int
  // }
  // 
  // void main() {
  //   var teams: Team = new Team[2]
  //   teams[0] = new Team("a", 10, 20);
  //   teams[1] = new Team("b", 18, 20);
  //   double sum = 0.0;
  //   sum = sum + (dbl_val(teams[0].wins) / dbl_val(teams[0].games))
  //   sum = sum + (dbl_val(teams[1].wins) / dbl_val(teams[1].games))
  //   print("The average win percentage is: ")
  //   print(sum / 2)
  //   println("")
  // }

  public static void main(String[] args) {
    VMFrameTemplate m = new VMFrameTemplate("main");

    // TODO: Add the instructions to the main (m) template to
    //       implement the above main function.

    // create struct array and store it to 0
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.ALLOCA());
    m.add(VMInstr.STORE(0));

    // set up array assignment
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH(0));

    // create first struct
    m.add(VMInstr.ALLOCS());
    m.add(VMInstr.DUP());
    m.add(VMInstr.PUSH("a"));
    m.add(VMInstr.SETF("name"));
    m.add(VMInstr.DUP());
    m.add(VMInstr.PUSH(10));
    m.add(VMInstr.SETF("wins"));
    m.add(VMInstr.DUP());
    m.add(VMInstr.PUSH(20));
    m.add(VMInstr.SETF("games"));

    // assign first struct
    m.add(VMInstr.SETI());

    // set up array assignment
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH(1));

    // create second struct
    m.add(VMInstr.ALLOCS());
    m.add(VMInstr.DUP());
    m.add(VMInstr.PUSH("B"));
    m.add(VMInstr.SETF("name"));
    m.add(VMInstr.DUP());
    m.add(VMInstr.PUSH(18));
    m.add(VMInstr.SETF("wins"));
    m.add(VMInstr.DUP());
    m.add(VMInstr.PUSH(20));
    m.add(VMInstr.SETF("games"));

    // assign second struct
    m.add(VMInstr.SETI());

    // sum assignment
    m.add(VMInstr.PUSH(0.0));
    m.add(VMInstr.STORE(1));

    // first math
    m.add(VMInstr.LOAD(1));

    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH(0));
    m.add(VMInstr.GETI());
    m.add(VMInstr.GETF("wins"));
    m.add(VMInstr.TODBL());
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH(0));
    m.add(VMInstr.GETI());
    m.add(VMInstr.GETF("games"));
    m.add(VMInstr.TODBL());
    m.add(VMInstr.DIV());

    m.add(VMInstr.ADD());
    m.add(VMInstr.STORE(1));

    // second math
    m.add(VMInstr.LOAD(1));

    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.GETI());
    m.add(VMInstr.GETF("wins"));
    m.add(VMInstr.TODBL());
    m.add(VMInstr.LOAD(0));
    m.add(VMInstr.PUSH(1));
    m.add(VMInstr.GETI());
    m.add(VMInstr.GETF("games"));
    m.add(VMInstr.TODBL());

    m.add(VMInstr.DIV());

    m.add(VMInstr.ADD());
    m.add(VMInstr.STORE(1));

    m.add(VMInstr.PUSH("The average win percentage is: "));
    m.add(VMInstr.WRITE());

    m.add(VMInstr.LOAD(1));
    m.add(VMInstr.PUSH(2));
    m.add(VMInstr.TODBL());
    m.add(VMInstr.DIV());
    m.add(VMInstr.WRITE());

    m.add(VMInstr.PUSH("\n"));
    m.add(VMInstr.WRITE());

    // create the vm: 
    VM vm = new VM();
    // add the frame: 
    vm.add(m);
    // run the vm: 
    vm.run();
  }
}
