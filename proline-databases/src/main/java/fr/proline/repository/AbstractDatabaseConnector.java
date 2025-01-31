package fr.proline.repository;

import static fr.profi.util.StringUtils.LINE_SEPARATOR;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.profi.util.PropertiesUtils;
import fr.profi.util.StringUtils;

public abstract class AbstractDatabaseConnector implements IDatabaseConnector, IConnectionListener {

	private static final String HIBERNATE_PROP_FILES_SUFFIXE="_hibernate.properties";
	/* Constants */
	public static final String JDBC_APPNAME_KEY = "ApplicationName";
	
	/*** JPA ****/
	public static final String PERSISTENCE_JDBC_DRIVER_KEY = "javax.persistence.jdbc.driver";
	public static final String PERSISTENCE_JDBC_URL_KEY = "javax.persistence.jdbc.url";
	public static final String PERSISTENCE_JDBC_USER_KEY = "javax.persistence.jdbc.user";
	public static final String PERSISTENCE_JDBC_PASSWORD_KEY = "javax.persistence.jdbc.password";
	public static final String PERSISTENCE_VALIDATION_MODE_KEY = "javax.persistence.validation.mode";

	/*** HIBERNATE ****/
	public static final String HIBERNATE_DIALECT_KEY = "hibernate.dialect";
	public static final String HIBERNATE_FETCH_SIZE_KEY = "hibernate.jdbc.fetch_size";
	public static final String HIBERNATE_BATCH_SIZE_KEY = "hibernate.jdbc.batch_size";
	public static final String HIBERNATE_BATCH_VERSIONED_DATA_KEY = "hibernate.jdbc.batch_versioned_data";
	public static final String HIBERNATE_BYTECODE_OPTIMIZER_KEY = "hibernate.bytecode.use_reflection_optimizer";
	public static final String HIBERNATE_IMPLICIT_NAMING_STRATEGY_KEY = "hibernate.implicit_naming_strategy";
	public static final String HIBERNATE_ID_NEW_GENERATOR_MAPPINGS_KEY= "hibernate.id.new_generator_mappings";

	/*** HIBERNATE POOL SPECIFIC  ****/ 	
	// HikariCP config keys
	public static final String HIBERNATE_HIKARI_CONNECTION_PROVIDER_CLASS_KEY = "hibernate.connection.provider_class";
	public static final String HIBERNATE_HIKARI_POOL_MIN_IDLE_KEY = "hibernate.hikari.minimumIdle";
	public static final String HIBERNATE_HIKARI_POOL_MAX_SIZE_KEY = "hibernate.hikari.maximumPoolSize";
	public static final String HIBERNATE_HIKARI_POOL_MAX_IDLE_TIME_KEY = "hibernate.hikari.idleTimeout";
	
	//public static final String HIBERNATE_POOL_PREFERRED_TEST_QUERY_KEY = "hibernate.hikari.connectionTestQuery"; not recommanded

	// c3po config keys
	// Hibernate renamed from c3p0.minPoolSize !
	public static final String HIBERNATE_C3PO_POOL_MIN_SIZE_KEY = "hibernate.c3p0.min_size";
	// Hibernate renamed from c3p0.maxPoolSize !
	public static final String HIBERNATE_C3PO_POOL_MAX_SIZE_KEY = "hibernate.c3p0.max_size";
	// Hibernate renamed from c3p0.maxIdleTime !
	public static final String HIBERNATE_C3PO_POOL_MAX_IDLE_TIME_KEY = "hibernate.c3p0.timeout";
	public static final String HIBERNATE_C3PO_POOL_MAX_STATEMENTS_PER_CON_KEY = "hibernate.c3p0.maxStatementsPerConnection";
	public static final String HIBERNATE_C3PO_POOL_TEST_CON_ON_CHECKIN_KEY = "hibernate.c3p0.testConnectionOnCheckin";
	// Hibernate renamed from c3p0.idleConnectionTestPeriod !
	public static final String HIBERNATE_C3PO_POOL_IDLE_CON_TEST_PERIOD_KEY = "hibernate.c3p0.idle_test_period";
	public static final String HIBERNATE_C3PO__POOL_PREFERRED_TEST_QUERY_KEY = "hibernate.c3p0.preferredTestQuery";
	

