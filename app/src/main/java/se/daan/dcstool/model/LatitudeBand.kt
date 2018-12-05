package se.daan.dcstool.model

import kotlin.math.floor
import se.daan.dcstool.model.RowLetter

enum class LatitudeBand(val rowRepetition: Int, val firstRow: RowLetter, val lastRow: RowLetter) {
    C(0, RowLetter.M, RowLetter.A),
    D(1, RowLetter.A, RowLetter.J),
    E(1, RowLetter.J, RowLetter.T),
    F(1, RowLetter.T, RowLetter.G),
    G(2, RowLetter.G, RowLetter.R),
    H(2, RowLetter.R, RowLetter.E),
    J(3, RowLetter.E, RowLetter.P),
    K(3, RowLetter.P, RowLetter.C),
    L(4, RowLetter.C, RowLetter.M),
    M(4, RowLetter.M, RowLetter.V),
    N(0, RowLetter.A, RowLetter.J),
    P(0, RowLetter.J, RowLetter.T),
    Q(0, RowLetter.T, RowLetter.G),
    R(1, RowLetter.G, RowLetter.R),
    S(1, RowLetter.R, RowLetter.E),
    T(2, RowLetter.E, RowLetter.P),
    U(2, RowLetter.P, RowLetter.C),
    V(3, RowLetter.C, RowLetter.M),
    W(3, RowLetter.M, RowLetter.V),
    X(3, RowLetter.V, RowLetter.J);

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