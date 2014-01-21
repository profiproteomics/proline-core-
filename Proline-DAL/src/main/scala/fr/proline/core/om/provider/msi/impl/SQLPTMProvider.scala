package fr.proline.core.om.provider.msi.impl

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

import fr.proline.context.DatabaseConnectionContext
import fr.proline.core.dal.DoJDBCReturningWork
import fr.proline.core.om.builder.PtmDefinitionBuilder
import fr.proline.core.om.model.msi.PtmDefinition
import fr.proline.core.om.model.msi.PtmLocation
import fr.proline.core.om.provider.msi.IPTMProvider
import fr.proline.util.primitives._

class SQLPTMProvider(val psDbCtx: DatabaseConnectionContext) extends IPTMProvider {
  
  /*
  /** Returns a map */
  lazy val ptmSpecificityMap: Map[Int,PtmSpecificity] = {
      
    // Load PTM specificities
    val ptmSpecifs = psDb.getOrCreateTransaction.select( "SELECT * FROM ptm_specificity" ) { r => 
      val rs = r.rs
      val resStr = rs.getString("residue");
      val resChar = if( resStr != null ) resStr.charAt(0) else '\0'
        
      new PtmSpecificity( id = rs.getInt("id"),
                          location = rs.getString("location"), 
                          residue = resChar,
                          ptmId = rs.getInt("ptm_id")
                          )
      
      // TODO: load classification field
    }
    
    // Map ptmSpecifs by their id
    val mapBuilder = scala.collection.immutable.Map.newBuilder[Int,PtmSpecificity]
    for( ptmSpecif <- ptmSpecifs ) { mapBuilder += ( ptmSpecif.id -> ptmSpecif ) }
    mapBuilder.result()
    
  }*/

  /** Returns a map */
  lazy val ptmDefinitionById: Map[Long, PtmDefinition] = {
    
    DoJDBCReturningWork.withEzDBC(psDbCtx, { psEzDBC =>

      var ptmColNames: Seq[String] = null
      val ptmMapBuilder = scala.collection.immutable.Map.newBuilder[Long, Map[String, Any]]
  
      // Load PTM records
      psEzDBC.selectAndProcess("SELECT * FROM ptm") { r =>
  
        if (ptmColNames == null) { ptmColNames = r.columnNames }
  
        // Build the PTM record
        val ptmRecord = ptmColNames.map(colName => (colName -> r.nextAnyRefOrElse(null))).toMap
        val ptmId: Long = toLong(ptmRecord("id"))
        ptmMapBuilder += (ptmId -> ptmRecord)
  
      }
  
      val ptmRecordById = ptmMapBuilder.result()
  
      // Load PTM evidence records   
      var ptmEvidColNames: Seq[String] = null
  
      // Execute SQL query to load PTM evidence records
      val ptmEvidRecords = psEzDBC.select("SELECT * FROM ptm_evidence") { r =>
  
        if (ptmEvidColNames == null) { ptmEvidColNames = r.columnNames }
  
        // Build the PTM record
        var ptmEvidRecord = new collection.mutable.HashMap[String, Any]
        ptmEvidColNames foreach { colName => ptmEvidRecord.put(colName, r.nextAnyRefOrElse(null)) }
        // var ptmEvidRecord = ptmEvidColNames.map( colName => ( colName -> r.nextAnyRef.get ) ).toMap
  
        // Fix is_required boolean field
        if (ptmEvidRecord("is_required") == "true") { ptmEvidRecord("is_required") = true }
        else { ptmEvidRecord("is_required") = false }
  
        ptmEvidRecord.toMap
  
      }
  
      // Group PTM evidences by PTM id
      val ptmEvidRecordsByPtmId = ptmEvidRecords.groupBy(v => toLong(v.get("ptm_id").get))
  
      var ptmSpecifColNames: Seq[String] = null
      val ptmDefMapBuilder = scala.collection.immutable.Map.newBuilder[Long, PtmDefinition]
  
      // Load PTM specificity records
      psEzDBC.selectAndProcess("SELECT * FROM ptm_specificity") { r =>
  
        if (ptmSpecifColNames == null) { ptmSpecifColNames = r.columnNames }
  
        // Build the PTM specificity record
        val ptmSpecifRecord = ptmSpecifColNames.map(colName => (colName -> r.nextAnyRefOrElse(null))).toMap
  
        // Retrieve corresponding PTM
        val ptmId = toLong(ptmSpecifRecord("ptm_id"))
        val ptmRecord = ptmRecordById(ptmId)
  
        // Retrieve corresponding PTM evidences
        val ptmEvidRecords = ptmEvidRecordsByPtmId.get(ptmId).get
  
        // TODO: load classification
        // TODO: load PTM specif evidences
        val ptmDef = PtmDefinitionBuilder.buildPtmDefinition(
          ptmRecord = ptmRecord,
          ptmSpecifRecord = ptmSpecifRecord,
          ptmEvidenceRecords = ptmEvidRecords,
          ptmClassification = ""
        )
  
        ptmDefMapBuilder += (ptmDef.id -> ptmDef)
  
      }
  
      ptmDefMapBuilder.result()
    }, false)
    
  }

  lazy val ptmDefByNameAndLocation: Map[Tuple3[String, Char, PtmLocation.Location], PtmDefinition] = {
    this.ptmDefinitionById.values.map { p => (p.names.shortName, p.residue, PtmLocation.withName(p.location)) -> p } toMap
  }

  lazy val ptmIdByName: Map[String, Long] = {
    this.ptmDefinitionById.values.map { p => p.names.shortName -> p.id } toMap
  }

  /*private var ptmDefinitionMap: HashMap[Int, PtmDefinition] = new collection.mutable.HashMap[Int, PtmDefinition]
  
  /** Extends the map of PTM definitions using a provided list of PTM specificity ids */
  private def extendPtmDefMap( ptmSpecifIds: Seq[Int] ): Unit = {
    if( ptmSpecifIds.length == 0 ) { return () }
    
    val missingPtmSpecifIds = ptmSpecifIds.filter( !ptmDefinitionMap.contains(_) )
    if( missingPtmSpecifIds.length == 0 ) { return () }
    
    // Load PTM records corresponding to the missing PTM specificities
    val ptmSpecifs = psDb.transaction { tx =>       
      tx.select( "SELECT * FROM ptm_specificity, ptm WHERE "+
                 "ptm_specificity.ptm_id = ptm.id AND ptm_specificity.id IN (" +
                 missingPtmSpecifIds.mkString(",") + ")" ) { r => 
        
        val ptmSpecifRecord = 
        
        // TODO: load classification field
      }
    }
    
  }*/

  def getPtmDefinitionsAsOptions(ptmDefIds: Seq[Long]): Array[Option[PtmDefinition]] = {
    val ptmDefById = this.ptmDefinitionById
    ptmDefIds.map { ptmDefById.get(_) } toArray
  }

  def getPtmDefinitions(ptmDefIds: Seq[Long]): Array[PtmDefinition] = {
    this.getPtmDefinitionsAsOptions(ptmDefIds).filter(_.isDefined).map(_.get)
  }

  def getPtmDefinition(ptmShortName: String, ptmResidue: Char, ptmLocation: PtmLocation.Location): Option[PtmDefinition] = {
    this.ptmDefByNameAndLocation.get(ptmShortName, ptmResidue, ptmLocation)
  }

  def getPtmId(shortName: String): Option[Long] = {
    this.ptmIdByName.get(shortName)
  }

}