	public static final int DEFAULT_MAX_POOL_CONNECTIONS = 20; // TODO increase value for server side
	public static final int DEFAULT_MIN_IDLE_CONNECTIONS = 10; // TODO increase value for server side
	public static final int DEFAULT_MAX_IDLE_TIME = 14 * 60 * 1000; // Max idle time of 14 minutes
	public static final String PROLINE_MAX_POOL_CONNECTIONS_KEY = "proline.project.max.pool.connection";

	public static final String JDBC_SCHEME = "jdbc";

	private static final Logger LOG = LoggerFactory.getLogger(AbstractDatabaseConnector.class);

	private static final Map<String, Integer> CONNECTOR_INSTANCES = new HashMap<String, Integer>();

	private static final boolean DEFAULT_ORM_OPTIMIZATIONS = true;

	/* Instance variables */
	private final ProlineDatabaseType m_prolineDbType;

	private final Map<Object, Object> m_properties;

	private final String m_ident;

	private final Object m_connectorLock = new Object();

	/* All mutable fields are @GuardedBy("m_connectorLock") */
	private Map<Object, Object> m_additionalProperties;

	protected DataSource m_dataSource;

	private EntityManagerFactory m_entityManagerFactory;

	private List<EntityManager> m_entityManagers;

	private boolean m_closed;

	private int m_connectionCount = 0;

	/* Constructors */
	protected AbstractDatabaseConnector(
		final ProlineDatabaseType prolineDbType,
		final Map<Object, Object> properties) {

		if (prolineDbType == null) {
			throw new IllegalArgumentException("ProlineDbType is null");
		}

		// ----------- FOR DEV ONLY !!! read properties from config/<dbType>_hibernate.properties file
		Properties p = new Properties();
		try {
			File  f= new File("config/"+prolineDbType.name()+HIBERNATE_PROP_FILES_SUFFIXE);
			FileReader fr = new FileReader("config/"+prolineDbType.name()+HIBERNATE_PROP_FILES_SUFFIXE);
			p.load(fr);
			LOG.debug(" ------ Read properties file  : "+(prolineDbType.name()+HIBERNATE_PROP_FILES_SUFFIXE) +"from"+f.getAbsolutePath());
		} catch (IOException ignored) {

		}

		Iterator<String> keys = p.stringPropertyNames().iterator();
		HashMap<Object, Object> ap = new HashMap<>();
		while (keys.hasNext()){
			String k = keys.next();
			String res = p.getProperty(k);
			LOG.debug(" ------ add m_additionalProperties IS "+k+" : "+res);
			ap.put(k, res);
		}
		setAdditionalProperties(ap);


		m_prolineDbType = prolineDbType;

		if (properties == null) {
			throw new IllegalArgumentException("Properties Map is null");
		}

		m_properties = new HashMap<>(properties); // Protection copy

		final StringBuilder identBuffer = new StringBuilder(prolineDbType.getPersistenceUnitName());

		final String jdbcURL = PropertiesUtils.getProperty(m_properties, PERSISTENCE_JDBC_URL_KEY);

		if (!StringUtils.isEmpty(jdbcURL)) {
			identBuffer.append('_').append(jdbcURL.replaceAll(":", "_"));
		}

		m_ident = identBuffer.toString();

		final int newConnectorInstancesCount = addConnectorInstances(m_ident);

		if (newConnectorInstancesCount > 1) {
			/* Trace error in Production and Test mode */
			final Exception ex = new Exception("Multiple DatabaseConnector");

			LOG.error("There are " + newConnectorInstancesCount + " DatabaseConnector instances for ["
				+ m_ident + ']', ex);
		}

	}

	/* Public methods */
	public final ProlineDatabaseType getProlineDatabaseType() {
		return m_prolineDbType;
	}

	public final boolean isMemory() {
		/* Protection copy */
		final Map<Object, Object> propertiesCopy = new HashMap<>(m_properties);

		return isMemory(propertiesCopy);
	}

	public void setAdditionalProperties(final Map<Object, Object> additionalProperties) {

		synchronized (m_connectorLock) {

			if (isClosed()) {
				throw new IllegalStateException("Connector is ALREADY closed");
			}

			if (m_dataSource != null) {
				LOG.warn("DataSource ALREADY created : AdditionalProperties IGNORED");
			}

			if (m_entityManagerFactory != null) {
				LOG.warn("EntityManagerFactory ALREADY created : AdditionalProperties IGNORED");
			}

			if (additionalProperties == null) {
				m_additionalProperties = null;
			} else {
				m_additionalProperties = new HashMap<>(additionalProperties); // Protection copy
			}

		}

	}

