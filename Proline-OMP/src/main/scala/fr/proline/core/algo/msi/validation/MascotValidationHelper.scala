package fr.proline.core.algo.msi.validation

import math.{pow,log10}
import collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

import fr.proline.core.om.model.msi.{MsQuery,PeptideMatch}

case class MascotIonScoreThresholds( identityThreshold: Float, homologyThreshold: Float )

object MascotValidationHelper {
  
  implicit def doubleToFloat(d: Double): Float = d.toFloat
  
  def inferEvalue( ionsScore: Float, identityThreshold: Float, probability: Double = 0.05 ): Double = {
    probability * pow(10, (identityThreshold-ionsScore)/10 )
  }
  
  def calcNbCandidatePeptides( identityThreshold: Float, probability: Double = 0.05 ): Float = {
    (probability/0.05) * pow(10, identityThreshold/10 )
  }
  
  def inferNbPeptideCandidates( ionsScore: Float, eValue: Double ): Unit = {
    this.calcNbCandidatePeptides( ionsScore, eValue )
  }
  
  def calcIdentityThreshold( nbPeptideCandidates: Double, probability: Double ): Float = {
    - 10 * log10( (probability/0.05) / nbPeptideCandidates )
  }
  
  def inferIdentityThreshold( ionsScore: Float, eValue: Double, probability: Double = 0.05 ): Float = {
    ionsScore -10 * log10(probability/eValue)
  }
  
  def calcScoreThresholdOffset( newProb: Double, oldProb: Double ): Float = {
    - 10 * log10(newProb/oldProb)
  }
  
  def sumPeptideMatchesScoreOffsets( peptideMatches: Seq[PeptideMatch], mascotThresholdsByPepMatchId: Map[Int,MascotIonScoreThresholds] ): Float = {
  
    var( sumOfScoreOffsets, substractedThresholds ) = (0.0f,0.0f)
    for( peptideMatch <- peptideMatches ) {
      
      val peptideScore = peptideMatch.score
      val pmThresholds = mascotThresholdsByPepMatchId( peptideMatch.id )
      val( identityThreshold, homologyThreshold ) = ( pmThresholds.identityThreshold, pmThresholds.homologyThreshold )
      
      // If there is a homology threshold and ions score > homology threshold
      if( ! homologyThreshold.isNaN && homologyThreshold > 0 && peptideScore > homologyThreshold ) {
        sumOfScoreOffsets += peptideScore - homologyThreshold
        substractedThresholds += homologyThreshold
      }
      else if( peptideScore > identityThreshold ) {
        sumOfScoreOffsets += peptideScore - identityThreshold
        substractedThresholds += identityThreshold
      }
    }
    
    sumOfScoreOffsets
  }
  
  def sumPeptideMatchesScoreOffsets( peptideMatches: Seq[PeptideMatch] ): Float = {
    this.sumPeptideMatchesScoreOffsets( peptideMatches, this.getMascotThresholdsByPepMatchId(peptideMatches) )
  }
  
  def sumPeptideMatchesScoreOffsets( peptideMatches: Seq[PeptideMatch], scoreThresholdOffset: Float ): Float = {
  
    val pepMatchThresholdsMap = this.getMascotThresholdsByPepMatchId(peptideMatches)
    val pmThresholdsMapBuilder = collection.immutable.Map.newBuilder[Int,MascotIonScoreThresholds]
    
    // Add the score threshold offset to the peptide matches thresholds
    for( (pepMatchId, pmThresholds) <- pepMatchThresholdsMap ) {
      var( identityThreshold, homologyThreshold ) = ( pmThresholds.identityThreshold, pmThresholds.homologyThreshold )
      
      identityThreshold += scoreThresholdOffset
      if( ! homologyThreshold.isNaN && homologyThreshold > 0 ) homologyThreshold += scoreThresholdOffset 
      
      pmThresholdsMapBuilder += pepMatchId -> MascotIonScoreThresholds( identityThreshold, homologyThreshold )
    }
    
    this.sumPeptideMatchesScoreOffsets( peptideMatches, pmThresholdsMapBuilder.result() )
  }
  
