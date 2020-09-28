package bruno

import java.io.File

// samples for one2one
object One2One {

	
	// helper graph the activations vs expected for the network
	fun graphCostVsParameter(name:String, netp: Network, f: (Double) -> Double,onlyLastLayer:Boolean=true) {
		
		val batch = makebatch(f)
		
		for (l in (if (onlyLastLayer) netp.layers.size-2 else 0) until netp.layers.size-1)
			for (n in 0 until netp.layers[l+1].size){
				
				// graph the cost while varying the bias
			    val netb=netp.clone()	
				val gf=GraphFrame(name+"cost-vs-bias[$l][$n]")
				netb.batchcost(batch)
				
				gf.add (
//				, Pair(0.999, 1.001),
					Graphable(
						{ x ->
							//println(x)
							// modify the parameter						   
							netb.transitions[l].biases[n]=x
							//println("bias netp:"+netp.transitions[l].biases[n])
							netb.batchcost(batch)
							
							
							
							
						},
						"vs-bias[$l][$n]"
					)
				)
				
				gf.setXRange(Pair(-netp.transitions[l].biases[n],2*netp.transitions[l].biases[n]))
				gf.interactive()
				
				
				/*// graph the cost while varying a single weight
				for (m in 0 until netp.layers[l].size){
        		    val netw=netp.clone()	
        			graph(
        				name+"cost-vs-weight[$l][$m,$n]",  Pair(-0.1, 0.1),
        				Graphable(
        					{ x ->
        						// modify the parameter						   
        						netw.transitions[l].weights[m][n]=netp.transitions[l].weights[m][n]+netp.transitions[l].weights[m][n]*x							
        						netw.batchcost(batch)
        					},
        					"vs-w[$l][$m,$n]"
        				)
        			)
				
					
				}
 */
				
				
				
			}

		
		
		
		
	}
	// helper graph the activations vs expected for the network
	fun graphActivations(name: String, net: Network, f: (Double) -> Double,onlyLastLayer:Boolean=true) {

//
//		val l=net.layers.size-1
//		val n=0


		for (l in (if (onlyLastLayer) net.layers.size-1 else 0) until net.layers.size)
			for (n in 0 until net.layers[l].size)
				graph(
					"$name[$l][$n]", Pair(-0.1, 1.1),
					Graphable(
						{ x -> net.feedForward(DoubleArray(1) { x });net.layers[l].activations[n] },
						"a[" + l + "][" + n + "]",
						 Pair(-0.1, 1.1)
					),
					Graphable({ x -> f(x) }, "f(x)", Pair(-0.1, 1.1))
				)


	}

	val F_IDENTITY = { x: Double -> x } // identity
	val F_REVID = { x: Double -> 1 - x } // reverse identity function (shifted by 1)

	// manually crafted 1 transition network that approximate identity function
	fun manual_id() {
		val net = Network(intArrayOf(1, 1))

		net.transitions[0].biases = doubleArrayOf(-2.5)
		net.transitions[0].weights = arrayOf(
			doubleArrayOf(5.0)
		)

		graphActivations("tmp/manual-id", net, F_IDENTITY)
	}


	// manually crafted
// 1 transition network that approximate reverse identity function (shifted by 1)
	fun manual_revid() {

		val net = Network(intArrayOf(1, 1))

		net.transitions[0].biases = doubleArrayOf(2.5)
		net.transitions[0].weights = arrayOf(
			doubleArrayOf(-5.0)
		)



		graphActivations("tmp/manual-revid.png", net, F_REVID)

	}

	val F_BIT_UP = { x: Double -> if (x < 0.35 || x > 0.75) 0.0 else 1.0 } // ok

	// 2 hidden neurons network that approximate bit up function
	fun manual_bit_up() {

		var net = Network(intArrayOf(1, 2, 1))

		net.transitions[0].biases = doubleArrayOf(30.0, -60.0)
		net.transitions[0].weights = arrayOf(
			doubleArrayOf(-80.0, 80.0)
		)
		net.transitions[1].biases = doubleArrayOf(5.0)
		net.transitions[1].weights = arrayOf(
			doubleArrayOf(-10.0),
			doubleArrayOf(-10.0)
		)

		graphActivations("tmp/manual-bitup", net, F_BIT_UP)


		// now mess around with one weight
//		net.transitions[1].weights = arrayOf(
//			doubleArrayOf(1.0),
//			doubleArrayOf(-10.0)
//		)

//		net.transitions[0].biases = doubleArrayOf(8.0, -60.0)
//		
//		learn_function("hill",net,f,100)

		//	val f2 = { x: Double -> if (x > 0.5) 1 - x else x } // bug!!!!!
		//	learn_function("hill", net, f2, 20000)
	}

	val F_VALLEY = { x: Double -> if (x > 0.5) 2 * x - 1 else 1 - 2 * x }

	fun manual_valley() {

		var net = Network(intArrayOf(1, 2, 1))

		net.transitions[0].biases = doubleArrayOf(2.0, -4.0)
		net.transitions[0].weights = arrayOf(
			doubleArrayOf(-6.0, 6.0)
		)
		net.transitions[1].biases = doubleArrayOf(-7.8)
		net.transitions[1].weights = arrayOf(
			doubleArrayOf(11.0),
			doubleArrayOf(11.0)
		)

		
		println(net.batchcost( makebatch(F_VALLEY)));
		graphActivations("tmp/manual-valley", net, F_VALLEY)
		graphCostVsParameter("tmp/cost",net, F_VALLEY);

		// now mess around with one weight
//		net.transitions[1].weights = arrayOf(
//			doubleArrayOf(1.0),
//			doubleArrayOf(-10.0)
//		)

//		net.transitions[0].biases = doubleArrayOf(8.0, -60.0)
//		
//		learn_function("hill",net,f,100)

		//	val f2 = { x: Double -> if (x > 0.5) 1 - x else x } // bug!!!!!
		//	learn_function("hill", net, f2, 20000)
	}

