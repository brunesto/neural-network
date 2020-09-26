package bruno

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.Color
import java.io.File
import java.text.DecimalFormat


// return the min and max values found in v1
// v1 must contain at least one element
fun getRange(v1: DoubleArray): Pair<Double, Double> {
	var dMin = v1[0]
	var dMax = v1[0]
	for (d in v1) {
		if (d < dMin) dMin = d
		if (d > dMax) dMax = d
	}
	return Pair(dMin, dMax)
}

fun range(v: DoubleArray): Pair<Double, Double> {
	var dMin = v[0]
	var dMax = v[0]
	for (d in v) {
		if (d < dMin) dMin = d
		if (d > dMax) dMax = d
	}
	return Pair(dMin, dMax)
}

class Graphable(val f: (Double) -> Double, val name: String = "", val rangep: Pair<Double, Double>? = null) {


}

fun smallestPower(target: Double, base: Double = 10.0): Double {
	var r = 0.0
	while (Math.pow(base, r) < target) r++
	while (Math.pow(base, r) > target) r--
	//println("powerMin($target,$base) returns $r ("+Math.pow(base,r)+")")

	return r
}

fun graph(
	name: String,
	xrange: Pair<Double, Double>,
	vararg graphables: Graphable
) {

	val colors = arrayOf(Color.RED, Color.GREEN, Color.YELLOW, Color.PINK)

	var xMin = if (xrange == null) 0.0 else xrange.first
	var xMax = if (xrange == null) 1.0 else xrange.second



	if (xMin == xMax) {
		xMin *= 0.9
		xMax *= 1.1
	}

	// zero
	if (xMin == xMax) {
		xMin = -1.0
		xMax = +1.0
	}


	val xDiff = xMax - xMin
	//val xScale = Math.pow(10.0, smallestPower(xDiff) - 1)


	val height = 500;
	val width = 500;
	val image = BufferedImage(width + 100, height, BufferedImage.TYPE_INT_ARGB);
	val g = image.getGraphics()
	g.setColor(Color.BLACK)
	g.fillRect(0, 0, width, height);


	val step = (xMax - xMin) / width

	// convert from pixel to x value
	val pix2x = { i: Int -> xMin + i * step }
	// convert from x to pixel
	val x2pix = { x: Double -> (((x - xMin) / xDiff) * width).toInt() }


// draw the horizontal axis

	// X axis
	g.setColor(Color.DARK_GRAY)

	val yAxis = height - 50;


	val xScale = Math.pow(10.0, smallestPower(xDiff / 3.0))
	var x = (xMin / xScale).toInt() * xScale
	while (x <= xMax) {
		x += xScale


//	for (i in 0 until 12) {
		val dy = 0//(i % 2) * 15
//		val x = xfrom + xscale * i
		g.drawLine(x2pix(x), 0, x2pix(x), yAxis + 5 + dy)
		g.drawString(DecimalFormat("#.####").format(x), x2pix(x) - 10, yAxis + 5 + 20 + dy)
	}


	g.setColor(Color.BLACK)
	g.fillRect(width, 0, 100, height);
	g.setColor(Color.GRAY)
	g.drawLine(width, 0, width, height);
	for (fi in 0 until graphables.size) {


		// grab all values
		val vs =
			DoubleArray(width) { px ->
				val x = pix2x(px)
				val r = graphables[fi].f(x)
				r
			}


		// compute yrange
		val yrange = graphables[fi].rangep ?: range(vs)
		var yMin = yrange.first
		var yMax = yrange.second

		if (yMin == yMax) {
			yMin *= 0.9
			yMax *= 1.1
		}

		// zero
		if (yMin == yMax) {
			yMin = -1.0
			yMax = +1.0
		}


		val yDiff = yMax - yMin
		val y2pix = { y: Double -> height - 1 - (((y - yMin) / yDiff) * (height - 1.0)).toInt() }


		// label
		val yLabel = 20 + fi * 50
		g.setColor(colors[fi % colors.size])
		g.drawString(graphables[fi].name, width + 5, yLabel)
		g.drawString("min:" + DecimalFormat("#.####").format(yMin), width + 5, yLabel + 15)
		g.drawString("max:" + DecimalFormat("#.####").format(yMax), width + 5, yLabel + 30)


		// 0 ref
		g.setColor(Color.DARK_GRAY)
		g.drawLine(0, y2pix(0.0), width, y2pix(0.0))
		g.drawLine(x2pix(0.0), 0, x2pix(0.0), height)


		// Y axis		
		g.setColor(colors[fi % colors.size])
		val xAxis = 50 + fi * 30;

		val yScale = Math.pow(10.0, smallestPower(yDiff / 3.0))
		var y = (yMin / yScale).toInt() * yScale
		while (y <= yMax) {
			y += yScale
			g.drawLine(xAxis - 2, y2pix(y), xAxis + 2, y2pix(y))
			g.drawString(DecimalFormat("#.####").format(y), xAxis - 40, y2pix(y) + 10)
		}

		for (x in 1 until width) {

			g.drawLine(x - 1, y2pix(vs[x - 1]), x, y2pix(vs[x]))
		}


	}


	//val v2 = DoubleArray(width) { i -> val x = from + i * step;f2(x) }

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


	//	g.setColor(Color.GREEN)
//		g.drawLine(x - 1, convy(v2[x - 1]), x, convy(v2[x]))


	g.dispose();
	ImageIO.write(image, "png", File(name + ".png"));


}