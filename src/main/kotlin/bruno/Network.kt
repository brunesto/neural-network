package bruno

// CAPTCHA: in kotlin until is exclusive but downTo is inclusive ???
import java.util.*
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import java.awt.Color


// a Layer transition holds the weights and biases used to compute the neuron activation of the next layer
class LayerTransition(val inputSize: Int,val  outputSize: Int) {
	var biases = DoubleArray(outputSize) { i -> if (i == 0) 0.0 else rnd.nextDouble() } // TODO:why 0 when i==0?
	var weights = Array<DoubleArray>(inputSize) {
		DoubleArray(outputSize) {
			rnd.nextDouble()*2-1.0
		}
	}


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
	
	
	fun clone():LayerTransition{
		val r =LayerTransition(inputSize,outputSize)
		for (j in 0 until outputSize){
			r.biases[j]=biases[j]
			for (i in 0 until inputSize) {
				r.weights[i][j] = weights[i][j]
			}
			
		}
		return r
	}

}

class Layer(val size: Int) {
	// neuron activations
	var activations = DoubleArray(size) { 0.0 }


	fun clone():Layer{
		val r =Layer(size)
		for (i in 0 until activations.size)
			r.activations[i]=activations[i]
		return r
	}
	
	override fun toString(): String {
		var r = ""
		r += "" + activations.size + " neurons \n"
		r += "#       " + formatRuler(activations.size)
		r += "\n active "
		for (i in 0 until activations.size)
			r += format(activations[i])
		return r


	}
}

class Network(val sizes: IntArray) {


	var layers = Array(sizes.size) { i -> Layer(sizes[i]) };
	var transitions = Array(sizes.size - 1) { i -> LayerTransition(sizes[i], sizes[i + 1]) }


	fun clone():Network {
		val r=Network(sizes)
		val me=this
		r.layers=Array(sizes.size){i->me.layers[i].clone()}
		r.transitions=Array(sizes.size-1){i->me.transitions[i].clone()}	
		return r
	}

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


	// compute the total cost of batch
	fun batchcost(pairs: List<Pair<DoubleArray, DoubleArray>>): Double {
		var cost_Acc = 0.0
		for (p in pairs.indices) {

			//	LOG("learn sample " + p)
			val pair = pairs[p]
			feedForward(pair.first)
			LOG({ " " + toString(true, false) })
			cost_Acc += cost(pair.second)
		}
		LOG({ " " + toString(false, true) })





		val r=cost_Acc / pairs.size;
		//println("batchCost:$r")
		return r
	}


	// -- to string -----------------------------------------------------------
	override fun toString(): String {
		return toString(true, true)
	}

	fun toString(showLayers: Boolean, showTransitions: Boolean): String {
		var r = ""
		if (showLayers)
			r += "\nlayers:" + layers.size

		for (l in 0 until layers.size) {

			if (showLayers) {
				r += "\nlayer:" + l+" "+ layers[l]
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

