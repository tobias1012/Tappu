package tappu.util

import scala.io.Source
import scala.util.control.Breaks._

object Assembler {
  def asm(asm: String): Seq[Int] = {
    val source = Source.fromFile(asm)
    var program = List[Int]()
    var pc = 0
    
    for (line <- source.getLines()) {

        println(line)

        val parts = line.split("")
        breakable {
        for ( part <- parts ){
            val instr = part match {
                case "/" => break() // comment
                case ">" => 0
                case "<" => 1
                case "+" => 2
                case "-" => 3
                case "." => 4
                case "," => 5
                case "[" => 6
                case "]" => 7
                case _ => throw new Exception(s"Unknown instruction: ${parts(0)}")
                }

           
            instr match {
              case (i: Int) => {
                program = i :: program
                pc += 1
              }
            }
            
        }
      }
    }
    println("The Program:")
    program.foreach(printf("0x%02x ",_))
    println()
    program
  }
}