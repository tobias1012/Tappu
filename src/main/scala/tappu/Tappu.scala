package tappu

import chisel3._
import chisel3.util._

import tappu.util._

class Options {
  // Memory options
  // Default is all synchronous memories, this allows polymorphic programs
  var uartMem = false //TOdO: Implement
  var vecMem = false // Vec mem implements a ROM for the program, the tape is reserved for program data

  // Debugging options
  var debug = false
  var uartDebug = false //TODO: Implement
}

class Debug extends Bundle {
  val tapeOut = UInt(8.W)
  val instr = UInt(16.W)
  val counter = UInt(16.W)
  val quit = Bool()
}

class Tappu(prog: String, options: Options = new Options()) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(8.W))
    val out = Output(UInt(8.W))

    val dbg = if (options.debug) Some(Output(new Debug)) else None
  })


  val inReg = RegInit(0.U(8.W))
  inReg := io.in

  val outReg = RegInit(0.U(8.W))
  io.out := outReg
  
  val mem = if(options.vecMem) {
    Module(new TapeVecMemory(prog, 3))
  } else {
    Module(new TapeMemory(prog, 512))
  }

  val instrStep = WireDefault(0.U(9.W))
  val instr = Wire(UInt(16.W))
  val wrMode = WireDefault(WrMode.none)
  val wrData = Wire(UInt(9.W))
  val dataShift = Wire(UInt(9.W))

  val instrStepReg = RegInit("b100000000".U(9.W))

  
  mem.io.instrStep := instrStep
  instr := mem.io.instr
  
  mem.io.wrMode := wrMode
  mem.io.wrData := wrData
  mem.io.dataShift := dataShift

  if(options.debug){
    io.dbg.get.instr := instr
    io.dbg.get.tapeOut := mem.io.outData
    io.dbg.get.counter := mem.io.counter
    io.dbg.get.quit := false.B

  }
  
  // Defaults
  wrMode := WrMode.none
  wrData := 0.U
  dataShift := 0.U
  instrStepReg := "b100000000".U

  val instrFetch::instrDec::execute::memAcc::writeBack::halt::Nil = Enum(6)
  val cpuState = RegInit(instrFetch)

  switch(cpuState) {
    is (instrFetch) { // Instruction fetch
      instrStep := instrStepReg
      cpuState := instrDec
    }
    is (instrDec) { // Instruction decode
      cpuState := execute
    }
    is (execute) { // Execute
      cpuState := memAcc
      switch(instr(7,0)) {
        is(Opcode.Read.asUInt) {
          // Nothing to do right now.
          wrMode := WrMode.set
          wrData := inReg
        }
        is(Opcode.Print.asUInt) {
          outReg := mem.io.outData
        }
        is(Opcode.Left.asUInt) {
          dataShift := Cat(1.U(1.W), instr(15,8))
        }
        is(Opcode.Right.asUInt) {
          dataShift := Cat(0.U(1.W), instr(15,8))
        }
        is(Opcode.Add.asUInt) {
          wrMode := WrMode.plus
          wrData := instr(15,8)
        }
        is(Opcode.Sub.asUInt) {
          wrMode := WrMode.minus
          wrData := instr(15,8)
        }
        is(Opcode.LoopStart.asUInt) {
          // Nothing to do right now.
        }
        is(Opcode.LoopEnd.asUInt) {
          when (!(mem.io.outData === 0.U)) {
            instrStepReg := Cat(1.U(1.W), instr(15,8))
            cpuState := instrFetch
          }
        }
        is(Opcode.quit.asUInt) {
          if(options.debug){
            io.dbg.get.quit := true.B
          }
          cpuState := halt
        }
      }
      

    }
    is (memAcc) { // Memory access
      cpuState := writeBack
    }
    is (writeBack) { // Write back

      cpuState := instrFetch
    }
    is (halt) { // Halt
      // Do nothing
    }
  }

  io.out := outReg
}

object TappuMain {
  def main(args: Array[String]): Unit = {
    println("Generating Tappu")
    val options = new Options {
      debug = false
      vecMem = true
    }
    emitVerilog(new Tappu(args(0), options))
  }
}