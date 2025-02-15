package tappu

import chisel3._
import chisel3.util._
//Initialize memory with experimental feature
import chisel3.util.experimental.loadMemoryFromFile


import tappu.util._
import tappu.Compiler.compile

class TapeMemory(programPath: String, size: Int) extends Module {
  val io = IO(new Bundle {
    val dataShift = Input(UInt(9.W))
    val wrEn = Input(Bool())
    val wrData = Input(UInt(9.W))
    val outData = Output(UInt(8.W))
    
    val instrStep = Input(UInt(9.W))
    val instr = Output(UInt(16.W))

    val counter = Output(UInt(16.W))
    //val memBusy = Output(Bool())
  })



  val tapeCounterReg = RegInit(0.U(16.W))
  io.counter := tapeCounterReg

  val pc = RegInit(0.U(16.W))
  val nextPc = Wire(UInt(16.W))
  val pcReg = RegNext(pc)       // Holds previous PC


  // Programcounter at tape(1,0)
  val registerNum = 2

  val tape = SyncReadMem(size, UInt(8.W))
  //Write the data on the tape

  val memPath = compile(programPath, "tappuCPU.mem", size)
  loadMemoryFromFile(tape, memPath)
  
  //val idle::readInstr::updatePc::writeMem::Nil = Enum(4)
  //val memState = RegInit(idle)


  //When we shift the data
  when(io.dataShift =/= 0.U) {
    when(io.dataShift(8) === 0.U) {
      tapeCounterReg := Mux(tapeCounterReg + io.dataShift(7,0) > 255.U, 255.U, tapeCounterReg + io.dataShift(7,0))
    } .otherwise {
      tapeCounterReg := Mux(tapeCounterReg - io.dataShift(7,0) < 0.U, 0.U, tapeCounterReg - io.dataShift(7,0))
    }
  }

  val readData = tape.read(tapeCounterReg)

  when(io.wrEn) {
    tape.write(tapeCounterReg, Mux(io.wrData(8) === 0.U, readData + io.wrData(7,0), readData - io.wrData(7,0)))
  }
  
  val pcLow  = RegNext(tape.read(0.U)) // Delay PC low byte read
  val pcHigh = RegNext(tape.read(1.U)) // Delay PC high byte read
  pc := Cat(pcHigh, pcLow) // Construct 16-bit PC

  // Fetch instruction as little-endian
  val instrLow  = RegNext(tape.read(pcReg + registerNum.U))
  val instrHigh = RegNext(tape.read(pcReg + 1.U + registerNum.U))
  io.instr := Cat(instrHigh, instrLow)

  io.outData := readData // Output latest read data

  //increment the pc
  nextPc := 0.U
  when(io.instrStep(8) === 1.U) {
    when(io.instrStep(7,0) === 0.U) {
      nextPc := pc + 2.U  // Move forward by 2 (next instruction)
    } .otherwise {
      nextPc := pc - (io.instrStep(7,0) << 1) // Move backward by offset * 2
    }
    pc := nextPc
  }

  // Delayed PC write-back to memory (safe)
  when(io.instrStep(8) === 1.U) {
    tape.write(0.U, nextPc(7,0))
    tape.write(1.U, nextPc(15,8))
  }
}