package tappu

import chisel3._
import chisel3.util._

import tappu.util._

class Tappu(prog: String) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(8.W))
    val out = Output(UInt(8.W))
  })


  val inReg = RegInit(0.U(8.W))
  inReg := io.in

  val outReg = RegInit(0.U(8.W))
  outReg := io.out

  val mem = Module(new TapeMemory(prog, 256))
  

  val instrStep = Wire(Bool())
  val instr = Wire(UInt(16.W))
  val wrEn = Wire(Bool())
  val wrData = Wire(UInt(9.W))
  val dataShift = Wire(UInt(9.W))
  
  mem.io.instrStep := instrStep
  instr := mem.io.instr
  mem.io.wrEn := wrEn
  mem.io.wrData := wrData
  mem.io.dataShift := dataShift

  instrStep := false.B
  wrEn := false.B
  wrData := 0.U
  dataShift := 0.U


  val fetch::execute::peripheral::Nil = Enum(3)

  val state = RegInit(execute)
  switch(state) {
    is(fetch) {
      instrStep := true.B


      state := execute
    }
    is(execute) {
      instrStep := false.B
      //Only when read is starting
      when(instr(7,0) === Opcode.Read.asUInt) {
        state := peripheral
      }
      when(instr(7,0) === Opcode.Print.asUInt) {
        outReg := mem.io.outData
        state := fetch
      }
      when(instr(7,0) === Opcode.Right.asUInt) {
        dataShift := Cat(0.U, instr(15,8))
        state := fetch
      }
      when(instr(7,0) === Opcode.Left.asUInt) {
        dataShift := Cat(1.U, instr(15,8))
        state := fetch
      }
      when(instr(7,0) === Opcode.Add.asUInt) {
        wrEn := true.B
        wrData := Cat(0.U, instr(15,8))
        state := fetch
      }
      when(instr(7,0) === Opcode.Sub.asUInt) {
        wrEn := true.B
        wrData := Cat(1.U, instr(15,8))
        state := fetch
      }
      when(instr(7,0) === Opcode.Set.asUInt) {
        wrEn := true.B
        wrData := Cat(0.U, instr(15,8))
        state := fetch
      }

    }

    is(peripheral) {
      // Await io then change state back to fetch

      state := fetch
    }
  }


  io.out := outReg
}

object TappuMain {
  def main(args: Array[String]): Unit = {
    println("Generating Tappu")
    emitVerilog(new Tappu(args(0)))
  }
}