package fr.proline.core.om.utils

import scala.collection.JavaConversions.asScalaSet
import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import fr.proline.core.om.model.msi.IonTypes
import fr.proline.core.om.model.msi.LocatedPtm
import fr.proline.core.om.model.msi.Peptide
import fr.proline.core.om.model.msi.PeptideInstance
import fr.proline.core.om.model.msi.PeptideMatch
import fr.proline.core.om.model.msi.PeptideSet
import fr.proline.core.om.model.msi.PeptideSetItem
import fr.proline.core.om.model.msi.PtmDefinition
import fr.proline.core.om.model.msi.PtmEvidence
import fr.proline.core.om.model.msi.PtmNames
import fr.proline.core.orm.msi.repository.{ProteinSetRepositorty => proSetRepo}
import javax.persistence.EntityManager
import javax.persistence.Persistence
import fr.proline.repository.ProlineDatabaseType
import fr.proline.core.orm.util.DataStoreConnectorFactory

import fr.proline.core.utils.ResidueUtils._

/**
 * Provides method to convert Peptides and PTM objects from ORM to OM.
 * If specified in constructor, created objects will be stored in map( referenced by their ID) to be retrieve later if necessary.
 *
 * @author VD225637
 *
 */
class PeptidesOMConverterUtil(useCachedObject: Boolean = true) {

  val peptideInstancesCache = new HashMap[Long, fr.proline.core.om.model.msi.PeptideInstance]
  val peptideMatchesCache = new HashMap[Long, fr.proline.core.om.model.msi.PeptideMatch]
  val peptidesCache = new HashMap[Long, fr.proline.core.om.model.msi.Peptide]
  val locatedPTMsCache = new HashMap[Long, LocatedPtm]
  val ptmNamesCache = new HashMap[String, PtmNames]
  val ptmDefinitionsCache = new HashMap[Long, PtmDefinition]
  val peptideSetsCache = new HashMap[Long, fr.proline.core.om.model.msi.PeptideSet]

  type MsiPeptideMatch = fr.proline.core.orm.msi.PeptideMatch
  type MsiPeptideInstance = fr.proline.core.orm.msi.PeptideInstance
  type MsiPeptideSet = fr.proline.core.orm.msi.PeptideSet
  type MsiSeqDatabase = fr.proline.core.orm.msi.SeqDatabase
  type PsPeptide = fr.proline.core.orm.ps.Peptide
  type PsPeptidePtm = fr.proline.core.orm.ps.PeptidePtm
  type PsPtmSpecificity = fr.proline.core.orm.ps.PtmSpecificity

  val psPrecursorType = fr.proline.core.orm.ps.PtmEvidence.Type.Precursor

  //implicit def javaIntToScalaInt(javaInt: java.lang.Integer) = javaInt.intValue

  def convertPeptideMatchORM2OM(msiPepMatch: MsiPeptideMatch): PeptideMatch = {

    //Verify if object is in cache 
    if (useCachedObject && peptideMatchesCache.contains(msiPepMatch.getId())) {
      peptideMatchesCache(msiPepMatch.getId())
    }

    null
  }