	public final Object getProperty(Object key) {
		Object result = null;
		synchronized (m_connectorLock) {

			if (m_additionalProperties != null && m_additionalProperties.containsKey(key)) {
				result = m_additionalProperties.get(key);
			} else if (m_properties.containsKey(key)) {
				result = m_properties.get(key);
			}
		}

		return result;
	}

	public final DataSource getDataSource() {

		synchronized (m_connectorLock) {

			if (isClosed()) {
				throw new IllegalStateException("Connector is ALREADY closed");
			}

			if (m_dataSource == null) {
				final ProlineDatabaseType prolineDbType = getProlineDatabaseType();
				/* Protection copy */
				final Map<Object, Object> propertiesCopy = new HashMap<>(m_properties);

				if ((m_additionalProperties != null) && !m_additionalProperties.isEmpty()) {

					if (LOG.isDebugEnabled()) {
						LOG.debug("Creating DataSource with additionalProperties :" + LINE_SEPARATOR
							+ PropertiesUtils.formatProperties(m_additionalProperties));
					}

					propertiesCopy.putAll(m_additionalProperties);
				}

				try {
					m_dataSource = createDataSource(m_ident, propertiesCopy);
				} catch (Exception ex) {
					/* Log and re-throw */
					final String message = "Error creating DataSource for " + prolineDbType;
					LOG.error(message, ex);

					throw new RuntimeException(message, ex);
				}

			}

		} // End of synchronized block on m_connectorLock

		return m_dataSource;
	}
	
	public final Connection createUnmanagedConnection() throws SQLException {
		
		final String jdbcURL = PropertiesUtils.getProperty(m_properties, PERSISTENCE_JDBC_URL_KEY);
		final String user = PropertiesUtils.getProperty(m_properties, PERSISTENCE_JDBC_USER_KEY);
		final String password = PropertiesUtils.getProperty(m_properties, PERSISTENCE_JDBC_PASSWORD_KEY);
		
		return DriverManager.getConnection(jdbcURL,user,password);
	}

	protected final EntityManagerFactory getEntityManagerFactory() {

		synchronized (m_connectorLock) {

			if (isClosed()) {
				throw new IllegalStateException("Connector is ALREADY closed");
			}

			if (m_entityManagerFactory == null) {
				m_entityManagers = new ArrayList<>();

				final ProlineDatabaseType prolineDbType = getProlineDatabaseType();
				/* Protection copy */
				final Map<Object, Object> propertiesCopy = new HashMap<>(m_properties);

				if ((m_additionalProperties != null) && !m_additionalProperties.isEmpty()) {

					if (LOG.isDebugEnabled()) {
						LOG.debug("Creating EntityManagerFactory with additionalProperties :"
							+ LINE_SEPARATOR + PropertiesUtils.formatProperties(m_additionalProperties));
					}

					propertiesCopy.putAll(m_additionalProperties);
				}

				/*
				 * Force JDBC Driver, default Hibernate dialect and default ORM optimizations
				 */
				if (propertiesCopy.get(PERSISTENCE_JDBC_DRIVER_KEY) == null) {
					propertiesCopy.put(PERSISTENCE_JDBC_DRIVER_KEY, getDriverType().getJdbcDriver());
				}

				try {
					m_entityManagerFactory = createEntityManagerFactory(
						getProlineDatabaseType(),
						propertiesCopy,
						DEFAULT_ORM_OPTIMIZATIONS);
				} catch (Exception ex) {
					/* Log and re-throw */
					final String message = "Error creating EntityManagerFactory for " + prolineDbType;
					LOG.error(message, ex);

					throw new RuntimeException(message, ex);
				}
			}

		} // End of synchronized block on m_connectorLock

		return m_entityManagerFactory;
	}

	@Override
	public EntityManager createEntityManager() {
		//this.incrementOpenEntityManagerCount();
		EntityManager em = this.getEntityManagerFactory().createEntityManager();

		synchronized (m_connectorLock) {
			m_entityManagers.add(em);
		}

		return em;
	}

	@Override
	public int getOpenEntityManagerCount() {
		int openEmCount = 0;
		for (EntityManager m_entityManager : m_entityManagers) {
			if (m_entityManager.isOpen())
				openEmCount++;
		}
		return openEmCount;
	}

	@Override
	public int getOpenConnectionCount() {
		return m_connectionCount;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ' ' + m_ident;
	}