  def calcMascotMudpitScore( peptideMatches: Seq[PeptideMatch], pepMatchThresholdsMap: Map[Int,MascotIonScoreThresholds] ): Float = {
  
    var( mudpitScore, substractedThresholds, nbValidPepMatches ) = (0.0f,0.0f,0)
    
    for( peptideMatch <- peptideMatches) {
      
      val peptideScore = peptideMatch.score
      val pmThresholds = pepMatchThresholdsMap( peptideMatch.id )  
      val( identityThreshold, homologyThreshold ) = ( pmThresholds.identityThreshold, pmThresholds.homologyThreshold )
      
      // If there is a homology threshold and ions score > homology threshold
      if( ! homologyThreshold.isNaN && homologyThreshold > 0 && peptideScore > homologyThreshold ) {
        mudpitScore += peptideScore - homologyThreshold
        substractedThresholds += homologyThreshold
        nbValidPepMatches += 1
      }
      else if( peptideScore > identityThreshold ) {
        mudpitScore += peptideScore - identityThreshold
        substractedThresholds += identityThreshold
        nbValidPepMatches += 1
      }                                      
    }
    
    if( mudpitScore > 0 ) {
       val averageSubstractedThreshold = substractedThresholds / nbValidPepMatches
       mudpitScore += averageSubstractedThreshold
    }
    
    mudpitScore
  }
  
  def calcMascotMudpitScore( peptideMatches: Seq[PeptideMatch] ): Float = {
    this.calcMascotMudpitScore( peptideMatches, this.getMascotThresholdsByPepMatchId(peptideMatches) )
  }
  
  private def getMascotThresholdsByPepMatchId( peptideMatches: Seq[PeptideMatch] ): Map[Int,MascotIonScoreThresholds] = {
  
    val pmThresholdsMapBuilder = collection.immutable.Map.newBuilder[Int,MascotIonScoreThresholds]
    for( peptideMatch <- peptideMatches ) {
      
      val pmThresholds = this.getPeptideMatchThresholds( peptideMatch )
      var( identityThreshold, homologyThreshold ) = ( pmThresholds.identityThreshold, pmThresholds.homologyThreshold )
      
      if( identityThreshold.isNaN || identityThreshold < 13 ) identityThreshold = 13
      if( homologyThreshold.isNaN || homologyThreshold < 13 ) homologyThreshold = 13
      
      pmThresholdsMapBuilder += peptideMatch.id -> MascotIonScoreThresholds( identityThreshold, homologyThreshold )
    }
    
    pmThresholdsMapBuilder.result()
  }
  
  def getPeptideMatchThresholds( peptideMatch: PeptideMatch ): MascotIonScoreThresholds = {
    
    val targetMsqProps = peptideMatch.msQuery.properties.get.getTargetDbSearch.get
    val decoyMsqProps = peptideMatch.msQuery.properties.get.getDecoyDbSearch.get
    
    // Determine homology threshold
    val targetHt = targetMsqProps.getMascotHomologyThreshold.getOrElse(0f)
    val decoyHt = decoyMsqProps.getMascotHomologyThreshold.getOrElse(0f)
    val homologyThreshold = if( decoyHt > 0 ) decoyHt else targetHt
    
    // Determine identity threshold
    val targetIt = targetMsqProps.getMascotHomologyThreshold.getOrElse(0f)
    val decoyIt = decoyMsqProps.getMascotIdentityThreshold.getOrElse(0f)
    
    var identityThreshold = 0.0f
    if( decoyIt > 0 && targetIt > 0) { identityThreshold = (decoyIt + targetIt)/2 }
    else if( decoyIt > 0 ) { identityThreshold = decoyIt }
    else if( targetIt > 0 ) { identityThreshold = targetIt }
    
    MascotIonScoreThresholds( identityThreshold, homologyThreshold )
  }
  
  def getLowestPeptideMatchThreshold( peptideMatch: PeptideMatch ): Float = {
    this.getLowestPeptideMatchThreshold( this.getPeptideMatchThresholds( peptideMatch ) )
  }
  
  def getLowestPeptideMatchThreshold( pmThresholds: MascotIonScoreThresholds ): Float = {
    
    var( identityThreshold, homologyThreshold ) = ( pmThresholds.identityThreshold, pmThresholds.homologyThreshold )
    
    var lowestThreshold = 0.0f
    if( homologyThreshold > 0 && homologyThreshold < identityThreshold ) { lowestThreshold = homologyThreshold }
    else { lowestThreshold = identityThreshold }
    
    // Apply the cutoff derived from mascot minimal value (nb candidates = 20)
    if( lowestThreshold < 13 ) lowestThreshold = 13
    
    lowestThreshold
  }
  
  def getTargetMsQueryProperties( msQuery: MsQuery ): Map[String,Any] = {
    val msQueryProperties = msQuery.properties
    //msQueryProperties("target_db_search").asInstanceOf[Map[String,Any]]    
    null
  }
  
