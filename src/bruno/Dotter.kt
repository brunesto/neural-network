package bruno

import bruno.BackPropagation.LayerTransitionD
import bruno.BackPropagation.LayerD

// export 2 dot format
object Dotter {
	
	fun activation2color(ap: Double): String {
		val a = (ap * 255).toInt()
		val cr = 205
		val cg = Math.max(0,205 - a)
		val cb = Math.max(0,205 - a)
		
		val retVal="#" + String.format("%02X", cr) + String.format("%02X", cg) + String.format("%02X", cb)
		println("activation2color($ap) = $retVal")
		return retVal
	}
	
	val TABLESTART="<table BORDER=\"0\">"	
	val TABLEEND="</table>"

	fun dcost2text(prefix: String, d: Double?): String {
		if (d == null)
			return ""
		val color = if (d > 0) "green" else "blue"
		return "<font point-size=\"10\" color=\"" + color + "\">" + prefix + String.format("%4.3f", d) + "</font>"
	}

	fun dot(
		showActivations:Boolean,
		net: Network,
		transitiond: Array<LayerTransitionD>? = null,
		activationsd: Array<LayerD>? = null,
		expected: DoubleArray? = null
	): String {

		
		val n2string = { l: Int, i: Int -> "a_" + l + "_" + i }


		var r = "";
		r += "digraph g {"
		r += "\n graph [fontname=\"helvetica\",fontcolor=blue,fontsize=6];" // aint working?
		r += "\n// activations "
		for (l in 0 until net.layers.size) {
			val layer = net.layers[l];


			for (j in 0 until layer.size) {


				var tinfo = if (l > 0) {

					val prevlayer = net.layers[l - 1];
					val transition = net.transitions[l - 1];
					var za = 0.0;
					for (i in 0 until prevlayer.size) {
						val a = prevlayer.activations[i]
						val wij = transition.weights[i][j]
						za += a * wij
					}


					val bias = "<br/>b=" + String.format("%4.3f", transition.biases[j])

					val dbiass = dcost2text("<br/>bd=", transitiond?.get(l - 1)?.dCostVsBiases?.get(j))
					val zas = if (showActivations) "<font point-size=\"10\">" + "<br/>z=b+" + String.format(
						"%4.3f",
						za
					) + "</font>" else ""
					bias + dbiass + zas
				} else ""


				val color = if (showActivations) activation2color(layer.activations[j]) else "white"
				//00ff00"//""+(cr)+","+(cg)+","+(cb)//"+layer.activations[j]+","+layer.activations[j]

				val dcas = dcost2text("<br/>ad=", activationsd?.get(l)?.dCostVsdActivations?.get(j))
				val ass = (if (showActivations) {
					"<font point-size=\"10\">" +
							"<br/>a=" + String.format("%4.3f", layer.activations[j]) +
							"</font>"
				} else {
					""
				})
				r += "\n" + n2string(l, j) + " [style=filled,fillcolor =\"" + color + "\" ,label=<" +
						"[" + l + "][" + j + "]" +
						tinfo +
						ass +
						dcas +
						">" +

						"];"
			}
		}

		if (expected != null) {
			for (i in 0 until expected.size) {
				r += "\nexpected_" + i +
						" [label=\"" + String.format("%4.3f", expected[i]) +
						"\"" +
						"];"
			}

		}


		r += "\n// transitions "
		for (l in 1 until net.layers.size) {
			r += "\n// transitions from " + (l - 1) + " to " + l
			val prevlayer = net.layers[l - 1];
			val nextlayer = net.layers[l];
			val transition = net.transitions[l - 1];

			for (j in 0 until nextlayer.size) {
				var transitionWeightAcc = 0.0
				for (i in 0 until prevlayer.size)
					transitionWeightAcc += Math.abs(transition.weights[i][j])


				for (i in 0 until prevlayer.size) {
					val tw = transition.weights[i][j]
					val twn = tw / transitionWeightAcc // normalized transition weight 0..1

					val color = if (showActivations) activation2color(prevlayer.activations[i]) else "black"

					val ws = "w=" + String.format("%4.3f", tw) + " (" + (100 * twn).toInt() + "%)";

					val dw = dcost2text("<br/>cd=", transitiond?.get(l - 1)?.dCostVsWeights?.get(i)?.get(j))

					val wa = if (showActivations) {
						"<br/><font point-size=\"10\">" +
								"wa=" + String.format("%4.3f", prevlayer.activations[i] * tw) +
								"</font>"
					} else ""

					r += "\n" + n2string(l - 1, i) + "->" + n2string(l, j) +
							"[label=<" + ws + wa + dw +
							">, color=\"" + color + "\",penwidth=" + (Math.abs(twn) * 5) + "];"
				}
			}
		}
		
		
		if (expected != null) {
			for (i in 0 until expected.size) {
				val diff=expected[i]-net.layers[net.layers.size - 1].activations[i];
				r += "\n"+n2string(net.layers.size - 1, i)+" -> expected_" + i +
						" [label=<"+
						"d=" + String.format("%4.3f", diff) +
						"<br/>c=" + String.format("%4.3f", diff*diff) +
						">" +
						"];"
			}

		}

		
		
		
		
		r += "}";
		return r
	}
}