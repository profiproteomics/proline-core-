package fr.proline.core.om.model.lcms

import scala.collection.mutable.HashMap
import scala.reflect.BeanProperty
import com.codahale.jerkson.JsonSnakeCase
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import fr.proline.util.misc.InMemoryIdGen

class Peak (
    
  // Required fields
  val moz: Double,
  val intensity: Float,
  val leftHwhm: Double,
  val rightHwhm: Double
  
)

//object IsotopicPattern extends InMemoryIdGen
class IsotopicPattern (
    
  // Required fields
  //var id: Long,
  val moz: Double,
  val intensity: Float,
  val charge: Int,
  val scanInitialId: Int,
  
  val peaks: Option[Array[Peak]] = None,
  val overlappingIPs: Option[Array[IsotopicPattern]] = None,
  
  // Mutable optional fields
  var fitScore: Option[Float] = None,
  var properties: Option[IsotopicPatternProperties] = None
  
)

@JsonSnakeCase
@JsonInclude( Include.NON_NULL )
case class IsotopicPatternProperties

object Compound extends InMemoryIdGen
case class Compound(
  var id: Long,
  var identifier: String // maybe a peptide sequence and its ptm_string or a chemical formula
)

object Feature extends InMemoryIdGen

case class FeatureRelations(
  @transient var compound: Option[Compound] = None,  
  val ms2EventIds: Array[Long],
  val firstScanInitialId: Int,
  val lastScanInitialId: Int,
  val apexScanInitialId: Int,
  var firstScanId: Long = 0L,
  var lastScanId: Long = 0L,
  var apexScanId: Long = 0L,
  var bestChildId: Long = 0L,
  var bestChildMapId: Long = 0L,
  var theoreticalFeatureId: Long = 0L,
  var compoundId: Long = 0L,
  var mapLayerId: Long = 0L,
  var mapId: Long = 0L
)

case class Feature (
  
  // Required fields
  var id: Long,
  val moz: Double,
  var intensity: Float,
  val charge: Int,
  val elutionTime: Float,
  val duration: Float,
  val qualityScore: Double,
  var ms1Count: Int,
  var ms2Count: Int,
  val isOverlapping: Boolean,
  
  val isotopicPatterns: Option[Array[IsotopicPattern]],
  val relations: FeatureRelations,
  
  // Mutable optional fields
  var children: Array[Feature] = null,
  var subFeatures: Array[Feature] = null,
  var overlappingFeatures: Array[Feature] = null,
  var calibratedMoz: Option[Double] = None,
  var normalizedIntensity: Option[Float] = None,
  var correctedElutionTime: Option[Float] = None,
  var isClusterized: Boolean = false,
  var selectionLevel: Int = 2,
  
  var properties: Option[FeatureProperties] = None
  
) {
  
  // Requirements
  require( elutionTime.isNaN == false, "elution time must be a valid float value" )

  import fr.proline.util.ms.mozToMass
  
  lazy val mass = mozToMass( moz, charge )
  def isCluster = if( subFeatures == null ) false else subFeatures.length > 0
  def isMaster = if( children == null ) false else children.length > 0
  
  def getCorrectedElutionTimeOrElutionTime = correctedElutionTime.getOrElse(elutionTime)
  def getCalibratedMozOrMoz = calibratedMoz.getOrElse(moz)
  def getNormalizedIntensityOrIntensity = normalizedIntensity.getOrElse(intensity)
  
  def getRunMapIds(): Array[Long] = {
    if( this.isMaster ) children.flatMap( _.getRunMapIds ).distinct
    else if ( this.isCluster ) Array(this.subFeatures(0).relations.mapId)
    else Array(this.relations.mapId)
  }
  
  def toRunMapFeature(): Feature = {
    require( isCluster == false, "can't convert a cluster feature into a run map feature" )
    require( isMaster == false, "can't convert a master feature into a run map feature" )
    
    this.copy(
      calibratedMoz = None,
      normalizedIntensity = None,
      correctedElutionTime = None,
      isClusterized = false
    )
  }
  
  def toMasterFeature(): Feature = {
    val ftRelations = this.relations
    
    new Feature (
      id = Feature.generateNewId(),
      moz = this.moz,
      intensity = this.intensity,
      charge = this.charge,
      elutionTime = this.getCorrectedElutionTimeOrElutionTime, // master time scale must be corrected or be the ref
      duration = this.duration,
      calibratedMoz = this.calibratedMoz,
      normalizedIntensity = this.normalizedIntensity,
      correctedElutionTime = this.correctedElutionTime,
      qualityScore = this.qualityScore,
      ms1Count = this.ms1Count,
      ms2Count = this.ms2Count,
      isOverlapping = false,
      selectionLevel = this.selectionLevel,
      relations = new FeatureRelations(
        firstScanInitialId = ftRelations.firstScanInitialId,
        lastScanInitialId = ftRelations.lastScanInitialId,
        apexScanInitialId = ftRelations.apexScanInitialId,
        firstScanId = ftRelations.firstScanId,
        lastScanId = ftRelations.lastScanId,
        apexScanId = ftRelations.apexScanId,
        bestChildId = ftRelations.bestChildId,
        bestChildMapId = ftRelations.mapId,
        ms2EventIds = null
        ),
      isotopicPatterns = null,
      overlappingFeatures = null,
      children = Array(this)
    )
  }
  
  /*
  def isOverlapping(f: Feature, ppm : Double, lcmsRun:LcmsRun): Boolean = {
    
    /**
     * function to test if one feature is overlapping
     * 
     */
    //doing nothing if matching occurs
    this match {
      case f => return false
    }
    
    val mozTolerance =  math.max(moz, f.moz) * ppm / 1e6
    
    if (math.abs(moz - f.moz) > mozTolerance) {
      return false
    }
    
    var minTime = lcmsRun.scanById(this.relations.firstScanId).time 
    var maxTime = lcmsRun.scanById(relations.lastScanId).time
    var fminTime = lcmsRun.scanById(f.relations.firstScanId).time
    var fmaxTime = lcmsRun.scanById(f.relations.lastScanId).time
    
    if (maxTime > fminTime && minTime < fmaxTime)  {
      // intersection add stuff in overlapping feature isotopicPattern or new feature ?
      if (f.moz > moz) {
    	 if (!overlappingFeatures.contains(f)) {
    	  overlappingFeatures :+ f
    	  f.isClusterized = true
    	 }
      }else  {
        if (! f.overlappingFeatures.contains(this)) {
        	f.overlappingFeatures :+ this
        	this.isClusterized = true
        }
      }
      return true
    }
    false
  }*/
  
}

@JsonSnakeCase
@JsonInclude( Include.NON_NULL )
case class FeatureProperties (
  @BeanProperty var peakelsCount: Option[Int] = None,
  @BeanProperty var peakelsRatios: Option[Array[Float]] = None,
  @BeanProperty var overlapCorrelation: Option[Float] = None,
  @BeanProperty var overlapFactor: Option[Float] = None
)


case class TheoreticalFeature (
    
  // Required fields
  var id: Long,
  val moz: Double,
  val charge: Int,
  val elutionTime: Float,
  val origin: String,
  
  // Mutable optional fields
  var mapLayerId: Long = 0L,
  var mapId: Long = 0L,
  
  var properties: Option[TheoreticalFeatureProperties] = None
  
)

@JsonSnakeCase
@JsonInclude( Include.NON_NULL )
case class TheoreticalFeatureProperties
