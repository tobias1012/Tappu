package tappu.util

import scala.io.Source
import scala.util.control.Breaks._
import scala.util.parsing.combinator._


object Opcode extends Enumeration {
  type Type = Int
  val Right     = 0x00
  val Left      = 0x10
  val Add       = 0x20
  val Sub       = 0x30
  val Set       = 0x40
  val Print     = 0x50
  val Read      = 0x60
  val LoopStart = 0x70
  val LoopEnd   = 0x80
  val AccStore  = 0x90
  val AccLoad   = 0xA0
}




class Instruction(opcodeVal: Opcode.Type, dataVal: Int) {
  val opcode = opcodeVal
  val data = dataVal
}

class AssemblerParser extends RegexParsers {
  def comment: Parser[String] = """//.*""".r ^^ { _.toString }
  def opcode: Parser[Opcode.Type] =(  ">" ^^ { _=> Opcode.Right } //Right 
                                        | "<" ^^ { _ => Opcode.Left } //Left
                                        | "+" ^^ { _ => Opcode.Add } //Add
                                        | "-" ^^ { _ => Opcode.Sub } //Sub
                                        | ":" ^^ { _ => Opcode.Set } //Set
                                        | "." ^^ { _ => Opcode.Print } //Print
                                        | "," ^^ { _ => Opcode.Read } //Read
                                        | "[" ^^ { _ => Opcode.LoopStart } //Loop start
                                        | "]" ^^ { _ => Opcode.LoopEnd } //Loop end
                                        | "s" ^^ { _ => Opcode.AccStore} //Accumulator Store
                                        | "l" ^^ { _ => Opcode.AccLoad} //Accumulator Load
                                        )

  def hexNumber: Parser[Int] = """0x[0-9A-Fa-f]{1,2}""".r ^^ { s => Integer.parseInt(s.drop(2), 16) }
  def number: Parser[Int] = hexNumber | """\d{1,3}""".r ^^ { _.toInt }
  
  def instruction: Parser[Instruction] = opcode ~ opt(number) ^^ { case o ~ d => new Instruction(o, d.getOrElse(1)) }


  def program: Parser[List[Instruction]] = opt(rep(comment)) ~ phrase(rep(instruction ~ opt(comment) ^^ { case  i ~ c2 => i })) ^^ { case _ ~ l => l }
}

object Assembler extends  AssemblerParser{
  def asm(asm: String): Seq[Instruction] = {
    val source = Source.fromFile(asm)    
    
    val content = source.mkString

    println(content)
    var result = parse(program, content) 

    result match {
      case Success(result, _) => {
        println(result.flatMap(ins => List(ins.opcode, ins.data)))
        return result
      }
      case NoSuccess(msg, _) => throw new Exception("Parsing failed: " + msg)
      case _ => throw new Exception("Parsing failed")
    }
  }
}