  /**
   * Create a OM PeptideInstance corresponding to the specified ORM PeptideInstance.
   *
   * @param pepInstORM  ORM PeptideInstance to create the OM PeptodeInstance for
   * @param loadPepMatches specify if associated OM PeptideMatch should be created or not.
   * @param msiEM EntityManager to the MSIdb the data are issued from
   * @return an OM PeptideInstance corresponding to specified ORM PeptideInstance.
   */
  def convertPeptideInstanceORM2OM(msiPepInst: MsiPeptideInstance,
    loadPepMatches: Boolean,
    msiEM: EntityManager): PeptideInstance = {

    //Verify if object is in cache
    if (useCachedObject && peptideInstancesCache.contains(msiPepInst.getId())) {
      return peptideInstancesCache(msiPepInst.getId())
    }

    //Objects to access data in repositories    
    //val prolineRepo = ProlineRepository.getProlineRepositoryInstance()
    
    // FIXME this code is NOT Thread / Context safe
    
    val dataStoreConnectorFactory = DataStoreConnectorFactory.getInstance

    //Found PeptideInstance Children mapped by their id
    val pepInstChildById = new HashMap[Long, PeptideInstance]()

    //---- Peptide Matches Arrays 
    val msiPepMatches = msiPepInst.getPeptideInstancePeptideMatchMaps.map { _.getPeptideMatch } //ORM PeptideMatches

    var pepMatches: Array[PeptideMatch] = null //OM PeptideMatches. Get only if asked for 
    if (loadPepMatches) pepMatches = new Array[PeptideMatch](msiPepMatches.size)

    val pepMatchIds = new Array[Long](msiPepMatches.size) //OM PeptideMatches ids

    //**** Create PeptideMatches array and PeptideInstance Children    
    var index = 0
    for (nextMsiPM <- msiPepMatches) {

      //Go through PeptideMatch children and get associated PeptideInstance 
      val msiPepMatchChildIT = nextMsiPM.getChildren().iterator()
      while (msiPepMatchChildIT.hasNext()) {

        val nextMsiPMChild = msiPepMatchChildIT.next()
        val msiPepInstChild = proSetRepo.findPeptideInstanceForPepMatch(msiEM, nextMsiPMChild.getId)

        if (!pepInstChildById.contains(msiPepInstChild.getId)) {
          //Convert child ORM Peptide Instance to OM Peptide Instance
          pepInstChildById += msiPepInstChild.getId -> convertPeptideInstanceORM2OM(msiPepInstChild, loadPepMatches, msiEM)
        }
      }

      // Fill Peptide Matches Arrays
      pepMatchIds(index) = nextMsiPM.getId()

      if (loadPepMatches) pepMatches(index) = convertPeptideMatchORM2OM(nextMsiPM)

      index += 1
    }

    //Create Peptide Instance Child Arrays
    //val pepInstChildren = new Array[PeptideInstance](pepInstChildById.size)
    val pepInstChildren = pepInstChildById.values.toArray

    //Get Peptide, Unmodified Peptide && PeptideInstance 
    val psDBConnector = dataStoreConnectorFactory.getPsDbConnector()
    val emf = psDBConnector.getEntityManagerFactory()
    val em = emf.createEntityManager()

    val psPeptide = em.find(classOf[PsPeptide], msiPepInst.getPeptideId())
    val psUnmodifiedPep = em.find(classOf[PsPeptide], msiPepInst.getUnmodifiedPeptideId())
    var unmodifiedPep = if (psUnmodifiedPep == null) None else Some(convertPeptidePsORM2OM(psUnmodifiedPep))

    val msiUnmodifiedPepInst = proSetRepo.findPeptideInstanceForPeptide(msiEM, msiPepInst.getUnmodifiedPeptideId())
    val unmodifiedPepInst = if (msiUnmodifiedPepInst == null) None
    else Some(convertPeptideInstanceORM2OM(msiUnmodifiedPepInst, loadPepMatches, msiEM))

    //Create OM PeptideInstance 
    val convertedPepInst = new PeptideInstance(
      id = msiPepInst.getId(),
      peptide = convertPeptidePsORM2OM(psPeptide),
      //peptideMatchIds = pepMatchIds,
      peptideMatches = pepMatches,
      children = pepInstChildren,
      //unmodifiedPeptideId = if( unmodifiedPep == None ) 0 else unmodifiedPep.id,
      unmodifiedPeptide = unmodifiedPep,
      proteinMatchesCount = msiPepInst.getProteinMatchCount(),
      proteinSetsCount = msiPepInst.getProteinSetCount(),
      validatedProteinSetsCount=msiPepInst.getValidatedProteinSetCount(),  
      totalLeavesMatchCount=msiPepInst.getTotalLeavesMatchCount(),
      selectionLevel = msiPepInst.getSelectionLevel(),
      elutionTime = msiPepInst.getElutionTime().floatValue(),
      bestPeptideMatchId = msiPepInst.getBestPeptideMatchId(),
      resultSummaryId = msiPepInst.getResultSummary().getId())
    if (useCachedObject) peptideInstancesCache.put(msiPepInst.getId(), convertedPepInst)

    //*** Create PeptideSets for current PeptideInstance    
    val pepInstanceById = new HashMap[Long, PeptideInstance]()

    val msiPepSetItemIT = msiPepInst.getPeptideSetPeptideInstanceItems().iterator()
    val pepSetById = new HashMap[Long, PeptideSet]()

    while (msiPepSetItemIT.hasNext()) {
      val msiPepSetItem = msiPepSetItemIT.next()

      if (!pepSetById.contains(msiPepSetItem.getPeptideSet().getId)) {
        val pepSet = convertPepSetORM2OM(msiPepSetItem.getPeptideSet(), loadPepMatches, msiEM)
        pepSetById += (msiPepSetItem.getPeptideSet().getId -> pepSet)
      } else {
        //VDS: Should not happen : PeptideInstance only once in each PeptideSet ! 
        throw new Exception("PeptideInstance should be unique inside a PeptideSet !")
      }
    }

    // Update peptideSets attribute
    convertedPepInst.peptideSets = pepSetById.values.toArray

    convertedPepInst
  }

