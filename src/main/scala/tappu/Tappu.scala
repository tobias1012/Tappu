package tappu

import chisel3._
import chisel3.util._

import tappu.util._

class Debug extends Bundle {
  val tapeOut = UInt(8.W)
  val instr = UInt(16.W)
  val counter = UInt(16.W)
  val quit = Bool()
}

class Tappu(prog: String, debug: Boolean = false) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(8.W))
    val out = Output(UInt(8.W))

    val dbg = if (debug) Some(Output(new Debug)) else None
  })


  val inReg = RegInit(0.U(8.W))
  inReg := io.in

  val outReg = RegInit(0.U(8.W))
  io.out := outReg

  val mem = Module(new TapeMemory(prog, 512))
  

  val instrStep = WireDefault(0.U(9.W))
  val instr = Wire(UInt(16.W))
  val wrEn = WireDefault(false.B)
  val wrData = Wire(UInt(9.W))
  val dataShift = Wire(UInt(9.W))
  
  mem.io.instrStep := instrStep
  instr := mem.io.instr
  
  mem.io.wrEn := wrEn
  mem.io.wrData := wrData
  mem.io.dataShift := dataShift

  if(debug){
    io.dbg.get.instr := instr
    io.dbg.get.tapeOut := mem.io.outData
    io.dbg.get.counter := mem.io.counter
    io.dbg.get.quit := false.B

  }
  

  wrEn := false.B
  wrData := 0.U
  dataShift := 0.U


  val execute::peripheral::halt::Nil = Enum(3)

  val state = RegInit(execute)
  //instrStep := Cat(1.U(1.W), 1.U(8.W))
  instrStep := "b100000000".U

  switch(state) {
    is(execute) {
      //Only when read is starting
      when(instr(7,0) === Opcode.Read.asUInt) {
        state := peripheral
      }
      when(instr(7,0) === Opcode.Print.asUInt) {
        outReg := mem.io.outData
      }
      when(instr(7,0) === Opcode.Right.asUInt) {
        dataShift := Cat(0.U(1.W), instr(15,8))
      }
      when(instr(7,0) === Opcode.Left.asUInt) {
        dataShift := Cat(1.U(1.W), instr(15,8))
      }
      when(instr(7,0) === Opcode.Add.asUInt) {
        mem.io.wrEn := true.B
        wrData := Cat(0.U(1.W), instr(15,8))
      }
      when(instr(7,0) === Opcode.Sub.asUInt) {
        wrEn := true.B
        wrData := Cat(1.U, instr(15,8))
      }
      when(instr(7,0) === Opcode.Set.asUInt) {
        wrEn := true.B
        wrData := Cat(0.U, instr(15,8))
      }
      when(instr(7,0) === Opcode.LoopStart.asUInt) {
      }
      when(instr(7,0) === Opcode.LoopEnd.asUInt) {
        when (!(mem.io.outData === 0.U)) {
          instrStep := Cat(1.U(1.W), instr(15,8))
        }
      }
      when(instr(7,0) === Opcode.AccStore.asUInt) {
      }
      when(instr(7,0) === Opcode.AccLoad.asUInt) {
      }
      when(instr(7,0) === Opcode.quit.asUInt) {
        state := halt
      }

    }

    is(peripheral) {
      // Await io then change state back to execute

      state := execute
    }

    is(halt) {
      // Do nothing
      if(debug){
        io.dbg.get.quit := true.B
      }
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