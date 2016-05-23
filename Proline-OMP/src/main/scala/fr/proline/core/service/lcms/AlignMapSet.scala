package fr.proline.core.service.lcms

import fr.profi.jdbc.easy._
import fr.proline.api.service.IService
import fr.proline.context.LcMsDbConnectionContext
import fr.proline.core.dal.{ DoJDBCWork, DoJDBCReturningWork }
import fr.proline.core.algo.lcms.AlignmentParams
import fr.proline.core.algo.lcms.LcmsMapAligner
import fr.proline.core.om.model.lcms.MapSet
import fr.proline.core.om.storer.lcms.MapAlnSetStorer
import fr.proline.core.service.lcms._
import fr.proline.repository.IDatabaseConnector
import fr.proline.core.algo.lcms.alignment.AlignmentResult
import com.typesafe.scalalogging.LazyLogging

object AlignMapSet {

  def apply(
    lcmsDbCtx: LcMsDbConnectionContext,
    mapSet: MapSet, 
    alnResult: AlignmentResult
  ): Unit = {
    
    val mapSetAligner = new AlignMapSet( lcmsDbCtx, mapSet,  alnResult  )
    mapSetAligner.runService()
    ()
    
  }
  
}

class AlignMapSet(
  val lcmsDbCtx: LcMsDbConnectionContext,
  mapSet: MapSet, 
  alnResult: AlignmentResult
) extends ILcMsService with LazyLogging {

  def runService(): Boolean = {
    
    //val mapSetLoader = new MapSetLoader( lcmsDb )
    //val mapSet = mapSetLoader.getMapSet( mapSetId )
    val mapSetId = mapSet.id
    
    // Check if a transaction is already initiated
    val wasInTransaction = lcmsDbCtx.isInTransaction 
    if( !wasInTransaction ) lcmsDbCtx.beginTransaction()
    
    // Check if reference map already exists: if so delete alignments
    val existingAlnRefMapId = mapSet.getAlnReferenceMapId
    if( existingAlnRefMapId > 0 ) {
      DoJDBCWork.withEzDBC(lcmsDbCtx, { ezDBC =>
      
        
        ezDBC.execute( "DELETE FROM map_alignment WHERE map_set_id = " + mapSetId )
        
        // Update processed reference map
        ezDBC.execute( "UPDATE processed_map SET is_aln_reference = ? WHERE id = ?", false, existingAlnRefMapId )
      
      })
    }
    
    // Copy maps while removing feature clusters
    // TODO: check if it is better or not
    val childMapsWithoutClusters = mapSet.childMaps.map { _.copyWithoutClusters }
     
    // Perform the map alignment
    // do not recompute the map alignment
    /*
    val mapAligner = LcmsMapAligner( methodName = alnMethodName )
    val alnResult = mapAligner.computeMapAlignments( childMapsWithoutClusters, alnParams )
    * */
    
    val alnStorer = MapAlnSetStorer( lcmsDbCtx )
    alnStorer.storeMapAlnSets( alnResult.mapAlnSets, mapSetId, alnResult.alnRefMapId )
    
    // Commit transaction if it was initiated locally
    if( !wasInTransaction ) lcmsDbCtx.commitTransaction()
    
    // Update the maps the map set alignment sets
    mapSet.setAlnReferenceMapId( alnResult.alnRefMapId )
    mapSet.mapAlnSets = alnResult.mapAlnSets
    
    true
  }
}