package daan.se.dcstool.model

import java.text.DecimalFormat

data class MGRS
(
    val zone: Int,
    val latitudeBand: LatitudeBand,
    val columnLetter: ColumnLetter,
    val rowLetter: RowLetter,
    val easting: Int,
    val northing: Int
) {
    fun print(): String {
        val e = DecimalFormat("00000").format(easting)
        val n = DecimalFormat("00000").format(northing)

        return "$zone$latitudeBand ${columnLetter.name}${rowLetter.name} $e $n"
    }

    fun toUTM(): UTM {
        val e = ((columnLetter.ordinal % 8) + 1) * 100000
        val n =0 //TODO

        return UTM(zone, latitudeBand.getHemisphere(), latitudeBand, e, n)
    }

    companion object {
        fun fromUTM(utm: UTM): MGRS {
            val col = ColumnLetter.fromZoneAndEasting(utm.zone, utm.easting)
            val row = RowLetter.fromZoneAndNorthing(utm.zone, utm.northing)

            val easting = utm.easting % 100000
            val northing = utm.northing % 100000

            return MGRS(utm.zone, utm.getLatitudeBand(), col, row, easting, northing)
        }
    }
}

enum class ColumnLetter {
    A,
    B,
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
    X,
    Y,
    Z;

    companion object {
        fun fromZoneAndEasting(zone: Int, easting: Int): ColumnLetter {
            val idx = ((zone - 1) % 3) * 8 + easting / 100000 - 1

            return ColumnLetter.values().get(idx)
        }
    }
}

enum class RowLetter {
    A,
    B,
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
    V;

    companion object {
        fun fromZoneAndNorthing(zone: Int, northing: Int): RowLetter {
            val idxOffset = if(zone % 2 == 1) 0 else 5
            val idx = (idxOffset + northing / 100000) % RowLetter.values().size

            return RowLetter.values().get(idx)
        }
    }
}