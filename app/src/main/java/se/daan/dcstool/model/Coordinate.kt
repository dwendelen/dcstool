package se.daan.dcstool.model

interface Coordinate {
    fun toLaLoDegree(): LaLo<DegreeLaPart, DegreeLoPart>
    fun print(): String
}

interface CoordinateFactory<T : Coordinate> {
    fun fromLaLoDegree(laLoDegree: LaLo<DegreeLaPart, DegreeLoPart>): T
}