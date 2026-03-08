package com.typ.hearforme.domain.utils

infix fun LongArray.shouldMatchLengthOf(another: LongArray): LongArray {
    return if (this.size > another.size) {
        this.copyOfRange(0, another.size)
    } else {
        this
    }
}

infix fun IntArray.shouldMatchLengthOf(another: LongArray): IntArray {
    return if (this.size > another.size) {
        this.copyOfRange(0, another.size)
    } else {
        this
    }
}