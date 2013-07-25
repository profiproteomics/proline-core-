package fr.proline.core.om.storer.lcms.impl

import fr.proline.context.DatabaseConnectionContext
import fr.proline.core.om.model.lcms.ProcessedMap
import fr.proline.core.om.model.lcms.Feature
import fr.proline.core.om.storer.lcms.IProcessedMapStorer

class JPAProcessedMapStorer( lcmsDbCtx: DatabaseConnectionContext ) extends IProcessedMapStorer {
  
  def insertProcessedMap( processedMap: ProcessedMap ): Long = {
    throw new Exception("not yet implemented")
    
    if( ! processedMap.isProcessed ) throw new Exception( "can't store a run map" )
    
    0L
  
  }
  
  def storeProcessedMap( processedMap: ProcessedMap, storeClusters: Boolean ): Unit = {
    throw new Exception("not yet implemented")
    
    if( ! processedMap.isProcessed ) throw new Exception( "can't store a run map" )
    
    ()
  
  }
  
  def storeFeatureClusters( features: Seq[Feature] ): Unit = {
    throw new Exception("not yet implemented")
    ()
  }
  
}