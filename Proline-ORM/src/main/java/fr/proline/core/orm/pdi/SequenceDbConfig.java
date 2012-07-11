package fr.proline.core.orm.pdi;

import static javax.persistence.CascadeType.PERSIST;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The persistent class for the seq_db_config database table.
 * 
 */
@Entity
@Table(name = "seq_db_config")
public class SequenceDbConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Alphabet { AA, DNA };
	
	public enum Format { SWISS, GENEBANK, GFF };

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	private String name;
	
	@Enumerated(EnumType.STRING)
	private Alphabet alphabet;

	@Column(name = "ref_entry_format")
	@Enumerated(EnumType.STRING)
	private Format refEntryFormat;

	@Column(name = "serialized_properties")
	private String serializedProperties;

	// bi-directional many-to-one association to FastaParsingRule
	@ManyToOne(cascade = { PERSIST })
	@JoinColumn(name = "fasta_parsing_rule_id")
	private FastaParsingRule fastaParsingRule;

	// uni-directional many-to-one association to DatabaseType
	@ManyToOne
	@JoinColumn(name = "type")
	private DatabaseType type;

	protected SequenceDbConfig() {

	}
	
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public Alphabet getAlphabet() {
		return this.alphabet;
	}

	public void setAlphabet(Alphabet alphabet) {
		this.alphabet = alphabet;
	}

	public Format getRefEntryFormat() {
		return this.refEntryFormat;
	}

	public void setRefEntryFormat(Format refEntryFormat) {
		this.refEntryFormat = refEntryFormat;
	}

	public String getSerializedProperties() {
		return this.serializedProperties;
	}

	public void setSerializedProperties(String serializedProperties) {
		this.serializedProperties = serializedProperties;
	}

	public FastaParsingRule getFastaParsingRule() {
		return this.fastaParsingRule;
	}

	public void setFastaParsingRule(FastaParsingRule fastaParsingRule) {
		this.fastaParsingRule = fastaParsingRule;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DatabaseType getType() {
		return type;
	}

	public void setType(DatabaseType type) {
		this.type = type;
	}

}