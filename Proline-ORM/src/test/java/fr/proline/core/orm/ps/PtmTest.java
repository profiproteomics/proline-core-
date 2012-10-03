package fr.proline.core.orm.ps;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.*;

import java.util.Set;

import javax.persistence.TypedQuery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.proline.core.orm.ps.repository.PsPtmRepository;
import fr.proline.core.orm.utils.JPAUtil;
import fr.proline.repository.utils.DatabaseTestCase;
import fr.proline.repository.utils.DatabaseUtils;

public class PtmTest extends DatabaseTestCase {

	PsPtmRepository ptmRepo;
	
	@Before public void setUp() throws Exception {
        initDatabase();
        initEntityManager(JPAUtil.PersistenceUnitNames.PS_Key.getPersistenceUnitName());
        loadDataSet("/fr/proline/core/orm/ps/Unimod_Dataset.xml");
        ptmRepo = new PsPtmRepository(em);
	}

	@After public void tearDown() throws Exception {
		super.tearDown();
	}
	

	@Test public void readPtm() {
		TypedQuery<Ptm> query = em.createQuery("Select ptm from Ptm ptm where ptm.unimodId = :unimod_id", Ptm.class);
		query.setParameter("unimod_id", 21);
		Ptm ptm = query.getSingleResult();
		assertThat(ptm.getFullName(), equalTo("Phosphorylation"));
		Set<PtmEvidence> evidences = ptm.getEvidences();
		assertThat(evidences.size(), is(5));
		
		
		Set<PtmSpecificity> specificities = ptm.getSpecificities();
		assertThat(specificities.size(), is(8));
	}
	
	@Test public void findPtmByName() {
		Ptm phosPtm = ptmRepo.findPtmForName("Phospho");
		assertThat(phosPtm, notNullValue());
		assertThat(phosPtm.getShortName(), equalTo("Phospho"));
		assertThat(phosPtm.getFullName(), equalTo("Phosphorylation"));
		Ptm phosPtm2 = ptmRepo.findPtmForName("PHosPHo");
		assertThat(phosPtm2, notNullValue());
		assertThat(phosPtm2, sameInstance(phosPtm));
		Ptm phosPtm3 = ptmRepo.findPtmForName("PHosPHorylation");
		assertThat(phosPtm3, notNullValue());
		assertThat(phosPtm3, sameInstance(phosPtm));
	}
	
	@Test
	public void findPtmClassification() {
	    final PtmClassification classification = ptmRepo.findPtmClassificationForName("Chemical derivative");

	    assertNotNull("Chemical derivative PtmClassification", classification);
	}
	
	@Override
	public String getSQLScriptLocation() {
		return DatabaseUtils.H2_DATABASE_PS_SCRIPT_LOCATION;
	}
	
}
