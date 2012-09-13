package fr.proline.core.om.storer.msi

import java.sql.Timestamp

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable

import com.weiglewilczek.slf4s.Logging

import fr.proline.core.om.model.msi.InstrumentConfig
import fr.proline.core.om.model.msi.MSISearch
import fr.proline.core.om.model.msi.Ms2Query
import fr.proline.core.om.model.msi.MsQuery
import fr.proline.core.om.model.msi.Peaklist
import fr.proline.core.om.model.msi.PeaklistSoftware
import fr.proline.core.om.model.msi.Peptide
import fr.proline.core.om.model.msi.PeptideMatch
import fr.proline.core.om.model.msi.ProteinMatch
import fr.proline.core.om.model.msi.PtmDefinition
import fr.proline.core.om.model.msi.ResultSet
import fr.proline.core.om.model.msi.SeqDatabase
import fr.proline.core.om.model.msi.SequenceMatch
import fr.proline.core.orm.msi.ResultSet.Type
import fr.proline.core.orm.msi.repository.MsiEnzymeRepository
import fr.proline.core.orm.msi.repository.MsiInstrumentConfigRepository
import fr.proline.core.orm.msi.repository.MsiPeaklistSoftwareRepository
import fr.proline.core.orm.msi.repository.MsiPeptideRepository
import fr.proline.core.orm.msi.repository.MsiSeqDatabaseRepository
import fr.proline.core.orm.msi.repository.ScoringRepository
import fr.proline.core.orm.msi.MsiSearch
import fr.proline.core.orm.msi.ProteinMatchSeqDatabaseMapPK
import fr.proline.core.orm.msi.SequenceMatchPK
import fr.proline.core.orm.pdi.repository.SeqDatabaseRepository
import fr.proline.core.orm.ps.repository.PsPeptideRepository
import fr.proline.core.orm.ps.repository.PsPtmRepository
import fr.proline.core.orm.uds.repository.UdsEnzymeRepository
import fr.proline.core.orm.uds.repository.UdsInstrumentConfigurationRepository
import fr.proline.core.orm.uds.repository.UdsPeaklistSoftwareRepository
import fr.proline.core.orm.utils.JPAUtil
import fr.proline.core.orm.utils.StringUtils
import fr.proline.repository.DatabaseConnector
import javax.persistence.EntityManager
import javax.persistence.Persistence

/**
 * JPA implementation of ResultSet storer.
 *
 * @param msiDb DatabaseConnector to Msi Db
 * @param psDb DatabaseConnector to Ps Db
 * @param udsDb DatabaseConnector to Uds Db
 * @param pdiDb DatabaseConnector to Pdi Db
 */
