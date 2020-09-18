package bruno

import java.util.Random

// -- math --------------------------------------------------

val rnd = Random(8)


fun sigmoid(z: Double): Double {
	return 1 / (1 + Math.exp(-z))
}

//  Derivative of the sigmoid function	
fun sigmoidDerivative(z: Double): Double {
	return sigmoid(z) * (1 - sigmoid(z))
}


// -- log --------------------------------------------------

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