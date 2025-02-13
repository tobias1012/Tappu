package tappu

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec


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
    test(new Tappu("./programs/reader.tappu")){ dut =>      
      //Input a program, that reads its own bytes

      //dut.io.in.poke(0.U)
      //dut.io.out.expect(0.U)
    }
  }
}