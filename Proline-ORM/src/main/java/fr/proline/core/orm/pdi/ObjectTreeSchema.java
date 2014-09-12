package fr.proline.core.orm.pdi;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * The persistent class for the admin_infos database table.
 * 
 */
@Entity(name="fr.proline.core.orm.pdi.ObjectTreeSchema")
@Table(name="object_tree_schema")
public class ObjectTreeSchema implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="name")
	private String name;

	@Column(name="type")
	private String type;
	
    @Column(name="is_binary_mode")
    private boolean isBinaryMode;

	@Column(name="version")
	private String version;

	@Column(name="schema")
	private String schema;
	
	@Column(name="description")
	private String description;

	@Column(name="serialized_properties")
	private String serializedProperties;
	
	public ObjectTreeSchema() {
    }
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public boolean getIsBinaryMode() {
		return this.isBinaryMode;
	}

	public void setIsBinaryMode(boolean isBinaryMode) {
		this.isBinaryMode = isBinaryMode;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSerializedProperties() {
		return serializedProperties;
	}

	public void setSerializedProperties(String serializedProperties) {
		this.serializedProperties = serializedProperties;
	}

}