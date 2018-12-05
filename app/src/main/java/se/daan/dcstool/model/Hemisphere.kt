package se.daan.dcstool.model

enum class Hemisphere(val abbreviation: Char, val sign: Int) {
    NORTH('N', 1),
    EAST('E', 1),
    SOUTH('S', -1),
    WEST('W', -1)
}