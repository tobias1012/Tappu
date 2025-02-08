package tappu

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec


class TappuTester extends AnyFlatSpec with ChiselScalatestTester{
  //Find some tests to run
  behavior of "Tappu"
  it should "run a simple program" in {
    test(new Tappu()){ dut =>
      //Test Something
      //dut.io.in.poke(0.U)
      //dut.io.out.expect(0.U)
    }
  }
}