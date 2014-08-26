package fr.proline.core.algo.msi

import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import com.typesafe.scalalogging.slf4j.Logging
import fr.proline.context.IExecutionContext
import fr.proline.core.dal.AbstractMultipleDBTestCase
import fr.proline.core.dal.ContextFactory
import fr.proline.repository.DriverType
import fr.proline.core.dbunit.STR_F063442_F122817_MergedRSMs
import fr.proline.core.dbunit.DbUnitSampleDataset

object TypicalProteinChooserTest extends AbstractMultipleDBTestCase with Logging {

  // Define some vars
  val driverType = DriverType.H2
  val dbUnitResultFile = STR_F063442_F122817_MergedRSMs
  val targetRSMId: Long = 33L

  var executionContext: IExecutionContext = null

  @BeforeClass
  @throws(classOf[Exception])
  def setUp() = {

    logger.info("Initializing DBs")
    super.initDBsDBManagement(driverType)

    //Load Data
    pdiDBTestCase.loadDataSet(DbUnitSampleDataset.PROTEINS.getResourcePath)
    psDBTestCase.loadDataSet(dbUnitResultFile.psDbDatasetPath)
    msiDBTestCase.loadCompositeDataSet(
      Array(
        dbUnitResultFile.msiDbDatasetPath,
        "/fr/proline/core/algo/msi/Prot_ChangeTypical.xml"
      )
    )
    udsDBTestCase.loadDataSet(dbUnitResultFile.udsDbDatasetPath)    
    
    logger.info("PDI, PS, MSI and UDS dbs succesfully initialized !")
    
    executionContext = buildJPAContext()
  }

  @AfterClass
  override def tearDown() {
    if (executionContext != null) executionContext.closeAll()
    super.tearDown()
  }

  def buildJPAContext() = {
    ContextFactory.buildExecutionContext(dsConnectorFactoryForTest, 1, true) // Full JPA
  }
  
}

class TypicalProteinChooserTest extends AbstractMultipleDBTestCase with Logging {

  val targetRSMId = TypicalProteinChooserTest.targetRSMId
  val executionContext = TypicalProteinChooserTest.executionContext
  val msiEM = executionContext.getMSIDbConnectionContext().getEntityManager()

  @Test
  def testChangeTypicalProt() = {

    // Check which proteinSets should be modified by algo
    val nbrTremblShouldChange: Int = 9 // # proteinSet matching following rule 
    //VDS WARNING : Was 4, after modifying algo, changed proteinset was 9 dur to incorrect IS_IN_SUBSET value in XML file :  
    // Allways FALSE even for subset. Should be changed back to 4 with corrected XML file ! 

    val typicalChooser = new TypicalProteinChooser()

    val ruleDesc = new TypicalProteinChooserRule(
      ruleName = "Sprot AC preferred",
      applyToAcc = true,
      rulePattern = "\\w{6,6}"
    )
    val rules = Seq(ruleDesc)
    typicalChooser.changeTypical(targetRSMId, rules, msiEM)

    val nbrChangedTyp = typicalChooser.getChangedProteinSets.size

    Assert.assertEquals(nbrTremblShouldChange, nbrChangedTyp)

  }

  @Test
  def testChangeTypicalProtSameSubSet() = {

    // Check which proteinSets should be modified by algo
    var nbrTremblShouldChange: Int = 1

    val typicalChooser = new TypicalProteinChooser()
    val ruleDesc = new TypicalProteinChooserRule(
      ruleName = "Description ##SP  preferred",
      applyToAcc = false,
      rulePattern = "##SP.*"
    )
    val rules = Seq(ruleDesc)
    typicalChooser.changeTypical(targetRSMId, rules, msiEM)

    val nbrChangedTyp = typicalChooser.getChangedProteinSets.size

    Assert.assertEquals(nbrTremblShouldChange, nbrChangedTyp)

    // Check which proteinSets should be modified by algo
    nbrTremblShouldChange = 0

    val ruleDesc2 = new TypicalProteinChooserRule(
      ruleName = "Description ##DEV_ preferred",
      applyToAcc = false,
      rulePattern = "##DEV_.*"
    )
    val rules2 = Seq(ruleDesc2)
    typicalChooser.changeTypical(targetRSMId, rules2, msiEM)

    val nbrChangedTyp2 = typicalChooser.getChangedProteinSets.size

    Assert.assertEquals(nbrTremblShouldChange, nbrChangedTyp2)

  }

}
