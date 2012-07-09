package fr.proline.core.dal

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert
import org.scalatest.junit.JUnitSuite
import com.weiglewilczek.slf4s.Logging
import fr.proline.core.orm.utils.JPAUtil
import fr.proline.repository.utils.DatabaseTestConnector
import fr.proline.repository.utils.DatabaseUtils
import fr.proline.repository.DatabaseConnector
import fr.proline.repository.ProlineRepository
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence
import fr.proline.core.orm.ps.Ptm
import org.hamcrest.CoreMatchers

class DatabaseManagmentTest extends JUnitSuite  with Logging {
     
  var dbMgnt:DatabaseManagment = null
  protected var em:EntityManager = null
  protected var emf:EntityManagerFactory = null
  
  lazy val udsConnector : DatabaseTestConnector = {
	  val conn = new DatabaseTestConnector(DatabaseUtils.DEFAULT_DATABASE_PROPERTIES_FILENAME);
	  try {
		  // This is necessary since in-memory databases are closed when the last connection is closed. This
		  // method call creates a first connection that will be closed by closeAll() method.
		  conn.getConnection()
	  } catch {
	    case e: Exception =>  e.printStackTrace();
	  }
	conn		
  }
  
  @Before
  def initialize() = {
    DatabaseUtils.initDatabase(udsConnector, DatabaseUtils.H2_DATABASE_UDS_SCRIPT_LOCATION)
    emf  = Persistence.createEntityManagerFactory(JPAUtil.PersistenceUnitNames.getPersistenceUnitNameForDB(ProlineRepository.Databases.UDS), udsConnector.getEntityManagerSettings);
    em = emf.createEntityManager();
    DatabaseUtils.loadDataSet(udsConnector, "/fr/proline/core/om/uds/UDS_Simple_Dataset.xml");
    udsConnector.getDatabaseTester().onSetup();
    dbMgnt = new DatabaseManagment(udsConnector)
  }
  

  
  @After 
  @throws(classOf[Exception]) 
   def tearDown() ={
	  udsConnector.getDatabaseTester().onTearDown();
	  em.close();
	  emf.close();
	  udsConnector.closeAll();
  }
  
  @Test
  def testGetConnection() = {
    val myConn : DatabaseConnector = dbMgnt.psDBConnector
    logger.debug("--- PS DB CONN "+myConn.getConnection.getMetaData.getURL)
  }

  @Test
  def testGetPSInformationConnection() = {
    val myConn : DatabaseConnector = dbMgnt.psDBConnector
	emf  = Persistence.createEntityManagerFactory(JPAUtil.PersistenceUnitNames.getPersistenceUnitNameForDB(ProlineRepository.Databases.PS), myConn.getEntityManagerSettings);
    em = emf.createEntityManager();
    //READ  <PTM ID="93" UNIMOD_ID="122" FULL_NAME="Formylation" SHORT_NAME="Formyl"/>
    val myPtm :Ptm = em.find(classOf[Ptm], 93);
    Assert.assertThat(myPtm.getShortName,CoreMatchers.equalTo("Formyl"))
    logger.debug("--- PS DB CONN "+myConn.getConnection.getMetaData.getURL)
  }
}