package bruno

// CAPTCHA: in kotlin until is exclusive but downTo is inclusive ???
import java.util.*
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import java.awt.Color


val rnd = Random(8)

/**
Matrix-vector product
@see https://mathinsight.org/matrix_vector_multiplication



fun product(w: Array<DoubleArray>, a: DoubleArray): DoubleArray {
return DoubleArray(w.size) { i ->
var acc = 0.0
for (j in 0 until a.size)
acc += w[i][j] * a[j]
acc
}
}



// add cells of 2 vectors
fun plus(a: DoubleArray, b: DoubleArray): DoubleArray {
return DoubleArray(a.size) { i ->
a[i] + b[i]
}
}
 */


var log = false
fun LOG(f: () -> Any) {
	if (log) println("" + f())
}

fun format(ds: DoubleArray): String {
	var r = ""
	for (i in ds.indices)
		r += format(ds[i])
	return r
}

fun format(d: Double): String {
	return String.format("|%4.2f", d);
}

fun format(d: Int): String {
	return String.format("|%4d", d);
}

fun formatRuler(to: Int): String {
	var r = ""
	for (i in 0 until to)
		r += format(i)
	return r
}

// a Layer transition holds the weights and biases used to compute the neuron activation of the next layer
class LayerTransition(inputSize: Int, outputSize: Int) {
	var biases = DoubleArray(outputSize) { i -> if (i==0) 0.0 else rnd.nextDouble() } // TODO:why 0 when i==0?
	var weights = Array<DoubleArray>(inputSize) {
		DoubleArray(outputSize) {
			rnd.nextDouble()
		}
	}

	// derivatives 
	val dCostVsBiases = DoubleArray(outputSize) { 0.0 }
	val dCostVsWeights = Array<DoubleArray>(inputSize) { DoubleArray(outputSize) { 0.0 } }


	override fun toString(): String {
		val nextLayerSize = weights[0].size;
		var r = ""


		r += "\nbias and weights:"
		r += "\n"
		r += "    j\\i: bias" + formatRuler(weights.size)
		for (j in 0 until nextLayerSize) {
			r += "\n" + format(j) + ":  "
			r += format(biases[j])
			for (i in 0 until weights.size) {
				r += format(weights[i][j])
			}
		}
		return r
	}

}

class Layer(val size: Int) {
	// neuron activations
	var activations = DoubleArray(size) { 0.0 }

	// derivative of total cost with respect to activations
	val dCostVsdActivations = DoubleArray(size) { rnd.nextDouble() }


	override fun toString(): String {
		var r = ""
		r += "\n" + activations.size + " neurons \n"
		r += "#       " + formatRuler(activations.size)
		r += "\n active "
		for (i in 0 until activations.size)
			r += format(activations[i])
		return r


	}
}

class Network(val sizes: IntArray) {


	val layers = Array(sizes.size) { i -> Layer(sizes[i]) };
	val transitions = Array(sizes.size - 1) { i -> LayerTransition(sizes[i], sizes[i + 1]) }


	fun sigmoid(z: Double): Double {
		return 1 / (1 + Math.exp(-z))
	}

	//  Derivative of the sigmoid function	
	fun sigmoidDerivative(z: Double): Double {
		return sigmoid(z) * (1 - sigmoid(z))
	}


	/*fun sigmoid(z: DoubleArray): DoubleArray {
		return z.map { zi -> sigmoid(zi) }.toDoubleArray()
	}

	fun sigmoidDerivative(z: DoubleArray): DoubleArray {
		return z.map { zi -> sigmoidDerivative(zi) }.toDoubleArray()
	}*/


	// update the layers activation forward
	fun feedForward(input: DoubleArray): DoubleArray {

		// just copy over the input layer
		layers[0].activations = input


		for (l in 1 until layers.size) {
			val prevLayer = layers[l - 1]
			val nextLayer = layers[l]
			val transition = transitions[l - 1]


			// loop for every neuron j in nextLayer
			for (j in 0 until nextLayer.size) {

				// compute weighted input
				var z_acc = transition.biases[j]
				for (i in 0 until prevLayer.size) {
					val a = prevLayer.activations[i];
					val w = transition.weights[i][j];
					z_acc += a * w
				}

				// update activation
				nextLayer.activations[j] = sigmoid(z_acc)
			}
		}
		return layers[layers.size - 1].activations
	}

	fun sgd() {
		// TODO
	}

	fun miniBatch() {
		// TODO
	}

	fun label2expected(expected: Int): DoubleArray {
		return DoubleArray(sizes[layers.size - 1]) { i -> if (i == expected) 1.0 else 0.0 }
	}


