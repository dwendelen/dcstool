package se.daan.dcstool.model

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

data class MGRS
(
        val zone: Int,
        val latitudeBand: LatitudeBand,
        val columnLetter: ColumnLetter,
        val rowLetter: RowLetter,
        val easting: Double,
        val northing: Double
) : Coordinate {
    override fun toLaLoDegree(): LaLoDegree {
        return toUTM().toLaLoDegree()
    }

    override fun print(): String {
        val symbols = DecimalFormatSymbols()
        symbols.decimalSeparator = '.'

        val decimalFormat = DecimalFormat("00000", symbols)
        decimalFormat.roundingMode = RoundingMode.DOWN

        val e = decimalFormat.format(easting)
        val n = decimalFormat.format(northing)

        return "$zone${latitudeBand.name} ${columnLetter.name}${rowLetter.name} $e $n"
    }

    fun toUTM(): UTM {
        val e = columnLetter.toUTMEastingBase() + easting
        val n = latitudeBand.toUTMNorthingBase() + rowLetter.toUTMNorthingBase(zone) + northing

        return UTM(latitudeBand.getHemisphere(), zone, latitudeBand, e, n)
    }
}

object MGRSFactory : CoordinateFactory<MGRS> {
    override fun fromLaLoDegree(laLoDegree: LaLoDegree): MGRS {
        return fromUTM(UTMFactory.fromLaLoDegree(laLoDegree))
    }

    fun fromUTM(utm: UTM): MGRS {
        val col = ColumnLetter.fromZoneAndEasting(utm.zone, utm.easting)
        val row = RowLetter.fromZoneAndNorthing(utm.zone, utm.northing)

        val easting = utm.easting % 100000
        val northing = utm.northing % 100000

        return MGRS(utm.zone, utm.getLatitudeBand(), col, row, easting, northing)
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

    fun toUTMEastingBase(): Int {
        return ((ordinal % 8) + 1) * 100000
    }

    companion object {
        fun inZone(zone: Int): Collection<ColumnLetter> {
            val base = baseIdx(zone)

            return (base .. base + 7)
                    .map { ColumnLetter.values().get(it) }
        }

        fun fromZoneAndEasting(zone: Int, easting: Double): ColumnLetter {
            val idx = baseIdx(zone) + (easting / 100000.0).toInt() - 1

            return ColumnLetter.values().get(idx)
        }

        private fun baseIdx(zone: Int): Int {
            return ((zone - 1) % 3) * 8
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

    fun toUTMNorthingBase(zone: Int): Int {
        val idxOffset = idxOffset(zone)
        val idx = (ordinal - idxOffset) % RowLetter.values().size

        return idx * 100000
    }

    companion object {
        fun inZoneAndLatitudeBand(zone: Int, latitudeBand: LatitudeBand): Collection<RowLetter> {
            val idxOffset = idxOffset(zone)
            val startIdx = latitudeBand.firstRow.ordinal + idxOffset
            val lastIdx = latitudeBand.lastRow.ordinal + idxOffset

            return (startIdx..lastIdx)
                    .map { values()[it] }
        }

        fun fromZoneAndNorthing(zone: Int, northing: Double): RowLetter {
            val idxOffset = idxOffset(zone)
            val idx = (idxOffset + (northing / 100000.0).toInt()) % RowLetter.values().size

            return RowLetter.values().get(idx)
        }

        fun idxOffset(zone: Int): Int {
            return if (zone % 2 == 1) 0 else 5
        }
    }
}