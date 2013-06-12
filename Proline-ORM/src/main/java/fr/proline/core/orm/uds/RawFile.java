package fr.proline.core.orm.uds;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

/**
 * The persistent class for the raw_file database table.
 * 
 */
@Entity
@Table(name = "raw_file")
public class RawFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "name")
    private String rawFileName;

    @Column(name = "creation_timestamp")
    private Timestamp creationTimestamp;

    private String directory;

    private String extension;

    @Column(name = "instrument_id")
    private long instrumentId;

    @Column(name = "owner_id")
    private long ownerId;

    @Column(name = "serialized_properties")
    private String serializedProperties;

    // bi-directional many-to-one association to Run
    @OneToMany(mappedBy = "rawFile")
    @OrderBy("number")
    private List<Run> runs;

    public RawFile() {
    }

    public String getRawFileName() {
	return this.rawFileName;
    }

    public void setRawFileName(String rawFileName) {
	this.rawFileName = rawFileName;
    }

    public Timestamp getCreationTimestamp() {
	Timestamp result = null;

	if (creationTimestamp != null) {
	    result = (Timestamp) creationTimestamp.clone();
	}

	return result;
    }

    public void setCreationTimestamp(final Timestamp pCreationTimestamp) {

	if (pCreationTimestamp == null) {
	    creationTimestamp = null;
	} else {
	    creationTimestamp = (Timestamp) pCreationTimestamp.clone();
	}

    }

    public String getDirectory() {
	return this.directory;
    }

    public void setDirectory(String directory) {
	this.directory = directory;
    }

    public String getExtension() {
	return this.extension;
    }

    public void setExtension(String extension) {
	this.extension = extension;
    }

    public long getInstrumentId() {
	return instrumentId;
    }

    public void setInstrumentId(final long pInstrumentId) {
	instrumentId = pInstrumentId;
    }

    public long getOwnerId() {
	return ownerId;
    }

    public void setOwnerId(final long pOwnerId) {
	ownerId = pOwnerId;
    }

    public String getSerializedProperties() {
	return this.serializedProperties;
    }

    public void setSerializedProperties(String serializedProperties) {
	this.serializedProperties = serializedProperties;
    }

    public List<Run> getRuns() {
	return runs;
    }

    public void setRuns(final List<Run> runs) {
	this.runs = runs;
    }

}
