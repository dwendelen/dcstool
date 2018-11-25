package se.daan.dcstool.model

interface Coordinate {
    fun toLaLoDegree(): LaLoDegree
    fun print(): String
}

interface CoordinateFactory<T: Coordinate> {
    fun fromLaLoDegree(laLoDegree: LaLoDegree): T
}