package fr.proline.core.om.storer.msi.impl

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection
import fr.profi.jdbc.easy._
import fr.profi.util.serialization.ProfiJson
import fr.proline.context.MsiDbConnectionContext
import fr.proline.core.dal._
import fr.proline.core.dal.helper.MsiDbHelper
import fr.proline.core.dal.tables.SelectQueryBuilder1
import fr.proline.core.dal.tables.SelectQueryBuilder._
import fr.proline.core.dal.tables.msi.{MsiDbPeptideMatchRelationTable, MsiDbPeptideMatchTable, MsiDbPeptideReadablePtmStringTable, MsiDbPeptideTable, MsiDbProteinMatchTable, MsiDbSequenceMatchTable}
import fr.proline.core.om.storer.msi.IRsStorer
import fr.proline.core.om.model.msi._
import fr.proline.repository.util.PostgresUtils
import fr.profi.util.sql.encodeRecordForPgCopy
import fr.profi.util.StringUtils
import fr.profi.util.primitives._
import fr.proline.core.orm.msi.PeptideReadablePtmString

import scala.collection.mutable

private[msi] object PgRsWriter extends AbstractSQLRsWriter() {

  // val bulkCopyManager = new CopyManager( msiDb1.ezDBC.connection.asInstanceOf[BaseConnection] )

  private val peptideTableCols = MsiDbPeptideTable.columnsAsStrList.mkString(",")
  private val readablePtmTableCols = MsiDbPeptideReadablePtmStringTable.columnsAsStrList.mkString(",")
  private val pepMatchTableColsWithoutPK = MsiDbPeptideMatchTable.columnsAsStrList.filter(_ != "id").mkString(",")
  private val pepMatchRelTableCols = MsiDbPeptideMatchRelationTable.columnsAsStrList.mkString(",")
  private val protMatchTableColsWithoutPK = MsiDbProteinMatchTable.columnsAsStrList.filter(_ != "id").mkString(",")
  private val seqMatchTableCols = MsiDbSequenceMatchTable.columnsAsStrList.mkString(",")

  //"SELECT ms_query_id, peptide_id, id FROM peptide_match WHERE result_set_id = " + rsId
  
  private val pepMatchUniqueFKsQueryRank = new SelectQueryBuilder1(MsiDbPeptideMatchTable).mkSelectQuery( (t,c) =>
    List(t.MS_QUERY_ID,t.RANK, t.PEPTIDE_ID,t.ID) -> "WHERE "~ t.RESULT_SET_ID ~" = ?"
  )
  private val protMatchUniqueFKQuery = new SelectQueryBuilder1(MsiDbProteinMatchTable).mkSelectQuery( (t,c) => 
    List(t.ACCESSION,t.ID) -> "WHERE "~ t.RESULT_SET_ID ~" = ?"
  )
  
  //def fetchExistingPeptidesIdByUniqueKey( pepSequences: Seq[String] ):  Map[String,Int] = null

  // TODO: check first peptideByUniqueKey ???
  /*override def insertNewPeptides(peptides: Seq[Peptide], peptideByUniqueKey: HashMap[String,Peptide], msiDbCtx: DatabaseConnectionContext): Unit = {

    DoJDBCWork.withConnection(msiDbCtx) { msiCon =>
      
      val bulkCopyManager = PostgresUtils.getCopyManager(msiCon)
  
      /*
      // Create TMP table
      val tmpPeptideTableName = "tmp_peptide_" + (scala.math.random * 1000000).toInt
      logger.info("creating temporary table '" + tmpPeptideTableName + "'...")
  
      val stmt = msiCon.createStatement()
      stmt.executeUpdate("CREATE TEMP TABLE " + tmpPeptideTableName + " (LIKE peptide) ON COMMIT DROP")
      */
  
      // Bulk insert of peptides
      logger.info("BULK insert of peptides")
  
      //val pgBulkLoader = bulkCopyManager.copyIn("COPY " + tmpPeptideTableName + " ( " + peptideTableCols + " ) FROM STDIN")
      val pgBulkLoader = bulkCopyManager.copyIn(s"COPY ${MsiDbPeptideTable.name} ($peptideTableCols) FROM STDIN")
  
      // Iterate over peptides
      for (peptide <- peptides) {
  
        val ptmString = if (peptide.ptmString != null) peptide.ptmString else ""
        val peptideValues = List(
          peptide.id,
          peptide.sequence,
          ptmString,
          peptide.calculatedMass,
          peptide.properties.map(ProfiJson.serialize(_))
        )
  
        // Store peptide
        val peptideBytes = encodeRecordForPgCopy(peptideValues)
        pgBulkLoader.writeToCopy(peptideBytes, 0, peptideBytes.length)
      }
  
      // End of BULK copy
      val nbInsertedRecords = pgBulkLoader.endCopy()
  
      /*// Move TMP table content to MAIN table
      logger.info("move TMP table " + tmpPeptideTableName + " into MAIN peptide table")
      stmt.executeUpdate("INSERT into peptide (" + peptideTableCols + ") " +
        "SELECT " + peptideTableCols + " FROM " + tmpPeptideTableName
      )*/
    }
    
  }*/

  //def fetchProteinIdentifiers( accessions: Seq[String] ): Array[Any] = null

  //def fetchExistingProteins( protCRCs: Seq[String] ): Array[Protein] = null

  //def storeNewProteins( proteins: Seq[Protein] ): Array[Protein] = null
  override  def insertSpecifiedRsReadablePtmStrings(rs: ResultSet, readablePtmStringByPepId: Map[Long, PeptideReadablePtmString], msiDbCtx: MsiDbConnectionContext): Int = {

    DoJDBCReturningWork.withConnection(msiDbCtx) { msiCon =>

      // Define some vars
      val bulkCopyManager = PostgresUtils.getCopyManager(msiCon)
      val rsId = rs.id

      // Bulk insert of readable ptm strings
      logger.info("BULK insert of readable ptm strings")

      //val pgBulkLoader = bulkCopyManager.copyIn("COPY " + tmpReadblePtmTableName + " ( " + readablePtmTableCols + " ) FROM STDIN")
      val pgBulkLoader = bulkCopyManager.copyIn(
        s"COPY ${MsiDbPeptideReadablePtmStringTable.name} ($readablePtmTableCols) FROM STDIN"
      )

      // Iterate over peptides
      for (peptide <- rs.peptides; if StringUtils.isNotEmpty(peptide.readablePtmString)) {
        val pepReadablePtm =  if(readablePtmStringByPepId.contains(peptide.id)) readablePtmStringByPepId(peptide.id).getReadablePtmString else  peptide.readablePtmString

        val readablePtmValues = List(
          peptide.id,
          rsId,
          pepReadablePtm
        )

        // Store readable PTM string
        val readablePtmBytes = encodeRecordForPgCopy(readablePtmValues)
        pgBulkLoader.writeToCopy(readablePtmBytes, 0, readablePtmBytes.length)
      }

      // End of BULK copy
      val nbInsertedRecords = pgBulkLoader.endCopy()

      nbInsertedRecords.toInt

    }
  }

  override def insertRsReadablePtmStrings(rs: ResultSet, msiDbCtx: MsiDbConnectionContext): Int = {
    insertSpecifiedRsReadablePtmStrings(rs, Map.empty[Long, PeptideReadablePtmString], msiDbCtx)
  }

  override def insertRsPeptideMatches(rs: ResultSet, msiDbCtx: MsiDbConnectionContext): Int = {

    DoJDBCReturningWork.withEzDBC(msiDbCtx) { msiEzDBC =>
      
      val msiCon = msiEzDBC.connection
      val bulkCopyManager = PostgresUtils.getCopyManager(msiCon)
      val scoringIdByType = new MsiDbHelper(msiDbCtx).getScoringIdByType
      
      // Retrieve some vars
      val rsId = rs.id
      val peptideMatches = rs.peptideMatches
  
      logger.debug("Number of peptideMatches to write :"+peptideMatches.length)
      
      /*
      // Create TMP table
      val tmpPepMatchTableName = "tmp_peptide_match_" + (scala.math.random * 1000000).toInt
      logger.info("creating temporary table '" + tmpPepMatchTableName + "'...")
  
      val stmt = msiCon.createStatement()
      stmt.executeUpdate("CREATE TEMP TABLE " + tmpPepMatchTableName + " (LIKE peptide_match) ON COMMIT DROP")
      */
  
      // Bulk insert of peptide matches
      logger.info("BULK insert of peptide matches")

      var scoringErr = false
      var scoreType : String = ""
      
      //val pgBulkLoader = bulkCopyManager.copyIn("COPY " + tmpPepMatchTableName + " ( id, " + pepMatchTableColsWithoutPK + " ) FROM STDIN")
      val pgBulkLoader = bulkCopyManager.copyIn(
        s"COPY ${MsiDbPeptideMatchTable.name} ($pepMatchTableColsWithoutPK) FROM STDIN"
      )

      // prepare a map for ionSeries
      val ionSeriesById = new HashMap[String, Array[String]]

      // Iterate over peptide matches to store them
      for (peptideMatch <- peptideMatches if !scoringErr) {
        
        scoreType = peptideMatch.scoreType.toString()
        val scoringId = scoringIdByType.get(scoreType)
        if(!scoringId.isDefined)
          scoringErr = true 
        else {
        
          val msQuery = peptideMatch.msQuery
          val bestChildId = peptideMatch.bestChildId

          var pmCharge = msQuery.charge
          if(peptideMatch.properties.isDefined) {
            val key = _formatPeptideMatchKey(msQuery.id, peptideMatch.rank, peptideMatch.peptide.id)
            if(peptideMatch.properties.get.getOmssaProperties.isDefined) {
              pmCharge = peptideMatch.properties.get.getOmssaProperties.get.getCorrectedCharge
              // extract ion series here
              ionSeriesById.put(key, peptideMatch.properties.get.getOmssaProperties.get.getIonSeries)
              // remove it from omssa properties before peptide match insertion to avoid flooding the peptide_match table
              peptideMatch.properties.get.getOmssaProperties.get.setIonSeries(null)
            } else if(peptideMatch.properties.get.getXtandemProperties.isDefined) {
              // also extract ion series for xtandem

              val ionSeriesMatches: Array[String] = {
                if (peptideMatch.properties.get.getXtandemProperties.get.getIonSeriesMatches != null)
                  peptideMatch.properties.get.getXtandemProperties.get.getIonSeriesMatches.map(entry => entry._1 + "=" + entry._2).toArray
                else
                  Array.empty[String]
              }
              val ionSeriesScores: Array[String] = {
                if (peptideMatch.properties.get.getXtandemProperties.get.getIonSeriesScores != null )
                  peptideMatch.properties.get.getXtandemProperties.get.getIonSeriesScores.map(entry => entry._1 + "=" + entry._2).toArray
                else
                  Array.empty[String]
              }
              // convert the two maps into a single array (at least for now)
              val ionSeries: Array[String] = ionSeriesMatches ++ ionSeriesScores
              ionSeriesById.put(key, ionSeries)
              peptideMatch.properties.get.getXtandemProperties.get.setIonSeriesMatches(null)
              peptideMatch.properties.get.getXtandemProperties.get.setIonSeriesScores(null)
            }
          }

          // Build a row containing peptide match values
          val pepMatchValues = List(
            //peptideMatch.id,
            pmCharge,
            msQuery.moz,
            peptideMatch.score,
            peptideMatch.rank,
            peptideMatch.cdPrettyRank,
            peptideMatch.sdPrettyRank,
            peptideMatch.deltaMoz,
            peptideMatch.missedCleavage,
            peptideMatch.fragmentMatchesCount,
            peptideMatch.isDecoy,
            peptideMatch.properties.map(ProfiJson.serialize(_)),
            peptideMatch.peptide.id,
            msQuery.id,
            if (bestChildId == 0) None else Some(bestChildId),
            scoringId.get,
            rsId
          )
        
         // Store peptide match
          val pepMatchBytes = encodeRecordForPgCopy(pepMatchValues)
          pgBulkLoader.writeToCopy(pepMatchBytes, 0, pepMatchBytes.length)
          }           
      } // end go through peptideMatch or until scoringErr !
  
      // End of BULK copy
      if(scoringErr){
        //Error : Cancel copy and throw exception
        pgBulkLoader.cancelCopy()
        throw new IllegalArgumentException("requirement failed: "+ "can't find a scoring id for the score type '" + scoreType + "'")
      }
        
       val nbInsertedPepMatches = pgBulkLoader.endCopy()
              
      /*// Move TMP table content to MAIN table
      logger.info("move TMP table " + tmpPepMatchTableName + " into MAIN peptide_match table. # of tmp pepMatches: "+nbInsertedPepMatches)
      val insertedPepMatched = stmt.executeUpdate("INSERT into peptide_match (" + pepMatchTableColsWithoutPK + ") " +
        "SELECT " + pepMatchTableColsWithoutPK + " FROM " + tmpPepMatchTableName)
      */
      
      // Retrieve generated peptide match ids
      //Same peptide could be identified by two PSMs from same query if seq contains X that could be replaced by I/L for instance
      // Get PepMatch MS_QUERY_ID, RANK, PEPTIDE_ID, ID for RS
      val pepMatchIdByKey = msiEzDBC.select( pepMatchUniqueFKsQueryRank, rsId) { r =>
        (_formatPeptideMatchKey(r.nextLong, r.nextInt, r.nextLong) -> r.nextLong)
      }.toMap
        
      // Iterate over peptide matches to update them
      peptideMatches.foreach {
        pepMatch => {
          val key = _formatPeptideMatchKey(pepMatch.msQuery.id, pepMatch.rank, pepMatch.peptide.id)
          val newId = pepMatchIdByKey(key)
          pepMatch.id = newId
          if(ionSeriesById.size > 0 && ionSeriesById.isDefinedAt(key)) {
            insertIonSeries(pepMatch, ionSeriesById.get(key).get, msiDbCtx)
          }
        }
      }

      this._linkPeptideMatchesToChildren(peptideMatches, bulkCopyManager)
  
      nbInsertedPepMatches.toInt
    }
    
  }

  private def _formatPeptideMatchKey(msQueryId: Long, peptideMatchRank: Int, peptideId: Long): String = msQueryId + "_" + peptideMatchRank + "%" + peptideId

  private def _linkPeptideMatchesToChildren(peptideMatches: Seq[PeptideMatch], bulkCopyManager: CopyManager): Unit = {

    val pgBulkLoader = bulkCopyManager.copyIn(
      s"COPY ${MsiDbPeptideMatchRelationTable.name} ($pepMatchRelTableCols) FROM STDIN"
    )
    
    // Iterate over peptide matches to store them
    for (
      peptideMatch <- peptideMatches;
      pepMatchChildrenIds <- Option(peptideMatch.getChildrenIds);
      pepMatchChildId <- pepMatchChildrenIds
    ) {

      // Build a row containing peptide_match_relation values
      val pepMatchRelationValues = List(
        peptideMatch.id,
        pepMatchChildId,
        peptideMatch.resultSetId
      )

      // Store peptide match
      val pepMatchRelationBytes = encodeRecordForPgCopy(pepMatchRelationValues)
      pgBulkLoader.writeToCopy(pepMatchRelationBytes, 0, pepMatchRelationBytes.length)
    }

    // End of BULK copy
    val nbInsertedRecords = pgBulkLoader.endCopy()
    
    logger.info(s"BULK insert of $nbInsertedRecords peptide match relations has been done !")
  }

  override def insertRsProteinMatches(rs: ResultSet, msiDbCtx: MsiDbConnectionContext): Int = {

    DoJDBCReturningWork.withEzDBC(msiDbCtx) { msiEzDBC =>
      
      val msiCon = msiEzDBC.connection
      val bulkCopyManager = PostgresUtils.getCopyManager(msiCon)
      
      // TODO: retrieve this only once
      val scoringIdByType = new MsiDbHelper(msiDbCtx).getScoringIdByType
      
      // Retrieve some vars
      val rsId = rs.id
      val proteinMatches = rs.proteinMatches
  
      /*
      // Create TMP table
      val tmpProtMatchTableName = "tmp_protein_match_" + (scala.math.random * 1000000).toInt
      logger.info("creating temporary table '" + tmpProtMatchTableName + "'...")
  
      val stmt = msiCon.createStatement()
      stmt.executeUpdate("CREATE TEMP TABLE " + tmpProtMatchTableName + " (LIKE protein_match) ON COMMIT DROP")
      */
  
      // Bulk insert of protein matches
      logger.info("BULK insert of protein matches")

      var scoringErr = false
      var scoreType : String = ""

      //val pgBulkLoader = bulkCopyManager.copyIn("COPY " + tmpProtMatchTableName + " ( id, " + protMatchTableColsWithoutPK + " ) FROM STDIN")
      val pgBulkLoader = bulkCopyManager.copyIn(
        s"COPY ${MsiDbProteinMatchTable.name} ($protMatchTableColsWithoutPK) FROM STDIN"
      )
  
      // Map protein matches by their accession
      val proteinMatchByAcc = new HashMap[String,ProteinMatch]()
      proteinMatchByAcc.sizeHint(proteinMatches.length)
      
      // Iterate protein matches to store them
      for (proteinMatch <- proteinMatches if !scoringErr) {
        proteinMatchByAcc.put(proteinMatch.accession, proteinMatch)
  
        scoreType = proteinMatch.scoreType.toString
        val scoringId = scoringIdByType.get(scoreType)
        if(!scoringId.isDefined)
          scoringErr = true
        else {

          val scoringId = scoringIdByType.get(scoreType)
        require(scoringId.isDefined,"can't find a scoring id for the score type '" + scoreType + "'")
        //val pepMatchPropsAsJSON = if( peptideMatch.properties.isDefined ) ProfiJson.serialize(peptideMatch.properties.get) else ""
  
        val proteinId = proteinMatch.getProteinId
        
        // Build a row containing protein match values
        val protMatchValues = List(
          //proteinMatch.id,
          proteinMatch.accession,
          proteinMatch.description,
          Option(proteinMatch.geneName),
          proteinMatch.score,
          proteinMatch.sequenceMatches.length,
          proteinMatch.peptideMatchesCount,
          proteinMatch.isDecoy,
          proteinMatch.isLastBioSequence,
          proteinMatch.properties.map(ProfiJson.serialize(_)),
          proteinMatch.taxonId,
          if (proteinId > 0) Some(proteinId) else None,
          scoringId.get,
          rsId
        )
  
        // Store protein match
        val protMatchBytes = encodeRecordForPgCopy(protMatchValues)
        pgBulkLoader.writeToCopy(protMatchBytes, 0, protMatchBytes.length)
      }
    } // end go through protMatch or until scoringErr !

    if(scoringErr){
      //Error : Cancel copy and throw exception
      pgBulkLoader.cancelCopy()
      throw new IllegalArgumentException("requirement failed: "+ "can't find a scoring id for the score type '" + scoreType + "'")
    }

      // End of BULK copy
      val nbInsertedProtMatches = pgBulkLoader.endCopy()
  
      // Move TMP table content to MAIN table
      /*logger.info("move TMP table " + tmpProtMatchTableName + " into MAIN protein_match table")
      stmt.executeUpdate("INSERT into protein_match (" + protMatchTableColsWithoutPK + ") " +
        "SELECT " + protMatchTableColsWithoutPK + " FROM " + tmpProtMatchTableName)
      */
      
      // Retrieve generated protein match ids and update protein matches ids
      msiEzDBC.select(protMatchUniqueFKQuery, rsId) { r =>
        proteinMatchByAcc(r.nextString).id = r.nextLong
      }
      
      // Link protein matches to seq databases
      // TODO: implement this method with PgCopy
      this.linkProteinMatchesToSeqDatabases(rs,msiEzDBC,proteinMatches)
      
      nbInsertedProtMatches.toInt
    }
    
  }

  override def insertRsSequenceMatches(rs: ResultSet, msiDbCtx: MsiDbConnectionContext): Int = {

    DoJDBCReturningWork.withConnection(msiDbCtx) { msiCon =>
      
      val bulkCopyManager = PostgresUtils.getCopyManager(msiCon)
  
      // Retrieve some vars
      val rsId = rs.id
      val isDecoy = rs.isDecoy
      val proteinMatches = rs.proteinMatches
  
      /*
      // Create TMP table
      val tmpSeqMatchTableName = "tmp_sequence_match_" + (scala.math.random * 1000000).toInt
      logger.info("creating temporary table '" + tmpSeqMatchTableName + "'...")
  
      val stmt = msiCon.createStatement()
      stmt.executeUpdate("CREATE TEMP TABLE " + tmpSeqMatchTableName + " (LIKE sequence_match) ON COMMIT DROP")
      */
      
      // Bulk insert of sequence matches
      logger.info("BULK insert of sequence matches")
      
      //val pgBulkLoader = bulkCopyManager.copyIn("COPY " + tmpSeqMatchTableName + " ( " + seqMatchTableCols + " ) FROM STDIN")
      val pgBulkLoader = bulkCopyManager.copyIn(s"COPY ${MsiDbSequenceMatchTable.name} ($seqMatchTableCols) FROM STDIN")
      
      // Iterate over protein matches
      for (proteinMatch <- proteinMatches) {
  
        val proteinMatchId = proteinMatch.id
  
        for (seqMatch <- proteinMatch.sequenceMatches) {
  
          val seqMatchValues = List(
            proteinMatchId,
            seqMatch.getPeptideId,
            seqMatch.start,
            seqMatch.end,
            seqMatch.residueBefore.toString(),
            seqMatch.residueAfter.toString(),
            isDecoy,
            seqMatch.properties.map(ProfiJson.serialize(_)),
            seqMatch.getBestPeptideMatchId,
            rsId
          )
  
          // Store sequence match
          val seqMatchBytes = encodeRecordForPgCopy(seqMatchValues)
          pgBulkLoader.writeToCopy(seqMatchBytes, 0, seqMatchBytes.length)
        }
      }
  
      // End of BULK copy
      val nbInsertedRecords = pgBulkLoader.endCopy()
  
      // Move TMP table content to MAIN table
      /*logger.info("move TMP table " + tmpSeqMatchTableName + " into MAIN sequence_match table")
      stmt.executeUpdate("INSERT into sequence_match (" + seqMatchTableCols + ") " +
        "SELECT " + seqMatchTableCols + " FROM " + tmpSeqMatchTableName)
      */
  
      nbInsertedRecords.toInt
    }
    
  }

}