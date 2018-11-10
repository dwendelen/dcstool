package daan.se.dcstool.model

import kotlin.math.floor

enum class LatitudeBand {
    C,
    D,
    E,
    F,
    G,
    H,
    J,
    K,
    L,
    M,
    N,
    P,
    Q,
    R,
    S,
    T,
    U,
    V,
    W,
    X;

    fun getHemisphere() :Hemisphere {
        if(ordinal < N.ordinal)
            return Hemisphere.SOUTH
        else
            return Hemisphere.NORTH
    }

    companion object {
        fun getBandForLatitude(latitude: Double): LatitudeBand {
            val index = floor((latitude + 80.0) / 8.0)

            return LatitudeBand.values().get(index.toInt())
        }
    }
}