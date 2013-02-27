package fr.proline.core.dal

import java.sql.SQLException
import com.weiglewilczek.slf4s.Logging
import fr.proline.context.BasicExecutionContext
import fr.proline.context.DatabaseConnectionContext
import fr.proline.context.IExecutionContext
import fr.proline.repository.IDataStoreConnectorFactory
import fr.proline.repository.IDatabaseConnector
import fr.proline.util.ThreadLogger

object ContextFactory extends Logging {

  /**
   * Creates an ExecutionContext instance from given DataStoreConnectorFactory and project Id.
   *
   * @param dsFactory
   *            Factory of Proline DataStore connectors.
   * @param projectId
   *            Id of project to retrieve project specific Dbs (MSI, LCMS).
   * @param useJPA
   *            If <code>true</code> all returned Db contexts wrap a new <code>EntityManager</code> for
   *            relevant Proline Db. If <code>false</code> all returned Db contexts wrap a new SQL JDBC
   *            <code>Connection</code>.
   * @return A new instance of ExecutionContext
   */
  def buildExecutionContext(dsFactory: IDataStoreConnectorFactory, projectId: Int, useJPA: Boolean): IExecutionContext = {
    val currentThread = Thread.currentThread

    if (!currentThread.isInstanceOf[ThreadLogger]) {
      currentThread.setUncaughtExceptionHandler(new ThreadLogger("fr.proline.core.dal.ContextFactory"))
    }

    var udsDb: DatabaseConnectionContext = null

    val udsDbConnector = dsFactory.getUdsDbConnector

    if (udsDbConnector != null) {
      udsDb = buildDbConnectionContext(udsDbConnector, useJPA)
    }

    var pdiDb: DatabaseConnectionContext = null

    val pdiDbConnector = dsFactory.getPdiDbConnector

    if (pdiDbConnector != null) {
      pdiDb = buildDbConnectionContext(pdiDbConnector, useJPA)
    }

    var psDb: DatabaseConnectionContext = null

    val psDbConnector = dsFactory.getPsDbConnector

    if (psDbConnector != null) {
      psDb = buildDbConnectionContext(psDbConnector, useJPA)
    }

    /* Project specific Dbs */
    var msiDb: DatabaseConnectionContext = null;

    val msiDbConnector = dsFactory.getMsiDbConnector(projectId)

    if (msiDbConnector != null) {
      msiDb = buildDbConnectionContext(msiDbConnector, useJPA)
    }

    var lcMsDb: DatabaseConnectionContext = null;

    val lcMsDbConnector = dsFactory.getLcMsDbConnector(projectId)

    if (lcMsDbConnector != null) {
      // FIXME: remove this when LCMS ORM is implemented
      lcMsDb = buildDbConnectionContext(lcMsDbConnector, false)
      //lcMsDb = buildDbConnectionContext(lcMsDbConnector, useJPA)
    }

    new BasicExecutionContext(udsDb, pdiDb, psDb, msiDb, lcMsDb)
  }

  /**
   * Creates a <code>DatabaseConnectionContext</code> from given DatabaseConnector.
   *
   * @param dbConnector
   *            DatabaseConnector used to access a Proline Db.
   * @param useJPA
   *            If <code>true</code> returned Db context wraps a new <code>EntityManager</code>. If
   *            <code>false</code> returned SQLConnectionContext wraps a new SQL JDBC <code>Connection</code>.
   * @return A new instance of <code>DatabaseConnectionContext</code> or <code>SQLConnectionContext</code> for SQL
   */
  def buildDbConnectionContext(dbConnector: IDatabaseConnector, useJPA: Boolean): DatabaseConnectionContext = {
    if (dbConnector == null) {
      throw new IllegalArgumentException("DbConnector is null")
    }

    if (useJPA) {
      new DatabaseConnectionContext(dbConnector)
    } else {

      try {
        new SQLConnectionContext(dbConnector.getDataSource.getConnection, dbConnector.getDriverType)
      } catch {

        case sqlEx: SQLException => {
          /* Log and re-throw */
          val message = "Unable to obtain SQL JDBC Connection for " + dbConnector.getProlineDatabaseType
          logger.error(message, sqlEx)

          throw new RuntimeException(message, sqlEx)
        }

      }

    } // End if (SQL mode)
  }

}

object BuildExecutionContext extends Logging {

  /**
   * Creates an ExecutionContext instance from given DataStoreConnectorFactory and project Id.
   *
   * @param dsFactory
   *            Factory of Proline DataStore connectors.
   * @param projectId
   *            Id of project to retrieve project specific Dbs (MSI, LCMS).
   * @param useJPA
   *            If <code>true</code> all returned Db contexts wrap a new <code>EntityManager</code> for
   *            relevant Proline Db. If <code>false</code> all returned Db contexts wrap a new SQL JDBC
   *            <code>Connection</code>.
   * @return A new instance of ExecutionContext
   */
  def apply(dsFactory: IDataStoreConnectorFactory, projectId: Int, useJPA: Boolean): IExecutionContext = {
    ContextFactory.buildExecutionContext(dsFactory, projectId, useJPA)
  }

}

object BuildDbConnectionContext extends Logging {

  /**
   * Creates a <code>DatabaseConnectionContext</code> from given DatabaseConnector.
   *
   * @param dbConnector
   *            DatabaseConnector used to access a Proline Db.
   *
   * @return A new instance of <code>DatabaseConnectionContext</code> or <code>SQLConnectionContext</code> for SQL
   */
  def apply[CtxType <: DatabaseConnectionContext](dbConnector: IDatabaseConnector)(implicit m: Manifest[CtxType]): CtxType = {

    val useJPA = if (m.erasure == classOf[SQLConnectionContext]) false
    else true

    this.logger.info("creation of execution context " + (if (useJPA) "using JPA mode" else "using SQL mode "))

    this.apply(dbConnector, useJPA).asInstanceOf[CtxType]
  }

  /**
   * Creates a <code>DatabaseConnectionContext</code> from given DatabaseConnector.
   *
   * @param dbConnector
   *            DatabaseConnector used to access a Proline Db.
   * @param useJPA
   *            If <code>true</code> returned Db context wraps a new <code>EntityManager</code>. If
   *            <code>false</code> returned SQLConnectionContext wraps a new SQL JDBC <code>Connection</code>.
   * @return A new instance of <code>DatabaseConnectionContext</code> or <code>SQLConnectionContext</code> for SQL
   */
  def apply(dbConnector: IDatabaseConnector, useJPA: Boolean): DatabaseConnectionContext = {
    ContextFactory.buildDbConnectionContext(dbConnector, useJPA)
  }

}