  /**
   *  Create OM PeptideSet from ORM PeptideSet and associated objects :
   *  - PeptideSetItem and PeptideInstance (with PeptideMatch or not depending on getPepMatchForNewPepInst value)
   *
   * @param pepSetORM
   * @return
   */
  def convertPepSetORM2OM(msiPepSet: MsiPeptideSet, loadPepMatches: Boolean, msiEM: EntityManager): PeptideSet = {

    //Verify if exist in cache
    if (useCachedObject && peptideSetsCache.contains(msiPepSet.getId))
      return peptideSetsCache(msiPepSet.getId)

    val msiProtMatches = msiPepSet.getProteinMatches()
    val protMatchesIds = new Array[Long](msiProtMatches.size)
    val msiProtMatchesIter = msiProtMatches.iterator()

    var index = 0
    while (msiProtMatchesIter.hasNext()) {
      protMatchesIds(index) = msiProtMatchesIter.next().getId()
      index += 1
    }

    val pepSet = new PeptideSet(
      id = msiPepSet.getId(),
      items = null,
      isSubset = msiPepSet.getIsSubset(),
      score = msiPepSet.getScore(),
      scoreType = msiPepSet.getScoring().getName(),
      peptideMatchesCount = msiPepSet.getPeptideMatchCount(),
      proteinMatchIds = protMatchesIds,
      proteinSetId = msiPepSet.getProteinSet().getId(),
      resultSummaryId = msiPepSet.getResultSummaryId()
    )

    val msiPepSetItems = msiPepSet.getPeptideSetPeptideInstanceItems()
    val msiPepSetItemIT = msiPepSetItems.iterator()
    val pepSetItems = new Array[PeptideSetItem](msiPepSetItems.size);

    index = 0
    while (msiPepSetItemIT.hasNext()) {
      val msiPepSetItem = msiPepSetItemIT.next()

      val pepSetItem = new PeptideSetItem(
        selectionLevel = msiPepSetItem.getSelectionLevel(),
        peptideInstance = convertPeptideInstanceORM2OM(msiPepSetItem.getPeptideInstance(), loadPepMatches, msiEM),
        //peptideSetId = msiPepSet.getId(),
        peptideSet = Some(pepSet),
        isBestPeptideSet = Some(msiPepSetItem.getIsBestPeptideSet()),
        resultSummaryId = msiPepSet.getResultSummaryId())
      pepSetItems(index) = pepSetItem
      index += 1
    }
    pepSet.items = pepSetItems

    if (useCachedObject) peptideSetsCache += pepSet.id -> pepSet

    return pepSet;
  }

  def convertPeptidePsORM2OM(psPeptide: PsPeptide): Peptide = {

    // Check if object is in cache 
    if (useCachedObject && peptidesCache.contains(psPeptide.getId)) {
      return peptidesCache(psPeptide.getId)
    }

    // **** Create OM LocatedPtm for specified Peptide
    val psPtms = psPeptide.getPtms()
    val locatedPtms = if (psPtms == null) {
      new Array[LocatedPtm](0)
    } else {
      val ptmArray = new Array[LocatedPtm](psPtms.size())
      val psPepPtmIt = psPtms.iterator()

      var index = 0
      while (psPepPtmIt.hasNext()) {
        ptmArray(index) = convertPeptidePtmPsORM2OM(psPepPtmIt.next())
        index += 1
      }

      ptmArray
    }

    // **** Create OM Peptide
    val peptide = new Peptide(
      id = psPeptide.getId,
      sequence = psPeptide.getSequence(),
      ptmString = psPeptide.getPtmString(),
      ptms = locatedPtms,
      calculatedMass = psPeptide.getCalculatedMass(),
      properties = null)
    if (useCachedObject) peptidesCache.put(peptide.id, peptide)

    peptide
  }

  /**
   *  Convert from fr.proline.core.orm.ps.PeptidePtm (ORM) to fr.proline.core.om.model.msi.LocatedPtm (OM).
   *
   * LocatedPtm, PtmDefinition, PtmEvidence and PtmNames will be created from specified
   * PeptidePtm and associated PtmSpecificity, PtmEvidence and Ptm
   *
   *
   * @param ptmPsORM : fr.proline.core.orm.ps.PeptidePtm to convert
   * @return created LocatedPtm (with associated objects)
   */
  def convertPeptidePtmPsORM2OM(psPeptidePtm: PsPeptidePtm): LocatedPtm = {

    import fr.proline.util.regex.RegexUtils._

    // Check if object is in cache 
    if (useCachedObject && locatedPTMsCache.contains(psPeptidePtm.getId)) {
      return locatedPTMsCache(psPeptidePtm.getId)
    }

    var precursorEvidence: PtmEvidence = null
    val psPtmEvidencesIt = psPeptidePtm.getSpecificity().getPtm().getEvidences().iterator();

    while (psPtmEvidencesIt.hasNext() && precursorEvidence == null) {
      val psPtmEvidence = psPtmEvidencesIt.next()
      if (psPtmEvidence.getType().equals(psPrecursorType)) {
        precursorEvidence = new PtmEvidence(IonTypes.Precursor,
          psPtmEvidence.getComposition(),
          psPtmEvidence.getMonoMass(),
          psPtmEvidence.getAverageMass(),
          psPtmEvidence.getIsRequired())
      }
    }

    // Create OM PtmDefinition from ORM PtmSpecificity
    val ptmDefinition = convertPtmSpecificityORM2OM(psPeptidePtm.getSpecificity())

    //Create OM LocatedPtm from ORM PeptidePtm
    val locatedPtm = new LocatedPtm(
      definition = ptmDefinition,
      seqPosition = psPeptidePtm.getSeqPosition(),
      monoMass = psPeptidePtm.getMonoMass(),
      averageMass = psPeptidePtm.getAverageMass(),
      composition = precursorEvidence.composition,
      isNTerm = if (ptmDefinition.location =~ """.+N-term$""") true else false,
      isCTerm = if (ptmDefinition.location =~ """.+C-term$""") true else false)
    if (useCachedObject) locatedPTMsCache.put(psPeptidePtm.getId(), locatedPtm)

    locatedPtm
  }

