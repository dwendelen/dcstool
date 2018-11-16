package daan.se.dcstool.model

import kotlin.math.floor

enum class LatitudeBand(val rowRepetition: Int) {
    C(0),
    D(1),
    E(1),
    F(1),
    G(2),
    H(2),
    J(3),
    K(3),
    L(4),
    M(4),
    N(0),
    P(0),
    Q(0),
    R(1),
    S(1),
    T(2),
    U(2),
    V(3),
    W(3),
    X(3);

    fun getHemisphere(): Hemisphere {
        if (ordinal < N.ordinal)
            return Hemisphere.SOUTH
        else
            return Hemisphere.NORTH
    }

    fun toUTMNorthingBase(): Int {
        return rowRepetition * 2000000
    }

    companion object {
        fun getBandForLatitude(latitude: Double): LatitudeBand {
            val index = floor((latitude + 80.0) / 8.0)

            return LatitudeBand.values().get(index.toInt())
        }
    }
}