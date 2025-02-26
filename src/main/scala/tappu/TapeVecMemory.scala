package tappu

import chisel3._
import chisel3.util._
//Initialize memory with experimental feature
import chisel3.util.experimental.loadMemoryFromFile

import tappu.util._
import tappu.Compiler.compile_seq

class TapeVecMemory(programPath: String, size: Int)
    extends TapeAbstract(programPath, size) {

  // Memories for the program and the tape
  val program = VecInit(compile_seq(programPath).map(_.U))
  val tape = SyncReadMem(size, UInt(8.W))

  val init :: ready :: Nil = Enum(2)
  val state = RegInit(init)
  val tapeCounterReg = RegInit(0.U(16.W))
  val pc = RegInit(0.U(log2Ceil(program.length).W))

  io.counter := tapeCounterReg
  io.instr := 0.U
  io.outData := 0.U

  switch(state) {
    is(init) {
      when(tapeCounterReg =/= size.U) {
        // Initialize memory with zero
        tape.write(tapeCounterReg, 0.U)
        tapeCounterReg := tapeCounterReg + 1.U
      }.otherwise {
        tapeCounterReg := 0.U
        state := ready
      }
    }

    is(ready) {
      when(io.dataShift =/= 0.U) {
        when(io.dataShift(8) === 0.U) {
          tapeCounterReg := Mux(tapeCounterReg + io.dataShift(7, 0) >= size.U, (size - 1).U,tapeCounterReg + io.dataShift(7, 0)
          )
        }.otherwise {
          tapeCounterReg := Mux(io.dataShift(7, 0) >= tapeCounterReg,0.U,tapeCounterReg - io.dataShift(7, 0))
        }
      }
      val readData = tape.read(tapeCounterReg)
      when(io.wrMode =/= WrMode.none) {
        when(io.wrMode === WrMode.plus) {
          tape.write(tapeCounterReg,Mux(readData + io.wrData > 255.U, 255.U, readData + io.wrData))
        }.elsewhen(io.wrMode === WrMode.minus) {
          tape.write(tapeCounterReg, Mux(readData < io.wrData, 0.U, readData - io.wrData))
        }.elsewhen(io.wrMode === WrMode.set) {
          tape.write(tapeCounterReg, io.wrData)
        }
      }

      val instrLow = program(pc)
      val instrHigh = program(pc + 1.U)
      io.instr := Cat(instrHigh, instrLow)
      io.outData := readData

      when(io.instrStep(8) === 1.U(1.W)) {
        when(io.instrStep(7, 0) === 0.U) {
          pc := pc + 2.U
        }.otherwise {
          pc := pc - (io.instrStep(7, 0) << 1) // Move backward by offset * 2
        }
      }
    }
  }

}
