package daan.se.dcstool.model

enum class Hemisphere(val abbreviation: String, val sign: Int) {
    NORTH("N", 1),
    EAST("E", 1),
    SOUTH("S", -1),
    WEST("W", -1)
}