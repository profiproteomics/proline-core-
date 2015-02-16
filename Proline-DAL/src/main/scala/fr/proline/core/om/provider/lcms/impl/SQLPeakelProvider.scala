package fr.proline.core.om.provider.lcms.impl

import scala.collection.mutable.ArrayBuffer
import fr.profi.jdbc.ResultSetRow
import fr.proline.context.DatabaseConnectionContext
import fr.proline.core.dal.{ DoJDBCWork, DoJDBCReturningWork }
import fr.proline.core.dal.tables.SelectQueryBuilder._
import fr.proline.core.dal.tables.SelectQueryBuilder1
import fr.proline.core.dal.tables.lcms._
import fr.proline.core.om.model.lcms._
import fr.profi.util.sql._
import fr.profi.util.primitives._
import fr.profi.util.serialization.ProfiJson
import scala.collection.mutable.HashMap

class SQLPeakelProvider(val lcmsDbCtx: DatabaseConnectionContext) {
  
  val PeakelCols = LcmsDbPeakelColumns
  val PeakelItemCols = LcmsDbFeaturePeakelItemColumns
  
  def getPeakels( peakelIds: Seq[Long] ): Array[Peakel] = {
    if( peakelIds.isEmpty ) return Array()

    DoJDBCReturningWork.withEzDBC(lcmsDbCtx, { ezDBC =>
      
      // Build peakels SQL query
      val peakelQuery = new SelectQueryBuilder1(LcmsDbPeakelTable).mkSelectQuery( (t,c) =>
        List(t.*) -> "WHERE "~ t.ID ~" IN ("~ peakelIds.mkString(",") ~")"
      )
      
      // Load peakels
      ezDBC.select( peakelQuery ) { this.buildPeakel(_) } toArray
    })

  }

  def getRawMapPeakels(rawMapIds: Seq[Long]): Array[Peakel] = {
    if( rawMapIds.isEmpty ) return Array()

    DoJDBCReturningWork.withEzDBC(lcmsDbCtx, { ezDBC =>
      
      val mapIdsStr = rawMapIds.mkString(",")
      
      // Check that provided map ids correspond to raw maps
      val nbMaps = ezDBC.selectInt("SELECT count(id) FROM raw_map WHERE id IN (" + mapIdsStr + ")")
      require(nbMaps == rawMapIds.length, "map ids must correspond to existing run maps")
      
      // Build peakels SQL query
      val peakelQuery = new SelectQueryBuilder1(LcmsDbPeakelTable).mkSelectQuery( (t,c) =>
        List(t.*) -> "WHERE "~ t.MAP_ID ~" IN ("~ rawMapIds.mkString(",") ~")"
      )
      
      // Load peakels
      ezDBC.select( peakelQuery ) { this.buildPeakel(_) } toArray
    })

  }
  
  def buildPeakel( peakelRecord: ResultSetRow ): Peakel = {
    
    val r = peakelRecord

   // Read and deserialize peaks
    val peaksAsBytes = r.getBytes(PeakelCols.PEAKS)
    val lcMsPeaks = org.msgpack.ScalaMessagePack.read[Array[LcMsPeak]](peaksAsBytes)
    
    // Read and deserialize properties
    val propsAsJSON = r.getStringOption(PeakelCols.SERIALIZED_PROPERTIES)
    val propsOpt = propsAsJSON.map( ProfiJson.deserialize[PeakelProperties](_) )
    
    Peakel(
      id = r.getLong(PeakelCols.ID),
      moz = r.getDouble(PeakelCols.MOZ),
      elutionTime = toFloat(r.getAny(PeakelCols.ELUTION_TIME)),
      apexIntensity = toFloat(r.getAny(PeakelCols.APEX_INTENSITY)),
      area = toFloat(r.getAny(PeakelCols.APEX_INTENSITY)),
      duration = toFloat(r.getAny(PeakelCols.DURATION)),
      //fwhm = r.getAnyOption(PeakelCols.FWHM).map(toFloat(_)),
      isOverlapping = toBoolean(r.getAny(PeakelCols.IS_OVERLAPPING)),
      featuresCount = r.getInt(PeakelCols.FEATURE_COUNT),
      peaks = lcMsPeaks,
      firstScanId = r.getLong(PeakelCols.FIRST_SCAN_ID),
      lastScanId = r.getLong(PeakelCols.LAST_SCAN_ID),
      apexScanId = r.getLong(PeakelCols.APEX_SCAN_ID),
      rawMapId = r.getLong(PeakelCols.MAP_ID),
      properties = propsOpt
    )
    
  }
  
