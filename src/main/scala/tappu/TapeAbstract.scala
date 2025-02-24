package tappu

import chisel3._
import chisel3.util._
//Initialize memory with experimental feature
import chisel3.util.experimental.loadMemoryFromFile


import tappu.util._
import tappu.Compiler.compile

class TapeAbstract(programPath: String, size: Int) extends Module {
  val io = IO(new Bundle {
    val dataShift = Input(UInt(9.W))
    val wrEn = Input(Bool())
    val wrData = Input(UInt(9.W))
    val outData = Output(UInt(8.W))
    
    val instrStep = Input(UInt(9.W))
    val instr = Output(UInt(16.W))

    val counter = Output(UInt(16.W))
  })
}