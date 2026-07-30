package org.noztek.esktransport.core.utils

/**
 * Minimal QR Code Model 2 encoder for short byte-mode kiosk payloads.
 *
 * Supports version 3, error correction level L, and mask pattern 0. This is
 * enough for the app's ASCII wallet reference payloads while staying fully
 * multiplatform and dependency-free.
 */
object QrCodeMatrix {
    private const val Version = 3
    private const val Size = Version * 4 + 17
    private const val DataCodewords = 55
    private const val EccCodewords = 15

    fun encode(payload: String): List<List<Boolean>> {
        val data = encodeData(payload)
        val ecc = reedSolomonRemainder(data, EccCodewords)
        val codewords = data + ecc
        val modules = Array(Size) { BooleanArray(Size) }
        val reserved = Array(Size) { BooleanArray(Size) }

        drawFunctionPatterns(modules, reserved)
        drawCodewords(modules, reserved, codewords)
        drawFormatBits(modules, reserved)

        return modules.map { row -> row.toList() }
    }

    private fun encodeData(payload: String): List<Int> {
        val bytes = payload.encodeToByteArray()
        require(bytes.size <= 42) { "QR payload is too long for the kiosk reference encoder." }

        val bits = mutableListOf<Int>()
        appendBits(bits, 0x4, 4)
        appendBits(bits, bytes.size, 8)
        bytes.forEach { appendBits(bits, it.toInt() and 0xFF, 8) }

        val capacityBits = DataCodewords * 8
        appendBits(bits, 0, minOf(4, capacityBits - bits.size))
        while (bits.size % 8 != 0) bits += 0

        val codewords = mutableListOf<Int>()
        bits.chunked(8).forEach { chunk ->
            codewords += chunk.fold(0) { value, bit -> (value shl 1) or bit }
        }

        var pad = 0xEC
        while (codewords.size < DataCodewords) {
            codewords += pad
            pad = if (pad == 0xEC) 0x11 else 0xEC
        }

        return codewords
    }

    private fun appendBits(bits: MutableList<Int>, value: Int, count: Int) {
        for (shift in count - 1 downTo 0) {
            bits += (value ushr shift) and 1
        }
    }

    private fun drawFunctionPatterns(
        modules: Array<BooleanArray>,
        reserved: Array<BooleanArray>,
    ) {
        drawFinder(modules, reserved, 0, 0)
        drawFinder(modules, reserved, Size - 7, 0)
        drawFinder(modules, reserved, 0, Size - 7)
        drawAlignment(modules, reserved, 22, 22)

        for (i in 8 until Size - 8) {
            setFunction(modules, reserved, 6, i, i % 2 == 0)
            setFunction(modules, reserved, i, 6, i % 2 == 0)
        }

        setFunction(modules, reserved, 4 * Version + 9, 8, true)
        reserveFormatBits(reserved)
    }

    private fun drawFinder(
        modules: Array<BooleanArray>,
        reserved: Array<BooleanArray>,
        left: Int,
        top: Int,
    ) {
        for (dy in -1..7) {
            for (dx in -1..7) {
                val row = top + dy
                val col = left + dx
                if (row !in 0 until Size || col !in 0 until Size) continue

                val isFinder = dx in 0..6 &&
                    dy in 0..6 &&
                    (dx == 0 || dx == 6 || dy == 0 || dy == 6 || (dx in 2..4 && dy in 2..4))
                setFunction(modules, reserved, row, col, isFinder)
            }
        }
    }

    private fun drawAlignment(
        modules: Array<BooleanArray>,
        reserved: Array<BooleanArray>,
        centerRow: Int,
        centerCol: Int,
    ) {
        for (dy in -2..2) {
            for (dx in -2..2) {
                val distance = maxOf(kotlin.math.abs(dx), kotlin.math.abs(dy))
                setFunction(
                    modules = modules,
                    reserved = reserved,
                    row = centerRow + dy,
                    col = centerCol + dx,
                    value = distance != 1,
                )
            }
        }
    }

