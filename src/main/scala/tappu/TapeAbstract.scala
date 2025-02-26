package tappu

import chisel3._
import chisel3.util._
//Initialize memory with experimental feature
import chisel3.util.experimental.loadMemoryFromFile


import tappu.util._
import tappu.Compiler.compile

object WrMode extends ChiselEnum {
  val none, plus, minus, set = Value
}

class TapeAbstract(programPath: String, size: Int) extends Module {
  val io = IO(new Bundle {
    val dataShift = Input(UInt(9.W))
    val wrMode = Input(WrMode())
    val wrData = Input(UInt(8.W))
    val outData = Output(UInt(8.W))
    
    val instrStep = Input(UInt(9.W))
    val instr = Output(UInt(16.W))

    val counter = Output(UInt(16.W))
  })
}