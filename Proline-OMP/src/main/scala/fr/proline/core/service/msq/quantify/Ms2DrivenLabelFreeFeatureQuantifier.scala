package fr.proline.core.service.msq.quantify


import javax.persistence.EntityManager
import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import fr.profi.util.primitives._
import fr.profi.jdbc.easy._
import fr.profi.util.serialization.ProfiJson
import fr.proline.context.DatabaseConnectionContext
import fr.proline.context.IExecutionContext
import fr.proline.core.algo.msq.config._
import fr.proline.core.dal.DoJDBCReturningWork
import fr.proline.core.dal.tables.SelectQueryBuilder1
import fr.proline.core.dal.tables.SelectQueryBuilder._
import fr.proline.core.dal.tables.msi.MsiDbSpectrumTable
import fr.proline.core.om.model.lcms.MapSet
import fr.proline.core.om.model.msi.{Peptide, PeptideMatch}
import fr.proline.core.om.model.msq.ExperimentalDesign
import fr.proline.core.orm.uds.MasterQuantitationChannel
import fr.proline.core.service.lcms.io.ExtractMapSet
import fr.proline.context.DatabaseConnectionContext
import fr.proline.core.om.model.msi.ResultSummary
import fr.proline.core.om.provider.msi.impl.SQLResultSummaryProvider
import fr.proline.core.om.model.msq.SpectralCountProperties
import fr.proline.core.om.model.msq.MasterQuantChannelProperties


class Ms2DrivenLabelFreeFeatureQuantifier(
  val executionContext: IExecutionContext,
  val udsMasterQuantChannel: MasterQuantitationChannel,
  val experimentalDesign: ExperimentalDesign,
  val quantConfig: LabelFreeQuantConfig
) extends AbstractLabelFreeFeatureQuantifier {
  
  // Extract the LC-MS map set
  lazy val lcmsMapSet: MapSet = {
    
    val (pepByRunAndScanNbr, psmByRunAndScanNbr) = entityCache.getPepAndPsmByRunIdAndScanNumber(this.mergedResultSummary)
    val mapSetExtractor = new ExtractMapSet(
      this.lcmsDbCtx,
      this.udsMasterQuantChannel.getName,
      this.entityCache.getLcMsRuns(),
      quantConfig,
      Some(pepByRunAndScanNbr),
      Some(psmByRunAndScanNbr)
    )
    mapSetExtractor.run()
    mapSetExtractor.extractedMapSet
  }
  
  // Add processings specific to the MS2 driven strategy here
  override protected def quantifyMasterChannel(): Unit = {
    
    // Retrieve LC-MS maps ids mapped by the run id
    val lcMsMapIdByRunId = Map() ++ lcmsMapSet.childMaps.map( lcmsMap => lcmsMap.runId.get -> lcmsMap.id )
    
    // Update the LC-MS map id of each master quant channel
    val udsQuantChannels = udsMasterQuantChannel.getQuantitationChannels
    for( udsQuantChannel <- udsQuantChannels) {
      udsQuantChannel.setLcmsMapId( lcMsMapIdByRunId( udsQuantChannel.getRun().getId ) )
      udsEm.merge(udsQuantChannel)
    }
    
    // Update the map set id of the master quant channel
    udsMasterQuantChannel.setLcmsMapSetId(lcmsMapSet.id)
    udsEm.merge(udsMasterQuantChannel)
    
    super.quantifyMasterChannel()
  }
  
  override protected def getMergedResultSummary(msiDbCtx: DatabaseConnectionContext): ResultSummary = {
    if (masterQc.identResultSummaryId.isEmpty) {
      isMergedRsmProvided = false
      createMergedResultSummary(msiDbCtx)
    } else {
      isMergedRsmProvided = true
      
      val pRsmId = masterQc.identResultSummaryId.get
          
      this.logger.debug("Read Merged RSM with ID " + pRsmId)

      // Instantiate a Lazy RSM provider
      val rsmProvider = new SQLResultSummaryProvider(msiDbCtx = msiDbCtx, psDbCtx = psDbCtx)
      val identRsmOpt = rsmProvider.getResultSummary(pRsmId, true)
        
      require( identRsmOpt.isDefined, "can't load the result summary with id=" + pRsmId)
      
      // Update Master Quant Channel properties
      val mqchProperties = new MasterQuantChannelProperties(
        identResultSummaryId = masterQc.identResultSummaryId,
        identDatasetId = masterQc.identDatasetId,
        spectralCountProperties = None
      )
      udsMasterQuantChannel.setSerializedProperties(ProfiJson.serialize(mqchProperties))
      
      identRsmOpt.get
    }
  }

}