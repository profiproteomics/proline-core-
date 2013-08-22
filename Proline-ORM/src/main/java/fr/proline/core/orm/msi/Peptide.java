package fr.proline.core.orm.msi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

import fr.proline.core.orm.ps.PeptidePtm;
import fr.proline.util.StringUtils;

/**
 * The persistent class for the peptide database table.
 * 
 */
@Entity(name = "fr.proline.core.orm.msi.Peptide")
@NamedQueries({
	@NamedQuery(name = "findMsiPepsForSeq", query = "select p from fr.proline.core.orm.msi.Peptide p"
		+ " where upper(p.sequence) = :seq"),

	@NamedQuery(name = "findMsiPepsForIds", query = "select p from fr.proline.core.orm.msi.Peptide p"
		+ " where p.id in :ids"),

	@NamedQuery(name = "findMsiPeptForSeqAndPtmStr", query = "select p from fr.proline.core.orm.msi.Peptide p"
		+ " where (upper(p.sequence) = :seq) and (upper(p.ptmString) = :ptmStr))"),

	@NamedQuery(name = "findMsiPeptForSeq", query = "select p from fr.proline.core.orm.msi.Peptide p"
		+ " where (upper(p.sequence) = :seq) and (p.ptmString is null)")

})
public class Peptide implements Serializable, Comparable<Peptide> {

    private static final long serialVersionUID = 1L;

    @Id
    // MSI Peptide Id are not generated (taken from Ps Peptide entity)
    private long id;

    @Column(name = "calculated_mass")
    private double calculatedMass;

    @Column(name = "ptm_string")
    private String ptmString;

    private String sequence;

    @Column(name = "serialized_properties")
    private String serializedProperties;

    // Transient Variables not saved in database
    @Transient
    private TransientData transientData = null;

    public Peptide() {
    }

    /**
     * Create a Msi Peptide entity from a Ps Peptide entity. Created Msi Peptide entity shares the same Id
     * with given Ps Peptide.
     * 
     * @param psPeptide
     *            Peptide entity from psDb used to initialize Msi Peptide fields (must not be
     *            <code>null</code>)
     */
    public Peptide(final fr.proline.core.orm.ps.Peptide psPeptide) {

	if (psPeptide == null) {
	    throw new IllegalArgumentException("PsPeptide is null");
	}

	setId(psPeptide.getId());
	setCalculatedMass(psPeptide.getCalculatedMass());

	final String psPtmString = psPeptide.getPtmString();

	if (StringUtils.isEmpty(psPtmString)) {
	    setPtmString(null);
	} else {
	    setPtmString(psPtmString);
	}

	setSequence(psPeptide.getSequence());

	final String psPeptideProps = psPeptide.getSerializedProperties();

	if (StringUtils.isEmpty(psPeptideProps)) {
	    setSerializedProperties(null);
	} else {
	    setSerializedProperties(psPeptideProps);
	}

    }

    public long getId() {
	return id;
    }

    public void setId(final long pId) {
	id = pId;
    }

    public double getCalculatedMass() {
	return this.calculatedMass;
    }

    public void setCalculatedMass(double calculatedMass) {
	this.calculatedMass = calculatedMass;
    }

    public String getPtmString() {
	return this.ptmString;
    }

    public void setPtmString(String ptmString) {
	this.ptmString = ptmString;
    }

    public String getSequence() {
	return this.sequence;
    }

    public void setSequence(String sequence) {
	this.sequence = sequence;
    }

    public String getSerializedProperties() {
	return this.serializedProperties;
    }

    public void setSerializedProperties(String serializedProperties) {
	this.serializedProperties = serializedProperties;
    }

    public TransientData getTransientData() {
	if (transientData == null) {
	    transientData = new TransientData();
	}
	return transientData;
    }

    /**
     * Transient Data which will be not saved in database Used by the Proline Studio IHM
     * 
     * @author JM235353
     */
    public static class TransientData implements Serializable {
	private static final long serialVersionUID = 1L;

	private SequenceMatch sequenceMatch = null;
	private ArrayList<ProteinSet> proteinSetArray = null; // Protein Groups where the peptide has been
							      // found
	private HashMap<Integer, PeptidePtm> peptidePtmMap = null;

	protected TransientData() {
	}

	public SequenceMatch getSequenceMatch() {
	    return sequenceMatch;
	}

	public void setSequenceMatch(SequenceMatch sequenceMatch) {
	    this.sequenceMatch = sequenceMatch;
	}

	public ArrayList<ProteinSet> getProteinSetArray() {
	    return proteinSetArray;
	}

	public void setProteinSetArray(ArrayList<ProteinSet> proteinSetArray) {
	    this.proteinSetArray = proteinSetArray;
	}

	public HashMap<Integer, PeptidePtm> getPeptidePtmMap() {
	    return peptidePtmMap;
	}

	public void setPeptidePtmMap(HashMap<Integer, PeptidePtm> peptidePtmMap) {
	    this.peptidePtmMap = peptidePtmMap;
	}

	
	//JPM.TEST
	 ArrayList<Object> SproteinSetArray = null;
	public ArrayList<Object> getSProteinSetArray() {
	    return SproteinSetArray;
	}

	public void setSProteinSetArray(ArrayList<Object> proteinSetArray) {
		SproteinSetArray = proteinSetArray;
	}
	
	
    }

    /**
     * Method for Comparable interface. Compare Peptides according to their sequence
     * 
     * @param p
     * @return
     */
    @Override
    public int compareTo(Peptide p) {
	return sequence.compareTo(p.sequence);
    }

}
