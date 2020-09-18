package bruno

class One2One {
	
	

// 1 transition network that approximate identity function
fun one2one_manual_id() {
	val net = Network(intArrayOf(1, 1))

	net.transitions[0].biases = doubleArrayOf(-2.5)
	net.transitions[0].weights = arrayOf(
		doubleArrayOf(5.0)
	)

	val f = { x: Double -> x } // identity

	graphNetVsF("tmp/manual-id.png",net,f)
}

// graph the output vs expected for the network
fun graphNetVsF(name:String,net:Network,f:(Double)->Double){
	graph(
		name, -0.1, 1.1,
		{ x -> net.feedForward(DoubleArray(1) { x })[0] },
		{ x -> f(x) }
	)
}

// 1 transition network that approximate reverse identity function (shifted by 1)
fun one2one_manual_revid() {

	val net = Network(intArrayOf(1, 1))

	net.transitions[0].biases = doubleArrayOf(2.5)
	net.transitions[0].weights = arrayOf(
		doubleArrayOf(-5.0)
	)

	val f = { x: Double -> 1 - x }

	graphNetVsF("tmp/manual-revid.png",net,f)

}

// 2 hidden neurons network that approximate normal function
fun one2one_manual_hill() {

	var net = Network(intArrayOf(1, 2, 1))

//	net.transitions[0].biases = doubleArrayOf(3.0,-6.0)
	net.transitions[0].weights = arrayOf(
		doubleArrayOf(-8.0,8.0)
	)
//	
//	net.transitions[1].biases = doubleArrayOf(5.0)
//	net.transitions[1].weights = arrayOf(
//		doubleArrayOf(-10.0),
//		doubleArrayOf(-10.0)
//	)

	
	
//	net.transitions[0].biases = doubleArrayOf(0.56,0.72)
//	net.transitions[0].weights = arrayOf(
//		doubleArrayOf(0.01,0.63)
//	)
//	
//	net.transitions[1].biases = doubleArrayOf(0.42)
//	net.transitions[1].weights = arrayOf(
//		doubleArrayOf(0.91),
//		doubleArrayOf(0.91)
//	)


	val f = { x: Double -> if(x<0.35||x>0.75) 0.0 else 1.0 } // ok

	graphNetVsF("tmp/manual-hill.png",net,f)
	
	//one2one_learn_function("hill",net,f,1000)
	
	val f2 = { x: Double -> if (x > 0.5) 1 - x else x } // bug!!!!!
	one2one_learn_function("hill",net,f2,20000)
}

fun one2one_learn_id() {
	var net = Network(intArrayOf(1, 1))
	val f = { x: Double -> x } // identity
	one2one_learn_function("hill",net,f,1000)
}


fun one2one_learn_hill() {
    var net = Network(intArrayOf(1,2, 1))
//	val f = { x: Double -> if (x > 0.55) (1 - x+0.05) else if (x > 0.5) 0.5 else x } // bug!!!!!
	val f = { x: Double -> if (x > 0.5) 1 - x else x } // bug!!!!!
	one2one_learn_function("hill",net,f,1000)
	
	
	// to see if we are in a local minima
	// graph the cost when variying only a single parameter
	val batch=one2one_makebatch(f)
	graph(
		"tmp/hill-costvsl1-dca0.png",-10.0,10.0,
		{ x -> net.transitions[1].weights[1][0]=x;net.batchcost(batch)},
		{ x -> 0.0})

}
//	
//	val f = { x: Double -> if (x > 0.5) 1 - x else x }// bug!!!
//	val f = { x: Double -> if (x > 0.5)  x else 1-x } // ok
//	val f = { x: Double -> if(x<0.3||x>0.7) 1.0 else 0.0 } // ok
//	
//}

fun one2one_makebatch(f:(Double)->Double):List<Pair<DoubleArray, DoubleArray>>{
	val batch = ArrayList<Pair<DoubleArray, DoubleArray>>()
	val granularity=50
	for (i in 0 until granularity) {
		val x = i / granularity.toDouble();
		val input = DoubleArray(1) { x }
		val expected = DoubleArray(1) { f(x) }
		val pair = Pair(input, expected)
		batch.add(pair)
//		println(format(input)+" ->"+format(expected))
	}
	return batch
}
fun one2one_learn_function(name:String,net:Network,f:(Double)->Double,epochs:Int):Double {
	
	println(net.toString(false,true))
	// make the batch
	val batch=one2one_makebatch(f)
	
	// how often a png is dumped
	val graphEvery=100
	var epochs2graph=0
	var cost=0.0
	
	for (epoch in 0 until epochs) {
		if (--epochs2graph<=0){
			graphNetVsF("tmp/$name-@" + String.format("%05d", epoch) + ".png",net,f)
			epochs2graph=graphEvery
		}
		println("epoch:" + epoch)
		val bp=BackPropagation(net)
		bp.learn(0.2, batch)
		
		println("cost:" + net.batchcost(batch))
	}
	graphNetVsF("tmp/$name-@" + String.format("%05d", epochs) + ".png",net,f)
	graphNetVsF("tmp/$name-@done" + ".png",net,f)
	println(net.toString(false,true))
	println("cost:" + cost)
	return cost
}

fun play() {


	val net = Network(intArrayOf(1, 2, 1))

/*	net.transitions[0].biases = doubleArrayOf(3.0,-6.0)
	net.transitions[0].weights = arrayOf(
		doubleArrayOf(-8.0,8.0)
	)
	
	net.transitions[1].biases = doubleArrayOf(5.0)
	net.transitions[1].weights = arrayOf(
		doubleArrayOf(-10.0),
		doubleArrayOf(-10.0)
	)
*/

	println(net)

	//val f = { x: Double -> if (x > 0.5) 1 - x else x }// bug!!!
	val f = { x: Double -> if (x > 0.5)  x else 1-x } // ok
//	val f = { x: Double -> if(x<0.3||x>0.7) 1.0 else 0.0 } // ok


	val batch = ArrayList<Pair<DoubleArray, DoubleArray>>()
	for (i in 0 until 30) {
		val x = i / 30.0;
		val input = DoubleArray(1) { x }
		val expected = DoubleArray(1) { f(x) }
		val pair = Pair(input, expected)
		batch.add(pair)
//		println(format(input)+" ->"+format(expected))

	}

	graph(
		"tmp/test-init.png",-0.1, 1.1,
		{ x -> net.feedForward(DoubleArray(1) { x })[0] },
		{ x -> f(x) }
	)

	//println("cost:" + net.learn(1.0, batch))

//	for (epoch in 0 until 1000) {
//		println("*****" + epoch)
//		println("cost:" + net.learn(0.5, batch))
//
//		if (epoch % 1000 == 0) {
//			graph(
//				"test-" + String.format("%03d", epoch) + ".png",-0.1, 1.1,
//				{ x -> net.feedForward(DoubleArray(1) { x })[0] },
//				{ x -> f(x) }
//			)
//		}
//
//	}
	//log = true
//	println("cost:" + net.learn(1.0, batch))
	//println("cost:" + net.learn(1.0, batch))


	graph(
		"tmp/test-done.png", -0.1, 1.1,
		{ x -> net.feedForward(DoubleArray(1) { x })[0] },
		{ x -> f(x) }
	)
	
	
	
}


	
}

fun main(args: Array<String>) {
//	one2one_manual_hill();
	val one2one=One2One()
	one2one.one2one_learn_hill();
}



