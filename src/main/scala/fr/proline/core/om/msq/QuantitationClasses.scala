package fr.proline.core.om.msq

import scala.collection.mutable.ArrayBuffer
import fr.proline.core.om.helper.MiscUtils.InMemoryIdGen

trait Item {
  var selectionLevel: Int
  var properties: Map[String,Any]
}

trait QuantComponent extends Item {
  val rawAbundance: Float
  var abundance: Float
  var quantChannelId: Int
  
  def hasRawAbundance = if( rawAbundance.isNaN ) false else true
  def hasAbundance = if( abundance.isNaN ) false else true
}

trait LcmsQuantComponent extends QuantComponent {
  val elutionTime: Float
  val scanNumber: Int
}

trait MasterQuantComponent extends Item {
  var id: Int
  var quantComponentMap: Map[Int,QuantComponent] // QuantComponent mapped by quantChannelId
}

trait MasterLcmsQuantComponent extends MasterQuantComponent {
  val calculatedMoz: Double
  val charge: Int
  val elutionTime: Float
}

object QuantReporterIon extends InMemoryIdGen
case class QuantReporterIon( val moz: Double,                             
                             val rawAbundance: Float,
                             var abundance: Float,
                             var quantChannelId: Int,
                             var selectionLevel: Int,
                             var properties: Map[String,Any]
      
                            ) extends QuantComponent {
  
}

object MasterQuantReporterIon extends InMemoryIdGen

case class MasterQuantReporterIon( var id: Int,
                                   var msQueryId: Int,
                                   var spectrumId: Int,
                                   var scanNumber: Int,
                                   var quantComponentMap: Map[Int,QuantComponent],
                                   var selectionLevel: Int,
                                   var properties: Map[String,Any]

                                 ) extends MasterQuantComponent {
  
  def getQuantReporterIonMap: Map[Int,QuantReporterIon] = {
    this.quantComponentMap.map { entry => ( entry._1 -> entry._2.asInstanceOf[QuantReporterIon] ) }
  }
  
}

object QuantPeptideIon extends InMemoryIdGen

case class QuantPeptideIon(  val rawAbundance: Float,
                             var abundance: Float,
                             val elutionTime: Float,
                             val scanNumber: Int,
                             
                             val peptideMatchesCount: Int,
                             val bestScore: Float,
                             val predictedElutionTime: Float,
                             val predictedScanNumber: Int,
                             
                             var quantChannelId: Int, 
                             val peptideId: Int,
                             val peptideInstanceId: Int,
                             val msQueryIds: Array[Int],
                             val lcmsFeatureId: Int,
                             val unmodifiedPeptideIonId: Int = 0,
                             
                             var selectionLevel: Int,
                             var properties: Map[String,Any]

                           ) extends LcmsQuantComponent {
  
}

object MasterQuantPeptideIon extends InMemoryIdGen
  
case class MasterQuantPeptideIon(  var id: Int,
                                   
                                   val calculatedMoz: Double,
                                   val unlabeledMoz: Double,
                                   val charge: Int,
                                   val elutionTime: Float,
                                   val peptideMatchesCount: Int,
                                   
                                   var quantComponentMap: Map[Int,QuantComponent],
                                   var bestQuantPeptideIon: QuantPeptideIon,
                                   var masterQuantReporterIons: Array[MasterQuantReporterIon] = null,                                   
                                   
                                   val bestPeptideMatchId: Int,
                                   
                                   var selectionLevel: Int,
                                   var properties: Map[String,Any]

                                 ) extends MasterLcmsQuantComponent {
  
  def getQuantPeptideIonMap: Map[Int,QuantPeptideIon] = {
    this.quantComponentMap.map { entry => ( entry._1 -> entry._2.asInstanceOf[QuantPeptideIon] ) }
  }
  
}

object QuantPeptide extends InMemoryIdGen

case class QuantPeptide( val rawAbundance: Float,
                         var abundance: Float,
                         val elutionTime: Float,
                         
                         var quantChannelId: Int,
                         val peptideId: Int,
                         val peptideInstanceId: Int,
                         
                         var selectionLevel: Int,
                         var properties: Map[String,Any]

                       ) extends QuantComponent {
  
}

object MasterQuantPeptide extends InMemoryIdGen

case class MasterQuantPeptide( var id: Int,
    
                               val peptideMatchesCount: Float,
                               var proteinMatchesCount: Float,
                               
                               var quantPeptideMap: Map[Int,QuantPeptide], // QuantPeptide by quant channel id
                               var masterQuantPeptideIons: Array[MasterQuantPeptideIon] = null,
                               
                               val peptideId: Int, // without label in the context of isotopic labeling
                               val peptideInstanceId: Int, // without label in the context of isotopic labeling
                               var masterQuantProteinSetIds: Array[Int],
                               
                               var selectionLevel: Int,
                               var properties: Map[String,Any]
      
                             ) extends Item {
  

}

object QuantProteinSet extends InMemoryIdGen

case class QuantProteinSet( val rawAbundance: Float,
                            var abundance: Float,
                            var quantChannelId: Int,
                            var selectionLevel: Int,
                            var properties: Map[String,Any] 
                           ) extends QuantComponent {
  
}

object MasterQuantProteinSet extends InMemoryIdGen

case class MasterQuantProteinSet(  var id: Int,
    
                                   val peptideMatchesCount: Float,
                                   
                                   var quantProteinSetMap: Map[Int,QuantProteinSet], // QuantPeptide by quant channel id
                                   var masterQuantPeptides: Array[MasterQuantPeptide] = null,
                                   
                                   val selectedMasterQuantPeptideIds: Array[Int],
                                   val peptideSetId: Int,
                                   val proteinIds: Array[Int],
                                   val proteinMatchIds: Array[Int],
                                   val specificSampleId: Int = 0, // defined if the protein has been seen in a single sample
                                   
                                   var selectionLevel: Int,
                                   var properties: Map[String,Any]

                               ) extends Item {
  
}

object QuantResultSummary extends InMemoryIdGen

case class QuantResultSummary( var id: Int,
                               var description: String,
                               
                               var masterQuantProteinSets: Array[MasterQuantProteinSet],
                               var masterQuantPeptides: Array[MasterQuantProteinSet],
                               var masterQuantPeptideIons: Array[MasterQuantProteinSet],
                               
                               val quantResultSetId: Int,
                               
                               var properties: Map[String,Any]
                               )  {
  
}
