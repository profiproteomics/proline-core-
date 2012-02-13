package fr.proline.core.algo.lcms

case class ClusteringParams( mozTol: Double, mozTolUnit: String, timeTol: Float,
                             intensityComputation: String, timeComputation: String
                            )
                              
class FeatureClusterer {

  import scala.collection.mutable.ArrayBuffer
  import fr.proline.core.om.lcms.FeatureClasses._
  import fr.proline.core.om.lcms.MapClasses._
  import fr.proline.core.om.lcms.RunClasses._
  import fr.proline.core.om.helper.MsUtils
  import fr.proline.core.om.helper.MiscUtils.getMedianObject
  
  private val ftMozSortingFunc = new Function2[Feature, Feature, Boolean] {
    def apply(a: Feature, b: Feature): Boolean = if (a.moz < b.moz) true else false
  }
  
  private val ftTimeSortingFunc = new Function2[Feature, Feature, Boolean] {
    def apply(a: Feature, b: Feature): Boolean = if (a.elutionTime < b.elutionTime) true else false
  }
  
  def clusterizeFeatures( lcmsMap: LcmsMap, scans: Seq[LcmsScan], params: ClusteringParams ): ProcessedMap = {
    
    val lcmsMapId = if( lcmsMap.isProcessed ) lcmsMap.asInstanceOf[ProcessedMap].id
                    else lcmsMap.asInstanceOf[RunMap].id
                    
    // Retrieve parameters
    val mozTol = params.mozTol
    val mozTolUnit = params.mozTolUnit
    val timeTol = params.timeTol
    val intensityComputationMethod = params.intensityComputation
    val timeComputationMethod = params.timeComputation
    
    // Retrieve some vars
    val scanById = scans map { scan => (scan.id -> scan) } toMap
    val features = lcmsMap.features
    
    val featuresByCharge = features.groupBy( _.charge )
    val chargeStates = featuresByCharge.keys.toList.sort { (a,b) => a < b }
    
    // Iterate over charge states
    val singleFeatures = new ArrayBuffer[Feature]
    val totalFtGroups = new ArrayBuffer[ArrayBuffer[Feature]]
    // ArrayBuffer.fill[Feature](1,2)(null)
    //Array.ofDim[Feature]()
    
    for( charge <- chargeStates ) {
      println( "charge = " + charge )
      
      val sameChargeFeatures = featuresByCharge.get(charge).get
      
      // Sort features by m/z
      var ftsSortedByMoz = sameChargeFeatures.toList.sort { (a,b) => a.moz < b.moz }
      
      // Set some vars
      var prevFt = ftsSortedByMoz(0)      
      val ftsGroupedByMoz = new ArrayBuffer[ArrayBuffer[Feature]](1)
      ftsGroupedByMoz += ArrayBuffer(prevFt)
      var ftGroupIdx = 0
      
      // Cluster feature by moz
      for( ftIdx <- 1 until ftsSortedByMoz.length ) {
        
        val ft = ftsSortedByMoz(ftIdx)
        val ftMoz = ft.moz
        
        // Compute m/z tolerance in Dalton
        val mozTolInDalton = MsUtils.calcMozTolInDalton( ftMoz, mozTol, mozTolUnit )
        if ( math.abs(ftMoz - prevFt.moz) > mozTolInDalton) {
          // Append new group of features because current m/z is to far from the previous one
          ftGroupIdx += 1
          ftsGroupedByMoz += new ArrayBuffer[Feature](1)
        }
        
        // Append feature to its corresponding group
        ftsGroupedByMoz(ftGroupIdx) += ft
        
        // Replace previous feature by the current one
        prevFt = ft
      }
      
      // Clusterize features by time
      val sameChargeFtGroups = new ArrayBuffer[ArrayBuffer[Feature]]
      for( ftGroup <- ftsGroupedByMoz ) {
        if( ftGroup.length == 1 ) {
          singleFeatures += ftGroup(0)
        } else {
          
          val tmpFtsGroupedByTime = this.splitFtGroupByTime( ftGroup, timeTol, scanById )
          
          // Iterate over features grouped by time dimension
          for( tmpFtGroup <- tmpFtsGroupedByTime ) {
            if( tmpFtGroup.length == 1) {              
              // Put aside unclusterized features
              singleFeatures ++= tmpFtGroup              
            } else { sameChargeFtGroups += tmpFtGroup }
          }
        }
      }

      // Filtering same charge ft groups to remove m/z conflicts
      for( ftGroup <- sameChargeFtGroups ) {
        
        val mozTolInDalton = MsUtils.calcMozTolInDalton( ftGroup(0).moz, mozTol, mozTolUnit )
        val mozSplitFtGroups = this.splitFtGroupByMoz( ftGroup, mozTolInDalton )
        
        for( mozSplitFtGroup <- mozSplitFtGroups ) {
          
          // If we retrieve more than one feature in the group
          if( mozSplitFtGroup.length >= 2 ) {
            
            // Split again by time
            val timeSplitFtGroups = this.splitFtGroupByTime( mozSplitFtGroup, timeTol, scanById )
            
            for( tmpFtGroup <- timeSplitFtGroups ) {
              // If we retrieve only one feature in the group
              if( tmpFtGroup.length == 1 ) { singleFeatures += tmpFtGroup(0) }
              // Else => more than one feature in the group
              else { totalFtGroups += tmpFtGroup }
            }
            
          // Else put aside alone feature
          } else { singleFeatures += mozSplitFtGroup(0) }
        }
      }
      
      println( "number of detected feature clusters: " + totalFtGroups.length )
    }
    
    // Check if single features don't contain duplicated objects
    val tmpIdHash = new java.util.HashMap[Int,Int]
    for( ft <- singleFeatures ) {
      if( tmpIdHash containsKey ft.id ) throw new Exception("duplicated feature detected")
      tmpIdHash.put(ft.id,1)
    }
    
    // Convert total ft groups into feature clusters
    val ftClusters = new ArrayBuffer[Feature]
    for( totalFtGroup <- totalFtGroups ) {
      
      val totalFtGroupAsList = totalFtGroup.toList
      val medianFt = getMedianObject( totalFtGroupAsList, this.ftMozSortingFunc )
      val moz = medianFt.moz
      
      val intensity = this.computeClusterIntensity(totalFtGroupAsList, intensityComputationMethod);
      val elutionTime = this.computeClusterTime(totalFtGroupAsList, timeComputationMethod);
      
      // Set some vars
      val ms2EventIdSetBuilder = scala.collection.immutable.Set.newBuilder[Int] 
      for( ft <- totalFtGroup ) {
        if( ft.ms2EventIds != null ) {
          for( ms2EventId <- ft.ms2EventIds) ms2EventIdSetBuilder += ms2EventId
        }
      }
      val ms2EventIds = ms2EventIdSetBuilder.result().toArray[Int]
      val ms2Count = ms2EventIds.length
      val mostIntenseFt = this.findMostIntenseFeature(totalFtGroup.toList)
      val charge = mostIntenseFt.charge
      val qualityScore = mostIntenseFt.qualityScore
      //val apexIp = mostIntenseFt.apex
      val apexScanId = mostIntenseFt.apexScanId
      val apexScanInitialId = mostIntenseFt.apexScanInitialId
      val nbSubFts = totalFtGroup.length
      
      // Determine first ft scan and last ft scan
      val subftsSortedByAscTime = totalFtGroupAsList.sort { (a,b) => a.elutionTime < b.elutionTime }
      val( firstSubft, lastSubft ) = ( subftsSortedByAscTime(0), subftsSortedByAscTime(nbSubFts-1) )
      val( firstScanId, firstScanInitialId ) = ( firstSubft.firstScanId, firstSubft.firstScanInitialId )
      val( lastScanId, lastScanInitialId ) = ( lastSubft.lastScanId, lastSubft.lastScanInitialId )
      val ms1Count = 1 + scanById(lastScanId).cycle - scanById(firstScanId).cycle
      
      val ftCluster = new Feature(  id = Feature.generateNewId,
                                    moz = moz,
                                    charge = charge,
                                    intensity = intensity,
                                    elutionTime = elutionTime,
                                    ms1Count = ms1Count.toByte,
                                    ms2Count = ms2Count.toByte,
                                    firstScanId = firstScanId,
                                    lastScanId = lastScanId,
                                    apexScanId = apexScanId,
                                    firstScanInitialId = firstScanInitialId,
                                    lastScanInitialId = lastScanInitialId,
                                    apexScanInitialId = apexScanInitialId,
                                    ms2EventIds = ms2EventIds,
                                    subFeatures = subftsSortedByAscTime.toArray,
                                    isotopicPatterns = None,
                                    isOverlapping = false,
                                    overlappingFeatures = null,                                  
                                    qualityScore = qualityScore,
                                    mapId = lcmsMapId
                                  )
      //ftCluster.apex(apexIp) if defined apexIp
      
      ftClusters += ftCluster
      
    }
    
    // Add feature clusters to clusterized_features
    val clusterizedFeatures = (singleFeatures ++ ftClusters).toArray
    
   // lcmsMap.c
    
    var newLcmsMap: ProcessedMap = null
    if( lcmsMap.isProcessed) {
      
      // Create new processed map from existing processed map
      newLcmsMap = lcmsMap.asInstanceOf[ProcessedMap].copy( 
                     id = ProcessedMap.generateNewId(),
                     creationTimestamp = new java.util.Date(),
                     modificationTimestamp = new java.util.Date(),
                     features = clusterizedFeatures,
                     isLocked = false
                   )
      
    } else {
      // Create new processed map from existing run map
      newLcmsMap = lcmsMap.asInstanceOf[RunMap].toProcessedMap( 
                     id = ProcessedMap.generateNewId(),
                     number = 0,
                     mapSetId = 0,
                     features = clusterizedFeatures
                   )
    }
    
    newLcmsMap
    
  }
  
