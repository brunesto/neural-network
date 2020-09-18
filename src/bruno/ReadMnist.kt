package bruno

import java.io.File
import java.lang.IllegalArgumentException
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

/**
read the mnist images and labels (uncompressed)
@see http://yann.lecun.com/exdb/mnist/

 */
class ReadMnist(val imagesFile: String, val labelsFile: String) {


	/**
	 * labels  [0..9]
	 */
	lateinit var labels: IntArray;

	/**
	 * each pixel is a double [0...1]
	 */
	lateinit var images: MutableList<DoubleArray>;


	init {
		readLabels()
		readImages()

	}

	/**
 	  helper function - quick hack read up to 8 bytes as a long in most significant byte first
 	  negative values are not supported
	 */
	fun extract(container: ByteArray, i: Int, s: Int): Long {
		var acc: Long = 0;
		for (d in 0 until s) {
			acc *= 256
			acc += container[i + d]
		}
		return acc;
	}

	fun readLabels() {
		val t10kLabels: ByteArray = File(labelsFile).readBytes()


		// read the labels
		val magic = extract(t10kLabels, 0, 4).toInt()
		if (magic != 0x00000801) throw IllegalArgumentException("" + magic)
		val size = extract(t10kLabels, 4, 4).toInt()
		println("size:" + size)

		labels = IntArray(size)
		for (i in 0 until size) {
			labels[i] = t10kLabels[i + 8].toInt()
		}

	}


	fun readImages() {
		val t10kImages: ByteArray = File(imagesFile).readBytes()


		// read images
		val magic = extract(t10kImages, 0, 4).toInt()
		if (magic != 0x00000803) throw IllegalArgumentException("" + magic)

		val size = extract(t10kImages, 4, 4).toInt()
		val x = extract(t10kImages, 8, 4).toInt()
		val y = extract(t10kImages, 12, 4).toInt()
		println("size:$size,x:$x y:$y")

		images = mutableListOf<DoubleArray>()

		var c = 16
		for (i in 0 until size) {
			var image = DoubleArray(x * y)
			for (p in 0 until x * y) {
				val ub: Int = t10kImages[c].toUByte().toInt(); // unsigned byte 0...255
				image[p] = ub / 255.0 // [0..1]
				c++
			}
			images.add(image)

		}


	}


	/**
	 * helper function to dump the nth image as a png file
	 */
	fun saveAsPng(fileNameRoot: String, n: Int) {
		val w = 28
		val h = 28

		val image = images[n];
		val label = labels[n];


		val buffedImage = BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
		var c = 0;
		for (y in 0 until h) {
			for (x in 0 until w) {
				val v = 1 - image[c]
				buffedImage.setRGB(x, y, (v * 65535).toInt())
				c++
			}
		}
		ImageIO.write(buffedImage, "png", File(fileNameRoot + "" + n + "-" + label + ".png"))
	}

}

fun main(args: Array<String>) {


	val r = ReadMnist("data/t10k-images-idx3-ubyte", "data/t10k-labels-idx1-ubyte")

	for (i in 0..20)
		r.saveAsPng("/tmp/", i)


}