
APP ?= programs/test.tappu

# Generate Verilog code
doit:
	sbt "runMain tappu.TappuMain $(APP)"

# Run the test
test:
	sbt test

clean:
	git clean -fd

