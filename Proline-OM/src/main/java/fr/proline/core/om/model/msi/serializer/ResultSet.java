/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package fr.proline.core.om.model.msi.serializer;  
@SuppressWarnings("all")
public class ResultSet extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ResultSet\",\"namespace\":\"fr.proline.core.om.model.msi.serializer\",\"fields\":[{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"description\",\"type\":\"string\"},{\"name\":\"is_decoy\",\"type\":\"boolean\"},{\"name\":\"is_native\",\"type\":\"boolean\"},{\"name\":\"modification_timestamp\",\"type\":\"string\"},{\"name\":\"msi_search\",\"type\":{\"type\":\"record\",\"name\":\"MSISearch\",\"fields\":[{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"result_file_name\",\"type\":\"string\"},{\"name\":\"submitted_queries_count\",\"type\":\"int\"},{\"name\":\"search_settings\",\"type\":{\"type\":\"record\",\"name\":\"SearchSettings\",\"fields\":[{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"software_name\",\"type\":\"string\"},{\"name\":\"software_version\",\"type\":\"string\"},{\"name\":\"taxonomy\",\"type\":\"string\"},{\"name\":\"max_missed_cleavages\",\"type\":\"int\"},{\"name\":\"ms1_charge_states\",\"type\":\"string\"},{\"name\":\"ms1_mass_error_tolerance\",\"type\":\"double\"},{\"name\":\"ms1_mass_error_tolerance_unit\",\"type\":\"string\"},{\"name\":\"is_decoy\",\"type\":\"boolean\"},{\"name\":\"used_enzymes\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"variable_ptm_defs\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"fixed_ptm_defs\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"seq_databases\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"instrument_config\",\"type\":{\"type\":\"record\",\"name\":\"InstrumentConfig\",\"fields\":[{\"name\":\"id\",\"type\":\"int\",\"default\":\"\"},{\"name\":\"name\",\"type\":\"string\",\"default\":\"\"},{\"name\":\"instrument\",\"type\":{\"type\":\"record\",\"name\":\"Instrument\",\"fields\":[{\"name\":\"id\",\"type\":\"int\",\"default\":\"\"},{\"name\":\"name\",\"type\":\"string\",\"default\":\"\"},{\"name\":\"source\",\"type\":[\"string\",\"null\"]}]}},{\"name\":\"ms1_analyzer\",\"type\":\"string\",\"default\":\"\"},{\"name\":\"msn_analyzer\",\"type\":\"string\",\"default\":\"\"},{\"name\":\"activation_type\",\"type\":\"string\",\"default\":\"\"}]}},{\"name\":\"quantitation\",\"type\":\"string\"}]}}]}},{\"name\":\"peptide_matches\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"protein_matches\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"decoy_result_set_id\",\"type\":\"int\"},{\"name\":\"children_ids\",\"type\":{\"type\":\"array\",\"items\":\"int\"}}]}");
  @Deprecated public int id;
  @Deprecated public java.lang.CharSequence name;
  @Deprecated public java.lang.CharSequence description;
  @Deprecated public boolean is_decoy;
  @Deprecated public boolean is_native;
  @Deprecated public java.lang.CharSequence modification_timestamp;
  @Deprecated public fr.proline.core.om.model.msi.serializer.MSISearch msi_search;
  @Deprecated public java.util.List<java.lang.CharSequence> peptide_matches;
  @Deprecated public java.util.List<java.lang.CharSequence> protein_matches;
  @Deprecated public int decoy_result_set_id;
  @Deprecated public java.util.List<java.lang.Integer> children_ids;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return id;
    case 1: return name;
    case 2: return description;
    case 3: return is_decoy;
    case 4: return is_native;
    case 5: return modification_timestamp;
    case 6: return msi_search;
    case 7: return peptide_matches;
    case 8: return protein_matches;
    case 9: return decoy_result_set_id;
    case 10: return children_ids;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: id = (java.lang.Integer)value$; break;
    case 1: name = (java.lang.CharSequence)value$; break;
    case 2: description = (java.lang.CharSequence)value$; break;
    case 3: is_decoy = (java.lang.Boolean)value$; break;
    case 4: is_native = (java.lang.Boolean)value$; break;
    case 5: modification_timestamp = (java.lang.CharSequence)value$; break;
    case 6: msi_search = (fr.proline.core.om.model.msi.serializer.MSISearch)value$; break;
    case 7: peptide_matches = (java.util.List<java.lang.CharSequence>)value$; break;
    case 8: protein_matches = (java.util.List<java.lang.CharSequence>)value$; break;
    case 9: decoy_result_set_id = (java.lang.Integer)value$; break;
    case 10: children_ids = (java.util.List<java.lang.Integer>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'id' field.
   */
  public java.lang.Integer getId() {
    return id;
  }

  /**
   * Sets the value of the 'id' field.
   * @param value the value to set.
   */
  public void setId(java.lang.Integer value) {
    this.id = value;
  }

  /**
   * Gets the value of the 'name' field.
   */
  public java.lang.CharSequence getName() {
    return name;
  }

  /**
   * Sets the value of the 'name' field.
   * @param value the value to set.
   */
  public void setName(java.lang.CharSequence value) {
    this.name = value;
  }

  /**
   * Gets the value of the 'description' field.
   */
  public java.lang.CharSequence getDescription() {
    return description;
  }

  /**
   * Sets the value of the 'description' field.
   * @param value the value to set.
   */
  public void setDescription(java.lang.CharSequence value) {
    this.description = value;
  }

  /**
   * Gets the value of the 'is_decoy' field.
   */
  public java.lang.Boolean getIsDecoy() {
    return is_decoy;
  }

  /**
   * Sets the value of the 'is_decoy' field.
   * @param value the value to set.
   */
  public void setIsDecoy(java.lang.Boolean value) {
    this.is_decoy = value;
  }

  /**
   * Gets the value of the 'is_native' field.
   */
  public java.lang.Boolean getIsNative() {
    return is_native;
  }

  /**
   * Sets the value of the 'is_native' field.
   * @param value the value to set.
   */
  public void setIsNative(java.lang.Boolean value) {
    this.is_native = value;
  }

  /**
   * Gets the value of the 'modification_timestamp' field.
   */
  public java.lang.CharSequence getModificationTimestamp() {
    return modification_timestamp;
  }

  /**
   * Sets the value of the 'modification_timestamp' field.
   * @param value the value to set.
   */
  public void setModificationTimestamp(java.lang.CharSequence value) {
    this.modification_timestamp = value;
  }

  /**
   * Gets the value of the 'msi_search' field.
   */
  public fr.proline.core.om.model.msi.serializer.MSISearch getMsiSearch() {
    return msi_search;
  }

  /**
   * Sets the value of the 'msi_search' field.
   * @param value the value to set.
   */
  public void setMsiSearch(fr.proline.core.om.model.msi.serializer.MSISearch value) {
    this.msi_search = value;
  }

  /**
   * Gets the value of the 'peptide_matches' field.
   */
  public java.util.List<java.lang.CharSequence> getPeptideMatches() {
    return peptide_matches;
  }

  /**
   * Sets the value of the 'peptide_matches' field.
   * @param value the value to set.
   */
  public void setPeptideMatches(java.util.List<java.lang.CharSequence> value) {
    this.peptide_matches = value;
  }

  /**
   * Gets the value of the 'protein_matches' field.
   */
  public java.util.List<java.lang.CharSequence> getProteinMatches() {
    return protein_matches;
  }

  /**
   * Sets the value of the 'protein_matches' field.
   * @param value the value to set.
   */
  public void setProteinMatches(java.util.List<java.lang.CharSequence> value) {
    this.protein_matches = value;
  }

  /**
   * Gets the value of the 'decoy_result_set_id' field.
   */
  public java.lang.Integer getDecoyResultSetId() {
    return decoy_result_set_id;
  }

  /**
   * Sets the value of the 'decoy_result_set_id' field.
   * @param value the value to set.
   */
  public void setDecoyResultSetId(java.lang.Integer value) {
    this.decoy_result_set_id = value;
  }

  /**
   * Gets the value of the 'children_ids' field.
   */
  public java.util.List<java.lang.Integer> getChildrenIds() {
    return children_ids;
  }

  /**
   * Sets the value of the 'children_ids' field.
   * @param value the value to set.
   */
  public void setChildrenIds(java.util.List<java.lang.Integer> value) {
    this.children_ids = value;
  }

  /** Creates a new ResultSet RecordBuilder */
  public static fr.proline.core.om.model.msi.serializer.ResultSet.Builder newBuilder() {
    return new fr.proline.core.om.model.msi.serializer.ResultSet.Builder();
  }
  
  /** Creates a new ResultSet RecordBuilder by copying an existing Builder */
  public static fr.proline.core.om.model.msi.serializer.ResultSet.Builder newBuilder(fr.proline.core.om.model.msi.serializer.ResultSet.Builder other) {
    return new fr.proline.core.om.model.msi.serializer.ResultSet.Builder(other);
  }
  
  /** Creates a new ResultSet RecordBuilder by copying an existing ResultSet instance */
  public static fr.proline.core.om.model.msi.serializer.ResultSet.Builder newBuilder(fr.proline.core.om.model.msi.serializer.ResultSet other) {
    return new fr.proline.core.om.model.msi.serializer.ResultSet.Builder(other);
  }
  
  /**
   * RecordBuilder for ResultSet instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<ResultSet>
    implements org.apache.avro.data.RecordBuilder<ResultSet> {

    private int id;
    private java.lang.CharSequence name;
    private java.lang.CharSequence description;
    private boolean is_decoy;
    private boolean is_native;
    private java.lang.CharSequence modification_timestamp;
    private fr.proline.core.om.model.msi.serializer.MSISearch msi_search;
    private java.util.List<java.lang.CharSequence> peptide_matches;
    private java.util.List<java.lang.CharSequence> protein_matches;
    private int decoy_result_set_id;
    private java.util.List<java.lang.Integer> children_ids;

    /** Creates a new Builder */
    private Builder() {
      super(fr.proline.core.om.model.msi.serializer.ResultSet.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(fr.proline.core.om.model.msi.serializer.ResultSet.Builder other) {
      super(other);
    }
    
    /** Creates a Builder by copying an existing ResultSet instance */
    private Builder(fr.proline.core.om.model.msi.serializer.ResultSet other) {
            super(fr.proline.core.om.model.msi.serializer.ResultSet.SCHEMA$);
      if (isValidValue(fields()[0], other.id)) {
        this.id = (java.lang.Integer) data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.name)) {
        this.name = (java.lang.CharSequence) data().deepCopy(fields()[1].schema(), other.name);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.description)) {
        this.description = (java.lang.CharSequence) data().deepCopy(fields()[2].schema(), other.description);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.is_decoy)) {
        this.is_decoy = (java.lang.Boolean) data().deepCopy(fields()[3].schema(), other.is_decoy);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.is_native)) {
        this.is_native = (java.lang.Boolean) data().deepCopy(fields()[4].schema(), other.is_native);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.modification_timestamp)) {
        this.modification_timestamp = (java.lang.CharSequence) data().deepCopy(fields()[5].schema(), other.modification_timestamp);
        fieldSetFlags()[5] = true;
      }
      if (isValidValue(fields()[6], other.msi_search)) {
        this.msi_search = (fr.proline.core.om.model.msi.serializer.MSISearch) data().deepCopy(fields()[6].schema(), other.msi_search);
        fieldSetFlags()[6] = true;
      }
      if (isValidValue(fields()[7], other.peptide_matches)) {
        this.peptide_matches = (java.util.List<java.lang.CharSequence>) data().deepCopy(fields()[7].schema(), other.peptide_matches);
        fieldSetFlags()[7] = true;
      }
      if (isValidValue(fields()[8], other.protein_matches)) {
        this.protein_matches = (java.util.List<java.lang.CharSequence>) data().deepCopy(fields()[8].schema(), other.protein_matches);
        fieldSetFlags()[8] = true;
      }
      if (isValidValue(fields()[9], other.decoy_result_set_id)) {
        this.decoy_result_set_id = (java.lang.Integer) data().deepCopy(fields()[9].schema(), other.decoy_result_set_id);
        fieldSetFlags()[9] = true;
      }
      if (isValidValue(fields()[10], other.children_ids)) {
        this.children_ids = (java.util.List<java.lang.Integer>) data().deepCopy(fields()[10].schema(), other.children_ids);
        fieldSetFlags()[10] = true;
      }
    }

    /** Gets the value of the 'id' field */
    public java.lang.Integer getId() {
      return id;
    }
    
    /** Sets the value of the 'id' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder setId(int value) {
      validate(fields()[0], value);
      this.id = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'id' field has been set */
    public boolean hasId() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'id' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder clearId() {
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'name' field */
    public java.lang.CharSequence getName() {
      return name;
    }
    
    /** Sets the value of the 'name' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder setName(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.name = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'name' field has been set */
    public boolean hasName() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'name' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder clearName() {
      name = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'description' field */
    public java.lang.CharSequence getDescription() {
      return description;
    }
    
    /** Sets the value of the 'description' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder setDescription(java.lang.CharSequence value) {
      validate(fields()[2], value);
      this.description = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'description' field has been set */
    public boolean hasDescription() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'description' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder clearDescription() {
      description = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'is_decoy' field */
    public java.lang.Boolean getIsDecoy() {
      return is_decoy;
    }
    
    /** Sets the value of the 'is_decoy' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder setIsDecoy(boolean value) {
      validate(fields()[3], value);
      this.is_decoy = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'is_decoy' field has been set */
    public boolean hasIsDecoy() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'is_decoy' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder clearIsDecoy() {
      fieldSetFlags()[3] = false;
      return this;
    }

    /** Gets the value of the 'is_native' field */
    public java.lang.Boolean getIsNative() {
      return is_native;
    }
    
    /** Sets the value of the 'is_native' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder setIsNative(boolean value) {
      validate(fields()[4], value);
      this.is_native = value;
      fieldSetFlags()[4] = true;
      return this; 
    }
    
    /** Checks whether the 'is_native' field has been set */
    public boolean hasIsNative() {
      return fieldSetFlags()[4];
    }
    
    /** Clears the value of the 'is_native' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder clearIsNative() {
      fieldSetFlags()[4] = false;
      return this;
    }

    /** Gets the value of the 'modification_timestamp' field */
    public java.lang.CharSequence getModificationTimestamp() {
      return modification_timestamp;
    }
    
    /** Sets the value of the 'modification_timestamp' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder setModificationTimestamp(java.lang.CharSequence value) {
      validate(fields()[5], value);
      this.modification_timestamp = value;
      fieldSetFlags()[5] = true;
      return this; 
    }
    
    /** Checks whether the 'modification_timestamp' field has been set */
    public boolean hasModificationTimestamp() {
      return fieldSetFlags()[5];
    }
    
    /** Clears the value of the 'modification_timestamp' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder clearModificationTimestamp() {
      modification_timestamp = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    /** Gets the value of the 'msi_search' field */
    public fr.proline.core.om.model.msi.serializer.MSISearch getMsiSearch() {
      return msi_search;
    }
    
    /** Sets the value of the 'msi_search' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder setMsiSearch(fr.proline.core.om.model.msi.serializer.MSISearch value) {
      validate(fields()[6], value);
      this.msi_search = value;
      fieldSetFlags()[6] = true;
      return this; 
    }
    
    /** Checks whether the 'msi_search' field has been set */
    public boolean hasMsiSearch() {
      return fieldSetFlags()[6];
    }
    
    /** Clears the value of the 'msi_search' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder clearMsiSearch() {
      msi_search = null;
      fieldSetFlags()[6] = false;
      return this;
    }

    /** Gets the value of the 'peptide_matches' field */
    public java.util.List<java.lang.CharSequence> getPeptideMatches() {
      return peptide_matches;
    }
    
    /** Sets the value of the 'peptide_matches' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder setPeptideMatches(java.util.List<java.lang.CharSequence> value) {
      validate(fields()[7], value);
      this.peptide_matches = value;
      fieldSetFlags()[7] = true;
      return this; 
    }
    
    /** Checks whether the 'peptide_matches' field has been set */
    public boolean hasPeptideMatches() {
      return fieldSetFlags()[7];
    }
    
    /** Clears the value of the 'peptide_matches' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder clearPeptideMatches() {
      peptide_matches = null;
      fieldSetFlags()[7] = false;
      return this;
    }

    /** Gets the value of the 'protein_matches' field */
    public java.util.List<java.lang.CharSequence> getProteinMatches() {
      return protein_matches;
    }
    
    /** Sets the value of the 'protein_matches' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder setProteinMatches(java.util.List<java.lang.CharSequence> value) {
      validate(fields()[8], value);
      this.protein_matches = value;
      fieldSetFlags()[8] = true;
      return this; 
    }
    
    /** Checks whether the 'protein_matches' field has been set */
    public boolean hasProteinMatches() {
      return fieldSetFlags()[8];
    }
    
    /** Clears the value of the 'protein_matches' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder clearProteinMatches() {
      protein_matches = null;
      fieldSetFlags()[8] = false;
      return this;
    }

    /** Gets the value of the 'decoy_result_set_id' field */
    public java.lang.Integer getDecoyResultSetId() {
      return decoy_result_set_id;
    }
    
    /** Sets the value of the 'decoy_result_set_id' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder setDecoyResultSetId(int value) {
      validate(fields()[9], value);
      this.decoy_result_set_id = value;
      fieldSetFlags()[9] = true;
      return this; 
    }
    
    /** Checks whether the 'decoy_result_set_id' field has been set */
    public boolean hasDecoyResultSetId() {
      return fieldSetFlags()[9];
    }
    
    /** Clears the value of the 'decoy_result_set_id' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder clearDecoyResultSetId() {
      fieldSetFlags()[9] = false;
      return this;
    }

    /** Gets the value of the 'children_ids' field */
    public java.util.List<java.lang.Integer> getChildrenIds() {
      return children_ids;
    }
    
    /** Sets the value of the 'children_ids' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder setChildrenIds(java.util.List<java.lang.Integer> value) {
      validate(fields()[10], value);
      this.children_ids = value;
      fieldSetFlags()[10] = true;
      return this; 
    }
    
    /** Checks whether the 'children_ids' field has been set */
    public boolean hasChildrenIds() {
      return fieldSetFlags()[10];
    }
    
    /** Clears the value of the 'children_ids' field */
    public fr.proline.core.om.model.msi.serializer.ResultSet.Builder clearChildrenIds() {
      children_ids = null;
      fieldSetFlags()[10] = false;
      return this;
    }

    @Override
    public ResultSet build() {
      try {
        ResultSet record = new ResultSet();
        record.id = fieldSetFlags()[0] ? this.id : (java.lang.Integer) defaultValue(fields()[0]);
        record.name = fieldSetFlags()[1] ? this.name : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.description = fieldSetFlags()[2] ? this.description : (java.lang.CharSequence) defaultValue(fields()[2]);
        record.is_decoy = fieldSetFlags()[3] ? this.is_decoy : (java.lang.Boolean) defaultValue(fields()[3]);
        record.is_native = fieldSetFlags()[4] ? this.is_native : (java.lang.Boolean) defaultValue(fields()[4]);
        record.modification_timestamp = fieldSetFlags()[5] ? this.modification_timestamp : (java.lang.CharSequence) defaultValue(fields()[5]);
        record.msi_search = fieldSetFlags()[6] ? this.msi_search : (fr.proline.core.om.model.msi.serializer.MSISearch) defaultValue(fields()[6]);
        record.peptide_matches = fieldSetFlags()[7] ? this.peptide_matches : (java.util.List<java.lang.CharSequence>) defaultValue(fields()[7]);
        record.protein_matches = fieldSetFlags()[8] ? this.protein_matches : (java.util.List<java.lang.CharSequence>) defaultValue(fields()[8]);
        record.decoy_result_set_id = fieldSetFlags()[9] ? this.decoy_result_set_id : (java.lang.Integer) defaultValue(fields()[9]);
        record.children_ids = fieldSetFlags()[10] ? this.children_ids : (java.util.List<java.lang.Integer>) defaultValue(fields()[10]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
