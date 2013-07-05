package fr.proline.core.algo.msi.validation.proteinset

import com.weiglewilczek.slf4s.Logging
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import fr.proline.core.algo.msi.validation._
import fr.proline.core.om.model.msi.ResultSummary
import fr.proline.core.om.model.msi.ProteinSet
import fr.proline.core.om.model.msi.PeptideMatch
import fr.proline.core.algo.msi.filtering._

class PepMatchRulesValidator(
  val pepMatchFilterRule1: IOptimizablePeptideMatchFilter,
  val pepMatchFilterRule2: IOptimizablePeptideMatchFilter
) extends AbstractPepMatchRulesValidator with Logging {
  require( pepMatchFilterRule1.filterParameter == pepMatchFilterRule2.filterParameter )
  
  def filterParameter = pepMatchFilterRule1.filterParameter
  def filterDescription = pepMatchFilterRule1.filterDescription
  def getFilterProperties = pepMatchFilterRule1.getFilterProperties ++ pepMatchFilterRule2.getFilterProperties
  
  val expectedFdr = Option.empty[Float]
  var targetDecoyMode = Option.empty[TargetDecoyModes.Value]
  
  def validateProteinSets( targetRsm: ResultSummary, decoyRsm: Option[ResultSummary] ): ValidationResults = {
    
    // Retrieve some vars
    val targetProtSets = targetRsm.proteinSets
    val decoyProtSets = decoyRsm.map(_.proteinSets)
    val allProtSets = targetProtSets ++ decoyProtSets.getOrElse(Array())
    var allBestValidPepMatchesByPepSetId = targetRsm.getBestValidatedPepMatchesByPepSetId()
    if( decoyRsm.isDefined ) {
      allBestValidPepMatchesByPepSetId ++= decoyRsm.get.getBestValidatedPepMatchesByPepSetId()
    }
    
    // Validate results with the thresholds that provide the best results
    this._validateProteinSets( allProtSets, allBestValidPepMatchesByPepSetId, validationRules )
    
    // Compute validation result
    val valResult = this.computeValidationResult(targetRsm, decoyRsm)
    
    // Update validation result properties
    valResult.addProperties(
      Map(
        ValidationPropertyKeys.RULE_1_THRESHOLD_VALUE -> pepMatchFilterRule1.getThresholdValue,
        ValidationPropertyKeys.RULE_2_THRESHOLD_VALUE -> pepMatchFilterRule2.getThresholdValue
      )
    )
    
    // Return validation results
    ValidationResults( valResult )
  }
  
}