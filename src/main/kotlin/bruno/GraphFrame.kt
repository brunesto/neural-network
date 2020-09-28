package bruno

import javax.swing.JFrame
import javax.swing.JButton
import java.awt.EventQueue
import javax.swing.JPanel
import javax.swing.JLabel
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.AbstractButton
import java.awt.event.ActionListener
import java.awt.Graphics
import java.awt.event.ComponentEvent
import java.awt.event.ComponentAdapter
import java.text.DecimalFormat
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.event.MouseAdapter
import java.util.function.Consumer
import java.util.function.BiConsumer
import java.lang.NullPointerException


class Graphable(val f: (Double) -> Double, val name: String = "", val rangep: Pair<Double, Double>? = null) {
}


fun smallestPower(target: Double, base: Double = 10.0): Double {
	var r = 0.0
	while (Math.pow(base, r) < target) r++
	while (Math.pow(base, r) > target) r--
	//println("powerMin($target,$base) returns $r ("+Math.pow(base,r)+")")

	return r

}

val colors = arrayOf(Color.RED, Color.GREEN, Color.YELLOW, Color.PINK)


data class Point(val x: Double, val y: Double) {


	fun dividedBy(o: Point): Point {
		return Point(x / o.x, y / o.y)
	}

	fun minus(o: Point): Point {
		return Point(x - o.x, y - o.y)
	}

	fun plus(o: Point): Point {
		return Point(x + o.x, y + o.y)
	}

	fun multiply(o: Point): Point {
		return Point(x * o.x, y * o.y)
	}

	fun negation(): Point {
		return Point(-x, -y)
	}

	fun x(nx: Double): Point {
		return Point(nx, y)
	}

	fun y(ny: Double): Point {
		return Point(x, ny)
	}

	companion object {
		fun id(v: Double): Point = Point(v, v)
		fun make(x: Int, y: Int): Point = Point(x.toDouble(), y.toDouble())
	}


}

class GraphFrame(title: String) : JFrame() {


	// class used to help with converting pixel to value and vice versa
	class Pix2Value(val width: Int, val reverse: Boolean = false, val minp: Double? = null, val maxp: Double? = null) {

		// no lateinit?
		val min: Double
		val max: Double
		val diff: Double
		val step: Double

		// convert from pixel to x value
		fun pix2v(i: Int): Double {
			val p = if (reverse) width - i else i
			return  min + p * step;
		}

		// convert from x to pixel
		fun v2pix(x: Double): Int {
			val fwd = (((x - min) / diff) * width).toInt()  // TOOD optimize
			if (reverse)
				return width - fwd
			return fwd
		}

		init {
			var xMin = if (minp == null) 0.0 else minp 
			var xMax = if (maxp == null) 1.0 else maxp



			if (xMin == xMax) {
				xMin *= 0.9
				xMax *= 1.1
			}

			// zero
			if (xMin == xMax) {
				xMin = -1.0
				xMax = +1.0
			}

			if (xMin > xMax) {
				val aux = xMin
				xMin = xMax
				xMax = aux
			}



			min = xMin
			max = xMax


			diff = xMax - xMin
			step = (diff) / width
		}


	}

