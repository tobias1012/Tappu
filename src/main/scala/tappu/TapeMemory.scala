package tappu

import chisel3._
import chisel3.util._
import tappu.util._

class TapeMemory(programPath: String, size: Int) extends Module {
  val io = IO(new Bundle {
    val dataShift = Input(UInt(9.W))
    val wrEn = Input(Bool())
    val wrData = Input(UInt(9.W))
    val outData = Output(UInt(8.W))
    
    val instrStep = Input(Bool())
    val instr = Output(UInt(16.W))
  })

  val program = VecInit(Assembler.asm(programPath).flatMap( i => List(i.opcode, i.data).map(_.U)))


  val tapeCounterReg = RegInit(0.U(16.W))
  val pc = RegInit(0.U(16.W))
  val nextPc = Wire(UInt(16.W))

  // Programcounter at tape(1,0)
  // Readpointer at tape(2)
  // Accumulator at tape(3)
  val registerNum = 3
  val fullTapeSize = size + registerNum

  val tape = Mem(fullTapeSize, UInt(8.W))
  //Write the data on the tape
  for (i <- 0 until program.length) {
    tape(i+registerNum) := program(i)
  }

  tapeCounterReg := program.length.U
  tape(0) := 0.U
  tape(1) := 0.U
  tape(2) := tape(0)
  
  //When we shift the data
  when(!(io.dataShift === 0.U)) {
    when(io.dataShift(8) === 0.U) {
      tapeCounterReg := Mux(tapeCounterReg + io.dataShift(7,0) > 255.U, 255.U, tapeCounterReg + io.dataShift(7,0))
    }
    when(io.dataShift(8) === 1.U) {
      tapeCounterReg := Mux(tapeCounterReg - io.dataShift(7,0) < 0.U, 0.U, tapeCounterReg - io.dataShift(7,0))
    }
  }

  when(io.wrEn) {
    tape(tapeCounterReg) := Mux(io.wrData(8) === 0.U, tape(tapeCounterReg) + io.wrData(7,0), tape(tapeCounterReg) - io.wrData(7,0))
    
  }


  io.outData := tape(tapeCounterReg)
  //Load instructions as little endian
  pc := Cat(tape(1), tape(0))
  io.instr := Cat(tape( pc + registerNum.U), tape( pc + 1.U + registerNum.U))

  //increment the pc
  nextPc := 0.U
  when(io.instrStep) {
    //add two to account for the argument
    nextPc := pc + 2.U
    tape(0) := pc(7,0)
    tape(1) := pc(15,8)
  }
}