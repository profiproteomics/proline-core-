package fr.proline.core.om.model.msi

import scala.collection.mutable.HashMap
import scala.reflect.BeanProperty

import com.codahale.jerkson.JsonSnakeCase
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include

import org.apache.commons.lang3.StringUtils.isNotEmpty
import fr.proline.util.misc.InMemoryIdGen

object Protein extends InMemoryIdGen{
  
  /** A percentage (between 0 and 100) expressing the sequence coverage of the protein */
  def calcSequenceCoverage( protSeqLength: Int, seqPositions: Iterable[Tuple2[Int,Int]] ): Float = {
    
    // Map sequence positions
    val seqIndexSet = new java.util.HashSet[Int]()
    for( seqPosition <- seqPositions ) {
      for(seqIdx <- seqPosition._1 to seqPosition._2 ) {
        seqIndexSet.add(seqIdx)
      }
    }
    
    val coveredSeqLength = seqIndexSet.size() 
    val coverage = 100 * coveredSeqLength /protSeqLength
    
    coverage
  }
  
  import org.biojava.bio.BioException
  import org.biojava.bio.proteomics._
  import org.biojava.bio.seq._
  import org.biojava.bio.symbol._
  
  def calcMass( sequence: String ): Double = {
    try {
    	new MassCalc(SymbolPropertyTable.AVG_MASS, false).getMass( ProteinTools.createProtein(sequence) )
    } catch {
    	case e: BioException => Double.NaN
    }
  }
  
  def calcPI( sequence: String ): Float = {
    try {
    	(new IsoelectricPointCalc().getPI( ProteinTools.createProtein(sequence), true, true) ).toFloat
    } catch {
		  case e:IllegalAlphabetException => Float.NaN
		  case be:BioException =>  Float.NaN
	}    	
  }
  
  // TODO: compute the CRC64
  def calcCRC64( sequence: String ): String = {
    null
  }

}

@JsonSnakeCase
@JsonInclude( Include.NON_NULL )
case class Protein ( // Required fields
                   val id: Long,
                   val sequence: String,
                   var mass: Double,
                   var pi: Float,
                   val crc64: String,
                   val alphabet: String,
                   var properties: Option[ProteinProperties] = None
                   ) {
  
  // Requirements
  require( isNotEmpty(sequence) )
  require( alphabet.matches("aa|rna|dna") ) // TODO: create an enumeration
  
  // Define secondary constructors
  def this( sequence: String, id: Long = Protein.generateNewId(), alphabet: String = "aa" ) = {
      this( id,sequence, Protein.calcMass(sequence),
                         Protein.calcPI(sequence),
                         Protein.calcCRC64(sequence),
                         alphabet )
  }
  
  lazy val length = sequence.length()
  
  def getSequenceCoverage( seqPositions: Array[Tuple2[Int,Int]] ): Float = {
    Protein.calcSequenceCoverage( this.length, seqPositions )
  }

}

@JsonSnakeCase
@JsonInclude( Include.NON_NULL )
case class ProteinProperties


object ProteinMatch extends InMemoryIdGen

@JsonSnakeCase
@JsonInclude( Include.NON_NULL )
case class ProteinMatch ( 
                   // Required fields                    
                   val accession: String,
                   var description: String,
                   
                   // Immutable optional fields
                   val isDecoy: Boolean = false,
                   val isLastBioSequence: Boolean = false,
                   
                   // Mutable optional fields                     
                   var id: Long = 0,                   
                   var taxonId: Long = 0,
                   var resultSetId: Long = 0,

                   protected var proteinId: Long = 0,
                   @transient var protein: Option[Protein] = null,

                   var seqDatabaseIds: Array[Long] = null,
                   
                   var geneName: String = null,
                   var score: Float = 0,
                   var scoreType: String = null,
                   var coverage: Float = 0,
                   var peptideMatchesCount: Int = 0,
                   var sequenceMatches: Array[SequenceMatch] = null,
                   
                   var properties: Option[ProteinMatchProperties] = None
                   
                   ) {
  
  // Requirements
  require( accession != null && description != null, "accession and description must be defined" )
  
  lazy val peptidesCount: Int = {
    if( sequenceMatches == null) 0
    else sequenceMatches.map( _.getPeptideId ).distinct.length
  }

  def getProteinId : Long = { if(protein != null && protein != None) protein.get.id else proteinId }
  
}

@JsonSnakeCase
@JsonInclude( Include.NON_NULL )
case class ProteinMatchProperties

 
object ProteinSet extends InMemoryIdGen

