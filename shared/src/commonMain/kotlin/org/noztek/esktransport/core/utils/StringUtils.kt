package org.noztek.esktransport.core.utils

fun String.uppercaseFirstLetterOfEachWord(
    lowercaseRest: Boolean = true,
): String {
    var shouldUppercase = true

    return buildString(length) {
        this@uppercaseFirstLetterOfEachWord.forEach { char ->
            if (char.isLetter()) {
                append(
                    when {
                        shouldUppercase -> char.uppercaseChar()
                        lowercaseRest -> char.lowercaseChar()
                        else -> char
                    },
                )
                shouldUppercase = false
            } else {
                append(char)
                shouldUppercase = true
            }
        }
    }
}
