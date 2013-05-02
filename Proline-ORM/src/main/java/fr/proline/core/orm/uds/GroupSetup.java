package fr.proline.core.orm.uds;

import java.io.Serializable;
import javax.persistence.*;

import java.util.Set;


/**
 * The persistent class for the group_setup database table.
 * 
 */
@Entity
@Table(name="group_setup")
public class GroupSetup implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	private String name;

	@Column(name="serialized_properties")
	private String serializedProperties;

	//bi-directional many-to-many association to BiologicalGroup
	@ManyToMany(mappedBy="groupSetups")
	private Set<BiologicalGroup> biologicalGroups;

	//bi-directional many-to-one association to Dataset
    @ManyToOne    
    @JoinColumn(name="quantitation_id")
	private Dataset dataset;

	//bi-directional many-to-one association to RatioDefinition
	@OneToMany(mappedBy="groupSetup")
	private Set<RatioDefinition> ratioDefinitions;

    public GroupSetup() {
    }

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSerializedProperties() {
		return this.serializedProperties;
	}

	public void setSerializedProperties(String serializedProperties) {
		this.serializedProperties = serializedProperties;
	}

	public Set<BiologicalGroup> getBiologicalGroups() {
		return this.biologicalGroups;
	}

	public void setBiologicalGroups(Set<BiologicalGroup> biologicalGroups) {
		this.biologicalGroups = biologicalGroups;
	}
	
	// TODO: return a true QuantitationDataset object when it is implemented
	public Dataset getQuantitationDataset() {
		return this.dataset;
	}

	public void setQuantitationDataset(Dataset dataset) {
		this.dataset = dataset;
	}
	
	public Set<RatioDefinition> getRatioDefinitions() {
		return this.ratioDefinitions;
	}

	public void setRatioDefinitions(Set<RatioDefinition> ratioDefinitions) {
		this.ratioDefinitions = ratioDefinitions;
	}
	
}