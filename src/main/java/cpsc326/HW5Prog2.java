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

    // create the vm: 
    VM vm = new VM();
    // add the frame: 
    vm.add(m);
    // run the vm: 
    vm.run();
  }
}
