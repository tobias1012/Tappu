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


  // "Stack"Pointer at tape(0)
  // Programcounter at tape(1)
  // Readpointer at tape(2)
  val registerNum = 3
  val fullTapeSize = size + registerNum

  val tape = Mem(fullTapeSize, UInt(8.W))
  //Write the data on the tape
  for (i <- 0 until program.length) {
    tape(i+registerNum) := program(i).U
  }

  tape(0) := program.length.U
  tape(1) := registerNum.U
  tape(2) := tape(0)

  val instr = tape(tape(1))
  
  when(io.wrEn) {
    tape(tape(0)) := Mux(io.wrData(8) === 0.U, tape(tape(0)) + io.wrData(7,0), tape(tape(0)) - io.wrData(7,0))
    
  }


  io.outData := tape(tape(0))
}