  private def splitFtGroupByTime( ftGroup: Seq[Feature],
                                  timeTol: Float,
                                  scanById: Map[Int,LcmsScan] ): ArrayBuffer[ArrayBuffer[Feature]] = {
    
    // Sort features by first scan time
    val nbFts = ftGroup.length
    val ftsSortedByTime = ftGroup.toList.sort { (a,b) => 
                                                scanById(a.firstScanId).time <
                                                scanById(b.firstScanId).time }
    // Set some vars
    var curFtIdx = 0
    val ftsGroupedByTime = new ArrayBuffer[ArrayBuffer[Feature]](1)
    var ftGroupIdx = 0
    val ftGroupIdxByAssignedFtId = new java.util.HashMap[Int,Int]
    
    // Clusterize features by time
    for( putativeFirstCft <- ftsSortedByTime ) {
      curFtIdx += 1
      
      // Skip feature if it has been already assigned to a group
      if( ! ftGroupIdxByAssignedFtId.containsKey(putativeFirstCft.id) ) {
        ftsGroupedByTime += ArrayBuffer( putativeFirstCft )
        ftGroupIdxByAssignedFtId.put(putativeFirstCft.id, ftGroupIdx)
        
        val clusterFirstTime = scanById(putativeFirstCft.firstScanId).time
        val clusterLastTime = scanById(putativeFirstCft.lastScanId).time
        var( minTime, maxTime ) = ( clusterFirstTime - timeTol, clusterLastTime + timeTol )
        
        // Search for other cluster features
        var reachedFarthestFeature = false
        for( tmpFtIdx <- curFtIdx until nbFts ) {
          if( !reachedFarthestFeature ) {
            val tmpFt = ftsSortedByTime(tmpFtIdx)
            
            // Skip feature if it has been already assigned to a group
            if( ! ftGroupIdxByAssignedFtId.containsKey(tmpFt.id) ) {
              
              val tmpFtFirstTime = scanById(tmpFt.firstScanId).time
              if( tmpFtFirstTime <= maxTime ) {
                ftsGroupedByTime(ftGroupIdx) += tmpFt
                maxTime = scanById(tmpFt.lastScanId).time + timeTol
                ftGroupIdxByAssignedFtId.put( tmpFt.id, ftGroupIdx )
              } else {
                reachedFarthestFeature = true
              }
            }
          }
        }
        
        // Increment group index for next putative first cluster feature
        ftGroupIdx += 1
      }
    }
    
    ftsGroupedByTime

  }