  /**
   *  Convert from fr.proline.core.orm.ps.PeptideSpecificity(ORM) to fr.proline.core.om.model.msi.PtmDefinition (OM).
   *
   *
   * @param ptmSpecificityORM : fr.proline.core.orm.ps.PeptideSpecificity to convert
   * @return created PtmDefinition (with associated objects)
   */
  def convertPtmSpecificityORM2OM(psPtmSpecificity: PsPtmSpecificity): PtmDefinition = {

    import collection.JavaConversions.collectionAsScalaIterable

    //Verify PtmDefinition exist in cache
    if (useCachedObject && ptmDefinitionsCache.contains(psPtmSpecificity.getId))
      return ptmDefinitionsCache(psPtmSpecificity.getId)

    //*********** Create PtmNames from Ptm
    val psPtm = psPtmSpecificity.getPtm()
    val psPtmShortName = psPtm.getShortName()
    var ptmNames: PtmNames = null
    if (useCachedObject && ptmNamesCache.contains(psPtmShortName))
      ptmNames = ptmNamesCache(psPtmShortName)
    if (ptmNames == null) {
      ptmNames = new PtmNames(psPtmShortName, psPtm.getFullName())
      if (useCachedObject)
        ptmNamesCache.put(psPtmShortName, ptmNames)
    }

    //*************** PtmEvidences ***************//    

    //Get PtmEvidences referencing PtmSpecificity of specified PeptidePtm. Creates corresponding OM objects
    val psPtmEvidences = psPtmSpecificity.getEvidences()
    var ptmEvidences = new ArrayBuffer[PtmEvidence](psPtmEvidences.size())
    var precursorFound = false

    for (psPtmEvid <- collectionAsScalaIterable(psPtmEvidences)) {

      if (psPtmEvid.getType().equals(psPrecursorType))
        precursorFound = true

      ptmEvidences += new PtmEvidence(
        ionType = IonTypes.withName(psPtmEvid.getType().name()),
        composition = psPtmEvid.getComposition(),
        monoMass = psPtmEvid.getMonoMass(),
        averageMass = psPtmEvid.getAverageMass(),
        isRequired = psPtmEvid.getIsRequired())
    }
    if (!precursorFound) {

      //"Precursor" PtmEvidence for this Ptm
      var precursorEvidence: PtmEvidence = null;
      val psPtmEvidencesIt = psPtmSpecificity.getPtm().getEvidences().iterator();
      while (psPtmEvidencesIt.hasNext() && precursorEvidence == null) {
        val psPtmEvidence = psPtmEvidencesIt.next();

        if (psPtmEvidence.getType().equals(psPrecursorType)) {
          precursorEvidence = new PtmEvidence(
            ionType = IonTypes.Precursor,
            psPtmEvidence.getComposition(),
            psPtmEvidence.getMonoMass(),
            psPtmEvidence.getAverageMass(),
            psPtmEvidence.getIsRequired())
        }
      }

      ptmEvidences += precursorEvidence
    }

    // Create OM PtmDefinition from ORM PtmSpecificity    
    val ptmDef = new PtmDefinition(
      id = psPtmSpecificity.getId(),
      location = psPtmSpecificity.getLocation(),
      names = ptmNames,
      ptmEvidences = ptmEvidences.toArray,
      residue = characterToScalaChar(psPtmSpecificity.getResidue),
      classification = psPtmSpecificity.getClassification().getName(),
      ptmId = psPtmSpecificity.getPtm().getId())
    if (useCachedObject)
      ptmDefinitionsCache.put(psPtmSpecificity.getId(), ptmDef);

    ptmDef
  }

}