	// compare the output layer vs expected result
	// as defined in page 42
	fun cost(expected: DoubleArray): Double {
		var acc = 0.0;
		val last = layers[layers.size - 1].activations;

		for (i in 0 until last.size) {
			val diff = last[i] - expected[i]
			val d2 = diff * diff
			acc += d2
		}
		LOG({ "expected:" + format(expected) })
		LOG({ "last:" + format(last) })
		LOG({ "cost:" + format(acc) })
		return acc // in the book what is the point of the 2 here?

	}

	// Clear the weight and bias derivatives accumulators
	fun cleard() {
		for (l in 0 until transitions.size)
			for (j in 0 until sizes[l + 1]) {

				transitions[l].dCostVsBiases[j] = 0.0
				for (i in 0 until sizes[l]) {
					transitions[l].dCostVsWeights[i][j] = 0.0
				}

			}
	}


	// Compute the gradiant
	fun costd(expected: DoubleArray) {


		// Reset the derivative of cost vs neuron's activation
		// Note that the other derivatives (cost vs weight and bias) are not cleared since we want to accumulate till end of minibatch
		for (l in 0 until layers.size) {
			val layer = layers[l]
			for (i in 0 until layer.size) {
				layer.dCostVsdActivations[i] = 0.0
			}
		}

		// Compute derivative of cost vs the last (output) layer
		val lastLayer = layers[layers.size - 1];
		for (i in 0 until lastLayer.size)
			lastLayer.dCostVsdActivations[i] = 2 * lastLayer.activations[i] - 2 * expected[i]


		// now loop backwards for every layer pair 
		for (l in layers.size - 1 downTo 1) { // downTo is inclusive!
			val nextLayer = layers[l]
			val prevLayer = layers[l - 1]

			// transition from prevLayer to nextLayer
			val transition = transitions[l - 1]


			// compute derivative of cost vs
			// - weights of transition
			// - bias of transition
			// - activation of neurons in prevLayer


			// j is neuron of current layer			
			for (j in 0 until nextLayer.size) {


				// bias for j
				val bj = transition.biases[j]

				// compute zj, weighted input for neuron j 
				var zj_acc = bj;
				for (i in 0 until prevLayer.size) {
					// weight for going from i to j 
					val wij = transition.weights[i][j]
					zj_acc += prevLayer.activations[i] * wij
				}

				// partial derivatives
				// how does cost changes when Aj changes: this is computed by previous pass of the loop
				val dCost_Vs_dAj = nextLayer.dCostVsdActivations[j]
				// how does Aj changes when Zj changes
				val dAj_Vs_dZj = sigmoidDerivative(zj_acc);
				// how does cost changes when Zj changes - this will be used to compute the next 3 derivatives we are interested about
				val dCost_Vs_dZj = dCost_Vs_dAj * dAj_Vs_dZj;

				for (i in 0 until prevLayer.size) {
					// now we are looking at a single transition from prevLayer neuron i to nextLayer neuron j

					// weight for going from i to j 
					val wij = transition.weights[i][j]


					// partial derivatives

					// chain rules to compute derivative of cost vs prevLayer.activations[i]
					// this value is used to propagate the derivative of cost vs previous layers
					val dZj_Vs_dAi = wij;
					val dCost_Vs_dAi = dCost_Vs_dZj * dZj_Vs_dAi;
					prevLayer.dCostVsdActivations[i] += dCost_Vs_dAi


					// chain rules to compute derivative of cost vs transition.bias[j]
					// this value is part of the gradient
					val dZj_Vs_dBj = 1;
					val dCost_Vs_dBj = dCost_Vs_dZj * dZj_Vs_dBj;
					transition.dCostVsBiases[j] += dCost_Vs_dBj
					//println("transition.dCostVsBiases[$j]:" + transition.dCostVsBiases[j])

					// chain rules to compute derivative of cost vs transition.weight[i,j]
					// this value is part of the gradient
					val dZj_Vs_dWij = prevLayer.activations[i]
					val dCost_Vs_dWij = dCost_Vs_dZj * dZj_Vs_dWij;

					transition.dCostVsWeights[i][j] += dCost_Vs_dWij
					//println("transition.dCostVsWeights[$i][$j]:" + transition.dCostVsWeights[i][j])

				}
			}
		}

	}


	// update the weights and biases according to the gradiant
	fun changeWeights(m: Double) {


		for (l in 0 until transitions.size) {
			val transition = transitions[l]
			// update bias
			for (j in 0 until transition.biases.size) {
				transition.biases[j] += m * -transition.dCostVsBiases[j]
			}

			// update weight
			for (i in 0 until transition.weights.size) {
				for (j in 0 until transition.weights[i].size) {
					transition.weights[i][j] += m * -transition.dCostVsWeights[i][j]
				}
			}
		}
	}

    fun learn(learnRate: Double, pairs: List<Pair<DoubleArray, DoubleArray>>): Double {
		val cost=batch(pairs)
		changeWeights(learnRate);
		return cost
    }
	