  private def splitFtGroupByMoz( ftGroup: Seq[Feature], mozTol: Double ): ArrayBuffer[Seq[Feature]] = {
  
    val nbFeatures = ftGroup.length
    if( nbFeatures == 0 ) return null
    
    // Sort features by m/z
    val sortedFts = ftGroup.toList.sort { (a,b) => a.moz < b.moz }
    
    // Check if first ft moz is too far from last ft moz
    val firstFt = sortedFts(0)
    val lastFt = sortedFts(nbFeatures-1)
    
    var ftsGroupedByMz = new ArrayBuffer[Seq[Feature]]
    
    // If there is only one feature in the group or if the features are close in m/z
    if( nbFeatures == 1 || math.abs(firstFt.moz - lastFt.moz) < mozTol ) {
      // Keep the input feature group as is
      ftsGroupedByMz = ArrayBuffer( ftGroup )
    // Else split feature group into new groups
    } else {
      
      // Retrieve the median object which is taken as a reference
      val refFt = getMedianObject( sortedFts, this.ftMozSortingFunc )
      val refFtId = refFt.id
      val refFtMoz = refFt.moz
      
      // Aggregate features which are close in m/z to the reference
      val tmpFtGroup = ArrayBuffer( refFt )
      val tmpOtherFts = new ArrayBuffer[Feature]
      for( ft <- sortedFts ) {
        if( ft.id != refFtId ) {
          if( math.abs(ft.moz - refFtMoz ) < mozTol ) { tmpFtGroup += ft }
          else { tmpOtherFts += ft }
        }
      }
      
      // Append the new group to the list of groups
      ftsGroupedByMz += tmpFtGroup
      
      // Check if we need to split the group again (recursive method call)
      val nbOtherFts = tmpOtherFts.length
      if( nbOtherFts > 1 ) {
        val otherFtGroups = this.splitFtGroupByMoz( tmpOtherFts, mozTol )
        ftsGroupedByMz ++= otherFtGroups
      } else if ( nbOtherFts == 1 ) { ftsGroupedByMz += tmpOtherFts }
    }
    
    return ftsGroupedByMz
  }
  
