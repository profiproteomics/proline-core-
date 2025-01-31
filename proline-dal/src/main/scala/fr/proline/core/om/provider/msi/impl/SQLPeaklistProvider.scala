package fr.proline.core.om.provider.msi.impl

import fr.profi.jdbc.easy._
import fr.profi.util.primitives._
import fr.proline.context.MsiDbConnectionContext
import fr.proline.core.dal.DoJDBCReturningWork
import fr.proline.core.dal.tables.SelectQueryBuilder._
import fr.proline.core.dal.tables.SelectQueryBuilder1
import fr.proline.core.dal.tables.msi.MsiDbPeaklistTable
import fr.proline.core.om.builder.PeaklistBuilder
import fr.proline.core.om.model.msi.Peaklist

/**
 * @author David Bouyssie
 *
 */
class SQLPeaklistProvider(val msiDbCtx: MsiDbConnectionContext) {
  
  def getPeaklists(peaklistIds: Seq[Long]): Array[Peaklist] = {
    if( peaklistIds.isEmpty ) return Array()
    
    DoJDBCReturningWork.withEzDBC(msiDbCtx) { msiEzDBC =>

      PeaklistBuilder.buildPeaklists(
        SQLPeaklistProvider.selectPeaklistRecords(msiEzDBC,peaklistIds),
        pklSoftIds => SQLPeaklistSoftwareProvider.selectPklSoftRecords(msiEzDBC,pklSoftIds)
      )
      
    }
  }

}

object SQLPeaklistProvider {

  def selectPeaklistRecords(msiEzDBC: EasyDBC, peaklistIds: Seq[Long]): (IValueContainer => Peaklist) => Seq[Peaklist] = {
    
    val pklQuery = new SelectQueryBuilder1(MsiDbPeaklistTable).mkSelectQuery( (t,c) =>
      List(t.*) -> "WHERE "~ t.ID ~" IN("~ peaklistIds.mkString(",") ~")"
    )

    msiEzDBC.select(pklQuery)
  }
  
}


  