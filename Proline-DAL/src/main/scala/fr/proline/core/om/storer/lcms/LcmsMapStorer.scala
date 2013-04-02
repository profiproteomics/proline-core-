package fr.proline.core.om.storer.lcms

import java.io.File
import fr.proline.context.DatabaseConnectionContext
import fr.proline.core.om.model.lcms.Feature
import fr.proline.core.om.model.lcms.ILcMsMap
import fr.proline.core.om.model.lcms.LcMsRun
import fr.proline.core.om.model.lcms.ProcessedMap
import fr.proline.core.om.model.lcms.RunMap
import fr.proline.core.om.model.msi.Instrument
import fr.proline.core.om.storer.lcms.impl._
import fr.proline.repository.DriverType

trait IRunMapStorer {
  
  def storeRunMap( runMap: RunMap, storePeaks: Boolean = false ): Unit
  
 }


trait IRunStorer {
  
  def storeLcmsRun( run: LcMsRun, instrument: Instrument) : Unit
}

/** A factory object for implementations of the IRunMapStorer trait */
object RunMapStorer {
  def apply( lcmsDbCtx: DatabaseConnectionContext ): IRunMapStorer = {
    if( lcmsDbCtx.isJPA ) new JPARunMapStorer(lcmsDbCtx)
    else {
      lcmsDbCtx.getDriverType match {
        //case DriverType.POSTGRESQL => new PgRunMapStorer(lcmsDbCtx)
        case _ => new SQLRunMapStorer(lcmsDbCtx)
      }
    }
    
  }
}

trait IProcessedMapStorer {
  
  import fr.proline.core.om.model.lcms.ProcessedMap
  import fr.proline.core.om.model.lcms.Feature
  
  def storeProcessedMap( processedMap: ProcessedMap, storeClusters: Boolean = false ): Unit
  def storeFeatureClusters( features: Seq[Feature] ): Unit
  
}

/** A factory object for implementations of the IProcessedMapStorer trait */
object ProcessedMapStorer {
  
  def apply( lcmsDbCtx: DatabaseConnectionContext ): IProcessedMapStorer = {
    if( lcmsDbCtx.isJPA ) new JPAProcessedMapStorer(lcmsDbCtx)
    else {
      lcmsDbCtx.getDriverType match {
        //case DriverType.POSTGRESQL => new PgProcessedMapStorer(lcmsDbCtx)
        case _ => new SQLProcessedMapStorer(lcmsDbCtx)
      }
    }
  }
}

trait IMasterMapStorer {
  
  import fr.proline.core.om.model.lcms.ProcessedMap
  
  def storeMasterMap( processedMap: ProcessedMap ): Unit
  
 }

/** A factory object for implementations of the IMasterMapStorer trait */
object MasterMapStorer {
  
  def apply( lcmsDbCtx: DatabaseConnectionContext ): IMasterMapStorer = {
    if( lcmsDbCtx.isJPA ) new JPAMasterMapStorer(lcmsDbCtx)
    else {
      lcmsDbCtx.getDriverType match {
        //case DriverType.POSTGRESQL => new PgMasterMapStorer(lcmsDbCtx)
        case _ => new SQLMasterMapStorer(lcmsDbCtx)
      }
    }
  }
  
}
