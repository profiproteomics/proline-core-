/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package fr.proline.core.om.model.msi.serializer;  
@SuppressWarnings("all")
public class PeptideMatch extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"PeptideMatch\",\"namespace\":\"fr.proline.core.om.model.msi.serializer\",\"fields\":[{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"rank\",\"type\":\"int\"},{\"name\":\"score\",\"type\":\"float\"},{\"name\":\"score_type\",\"type\":\"string\"},{\"name\":\"delta_moz\",\"type\":\"double\"},{\"name\":\"is_decoy\",\"type\":\"boolean\"},{\"name\":\"missed_cleavage\",\"type\":\"int\"},{\"name\":\"fragment_matches_count\",\"type\":\"int\"},{\"name\":\"properties\",\"type\":{\"type\":\"record\",\"name\":\"PeptideMatchProperties\",\"fields\":[{\"name\":\"mascot_properties\",\"type\":{\"type\":\"record\",\"name\":\"PeptideMatchMascotProperties\",\"fields\":[{\"name\":\"expectation_value\",\"type\":\"float\"},{\"name\":\"readable_var_mods\",\"type\":[\"string\",\"null\"]},{\"name\":\"var_mods_positions\",\"type\":[\"string\",\"null\"]}]}}]}},{\"name\":\"peptide\",\"type\":{\"type\":\"record\",\"name\":\"Peptide\",\"fields\":[{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"sequence\",\"type\":\"string\"},{\"name\":\"ptm_string\",\"type\":\"string\"},{\"name\":\"calculated_mass\",\"type\":\"double\"},{\"name\":\"ptms\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}},{\"name\":\"ms_query\",\"type\":{\"type\":\"record\",\"name\":\"MsQuery\",\"fields\":[{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"initial_id\",\"type\":\"int\"},{\"name\":\"moz\",\"type\":\"double\"},{\"name\":\"charge\",\"type\":\"int\"},{\"name\":\"ms_level\",\"type\":\"int\"},{\"name\":\"spectrum_title\",\"type\":\"string\"},{\"name\":\"spectrum_id\",\"type\":\"int\"},{\"name\":\"properties\",\"type\":{\"type\":\"record\",\"name\":\"MsQueryProperties\",\"fields\":[{\"name\":\"target_db_search\",\"type\":{\"type\":\"record\",\"name\":\"MsQueryDbSearchProperties\",\"fields\":[{\"name\":\"candidate_peptides_count\",\"type\":\"int\",\"default\":\"\"},{\"name\":\"mascot_identity_threshold\",\"type\":[\"float\",\"null\"]},{\"name\":\"mascot_homology_threshold\",\"type\":[\"float\",\"null\"]}]}},{\"name\":\"decoy_db_search\",\"type\":\"MsQueryDbSearchProperties\"}]}}]}},{\"name\":\"children_ids\",\"type\":{\"type\":\"array\",\"items\":\"int\"}},{\"name\":\"best_child_id\",\"type\":\"int\",\"default\":\"\"},{\"name\":\"result_set_id\",\"type\":\"int\"}]}");
  @Deprecated public int id;
  @Deprecated public int rank;
  @Deprecated public float score;
  @Deprecated public java.lang.CharSequence score_type;
  @Deprecated public double delta_moz;
  @Deprecated public boolean is_decoy;
  @Deprecated public int missed_cleavage;
  @Deprecated public int fragment_matches_count;
  @Deprecated public fr.proline.core.om.model.msi.serializer.PeptideMatchProperties properties;
  @Deprecated public fr.proline.core.om.model.msi.serializer.Peptide peptide;
  @Deprecated public fr.proline.core.om.model.msi.serializer.MsQuery ms_query;
  @Deprecated public java.util.List<java.lang.Integer> children_ids;
  @Deprecated public int best_child_id;
  @Deprecated public int result_set_id;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return id;
    case 1: return rank;
    case 2: return score;
    case 3: return score_type;
    case 4: return delta_moz;
    case 5: return is_decoy;
    case 6: return missed_cleavage;
    case 7: return fragment_matches_count;
    case 8: return properties;
    case 9: return peptide;
    case 10: return ms_query;
    case 11: return children_ids;
    case 12: return best_child_id;
    case 13: return result_set_id;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: id = (java.lang.Integer)value$; break;
    case 1: rank = (java.lang.Integer)value$; break;
    case 2: score = (java.lang.Float)value$; break;
    case 3: score_type = (java.lang.CharSequence)value$; break;
    case 4: delta_moz = (java.lang.Double)value$; break;
    case 5: is_decoy = (java.lang.Boolean)value$; break;
    case 6: missed_cleavage = (java.lang.Integer)value$; break;
    case 7: fragment_matches_count = (java.lang.Integer)value$; break;
    case 8: properties = (fr.proline.core.om.model.msi.serializer.PeptideMatchProperties)value$; break;
    case 9: peptide = (fr.proline.core.om.model.msi.serializer.Peptide)value$; break;
    case 10: ms_query = (fr.proline.core.om.model.msi.serializer.MsQuery)value$; break;
    case 11: children_ids = (java.util.List<java.lang.Integer>)value$; break;
    case 12: best_child_id = (java.lang.Integer)value$; break;
    case 13: result_set_id = (java.lang.Integer)value$; break;
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
   * Gets the value of the 'rank' field.
   */
  public java.lang.Integer getRank() {
    return rank;
  }

  /**
   * Sets the value of the 'rank' field.
   * @param value the value to set.
   */
  public void setRank(java.lang.Integer value) {
    this.rank = value;
  }

  /**
   * Gets the value of the 'score' field.
   */
  public java.lang.Float getScore() {
    return score;
  }

  /**
   * Sets the value of the 'score' field.
   * @param value the value to set.
   */
  public void setScore(java.lang.Float value) {
    this.score = value;
  }

  /**
   * Gets the value of the 'score_type' field.
   */
  public java.lang.CharSequence getScoreType() {
    return score_type;
  }

  /**
   * Sets the value of the 'score_type' field.
   * @param value the value to set.
   */
  public void setScoreType(java.lang.CharSequence value) {
    this.score_type = value;
  }

  /**
   * Gets the value of the 'delta_moz' field.
   */
  public java.lang.Double getDeltaMoz() {
    return delta_moz;
  }

  /**
   * Sets the value of the 'delta_moz' field.
   * @param value the value to set.
   */
  public void setDeltaMoz(java.lang.Double value) {
    this.delta_moz = value;
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
   * Gets the value of the 'missed_cleavage' field.
   */
  public java.lang.Integer getMissedCleavage() {
    return missed_cleavage;
  }

  /**
   * Sets the value of the 'missed_cleavage' field.
   * @param value the value to set.
   */
  public void setMissedCleavage(java.lang.Integer value) {
    this.missed_cleavage = value;
  }

  /**
   * Gets the value of the 'fragment_matches_count' field.
   */
  public java.lang.Integer getFragmentMatchesCount() {
    return fragment_matches_count;
  }

  /**
   * Sets the value of the 'fragment_matches_count' field.
   * @param value the value to set.
   */
  public void setFragmentMatchesCount(java.lang.Integer value) {
    this.fragment_matches_count = value;
  }

  /**
   * Gets the value of the 'properties' field.
   */
  public fr.proline.core.om.model.msi.serializer.PeptideMatchProperties getProperties() {
    return properties;
  }

  /**
   * Sets the value of the 'properties' field.
   * @param value the value to set.
   */
  public void setProperties(fr.proline.core.om.model.msi.serializer.PeptideMatchProperties value) {
    this.properties = value;
  }

  /**
   * Gets the value of the 'peptide' field.
   */
  public fr.proline.core.om.model.msi.serializer.Peptide getPeptide() {
    return peptide;
  }

  /**
   * Sets the value of the 'peptide' field.
   * @param value the value to set.
   */
  public void setPeptide(fr.proline.core.om.model.msi.serializer.Peptide value) {
    this.peptide = value;
  }

  /**
   * Gets the value of the 'ms_query' field.
   */
  public fr.proline.core.om.model.msi.serializer.MsQuery getMsQuery() {
    return ms_query;
  }

  /**
   * Sets the value of the 'ms_query' field.
   * @param value the value to set.
   */
  public void setMsQuery(fr.proline.core.om.model.msi.serializer.MsQuery value) {
    this.ms_query = value;
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

  /**
   * Gets the value of the 'best_child_id' field.
   */
  public java.lang.Integer getBestChildId() {
    return best_child_id;
  }

  /**
   * Sets the value of the 'best_child_id' field.
   * @param value the value to set.
   */
  public void setBestChildId(java.lang.Integer value) {
    this.best_child_id = value;
  }

  /**
   * Gets the value of the 'result_set_id' field.
   */
  public java.lang.Integer getResultSetId() {
    return result_set_id;
  }

  /**
   * Sets the value of the 'result_set_id' field.
   * @param value the value to set.
   */
  public void setResultSetId(java.lang.Integer value) {
    this.result_set_id = value;
  }

  /** Creates a new PeptideMatch RecordBuilder */
  public static fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder newBuilder() {
    return new fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder();
  }
  
  /** Creates a new PeptideMatch RecordBuilder by copying an existing Builder */
  public static fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder newBuilder(fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder other) {
    return new fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder(other);
  }
  
  /** Creates a new PeptideMatch RecordBuilder by copying an existing PeptideMatch instance */
  public static fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder newBuilder(fr.proline.core.om.model.msi.serializer.PeptideMatch other) {
    return new fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder(other);
  }
  
  /**
   * RecordBuilder for PeptideMatch instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<PeptideMatch>
    implements org.apache.avro.data.RecordBuilder<PeptideMatch> {

    private int id;
    private int rank;
    private float score;
    private java.lang.CharSequence score_type;
    private double delta_moz;
    private boolean is_decoy;
    private int missed_cleavage;
    private int fragment_matches_count;
    private fr.proline.core.om.model.msi.serializer.PeptideMatchProperties properties;
    private fr.proline.core.om.model.msi.serializer.Peptide peptide;
    private fr.proline.core.om.model.msi.serializer.MsQuery ms_query;
    private java.util.List<java.lang.Integer> children_ids;
    private int best_child_id;
    private int result_set_id;

    /** Creates a new Builder */
    private Builder() {
      super(fr.proline.core.om.model.msi.serializer.PeptideMatch.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder other) {
      super(other);
    }
    
    /** Creates a Builder by copying an existing PeptideMatch instance */
    private Builder(fr.proline.core.om.model.msi.serializer.PeptideMatch other) {
            super(fr.proline.core.om.model.msi.serializer.PeptideMatch.SCHEMA$);
      if (isValidValue(fields()[0], other.id)) {
        this.id = (java.lang.Integer) data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.rank)) {
        this.rank = (java.lang.Integer) data().deepCopy(fields()[1].schema(), other.rank);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.score)) {
        this.score = (java.lang.Float) data().deepCopy(fields()[2].schema(), other.score);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.score_type)) {
        this.score_type = (java.lang.CharSequence) data().deepCopy(fields()[3].schema(), other.score_type);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.delta_moz)) {
        this.delta_moz = (java.lang.Double) data().deepCopy(fields()[4].schema(), other.delta_moz);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.is_decoy)) {
        this.is_decoy = (java.lang.Boolean) data().deepCopy(fields()[5].schema(), other.is_decoy);
        fieldSetFlags()[5] = true;
      }
      if (isValidValue(fields()[6], other.missed_cleavage)) {
        this.missed_cleavage = (java.lang.Integer) data().deepCopy(fields()[6].schema(), other.missed_cleavage);
        fieldSetFlags()[6] = true;
      }
      if (isValidValue(fields()[7], other.fragment_matches_count)) {
        this.fragment_matches_count = (java.lang.Integer) data().deepCopy(fields()[7].schema(), other.fragment_matches_count);
        fieldSetFlags()[7] = true;
      }
      if (isValidValue(fields()[8], other.properties)) {
        this.properties = (fr.proline.core.om.model.msi.serializer.PeptideMatchProperties) data().deepCopy(fields()[8].schema(), other.properties);
        fieldSetFlags()[8] = true;
      }
      if (isValidValue(fields()[9], other.peptide)) {
        this.peptide = (fr.proline.core.om.model.msi.serializer.Peptide) data().deepCopy(fields()[9].schema(), other.peptide);
        fieldSetFlags()[9] = true;
      }
      if (isValidValue(fields()[10], other.ms_query)) {
        this.ms_query = (fr.proline.core.om.model.msi.serializer.MsQuery) data().deepCopy(fields()[10].schema(), other.ms_query);
        fieldSetFlags()[10] = true;
      }
      if (isValidValue(fields()[11], other.children_ids)) {
        this.children_ids = (java.util.List<java.lang.Integer>) data().deepCopy(fields()[11].schema(), other.children_ids);
        fieldSetFlags()[11] = true;
      }
      if (isValidValue(fields()[12], other.best_child_id)) {
        this.best_child_id = (java.lang.Integer) data().deepCopy(fields()[12].schema(), other.best_child_id);
        fieldSetFlags()[12] = true;
      }
      if (isValidValue(fields()[13], other.result_set_id)) {
        this.result_set_id = (java.lang.Integer) data().deepCopy(fields()[13].schema(), other.result_set_id);
        fieldSetFlags()[13] = true;
      }
    }

    /** Gets the value of the 'id' field */
    public java.lang.Integer getId() {
      return id;
    }
    
    /** Sets the value of the 'id' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder setId(int value) {
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
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder clearId() {
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'rank' field */
    public java.lang.Integer getRank() {
      return rank;
    }
    
    /** Sets the value of the 'rank' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder setRank(int value) {
      validate(fields()[1], value);
      this.rank = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'rank' field has been set */
    public boolean hasRank() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'rank' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder clearRank() {
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'score' field */
    public java.lang.Float getScore() {
      return score;
    }
    
    /** Sets the value of the 'score' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder setScore(float value) {
      validate(fields()[2], value);
      this.score = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'score' field has been set */
    public boolean hasScore() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'score' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder clearScore() {
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'score_type' field */
    public java.lang.CharSequence getScoreType() {
      return score_type;
    }
    
    /** Sets the value of the 'score_type' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder setScoreType(java.lang.CharSequence value) {
      validate(fields()[3], value);
      this.score_type = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'score_type' field has been set */
    public boolean hasScoreType() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'score_type' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder clearScoreType() {
      score_type = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /** Gets the value of the 'delta_moz' field */
    public java.lang.Double getDeltaMoz() {
      return delta_moz;
    }
    
    /** Sets the value of the 'delta_moz' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder setDeltaMoz(double value) {
      validate(fields()[4], value);
      this.delta_moz = value;
      fieldSetFlags()[4] = true;
      return this; 
    }
    
    /** Checks whether the 'delta_moz' field has been set */
    public boolean hasDeltaMoz() {
      return fieldSetFlags()[4];
    }
    
    /** Clears the value of the 'delta_moz' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder clearDeltaMoz() {
      fieldSetFlags()[4] = false;
      return this;
    }

    /** Gets the value of the 'is_decoy' field */
    public java.lang.Boolean getIsDecoy() {
      return is_decoy;
    }
    
    /** Sets the value of the 'is_decoy' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder setIsDecoy(boolean value) {
      validate(fields()[5], value);
      this.is_decoy = value;
      fieldSetFlags()[5] = true;
      return this; 
    }
    
    /** Checks whether the 'is_decoy' field has been set */
    public boolean hasIsDecoy() {
      return fieldSetFlags()[5];
    }
    
    /** Clears the value of the 'is_decoy' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder clearIsDecoy() {
      fieldSetFlags()[5] = false;
      return this;
    }

    /** Gets the value of the 'missed_cleavage' field */
    public java.lang.Integer getMissedCleavage() {
      return missed_cleavage;
    }
    
    /** Sets the value of the 'missed_cleavage' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder setMissedCleavage(int value) {
      validate(fields()[6], value);
      this.missed_cleavage = value;
      fieldSetFlags()[6] = true;
      return this; 
    }
    
    /** Checks whether the 'missed_cleavage' field has been set */
    public boolean hasMissedCleavage() {
      return fieldSetFlags()[6];
    }
    
    /** Clears the value of the 'missed_cleavage' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder clearMissedCleavage() {
      fieldSetFlags()[6] = false;
      return this;
    }

    /** Gets the value of the 'fragment_matches_count' field */
    public java.lang.Integer getFragmentMatchesCount() {
      return fragment_matches_count;
    }
    
    /** Sets the value of the 'fragment_matches_count' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder setFragmentMatchesCount(int value) {
      validate(fields()[7], value);
      this.fragment_matches_count = value;
      fieldSetFlags()[7] = true;
      return this; 
    }
    
    /** Checks whether the 'fragment_matches_count' field has been set */
    public boolean hasFragmentMatchesCount() {
      return fieldSetFlags()[7];
    }
    
    /** Clears the value of the 'fragment_matches_count' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder clearFragmentMatchesCount() {
      fieldSetFlags()[7] = false;
      return this;
    }

    /** Gets the value of the 'properties' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatchProperties getProperties() {
      return properties;
    }
    
    /** Sets the value of the 'properties' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder setProperties(fr.proline.core.om.model.msi.serializer.PeptideMatchProperties value) {
      validate(fields()[8], value);
      this.properties = value;
      fieldSetFlags()[8] = true;
      return this; 
    }
    
    /** Checks whether the 'properties' field has been set */
    public boolean hasProperties() {
      return fieldSetFlags()[8];
    }
    
    /** Clears the value of the 'properties' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder clearProperties() {
      properties = null;
      fieldSetFlags()[8] = false;
      return this;
    }

    /** Gets the value of the 'peptide' field */
    public fr.proline.core.om.model.msi.serializer.Peptide getPeptide() {
      return peptide;
    }
    
    /** Sets the value of the 'peptide' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder setPeptide(fr.proline.core.om.model.msi.serializer.Peptide value) {
      validate(fields()[9], value);
      this.peptide = value;
      fieldSetFlags()[9] = true;
      return this; 
    }
    
    /** Checks whether the 'peptide' field has been set */
    public boolean hasPeptide() {
      return fieldSetFlags()[9];
    }
    
    /** Clears the value of the 'peptide' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder clearPeptide() {
      peptide = null;
      fieldSetFlags()[9] = false;
      return this;
    }

    /** Gets the value of the 'ms_query' field */
    public fr.proline.core.om.model.msi.serializer.MsQuery getMsQuery() {
      return ms_query;
    }
    
    /** Sets the value of the 'ms_query' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder setMsQuery(fr.proline.core.om.model.msi.serializer.MsQuery value) {
      validate(fields()[10], value);
      this.ms_query = value;
      fieldSetFlags()[10] = true;
      return this; 
    }
    
    /** Checks whether the 'ms_query' field has been set */
    public boolean hasMsQuery() {
      return fieldSetFlags()[10];
    }
    
    /** Clears the value of the 'ms_query' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder clearMsQuery() {
      ms_query = null;
      fieldSetFlags()[10] = false;
      return this;
    }

    /** Gets the value of the 'children_ids' field */
    public java.util.List<java.lang.Integer> getChildrenIds() {
      return children_ids;
    }
    
    /** Sets the value of the 'children_ids' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder setChildrenIds(java.util.List<java.lang.Integer> value) {
      validate(fields()[11], value);
      this.children_ids = value;
      fieldSetFlags()[11] = true;
      return this; 
    }
    
    /** Checks whether the 'children_ids' field has been set */
    public boolean hasChildrenIds() {
      return fieldSetFlags()[11];
    }
    
    /** Clears the value of the 'children_ids' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder clearChildrenIds() {
      children_ids = null;
      fieldSetFlags()[11] = false;
      return this;
    }

    /** Gets the value of the 'best_child_id' field */
    public java.lang.Integer getBestChildId() {
      return best_child_id;
    }
    
    /** Sets the value of the 'best_child_id' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder setBestChildId(int value) {
      validate(fields()[12], value);
      this.best_child_id = value;
      fieldSetFlags()[12] = true;
      return this; 
    }
    
    /** Checks whether the 'best_child_id' field has been set */
    public boolean hasBestChildId() {
      return fieldSetFlags()[12];
    }
    
    /** Clears the value of the 'best_child_id' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder clearBestChildId() {
      fieldSetFlags()[12] = false;
      return this;
    }

    /** Gets the value of the 'result_set_id' field */
    public java.lang.Integer getResultSetId() {
      return result_set_id;
    }
    
    /** Sets the value of the 'result_set_id' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder setResultSetId(int value) {
      validate(fields()[13], value);
      this.result_set_id = value;
      fieldSetFlags()[13] = true;
      return this; 
    }
    
    /** Checks whether the 'result_set_id' field has been set */
    public boolean hasResultSetId() {
      return fieldSetFlags()[13];
    }
    
    /** Clears the value of the 'result_set_id' field */
    public fr.proline.core.om.model.msi.serializer.PeptideMatch.Builder clearResultSetId() {
      fieldSetFlags()[13] = false;
      return this;
    }

    @Override
    public PeptideMatch build() {
      try {
        PeptideMatch record = new PeptideMatch();
        record.id = fieldSetFlags()[0] ? this.id : (java.lang.Integer) defaultValue(fields()[0]);
        record.rank = fieldSetFlags()[1] ? this.rank : (java.lang.Integer) defaultValue(fields()[1]);
        record.score = fieldSetFlags()[2] ? this.score : (java.lang.Float) defaultValue(fields()[2]);
        record.score_type = fieldSetFlags()[3] ? this.score_type : (java.lang.CharSequence) defaultValue(fields()[3]);
        record.delta_moz = fieldSetFlags()[4] ? this.delta_moz : (java.lang.Double) defaultValue(fields()[4]);
        record.is_decoy = fieldSetFlags()[5] ? this.is_decoy : (java.lang.Boolean) defaultValue(fields()[5]);
        record.missed_cleavage = fieldSetFlags()[6] ? this.missed_cleavage : (java.lang.Integer) defaultValue(fields()[6]);
        record.fragment_matches_count = fieldSetFlags()[7] ? this.fragment_matches_count : (java.lang.Integer) defaultValue(fields()[7]);
        record.properties = fieldSetFlags()[8] ? this.properties : (fr.proline.core.om.model.msi.serializer.PeptideMatchProperties) defaultValue(fields()[8]);
        record.peptide = fieldSetFlags()[9] ? this.peptide : (fr.proline.core.om.model.msi.serializer.Peptide) defaultValue(fields()[9]);
        record.ms_query = fieldSetFlags()[10] ? this.ms_query : (fr.proline.core.om.model.msi.serializer.MsQuery) defaultValue(fields()[10]);
        record.children_ids = fieldSetFlags()[11] ? this.children_ids : (java.util.List<java.lang.Integer>) defaultValue(fields()[11]);
        record.best_child_id = fieldSetFlags()[12] ? this.best_child_id : (java.lang.Integer) defaultValue(fields()[12]);
        record.result_set_id = fieldSetFlags()[13] ? this.result_set_id : (java.lang.Integer) defaultValue(fields()[13]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