	fun learn_id() {
		var net = Network(intArrayOf(1, 1))
		learn_function("id", net, F_IDENTITY, 1000)
	}


	fun learn_revid() {
		var net = Network(intArrayOf(1, 1))
		learn_function("revid", net, F_REVID, 100)
	}


	fun learn_valley() {
		var net = Network(intArrayOf(1, 2, 1))


//		        // stuck :(
//		net.transitions[0].biases = doubleArrayOf(2.05, -4.01)
//		net.transitions[0].weights = arrayOf(
//			doubleArrayOf(-0.34, 0.19)
//		)
//		net.transitions[1].biases = doubleArrayOf(-0.41);//-7.8)
//		net.transitions[1].weights = arrayOf(
//			doubleArrayOf(0.44),
//			doubleArrayOf(0.55)
//		)
//		

		learn_function("valley", net, F_VALLEY, 1)
	}


	// graph the cost vs parameter change
	// net: network (will be cloned)
	// m: modify the network
	// f: target function 
	fun graphCostVsParameter(
		name: String,
		net: Network,
		wMin: Double,
		wMax: Double,
		m: (Network, Double) -> Unit,
		f: (Double) -> Double
	) {
		val batch = makebatch(f)
		graph(
			"tmp/" + name + ".png", Pair(wMin, wMax),
//			{ x -> net.transitions[1].weights[1][0] = x;net.batchcost(batch) },
			Graphable({ x -> m(net, x);net.batchcost(batch) }, name)
		)

	}

	fun learn_hill() {
		var net = Network(intArrayOf(1, 2, 1))
//	val f = { x: Double -> if (x > 0.55) (1 - x+0.05) else if (x > 0.5) 0.5 else x } // bug!!!!!


		net.transitions[0].biases = doubleArrayOf(30.0, -60.0)
		net.transitions[0].weights = arrayOf(
			doubleArrayOf(-80.0, 80.0)
		)
		net.transitions[1].biases = doubleArrayOf(5.0)
		net.transitions[1].weights = arrayOf(
			doubleArrayOf(-10.0),
			doubleArrayOf(-10.0)
		)

		val f = { x: Double -> if (x > 0.5) 1 - x else x } // bug!!!!!
		//val f = { x: Double -> if (x < 0.35 || x > 0.75) 0.0 else 1.0 } // ok

		learn_function("hill", net, f, 100)


		// to see if we are in a local minima
		// graph the cost when variying only a single parameter
		val batch = makebatch(f)
		graph(
			"tmp/hill-costvsl1-dca0.png", Pair(-10.0, 10.0),
			Graphable({ x -> net.transitions[1].weights[1][0] = x;net.batchcost(batch) }),
			
		)
//		
//		graphNetVsF("tmp/hill-costvsl1-dca0.png",Range(-10.0,10.0),)

	}
//	
//	val f = { x: Double -> if (x > 0.5) 1 - x else x }// bug!!!
//	val f = { x: Double -> if (x > 0.5)  x else 1-x } // ok
//	val f = { x: Double -> if(x<0.3||x>0.7) 1.0 else 0.0 } // ok
//	
//}

	fun makebatch(f: (Double) -> Double): List<Pair<DoubleArray, DoubleArray>> {
		val batch = ArrayList<Pair<DoubleArray, DoubleArray>>()
		val granularity = 50
		for (i in 0 until granularity) {
			val x = i / granularity.toDouble();
			val input = DoubleArray(1) { x }
			val expected = DoubleArray(1) { f(x) }
			val pair = Pair(input, expected)
			batch.add(pair)
			//println(format(input) + " ->" + format(expected))
		}
		return batch
	}

	fun learn_function(name: String, net: Network, f: (Double) -> Double, epochs: Int): Double {

		graphActivations("tmp/$name-@start", net, f)
		File("tmp/$name-@start.dot").writeText(Dotter.dot(false, net))
		println(net.toString(false, true))

		// make the batch
		val batch = makebatch(f)

		// how often a png is dumped
		val graphEvery = epochs / 10
		var epochs2graph = 0
		var cost = 0.0

		for (epoch in 0 until epochs) {
			graphActivations("tmp/$name", net, f)
			if (--epochs2graph <= 0) {
				graphActivations("tmp/$name-@" + String.format("%05d", epoch), net, f)
				epochs2graph = graphEvery

				println(net.toString(false, true))
				println("cost:" + net.batchcost(batch))
				println("epoch:" + epoch)
			}

			val bp = BackPropagation(net)
			bp.learn(1.0, batch)


		}

		graphActivations("tmp/$name-@done", net, f)
		File("tmp/$name-@done.dot").writeText(Dotter.dot(false, net))

		println(net.toString(false, true))
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
		val f = { x: Double -> if (x > 0.5) x else 1 - x } // ok
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
			"tmp/test-init.png",Pair(-0.1, 1.1),
			Graphable({ x -> net.feedForward(DoubleArray(1) { x })[0] },"out"),
			Graphable({ x -> f(x) },"f(x)")
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
			"tmp/test-done.png",Pair(-0.1, 1.1),
			Graphable({ x -> net.feedForward(DoubleArray(1) { x })[0] },"out"),
			Graphable({ x -> f(x)},"f(x)")
		)


	}


}

fun main(args: Array<String>) {
	
	
//	One2One.learn_valley();

	One2One.manual_valley();
}



