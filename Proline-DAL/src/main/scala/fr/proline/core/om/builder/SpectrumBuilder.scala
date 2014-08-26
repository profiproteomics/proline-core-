package fr.proline.core.om.builder

import fr.profi.util.bytes._
import fr.profi.util.primitives._
import fr.profi.util.serialization._
import fr.proline.core.dal.tables.msi._
import fr.proline.core.om.model.msi._

/**
 * @author David Bouyssie
 *
 */
object SpectrumBuilder {
  
  protected val SpectrumCols = MsiDbSpectrumColumns
  
  def buildSpectra(eachRecord: (IValueContainer => Spectrum) => Seq[Spectrum]): Array[Spectrum] = {
    eachRecord( buildSpectrum ).toArray
  }
  
  def buildSpectrum(record: IValueContainer): Spectrum = {
    
    val r = record    
    val properties = r.getStringOption(SpectrumCols.SERIALIZED_PROPERTIES).map(ProfiJson.deserialize[SpectrumProperties](_))

    new Spectrum(
      id = toLong(r.getAny(SpectrumCols.ID)),
      title = r.getString(SpectrumCols.TITLE),
      precursorMoz = r.getDouble(SpectrumCols.PRECURSOR_MOZ),
      precursorIntensity = r.getFloatOrElse(SpectrumCols.PRECURSOR_INTENSITY,Float.NaN),
      precursorCharge = r.getInt(SpectrumCols.PRECURSOR_CHARGE),
      isSummed = r.getBoolean(SpectrumCols.IS_SUMMED),
      firstCycle = r.getIntOrElse(SpectrumCols.FIRST_CYCLE,0),
      lastCycle = r.getIntOrElse(SpectrumCols.LAST_CYCLE,0),
      firstScan = r.getIntOrElse(SpectrumCols.FIRST_SCAN,0),
      lastScan = r.getIntOrElse(SpectrumCols.LAST_SCAN,0),
      firstTime = r.getFloatOrElse(SpectrumCols.FIRST_TIME,0L),
      lastTime = r.getFloatOrElse(SpectrumCols.LAST_TIME,0L),
      mozList = bytesTodoublesOption(r.getBytesOption(SpectrumCols.MOZ_LIST)),
      intensityList = bytesTofloatsOption(r.getBytesOption(SpectrumCols.INTENSITY_LIST)),
      peaksCount = r.getInt(SpectrumCols.PEAK_COUNT),
      instrumentConfigId = r.getLong(SpectrumCols.INSTRUMENT_CONFIG_ID),
      peaklistId = r.getLong(SpectrumCols.PEAKLIST_ID)
    )

  }

}