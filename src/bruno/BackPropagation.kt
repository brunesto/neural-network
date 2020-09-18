package bruno

// CAPTCHA: in kotlin until is exclusive but downTo is inclusive ???
import java.util.*
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import java.awt.Color


class BackPropagation(val net: Network) {

	

	// inner class used to store cost derivative for the layer transition
	class LayerTransitionD(inputSize: Int, outputSize: Int) {

		fun acc(that: LayerTransitionD) {
			for (j in 0 until dCostVsBiases.size) {
				dCostVsBiases[j] += that.dCostVsBiases[j]
				for (i in 0 until dCostVsWeights.size) {
					dCostVsWeights[i][j] += that.dCostVsWeights[i][j]
				}
			}

		}


		// derivatives 
		val dCostVsBiases = DoubleArray(outputSize) { 0.0 }
		val dCostVsWeights = Array<DoubleArray>(inputSize) { DoubleArray(outputSize) { 0.0 } }


		override fun toString(): String {
			val nextLayerSize = dCostVsWeights[0].size;
			var r = ""


			r += "\ncost vs bias and weight derivatives:"
			r += "\n"
			r += "    j\\i: bias" + formatRuler(dCostVsBiases.size)
			for (j in 0 until nextLayerSize) {
				r += "\n" + format(j) + ":  "
				r += format(dCostVsBiases[j])
				for (i in 0 until dCostVsWeights.size) {
					r += format(dCostVsWeights[i][j])
				}
			}
			return r
		}

	}

	// helper to accumulate an array of transition's derivatives
	fun acc(target: Array<LayerTransitionD>, src: Array<LayerTransitionD>) {
		for (l in 0 until target.size) {
			target[l].acc(src[l])
		}
	}

	fun toString(transitionsd: Array<LayerTransitionD>): String {
		var r = ""
//		if (showLayers)
//			r += "\nlayers d:" + layersd.size
//
		for (l in 0 until transitionsd.size) {

//			if (showLayers) {
//				r += "\nlayer d:" + l
//				r += layersd[l]
//			}
//
//			if (showLayers && showTransitions)
//				r += "\n"
//			if (showTransitions) {
			r += "\ntransitiond:" + l + " - " + (l + 1)
			if (l < transitionsd.size)
				r += transitionsd[l]

		}
		return r


	}


	// inner class used to store cost against individual neuron activation derivative
	// this class scope is only within the costd() function
	class LayerD(val size: Int) {

		// derivative of total cost with respect to activations
		val dCostVsdActivations = DoubleArray(size) { 0.0 }


		override fun toString(): String {
			var r = ""
			r += "\n" + dCostVsdActivations.size + " neurons \n"
			r += "#       " + formatRuler(dCostVsdActivations.size)
			r += "\n active "
			for (i in 0 until dCostVsdActivations.size)
				r += format(dCostVsdActivations[i])
			return r


		}
	}


	// costd() will accumulate derivatives within transitionsd


	// Clear the transitions weight and bias derivatives accumulators
	fun newTransitionsd(): Array<LayerTransitionD> {
//		for (l in 0 until transitionsd.size)
//			for (j in 0 until net.sizes[l + 1]) {
//
//				transitionsd[l].dCostVsBiases[j] = 0.0
//				for (i in 0 until net.sizes[l]) {
//					transitionsd[l].dCostVsWeights[i][j] = 0.0
//				}
//
//			}
		return Array(net.sizes.size - 1) { i -> LayerTransitionD(net.sizes[i], net.sizes[i + 1]) }
	}


	// Compute the gradiant
	fun costd(pair: Pair<DoubleArray, DoubleArray>): Array<LayerTransitionD> {
		val transitionsd = newTransitionsd()
		net.feedForward(pair.first)
		//println(net.toString(true, true))
		// derivative of cost vs neuron activation	
		val layersd = Array(net.sizes.size) { i -> LayerD(net.sizes[i]) };

		// Compute derivative of cost vs the last (output) layer
		val lastLayerd = layersd[net.sizes.size - 1];
		val lastLayer = net.layers[net.sizes.size - 1];
		for (i in 0 until lastLayer.size)
			lastLayerd.dCostVsdActivations[i] = 2 * lastLayer.activations[i] - 2 * pair.second[i]


		// now loop backwards for every layer pair 
		for (l in layersd.size - 1 downTo 1) { // downTo is inclusive!
			val nextLayer = net.layers[l]
			val prevLayer = net.layers[l - 1]

			val nextLayerd = layersd[l]
			val prevLayerd = layersd[l - 1]


			// transition from prevLayer to nextLayer
			val transitiond = transitionsd[l - 1]
			val transition = net.transitions[l - 1]


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
				val dCost_Vs_dAj = nextLayerd.dCostVsdActivations[j]
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
					prevLayerd.dCostVsdActivations[i] += dCost_Vs_dAi


					// chain rules to compute derivative of cost vs transition.bias[j]
					// this value is part of the gradient
					val dZj_Vs_dBj = 1;
					val dCost_Vs_dBj = dCost_Vs_dZj * dZj_Vs_dBj;
					transitiond.dCostVsBiases[j] += dCost_Vs_dBj
					//println("transition.dCostVsBiases[$j]:" + transition.dCostVsBiases[j])

					// chain rules to compute derivative of cost vs transition.weight[i,j]
					// this value is part of the gradient
					val dZj_Vs_dWij = prevLayer.activations[i]
					val dCost_Vs_dWij = dCost_Vs_dZj * dZj_Vs_dWij;

					transitiond.dCostVsWeights[i][j] += dCost_Vs_dWij
					//println("transition.dCostVsWeights[$i][$j]:" + transition.dCostVsWeights[i][j])

				}
			}
		}

		//println(toString(transitionsd))

//		File("tmp/net.dot").writeText(Dotter.dot(true,net, transitionsd, layersd, pair.second))

		return transitionsd
	}


	// update the weights and biases according to the gradiant
	fun changeNetworkWeights(m: Double, transitionsd: Array<LayerTransitionD>) {

		val onlyLastNLayers=transitionsd.size
		val till=transitionsd.size-onlyLastNLayers
		for (l in transitionsd.size-1 downTo till ) {
			val transitiond = transitionsd[l]
			val transition = net.transitions[l]
			// update bias
			for (j in 0 until transition.biases.size) {
				transition.biases[j] += m * -transitiond.dCostVsBiases[j]
			}

			// update weight
			for (i in 0 until transition.weights.size) {
				for (j in 0 until transition.weights[i].size) {
					transition.weights[i][j] += m * -transitiond.dCostVsWeights[i][j]
				}
			}
		}
	}


	// update the derivatives on a batch
	fun batch(pairs: List<Pair<DoubleArray, DoubleArray>>): Array<LayerTransitionD> {
		val transitionsdAcc = newTransitionsd()

		for (p in pairs.indices) {
			//	LOG("learn sample " + p)
			val pair = pairs[p]
			val transitionsd = costd(pair)
			acc(transitionsdAcc, transitionsd)

		}
		LOG({ " " + toString() })
		
		return transitionsdAcc
	}

	fun learn(learnRate: Double, pairs: List<Pair<DoubleArray, DoubleArray>>) {
	//	File("tmp/net.dot").writeText(Dotter.dot(false,net))
		val transitionsdAcc = batch(pairs)
//		File("tmp/net.dot").writeText(Dotter.dot(false,net, transitionsdAcc))
		changeNetworkWeights(learnRate, transitionsdAcc);
//		File("tmp/net.dot").writeText(Dotter.dot(false,net))
		println("done")
	}


}


