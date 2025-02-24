package tappu

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.control.Breaks._

import tappu.util.Opcode


class TappuTester extends AnyFlatSpec with ChiselScalatestTester{
  //Find some tests to run
  val options = new Options {
    debug = true
    vecMem = true
  }

  behavior of "Tappu"
  it should "run a simple program" in {
    test(new Tappu("./programs/helloworld.tappu", options)){ dut =>
      //dut.clock.setTimeout(0)
      var max_iter = 1000
      var success_string = "Hello World"
      var seen_string = ""
      while(max_iter != 0) {
        max_iter -= 1
        
        val instr = dut.io.dbg.get.instr.peek()
        //println("Instr: " + Opcode.intToString(instr(7,0).litValue.toInt) + " 0x" + instr(15,8).litValue.toInt.toHexString)
        //println("Current Tape value: " + dut.io.dbg.get.tapeOut.peek().litValue)

        if(instr(7,0).litValue.toInt == Opcode.Print){
          seen_string = seen_string + dut.io.dbg.get.tapeOut.peek().litValue.toInt.toChar 
          if(seen_string == success_string){
            println("SUCCESS")
            max_iter = 0
          }
        }
        if(dut.io.dbg.get.quit.peek().litToBoolean){
          println("QUIT")
          max_iter = 0
        }
        dut.clock.step(1)
      }
    }
  }
  it should "print 1,2,3" in {
    test(new Tappu("./programs/reader.tappu", options)){ dut =>  
      //Input a program, that writes 1,2,3
      val vals = List(1,2,3)
      var found = 0
      for(i <- 1 to 13){
        //println("Current cycle: " + i)
        //println("OUTPUT: " + dut.io.out.peek().litValue)
        //println("Current Tape value: " + dut.io.dbg.get.tapeOut.peek().litValue)
        val instr = dut.io.dbg.get.instr.peek()
        if(instr(7,0).litValue.toInt == Opcode.Print){
          dut.io.dbg.get.tapeOut.expect(vals(found).U)
          found += 1
          println("Found: " + found)
        }
        //println("Instr: " + Opcode.intToString(instr(7,0).litValue.toInt) + " 0x" + instr(15,8).litValue.toInt.toHexString)
        //println("Counter: " + dut.io.dbg.get.counter.peek().litValue)
        dut.clock.step(1)

      }
      //dut.io.out.expect(0.U)
    }
  }
  it should "run a loop correctly" in {
    test(new Tappu("./programs/loopTest.tappu", options)){ dut =>  
      //dut.clock.setTimeout(0)    
      //Input a program, that reads its own bytes
      var run = true
      while(run){
        //println("######################")
        //println("OUTPUT: " + dut.io.out.peek().litValue)
        println("Current Tape value: " + dut.io.dbg.get.tapeOut.peek().litValue)
        val instr = dut.io.dbg.get.instr.peek()
        println("Instr: " + Opcode.intToString(instr(7,0).litValue.toInt) + " 0x" + instr(15,8).litValue.toInt.toHexString)
        //println("Counter: " + dut.io.dbg.get.counter.peek().litValue)
        
        if(dut.io.dbg.get.quit.peek().litToBoolean){
          println("QUIT")
          run = false
        }
        //println()
        dut.clock.step(1)
      }
      //dut.io.in.poke(0.U)
      dut.io.out.expect(40.U)
    }
  }
}