class JPARsStorer(private val msiDb: DatabaseConnector,
  private val psDb: DatabaseConnector,
  private val udsDb: DatabaseConnector,
  private val pdiDb: DatabaseConnector) extends Logging {
  
  type MsiResultSet = fr.proline.core.orm.msi.ResultSet
  type MsiPeakList = fr.proline.core.orm.msi.Peaklist
  type MsiPeaklistSoftware = fr.proline.core.orm.msi.PeaklistSoftware
  type MsiSearchSetting = fr.proline.core.orm.msi.SearchSetting
  type MsiInstrumentConfig = fr.proline.core.orm.msi.InstrumentConfig
  type MsiEnzyme = fr.proline.core.orm.msi.Enzyme
  type MsiSearchSettingsSeqDatabaseMap = fr.proline.core.orm.msi.SearchSettingsSeqDatabaseMap
  type MsiSeqDatabase = fr.proline.core.orm.msi.SeqDatabase
  type MsiPtmSpecificity = fr.proline.core.orm.msi.PtmSpecificity
  type MsiUsedPtm = fr.proline.core.orm.msi.UsedPtm
  type MsiPeptideMatch = fr.proline.core.orm.msi.PeptideMatch
  type MsiPeptide = fr.proline.core.orm.msi.Peptide
  type MsiMsQuery = fr.proline.core.orm.msi.MsQuery
  type MsiSpectrum = fr.proline.core.orm.msi.Spectrum
  type MsiProteinMatch = fr.proline.core.orm.msi.ProteinMatch
  type MsiBioSequence = fr.proline.core.orm.msi.BioSequence
  type MsiProteinMatchSeqDatabaseMap = fr.proline.core.orm.msi.ProteinMatchSeqDatabaseMap
  type MsiSequenceMatch = fr.proline.core.orm.msi.SequenceMatch

  type PsPeptide = fr.proline.core.orm.ps.Peptide
  type PsPtm = fr.proline.core.orm.ps.Ptm

  type PdiBioSequence = fr.proline.core.orm.pdi.BioSequence

  var knownSpectrumIdByTitle:Map[String, Int] = null
  
  def storeResultSet(resultSet: ResultSet, spectrumIdByTitle: Map[String,Int] ) {
    knownSpectrumIdByTitle = spectrumIdByTitle
    storeResultSet(resultSet)
  }
  
  /**
   * Stores an OM ResultSet into Msi Db using instance msiDb, psDb, udsDb and pdiDb DatabaseConnectors.
   *
   * @param resultSet ResultSet object, must not be {{{null}}} and must be a newly created instance ("In memory" Id < 0).
   */
  def storeResultSet(resultSet: ResultSet) {

    if (resultSet == null) {
      throw new IllegalArgumentException("ResultSet is null")
    }

    val msiEmf = Persistence.createEntityManagerFactory(JPAUtil.PersistenceUnitNames.MSI_Key.getPersistenceUnitName,
      msiDb.getEntityManagerSettings)
    val psEmf = Persistence.createEntityManagerFactory(JPAUtil.PersistenceUnitNames.PS_Key.getPersistenceUnitName,
      psDb.getEntityManagerSettings)
    val udsEmf = Persistence.createEntityManagerFactory(JPAUtil.PersistenceUnitNames.UDS_Key.getPersistenceUnitName,
      udsDb.getEntityManagerSettings)
    val pdiEmf = Persistence.createEntityManagerFactory(JPAUtil.PersistenceUnitNames.PDI_Key.getPersistenceUnitName,
      pdiDb.getEntityManagerSettings)

    var msiEm: EntityManager = null

    var psEm: EntityManager = null
    var udsEm: EntityManager = null
    var pdiEm: EntityManager = null

    try {
      msiEm = msiEmf.createEntityManager
      psEm = psEmf.createEntityManager
      udsEm = udsEmf.createEntityManager
      pdiEm = pdiEmf.createEntityManager

      storeResultSet(msiEm, psEm, udsEm, pdiEm, resultSet)
    } finally {

      /* Close all created EntityManagers */
      if (pdiEm != null) {
        try {
          pdiEm.close()
        } catch {
          case exClose => logger.error("Unable to close pdiEm", exClose)
        }
      }

      if (udsEm != null) {
        try {
          udsEm.close()
        } catch {
          case exClose => logger.error("Unable to close udsEm", exClose)
        }
      }

      if (psEm != null) {
        try {
          psEm.close()
        } catch {
          case exClose => logger.error("Unable to close psEm", exClose)
        }
      }

      if (msiEm != null) {
        try {
          msiEm.close()
        } catch {
          case exClose => logger.error("Unable to close msiEm", exClose)
        }
      }

    } // End try - finally block on EntityManagers

  }

  /**
   * Stores an OM ResultSet into Msi Db using given msiEm, psEm, udsEm and pdiEm JPA EntityManagers.
   * EntityManagers must be in "open state", transactions will be started on msiEm and psEm EntityManagers.
   *
   * @param resultSet ResultSet object, must not be {{{null}}} and must be a newly created instance ("In memory" Id < 0).
   */
  def storeResultSet(msiEm: EntityManager, psEm: EntityManager, udsEm: EntityManager, pdiEm: EntityManager, resultSet: ResultSet) {

    checkEntityManager(msiEm)

    if (resultSet == null) {
      throw new IllegalArgumentException("ResultSet is null")
    }

    /* Global caches (entities shared by ResultSets: Decoy, children...) :
     * OM Id (can be "In memory") -> ORM loaded or persisted entity */
    val knownResultSets = mutable.Map.empty[Int, MsiResultSet]
    val knownMsiSearchs = mutable.Map.empty[Int, MsiSearch]
    val knownPeakLists = mutable.Map.empty[Int, MsiPeakList]
    val knownSeqDatabases = mutable.Map.empty[Int, MsiSeqDatabase]
    val knownPtmSpecificities = mutable.Map.empty[Int, MsiPtmSpecificity]
    val msiPeptides = mutable.Map.empty[PeptideIdent, MsiPeptide] // Retrieved and created Msi Peptides
    val knownPeptideMatches = mutable.Map.empty[Int, MsiPeptideMatch]
    val knownMsQueries = mutable.Map.empty[Int, MsiMsQuery]

    /* Postponed tasks : after having persisted other ResultSet entities and committed msiEm transaction
     * Contains ResultSet specific entities */
    val proteinMatchSeqDatabases = mutable.Map.empty[MsiResultSet, mutable.Map[MsiProteinMatch, Array[Int]]]
    val sequenceMatches = mutable.Map.empty[MsiResultSet, mutable.Map[MsiProteinMatch, Array[SequenceMatch]]]

    val msiTransaction = msiEm.getTransaction
    var msiTransacOk: Boolean = false

    var msiResultSet: MsiResultSet = null

    logger.debug("Starting first Msi Db transaction")

    try {
      msiTransaction.begin()
      msiTransacOk = false

      if (resultSet.id > 0) {
        throw new UnsupportedOperationException("Updating a ResultSet is not supported yet !")
      } else {
        logger.info("Persisting a newly created ResultSet")

        msiResultSet = loadOrCreateResultSet(knownResultSets,
          knownMsiSearchs,
          knownPeakLists,
          knownSeqDatabases,
          knownPtmSpecificities,
          msiPeptides,
          knownPeptideMatches,
          knownMsQueries,
          msiEm, psEm, udsEm, pdiEm,
          resultSet, proteinMatchSeqDatabases, sequenceMatches)
      }

      msiTransaction.commit()
      msiTransacOk = true
    } finally {

      /* Check msiTransaction integrity */
      if ((msiTransaction != null) && !msiTransacOk) {
        try {
          msiTransaction.rollback()
        } catch {
          case ex => logger.error("Error rollbacking Msi Db transaction", ex)
        }
      }

    } // End try - finally block on msiTransaction

    if (msiTransacOk) {
      resultSet.id = msiResultSet.getId // Update OM entity with persisted Primary key

      if (!proteinMatchSeqDatabases.isEmpty || !sequenceMatches.isEmpty) {
        /* Postponed proteinMatchSeqDatabases and sequenceMatches handling in a new transaction */
        logger.debug("Starting second Msi Db transaction")

        try {
          msiTransaction.begin()
          msiTransacOk = false

          for (pMSDEntry <- proteinMatchSeqDatabases) {
            /* Re-attach MsiResultSet entity to current Msi transaction */
            val currentMsiResultSet = msiEm.merge(pMSDEntry._1)

            /* Handle ProteinMatchSeqDatabaseMap after having persisted MsiProteinMatches, SeqDatabases and the MsiResultSet */
            for (currentPMSDEntry <- pMSDEntry._2; seqDatabaseId <- currentPMSDEntry._2) {
              bindProteinMatchSeqDatabaseMap(msiEm, currentPMSDEntry._1.getId, seqDatabaseId, knownSeqDatabases, currentMsiResultSet)
            }

          }

          for (smEntry <- sequenceMatches) {

            /* Handle sequenceMatches after having persisted MsiProteinMatches, MsiPeptideMatches and the MsiResultSet */
            for (currentSMEntry <- smEntry._2; sequenceMatch <- currentSMEntry._2) {
              createSequenceMatch(msiEm, sequenceMatch, currentSMEntry._1.getId, msiPeptides, knownPeptideMatches, smEntry._1.getId)
            }

          }

          msiTransaction.commit()
          msiTransacOk = true
        } finally {

          /* Check msiTransaction integrity */
          if ((msiTransaction != null) && !msiTransacOk) {
            try {
              msiTransaction.rollback()
            } catch {
              case ex => logger.error("Error rollbacking Msi Db transaction", ex)
            }
          }

        } // End try - finally block on msiTransaction

      } // End if (postponed Maps are not empty)

    } // End if (first msiTransac is Ok)

  }

  /**
   * Retrieves a known ResultSet or an already persisted ResultSet or persists a new ResultSet entity into Msi Db.
   *
   * @param resultSet ResultSet object, must not be {{{null}}}.
   * @param msiEm Msi EntityManager must have a valid transaction started.
   */
  def loadOrCreateResultSet(knownResultSets: mutable.Map[Int, MsiResultSet],
    knownMsiSearchs: mutable.Map[Int, MsiSearch],
    knownPeakLists: mutable.Map[Int, MsiPeakList],
    knownSeqDatabases: mutable.Map[Int, MsiSeqDatabase],
    knownPtmSpecificities: mutable.Map[Int, MsiPtmSpecificity],
    msiPeptides: mutable.Map[PeptideIdent, MsiPeptide],
    knownPeptideMatches: mutable.Map[Int, MsiPeptideMatch],
    knownMsQueries: mutable.Map[Int, MsiMsQuery],
    msiEm: EntityManager, psEm: EntityManager, udsEm: EntityManager, pdiEm: EntityManager,
    resultSet: ResultSet,
    proteinMatchSeqDatabases: mutable.Map[MsiResultSet, mutable.Map[MsiProteinMatch, Array[Int]]],
    sequenceMatches: mutable.Map[MsiResultSet, mutable.Map[MsiProteinMatch, Array[SequenceMatch]]]): MsiResultSet = {

    // TODO Check this algo (QUANTITATION = resultSet.isQuantified ? )
    def parseType(resultSet: ResultSet): Type = {
      assert(resultSet != null, "ResultSet is null")

      if (resultSet.isNative) {

        if (resultSet.isDecoy) {
          Type.DECOY_SEARCH
        } else {
          Type.SEARCH
        }

      } else {
        Type.USER
      }

    }

    if (knownResultSets == null) {
      throw new IllegalArgumentException("KnownResultSets is null")
    }

    checkEntityManager(msiEm)

    checkEntityManager(pdiEm)

    if (resultSet == null) {
      throw new IllegalArgumentException("ResultSet is null")
    }

    if (proteinMatchSeqDatabases == null) {
      throw new IllegalArgumentException("ProteinMatchSeqDatabases map is null")
    }

    if (sequenceMatches == null) {
      throw new IllegalArgumentException("SequenceMatches map is null")
    }

    val omResultSetId = resultSet.id

    val knownMsiResultSet = knownResultSets.get(omResultSetId)

    if (knownMsiResultSet.isDefined) {
      knownMsiResultSet.get
    } else {

      if (omResultSetId > 0) {
        val foundMsiResultSet = msiEm.getReference(classOf[MsiResultSet], omResultSetId)

        knownResultSets += omResultSetId -> foundMsiResultSet

        foundMsiResultSet
      } else {
        val msiResultSet = new MsiResultSet()
        msiResultSet.setDescription(resultSet.description)
        // ResultSet.modificationTimestamp field is initialized by MsiResultSet constructor
        msiResultSet.setName(resultSet.name)
        msiResultSet.setType(parseType(resultSet))

        val msiSearch = loadOrCreateMsiSearch(knownMsiSearchs, knownPeakLists, knownSeqDatabases, knownPtmSpecificities, msiEm, udsEm, pdiEm, psEm, resultSet.getMSISearchId, resultSet.msiSearch)
        msiResultSet.setMsiSearch(msiSearch)

        /* Check associated decoy ResultSet */
        val omDecoyResultSetId = resultSet.getDecoyResultSetId

        val knownMsiDecoyRs = knownResultSets.get(omDecoyResultSetId)

        val msiDecoyRs =
          if (knownMsiDecoyRs.isDefined) {
            knownMsiDecoyRs.get
          } else {

            if (omDecoyResultSetId > 0) {
              val foundMsiDecoyRs = msiEm.getReference(classOf[MsiResultSet], omDecoyResultSetId)

              knownResultSets += omDecoyResultSetId -> foundMsiDecoyRs

              foundMsiDecoyRs
            } else {

              if ((resultSet.decoyResultSet != null) && resultSet.decoyResultSet.isDefined) {

                loadOrCreateResultSet(knownResultSets,
                  knownMsiSearchs,
                  knownPeakLists,
                  knownSeqDatabases,
                  knownPtmSpecificities,
                  msiPeptides,
                  knownPeptideMatches,
                  knownMsQueries,
                  msiEm, psEm, udsEm, pdiEm,
                  resultSet.decoyResultSet.get,
                  proteinMatchSeqDatabases,
                  sequenceMatches)

              } else {
                null
              }

            }

          }

        msiResultSet.setDecoyResultSet(msiDecoyRs)

        msiEm.persist(msiResultSet)

        knownResultSets += omResultSetId -> msiResultSet

        logger.debug("Msi ResultSet {" + omResultSetId + "} persisted")

        /* Peptides & PeptideMatches */
        retrievePeptides(msiEm, psEm, resultSet.peptides, msiPeptides)

        val scoringRepository = new ScoringRepository(msiEm)

        for (peptMatch <- resultSet.peptideMatches) {
          createPeptideMatch(knownPeptideMatches, knownMsQueries, msiEm, scoringRepository,
            peptMatch, msiPeptides, msiResultSet, msiSearch)
        }

        val knownPMSD = proteinMatchSeqDatabases.get(msiResultSet)

        val currentProteinMatchSeqDatabases = if (knownPMSD.isDefined) {
          knownPMSD.get
        } else {
          mutable.Map.empty[MsiProteinMatch, Array[Int]]
        }

        val knownSM = sequenceMatches.get(msiResultSet)

        val currentSequenceMatches = if (knownSM.isDefined) {
          knownSM.get
        } else {
          mutable.Map.empty[MsiProteinMatch, Array[SequenceMatch]]
        }

        /* Proteins (BioSequence) & ProteinMatches */
        for (protMatch <- resultSet.proteinMatches) {
          val msiProteinMatch = createProteinMatch(msiEm, pdiEm, scoringRepository, protMatch, msiResultSet)

          if ((protMatch.seqDatabaseIds != null) && !protMatch.seqDatabaseIds.isEmpty) {
            currentProteinMatchSeqDatabases += msiProteinMatch -> protMatch.seqDatabaseIds
          }

          if (protMatch.sequenceMatches != null) {
            currentSequenceMatches += msiProteinMatch -> protMatch.sequenceMatches
          }

        } // End loop for each ProteinMatch

        if (!currentProteinMatchSeqDatabases.isEmpty) {
          proteinMatchSeqDatabases += msiResultSet -> currentProteinMatchSeqDatabases
        }

        if (!currentSequenceMatches.isEmpty) {
          sequenceMatches += msiResultSet -> currentSequenceMatches
        }

        // TODO handle ResultSet.children    Uniquement pour le grouping ?

        msiResultSet
      } // End if (omResultSetId <= 0)

    } // End if (msiResultSet is not in knownResultSets)

  }

  /**
   * Retrieves a known MsiSearch or an already persisted MsiSearch or persists a new MsiSearch entity into Msi Db.
   *
   * @param msiSearchId MsiSearch OM Id
   * @param search MSISearch object, must not be {{{null}}} if msiSearchId <= 0.
   */
  def loadOrCreateMsiSearch(knownMsiSearchs: mutable.Map[Int, MsiSearch],
    knownPeakLists: mutable.Map[Int, MsiPeakList],
    knownSeqDatabases: mutable.Map[Int, MsiSeqDatabase],
    knownPtmSpecificities: mutable.Map[Int, MsiPtmSpecificity],
    msiEm: EntityManager, udsEm: EntityManager, pdiEm: EntityManager, psEm: EntityManager,
    msiSearchId: Int, search: MSISearch): MsiSearch = {

    if (knownMsiSearchs == null) {
      throw new IllegalArgumentException("KnownMsiSearchs is null")
    }

    checkEntityManager(msiEm)

    val knownMsiSearch = knownMsiSearchs.get(msiSearchId)

    if (knownMsiSearch.isDefined) {
      knownMsiSearch.get
    } else {

      if (msiSearchId > 0) {
        val foundMsiSearch = msiEm.getReference(classOf[MsiSearch], msiSearchId)

        knownMsiSearchs += msiSearchId -> foundMsiSearch

        foundMsiSearch
      } else if (search == null) {
        throw new IllegalArgumentException("MsiSearch is mandatory")
      } else {
        val omMsiSearchId = search.id

        val msiSearch = new MsiSearch()
        msiSearch.setDate(new Timestamp(search.date.getTime))
        msiSearch.setQueriesCount(search.queriesCount)
        msiSearch.setResultFileName(search.resultFileName)
        msiSearch.setResultFileDirectory(search.resultFileDirectory)
        msiSearch.setSearchedSequencesCount(search.searchedSequencesCount)
        msiSearch.setJobNumber(search.jobNumber)

        // TODO handle serializedProperties

        msiSearch.setSubmittedQueriesCount(search.submittedQueriesCount)
        msiSearch.setTitle(search.title)
        msiSearch.setUserEmail(search.userEmail)
        msiSearch.setUserName(search.userName)

        msiSearch.setPeaklist(loadOrCreatePeakList(knownPeakLists, msiEm, udsEm, search.peakList))

        msiSearch.setSearchSetting(loadOrCreateSearchSetting(knownSeqDatabases, knownPtmSpecificities, msiEm, udsEm, pdiEm, psEm, search))

        msiEm.persist(msiSearch)
        logger.debug("MsiSearch {" + omMsiSearchId + "} presisted")

        knownMsiSearchs += omMsiSearchId -> msiSearch

        msiSearch
      }

    } // End if (msiSearch is not in knownMsiSearchs)

  }

  /**
   * Retrieves a known Peaklist or an already persisted Peaklist or persists a new Peaklist entity into Msi Db.
   *
   * @param peakList Peaklist object, must not be {{{null}}}.
   */
  def loadOrCreatePeakList(knownPeakLists: mutable.Map[Int, MsiPeakList],
    msiEm: EntityManager, udsEm: EntityManager,
    peakList: Peaklist): MsiPeakList = {

    if (knownPeakLists == null) {
      throw new IllegalArgumentException("KnownPeakLists is null")
    }

    checkEntityManager(msiEm)

    if (peakList == null) {
      throw new IllegalArgumentException("PeakList is null")
    }

    val omPeakListId = peakList.id

    val knownMsiPeakList = knownPeakLists.get(omPeakListId)

    if (knownMsiPeakList.isDefined) {
      knownMsiPeakList.get
    } else {

      if (omPeakListId > 0) {
        val foundMsiPeakList = msiEm.getReference(classOf[MsiPeakList], omPeakListId)

        knownPeakLists += omPeakListId -> foundMsiPeakList

        foundMsiPeakList
      } else {
        val msiPeakList = new MsiPeakList()
        msiPeakList.setMsLevel(peakList.msLevel)
        msiPeakList.setPath(peakList.path)
        msiPeakList.setRawFileName(peakList.rawFileName)

        // TODO handle serializedProperties

        // TODO Set meaningful value in PeakList.spectrumDataCompression field
        msiPeakList.setSpectrumDataCompression("none")
        msiPeakList.setType(peakList.fileType)

        if (peakList.peaklistSoftware != null) {
          msiPeakList.setPeaklistSoftware(loadOrCreatePeaklistSoftware(msiEm, udsEm, peakList.peaklistSoftware))
        }

        // TODO handle PeakList.children    Uniquement pour le grouping ?

        msiEm.persist(msiPeakList)

        knownPeakLists += omPeakListId -> msiPeakList

        logger.debug("Msi PeakList {" + omPeakListId + "} persisted")

        msiPeakList
      } // End if (omPeakListId <= 0)

    } // End if (msiPeakList is not in knownPeakLists)

  }

  /**
   * Retrieves an already persisted PeaklistSoftware or persists a new PeaklistSoftware entity into Msi Db from an existing Uds Db entity.
   *
   * @param peaklistSoftware PeaklistSoftware object, must not be {{{null}}}.
   */
  def loadOrCreatePeaklistSoftware(msiEm: EntityManager, udsEm: EntityManager,
    peaklistSoftware: PeaklistSoftware): MsiPeaklistSoftware = {

    checkEntityManager(msiEm)

    checkEntityManager(udsEm)

    if (peaklistSoftware == null) {
      throw new IllegalArgumentException("PeaklistSoftware is null")
    }

    val omPeakListSoftwareId = peaklistSoftware.id

    var msiPeaklistSoftware: MsiPeaklistSoftware = null

    if (omPeakListSoftwareId > 0) {
      msiPeaklistSoftware = msiEm.find(classOf[MsiPeaklistSoftware], omPeakListSoftwareId)
    }

    if (msiPeaklistSoftware == null) {
      msiPeaklistSoftware = (new MsiPeaklistSoftwareRepository(msiEm)).findPeaklistSoftForNameAndVersion(peaklistSoftware.name, peaklistSoftware.version)

      if (msiPeaklistSoftware != null) {
        peaklistSoftware.id = msiPeaklistSoftware.getId // Update OM entity with persisted Primary key
      }

    }

    if (msiPeaklistSoftware == null) {
      val udsPeaklistSoftware = (new UdsPeaklistSoftwareRepository(udsEm)).findPeaklistSoftForNameAndVersion(peaklistSoftware.name, peaklistSoftware.version)

      if (udsPeaklistSoftware == null) {
        throw new IllegalArgumentException("PeaklistSoftware [" + peaklistSoftware.name + "] [" + peaklistSoftware.version + "] NOT found in Uds Db")
      } else {
        msiPeaklistSoftware = new MsiPeaklistSoftware(udsPeaklistSoftware)

        msiEm.persist(msiPeaklistSoftware)

        peaklistSoftware.id = udsPeaklistSoftware.getId // Update OM entity with persisted Primary key

        logger.debug("Msi PeaklistSoftware #" + udsPeaklistSoftware.getId + " persisted")
      }

    }

    msiPeaklistSoftware
  }

  /**
   * Retrieves an already persisted SearchSetting or persists a new SearchSetting entity into Msi Db.
   *
   * @param search Associated MSISearch object, must not be {{{null}}} and must be attached to {{{msiEm}}} persistence context before calling this method.
   */
  def loadOrCreateSearchSetting(knownSeqDatabases: mutable.Map[Int, MsiSeqDatabase],
    knownPtmSpecificities: mutable.Map[Int, MsiPtmSpecificity],
    msiEm: EntityManager, udsEm: EntityManager, pdiEm: EntityManager, psEm: EntityManager,
    search: MSISearch): MsiSearchSetting = {

    checkEntityManager(msiEm)

    checkEntityManager(udsEm)

    checkEntityManager(pdiEm)

    if (search == null) {
      throw new IllegalArgumentException("Search is null")
    }

    val searchSettings = search.searchSettings
    val omSearchSettingsId = searchSettings.id

    if (omSearchSettingsId > 0) { // searchSettings is mandatory
      msiEm.getReference(classOf[MsiSearchSetting], omSearchSettingsId)
    } else {
      val msiSearchSetting = new MsiSearchSetting()
      msiSearchSetting.setIsDecoy(searchSettings.isDecoy)
      msiSearchSetting.setMaxMissedCleavages(searchSettings.maxMissedCleavages)
      msiSearchSetting.setPeptideChargeStates(searchSettings.ms1ChargeStates)
      msiSearchSetting.setPeptideMassErrorTolerance(searchSettings.ms1ErrorTol)
      msiSearchSetting.setPeptideMassErrorToleranceUnit(searchSettings.ms1ErrorTolUnit)
      msiSearchSetting.setQuantitation(searchSettings.quantitation)

      // TODO handle serializedProperties

      msiSearchSetting.setSoftwareName(searchSettings.softwareName)
      msiSearchSetting.setSoftwareVersion(searchSettings.softwareVersion)
      msiSearchSetting.setTaxonomy(searchSettings.taxonomy)

      msiSearchSetting.setInstrumentConfig(loadOrCreateInstrumentConfig(msiEm, new MsiInstrumentConfigRepository(msiEm),
        new UdsInstrumentConfigurationRepository(udsEm), searchSettings.instrumentConfig))

      val msiEnzymeRepo = new MsiEnzymeRepository(msiEm)
      val udsEnzymeRepo = new UdsEnzymeRepository(udsEm)

      for (enzyme <- searchSettings.usedEnzymes) {
        msiSearchSetting.addEnzyme(loadOrCreateEnzyme(msiEm, msiEnzymeRepo, udsEnzymeRepo, enzyme))
      }

      msiEm.persist(msiSearchSetting)
      logger.debug("Msi SearchSettings {" + omSearchSettingsId + "} persisted")

      /* Task done after persisting msiSearchSetting */
      val msiSeqDatabaseRepo = new MsiSeqDatabaseRepository(msiEm)
      val pdiSeqDatabaseRepo = new SeqDatabaseRepository(pdiEm);

      for (seqDatabase <- searchSettings.seqDatabases) {
        val msiSearchSettingsSeqDatabaseMap = new MsiSearchSettingsSeqDatabaseMap()
        msiSearchSettingsSeqDatabaseMap.setSearchedSequencesCount(search.searchedSequencesCount)

        // TODO handle serializedProperties

        msiSearchSettingsSeqDatabaseMap.setSearchSetting(msiSearchSetting) // msiSearchSetting must be in persistence context
        msiSearchSetting.addSearchSettingsSeqDatabaseMap(msiSearchSettingsSeqDatabaseMap) // Reverse association

        val msiSeqDatabase = loadOrCreateSeqDatabase(knownSeqDatabases, msiEm, msiSeqDatabaseRepo, pdiSeqDatabaseRepo, seqDatabase)
        if(msiSeqDatabase != null) {
        	msiSearchSettingsSeqDatabaseMap.setSeqDatabase(msiSeqDatabase) // msiSeqDatabase must be in persistence context
        	msiSeqDatabase.addSearchSettingsSeqDatabaseMap(msiSearchSettingsSeqDatabaseMap) // Reverse association        

        	msiEm.persist(msiSearchSettingsSeqDatabaseMap)
        	logger.debug("Msi SettingsSeqDatabaseMap SearchSetting {" + omSearchSettingsId + "} SeqDatabase #" + msiSeqDatabase.getId + " persisted")
        }
      }

      val psPtmRepo = new PsPtmRepository(psEm)

      for (variablePtmDef <- searchSettings.variablePtmDefs) {
        bindPtmSpecificity(knownPtmSpecificities, msiEm, psPtmRepo, variablePtmDef, false, msiSearchSetting)
      }

      for (fixedPtmDef <- searchSettings.fixedPtmDefs) {
        bindPtmSpecificity(knownPtmSpecificities, msiEm, psPtmRepo, fixedPtmDef, false, msiSearchSetting)
      }

      msiSearchSetting
    } // End if (omSearchSettingsId <= 0)

  }

  /**
   * Retrieves an already persisted InstrumentConfig or persists a new InstrumentConfig entity into Msi Db from an existing Uds Db entity.
   *
   * @param instrumentConfig InstrumentConfig object, must not be {{{null}}}.
   */
  def loadOrCreateInstrumentConfig(msiEm: EntityManager,
    msiInstrumentConfigRepo: MsiInstrumentConfigRepository, udsInstrumentConfigRepo: UdsInstrumentConfigurationRepository,
    instrumentConfig: InstrumentConfig): MsiInstrumentConfig = {

    checkEntityManager(msiEm)

    if (msiInstrumentConfigRepo == null) {
      throw new IllegalArgumentException("MsiInstrumentConfigRepo is null")
    }

    if (udsInstrumentConfigRepo == null) {
      throw new IllegalArgumentException("UdsInstrumentConfigRepo is null")
    }

    if (instrumentConfig == null) {
      throw new IllegalArgumentException("InstrumentConfig is null")
    }

    var msiInstrumentConfig: MsiInstrumentConfig = null

    if (instrumentConfig.id > 0) {
      msiInstrumentConfig = msiEm.find(classOf[MsiInstrumentConfig], instrumentConfig.id)
    }

    if (msiInstrumentConfig == null) {
      msiInstrumentConfig = msiInstrumentConfigRepo.findInstrumConfForNameAndMs1AndMsn(instrumentConfig.name,
        instrumentConfig.ms1Analyzer, instrumentConfig.msnAnalyzer)
    }

    if (msiInstrumentConfig == null) {
      val udsInstrumentConfiguration = udsInstrumentConfigRepo.findInstrumConfForNameAndMs1AndMsn(instrumentConfig.name,
        instrumentConfig.ms1Analyzer, instrumentConfig.msnAnalyzer)

      if (udsInstrumentConfiguration == null) {
        throw new IllegalArgumentException("InstrumentConfiguration [" + instrumentConfig.name +
          "] [" + instrumentConfig.ms1Analyzer + "] NOT found in Uds Db")
      } else {
        msiInstrumentConfig = new MsiInstrumentConfig(udsInstrumentConfiguration)

        msiEm.persist(msiInstrumentConfig)
        logger.debug("Msi InstrumentConfig #" + udsInstrumentConfiguration.getId + " persisted")
      }

    }

    msiInstrumentConfig
  }

  /**
   * Retrieves an already persisted Enzyme or persists a new Enzyme entity into Msi Db from an existing Uds Db entity.
   *
   * @param enzymeName Name of the Enzyme (ignoring case), must not be empty.
   */
  def loadOrCreateEnzyme(msiEm: EntityManager,
    msiEnzymeRepo: MsiEnzymeRepository, udsEnzymeRepo: UdsEnzymeRepository,
    enzymeName: String): MsiEnzyme = {

    checkEntityManager(msiEm)

    if (msiEnzymeRepo == null) {
      throw new IllegalArgumentException("MsiEnzymeRepo is null")
    }

    if (udsEnzymeRepo == null) {
      throw new IllegalArgumentException("UdsEnzymeRepo is null")
    }

    if (StringUtils.isEmpty(enzymeName)) {
      throw new IllegalArgumentException("Invalid enzymeName")
    }

    var msiEnzyme: MsiEnzyme = msiEnzymeRepo.findEnzymeForName(enzymeName)

    if (msiEnzyme == null) {
      val udsEnzyme = udsEnzymeRepo.findEnzymeForName(enzymeName)

      if (udsEnzyme == null) {
        throw new IllegalArgumentException("Enzyme [" + enzymeName + "] NOT found in Uds Db")
      } else {
        msiEnzyme = new MsiEnzyme(udsEnzyme)

        msiEm.persist(msiEnzyme)
        logger.debug("Msi Enzyme #" + udsEnzyme.getId + " persisted")
      }

    }

    msiEnzyme
  }

  /**
   * Retrieves a known SeqDatabase or an already persisted SeqDatabase or persists a new SeqDatabase entity into Msi Db from an existing Pdi Db entity.
   * If SeqDatabase doesn't exist in PDIdb, return null
   *
   * @param seqDatabase SeqDatabase object, must not be {{{null}}}.
   */
  def loadOrCreateSeqDatabase(knownSeqDatabases: mutable.Map[Int, MsiSeqDatabase],
    msiEm: EntityManager,
    msiSeqDatabaseRepo: MsiSeqDatabaseRepository, pdiSeqDatabaseRepo: SeqDatabaseRepository,
    seqDatabase: SeqDatabase): MsiSeqDatabase = {

    if (knownSeqDatabases == null) {
      throw new IllegalArgumentException("KnownSeqDatabases is null")
    }

    checkEntityManager(msiEm)

    if (msiSeqDatabaseRepo == null) {
      throw new IllegalArgumentException("MsiSeqDatabaseRepo is null")
    }

    if (pdiSeqDatabaseRepo == null) {
      throw new IllegalArgumentException("PdiSeqDatabaseRepo is null")
    }

    if (seqDatabase == null) {
      throw new IllegalArgumentException("SeqDatabase is null")
    }

    val omSeqDatabaseId = seqDatabase.id

    val knownMsiSeqDatabase = knownSeqDatabases.get(omSeqDatabaseId)

    if (knownMsiSeqDatabase.isDefined) {
      knownMsiSeqDatabase.get
    } else {
      var msiSeqDatabase: MsiSeqDatabase = null

      if (omSeqDatabaseId > 0) {
        /* Try to load from Msi Db by Id */
        msiSeqDatabase = msiEm.find(classOf[MsiSeqDatabase], omSeqDatabaseId)

        if (msiSeqDatabase != null) {
          knownSeqDatabases += omSeqDatabaseId -> msiSeqDatabase
        }

      }

      if (msiSeqDatabase == null) {
        /* Try to load from Msi Db by name and Fasta file path */
        msiSeqDatabase = msiSeqDatabaseRepo.findSeqDatabaseForNameAndFastaAndVersion(seqDatabase.name, seqDatabase.filePath)

        if (msiSeqDatabase != null) {
          knownSeqDatabases += omSeqDatabaseId -> msiSeqDatabase

          seqDatabase.id = msiSeqDatabase.getId // Update OM entity with persisted Primary key
        } // End if (msiSeqDatabase is not null)

      } // End if (msiSeqDatabase is null)

      if (msiSeqDatabase == null) {
        /* Try to load from Pdi Db by name and Fasta file path */
        val pdiSeqDatabaseInstance = pdiSeqDatabaseRepo.findSeqDbInstanceWithNameAndFile(seqDatabase.name, seqDatabase.filePath)

        if (pdiSeqDatabaseInstance == null) {
          logger.warn("SeqDatabase not defined in PDI db, protein matches will not be associated to bioSequence");
           knownSeqDatabases += omSeqDatabaseId -> null
//          throw new IllegalArgumentException("SeqDatabase [" + seqDatabase.name + "] [" + seqDatabase.filePath + "] NOT found in Pdi Db");
        } else {
          /* Create derived Msi entity */
          msiSeqDatabase = new MsiSeqDatabase(pdiSeqDatabaseInstance);

          msiEm.persist(msiSeqDatabase);

          knownSeqDatabases += omSeqDatabaseId -> msiSeqDatabase

          seqDatabase.id = pdiSeqDatabaseInstance.getId // Update OM entity with Primary key

          logger.debug("Msi SeqDatabase #" + pdiSeqDatabaseInstance.getId + " persisted")
        } // End if (pdiSeqDatabaseInstance is not null)

      } // End if (msiSeqDatabase is null)

      msiSeqDatabase
    } // End if (msiSeqDatabase not in knownSeqDatabases)

  }

  /**
   * Retrieves a known PtmSpecificity or an already persisted PtmSpecificity or persists a new PtmSpecificity entity into Msi Db from an existing Ps Db entity.
   *
   * @param ptmDefinition PtmDefinition object, must not be {{{null}}}.
   */
  def loadOrCreatePtmSpecificity(knownPtmSpecificities: mutable.Map[Int, MsiPtmSpecificity],
    msiEm: EntityManager,
    psPtmRepo: PsPtmRepository,
    ptmDefinition: PtmDefinition): MsiPtmSpecificity = {

    if (knownPtmSpecificities == null) {
      throw new IllegalArgumentException("KnownPtmSpecificities is null")
    }

    checkEntityManager(msiEm)

    if (ptmDefinition == null) {
      throw new IllegalArgumentException("PtmDefinition is null")
    }

    if (psPtmRepo == null) {
      throw new IllegalArgumentException("PsPtmRepo is null")
    }

    val knownMsiPtmSpecificity = knownPtmSpecificities.get(ptmDefinition.id)

    if (knownMsiPtmSpecificity.isDefined) {
      knownMsiPtmSpecificity.get
    } else {
      var msiPtmSpecificity: MsiPtmSpecificity = null

      if (ptmDefinition.id > 0) {
        /* Try to load from Msi Db by Id */
        msiPtmSpecificity = msiEm.find(classOf[MsiPtmSpecificity], ptmDefinition.id)
      }

      if (msiPtmSpecificity == null) {
        /* Try to load from Ps Db by name, location and residue */
        val residueStr: String = if (ptmDefinition.residue == '\0') {
          null
        } else {
          "" + ptmDefinition.residue
        }

        val psPtmSpecificity = psPtmRepo.findPtmSpecificityForNameLocResidu(ptmDefinition.names.shortName, ptmDefinition.location, residueStr)

        if (psPtmSpecificity == null) {
          logger.warn("PtmSpecificity [" + ptmDefinition.names.shortName + "] NOT found in Ps Db")
        } else {
          /* Avoid duplicate msiPtmSpecificity entities creation */
          msiPtmSpecificity = msiEm.find(classOf[MsiPtmSpecificity], psPtmSpecificity.getId)

          if (msiPtmSpecificity == null) {
            /* Create derived Msi entity */
            msiPtmSpecificity = new MsiPtmSpecificity(psPtmSpecificity)

            msiEm.persist(msiPtmSpecificity)
            logger.debug("Msi PtmSpecificity #" + psPtmSpecificity.getId + " persisted")
          }

        } // End if (psPtmSpecificity is not null)

      } // End if (msiPtmSpecificity is null)

      if (msiPtmSpecificity != null) {
        knownPtmSpecificities += ptmDefinition.id -> msiPtmSpecificity
      } // End if (msiPtmSpecificity is not null)

      msiPtmSpecificity
    } // End if (msiPtmSpecificity not found in knownPtmSpecificities)

  }

  /**
   * Retrieves Peptides from Msi Db or persists new Peptide entities into Msi Db from existing or '''created''' Ps Db entities.
   *
   * @param msiEm Msi EntityManager must have a valid transaction started.
   * @param psEm A transaction may be started on psEm to persist new Peptides in Ps Db.
   * @param peptides Array of Peptide objects to fetch, must not be {{{null}}}.
   * @param msiPeptides Mutable Map will contain fetched and created Msi Peptide entities accessed by PeptideIdent(sequence, ptmString). Map must not be {{{null}}}.
   * The map can contain already fetched Peptides in current Msi transaction.
   */
  def retrievePeptides(msiEm: EntityManager, psEm: EntityManager,
    peptides: Array[Peptide],
    msiPeptides: mutable.Map[PeptideIdent, MsiPeptide]) {

    /**
     * Build a Java List<Integer> from a Scala Collection[Int].
     */
    def buildIdsList(omIds: Traversable[Int]): java.util.List[Integer] = {
      val javaIds = new java.util.ArrayList[Integer](omIds.size)

      for (omId <- omIds) {
        javaIds.add(Integer.valueOf(omId))
      }

      javaIds
    }

    checkEntityManager(msiEm)

    checkEntityManager(psEm)

    if (peptides == null) {
      throw new IllegalArgumentException("Peptides array is null")
    }

    if (msiPeptides == null) {
      throw new IllegalArgumentException("MsiPeptides map is null")
    }

    /* These are mutable Collections : found and created Peptides are removed by the algo */
    val remainingPeptides = mutable.Map.empty[PeptideIdent, Peptide]
    val remainingOmPeptidesIds = mutable.Set.empty[Int] // Keep OM Peptide Ids > 0

    for (peptide <- peptides) {
      val peptIdent = new PeptideIdent(peptide.sequence, peptide.ptmString)

      if (!msiPeptides.contains(peptIdent)) {
        remainingPeptides += peptIdent -> peptide

        val omPeptideId = peptide.id

        if (omPeptideId > 0) {
          remainingOmPeptidesIds += omPeptideId
        }

      }

    }

    /* Retrieve all known Peptides from Msi Db by omIds > 0 */
    if (!remainingOmPeptidesIds.isEmpty) {
      logger.debug("Trying to retrieve " + remainingOmPeptidesIds.size + " Peptides from Msi by Ids")

      val msiPeptideRepo = new MsiPeptideRepository(msiEm)

      val foundMsiPeptides = msiPeptideRepo.findPeptidesForIds(buildIdsList(remainingOmPeptidesIds))

      if ((foundMsiPeptides != null) && !foundMsiPeptides.isEmpty()) {

        for (msiPeptide <- foundMsiPeptides) {
          val peptideId = msiPeptide.getId
          val peptIdent = new PeptideIdent(msiPeptide.getSequence, msiPeptide.getPtmString)

          msiPeptides += peptIdent -> msiPeptide

          remainingPeptides.remove(peptIdent)
          remainingOmPeptidesIds.remove(peptideId)
        }

      }

    }

    /* Retrieve all known Peptides from Ps Db by omIds > 0 */
    val psPeptideRepo = new PsPeptideRepository(psEm)

    if (!remainingOmPeptidesIds.isEmpty) {
      logger.debug("Trying to retrieve " + remainingOmPeptidesIds.size + " Peptides from Ps by Ids")

      val foundPsPeptides = psPeptideRepo.findPeptidesForIds(buildIdsList(remainingOmPeptidesIds))

      if ((foundPsPeptides != null) && !foundPsPeptides.isEmpty()) {

        for (psPeptide <- foundPsPeptides) {
          val peptideId = psPeptide.getId
          val peptIdent = new PeptideIdent(psPeptide.getSequence, psPeptide.getPtmString)

          var msiPeptide = msiEm.find(classOf[MsiPeptide], peptideId)

          if (msiPeptide == null) {
            /* Create derived Msi entity */
            msiPeptide = new MsiPeptide(psPeptide)

            msiEm.persist(msiPeptide)

            logger.debug("Msi Peptide #" + peptideId + " persisted")
          }

          msiPeptides += peptIdent -> msiPeptide

          remainingPeptides.remove(peptIdent)
          remainingOmPeptidesIds.remove(msiPeptide.getId)
        }

      }

    }

    if (!remainingOmPeptidesIds.isEmpty) {
      throw new IllegalArgumentException("Peptides (" + remainingOmPeptidesIds.mkString(", ") + ") NOT found in Ps Db")
    }

    if (!remainingPeptides.isEmpty) {
      logger.debug("Trying to retrieve " + remainingPeptides.size + " Peptides from Ps by (sequence, ptmString)")

      for (peptIdent <- remainingPeptides.keySet.toSet[PeptideIdent]) {
        /* Try to load from Ps Db by sequence and ptmString */
        val foundPsPeptide = psPeptideRepo.findPeptideForSequenceAndPtmStr(peptIdent.sequence, peptIdent.ptmString)

        if (foundPsPeptide != null) {
          val peptideId = foundPsPeptide.getId

          var msiPeptide = msiEm.find(classOf[MsiPeptide], peptideId)

          if (msiPeptide == null) {
            /* Create derived Msi entity */
            msiPeptide = new MsiPeptide(foundPsPeptide)

            msiEm.persist(msiPeptide)

            logger.debug("Msi Peptide #" + peptideId + " persisted")
          }

          msiPeptides += peptIdent -> msiPeptide

          val omPeptide = remainingPeptides.remove(peptIdent)
          if (omPeptide.isDefined) {
            omPeptide.get.id = peptideId // Update OM entity with persisted Primary key
          }

        } // End if (foundPsPeptide is not null)

      } // End loop for each remainingPeptide => search in Ps

    } // End if (remainingPeptides is not empty)

    if (!remainingPeptides.isEmpty) {
      /* Create new Peptides into Ps Db */
      val createdPsPeptides = persistPsPeptides(psEm, remainingPeptides.toMap[PeptideIdent, Peptide])

      for (peptideEntry <- createdPsPeptides) {
        val peptIdent = peptideEntry._1
        val psPeptide = peptideEntry._2

        /* Create derived Msi entity */
        val msiPeptide = new MsiPeptide(psPeptide)

        msiEm.persist(msiPeptide)

        msiPeptides += peptIdent -> msiPeptide

        val omPeptide = remainingPeptides.remove(peptIdent)
        if (omPeptide.isDefined) {
          omPeptide.get.id = psPeptide.getId // Update OM entity with persisted Primary key
        }

        logger.debug("Msi Peptide #" + psPeptide.getId + " persisted")
      } // End loop for each createdPsPeptide => create in Msi

    } // End if (remainingPeptides is not empty)

    if (!remainingPeptides.isEmpty) {
      logger.error("There are " + remainingPeptides.size + " unknown Peptides in ResultSet")
    } // End if (remainingPeptides is not empty)

  }

  /**
   * Persists new Peptide entities into Ps Db.
   *
   * @param psEm A transaction will be started on psEm to persist new Peptides in Ps Db.
   * @param peptides Map of Peptide objects to create, accessed by PeptideIdent(sequence, ptmString). Must not be {{{null}}}.
   * @return Map of created Ps Peptide entities accessed by PeptideIdent.
   */
  def persistPsPeptides(psEm: EntityManager, peptides: Map[PeptideIdent, Peptide]): Map[PeptideIdent, PsPeptide] = {

    checkEntityManager(psEm)

    if (peptides == null) {
      throw new IllegalArgumentException("Peptides map is null")
    }

    logger.debug("Creating " + peptides.size + " Peptides into Ps Db")

    val createdPsPeptides = Map.newBuilder[PeptideIdent, PsPeptide]

    val psTransaction = psEm.getTransaction
    var psTransacOk = false

    logger.debug("Starting Ps Db transaction")

    try {
      psTransaction.begin()
      psTransacOk = false

      val PsPtmRepository = new PsPtmRepository(psEm)

      for (peptideEntry <- peptides.toMap[PeptideIdent, Peptide]) {
        val peptIdent = peptideEntry._1
        val peptide = peptideEntry._2
        val newPsPeptide = new PsPeptide
        newPsPeptide.setSequence(peptIdent.sequence)
        newPsPeptide.setPtmString(peptIdent.ptmString)
        newPsPeptide.setCalculatedMass(peptide.calculatedMass)

        // TODO handle serializedProperties
        // TODO handle atomLabel

//      TODO : ADD PTM REFERENCE FROM PEPTIDE
//        if ((peptide.ptms != null) && !peptide.ptms.isEmpty) {
//          // TODO handle Peptides.ptms
//          throw new UnsupportedOperationException("Peptides PTM are not supported yet !")
//        }

        psEm.persist(newPsPeptide)

        createdPsPeptides += peptIdent -> newPsPeptide

        logger.debug("Ps Peptide {" + peptide.id + ", "+newPsPeptide.getId+"} persisted")
      } // End loop for each Peptides

      psTransaction.commit()
      psTransacOk = true

      createdPsPeptides.result
    } finally {

      /* Check psTransaction integrity */
      if ((psTransaction != null) && !psTransacOk) {

        try {
          psTransaction.rollback()
        } catch {
          case ex => logger.error("Error rollbacking Ps Db transaction", ex)
        }

      }

    } // End try - finally block on psTransaction

  }

  /**
   * Retrieves a known PeptideMatch or persists a new PeptideMatch entity into Msi Db.
   *
   * @param peptideMatch PeptideMatch object, must not be {{{null}}}.
   * @param msiPeptides Map of already fetched Msi Peptide entities accessed by PeptideIdent, must not be {{{null}}}.
   * @param msiResultSet Associated Msi ResultSet entity, must be attached to {{{msiEm}}} persistence context before calling this method.
   * @param msiSearch Associated MsiSearch entity, must be attached to {{{msiEm}}} persistence context before calling this method.
   */
  def createPeptideMatch(knownPeptideMatches: mutable.Map[Int, MsiPeptideMatch],
    knownMsQueries: mutable.Map[Int, MsiMsQuery],
    msiEm: EntityManager,
    scoringRepository: ScoringRepository,
    peptideMatch: PeptideMatch,
    msiPeptides: mutable.Map[PeptideIdent, MsiPeptide],
    msiResultSet: MsiResultSet,
    msiSearch: MsiSearch): MsiPeptideMatch = {

    if (knownPeptideMatches == null) {
      throw new IllegalArgumentException("KnownPeptideMatches is null")
    }

    checkEntityManager(msiEm)

    if (scoringRepository == null) {
      throw new IllegalArgumentException("ScoringRepository is null")
    }

    if (peptideMatch == null) {
      throw new IllegalArgumentException("PeptideMatch is null")
    }

    if (msiPeptides == null) {
      throw new IllegalArgumentException("MsiPeptides map is null")
    }

    if (msiResultSet == null) {
      throw new IllegalArgumentException("MsiResultSet is null")
    }

    val omPeptideMatchId = peptideMatch.id

    val knownMsiPeptideMatch = knownPeptideMatches.get(omPeptideMatchId)

    if (knownMsiPeptideMatch.isDefined) {
      knownMsiPeptideMatch.get
    } else {

      if (omPeptideMatchId > 0) {
        throw new UnsupportedOperationException("Updating existing PeptideMatch #" + omPeptideMatchId + " is not supported")
      } else {
        val msiPeptideMatch = new MsiPeptideMatch()
        msiPeptideMatch.setDeltaMoz(peptideMatch.deltaMoz)
        msiPeptideMatch.setFragmentMatchCount(peptideMatch.fragmentMatchesCount)
        msiPeptideMatch.setIsDecoy(peptideMatch.isDecoy)
        msiPeptideMatch.setMissedCleavage(peptideMatch.missedCleavage)

        val msiPeptide = msiPeptides.get(new PeptideIdent(peptideMatch.peptide.sequence, peptideMatch.peptide.ptmString))

        val msiPeptideId: Int =
          if (msiPeptide.isDefined) {
            msiPeptide.get.getId
          } else {
            -1
          }

        if (msiPeptideId <= 0) {
          throw new IllegalArgumentException("Unknown Msi Peptide Id: " + msiPeptideId)
        } else {
          msiPeptideMatch.setPeptideId(msiPeptideId)
        }

        msiPeptideMatch.setRank(peptideMatch.rank)

        msiPeptideMatch.setResultSet(msiResultSet) // msiResultSet must be in persistence context

        msiPeptideMatch.setScore(peptideMatch.score)

        val msiScoringId = scoringRepository.getScoringIdForType(peptideMatch.scoreType)

        if (msiScoringId == null) {
          throw new IllegalArgumentException("Scoring [" + peptideMatch.scoreType + "] NOT found in Msi Db")
        } else {
          msiPeptideMatch.setScoringId(msiScoringId.intValue)
        }

        // TODO handle serializedProperties

        if (peptideMatch.msQuery == null) {
          throw new IllegalArgumentException("MsQuery is mandatory in PeptideMatch")
        }

        val msiMsQuery = loadOrCreateMsQuery(knownMsQueries, msiEm, peptideMatch.msQuery, msiSearch) // msiSearch must be in persistence context

        msiPeptideMatch.setMsQuery(msiMsQuery) // msiMsQuery must be in persistence context
        msiMsQuery.addPeptideMatch(msiPeptideMatch) // Reverse association

        msiPeptideMatch.setCharge(msiMsQuery.getCharge)
        msiPeptideMatch.setExperimentalMoz(msiMsQuery.getMoz)

        /* Check associated best PeptideMatch */
        val bestOmPeptideMatchId = peptideMatch.getBestChildId

        val knownMsiBestChild = knownPeptideMatches.get(bestOmPeptideMatchId)

        val msiBestChild = if (knownMsiBestChild.isDefined) {
          knownMsiBestChild.get
        } else {

          if (bestOmPeptideMatchId > 0) {
            val foundBestChild = msiEm.getReference(classOf[MsiPeptideMatch], bestOmPeptideMatchId)

            knownPeptideMatches += bestOmPeptideMatchId -> foundBestChild

            foundBestChild
          } else {

            if ((peptideMatch.bestChild != null) && peptideMatch.bestChild.isDefined) {
              createPeptideMatch(knownPeptideMatches, knownMsQueries, msiEm, scoringRepository,
                peptideMatch.bestChild.get, msiPeptides, msiResultSet, msiSearch)
            } else {
              null
            }

          }

        }

        msiPeptideMatch.setBestPeptideMatch(msiBestChild)

        // TODO handle PeptideMatch.children    Uniquement pour le grouping ?

        msiEm.persist(msiPeptideMatch)

        knownPeptideMatches += omPeptideMatchId -> msiPeptideMatch

        logger.debug("Msi PeptideMatch {" + omPeptideMatchId + "} persisted")

        msiPeptideMatch
      } // End if (omPeptideMatchId <= 0)

    } // End if (msiPeptideMatch is not in knownPeptideMatches)

  }

  /**
   * Retrieves a known MsQuery or an already persisted MsQuery or persists a new MsQuery entity into Msi Db.
   *
   * @param msQuery MsQuery object, must not be {{{null}}}.
   * @param msiSearch Associated MsiSearch entity, must be attached to {{{msiEm}}} persistence context before calling this method.
   */
  def loadOrCreateMsQuery(knownMsQueries: mutable.Map[Int, MsiMsQuery],
    msiEm: EntityManager,
    msQuery: MsQuery,
    msiSearch: MsiSearch): MsiMsQuery = {

    if (knownMsQueries == null) {
      throw new IllegalArgumentException("KnownMsQueries is null")
    }

    checkEntityManager(msiEm)

    if (msQuery == null) {
      throw new IllegalArgumentException("MsQuery is null")
    }

    if (msiSearch == null) {
      throw new IllegalArgumentException("MsiSearch is null")
    }

    val omMsQueryId = msQuery.id

    val knownMsiMsQuery = knownMsQueries.get(omMsQueryId)

    if (knownMsiMsQuery.isDefined) {
      knownMsiMsQuery.get
    } else {

      if (omMsQueryId > 0) {
        val foundMsiMsQuery = msiEm.getReference(classOf[MsiMsQuery], omMsQueryId)

        foundMsiMsQuery.setMsiSearch(msiSearch)

        knownMsQueries += omMsQueryId -> foundMsiMsQuery

        foundMsiMsQuery
      } else {
        val msiMsQuery = new MsiMsQuery()
        msiMsQuery.setCharge(msQuery.charge)
        msiMsQuery.setInitialId(msQuery.initialId)
        msiMsQuery.setMoz(msQuery.moz)
        msiMsQuery.setMsiSearch(msiSearch) // msiSearch must be in persistence context

        // TODO handle serializedProperties

        if (msQuery.isInstanceOf[Ms2Query]) {
          val ms2Query = msQuery.asInstanceOf[Ms2Query]
          var omSpectrumId = ms2Query.spectrumId

          val newSpectumID = if(ms2Query.spectrumTitle == null || knownSpectrumIdByTitle == null) None else knownSpectrumIdByTitle.get(ms2Query.spectrumTitle)
          if(newSpectumID.isDefined)
            omSpectrumId = newSpectumID.get
            
          // TODO Spectrums should be persisted before RsStorer (with PeakList entity)
          if (omSpectrumId > 0) {
            val msiSpectrum = msiEm.find(classOf[MsiSpectrum], omSpectrumId)

            if (msiSpectrum == null) {
              throw new IllegalArgumentException("Spectrum #" + omSpectrumId + " NOT found in Msi Db")
            } else {
              val spectrumTitle = msiSpectrum.getTitle

              if ((spectrumTitle != null) && spectrumTitle.equals(ms2Query.spectrumTitle)) {
                msiMsQuery.setSpectrum(msiSpectrum)
              } else {
                throw new IllegalArgumentException("Invalid Spectrum.title")
              }

            }

          } else {
            logger.warn("Invalid Spectrum Id: " + omSpectrumId)
          }

        } // End if (msQuery is a Ms2Query)

        msiEm.persist(msiMsQuery)

        knownMsQueries += omMsQueryId -> msiMsQuery

        logger.debug("Msi MsQuery {" + omMsQueryId + "} persisted")

        // TODO handle MsQueryProperties

        msiMsQuery
      } // End if (omMsQueryId <= 0)

    } // End if (msiMsQuery is not in knownMsQueries)

  }

  /**
   * Persists a new ProteinMatch entity into Msi Db.
   *
   * @param proteinMatch ProteinMatch object, must not be {{{null}}}.
   * @param msiResultSet Associated Msi ResultSet entity, must be attached to {{{msiEm}}} persistence context before calling this method.
   */
  def createProteinMatch(msiEm: EntityManager, pdiEm: EntityManager,
    scoringRepository: ScoringRepository,
    proteinMatch: ProteinMatch,
    msiResultSet: MsiResultSet): MsiProteinMatch = {

    checkEntityManager(msiEm)

    if (scoringRepository == null) {
      throw new IllegalArgumentException("ScoringRepository is null")
    }

    if (proteinMatch == null) {
      throw new IllegalArgumentException("ProteinMatch is null")
    }

    val omProteinMatchId = proteinMatch.id

    if (omProteinMatchId > 0) {
      throw new UnsupportedOperationException("Updating existing ProteinMatch #" + omProteinMatchId + " is not supported")
    }

    if (msiResultSet == null) {
      throw new IllegalArgumentException("MsiResultSet is null")
    }

    /* Create new MsiProteinMatch */
    val msiProteinMatch = new MsiProteinMatch()
    msiProteinMatch.setAccession(proteinMatch.accession)

    val omProteinId = proteinMatch.getProteinId

    if (omProteinId > 0) {
      val msiBioSequence = loadOrCreateBioSequence(msiEm, pdiEm, omProteinId)

      if (msiBioSequence != null) {
        msiProteinMatch.setBioSequenceId(msiBioSequence.getId)
      }

    } else {
      val protein = proteinMatch.protein

      if ((protein != null) && protein.isDefined) {
        val definedProtein = protein.get

        logger.warn("Unknown Protein {" + omProteinId + "} sequence [" + definedProtein.sequence + ']')
      }

    }

    msiProteinMatch.setIsLastBioSequence(proteinMatch.isLastBioSequence)
    msiProteinMatch.setCoverage(proteinMatch.coverage)
    msiProteinMatch.setDescription(proteinMatch.description)
    msiProteinMatch.setGeneName(proteinMatch.geneName)
    msiProteinMatch.setIsDecoy(proteinMatch.isDecoy)

    // TODO handle peptideCount

    msiProteinMatch.setPeptideCount(proteinMatch.peptideMatchesCount)
    msiProteinMatch.setResultSet(msiResultSet) // msiResultSet must be in persistence context
    msiProteinMatch.setScore(proteinMatch.score)

    if (proteinMatch.scoreType != null) {
      val msiScoringId = scoringRepository.getScoringIdForType(proteinMatch.scoreType)

      if (msiScoringId == null) {
        throw new IllegalArgumentException("Scoring [" + proteinMatch.scoreType + "] NOT found in Msi Db")
      } else {
        msiProteinMatch.setScoringId(msiScoringId.intValue)
      }

    }

    // TODO handle serializedProperties

    val omTaxonId = proteinMatch.taxonId

    if (omTaxonId != 0) {
      msiProteinMatch.setTaxonId(omTaxonId)
    }

    msiEm.persist(msiProteinMatch)
    logger.debug("Msi ProteinMatch {" + omProteinMatchId + "} persisted")

    // TODO handle ProteinMatchProperties

    msiProteinMatch
  }

  /**
   * Retrieves BioSequence (Protein) from Msi Db or persists new BioSequence entity into Msi Db from existing Pdi Db entity.
   *
   * @param proteinId BioSequence (Protein) Primary key, must be > 0 and denote on existing BioSequence entity in Pdi Db.
   */
  def loadOrCreateBioSequence(msiEm: EntityManager, pdiEm: EntityManager, proteinId: Int): MsiBioSequence = {

    checkEntityManager(msiEm)

    checkEntityManager(pdiEm)

    if (proteinId <= 0) {
      throw new IllegalArgumentException("Invalid proteinId")
    }

    var msiBioSequence: MsiBioSequence = msiEm.find(classOf[MsiBioSequence], proteinId)

    if (msiBioSequence == null) {
      val pdiBioSequence = pdiEm.find(classOf[PdiBioSequence], proteinId)

      if (pdiBioSequence == null) {
        throw new IllegalArgumentException("BioSequence #" + proteinId + " NOT found in Pdi Db")
      } else {
        msiBioSequence = new MsiBioSequence(pdiBioSequence)

        msiEm.persist(msiBioSequence)
        logger.debug("Msi BioSequence #" + pdiBioSequence.getId + " persisted")
      }

    }

    msiBioSequence
  }

  /**
   * Persists a new SequenceMatch entity into Msi Db.
   *
   * @param sequenceMatch SequenceMatch object, must not be {{{null}}}.
   * @param msiProteinMatchId ProteinMatch Primary key, must be > 0 and denote an existing ProteinMatch entity in Msi Db (Msi transaction committed).
   * @param msiPeptides Map of already fetched Msi Peptide entities accessed by PeptideIdent, must not be {{{null}}}.
   * Peptide ids must be effective Msi Db Primary keys (Msi transaction committed).
   * @param knownPeptideMatches Map of already created Msi PeptideMatch entities accessed by OM Ids, must not be {{{null}}}.
   * PeptideMatch ids must be effective Msi Db Primary keys (Msi transaction committed).
   * @param msiResultSetId ResultSet Primary key, must be > 0 and denote an existing ResultSet entity in Msi Db (Msi transaction committed).
   */
  def createSequenceMatch(msiEm: EntityManager,
    sequenceMatch: SequenceMatch,
    msiProteinMatchId: Int,
    msiPeptides: mutable.Map[PeptideIdent, MsiPeptide],
    knownPeptideMatches: mutable.Map[Int, MsiPeptideMatch],
    msiResultSetId: Int): MsiSequenceMatch = {

    checkEntityManager(msiEm)

    if (sequenceMatch == null) {
      throw new IllegalArgumentException("SequenceMatch is null")
    }

    if (msiProteinMatchId <= 0) {
      throw new IllegalArgumentException("Invalid Msi ProteinMatch Id")
    }

    if (msiPeptides == null) {
      throw new IllegalArgumentException("MsiPeptides map is null")
    }

    if (knownPeptideMatches == null) {
      throw new IllegalArgumentException("KnownPeptideMatches is null")
    }

    if (msiResultSetId <= 0) {
      throw new IllegalArgumentException("Invalid Msi ResultSet Id")
    }

    /**
     * Retrieves Msi Peptide entity Primary key from given OM Id or Peptide object.
     */
    def retrieveMsiPeptideId(peptideId: Int, peptide: Option[Peptide]): Int = {
      var msiPeptideId: Int = -1

      if (peptideId > 0) {
        msiPeptideId = peptideId
      } else {

        if ((peptide != null) && peptide.isDefined) {
          val definedPeptide = peptide.get
          val peptideIdent = new PeptideIdent(definedPeptide.sequence, definedPeptide.ptmString)

          val knownMsiPeptide = msiPeptides.get(peptideIdent)

          if (knownMsiPeptide.isDefined) {
            msiPeptideId = knownMsiPeptide.get.getId
          }

        }

      }

      msiPeptideId
    }

    /**
     * Retrieves Msi PeptideMatch entity Primary key from given OM Id.
     */
    def retrieveMsiPeptideMatchId(peptideMatchId: Int): Int = {
      var msiPeptideMatchId: Int = -1

      if (peptideMatchId > 0) {
        msiPeptideMatchId = peptideMatchId
      } else {
        val knownMsiPeptideMatch = knownPeptideMatches.get(peptideMatchId)

        if (knownMsiPeptideMatch.isDefined) {
          msiPeptideMatchId = knownMsiPeptideMatch.get.getId
        }

      }

      msiPeptideMatchId
    }

    val msiSequenceMatchPK = new SequenceMatchPK()
    msiSequenceMatchPK.setProteinMatchId(msiProteinMatchId)

    /* Retrieve Peptide Id from Msi */
    val msiPeptideId = retrieveMsiPeptideId(sequenceMatch.getPeptideId, sequenceMatch.peptide)

    if (msiPeptideId > 0) {
      msiSequenceMatchPK.setPeptideId(msiPeptideId)
    } else {
      throw new IllegalArgumentException("Unknown Msi Peptide Id: " + msiPeptideId)
    }

    msiSequenceMatchPK.setStart(sequenceMatch.start)
    msiSequenceMatchPK.setStop(sequenceMatch.end)

    val msiSequenceMatch = new MsiSequenceMatch()
    msiSequenceMatch.setId(msiSequenceMatchPK)

    /* Retrieve best PeptideMatch Id from Msi */
    val msiPeptideMatchId = retrieveMsiPeptideMatchId(sequenceMatch.getBestPeptideMatchId)

    if (msiPeptideMatchId > 0) {
      msiSequenceMatch.setBestPeptideMatchId(msiPeptideMatchId)
    } else {
      throw new IllegalArgumentException("Unknown Msi best PeptideMatch Id: " + msiPeptideMatchId)
    }

    msiSequenceMatch.setIsDecoy(sequenceMatch.isDecoy)
    msiSequenceMatch.setResidueAfter("" + sequenceMatch.residueAfter)
    msiSequenceMatch.setResidueBefore("" + sequenceMatch.residueBefore)
    msiSequenceMatch.setResultSetId(msiResultSetId)

    // TODO handle serializedProperties

    msiEm.persist(msiSequenceMatch)
    logger.debug("Msi SequenceMatch for ProteinMatch #" + msiProteinMatchId + " Peptide #" + msiPeptideId + " persisted")

    msiSequenceMatch
  }

  /* Private methods */
  private def checkEntityManager(em: EntityManager) {

    if ((em == null) || !em.isOpen) {
      throw new IllegalArgumentException("Invalid EntityManager")
    }

  }

  /**
   *  @param msiSearchSetting Associated Msi SearchSetting entity, must be attached to {{{msiEm}}} persistence context before calling this method.
   */
  private def bindPtmSpecificity(knownPtmSpecificities: mutable.Map[Int, MsiPtmSpecificity],
    msiEm: EntityManager,
    psPtmRepo: PsPtmRepository,
    ptmDefinition: PtmDefinition, isFixed: Boolean,
    msiSearchSetting: MsiSearchSetting) {

    checkEntityManager(msiEm)

    assert(ptmDefinition != null, "PtmDefinition is null")

    assert(msiSearchSetting != null, "MsiSearchSetting is null")

    val msiUsedPtm = new MsiUsedPtm()
    msiUsedPtm.setIsFixed(isFixed)
    msiUsedPtm.setShortName(ptmDefinition.names.shortName)
    // TODO UsedPtm.type field should be removed from Msi Db schema

    val msiPtmSpecificity = loadOrCreatePtmSpecificity(knownPtmSpecificities, msiEm, psPtmRepo, ptmDefinition)

    if (msiPtmSpecificity == null) {
      throw new IllegalArgumentException("Unknown PtmSpecificity [" + ptmDefinition.names.shortName + ']')
    } else {
      msiUsedPtm.setPtmSpecificity(msiPtmSpecificity)
      msiPtmSpecificity.addUsedPtm(msiUsedPtm) // Reverse association
    }

    msiUsedPtm.setSearchSetting(msiSearchSetting) // msiSearchSetting must be in persistence context
    msiSearchSetting.addUsedPtms(msiUsedPtm) // Reverse association

    msiEm.persist(msiUsedPtm)
    logger.debug("Msi UsedPtm name [" + ptmDefinition.names.shortName + "] PtmSpecificity #" + msiPtmSpecificity.getId + " persisted")
  }

  private def bindProteinMatchSeqDatabaseMap(msiEm: EntityManager,
    msiProteinMatchId: Int,
    seqDatabaseId: Int,
    knownSeqDatabases: mutable.Map[Int, MsiSeqDatabase],
    msiResultSet: MsiResultSet) {

    checkEntityManager(msiEm)

    assert(msiProteinMatchId > 0, "Invalid Msi ProteinMatch Id")

    assert(knownSeqDatabases != null, "KnownSeqDatabases is null")

    assert(msiResultSet != null, "MsiResultSet is null")

    def retrieveMsiSeqDatabase(seqDatabseId: Int): MsiSeqDatabase = {
      val knownMsiSeqDatabase = knownSeqDatabases.get(seqDatabseId)

      if (knownMsiSeqDatabase.isDefined) {
        knownMsiSeqDatabase.get
      } else {

        if (seqDatabseId > 0) {
          val foundMsiSeqDatabase = msiEm.find(classOf[MsiSeqDatabase], seqDatabseId)

          if (foundMsiSeqDatabase != null) {
            knownSeqDatabases += seqDatabseId -> foundMsiSeqDatabase
          }

          foundMsiSeqDatabase
        } else {
          null
        }

      }

    }

    /* No need to re-attach msiSeqDatabase object: only retrieve persisted Primary key */
    val msiSeqDatabase = retrieveMsiSeqDatabase(seqDatabaseId)

    if (msiSeqDatabase == null) {
      logger.warn("Unknown Msi SeqDatabase Id: " + seqDatabaseId)
    } else {
      val proteinMatchSeqDatabaseMapPK = new ProteinMatchSeqDatabaseMapPK()
      proteinMatchSeqDatabaseMapPK.setProteinMatchId(msiProteinMatchId)
      proteinMatchSeqDatabaseMapPK.setSeqDatabaseId(msiSeqDatabase.getId)

      val msiProteinMatchSeqDatabase = new MsiProteinMatchSeqDatabaseMap()
      // TODO handle serializedProperties
      msiProteinMatchSeqDatabase.setId(proteinMatchSeqDatabaseMapPK)
      msiProteinMatchSeqDatabase.setResultSetId(msiResultSet)

      msiEm.persist(msiProteinMatchSeqDatabase)
      logger.debug("Msi ProteinMatchSeqDatabase for ProteinMatch #" + msiProteinMatchId + " SeqDatabase #" + msiSeqDatabase.getId + " persisted")
    }

  }

}
