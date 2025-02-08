package tappu

import chisel3._
import chisel3.util._

import tappu.util._

class Tappu extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(8.W))
    val out = Output(UInt(8.W))
  })

  val prog = Assembler.asm("./programs/test.tappu")

  val reg = RegInit(0.U(8.W))
  reg := io.in
  io.out := reg
}

object TappuMain {
  def main(args: Array[String]): Unit = {
    println("Generating Tappu")
    emitVerilog(new Tappu())
  }
}