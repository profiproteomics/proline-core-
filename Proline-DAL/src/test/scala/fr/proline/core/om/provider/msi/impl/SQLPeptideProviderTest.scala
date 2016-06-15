package fr.proline.core.om.provider.msi.impl

import scala.collection.mutable.ArrayBuffer

import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Assert._
import org.junit.Before
import org.junit.Test

import fr.proline.core.dal._
import fr.proline.core.om.model.msi._
import fr.proline.repository.ProlineDatabaseType
import fr.proline.repository.util.DatabaseTestCase

@Test
class SQLPeptideProviderTest extends DatabaseTestCase {

  private val SEQ_TO_FOUND: String = "LTGMAFR"

  override def getProlineDatabaseType() = ProlineDatabaseType.PS

  @Before
  @throws(classOf[Exception])
  def setUp() = {
    initDatabase()

    SQLPeptideProvider.clear() // Clear peptide cache between tests

    //loadDataSet("/fr/proline/core/om/ps/Unimod_Dataset.xml")
    loadCompositeDataSet(Array("/dbunit/datasets/ps-db_init_dataset.xml", "/dbunit/datasets/ps/Peptides_Dataset.xml"))
  }

    override def getPropertiesFileName(): String = {
	return "db_settings/h2/db_ps.properties";
  }
  
    
  @Test
  def getSinglePeptide() = {
    val psDb = BuildDbConnectionContext(getConnector, false)

    try {

      val sqlPepProvider = new SQLPeptideProvider(psDb)

      val pep: Option[Peptide] = sqlPepProvider.getPeptide(4)
      assertThat(pep, CoreMatchers.notNullValue())
      assertNotSame(pep, None)
      assertThat(pep.get.calculatedMass, CoreMatchers.equalTo(810.405807))

    } finally {
      psDb.close
    }

  }

  @Test
  def getMultiplePeptides() = {
    val ids = new ArrayBuffer[Long]
    ids += 0
    ids += 1
    ids += 4

    val psDb = BuildDbConnectionContext(getConnector, false)

    try {

      val sqlPepProvider = new SQLPeptideProvider(psDb)

      val peps: Array[Option[Peptide]] = sqlPepProvider.getPeptidesAsOptions(ids)
      assertThat(peps, CoreMatchers.notNullValue())
      assertThat(peps.length, CoreMatchers.equalTo(3))
      assertEquals(peps.apply(2).get.id, 4L)
      assertThat(peps(2).get.calculatedMass, CoreMatchers.equalTo(810.405807))

    } finally {
      psDb.close
    }

  }

  @Test
  def getPeptideWithNTermPTM() = {
    val psDb = BuildDbConnectionContext(getConnector, false)

    try {
      val sqlPepProvider = new SQLPeptideProvider(psDb)

      val pep: Option[Peptide] = sqlPepProvider.getPeptide(6)
      assertThat(pep, CoreMatchers.notNullValue())
      assertNotSame(pep, None)

      assertEquals(pep.get.id, 6L)
      assertThat(pep.get.ptms.length, CoreMatchers.equalTo(1))
      assertThat(pep.get.ptms(0).definition.names.shortName, CoreMatchers.equalTo("Acetyl"))
      assertTrue(pep.get.ptms(0).isNTerm)

    } finally {
      psDb.close
    }

  }

  @Test
  def getPeptideOnSeqAndNoPtms() = {
    val psDb = BuildDbConnectionContext(getConnector, false)

    try {
      val sqlPepProvider = new SQLPeptideProvider(psDb)

      val ptms = new Array[LocatedPtm](0)
      val pep: Option[Peptide] = sqlPepProvider.getPeptide(SEQ_TO_FOUND, ptms)
      assertThat(pep, CoreMatchers.notNullValue())
      assertNotSame(pep, None)
      assertTrue(pep.get.ptms == null || pep.get.ptms.length == 0)

    } finally {
      psDb.close
    }

  }

  @Test
  def getPeptideOnSeqAndPtms() = {
    val psDb = BuildDbConnectionContext(getConnector, false)

    try {
      val sqlPepProvider = new SQLPeptideProvider(psDb)

      var ptmsBuilder = Array.newBuilder[LocatedPtm]

      val ptmEvi: PtmEvidence = new PtmEvidence(IonTypes.Precursor, "", Double.MaxValue, Double.MaxValue, false)
      val ptmEvidences = Array[PtmEvidence](ptmEvi)

      val ptmDef = new PtmDefinition(0, "ANYWHERE", new PtmNames("Oxidation", "Oxidation or Hydroxylation"), ptmEvidences, 'M', null, 0)
      ptmsBuilder += new LocatedPtm(ptmDef, 3, Double.MaxValue, Double.MaxValue, "O", false, false)

      /*val provTest = new fr.proline.core.om.provider.msi.impl.ORMPTMProvider( this.em )
	  val ptmDefs = provTest.getPtmDefinitions(List(1,2,30))
	  ptmDefs.foreach { ptm => println(ptm.get.names.shortName ) }*/

      val pep: Option[Peptide] = sqlPepProvider.getPeptide(SEQ_TO_FOUND, ptmsBuilder.result())
      assertThat(pep, CoreMatchers.notNullValue())
      assertNotSame(pep, None)
      assertThat(pep.get.ptms.length, CoreMatchers.equalTo(1))
      assertThat(pep.get.ptms(0).seqPosition, CoreMatchers.equalTo(3))

    } finally {
      psDb.close
    }

  }

  @Test
  def getPeptideWithMultiplePtmSpecificities() = {
    val psDb = BuildDbConnectionContext(getConnector, false)

    try {

      val sqlPepProvider = new SQLPeptideProvider(psDb)

      val pep: Option[Peptide] = sqlPepProvider.getPeptide(7)
      assertThat(pep, CoreMatchers.notNullValue())
      assertNotSame(pep, None)
      assertThat(pep.get.ptms.head.definition.neutralLosses.size, CoreMatchers.equalTo(2))

    } finally {
      psDb.close
    }

  }

  @After
  override def tearDown() = {

    try {
      SQLPeptideProvider.clear() // Clear peptide cache between tests
    } finally {
      super.tearDown()
    }

  }

}