  def getDecoyMsQueryProperties( msQuery: MsQuery ): Map[String,Any] = {
    val msQueryProperties = msQuery.properties
    //msQueryProperties("decoy_db_search").asInstanceOf[Map[String,Any]]    
    null
  }
    
  def buildJointTable( pepMatchJointTable: Array[Pair[PeptideMatch,PeptideMatch]],
                       valuePicker: (PeptideMatch) => Double ): Array[Pair[Double,Double]] = {
    
    val jointTable = pepMatchJointTable.map { pmPair => Pair( valuePicker( pmPair._1 ),
                                                              valuePicker( pmPair._2 ) ) } 
    
    jointTable
  }
  
  def rocAnalysis( pmJointTable: Array[Pair[PeptideMatch,PeptideMatch]], 
                   pmScoringParam: String = "log_evalue" ): Array[ValidationResult] = { // pmScoringParam = log_evalue || score_offset
    
    // Create anonymous functions to extract the right peptide match values
    val logEvaluePicker = { pm: PeptideMatch => if( pm != null) - log10( calcPepMatchEvalue(pm) ) else 1 } // 1 is for S=0 and IT=13
    val scoreOffsetPicker = { pm: PeptideMatch => if( pm != null) calcPepMatchScoreOffset(pm).toDouble else -13 } // -13 is for S=0 and IT=13
    val valuePickerMap = Map( "log_evalue" -> logEvaluePicker, "score_offset" -> scoreOffsetPicker )
    
    // Create anonymous functions to compute the right threshold value
    val thresholdComputerMap = Map( "log_evalue" -> { prob:Double => - log10( prob ) },
                                    "score_offset" -> { prob:Double =>  - 10 * log10(prob/0.05) }
                                  )
    
    // Build log evalue joint table
    val jointTable = this.buildJointTable( pmJointTable, valuePickerMap(pmScoringParam) )    

    // Instantiate a target decoy computer
    val tdComputer = fr.proline.core.algo.msi.TargetDecoyComputer
    
    // Define some vars
    var( probThreshold, fdr ) = ( 1.0, 100.0f )
    val rocPoints = new ArrayBuffer[ValidationResult]
    
    while( fdr > 0 ) {
      // Run target/decoy competition
      val competitionThreshold = thresholdComputerMap(pmScoringParam)(probThreshold)
      val competitionCount = tdComputer.computeTdCompetition( jointTable, competitionThreshold )
      //print Dumper competitionCount
      
      val( targetCount, decoyCount ) = ( competitionCount._1, competitionCount._2 )
      val( tB, tO, dB, dO ) = ( targetCount.better, targetCount.only , decoyCount.better, decoyCount.only )
      
      // Compute FDR (note that FDR may be greater than 100%)
      fdr = tdComputer.computeTdFdr( tB, tO, dB, dO )
      
      // Add ROC point to the list
      val rocPoint = ValidationResult( nbTargetMatches = tB + tO + dB,
                                       nbDecoyMatches = Some(dB + dO + tB),
                                       fdr = Some(fdr),
                                       properties = Some( HashMap( "p_value" -> probThreshold ) )
                                     )
                                     
      rocPoints += rocPoint
      
      //print 'fdr:'.fdr."\n"
  
      // Update probablity threshold
      probThreshold *= 0.95 // has been arbitrary chosen
    }
    
    rocPoints.toArray
  }
  
  // TODO: memoize
  def calcPepMatchEvalue( peptideMatch: PeptideMatch ): Double = {
    
    val lowestPepMatchThreshold = this.getLowestPeptideMatchThreshold( peptideMatch )
    this.inferEvalue( peptideMatch.score, lowestPepMatchThreshold )

  }
  
  def calcPepMatchScoreOffset( peptideMatch: PeptideMatch ): Float = {
    
    val lowestScoreThreshold = this.getLowestPeptideMatchThreshold( peptideMatch )
    peptideMatch.score - lowestScoreThreshold
    
  }

  // TODO: memoize
  def isPepMatchValid( peptideMatch: PeptideMatch,
                       probabilityThreshold: Double,
                       minSeqLength: Int = 0 ): Boolean = {
    
    if( peptideMatch.peptide.sequence.length < minSeqLength ) { return false }
    
    val pmEvalue = this.calcPepMatchEvalue( peptideMatch )
    
    if( pmEvalue <= probabilityThreshold ) true else false
  }

}