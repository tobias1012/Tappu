package tappu

import tappu.util._
import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter

object Compiler {
  def pad(s: String, n: Int): String = {
    if (s.length < n) {
      pad("0" + s, n)
    } else {
      s
    }
  }

  def compile(programPath: String, outPath: String, memorySize: Int): String = {
    var program = Assembler.asm(programPath).flatMap( i => List(i.opcode, i.data).map(d => "0x" + pad(d.toHexString,2) + "\n"))
    val outFile = new File(outPath)

    //add initialization zeroing out the memory
    val memorySize = 512
    //add the buffer for pc at the start
    program = Array.fill(2)("0x00\n").toIndexedSeq ++ program
    //Initialize the rest of the memory with zeros
    program = program ++ Array.fill(memorySize - program.length -2)("0x00\n")
    //program = program.reverse
    val writer = new BufferedWriter(new FileWriter(outFile))
    for (i <- 0 until program.length) {
      writer.write(program(i))
    }
    writer.close()
    outFile.getAbsolutePath()
  }

  def main(args: Array[String]): Unit = {
    compile(args(0), args(1), 512)
  }
}