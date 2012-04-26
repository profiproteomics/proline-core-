package fr.proline.core

import net.noerd.prequel._

object LcmsDb {
  
  def apply( projectId: Int ): LcmsDb = {
    
    // TODO: change the configuration according to the project id
    
    val lcmsDbConfig = this.getDefaultConfig
    new LcmsDb( lcmsDbConfig )
  }
  
  def getDefaultConfig = DatabaseConfig(
    driver = "org.sqlite.JDBC",
    jdbcURL = "jdbc:sqlite:D:/prosper/data/lcms-db.sqlite",
    isolationLevel = IsolationLevels.Serializable,
    sqlFormatter = SQLFormatter.HSQLDBSQLFormatter
    )
}

class LcmsDb( val config: DatabaseConfig,
              val boolStrAsInt: Boolean = true, // TODO: set to false when DB model is updated
              val maxVariableNumber: Int = 999 ) extends Database {
  
}