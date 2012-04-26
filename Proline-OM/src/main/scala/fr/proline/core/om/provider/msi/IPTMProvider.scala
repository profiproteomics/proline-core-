package fr.proline.core.om.provider.msi
import fr.proline.core.om.model.msi.PtmDefinition



trait IPTMProvider {

  def getPtmDefinitions(ptmDefIds: Seq[Int]) : Array[Option[PtmDefinition]]
  
  def getPtmDefinition(ptmDefID: Int) : Option[PtmDefinition] = { getPtmDefinitions( Array(ptmDefID) )(0) }
    
  def getPtmDefinition(ptmName : String, ptmResidu : Char, ptmLocation : String) : Option[PtmDefinition] 
  
  def getPtmId(fullName : String, shortName : String) : Option[Int]
  
}