  def getPeakelItems( peakelIds: Seq[Long], loadPeakels: Boolean = true ): Array[FeaturePeakelItem] = {
    if( peakelIds.isEmpty ) return Array()
    
    DoJDBCReturningWork.withEzDBC(lcmsDbCtx, { ezDBC =>
      
      // Build peakels SQL query
      val peakelItemQuery = new SelectQueryBuilder1(LcmsDbFeaturePeakelItemTable).mkSelectQuery( (t,c) =>
        List(t.*) -> "WHERE "~ t.PEAKEL_ID ~" IN("~ peakelIds.mkString(",") ~") "
      )
      
      // Load peakel items
      val peakelItems = ezDBC.select( peakelItemQuery ) { this.buildFeaturePeakelItem(_) } toArray
      
      if( loadPeakels ) {
        
        // Load peakels and map them bey their id
        val peakelById = this.getPeakels(peakelIds).map( p => p.id -> p ).toMap
        
        // Attach peakels to peakel items
        for( peakelItem <- peakelItems ) {
          peakelItem.peakelReference = peakelById(peakelItem.peakelReference.id)
        }
      }
      
      peakelItems
    })

  }
  
  def getFeaturePeakelItems( featureIds: Seq[Long], loadPeakels: Boolean = true ): Array[FeaturePeakelItem] = {
    if( featureIds.isEmpty ) return Array()
    
    DoJDBCReturningWork.withEzDBC(lcmsDbCtx, { ezDBC =>
      
      // Build peakels SQL query
      val peakelItemQuery = new SelectQueryBuilder1(LcmsDbFeaturePeakelItemTable).mkSelectQuery( (t,c) =>
        List(t.*) -> "WHERE "~ t.FEATURE_ID ~" IN("~ featureIds.mkString(",") ~") "
      )
      
      // Load peakel items
      val peakelItems = ezDBC.select( peakelItemQuery ) { this.buildFeaturePeakelItem(_) } toArray
      
      if( loadPeakels ) {
        
        val peakelIds = peakelItems.map( _.peakelReference.id )
        
        // Load peakels and map them bey their id
        val peakelById = this.getPeakels(peakelIds).map( p => p.id -> p ).toMap
        
        // Attach peakels to peakel items
        for( peakelItem <- peakelItems ) {
          peakelItem.peakelReference = peakelById(peakelItem.peakelReference.id)
        }
      }
      
      peakelItems
    })

  }
  
  def getRawMapPeakelItems(rawMapIds: Seq[Long], loadPeakels: Boolean = true): Array[FeaturePeakelItem] = {
    if( rawMapIds.isEmpty ) return Array()
    
    DoJDBCReturningWork.withEzDBC(lcmsDbCtx, { ezDBC =>
      
      val mapIdsStr = rawMapIds.mkString(",")
      
      // Build feature peakel items SQL query
      val peakelItemQuery = new SelectQueryBuilder1(LcmsDbFeaturePeakelItemTable).mkSelectQuery( (t,c) =>
        List(t.*) -> "WHERE "~ t.MAP_ID ~" IN ("~ rawMapIds.mkString(",") ~")"
      )
      
      val peakelItems = ezDBC.select( peakelItemQuery ) { this.buildFeaturePeakelItem(_) } toArray
      
      if( loadPeakels ) {
        
        // Load peakels and map them bey their id
        val peakelById = this.getRawMapPeakels(rawMapIds).map( p => p.id -> p ).toMap
        
        // Attach peakels to peakel items
        for( peakelItem <- peakelItems ) {
          peakelItem.peakelReference = peakelById(peakelItem.peakelReference.id)
        }
      }
      
      peakelItems
    })
    
  }
  
  def buildFeaturePeakelItem( itemRecord: ResultSetRow ): FeaturePeakelItem = {
    
    val r = itemRecord
    
    // Read and deserialize properties
    val propsAsJSON = r.getStringOption(PeakelItemCols.SERIALIZED_PROPERTIES)
    val propsOpt = propsAsJSON.map( ProfiJson.deserialize[FeaturePeakelItemProperties](_) )
    
    FeaturePeakelItem(
      featureReference = FeatureIdentifier( r.getLong(PeakelItemCols.FEATURE_ID) ),
      peakelReference = PeakelIdentifier( r.getLong(PeakelItemCols.PEAKEL_ID) ),
      isotopeIndex = r.getInt(PeakelItemCols.ISOTOPE_INDEX),
      // FIXME: this value this should be retrieved from the database
      // At the moment this information can be retrieved from Feature properties
      isBasePeakel = false,
      properties = propsOpt
    )
  }

}
