package fr.proline.core.algo.msi

import scala.collection.mutable.{ArrayBuffer,HashMap,HashSet}
import fr.proline.core.om.model.msi._
import com.weiglewilczek.slf4s.Logging
import fr.proline.core.algo.msi.validation.TargetDecoyModes
import fr.proline.util.StringUtils.isEmpty
  
object AdditionMode extends Enumeration {
    type AdditionMode = Value
    val Aggregate, Union = Value
}

class ResultSetAdditioner(val resultSetId: Long, val isDecoy: Boolean = false, seqLengthByProtId: Option[Map[Long,Int]] = None) extends Logging {
  
  var mode = AdditionMode.Aggregate
  
  val proteinMatchesByKey = new HashMap[String,ProteinMatch]
  val pepMatchesByPepId = new HashMap[Long,ArrayBuffer[PeptideMatch]]
  val peptideById = new HashMap[Long,Peptide] 
  
  val mergedPeptideMatches = new HashMap[Long, PeptideMatch]
  val mergedProteinMatches = new ArrayBuffer[ProteinMatch]
  val distinctTdModes = new HashSet[String]
  
  def createProteinMatchFrom(proteinMatch: ProteinMatch) : ProteinMatch = {
      // Retrieve all sequence matches of this protein match group
      val seqMatchByPepId = new HashMap[Long,SequenceMatch]
      val seqDatabaseIdSet = new HashSet[Long]
      
      val seqMatches = new ArrayBuffer[SequenceMatch]()
      
      for( seqMatch <- proteinMatch.sequenceMatches )  { 
          val newSeqMatch = seqMatch.copy( resultSetId = resultSetId )
          seqMatches += newSeqMatch          
        }

      if( proteinMatch.seqDatabaseIds != null ) seqDatabaseIdSet ++= proteinMatch.seqDatabaseIds
        
      // Retrieve protein id
      val proteinId = proteinMatch.getProteinId
      
      // Compute protein match sequence coverage  
      var coverage = 0f
      if( proteinId != 0 && seqLengthByProtId.isDefined) {
        val seqLength = seqLengthByProtId.get.get(proteinId)
        if( seqLength == None ) {
          throw new Exception( "can't find a sequence length for the protein with id='"+proteinId+"'" )
        }
        
        val seqPositions = seqMatches.map { s => ( s.start, s.end ) }        
        coverage = Protein.calcSequenceCoverage( seqLength.get, seqPositions )
      }
      
      val newProteinMatch = new ProteinMatch(
                                  id = ProteinMatch.generateNewId,
                                  accession = proteinMatch.accession,
                                  description = proteinMatch.description,
                                  coverage = coverage,
                                  peptideMatchesCount = seqMatches.length,
                                  isDecoy = proteinMatch.isDecoy,
                                  sequenceMatches = seqMatches.toArray,
                                  seqDatabaseIds = seqDatabaseIdSet.toArray,
                                  proteinId = proteinId,
                                  taxonId = proteinMatch.taxonId,
                                  scoreType = proteinMatch.scoreType
                                 )
      newProteinMatch    
  }

  def updateProteinMatch(updated:ProteinMatch, from:ProteinMatch) {
    if (isEmpty(updated.description) && ! isEmpty(from.description))  updated.description = from.description
    // Iterate over sequenceMatches and verify peptide.ids
    val seqMatchesPeptideIds = updated.sequenceMatches.map{ _.getPeptideId }
    for (seqMatch <- from.sequenceMatches) {
      if (!seqMatchesPeptideIds.contains(seqMatch.getPeptideId)) {
        //creates new sequenceMatch for the proteinMatch
        val newSeqMatch = seqMatch.copy( resultSetId = resultSetId )
        updated.sequenceMatches +:= newSeqMatch
      }
    }
    // update seq_database_map
    if (from.seqDatabaseIds != null) {
      updated.seqDatabaseIds ++= from.seqDatabaseIds
    }
  }
  
  def createPeptideMatchFrom(id:Option[Long] = None, peptideMatch: PeptideMatch): PeptideMatch = {
    val newPepMatchId = id.getOrElse(PeptideMatch.generateNewId())
    val childrenIds = new Array[Long](1)
    childrenIds(0) = peptideMatch.id
    peptideMatch.copy(id = newPepMatchId, childrenIds = childrenIds, resultSetId = resultSetId)
  }
  
