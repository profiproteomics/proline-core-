/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package fr.proline.core.om.model.msi.serializer;  
@SuppressWarnings("all")
public class InstrumentConfig extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"InstrumentConfig\",\"namespace\":\"fr.proline.core.om.model.msi.serializer\",\"fields\":[{\"name\":\"id\",\"type\":\"int\",\"default\":\"\"},{\"name\":\"name\",\"type\":\"string\",\"default\":\"\"},{\"name\":\"instrument\",\"type\":{\"type\":\"record\",\"name\":\"Instrument\",\"fields\":[{\"name\":\"id\",\"type\":\"int\",\"default\":\"\"},{\"name\":\"name\",\"type\":\"string\",\"default\":\"\"},{\"name\":\"source\",\"type\":[\"string\",\"null\"]}]}},{\"name\":\"ms1_analyzer\",\"type\":\"string\",\"default\":\"\"},{\"name\":\"msn_analyzer\",\"type\":\"string\",\"default\":\"\"},{\"name\":\"activation_type\",\"type\":\"string\",\"default\":\"\"}]}");
  @Deprecated public int id;
  @Deprecated public java.lang.CharSequence name;
  @Deprecated public fr.proline.core.om.model.msi.serializer.Instrument instrument;
  @Deprecated public java.lang.CharSequence ms1_analyzer;
  @Deprecated public java.lang.CharSequence msn_analyzer;
  @Deprecated public java.lang.CharSequence activation_type;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return id;
    case 1: return name;
    case 2: return instrument;
    case 3: return ms1_analyzer;
    case 4: return msn_analyzer;
    case 5: return activation_type;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: id = (java.lang.Integer)value$; break;
    case 1: name = (java.lang.CharSequence)value$; break;
    case 2: instrument = (fr.proline.core.om.model.msi.serializer.Instrument)value$; break;
    case 3: ms1_analyzer = (java.lang.CharSequence)value$; break;
    case 4: msn_analyzer = (java.lang.CharSequence)value$; break;
    case 5: activation_type = (java.lang.CharSequence)value$; break;
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
   * Gets the value of the 'instrument' field.
   */
  public fr.proline.core.om.model.msi.serializer.Instrument getInstrument() {
    return instrument;
  }

  /**
   * Sets the value of the 'instrument' field.
   * @param value the value to set.
   */
  public void setInstrument(fr.proline.core.om.model.msi.serializer.Instrument value) {
    this.instrument = value;
  }

  /**
   * Gets the value of the 'ms1_analyzer' field.
   */
  public java.lang.CharSequence getMs1Analyzer() {
    return ms1_analyzer;
  }

  /**
   * Sets the value of the 'ms1_analyzer' field.
   * @param value the value to set.
   */
  public void setMs1Analyzer(java.lang.CharSequence value) {
    this.ms1_analyzer = value;
  }

  /**
   * Gets the value of the 'msn_analyzer' field.
   */
  public java.lang.CharSequence getMsnAnalyzer() {
    return msn_analyzer;
  }

  /**
   * Sets the value of the 'msn_analyzer' field.
   * @param value the value to set.
   */
  public void setMsnAnalyzer(java.lang.CharSequence value) {
    this.msn_analyzer = value;
  }

  /**
   * Gets the value of the 'activation_type' field.
   */
  public java.lang.CharSequence getActivationType() {
    return activation_type;
  }

  /**
   * Sets the value of the 'activation_type' field.
   * @param value the value to set.
   */
  public void setActivationType(java.lang.CharSequence value) {
    this.activation_type = value;
  }

  /** Creates a new InstrumentConfig RecordBuilder */
  public static fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder newBuilder() {
    return new fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder();
  }
  
  /** Creates a new InstrumentConfig RecordBuilder by copying an existing Builder */
  public static fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder newBuilder(fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder other) {
    return new fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder(other);
  }
  
  /** Creates a new InstrumentConfig RecordBuilder by copying an existing InstrumentConfig instance */
  public static fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder newBuilder(fr.proline.core.om.model.msi.serializer.InstrumentConfig other) {
    return new fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder(other);
  }
  
  /**
   * RecordBuilder for InstrumentConfig instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<InstrumentConfig>
    implements org.apache.avro.data.RecordBuilder<InstrumentConfig> {

    private int id;
    private java.lang.CharSequence name;
    private fr.proline.core.om.model.msi.serializer.Instrument instrument;
    private java.lang.CharSequence ms1_analyzer;
    private java.lang.CharSequence msn_analyzer;
    private java.lang.CharSequence activation_type;

    /** Creates a new Builder */
    private Builder() {
      super(fr.proline.core.om.model.msi.serializer.InstrumentConfig.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder other) {
      super(other);
    }
    
    /** Creates a Builder by copying an existing InstrumentConfig instance */
    private Builder(fr.proline.core.om.model.msi.serializer.InstrumentConfig other) {
            super(fr.proline.core.om.model.msi.serializer.InstrumentConfig.SCHEMA$);
      if (isValidValue(fields()[0], other.id)) {
        this.id = (java.lang.Integer) data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.name)) {
        this.name = (java.lang.CharSequence) data().deepCopy(fields()[1].schema(), other.name);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.instrument)) {
        this.instrument = (fr.proline.core.om.model.msi.serializer.Instrument) data().deepCopy(fields()[2].schema(), other.instrument);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.ms1_analyzer)) {
        this.ms1_analyzer = (java.lang.CharSequence) data().deepCopy(fields()[3].schema(), other.ms1_analyzer);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.msn_analyzer)) {
        this.msn_analyzer = (java.lang.CharSequence) data().deepCopy(fields()[4].schema(), other.msn_analyzer);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.activation_type)) {
        this.activation_type = (java.lang.CharSequence) data().deepCopy(fields()[5].schema(), other.activation_type);
        fieldSetFlags()[5] = true;
      }
    }

    /** Gets the value of the 'id' field */
    public java.lang.Integer getId() {
      return id;
    }
    
    /** Sets the value of the 'id' field */
    public fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder setId(int value) {
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
    public fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder clearId() {
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'name' field */
    public java.lang.CharSequence getName() {
      return name;
    }
    
    /** Sets the value of the 'name' field */
    public fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder setName(java.lang.CharSequence value) {
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
    public fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder clearName() {
      name = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'instrument' field */
    public fr.proline.core.om.model.msi.serializer.Instrument getInstrument() {
      return instrument;
    }
    
    /** Sets the value of the 'instrument' field */
    public fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder setInstrument(fr.proline.core.om.model.msi.serializer.Instrument value) {
      validate(fields()[2], value);
      this.instrument = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'instrument' field has been set */
    public boolean hasInstrument() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'instrument' field */
    public fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder clearInstrument() {
      instrument = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'ms1_analyzer' field */
    public java.lang.CharSequence getMs1Analyzer() {
      return ms1_analyzer;
    }
    
    /** Sets the value of the 'ms1_analyzer' field */
    public fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder setMs1Analyzer(java.lang.CharSequence value) {
      validate(fields()[3], value);
      this.ms1_analyzer = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'ms1_analyzer' field has been set */
    public boolean hasMs1Analyzer() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'ms1_analyzer' field */
    public fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder clearMs1Analyzer() {
      ms1_analyzer = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /** Gets the value of the 'msn_analyzer' field */
    public java.lang.CharSequence getMsnAnalyzer() {
      return msn_analyzer;
    }
    
    /** Sets the value of the 'msn_analyzer' field */
    public fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder setMsnAnalyzer(java.lang.CharSequence value) {
      validate(fields()[4], value);
      this.msn_analyzer = value;
      fieldSetFlags()[4] = true;
      return this; 
    }
    
    /** Checks whether the 'msn_analyzer' field has been set */
    public boolean hasMsnAnalyzer() {
      return fieldSetFlags()[4];
    }
    
    /** Clears the value of the 'msn_analyzer' field */
    public fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder clearMsnAnalyzer() {
      msn_analyzer = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /** Gets the value of the 'activation_type' field */
    public java.lang.CharSequence getActivationType() {
      return activation_type;
    }
    
    /** Sets the value of the 'activation_type' field */
    public fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder setActivationType(java.lang.CharSequence value) {
      validate(fields()[5], value);
      this.activation_type = value;
      fieldSetFlags()[5] = true;
      return this; 
    }
    
    /** Checks whether the 'activation_type' field has been set */
    public boolean hasActivationType() {
      return fieldSetFlags()[5];
    }
    
    /** Clears the value of the 'activation_type' field */
    public fr.proline.core.om.model.msi.serializer.InstrumentConfig.Builder clearActivationType() {
      activation_type = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    @Override
    public InstrumentConfig build() {
      try {
        InstrumentConfig record = new InstrumentConfig();
        record.id = fieldSetFlags()[0] ? this.id : (java.lang.Integer) defaultValue(fields()[0]);
        record.name = fieldSetFlags()[1] ? this.name : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.instrument = fieldSetFlags()[2] ? this.instrument : (fr.proline.core.om.model.msi.serializer.Instrument) defaultValue(fields()[2]);
        record.ms1_analyzer = fieldSetFlags()[3] ? this.ms1_analyzer : (java.lang.CharSequence) defaultValue(fields()[3]);
        record.msn_analyzer = fieldSetFlags()[4] ? this.msn_analyzer : (java.lang.CharSequence) defaultValue(fields()[4]);
        record.activation_type = fieldSetFlags()[5] ? this.activation_type : (java.lang.CharSequence) defaultValue(fields()[5]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