	public final void close() {

		synchronized (m_connectorLock) {

			if (!m_closed) { // Close only once
				m_closed = true;

				if (m_entityManagerFactory != null) {

					if (this.getOpenEntityManagerCount() > 0) {
						LOG.warn(
							"Some EntityManager instances are still open. " +
								"Closing the database connector before closing all related EntityManager instances may lead to inconsistent behavior.");
					}

					LOG.debug("Closing EntityManagerFactory for [{}]", m_ident);
					try {
						m_entityManagerFactory.close();
					} catch (Exception exClose) {
						LOG.error("Error closing EntityManagerFactory for [" + m_ident + ']', exClose);
					}

				}

				if (m_dataSource != null) {
					doClose(m_ident, m_dataSource);
				}

				final int remainingConnectorInstancesCount = removeConnectorInstances(m_ident);

				if (remainingConnectorInstancesCount > 0) {
					LOG.error("There are {} remaining DatabaseConnector instances for [{}]",
						remainingConnectorInstancesCount, m_ident);
				}

			} // End if (connector is not already closed)

		} // End of synchronized block on m_connectorLock

	}

	public final boolean isClosed() {
		boolean result;

		synchronized (m_connectorLock) {
			result = m_closed;
		} // End of synchronized block on m_connectorLock

		return result;
	}

	protected boolean isMemory(final Map<Object, Object> properties) {
		return false;
	}

	/**
	 * This method is called holding <code>m_connectorLock</code> intrinsic object <strong>lock</strong> by <code>getDataSource</code>.
	 * 
	 * @param ident
	 * @param properties
	 * @return
	 */
	protected abstract DataSource createDataSource(final String ident, final Map<Object, Object> properties);

	/**
	 * This method is called holding <code>m_connectorLock</code> intrinsic object <strong>lock</strong> by <code>getEntityManagerFactory</code>.
	 * 
	 * @param prolineDbType
	 * @param properties
	 * @param ormOptimizations
	 * @return
	 */
	protected EntityManagerFactory createEntityManagerFactory(
		final ProlineDatabaseType prolineDbType,
		final Map<Object, Object> properties,
		final boolean ormOptimizations) {

		if (prolineDbType == null) {
			throw new IllegalArgumentException("Database is null");
		}

		if (properties == null) {
			throw new IllegalArgumentException("Properties Map is null");
		}

		if (ormOptimizations) {
			optimize(properties);
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace("Effective EntityManagerFactory settings for " + prolineDbType + " :" + LINE_SEPARATOR
				+ PropertiesUtils.formatProperties(properties));
		}

		return Persistence.createEntityManagerFactory(prolineDbType.getPersistenceUnitName(), properties);
	}

	/**
	 * This method is called holding <code>m_connectorLock</code> intrinsic object <strong>lock</strong> by <code>close</code>.
	 * 
	 * @param source
	 */
	protected void doClose(final String ident, final DataSource source) {
		LOG.warn("Closing DatabaseConnector [{}] does not close already retrieved SQL JDBC Connection resources", ident);
	}

	protected static void enableC3P0Pool(final Map<Object, Object> properties) {

		if (properties == null) {
			throw new IllegalArgumentException("Properties Map is null");
		}

		properties.putIfAbsent(HIBERNATE_C3PO_POOL_MIN_SIZE_KEY, "0");// minPoolSize = 0

		properties.putIfAbsent(HIBERNATE_C3PO_POOL_MAX_SIZE_KEY, Integer.toString(DEFAULT_MAX_POOL_CONNECTIONS));

		properties.putIfAbsent(HIBERNATE_C3PO_POOL_MAX_IDLE_TIME_KEY, "840");	// Max idle time of 14 minutes

		properties.putIfAbsent(HIBERNATE_C3PO_POOL_MAX_STATEMENTS_PER_CON_KEY,"30");

		// If JDBC driver does NOT support Connection.isValid() method, override preferredTestQuery

		properties.putIfAbsent(HIBERNATE_C3PO_POOL_TEST_CON_ON_CHECKIN_KEY,"true"); // Check connection is valid asynchronously at every connection checkin

		properties.putIfAbsent(HIBERNATE_C3PO_POOL_IDLE_CON_TEST_PERIOD_KEY,"300"); // Check pooled but unchecked-out connections every 5 minutes (fastest recommended TCP keepalive)
	}

	
	protected static void enableHikariPool(final Map<Object, Object> properties) {

		if (properties == null) {
			throw new IllegalArgumentException("Properties Map is null");
		}
		properties.putIfAbsent(HIBERNATE_HIKARI_CONNECTION_PROVIDER_CLASS_KEY, "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");

		properties.putIfAbsent(HIBERNATE_HIKARI_POOL_MIN_IDLE_KEY, Integer.toString(DEFAULT_MIN_IDLE_CONNECTIONS));

		properties.putIfAbsent(HIBERNATE_HIKARI_POOL_MAX_SIZE_KEY, Integer.toString(DEFAULT_MAX_POOL_CONNECTIONS));

	  properties.putIfAbsent(HIBERNATE_HIKARI_POOL_MAX_IDLE_TIME_KEY, Integer.toString(DEFAULT_MAX_IDLE_TIME)); // Max idle time of 14 minutes

	}