  private def findMostIntenseFeature( features: List[Feature] ): Feature = {
    
    val sortedFeatures = features.sort { (a,b) => a.intensity > b.intensity }
    sortedFeatures(0)
    
    // Group features by intensity
    /*val ftByIntensity = features map { ft => (ft.intensity -> ft) }
    //push(@{ ftByIntensity( _.intensity ) }, _ ) for features
    val ftIntensities = keys(ftByIntensity)
    ftIntensities = sort { a <=> b } ftIntensities
    
    val ftMostIntense = ftByIntensity(ftIntensities(-1)).(0)
    return ftMostIntense*/
    
  }
  
  private def computeClusterTime( features: List[Feature], methodName: String ): Float = {
  
    var ftClusterTime = Float.NaN
    
    if( methodName == "median" ) ftClusterTime = getMedianObject( features, ftTimeSortingFunc ).elutionTime
    else if ( methodName == "most_intense" ) ftClusterTime = this.findMostIntenseFeature(features).elutionTime
    else throw new Exception("the cluster time computation method '"+methodName+"' is not implemented")
    
    ftClusterTime
  
  }
  
  private def computeClusterIntensity ( features: List[Feature], methodName: String ): Double = {
    
    var ftClusterIntensity = 0.0
    if ( methodName == "sum" ) for( ft <- features ) ftClusterIntensity += ft.intensity
    else if ( methodName == "most_intense" ) ftClusterIntensity = this.findMostIntenseFeature(features).intensity
    else throw new Exception( "the cluster intensity computation method '"+methodName+"' is not implemented" )
    
    ftClusterIntensity
  
  }

}
