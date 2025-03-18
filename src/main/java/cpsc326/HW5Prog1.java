/**
 * CPSC 326, Spring 2025
 * Example program 1 for HW-5.
 */

package cpsc326;

/**
 * Class for HW5 program 1.
 */
public class HW5Prog1 {

  // Implement the following MyPL program: 
  // 
  // bool is_prime(n: int) {
  //   var m: int = n / 2
  //   var v: int = 2
  //   while v <= m {
  //     var r: int = n / v
  //     var p: int = r * v
  //     if p == n {
  //       return false
  //     }
  //     v = v + 1
  //   }
  //   return true
  // }
  //
  // void main() {
  //   println("Please enter integer values to sum (prime to quit)")
  //   var sum: int = 0
  //   while true {
  //     print("Enter an int: ")
  //     var val: int = int_val(readln())
  //     if is_prime(val) {
  //       println("The sum is: " + str_val(sum))
  //       println("Goodbye!")
  //       return null
  //     }
  //     sum = sum + val
  //   }
  // }
  
  public static void main(String[] args) {
    VMFrameTemplate m = new VMFrameTemplate("main");
    VMFrameTemplate p = new VMFrameTemplate("is_prime");

    // TODO: Add the instructions for main (m) and is_prime (p)
    //       templates to implement the corresponding main and
    //       is_prime functions above.
    

    // create the vm: 
    VM vm = new VM();
    // add the frames to the vm:
    vm.add(m);
    vm.add(p);
    // run the program: 
    vm.run();
  }
}