	protected static Integer getMaxPoolConnection(final Map<Object, Object> properties){
		Integer maxConnection = DEFAULT_MAX_POOL_CONNECTIONS;
		if (properties.containsKey(PROLINE_MAX_POOL_CONNECTIONS_KEY)) {
			if (Integer.class.isInstance(properties.get(PROLINE_MAX_POOL_CONNECTIONS_KEY)))
				maxConnection = (Integer) properties.get(PROLINE_MAX_POOL_CONNECTIONS_KEY);
			else {
				try {
					maxConnection = Integer.parseInt((String) properties.get(PROLINE_MAX_POOL_CONNECTIONS_KEY));
				} catch (NumberFormatException nfe) {
					maxConnection = DEFAULT_MAX_POOL_CONNECTIONS;
				}
			}
		}
		return maxConnection;
	}
	
	private static int addConnectorInstances(final String ident) {
		assert (!StringUtils.isEmpty(ident)) : "addConnectorInstances() invalid ident";

		int result = 0;

		synchronized (CONNECTOR_INSTANCES) {
			final Integer oldCountObj = CONNECTOR_INSTANCES.get(ident);

			int oldCount = 0;

			if (oldCountObj != null) {
				oldCount = oldCountObj;
			}

			if (oldCount < 0) {
				LOG.error("Inconsistent Connector instances count (adding to {}): {}", ident, oldCount);

				oldCount = 0;
			}

			result = oldCount + 1;

			CONNECTOR_INSTANCES.put(ident, result);
		} // End of synchronized block on CONNECTOR_INSTANCES

		return result;
	}

	private static int removeConnectorInstances(final String ident) {
		assert (!StringUtils.isEmpty(ident)) : "removeConnectorInstances() invalid ident";

		int result = 0;

		synchronized (CONNECTOR_INSTANCES) {
			final Integer oldCountObj = CONNECTOR_INSTANCES.get(ident);

			int oldCount = 0;

			if (oldCountObj != null) {
				oldCount = oldCountObj;
			}

			if (oldCount <= 0) {
				LOG.error("Inconsistent Connector instances count (removing from {}): {}", ident, oldCount);
			} else {
				result = oldCount - 1;
			}

			CONNECTOR_INSTANCES.put(ident, Integer.valueOf(result));
		} // End of synchronized block on CONNECTOR_INSTANCES

		return result;
	}

	private static void optimize(final Map<Object, Object> properties) {
		assert (properties != null) : "optimize() properties is null";
		properties.putIfAbsent(HIBERNATE_IMPLICIT_NAMING_STRATEGY_KEY,"legacy-hbm");//to allow mapping between ORM ClassName and table

		properties.putIfAbsent(PERSISTENCE_VALIDATION_MODE_KEY, "none");

		properties.putIfAbsent(HIBERNATE_FETCH_SIZE_KEY, "1000");

		properties.putIfAbsent(HIBERNATE_BATCH_SIZE_KEY, "30");

		properties.putIfAbsent(HIBERNATE_BATCH_VERSIONED_DATA_KEY, "true");

		properties.putIfAbsent(HIBERNATE_BYTECODE_OPTIMIZER_KEY, "true");

		properties.putIfAbsent(HIBERNATE_ID_NEW_GENERATOR_MAPPINGS_KEY, "false");

	}

	@Override
	public void connectionClosed(ConnectionWrapper connectionWrapper) {
		synchronized (m_connectorLock) {
			m_connectionCount--;
		}
	}

	@Override
	public void connectionCreated(Connection connection) {
		synchronized (m_connectorLock) {
			m_connectionCount++;
		}
	}

}
