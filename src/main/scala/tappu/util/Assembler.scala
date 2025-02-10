package tappu.util

import scala.io.Source
import scala.util.control.Breaks._
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.combinator._


class AssemblerParser extends RegexParsers {
  def comment: Parser[String] = """//.*""".r ^^ { _.toString }
  def instruction: Parser[Int] =  ">" ^^ { _=> 0 } 
                                | "<" ^^ { _ => 1 } 
                                | "+" ^^ { _ => 2 } 
                                | "-" ^^ { _ => 3 } 
                                | "." ^^ { _ => 4 } 
                                | "," ^^ { _ => 5 } 
                                | "[" ^^ { _ => 6 } 
                                | "]" ^^ { _ => 7 } 
                                | "p" ^^ { _ => 8}
                                | "q" ^^ { _ => 9}
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