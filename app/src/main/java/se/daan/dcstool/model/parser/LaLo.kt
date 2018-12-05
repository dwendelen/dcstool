package se.daan.dcstool.model.parser

import se.daan.dcstool.model.LaLo
import se.daan.dcstool.model.LaLoPart
import se.daan.dcstool.model.PartType

interface PartPieceInit<O: PartPieceIntermediate<*>>
    : Piece<HemiInput, O>

interface PartPieceNotInit

interface PartPieceIntermediate<O: PartPieceNotInit>
    : Piece<DigitInput, O>,
        PartPieceNotInit

interface PartPieceDone<Self: PartPieceDone<Self, P>, P : LaLoPart>
    : Piece<DigitInput, Self>,
        PartPieceNotInit {
    fun getPart(type: PartType): P
}

interface LaLoFormat12
interface LaLoFormat23
interface LaLoFormat3Done

data class LaLoFormat0(
        val lat: PartPieceInit<*>,
        val lon: PartPieceInit<*>
) : DelegatingPiece<HemiInput, PartPieceIntermediate<*>, LaLoFormat1>() {
    override val currentPiece = lat

    override fun handleCurrent(newPiece: PartPieceIntermediate<*>): LaLoFormat1 {
        return LaLoFormat1(newPiece, lon)
    }

    override fun print(): CharSequence {
        return lat.print()
    }
}

data class LaLoFormat1(
        val lat: PartPieceIntermediate<*>,
        val lon: PartPieceInit<*>
) : DelegatingPiece<DigitInput, PartPieceNotInit, LaLoFormat12>(),
        LaLoFormat12 {
    override val currentPiece = lat

    override fun handleCurrent(newPiece: PartPieceNotInit): LaLoFormat12 {
        return when (newPiece) {
            is PartPieceIntermediate<*> -> LaLoFormat1(newPiece, lon)
            is PartPieceDone<*,* > -> LaLoFormat2(newPiece, lon)
            else -> throw IllegalStateException()
        }
    }

    override fun print(): CharSequence {
        return lat.print()
    }
}

data class LaLoFormat2<P : LaLoPart>(
        val lat: PartPieceDone<*, P>,
        val lon: PartPieceInit<*>
) : Delegating2Piece<Input, LaLoFormat23, DigitInput, HemiInput, PartPieceDone<*, P>, PartPieceIntermediate<*>>(),
        LaLoFormat12,
        LaLoFormat23 {
    override val currentPiece = lat
    override val nextPiece = lon

    override fun handleCurrent(newPiece: PartPieceDone<*, P>): LaLoFormat2<P> {
        return LaLoFormat2(newPiece, lon)
    }

    override fun handleNext(newPiece: PartPieceIntermediate<*>): LaLoFormat3<P> {
        return LaLoFormat3(lat.getPart(PartType.Latitude), newPiece)
    }

    override fun print(): CharSequence {
        return "${lat.print()} ${lon.print()}"
    }
}

data class LaLoFormat3<P: LaLoPart>(
        val lat: P,
        val lon: PartPieceIntermediate<*>
) : DelegatingPiece<DigitInput, PartPieceNotInit, LaLoFormat3Done>(),
        LaLoFormat23,
        LaLoFormat3Done
{
    override val currentPiece = lon

    override fun handleCurrent(newPiece: PartPieceNotInit): LaLoFormat3Done {
        return when (newPiece) {
            is PartPieceIntermediate<*> -> LaLoFormat3(lat, newPiece)
            is PartPieceDone<*, *> -> LaLoFormatDone(lat, newPiece as PartPieceDone<*, P>)
            else -> throw IllegalStateException()
        }
    }

    override fun print(): CharSequence {
        return "${lat.print()} ${lon.print()}"
    }
}

data class LaLoFormatDone<P: LaLoPart>(
        val lat: P,
        val lon: PartPieceDone<*, P>
) : DelegatingPiece<DigitInput, PartPieceDone<*, P>, LaLoFormatDone<P>>(),
        LaLoFormat3Done,
        FinalFormat
{
    override val currentPiece = lon

    override fun handleCurrent(newPiece: PartPieceDone<*, P>): LaLoFormatDone<P> {
        return LaLoFormatDone(lat, newPiece)
    }

    override fun print(): CharSequence {
        return "${lat.print()} ${lon.print()}"
    }

    override val coordinate: LaLo<P, P>
        get() {
            return LaLo(lat, lon.getPart(PartType.Longitude))
        }
}