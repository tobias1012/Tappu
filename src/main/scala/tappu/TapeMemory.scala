package tappu.tape

import chisel3._

class TapeMemory(program: List[Int], size: Int) extends Module {
  val io = IO(new Bundle {
    val dataShift = Input(UInt(9.W))
    val wrEn = Input(Bool())
    val wrData = Input(UInt(9.W))
    val outData = Output(UInt(8.W))
    val tapePointer = Output(UInt(8.W))
  })


  val tapeCounterReg = RegInit(0.U(16.W))

  // Programcounter at tape(1,0)
  // Readpointer at tape(2)
  // Accumulator at tape(3)
  val registerNum = 3
  val fullTapeSize = size + registerNum

  val tape = Mem(fullTapeSize, UInt(8.W))
  //Write the data on the tape
  for (i <- 0 until program.length) {
    tape(i+registerNum) := program(i).U
  }

  tapeCounterReg := program.length.U
  tape(0) := registerNum.U
  tape(1) := 0.U
  tape(2) := tape(0)
  tape(3) := 0.U

  val instr = tape(tape(0))
  
  when(io.wrEn) {
    val a = 0.U
    tape(tapeCounterReg) := Mux(io.wrData(8) === 0.U, tape(tapeCounterReg) + io.wrData(7,0), tape(tapeCounterReg) - io.wrData(7,0))
    
  }


  io.outData := tape(tapeCounterReg)
}