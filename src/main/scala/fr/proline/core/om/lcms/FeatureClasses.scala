package fr.proline.core.om.lcms

package FeatureClasses {
  
  import scala.collection.mutable.HashMap
  import fr.proline.core.om.helper.MiscUtils.InMemoryIdGen
  
  class Peak (
      
          // Required fields
          val moz: Double,
          val intensity: Double,
          val leftHwhm: Double,
          val rightHwhm: Double
          
          ) {
    
  }
  
  class IsotopicPattern (
      
          // Required fields
          var id: Int,
          val moz: Double,
          val intensity: Double,
          val charge: Byte,
          val fitScore: Double,
          val peaks: Array[Peak],
          val scanInitialId: Int,
          val overlappingIPs: Array[IsotopicPattern],
          
          // Mutable optional fields
          var properties: HashMap[String, Any] = new collection.mutable.HashMap[String, Any]
          
          ) {
    
    
  }
  
  object Feature extends InMemoryIdGen
  
  class Feature (
          
          // Required fields
          var id: Int,
          val moz: Double,
          var intensity: Double,
          val charge: Byte,
          val elutionTime: Float,
          val qualityScore: Double,
          var ms1Count: Short,
          var ms2Count: Short,
          val isOverlapping: Boolean,
          val firstScanInitialId: Int,
          val lastScanInitialId: Int,
          val apexScanInitialId: Int,
          val ms2EventIds: Array[Int],
          val isotopicPatterns: Option[Array[IsotopicPattern]],
          val overlappingFeatures: Array[Feature],
          
          // Mutable optional fields
          var children: Array[Feature] = null,
          var subFeatures: Array[Feature] = null,
          var calibratedMoz: Double = Double.NaN,
          var normalizedIntensity: Double = Double.NaN,
          var correctedElutionTime: Float = Float.NaN,
          var isClusterized: Boolean = false,
          var selectionLevel: Byte = 2,
          
          var firstScanId: Int = 0,
          var lastScanId: Int = 0,
          var apexScanId: Int = 0,
          var bestChildId: Int = 0,
          var theoreticalFeatureId: Int = 0,
          var compoundId: Int = 0,
          var mapLayerId: Int = 0,
          var mapId: Int = 0,
          
          var properties: HashMap[String, Any] = new collection.mutable.HashMap[String, Any]
          
          ) {
    
    // Requirements

    import fr.proline.core.om.helper.MsUtils
    
    lazy val mass = MsUtils.mozToMass( moz, charge )
    lazy val isCluster = if( subFeatures == null ) false else subFeatures.length > 0
    
    def getCorrectedElutionTime = if( correctedElutionTime.isNaN ) elutionTime else correctedElutionTime
  
    def toMasterFeature(): Feature = {
      new Feature ( id = Feature.generateNewId(),
                    moz = this.moz,
                    intensity = this.intensity,
                    charge = this.charge,
                    elutionTime = this.correctedElutionTime,
                    qualityScore = this.qualityScore,
                    ms1Count = this.ms1Count,
                    ms2Count = this.ms2Count,
                    isOverlapping = false,
                    firstScanId = this.firstScanId,
                    lastScanId = this.lastScanId,
                    apexScanId = this.apexScanId,
                    firstScanInitialId = this.firstScanInitialId,
                    lastScanInitialId = this.lastScanInitialId,
                    apexScanInitialId = this.apexScanInitialId,
                    ms2EventIds = null,
                    isotopicPatterns = null,
                    overlappingFeatures = null,
                    children = Array(this)
                  )
    }
  }
  
  class TheoreticalFeature (
      
          // Required fields
          var id: Int,
          val moz: Double,
          val charge: Byte,
          val elutionTime: Float,
          val origin: String,
          
          // Mutable optional fields
          var mapLayerId: Int = 0,
          var mapId: Int = 0,
          
          var properties: HashMap[String, Any] = new collection.mutable.HashMap[String, Any]
          
          ) {


  
  }



}