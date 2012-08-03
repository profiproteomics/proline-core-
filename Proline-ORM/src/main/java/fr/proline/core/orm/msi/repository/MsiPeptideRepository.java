package fr.proline.core.orm.msi.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.utils.JPARepository;
import fr.proline.core.orm.utils.StringUtils;

public class MsiPeptideRepository extends JPARepository {

    public MsiPeptideRepository(final EntityManager msiEm) {
	super(msiEm);
    }

    public List<Peptide> findPeptidesForSequence(final String seq) {

	if (StringUtils.isEmpty(seq)) {
	    throw new IllegalArgumentException("Invalid seq");
	}

	final TypedQuery<Peptide> query = getEntityManager().createNamedQuery("findMsiPepsForSeq",
		Peptide.class);
	query.setParameter("seq", seq.toLowerCase());

	return query.getResultList();
    }

    public Peptide findPeptideForSequenceAndPtmStr(final String seq, final String ptmStr) {

	if (StringUtils.isEmpty(seq)) {
	    throw new IllegalArgumentException("Invalid seq");
	}

	Peptide result = null;

	TypedQuery<Peptide> query = null;

	if (ptmStr == null) { // Assume NULL <> "" (empty)
	    query = getEntityManager().createNamedQuery("findMsiPeptForSeq", Peptide.class);
	} else {
	    query = getEntityManager().createNamedQuery("findMsiPeptForSeqAndPtmStr", Peptide.class);
	    query.setParameter("ptmStr", ptmStr.toLowerCase());
	}

	query.setParameter("seq", seq.toLowerCase()); // In all cases give a Peptide sequence

	final List<Peptide> peptides = query.getResultList();

	if ((peptides != null) && !peptides.isEmpty()) {

	    if (peptides.size() == 1) {
		result = peptides.get(0);
	    } else {
		throw new RuntimeException("There are more than one Peptide for given sequence and ptmString");
	    }

	}

	return result;
    }

}