@JsonSnakeCase
@JsonInclude( Include.NON_NULL )
case class ProteinSet ( 
                 // Required fields
                 @transient val peptideSet: PeptideSet,
                 var hasPeptideSubset: Boolean,
                 var isDecoy: Boolean, // TODO: add to MSIdb
                 
                 // Immutable optional fields
                  
                 // Mutable optional fields
                 var id: Long = 0,
                 var resultSummaryId: Long = 0,
                 
                 var proteinMatchIds: Array[Long] = null, //One of these 2 values should be specified
                 @transient var proteinMatches: Option[Array[ProteinMatch]] = null,
                 
                 protected var typicalProteinMatchId: Long = 0,
                 @transient protected var typicalProteinMatch: Option[ProteinMatch] = null,
                 
                 var isValidated: Boolean = true,
                 var selectionLevel: Int = 2,

                 var properties: Option[ProteinSetProperties] = None,
                 var proteinMatchPropertiesById: Map[Long, ProteinMatchResultSummaryProperties ] = null
                 
                 ) {

  lazy val peptideSetId = peptideSet.id
  
  // Requirements
  require( proteinMatchIds != null || proteinMatches != null )
  

    
  def setTypicalProteinMatch(newTypicalPM : ProteinMatch) :Unit = {
	  require(newTypicalPM != null ,"A typical ProteinMatch should be defined !")
	  if(proteinMatches!= null && proteinMatches.isDefined) 
		  require(proteinMatches.get.contains(newTypicalPM) ,"Typical ProteinMatch should belong to this ProteinSet !")
	  else
		  require(proteinMatchIds.contains(newTypicalPM.id) ,"Typical ProteinMatch should belong to this ProteinSet !")
		  	  
	  typicalProteinMatchId = newTypicalPM.id
	  typicalProteinMatch = Some(newTypicalPM)
  }
  
  def getTypicalProteinMatch() :Option[ProteinMatch]={typicalProteinMatch}
  
  def getProteinMatchIds : Array[Long] = { if(proteinMatches != null && proteinMatches != None) proteinMatches.get.map(_.id)  else proteinMatchIds  }

  def getTypicalProteinMatchId : Long = { if(typicalProteinMatch != null && typicalProteinMatch != None) typicalProteinMatch.get.id else typicalProteinMatchId }
   
  /**
   * Return a list of all ProteinMatch ids, identified as same set or sub set of this ProteinSet, 
   * referenced by their PeptideSet.
   * If PeptideSet are not accessible, a IllegalAccessException will be thrown. 
   *	
   */
  @throws(classOf[IllegalAccessException])
  def getAllProteinMatchesIdByPeptideSet :  Map[PeptideSet,Array[Long]]   = {
    if(peptideSet.hasStrictSubset && (peptideSet.strictSubsets == null || !peptideSet.strictSubsets.isDefined) )
      throw new IllegalAccessException("PeptideSets not accessible")
    
    val resultMapBuilder = Map.newBuilder[PeptideSet,Array[Long]]
    
    resultMapBuilder += peptideSet -> peptideSet.proteinMatchIds
    if(peptideSet.hasStrictSubset) {
       peptideSet.strictSubsets.get.foreach(pepSet => {
         resultMapBuilder += pepSet -> pepSet.proteinMatchIds
       })       
     }
    resultMapBuilder.result
  }
  
 override def hashCode = {
   id.hashCode 
 }
 
 override def toString() : String = {
   val toStrBulider= new StringBuilder(id.toString)
   if(typicalProteinMatch != null && typicalProteinMatch.isDefined)
     toStrBulider.append(typicalProteinMatch.get.accession)
   else     
     toStrBulider.append(" typicalProteinMatch ID : ").append(typicalProteinMatchId)
   toStrBulider.result
 }
 
}

@JsonSnakeCase
@JsonInclude( Include.NON_NULL )
case class ProteinSetProperties

@JsonSnakeCase
@JsonInclude( Include.NON_NULL )
case class ProteinMatchResultSummaryProperties

@JsonSnakeCase
@JsonInclude( Include.NON_NULL )
case class SequenceMatch ( // Required fields                     
                   val start: Int,
                   val end: Int,
                   val residueBefore: Char,
                   val residueAfter: Char,
                   
                   // Immutable optional fields
                   val isDecoy: Boolean = false,
                   var resultSetId : Long = 0,
                   
                   // Mutable optional fields
                   protected var peptideId: Long = 0,
                   @transient var peptide: Option[Peptide] = null,
                   
                   var bestPeptideMatchId: Long = 0,
                   @transient var bestPeptideMatch: Option[PeptideMatch] = null,
                   
                   var properties: Option[SequenceMatchProperties] = None
                   
                   ) {
  
  // Requirements
  require( start > 0 , "peptide sequence position must be striclty positive" )
  require( end > start , "peptide end position must be greater than start position" )
  
  def getPeptideId : Long = { if(peptide != null && peptide != None) peptide.get.id else peptideId }

  def getBestPeptideMatchId : Long = { if(bestPeptideMatch != null && bestPeptideMatch != None) bestPeptideMatch.get.id else bestPeptideMatchId }
  
}

@JsonSnakeCase
@JsonInclude( Include.NON_NULL )
case class SequenceMatchProperties