	// update the derivatives
	// return the cost
	fun batch(pairs: List<Pair<DoubleArray, DoubleArray>>): Double {

		cleard()

		var cost_Acc = 0.0
		for (p in pairs.indices) {

			//	LOG("learn sample " + p)
			val pair = pairs[p]
			feedForward(pair.first)
			LOG({ " " + toString(true, false) })
			cost_Acc += cost(pair.second)
			costd(pair.second);

		}
		LOG({ " " + toString(false, true) })
		




		return cost_Acc / pairs.size;
	}

	override fun toString(): String {
		return toString(true, true)
	}

	fun toString(showLayers: Boolean, showTransitions: Boolean): String {
		var r = ""
		if (showLayers)
			r += "\nlayers:" + layers.size

		for (l in 0 until layers.size) {

			if (showLayers) {
				r += "\nlayer:" + l
				r += layers[l]
			}

			if (showLayers && showTransitions)
				r += "\n"
			if (showTransitions) {
				r += "\ntransition:" + l + " - " + (l + 1)
				if (l < transitions.size)
					r += transitions[l]
			}
		}
		return r


	}

}


fun graph(name: String, from: Double, to: Double, f1: (Double) -> Double, f2: (Double) -> Double) {


	val height = 500;
	val width = 500;
	val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	val g = image.getGraphics()
	g.setColor(Color.BLACK)
	g.fillRect(0, 0, width, height);

	val step = (to - from) / width

	val rconvx = { i: Int -> from + i * step }
	val xratio = to - from
    val convx = { x: Double ->  (((x-from)/xratio)*width).toInt() }

	val v1 = DoubleArray(width) { i ->
		val x = rconvx(i)
		val r = f1(x)
		//println("$i->$x->$r");
		r

	}
	val v2 = DoubleArray(width) { i -> val x = from + i * step;f2(x) }

	/*
	var dMin = v1[0]
	var dMax = v1[0]
	for (d in v1) {
		if (d < dMin) dMin = d
		if (d > dMax) dMax = d
	}

	for (d in v2) {
		if (d < dMin) dMin = d
		if (d > dMax) dMax = d
	}
 */
	val dMin = -0.1
	val dMax = 1.1


	val ratio = dMax - dMin
	val convy = { y: Double -> height - 1 - (((y - dMin) / ratio) * (height - 1.0)).toInt() }


	g.setColor(Color.DARK_GRAY)
	g.drawLine(0, convy(0.0), width, convy(0.0))
	g.drawLine(convx(0.0), 0, convx(0.0), height)

	for (x in 1 until width) {


		g.setColor(Color.RED)
		g.drawLine(x - 1, convy(v1[x - 1]), x, convy(v1[x]))


		g.setColor(Color.GREEN)
		g.drawLine(x - 1, convy(v2[x - 1]), x, convy(v2[x]))

	}

	g.dispose();
	ImageIO.write(image, "png", File(name));


}

// 1 transition network that approximate identity function
fun one2one_manual_id() {
	val net = Network(intArrayOf(1, 1))

	net.transitions[0].biases = doubleArrayOf(-2.5)
	net.transitions[0].weights = arrayOf(
		doubleArrayOf(5.0)
	)

	val f = { x: Double -> x } // identity

	graphNetVsF("manual-id.png",net,f)
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

	graphNetVsF("manual-revid.png",net,f)

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

	graphNetVsF("manual-hill.png",net,f)
	
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
		"hill-costvsl1-dca0.png",-10.0,10.0,
		{ x -> net.transitions[1].weights[1][0]=x;net.batch(batch)},
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
			graphNetVsF(name+"-@" + String.format("%05d", epoch) + ".png",net,f)
			epochs2graph=graphEvery
		}
		println("epoch:" + epoch)
		cost=net.learn(0.2, batch)
		println("cost:" + cost)
	}
	graphNetVsF("name-@" + String.format("%05d", epochs) + ".png",net,f)
	graphNetVsF("name-@done" + ".png",net,f)
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
		"test-init.png",-0.1, 1.1,
		{ x -> net.feedForward(DoubleArray(1) { x })[0] },
		{ x -> f(x) }
	)

	//println("cost:" + net.learn(1.0, batch))

	for (epoch in 0 until 1000) {
		println("*****" + epoch)
		println("cost:" + net.learn(0.5, batch))

		if (epoch % 1000 == 0) {
			graph(
				"test-" + String.format("%03d", epoch) + ".png",-0.1, 1.1,
				{ x -> net.feedForward(DoubleArray(1) { x })[0] },
				{ x -> f(x) }
			)
		}

	}
	//log = true
//	println("cost:" + net.learn(1.0, batch))
	//println("cost:" + net.learn(1.0, batch))


	graph(
		"test-done.png", -0.1, 1.1,
		{ x -> net.feedForward(DoubleArray(1) { x })[0] },
		{ x -> f(x) }
	)
	
	
	
}


fun main(args: Array<String>) {
//	one2one_manual_hill();
	one2one_learn_hill();
}