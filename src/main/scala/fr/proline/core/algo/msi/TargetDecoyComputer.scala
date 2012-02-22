package fr.proline.core.algo.lcms

object TargetDecoyComputer {
  
  import scala.collection.mutable.ArrayBuffer
  import fr.proline.core.om.msi.PeptideClasses._
  import fr.proline.core.om.msi.ResultSetClasses._
  
  def buildPeptideMatchJointTable( peptideMatches: Seq[PeptideMatch],
                                   targetResultSetId: Int,
                                   decoyResultSetId: Int ): Array[Pair[PeptideMatch,PeptideMatch]] = {
    
    // Filter peptide matches to have only first rank ones
    val firstRankPSMs = peptideMatches.filter { _.rank == 1 }
    
    // Group PSMs by MS query initial id
    val pepMatchesByMsQueryInitialId = firstRankPSMs.groupBy( _.msQuery.initialId )
    
    // Build peptide match joint table     
    val jointTable = new ArrayBuffer[Pair[PeptideMatch,PeptideMatch]]    
    for( (msQueryInitialId, pepMatches) <- pepMatchesByMsQueryInitialId ) {
      
      // Group peptide matches by result set id
      val pepMatchesByRsId = pepMatches.groupBy(_.resultSetId)
      val targetPepMatches = pepMatchesByRsId.get(targetResultSetId)
      val decoyPepMatches = pepMatchesByRsId.get(decoyResultSetId)
      
      // Remove peptide match duplicates (same score and same rank but different peptides = see Mascot pretty rank )
      var targetPepMatch: PeptideMatch = null
      if( targetPepMatches != None ) {
        targetPepMatch = targetPepMatches.get.toList.sort( (a,b) => a.score > b.score ).head
      }
      
      // Remove peptide match duplicates (same score and same rank but different peptides = see Mascot pretty rank )
      var decoyPepMatch: PeptideMatch = null
      if( decoyPepMatches != None ) {
        targetPepMatch = targetPepMatches.get.toList.sort( (a,b) => a.score > b.score ).head
      }
      
      jointTable += Pair(targetPepMatch,decoyPepMatch)
      
    }
  
    jointTable.toArray
  }
    
  case class TDCompetitionCounts( var better: Int = 0, var only: Int = 0, var under: Int = 0 )
  
  def computeTdCompetition( scoreJointTable: Seq[Pair[Double,Double]], scoreThreshold: Double 
                          ): Pair[TDCompetitionCounts,TDCompetitionCounts] = {
  
    val competitionCounts = Map( "target" -> TDCompetitionCounts(),
                                 "decoy" -> TDCompetitionCounts() )                                
    case class TDCompetitionResult( winner: String, winnerScore: Double, looserScore: Double )
    
    for( val scores <- scoreJointTable ) {
      
      val( targetScore, decoyScore ) = ( scores._1, scores._2 )
      var compet: TDCompetitionResult = null
      
      // If decoy value equals target value we consider that decoy wins
      if( targetScore > decoyScore ) { compet = TDCompetitionResult("target", targetScore, decoyScore ) }
      else { compet = TDCompetitionResult("decoy", decoyScore, targetScore ) }
      
      // Assign competition winner to a given class = better, only or under
      if( compet.winnerScore >= scoreThreshold ) {
        if( compet.looserScore >= scoreThreshold ) { competitionCounts(compet.winner).better += 1 }
        else { competitionCounts(compet.winner).only += 1 }
      }
      else { competitionCounts(compet.winner).under += 1 }
    }
    
    Pair(competitionCounts("target"), competitionCounts("decoy"))
  }
  

  /** Classic method for fdr computation. */
  def computeFdr ( tp: Int, dp: Int ): Double = { 
    require( tp > 0 && dp >= 0 )
    
    100 * dp  / (tp + dp )
  }  

  /** Computes FDR for separate target/decoy databases (Matrix Science).
  * tp = target positive  dp = decoy positive
  */
  def computeSdFdr( tp: Int, dp: Int ): Unit = {
    require( tp > 0 && dp >= 0 )
    
    100 * dp / tp    
  }
  
  /** Computes FDR for concatenated target/decoy databases (Elias and Gygi, Nature Methods, 2007)
  * tp = target positive  dp = decoy positive
  */
  def computeCdFdr( tp: Int, dp: Int ): Unit = {
    require( tp > 0 && dp >= 0 )
    
    100 * 2 * dp  / (tp + dp )
  }

  /** Computes FDR using the refined method described by Navarro et al. (JPR, 2009)
  * tB = target better ; tO = target only ; dB = decoy better ; dO = decoy only
  */
  def computeTdFdr( tB: Int, tO: Int, dB: Int, dO: Int ): Unit = { 
    require( tB + tO + dB > 0 )
    
    100 * (2 * dB + dO) / (tB + tO + dB)
  }

}