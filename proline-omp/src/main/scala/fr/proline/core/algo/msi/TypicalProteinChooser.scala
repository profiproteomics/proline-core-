package fr.proline.core.algo.msi

import scala.collection.JavaConverters._
import fr.profi.util.regex.RegexUtils._
import fr.proline.core.orm.msi.{ProteinMatch, ProteinSet}
import com.typesafe.scalalogging.LazyLogging
import javax.persistence.EntityManager

/**
 * Algo to change the typical protein of all Protein Set for a given ResultSummary.
 * To new selected typical should satisfy the specified TypicalProteinChooserRule :
 * - Pattern matching accession or description
 * 
 * If multiple TypicalProteinChooserRules are specified 
 * 
 */
class TypicalProteinChooser () extends LazyLogging {

  private var modifiedProteinSets : Seq[fr.proline.core.orm.msi.ProteinSet] = null
  
  
  /**
   * Search for a protein in each protein set which response to one of the specified TypicalProteinChooserRule.
   * The rules are ordered by preference this means that a protein which response to rule 1 will be prefered
   * than protein that response to second rule in specified list.
   *   
   */
	def changeTypical(rsmId: Long, priorityRulesToApply : Seq[TypicalProteinChooserRule], msiEM : EntityManager){
   
	logger.info(" Load data for Typical Protein Chooser multi rules")
    val ormProtSetRSM = msiEM.createQuery("FROM fr.proline.core.orm.msi.ProteinSet protSet WHERE resultSummary.id = :rsmId",
			classOf[fr.proline.core.orm.msi.ProteinSet]).setParameter("rsmId", rsmId).getResultList.asScala.toList
    
    		  	
    var modifiedProtSet = Seq.newBuilder[fr.proline.core.orm.msi.ProteinSet]
    ormProtSetRSM.foreach(protSet => {
      
			val sameSetProts =  protSet.getProteinSetProteinMatchItems.asScala.filter(!_.getIsInSubset).map(pspmi => { pspmi.getProteinMatch}).toSeq.sortBy(_.getAccession())
			val currentTypical = sameSetProts.filter(_.getId == protSet.getProteinMatchId)(0)
    	    	
    	var foundNewTypical = false
    	val potentialTypicals = Seq.newBuilder[ProteinMatch]
    	
    	var ruleIndex =0
    	while (ruleIndex < priorityRulesToApply.length && !foundNewTypical) {
    	  val ruleToApply = priorityRulesToApply(ruleIndex)
    	  ruleIndex +=1
 		
    		//Search in in samesets    
          	sameSetProts.foreach(sameset => {
            val valueToTest: String = if (ruleToApply.applyToAcc) sameset.getAccession else sameset.getDescription
            if (valueToTest =~ ruleToApply.rulePattern) {
              potentialTypicals += sameset
              foundNewTypical = true // sameset respect current constraint
            }
          }) //End go through sameset         
      } // end go through rules
    	
	  //New typical to save !     
	  if(foundNewTypical){
	     val newTypical = potentialTypicals.result.sortBy(_.getAccession).head
	     if(!newTypical.equals(currentTypical)){
//	       logger.trace("NEW Typical, was "+currentTypical.getAccession()+" now => "+newTypical.getAccession())
	    	 protSet.setProteinMatchId(newTypical.getId)
	    	 modifiedProtSet += protSet
	     }
	  }
    })
    
    modifiedProteinSets = modifiedProtSet.result
    logger.info("Changed "+modifiedProteinSets.size+" typical proteins ")

  }
    
  def getChangedProteinSets: Seq[ProteinSet] = {modifiedProteinSets}
  
}

case class  TypicalProteinChooserRule(ruleName : String, applyToAcc : Boolean, rulePattern : String){  
  
}

