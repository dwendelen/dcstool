package daan.se.dcstool.model

import daan.se.dcstool.model.Hemisphere.*
import java.text.DecimalFormat
import kotlin.math.*

/*
 * Formula: https://en.wikipedia.org/wiki/Universal_Transverse_Mercator_coordinate_system
 * To create test data: http://www.rcn.montana.edu/Resources/Converter.aspx
 */
data class UTM
(
        val hemisphere: Hemisphere,
        val zone: Int,
        private val latitudeBand: LatitudeBand?,
        val easting: Double,
        val northing: Double
) : Coordinate {
    override fun print(): String {
        val e = DecimalFormat("000000").format(easting)
        val n = DecimalFormat("0000000").format(northing)
        val latBand = if (latitudeBand != null) latitudeBand.name else ""

        return "${hemisphere.abbreviation} $zone$latBand $e $n"
    }

    fun getLatitudeBand(): LatitudeBand {
        if (latitudeBand != null) {
            return latitudeBand
        } else {
            val withLat = UTMFactory.fromLaLoDegree(toLaLoDegree())

            return withLat.latitudeBand!!
        }
    }

    override fun toLaLoDegree(): LaLoDegree {
        val N0: Double = if (hemisphere == NORTH) 0.0 else 10000000.0

        val e = (northing - N0) / (k0 * A)
        val n = (easting - E0) / (k0 * A)

        val sumE = b1 * sin(2 * e) * cosh(2 * n) +
                b2 * sin(4 * e) * cosh(4 * n) +
                b3 * sin(6 * e) * cosh(6 * n)
        val ee = e - sumE

        val sumN = b1 * cos(2 * e) * sinh(2 * n) +
                b2 * cos(4 * e) * sinh(4 * n) +
                b3 * cos(6 * e) * sinh(6 * n)
        val nn = n - sumN

        val X = asin(sin(ee) / cosh(nn))

        val phi = X + c1 * sin(2 * X) + c2 * sin(4 * X) + c3 * sin(6 * X)
        val delta0 = (zone * 6 - 180 - 3) / 180.0 * PI
        val delta = delta0 + atan(sinh(nn) / cos(ee))

        val lonHemisphere = if (delta < 0) WEST else EAST
        val lat = abs(phi * 180 / PI)
        val lon = lonHemisphere.sign * delta * 180 / PI

        return LaLoDegree(hemisphere, lat, lonHemisphere, lon)
    }
}

object UTMFactory : CoordinateFactory<UTM> {
    override fun fromLaLoDegree(laLoDegree: LaLoDegree): UTM {
        val lat = laLoDegree.latitudeDegrees * laLoDegree.latitudeHemisphere.sign
        val lon = laLoDegree.longitudeDegrees * laLoDegree.longitudeHemisphere.sign

        val zone = floor((lon + 180.0) / 6.0).toInt() + 1
        val latitudeBand = LatitudeBand.getBandForLatitude(lat)

        val phi = lat / 180.0 * PI
        val delta = lon / 180.0 * PI
        val delta0 = (zone * 6 - 180 - 3) / 180.0 * PI

        val N0: Double = if (laLoDegree.latitudeHemisphere == NORTH) 0.0 else 10000000.0
        val t = sinh(atanh(sin(phi)) - _2vn_1_n * atanh(_2vn_1_n * sin(phi)))
        val ee = atan(t / cos(delta - delta0))
        val nn = atanh(sin(delta - delta0) / sqrt(1 + (t * t)))


        val sumE = a1 * cos(2 * ee) * sinh(2 * nn) +
                a2 * cos(4 * ee) * sinh(4 * nn) +
                a3 * cos(6 * ee) * sinh(6 * nn)
        val E = E0 + k0 * A * (nn + sumE)

        val sumN = a1 * sin(2 * ee) * cosh(2 * nn) +
                a2 * sin(4 * ee) * cosh(4 * nn) +
                a3 * sin(6 * ee) * cosh(6 * nn)
        val N = N0 + k0 * A * (ee + sumN)

        return UTM(laLoDegree.latitudeHemisphere, zone, latitudeBand, E, N)
    }
}

const val a: Double = 6378137.0
const val f: Double = 1.0 / 298.257223563
const val k0: Double = 0.9996
const val E0: Double = 500000.0

const val n = f / (2.0 - f)
const val A = (a / (1.0 + n)) * (1.0 + n * n / 4.0 + n * n * n * n / 64.0)

const val a1 = 1.0 / 2.0 * n - 2.0 / 3.0 * n * n + 5.0 / 16.0 * n * n * n
const val a2 = 13.0 / 48.0 * n * n - 3.0 / 5.0 * n * n * n
const val a3 = 61.0 / 240.0 * n * n * n

const val b1 = 1.0 / 2.0 * n - 2.0 / 3.0 * n * n + 37.0 / 96.0 * n * n * n
const val b2 = 1.0 / 48.0 * n * n + 1.0 / 15.0 * n * n * n
const val b3 = 17.0 / 480.0 * n * n * n

const val c1 = 2.0 * n - 2.0 / 3.0 * n * n - 2 * n * n * n
const val c2 = 7.0 / 3.0 * n * n - 8.0 / 5.0 * n * n * n
const val c3 = 56.0 / 15.0 * n * n * n

val _2vn_1_n: Double = (2.0 * sqrt(n)) / (1.0 + n)