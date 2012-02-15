package fr.proline.core.algo.lcms.normalization

class IntensitySumMapNormalizer extends IMapSetNormalizer {
  
  import scala.collection.mutable.ArrayBuffer
  import fr.proline.core.om.lcms._

  protected def computeNormalizationFactorByMapId( mapSet: MapSet ): Map[Int,Float] = {

    this.calcNormalizationFactorByMapId( mapSet.getChildMapIds.toList, this.getIntensitySumByMapId(mapSet)  )

  }
  
  private[normalization] def getIntensitySumByMapId( mapSet: MapSet ): Map[Int,Double] = {
    
    val intensitySumByMapIdBuilder = scala.collection.immutable.Map.newBuilder[Int,Double]
    
    // Compute intensity sum for each map
    for( map <- mapSet.childMaps ) {
      
      var mapSumIntensity = 0.0      
      for( mapFt <- map.features ) {        
        mapSumIntensity += mapFt.intensity
      }
      
      intensitySumByMapIdBuilder += (map.id -> mapSumIntensity)
    }
    
    intensitySumByMapIdBuilder.result()
  }
  
}