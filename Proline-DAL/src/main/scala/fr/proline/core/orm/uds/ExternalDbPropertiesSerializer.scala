package fr.proline.core.orm.uds

import com.codahale.jerkson.Json.parse
import com.codahale.jerkson.Json.generate
import fr.proline.core.om.model.uds.ExternalDbProperties
import fr.proline.repository.DriverType

object ExternalDbPropertiesSerializer {
  
  def deserialize( extDb: ExternalDb ) {
    
    // Parse properties
    val extDbProps = parse[ExternalDbProperties]( extDb.getSerializedProperties() )
    
    // Populate properties
    val driverType = extDbProps.getDriverType.get
    extDb.setDriverType( DriverType.valueOf(extDbProps.getDriverType.get) )
  }
  
  def serialize( extDb: ExternalDb ) {
    
    // Build properties object    
    if( extDb.getDriverType != null ) {
      
      val extDbProps = new ExternalDbProperties()
      extDbProps.setDriverType( Some(extDb.getDriverType.toString) )
      
      // Generate JSON string
      extDb.setSerializedProperties( generate( extDbProps ) )
    }

  }

}