	val buttonsPanel = JPanel()
	val graphPanel = object : JPanel() {


		fun paintHzRuler(g: Graphics) {
			// draw the hz axis
			g.setColor(Color.DARK_GRAY)

			val yAxis = height - 50;
			g.drawLine(0, yAxis, width, yAxis)
			val xScale = Math.pow(10.0, smallestPower(xPix2Value.diff / 3.0))
			var x = ((xPix2Value.min / xScale) - 1.0).toInt() * xScale
			while (x <= xPix2Value.max) {
				x += xScale
				val dy = 0//(i % 2) * 15
				g.drawLine(xPix2Value.v2pix(x), 0, xPix2Value.v2pix(x), yAxis + 5 + dy)
				g.drawString(DecimalFormat("#.####").format(x), xPix2Value.v2pix(x) - 10, yAxis + 5 + 20 + dy)


			}

			val hx = highlightPixX
			if (hx != null) {
				g.drawRect(hx - 3, yAxis - 3, 6, 6)
				g.drawString(DecimalFormat("#.####").format(xPix2Value.pix2v(hx)), hx + 5, yAxis + 10)
			}


		}

		fun paintVtRuler(fi: Int, yPix2Value: Pix2Value, g: Graphics) {

			val xAxis = 50 + fi * 30;
			g.drawLine(xAxis, 0, xAxis, height)
			val yScale = Math.pow(10.0, smallestPower(yPix2Value.diff / 3.0))
			var y = (yPix2Value.min / yScale).toInt() * yScale
			while (y <= yPix2Value.max) {
				y += yScale
				g.drawLine(xAxis - 2, yPix2Value.v2pix(y), xAxis + 2, yPix2Value.v2pix(y))
				g.drawString(DecimalFormat("#.####").format(y), xAxis - 40, yPix2Value.v2pix(y) + 10)
			}
		}

		override fun paintComponent(g: Graphics) {
			super.paintComponent(g);
			paintHzRuler(g)


			for (fi in 0 until graphables.size) {


				// grab all values
				val vs =
					DoubleArray(width) { px ->

						val x = xPix2Value.pix2v(px)
						val r = graphables[fi].f(x)
						r
					}


				// 0 ref
				g.setColor(Color.DARK_GRAY)
				g.drawLine(0, yPix2Value.v2pix(0.0), width, yPix2Value.v2pix(0.0))
				g.drawLine(xPix2Value.v2pix(0.0), 0, xPix2Value.v2pix(0.0), height)


				// Y axis		
				g.setColor(colors[fi % colors.size])
				paintVtRuler(fi, yPix2Value, g)




				for (x in 1 until width) {
					if (highlightPixX != null && highlightPixX == x) {
						g.drawRect(x - 3, yPix2Value.v2pix(vs[x]) - 3, 6, 6)
						g.drawString(DecimalFormat("#.####").format(vs[x]), x + 5, yPix2Value.v2pix(vs[x]) + 10)
					}
					g.drawLine(x - 1, yPix2Value.v2pix(vs[x - 1]), x, yPix2Value.v2pix(vs[x]))
				}


			}


		}
	}
	val statusLabel = JLabel("init")

	var graphables = ArrayList<Graphable>()
//	 {
//		Graphable({ x -> Math.cos(x) }, "cos(x)")
//	}


	fun add(g: Graphable): GraphFrame {
		graphables.add(g)
		return this
	}

	lateinit var center: Point
	lateinit var area: Point

	lateinit var xPix2Value: Pix2Value

	// ref 
	lateinit var yPix2Value: Pix2Value


	lateinit var min: Point
	lateinit var max: Point

	init {
		resetView()
		createUI(title)
	}


	fun expandArea2Resized() {

		val xmin = xPix2Value.pix2v(0)
		val ymin = yPix2Value.pix2v(0)
		val xmax = xPix2Value.pix2v(graphPanel.width.toInt())
		val ymax = yPix2Value.pix2v(graphPanel.height.toInt())
		area = Point(xmax - xmin, ymax - ymin)
		recomputeBoundaries()
	}


	fun recomputeBoundaries() {
		min = center.minus(area.dividedBy(Point.id(2.0)))
		max = center.plus(area.dividedBy(Point.id(2.0)))


		xPix2Value = Pix2Value(graphPanel.width.toInt(), false,min.x, max.x)
		yPix2Value = Pix2Value(graphPanel.height.toInt(), true,min.y, max.y)


		redraw()
		updateStatus()
	}

	fun redraw() {
		graphPanel.invalidate();
		graphPanel.repaint();
	}

	fun btnClick(button: AbstractButton, run: Runnable, tooltip: String? = null): AbstractButton {
		button.addActionListener({ _ -> run.run() })
		button.setToolTipText(tooltip)
		return button
	}



	fun zoomGraph(dz: Double,skipX:Boolean ) {
		
		val original=area;
		
		
		if (dz > 0)
			area = area.multiply(Point.id(dz));
		else
			area = area.dividedBy(Point.id(dz).negation());
		
		// restore x
		if (skipX)
			area=area.x(original.x)

		recomputeBoundaries()

	}

	fun setXRange(range: Pair<Double, Double>) {
		center = center.x((range.first + range.second) / 2.0)
		area = area.x(range.second - range.first)
		recomputeBoundaries()
	}

	fun resetView() {
		center = Point(0.0, 0.0)
		area = Point(2.0, 2.0)
		recomputeBoundaries()

	}

	fun moveGraphStep(d: Point) {
		moveGraph(area.multiply(d))
	}

	fun moveGraph(d: Point) {
		center = center.plus(d)

		recomputeBoundaries()

	}

	fun updateStatus() {
		statusLabel.setText((if (highlightPixX == null) "" else ("=>" + highlightPixX)) + " " + graphPanel.height + "x" + graphPanel.width + "px @:" + center + " a:" + area)


	}