  def addResultSet(rs:ResultSet) {
  
    logger.info("Start adding ResultSet #"+rs.id)
    val start = System.currentTimeMillis()
    for( peptideMatch <- rs.peptideMatches) {
      if (pepMatchesByPepId.contains(peptideMatch.peptide.id))  {
        val mergedpeptideMatches = pepMatchesByPepId(peptideMatch.peptide.id)
        if(AdditionMode.Aggregate.equals(mode)) {
          if (mergedpeptideMatches(0).score < peptideMatch.score) {
            //update mergedpeptideMatches(0) properties
	        var newPeptideMatch = createPeptideMatchFrom(id = Some(mergedpeptideMatches(0).id), peptideMatch = peptideMatch)
	        // update children Ids, TODO and bestchild
	        newPeptideMatch.childrenIds ++:= mergedpeptideMatches(0).childrenIds
	        //register new PeptideMatch
	        val matches = pepMatchesByPepId.get(peptideMatch.peptide.id).get
	        matches(0) = newPeptideMatch
	        mergedPeptideMatches += (newPeptideMatch.id -> newPeptideMatch)
          } else {
            // update children Ids, TODO and bestchild
            mergedpeptideMatches(0).childrenIds +:= peptideMatch.id
          }
        } else {
	        val newPeptideMatch = createPeptideMatchFrom(peptideMatch = peptideMatch)
	        mergedpeptideMatches += newPeptideMatch
	        val matches = pepMatchesByPepId.get(peptideMatch.peptide.id).get
	        matches += newPeptideMatch
        }
      } else {
        
        val peptide = peptideMatch.peptide
        peptideById+= ( peptide.id -> peptide)
        // creates new PeptideMatch and add it to peptideMatches
        val newPeptideMatch = createPeptideMatchFrom(peptideMatch = peptideMatch)
        pepMatchesByPepId.getOrElseUpdate(peptide.id, new ArrayBuffer[PeptideMatch](1) ) += newPeptideMatch
        mergedPeptideMatches += (newPeptideMatch.id -> newPeptideMatch)
      }
      
    }
    
    // Iterate over protein matches to merge them by a unique key
      for( proteinMatch <- rs.proteinMatches ) {
        var protMatchKey = ""
        if( proteinMatch.getProteinId != 0 ) {
          // Build key using protein id and taxon id if they are defined
          protMatchKey = proteinMatch.getProteinId + "%" + proteinMatch.taxonId
        } else {
          // Else the key in the accession number
          protMatchKey = proteinMatch.accession
        }
        if (proteinMatchesByKey.contains(protMatchKey)) {
          // update sequence_matches (matching new peptide ?), update seq_database_ids
      	  updateProteinMatch(proteinMatchesByKey.get(protMatchKey).get, proteinMatch)
        } else {
          // new proteinMatch : creates sequenceMatches and new proteinMatch
          val newProteinMatch = createProteinMatchFrom(proteinMatch)
          proteinMatchesByKey += (protMatchKey -> newProteinMatch)
          mergedProteinMatches += newProteinMatch
        }
        
      }
      
      distinctTdModes += rs.getTargetDecoyMode.getOrElse("")
      logger.info("ResultSet #"+rs.id+" merged/added in "+(System.currentTimeMillis()-start)+" ms")
  }
  
  
  def toResultSet() : ResultSet = {
	 val start = System.currentTimeMillis()
    val mergedTdModeOpt = if( distinctTdModes.size > 1 ) Some(TargetDecoyModes.MIXED.toString)
    else {
      val tdModeStr = distinctTdModes.first
      if( tdModeStr == "" ) None else Some(tdModeStr)
    }
    
    // Set merged RS properties
    val mergedProperties = new ResultSetProperties()
    mergedProperties.setTargetDecoyMode(mergedTdModeOpt)

    // update bestpeptideMatch for each sequenceMatch
    if (AdditionMode.Union.equals(mode)) {
      for ((pepId, pepMatches) <- pepMatchesByPepId) {
        pepMatches.sortWith(_.score > _.score)
      }
    }
    for (proteinMatch <- mergedProteinMatches) {
      for (seqMatch <- proteinMatch.sequenceMatches) {
        seqMatch.bestPeptideMatchId = pepMatchesByPepId(seqMatch.getPeptideId)(0).id
      }
    }
   
    // Create merged result set    
    val mergedResultSet = new ResultSet(
      id = resultSetId,
      proteinMatches = mergedProteinMatches.toArray,
      peptideMatches = mergedPeptideMatches.values.toArray,
      peptides = peptideById.values.toArray,
      isDecoy = isDecoy,
      isNative = false,
      properties = Some(mergedProperties)
      // FIXME: is this the best solution ???
      //msiSearch = resultSets(0).msiSearch
    )
    
    this.logger.info( "Result Sets have been merged:")
    this.logger.info( "- nb merged protein matches = " + mergedResultSet.proteinMatches.length )
    this.logger.info( "- nb merged peptide matches = " + mergedResultSet.peptideMatches.length )
    this.logger.info( "- nb merged peptides = " + mergedResultSet.peptides.length )

    logger.info("Merged ResultSet #"+resultSetId+" created in "+(System.currentTimeMillis()-start)+" ms")
    mergedResultSet
  }
  

  
}
