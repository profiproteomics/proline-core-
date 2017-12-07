package fr.proline.core.algo.lcms

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import fr.profi.util.ms.MassTolUnit
import fr.profi.util.serialization.ProfiJson
import fr.proline.core.algo.lcms.filtering.Filter
import fr.proline.core.algo.lcms.filtering.FilterOperator
import fr.proline.core.algo.msq.config.ExtractionParams
import fr.proline.core.algo.msq.config.LabelFreeQuantConfig
import fr.proline.core.om.msi.AbstractSerializationSpec
import fr.proline.core.om.msi.SerializationSpecif

@RunWith(classOf[JUnitRunner])
class SerializationSpec extends AbstractSerializationSpec {
  
  val quantConfig = LabelFreeQuantConfig(
    extractionParams = ExtractionParams(  // for extraction and MS2 mapping
      mozTol = 5.0,
      mozTolUnit = MassTolUnit.PPM.toString
    ),
    clusteringParams = ClusteringParams(
      mozTol = 5.0,
      mozTolUnit = MassTolUnit.PPM.toString,
      timeTol = 15f, // in seconds
      intensityComputation = ClusterIntensityComputation.MOST_INTENSE.toString,
      timeComputation = ClusterTimeComputation.MOST_INTENSE.toString
    ),
    alnMethodName = AlnMethod.ITERATIVE.toString,
    alnParams = AlignmentParams(
      massInterval = 20000, // big value mean not used
      smoothingMethodName = AlnSmoothing.TIME_WINDOW.toString,
      smoothingParams = AlnSmoothingParams(
        windowSize = 60, // seconds
        windowOverlap = 20, // percents
        minWindowLandmarks = 50
      ),
      ftMappingMethodName = None,
      ftMappingParams = FeatureMappingParams(
        mozTol = 5.0,
        mozTolUnit = MassTolUnit.PPM.toString,
        timeTol = 300
      )
    ),
    ftFilter = Filter(
      name = FeatureFilterType.INTENSITY.toString,
      operator = FilterOperator.GT.toString,
      value = 0.0
    ),
    ftMappingParams = FeatureMappingParams(
      mozTol = 5.0,
      mozTolUnit = MassTolUnit.PPM.toString,
      timeTol = 30f
    ),
    normalizationMethod = Some(NormalizationMethod.MEDIAN_RATIO.toString)
  )
  
  // Note : the used values are not representative of any real case
  val jsonSpecifs = List(
    SerializationSpecif(
      "A LabelFree Quant Config",
      profiDeserializer = Some( jsonData => ProfiJson.deserialize[LabelFreeQuantConfig](jsonData) ),
      quantConfig,
      """{"extraction_params":{"moz_tol":5.0,"moz_tol_unit":"ppm"},"clustering_params":"""+
      """{"moz_tol":5.0,"moz_tol_unit":"ppm","time_tol":15.0,"intensity_computation":"MOST_INTENSE","""+
      """"time_computation":"MOST_INTENSE"},"aln_method_name":"ITERATIVE","aln_params":{"mass_interval":20000,"""+
      """"smoothing_method_name":"TIME_WINDOW","smoothing_params":{"window_size":60,"window_overlap":20,"""+
      """"min_window_landmarks":50},"ft_mapping_params":{"moz_tol":5.0,"moz_tol_unit":"ppm","time_tol":300.0},"""+
      """"max_iterations":3},"ft_filter":{"name":"INTENSITY","operator":"GT","value":0.0},"ft_mapping_params":"""+
      """{"moz_tol":5.0,"moz_tol_unit":"ppm","time_tol":30.0},"normalization_method":"MEDIAN_RATIO","""+
      """"detect_features":false,"detect_peakels":false,"start_from_validated_peptides":true,"""+
      """"use_last_peakel_detection":false,"perform_cross_assignment_inside_groups_only":false, "restrain_cross_assignment_to_reliable_features":false}"""
    )
  )
  
  this.checkJsonSpecifs( jsonSpecifs )
  
}