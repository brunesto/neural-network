package bruno

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.Color
import java.io.File


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
	ImageIO.write(image, "png", File(name+".png"));


}