package tappu

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

import tappu.util.Opcode


class TappuTester extends AnyFlatSpec with ChiselScalatestTester{
  //Find some tests to run
  behavior of "Tappu"
  it should "run a simple program" in {
    test(new Tappu("./programs/helloworld.tappu")){ dut =>      
      //Test Something
      //dut.io.in.poke(0.U)
      //dut.io.out.expect(0.U)
    }
  }
  it should "print 1,2,3" in {
    test(new Tappu("./programs/reader.tappu", debug = true)){ dut =>      
      //Input a program, that reads its own bytes
      for(i <- 1 to 13){
        println("Current cycle: " + i)
        println("OUTPUT: " + dut.io.out.peek().litValue)
        println("Current Tape value: " + dut.io.dbg.get.tapeOut.peek().litValue)
        val instr = dut.io.dbg.get.instr.peek()
        println("Instr: " + Opcode.intToString(instr(7,0).litValue.toInt) + " 0x" + instr(15,8).litValue.toInt.toHexString)
        println("Counter: " + dut.io.dbg.get.counter.peek().litValue)
        dut.clock.step(1)

      }
      //dut.io.in.poke(0.U)
      //dut.io.out.expect(0.U)
    }
  }
}