package fr.proline.core.service.uds

import java.util.HashSet
import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.collection.mutable.HashMap
import fr.proline.api.service.IService
import fr.proline.core.om.model.msq._
import fr.proline.core.orm.uds.{
  BiologicalGroup => UdsBiologicalGroup,
  BiologicalSample => UdsBiologicalSample,
  Dataset => UdsDataset,
  GroupSetup => UdsGroupSetup,
  MasterQuantitationChannel => UdsMasterQuantitationChannel,
  Project => UdsProject,
  QuantitationChannel => UdsQuantChannel,
  QuantitationLabel => UdsQuantLabel,
  QuantitationMethod => UdsQuantMethod,
  RatioDefinition => UdsRatioDefinition,
  Run => UdsRun,
  SampleAnalysis => UdsSampleAnalysis
}
import fr.proline.core.orm.uds.Dataset.DatasetType
import fr.proline.repository.IDataStoreConnectorFactory
import fr.proline.util.sql.getTimeAsSQLTimestamp
import com.typesafe.scalalogging.slf4j.Logging

class CreateQuantitation(
  dbManager: IDataStoreConnectorFactory,
  name: String,
  description: String,
  projectId: Long,
  methodId: Long,
  experimentalDesign: ExperimentalDesign
) extends IService with Logging {

  private var _udsQuantitation: UdsDataset = null
  def getUdsQuantitation() = _udsQuantitation

  def runService() = {

    // Create entity manager
    val udsEM = dbManager.getUdsDbConnector.getEntityManagerFactory.createEntityManager()

    // Retrieve some vars
    val biologicalSamples = experimentalDesign.biologicalSamples
    val groupSetups = experimentalDesign.groupSetups
    val masterQuantChannels = experimentalDesign.masterQuantChannels

    val udsProject = udsEM.find(classOf[UdsProject], projectId)
    require(udsProject != null, "undefined project with id=" + projectId)

    val udsQuantMethod = udsEM.find(classOf[UdsQuantMethod], methodId)
    require(udsQuantMethod != null, "undefined method with id=" + methodId)

    // Retrieve existing quantitations for this project
    // TODO: add JPA method getQuantitations to the project entity
    val existingQuants = udsEM.createQuery("FROM fr.proline.core.orm.uds.Dataset WHERE project_id = " + projectId,
      classOf[fr.proline.core.orm.uds.Dataset]).getResultList().toList

    var previousQuantNum = 0
    if (existingQuants.length != 0) { previousQuantNum = existingQuants.last.getNumber() }

    // Begin new transaction
    udsEM.getTransaction().begin()

    val mqcCount = masterQuantChannels.length

    // Create new quantitation
    val udsQuantitation = new UdsDataset(udsProject)
    udsQuantitation.setNumber(previousQuantNum + 1)
    udsQuantitation.setName(name)
    udsQuantitation.setDescription(description)
    udsQuantitation.setType(DatasetType.QUANTITATION)
    udsQuantitation.setCreationTimestamp(getTimeAsSQLTimestamp)
    udsQuantitation.setChildrenCount(0)
    udsQuantitation.setMethod(udsQuantMethod)
    udsEM.persist(udsQuantitation)

    this._udsQuantitation = udsQuantitation

    // Store biological samples
    val udsBioSampleByNum = new HashMap[Int, UdsBiologicalSample]
    var bioSampleNum = 0
    var bioSampleNumFound = false
    if(biologicalSamples.length>0){
      if(biologicalSamples(0).number > 0)
    	  bioSampleNumFound = true
	  else
	    logger.warn("Biological Sample Number not specified, use incrementation on iterator !! Hopes it correspond to biological group references. ")
    }
      
    for (bioSample <- biologicalSamples) {
      bioSampleNum += 1

      val udsBioSample = new UdsBiologicalSample()
      udsBioSample.setNumber(if(bioSampleNumFound) bioSample.number else bioSampleNum)
      udsBioSample.setName(bioSample.name)
      udsBioSample.setDataset(udsQuantitation)
      udsEM.persist(udsBioSample)

      udsBioSampleByNum(udsBioSample.getNumber()) = udsBioSample
    }

    // Store group setups
    var groupSetupNumber = 0
    var groupSetupNumberFound = false
     if(groupSetups.length>0){
      if(groupSetups(0).number > 0)
    	  groupSetupNumberFound = true
	  else
	    logger.warn("Group number not specified, use incrementation on iterator !! ")
    }
    
    for (groupSetup <- groupSetups) {
      groupSetupNumber += 1

      // Save group setup
      val udsGroupSetup = new UdsGroupSetup()
      udsGroupSetup.setNumber(if(groupSetupNumberFound)groupSetup.number else groupSetupNumber)
      udsGroupSetup.setName(groupSetup.name)
      udsGroupSetup.setQuantitationDataset(udsQuantitation)
      udsEM.persist(udsGroupSetup)

      // Create a set of group setups
      val udsGroupSetups = new HashSet[UdsGroupSetup]()
      udsGroupSetups.add(udsGroupSetup)

      // Retrieve biological groups
      val biologicalGroups = groupSetup.biologicalGroups

      // Store biological groups
      val udsBioGroupByNum = new HashMap[Int, UdsBiologicalGroup]
      var bioGroupNumber = 0
      
      var bioGroupNumberFound = false
      if(biologicalGroups.length>0){
	      if(biologicalGroups(0).number > 0)
	    	  bioGroupNumberFound = true
		  else
		    logger.warn("Biological group number not specified, use incrementation on iterator !! ")
      }
    
      
      for (biologicalGroup <- biologicalGroups) {
        bioGroupNumber += 1

        // Store biological group
        val udsBioGroup = new UdsBiologicalGroup()
        udsBioGroup.setNumber(if(bioGroupNumberFound) biologicalGroup.number else bioGroupNumber)
        udsBioGroup.setName(biologicalGroup.name)
        udsBioGroup.setGroupSetups(udsGroupSetups)
        udsBioGroup.setQuantitationDataset(udsQuantitation)
        udsEM.persist(udsBioGroup)

        // Map the group id by the group number
        udsBioGroupByNum(udsBioGroup.getNumber()) = udsBioGroup

        // Retrieve the list of biological samples belonging to this biological group
        val sampleNumbers = biologicalGroup.sampleNumbers

        val udsBioSampleSet = new java.util.ArrayList[UdsBiologicalSample]
        for (sampleNumber <- sampleNumbers) {

          if (udsBioSampleByNum.contains(sampleNumber) == false) {
            throw new Exception("can't map the biological group named '" + biologicalGroup.name + "' with the sample #" + sampleNumber)
          }

          udsBioSampleSet.add(udsBioSampleByNum(sampleNumber))
        }

        // Link biological group to corresponding biological samples
        udsBioGroup.setBiologicalSamples(udsBioSampleSet)
        udsEM.persist(udsBioGroup)
      }

      // Retrieve ratio definitions
      val ratioDefinitions = groupSetup.ratioDefinitions

      // Store ratio definitions
      var ratioDefNumber = 0
      for (ratioDefinition <- ratioDefinitions) {
        ratioDefNumber += 1

        val udsRatioDef = new UdsRatioDefinition()
        udsRatioDef.setNumber(ratioDefNumber)
        udsRatioDef.setNumerator(udsBioGroupByNum(ratioDefinition.numeratorGroupNumber))
        udsRatioDef.setDenominator(udsBioGroupByNum(ratioDefinition.denominatorGroupNumber))
        udsRatioDef.setGroupSetup(udsGroupSetup)
        udsEM.persist(udsRatioDef)

      }

    }

    // Store fractions
    var fractionNumber = 0
	var fractionNumberFound = false
	if(masterQuantChannels.length>0){
		if(masterQuantChannels(0).number > 0)
			fractionNumberFound = true
		else
			logger.warn("Fraction number not specified, use incrementation on iterator !! ")
	}
    
    val udsSampleReplicateByKey = new HashMap[String, UdsSampleAnalysis]
    for (masterQuantChannel <- masterQuantChannels) {
      fractionNumber += 1

      // Save quantitation fraction
      val udsQf = new UdsMasterQuantitationChannel()
      udsQf.setNumber(if(fractionNumberFound) masterQuantChannel.number else fractionNumber)
      udsQf.setName(masterQuantChannel.name.getOrElse(""))
      udsQf.setDataset(udsQuantitation)

      if (masterQuantChannel.lcmsMapSetId.isDefined) {
        udsQf.setLcmsMapSetId(masterQuantChannel.lcmsMapSetId.get)
      }

      udsEM.persist(udsQf)

      val quantChannels = masterQuantChannel.quantChannels
      var quantChannelNum = 0
      var quantChannelNumFound =false 
      if(quantChannels.length>0){
		if(quantChannels(0).number > 0)
			quantChannelNumFound = true
		else
			logger.warn("Quantchannel number not specified, use incrementation on iterator !! ")
      }

      
      // Iterate over each fraction quant channel
      val replicateNumBySampleNum = new HashMap[Int, Int]
      for (quantChannel <- quantChannels) {
        quantChannelNum += 1
        
        // Retrieve some vars
        val sampleNum = quantChannel.sampleNumber
        val udsBioSample = udsBioSampleByNum(sampleNum)
        
        // Retrieve replicate number and increment it
        val replicateNum = replicateNumBySampleNum.getOrElseUpdate(sampleNum, 0) + 1
        replicateNumBySampleNum(sampleNum) = replicateNum

        // Retrieve analysis replicate if it already exists
        val contextKey = sampleNum + "." + replicateNum

        if (udsSampleReplicateByKey.contains(contextKey) == false) {

          //val rdbReplicate = udsAnalysisReplicateByKey(contextKey)
          // Store sample replicate
          val udsReplicate = new UdsSampleAnalysis()
          udsReplicate.setNumber(replicateNum)
          val bioSpl = new java.util.ArrayList[UdsBiologicalSample]()
          bioSpl.add(udsBioSample)
          udsReplicate.setBiologicalSample(bioSpl)
          udsReplicate.setDataset(udsQuantitation)
          udsEM.persist(udsReplicate)

          udsSampleReplicateByKey(contextKey) = udsReplicate
        }

        val udsQuantChannel = new UdsQuantChannel()
        udsQuantChannel.setNumber(if(quantChannelNumFound) quantChannel.number else quantChannelNum)
        udsQuantChannel.setName("")
        udsQuantChannel.setContextKey(contextKey)
        udsQuantChannel.setIdentResultSummaryId(quantChannel.identResultSummaryId)
        udsQuantChannel.setSampleReplicate(udsSampleReplicateByKey(contextKey))
        udsQuantChannel.setBiologicalSample(udsBioSample)
        udsQuantChannel.setMasterQuantitationChannel(udsQf)
        udsQuantChannel.setQuantitationDataset(udsQuantitation)
        
        if( quantChannel.runId.isDefined ) {
          val udsRun = udsEM.find(classOf[UdsRun], quantChannel.runId.get)
          udsQuantChannel.setRun(udsRun)
        }

        // TODO: check method type
        if (quantChannel.lcmsMapId.isDefined) {
          udsQuantChannel.setLcmsMapId(quantChannel.lcmsMapId.get)
        } else if (quantChannel.quantLabelId.isDefined) {
          val udsQuantLabel = udsEM.find(classOf[UdsQuantLabel], quantChannel.quantLabelId.get)
          udsQuantChannel.setLabel(udsQuantLabel)
        }

        udsEM.persist(udsQuantChannel)

      }
    }

    //rdbQuantitation.quantitationFractions(quantiFrations)
    //rdbQuantitation.analyses (analyses)

    // Commit transaction
    udsEM.getTransaction().commit()

    // Close entity manager
    udsEM.close()

    true
  }

}
