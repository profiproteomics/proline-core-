package fr.proline.core.om.model.msi

import scala.reflect.BeanProperty
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include


@JsonInclude( Include.NON_NULL )
case class FilterDescriptor (
  @BeanProperty var parameter: String,
  @BeanProperty var description: Option[String] = None,
  @BeanProperty var properties: Option[Map[String,Any]] = None
)

