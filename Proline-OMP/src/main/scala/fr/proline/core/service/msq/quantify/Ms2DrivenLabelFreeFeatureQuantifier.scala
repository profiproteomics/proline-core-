package fr.proline.core.service.msq.quantify

import java.io.File
import javax.persistence.EntityManager
import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.collection.mutable.HashMap
import fr.proline.context.IExecutionContext
import fr.proline.core.algo.lcms.ClusteringParams
import fr.proline.core.algo.lcms.FeatureMappingParams
import fr.proline.core.algo.lcms.LabelFreeQuantConfig
import fr.proline.core.om.model.lcms.LcMsRun
import fr.proline.core.om.model.lcms.MapSet
import fr.proline.core.om.model.msi.{Instrument,Peptide}
import fr.proline.core.om.model.msq.ExperimentalDesign
import fr.proline.core.orm.uds.MasterQuantitationChannel
import fr.proline.core.service.lcms.io.ExtractMapSet
import fr.proline.repository.IDataStoreConnectorFactory

class Ms2DrivenLabelFreeFeatureQuantifier(
  val executionContext: IExecutionContext,
  val experimentalDesign: ExperimentalDesign,
  val udsMasterQuantChannel: MasterQuantitationChannel,
  val quantConfig: LabelFreeQuantConfig
) extends AbstractLabelFreeFeatureQuantifier {
  
  lazy val runIdByRsmId = {
    Map() ++ udsMasterQuantChannel.getQuantitationChannels().map { udsQC =>
      udsQC.getIdentResultSummaryId() -> udsQC.getRun().getId()
    }
  }
  
  // TODO: try to handle PSMs with rank > 1
  lazy val peptideByRunIdAndScanNumber = {
    
    val peptideMap = new collection.mutable.HashMap[Long, HashMap[Int, Peptide]]()
    
    for( rsm <- this.identResultSummaries ) {
      val runId = runIdByRsmId(rsm.id)
      val valPepMatchIds = rsm.peptideInstances.flatMap( _.getPeptideMatchIds )
      val pepMatchById = rsm.resultSet.get.peptideMatchById
      
      for( valPepMatchId <- valPepMatchIds ) {
        val valPepMatch = pepMatchById(valPepMatchId)
        // FIXME: how to deal with other ranked PSMs ?
        if( valPepMatch.rank == 1 ) {
          val spectrumId = valPepMatch.getMs2Query.spectrumId
          val scanNumber = this.scanNumberBySpectrumId(spectrumId)
          
          peptideMap.getOrElseUpdate(runId, new HashMap[Int, Peptide])(scanNumber) = valPepMatch.peptide
        }
      }
    }
    
    peptideMap.toMap
  }
  
  // Extract the LC-MS map set
  lazy val lcmsMapSet: MapSet = {
    val mapSetExtractor = new ExtractMapSet(this.lcmsDbCtx,quantConfig, Some(peptideByRunIdAndScanNumber) )
    mapSetExtractor.run()
    mapSetExtractor.extractedMapSet
  }
  
  override protected def quantifyMasterChannel(): Unit = {
    
    // Add processings specific to the MS2 driven strategy here
    val lcMsMapIdByRunId = Map() ++ lcmsMapSet.childMaps.map( lcmsMap => lcmsMap.runId.get -> lcmsMap.id )
    val udsQuantChannels = udsMasterQuantChannel.getQuantitationChannels
    for( udsQuantChannel <- udsQuantChannels) {
      udsQuantChannel.setLcmsMapId( lcMsMapIdByRunId( udsQuantChannel.getRun().getId ) )
    }
    
    super.quantifyMasterChannel()
  }

}