	val zf = 1.5;
	val mr = 0.25;


	var highlightPixX: Int? = null

	private fun createUI(title: String) {

		setTitle(title)
		setLayout(BorderLayout())
		add(graphPanel, BorderLayout.CENTER)
		add(buttonsPanel, BorderLayout.NORTH)
		add(statusLabel, BorderLayout.SOUTH)

		buttonsPanel.add(btnClick(JButton("0"), { resetView() }, "reset"))
		buttonsPanel.add(btnClick(JButton("+"), { zoomGraph(-zf,false) }))
		buttonsPanel.add(btnClick(JButton("-"), { zoomGraph(+zf,false) }))
		buttonsPanel.add(btnClick(JButton("<-"), { moveGraphStep(Point(+mr, 0.0)) }))
		buttonsPanel.add(btnClick(JButton("->"), { moveGraphStep(Point(-mr, 0.0)) }))
		buttonsPanel.add(btnClick(JButton("^"), { moveGraphStep(Point(0.0, -mr)) }))
		buttonsPanel.add(btnClick(JButton("v"), { moveGraphStep(Point(0.0, +mr)) }))

		buttonsPanel.add(btnClick(JButton("r"), { recomputeBoundaries() }, "refresh"))
		graphPanel.setBackground(Color.GRAY)

		defaultCloseOperation = JFrame.EXIT_ON_CLOSE
		setSize(200, 300)
		setLocationRelativeTo(null)

		graphPanel.addComponentListener(
			object : ComponentAdapter() {
				override fun componentResized(e: ComponentEvent) {
					expandArea2Resized()
				}
			}
		);


		graphPanel.addMouseWheelListener(
			{ e ->
				val notches = e.getWheelRotation()
				if (notches < 0)
					zoomGraph(-zf,e.isControlDown())
				else
					zoomGraph(+zf,e.isControlDown())
			}
		);


		graphPanel.addMouseMotionListener(
			object : MouseMotionAdapter() {
				override fun mouseMoved(e: MouseEvent) {
					highlightPixX = e.getX()
					redraw()
					updateStatus()
				}
			}
		)



		MouseDragHelper(
			graphPanel,
			null,
			null,//			{ s, e -> this@GraphFrame.onMouseDrag(s, e,false) }, // this@GraphFrame::onMouseDrag crashes the compiler
			{ s, e -> this@GraphFrame.onMouseDrag(s, e, true) }
		)

	}


	fun onMouseDrag(e: MouseEvent, s: MouseEvent, last: Boolean) {


		val sx = xPix2Value.pix2v(s.getX())
		val ex = xPix2Value.pix2v(e.getX())
		val dx = ex - sx;

		val sy = yPix2Value.pix2v(s.getY())
		val ey = yPix2Value.pix2v(e.getY())
		val dy = ey - sy;

		val dV = Point(dx, dy)

		moveGraph(dV)
	}

	class MouseDragHelper(
		target: JPanel,
		dragStarted: Consumer<MouseEvent>?,
		dragMove: BiConsumer<MouseEvent, MouseEvent>?,
		dragEnd: BiConsumer<MouseEvent, MouseEvent>?
	) {

		var dragging = false
		var lastPressMouseEvent: MouseEvent? = null

		init {

			target.addMouseListener(
				object : MouseAdapter() {

					override fun mousePressed(e: MouseEvent) {
						println("Mouse pressed" + e);
						dragging = false
						lastPressMouseEvent = e
					}

					override fun mouseReleased(e: MouseEvent) {
						println("Mouse released" + e);
						if (dragging && dragEnd != null)
							dragEnd.accept(lastPressMouseEvent as MouseEvent, e)

						dragging = false
						lastPressMouseEvent = null
					}
				}
			)
			target.addMouseMotionListener(
				object : MouseMotionAdapter() {
					override fun mouseMoved(e: MouseEvent) {
						println("Mouse moved" + e);
					}

					override fun mouseDragged(e: MouseEvent) {
						println("Mouse dragged" + e);


						if (dragging == false && dragStarted != null) {
							// the kotlin way
							val lpme = lastPressMouseEvent
							if (lpme == null)
								throw NullPointerException();
							dragStarted.accept(lpme)


						}
						dragging = true

						// the real way
						if (dragMove != null)
							dragMove.accept(lastPressMouseEvent as MouseEvent, e)
					}


				}
			)
		}

	}

	fun interactive() {
		isVisible = true
	}
}

fun main() {
	EventQueue.invokeLater({ GraphFrame("Simple").interactive() })


}