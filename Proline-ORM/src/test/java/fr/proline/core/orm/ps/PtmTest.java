package fr.proline.core.orm.ps;

import static org.junit.Assert.*;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.proline.core.orm.ps.repository.PsPtmRepository;
import fr.proline.repository.ProlineDatabaseType;
import fr.proline.repository.util.DatabaseTestCase;

public class PtmTest extends DatabaseTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(PtmTest.class);

    @Override
    public ProlineDatabaseType getProlineDatabaseType() {
	return ProlineDatabaseType.PS;
    }

    @Before
    public void setUp() throws Exception {
	initDatabase();

	// "/fr/proline/core/orm/ps/Unimod_Dataset.xml"
	String[] datasets = new String[] { "/dbunit/datasets/ps-db_init_dataset.xml",
		"/dbunit/datasets/ps/Peptides_Dataset.xml" };

	loadCompositeDataSet(datasets);
    }

    @Test
    public void readPtm() {
	final EntityManagerFactory emf = getConnector().getEntityManagerFactory();

	final EntityManager psEm = emf.createEntityManager();

	try {
	    TypedQuery<Ptm> query = psEm.createQuery(
		    "Select ptm from Ptm ptm where ptm.unimodId = :unimod_id", Ptm.class);
	    query.setParameter("unimod_id", Long.valueOf(21L));
	    Ptm ptm = query.getSingleResult();
	    assertEquals(ptm.getFullName(), "Phosphorylation");
	    Set<PtmEvidence> evidences = ptm.getEvidences();
	    assertEquals(evidences.size(), 5);

	    Set<PtmSpecificity> specificities = ptm.getSpecificities();
	    assertEquals(specificities.size(), 8);
	} finally {

	    if (psEm != null) {
		try {
		    psEm.close();
		} catch (Exception exClose) {
		    LOG.error("Error closing PS EntityManager", exClose);
		}
	    }

	}

    }

    @Test
    public void findPtmByName() {
	final EntityManagerFactory emf = getConnector().getEntityManagerFactory();

	final EntityManager psEm = emf.createEntityManager();

	try {
	    Ptm phosPtm = PsPtmRepository.findPtmForName(psEm, "Phospho");
	    assertNotNull(phosPtm);
	    assertEquals(phosPtm.getShortName(), "Phospho");
	    assertEquals(phosPtm.getFullName(), "Phosphorylation");
	    Ptm phosPtm2 = PsPtmRepository.findPtmForName(psEm, "PHosPHo");
	    assertNotNull(phosPtm2);
	    assertSame(phosPtm2, phosPtm);
	    Ptm phosPtm3 = PsPtmRepository.findPtmForName(psEm, "PHosPHorylation");
	    assertNotNull(phosPtm3);
	    assertSame(phosPtm3, phosPtm);
	} finally {

	    if (psEm != null) {
		try {
		    psEm.close();
		} catch (Exception exClose) {
		    LOG.error("Error closing PS EntityManager", exClose);
		}
	    }

	}

    }

    @Test
    public void findPtmClassification() {
	final EntityManagerFactory emf = getConnector().getEntityManagerFactory();

	final EntityManager psEm = emf.createEntityManager();

	try {
	    final PtmClassification classification = PsPtmRepository.findPtmClassificationForName(psEm,
		    "Chemical derivative");

	    assertNotNull("Chemical derivative PtmClassification", classification);
	} finally {

	    if (psEm != null) {
		try {
		    psEm.close();
		} catch (Exception exClose) {
		    LOG.error("Error closing PS EntityManager", exClose);
		}
	    }

	}

    }

    @After
    public void tearDown() {
	super.tearDown();
    }

}
