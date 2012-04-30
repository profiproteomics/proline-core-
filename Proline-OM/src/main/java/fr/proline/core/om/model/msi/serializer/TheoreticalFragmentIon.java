/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package fr.proline.core.om.model.msi.serializer;  
@SuppressWarnings("all")
public class TheoreticalFragmentIon extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"TheoreticalFragmentIon\",\"namespace\":\"fr.proline.core.om.model.msi.serializer\",\"fields\":[{\"name\":\"description\",\"type\":\"string\",\"default\":\"\"},{\"name\":\"required_serie\",\"type\":{\"type\":\"record\",\"name\":\"FragmentIonType\",\"fields\":[{\"name\":\"ion_series\",\"type\":\"string\",\"default\":\"\"},{\"name\":\"neutral_loss\",\"type\":[\"string\",\"null\"]}]}},{\"name\":\"required_serie_quality_level\",\"type\":\"string\",\"default\":\"\"},{\"name\":\"fragment_max_moz\",\"type\":[\"double\",\"null\"]},{\"name\":\"residue_constraint\",\"type\":[\"string\",\"null\"]},{\"name\":\"ion_type\",\"type\":\"FragmentIonType\"}]}");
  @Deprecated public java.lang.CharSequence description;
  @Deprecated public fr.proline.core.om.model.msi.serializer.FragmentIonType required_serie;
  @Deprecated public java.lang.CharSequence required_serie_quality_level;
  @Deprecated public java.lang.Double fragment_max_moz;
  @Deprecated public java.lang.CharSequence residue_constraint;
  @Deprecated public fr.proline.core.om.model.msi.serializer.FragmentIonType ion_type;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return description;
    case 1: return required_serie;
    case 2: return required_serie_quality_level;
    case 3: return fragment_max_moz;
    case 4: return residue_constraint;
    case 5: return ion_type;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: description = (java.lang.CharSequence)value$; break;
    case 1: required_serie = (fr.proline.core.om.model.msi.serializer.FragmentIonType)value$; break;
    case 2: required_serie_quality_level = (java.lang.CharSequence)value$; break;
    case 3: fragment_max_moz = (java.lang.Double)value$; break;
    case 4: residue_constraint = (java.lang.CharSequence)value$; break;
    case 5: ion_type = (fr.proline.core.om.model.msi.serializer.FragmentIonType)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
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
   * Gets the value of the 'required_serie' field.
   */
  public fr.proline.core.om.model.msi.serializer.FragmentIonType getRequiredSerie() {
    return required_serie;
  }

  /**
   * Sets the value of the 'required_serie' field.
   * @param value the value to set.
   */
  public void setRequiredSerie(fr.proline.core.om.model.msi.serializer.FragmentIonType value) {
    this.required_serie = value;
  }

  /**
   * Gets the value of the 'required_serie_quality_level' field.
   */
  public java.lang.CharSequence getRequiredSerieQualityLevel() {
    return required_serie_quality_level;
  }

  /**
   * Sets the value of the 'required_serie_quality_level' field.
   * @param value the value to set.
   */
  public void setRequiredSerieQualityLevel(java.lang.CharSequence value) {
    this.required_serie_quality_level = value;
  }

  /**
   * Gets the value of the 'fragment_max_moz' field.
   */
  public java.lang.Double getFragmentMaxMoz() {
    return fragment_max_moz;
  }

  /**
   * Sets the value of the 'fragment_max_moz' field.
   * @param value the value to set.
   */
  public void setFragmentMaxMoz(java.lang.Double value) {
    this.fragment_max_moz = value;
  }

  /**
   * Gets the value of the 'residue_constraint' field.
   */
  public java.lang.CharSequence getResidueConstraint() {
    return residue_constraint;
  }

  /**
   * Sets the value of the 'residue_constraint' field.
   * @param value the value to set.
   */
  public void setResidueConstraint(java.lang.CharSequence value) {
    this.residue_constraint = value;
  }

  /**
   * Gets the value of the 'ion_type' field.
   */
  public fr.proline.core.om.model.msi.serializer.FragmentIonType getIonType() {
    return ion_type;
  }

  /**
   * Sets the value of the 'ion_type' field.
   * @param value the value to set.
   */
  public void setIonType(fr.proline.core.om.model.msi.serializer.FragmentIonType value) {
    this.ion_type = value;
  }

  /** Creates a new TheoreticalFragmentIon RecordBuilder */
  public static fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder newBuilder() {
    return new fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder();
  }
  
  /** Creates a new TheoreticalFragmentIon RecordBuilder by copying an existing Builder */
  public static fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder newBuilder(fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder other) {
    return new fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder(other);
  }
  
  /** Creates a new TheoreticalFragmentIon RecordBuilder by copying an existing TheoreticalFragmentIon instance */
  public static fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder newBuilder(fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon other) {
    return new fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder(other);
  }
  
  /**
   * RecordBuilder for TheoreticalFragmentIon instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<TheoreticalFragmentIon>
    implements org.apache.avro.data.RecordBuilder<TheoreticalFragmentIon> {

    private java.lang.CharSequence description;
    private fr.proline.core.om.model.msi.serializer.FragmentIonType required_serie;
    private java.lang.CharSequence required_serie_quality_level;
    private java.lang.Double fragment_max_moz;
    private java.lang.CharSequence residue_constraint;
    private fr.proline.core.om.model.msi.serializer.FragmentIonType ion_type;

    /** Creates a new Builder */
    private Builder() {
      super(fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder other) {
      super(other);
    }
    
    /** Creates a Builder by copying an existing TheoreticalFragmentIon instance */
    private Builder(fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon other) {
            super(fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.SCHEMA$);
      if (isValidValue(fields()[0], other.description)) {
        this.description = (java.lang.CharSequence) data().deepCopy(fields()[0].schema(), other.description);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.required_serie)) {
        this.required_serie = (fr.proline.core.om.model.msi.serializer.FragmentIonType) data().deepCopy(fields()[1].schema(), other.required_serie);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.required_serie_quality_level)) {
        this.required_serie_quality_level = (java.lang.CharSequence) data().deepCopy(fields()[2].schema(), other.required_serie_quality_level);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.fragment_max_moz)) {
        this.fragment_max_moz = (java.lang.Double) data().deepCopy(fields()[3].schema(), other.fragment_max_moz);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.residue_constraint)) {
        this.residue_constraint = (java.lang.CharSequence) data().deepCopy(fields()[4].schema(), other.residue_constraint);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.ion_type)) {
        this.ion_type = (fr.proline.core.om.model.msi.serializer.FragmentIonType) data().deepCopy(fields()[5].schema(), other.ion_type);
        fieldSetFlags()[5] = true;
      }
    }

    /** Gets the value of the 'description' field */
    public java.lang.CharSequence getDescription() {
      return description;
    }
    
    /** Sets the value of the 'description' field */
    public fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder setDescription(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.description = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'description' field has been set */
    public boolean hasDescription() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'description' field */
    public fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder clearDescription() {
      description = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'required_serie' field */
    public fr.proline.core.om.model.msi.serializer.FragmentIonType getRequiredSerie() {
      return required_serie;
    }
    
    /** Sets the value of the 'required_serie' field */
    public fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder setRequiredSerie(fr.proline.core.om.model.msi.serializer.FragmentIonType value) {
      validate(fields()[1], value);
      this.required_serie = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'required_serie' field has been set */
    public boolean hasRequiredSerie() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'required_serie' field */
    public fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder clearRequiredSerie() {
      required_serie = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'required_serie_quality_level' field */
    public java.lang.CharSequence getRequiredSerieQualityLevel() {
      return required_serie_quality_level;
    }
    
    /** Sets the value of the 'required_serie_quality_level' field */
    public fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder setRequiredSerieQualityLevel(java.lang.CharSequence value) {
      validate(fields()[2], value);
      this.required_serie_quality_level = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'required_serie_quality_level' field has been set */
    public boolean hasRequiredSerieQualityLevel() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'required_serie_quality_level' field */
    public fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder clearRequiredSerieQualityLevel() {
      required_serie_quality_level = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'fragment_max_moz' field */
    public java.lang.Double getFragmentMaxMoz() {
      return fragment_max_moz;
    }
    
    /** Sets the value of the 'fragment_max_moz' field */
    public fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder setFragmentMaxMoz(java.lang.Double value) {
      validate(fields()[3], value);
      this.fragment_max_moz = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'fragment_max_moz' field has been set */
    public boolean hasFragmentMaxMoz() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'fragment_max_moz' field */
    public fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder clearFragmentMaxMoz() {
      fragment_max_moz = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /** Gets the value of the 'residue_constraint' field */
    public java.lang.CharSequence getResidueConstraint() {
      return residue_constraint;
    }
    
    /** Sets the value of the 'residue_constraint' field */
    public fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder setResidueConstraint(java.lang.CharSequence value) {
      validate(fields()[4], value);
      this.residue_constraint = value;
      fieldSetFlags()[4] = true;
      return this; 
    }
    
    /** Checks whether the 'residue_constraint' field has been set */
    public boolean hasResidueConstraint() {
      return fieldSetFlags()[4];
    }
    
    /** Clears the value of the 'residue_constraint' field */
    public fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder clearResidueConstraint() {
      residue_constraint = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /** Gets the value of the 'ion_type' field */
    public fr.proline.core.om.model.msi.serializer.FragmentIonType getIonType() {
      return ion_type;
    }
    
    /** Sets the value of the 'ion_type' field */
    public fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder setIonType(fr.proline.core.om.model.msi.serializer.FragmentIonType value) {
      validate(fields()[5], value);
      this.ion_type = value;
      fieldSetFlags()[5] = true;
      return this; 
    }
    
    /** Checks whether the 'ion_type' field has been set */
    public boolean hasIonType() {
      return fieldSetFlags()[5];
    }
    
    /** Clears the value of the 'ion_type' field */
    public fr.proline.core.om.model.msi.serializer.TheoreticalFragmentIon.Builder clearIonType() {
      ion_type = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    @Override
    public TheoreticalFragmentIon build() {
      try {
        TheoreticalFragmentIon record = new TheoreticalFragmentIon();
        record.description = fieldSetFlags()[0] ? this.description : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.required_serie = fieldSetFlags()[1] ? this.required_serie : (fr.proline.core.om.model.msi.serializer.FragmentIonType) defaultValue(fields()[1]);
        record.required_serie_quality_level = fieldSetFlags()[2] ? this.required_serie_quality_level : (java.lang.CharSequence) defaultValue(fields()[2]);
        record.fragment_max_moz = fieldSetFlags()[3] ? this.fragment_max_moz : (java.lang.Double) defaultValue(fields()[3]);
        record.residue_constraint = fieldSetFlags()[4] ? this.residue_constraint : (java.lang.CharSequence) defaultValue(fields()[4]);
        record.ion_type = fieldSetFlags()[5] ? this.ion_type : (fr.proline.core.om.model.msi.serializer.FragmentIonType) defaultValue(fields()[5]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
