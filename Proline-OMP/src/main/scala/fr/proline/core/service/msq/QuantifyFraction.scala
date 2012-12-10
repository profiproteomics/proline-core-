package fr.proline.core.service.msq

import fr.proline.api.service.IService
import fr.proline.core.orm.uds.{ QuantitationFraction => UdsQuantFraction }
import fr.proline.core.orm.util.DatabaseManager

class QuantifyFraction( dbManager: DatabaseManager, quantFractionId: Int ) extends IService {
  
  def runService() = {
    
    // Create entity manager
    val udsEM = dbManager.getUdsDbConnector.getEntityManagerFactory.createEntityManager()
    
    // Retrieve the quantitation fraction
    val udsQuantFraction = udsEM.find(classOf[UdsQuantFraction], quantFractionId)    
    require( udsQuantFraction != null,
             "undefined quantitation fraction with id=" + udsQuantFraction )
    
    FractionQuantifier( dbManager, udsEM, udsQuantFraction ).quantify()
    
    // Close entity manager
    udsEM.close()
    
    false
  }

}

object FractionQuantifier {
  
  import javax.persistence.EntityManager
  import fr.proline.core.service.msq.impl._
  
  def apply( dbManager: DatabaseManager,
             udsEm: EntityManager,
             udsQuantFraction: UdsQuantFraction ): IQuantifier = {
    
    val udsQuantMethod = udsQuantFraction.getQuantitation.getMethod
    val quantMethodType = udsQuantMethod.getType
    val abundanceUnit = udsQuantMethod.getAbundanceUnit
    
    var fractionQuantifier: IQuantifier = null
    
    if( abundanceUnit == "reporter_ion" ) {      
    
    /*require Pairs::Msq::Module::Quantifier::ReporterIons
    fractionQuantifier = new Pairs::Msq::Module::Quantifier::ReporterIons(
                                  rdb_quantitation_fraction = rdbQuantFraction
                                  )*/
    
    } 
    else if( quantMethodType == "label_free" ) {
      if( abundanceUnit == "feature" ) {
        fractionQuantifier = new Ms1DrivenLabelFreeFeatureQuantifier(
                                   dbManager = dbManager,
                                   udsEm = udsEm,
                                   udsQuantFraction = udsQuantFraction
                                 )
      }
      else if( abundanceUnit == "spectral_count" ) {
        fractionQuantifier = new SpectralCountQuantifier(
                                   dbManager = dbManager,
                                   udsEm = udsEm,
                                   udsQuantFraction = udsQuantFraction
                                 )
      }
    }
    
    assert( fractionQuantifier != null, "The needed quantifier is not yet implemented" )
    
    fractionQuantifier

  }

}