    private fun reserveFormatBits(reserved: Array<BooleanArray>) {
        for (i in 0..5) reserved[8][i] = true
        reserved[8][7] = true
        reserved[8][8] = true
        reserved[7][8] = true
        for (i in 9..14) reserved[14 - i][8] = true
        for (i in 0..7) reserved[Size - 1 - i][8] = true
        for (i in 8..14) reserved[8][Size - 15 + i] = true
    }

    private fun drawCodewords(
        modules: Array<BooleanArray>,
        reserved: Array<BooleanArray>,
        codewords: List<Int>,
    ) {
        var bitIndex = 0
        var upward = true
        var right = Size - 1

        while (right > 0) {
            if (right == 6) right -= 1
            val rowRange = if (upward) Size - 1 downTo 0 else 0 until Size

            for (row in rowRange) {
                for (col in right downTo right - 1) {
                    if (reserved[row][col]) continue

                    val bit = if (bitIndex < codewords.size * 8) {
                        (codewords[bitIndex / 8] ushr (7 - bitIndex % 8)) and 1
                    } else {
                        0
                    }
                    val maskedBit = bit xor if ((row + col) % 2 == 0) 1 else 0
                    modules[row][col] = maskedBit == 1
                    bitIndex += 1
                }
            }

            upward = !upward
            right -= 2
        }
    }

    private fun drawFormatBits(
        modules: Array<BooleanArray>,
        reserved: Array<BooleanArray>,
    ) {
        val format = formatBits(errorCorrectionAndMask = 0b01000)
        for (i in 0 until 15) {
            val bit = ((format ushr i) and 1) == 1

            when (i) {
                in 0..5 -> setFunction(modules, reserved, 8, i, bit)
                6 -> setFunction(modules, reserved, 8, 7, bit)
                7 -> setFunction(modules, reserved, 8, 8, bit)
                8 -> setFunction(modules, reserved, 7, 8, bit)
                else -> setFunction(modules, reserved, 14 - i, 8, bit)
            }

            if (i < 8) {
                setFunction(modules, reserved, Size - 1 - i, 8, bit)
            } else {
                setFunction(modules, reserved, 8, Size - 15 + i, bit)
            }
        }

        setFunction(modules, reserved, 4 * Version + 9, 8, true)
    }

    private fun formatBits(errorCorrectionAndMask: Int): Int {
        var data = errorCorrectionAndMask shl 10
        val generator = 0x537
        for (shift in 14 downTo 10) {
            if (((data ushr shift) and 1) != 0) {
                data = data xor (generator shl (shift - 10))
            }
        }
        return ((errorCorrectionAndMask shl 10) or data) xor 0x5412
    }

    private fun setFunction(
        modules: Array<BooleanArray>,
        reserved: Array<BooleanArray>,
        row: Int,
        col: Int,
        value: Boolean,
    ) {
        modules[row][col] = value
        reserved[row][col] = true
    }

    private fun reedSolomonRemainder(data: List<Int>, degree: Int): List<Int> {
        val generator = reedSolomonGenerator(degree)
        val result = MutableList(degree) { 0 }

        for (byte in data) {
            val factor = byte xor result.removeAt(0)
            result += 0
            for (i in 0 until degree) {
                result[i] = result[i] xor gfMultiply(generator[i], factor)
            }
        }

        return result
    }

    private fun reedSolomonGenerator(degree: Int): List<Int> {
        val result = MutableList(degree) { 0 }
        result[degree - 1] = 1
        var root = 1

        for (i in 0 until degree) {
            for (j in 0 until degree) {
                result[j] = gfMultiply(result[j], root)
                if (j + 1 < degree) {
                    result[j] = result[j] xor result[j + 1]
                }
            }
            root = gfMultiply(root, 2)
        }

        return result
    }

    private fun gfMultiply(left: Int, right: Int): Int {
        var x = left
        var y = right
        var result = 0

        while (y != 0) {
            if ((y and 1) != 0) result = result xor x
            x = x shl 1
            if ((x and 0x100) != 0) x = x xor 0x11D
            y = y ushr 1
        }

        return result and 0xFF
    }
}
