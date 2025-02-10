package tappu.util

import scala.io.Source
import scala.util.control.Breaks._
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.combinator._


class AssemblerParser extends RegexParsers {
  def comment: Parser[String] = """//.*""".r ^^ { _.toString }
  def instruction: Parser[Int] =(  ">" ^^ { _=> 0 } //Right 
                                | "<" ^^ { _ => 1 } //Left
                                | "+" ^^ { _ => 2 } //Add
                                | "-" ^^ { _ => 3 } //Sub
                                | ":" ^^ { _ => 3 } //Set
                                | "." ^^ { _ => 4 } //Print
                                | "," ^^ { _ => 5 } //Read
                                | "[" ^^ { _ => 6 } //Loop start
                                | "]" ^^ { _ => 7 } //Loop end
                                | "s" ^^ { _ => 10} //Accumulator Store
                                | "l" ^^ { _ => 11} //Accumulator Load
                                )
  def program: Parser[List[Int]] = (comment ~ program ^^ { case c ~ p => p }
                                   | rep(instruction) ^^ {case i => i }
                                   )
}

object Assembler extends  AssemblerParser{
  def asm(asm: String): Seq[Int] = {
    val source = Source.fromFile(asm)    
    
    val content = source.mkString

    println(content)
    var result = parse(program, content) 

    result match {
      case Success(result, _) => {
        println(result)
        return result
      }
      case NoSuccess(msg, _) => throw new Exception("Parsing failed: " + msg)
      case _ => throw new Exception("Parsing failed")
    }
  }
}