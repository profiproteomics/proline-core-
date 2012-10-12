package fr.proline.core.om.provider.msi.impl

import fr.proline.core.om.provider.msi.IProteinProvider
import scala.collection.Seq
import fr.proline.core.om.model.msi.SeqDatabase
import fr.proline.core.om.model.msi.Protein
import javax.persistence.EntityManager
import fr.proline.core.orm.pdi.BioSequence
import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.collection.JavaConversions.seqAsJavaList
import fr.proline.core.om.utils.PeptidesOMConverterUtil
import com.weiglewilczek.slf4s.Logging
import javax.persistence.NoResultException
import javax.persistence.NonUniqueResultException
import fr.proline.core.orm.pdi.repository.PdiBioSequenceRepository
import fr.proline.core.om.utils.ProteinsOMConverterUtil

/**
 * ORMProteinProvider provides access to Protein stored in PDI database.
 * 
 * Specified EntityManager should be a PDIdb EntityManager
 */
class ORMProteinProvider (val em:EntityManager ) extends IProteinProvider with Logging {
  
  val converter = new ProteinsOMConverterUtil(true)
  
  def getProteinsAsOptions(protIds: Seq[Int]): Array[Option[Protein]] = {
	
    var foundOMProtBuilder = Array.newBuilder[Option[Protein]]	
	val pdiBioSeqs = em.createQuery("FROM fr.proline.core.orm.pdi.BioSequence bioSeq WHERE id IN (:ids)",
	                                          classOf[fr.proline.core.orm.pdi.BioSequence] )
                                          .setParameter("ids", seqAsJavaList(protIds) ).getResultList().toList
                                          
    var resultIndex =0 
	protIds.foreach( protId =>{	  
		// Current Prot not found. Store None and go to next prot Id
		if(resultIndex >=pdiBioSeqs.length || pdiBioSeqs.apply(resultIndex).getId != protId){
		  foundOMProtBuilder += None
		} else{	       
		  //Current Prot found in Repository. Just save and go to next Prot Id and found Prot
		  foundOMProtBuilder += Some(converter.convertPdiBioSeqORM2OM(pdiBioSeqs.apply(resultIndex)))
	      resultIndex+=1
		}
    })
	     
	 if(resultIndex <= pdiBioSeqs.length-1){
      val msg = "Returned Proteins from Repository was not stored in final result ! Some errors occured ! "
      logger.warn(msg)
      throw new Exception(msg)
    }   
    foundOMProtBuilder.result
  }

  def getProtein(seq: String): Option[Protein] = { 
    try {
	  val bioSeq : BioSequence =  em.createQuery("SELECT bs FROM fr.proline.core.orm.pdi.BioSequence bs where bs.sequence = :seq",classOf[fr.proline.core.orm.pdi.BioSequence])
			  							.setParameter("seq", seq).getSingleResult()
	  Some(converter.convertPdiBioSeqORM2OM(bioSeq))
    } catch {
      case ex : Exception => {
        logger.warn(ex.getMessage)
        return None
      }
    }
    
  }

  def getProtein(accession: String, seqDb: SeqDatabase): Option[Protein] = { 
    val bioSeqRepo = new PdiBioSequenceRepository(em)
    val bioSeq = bioSeqRepo.findBioSequencePerAccessionAndSeqDB(accession, seqDb.id)
    if(bioSeq == null )
      return None
      
      Some(converter.convertPdiBioSeqORM2OM(bioSeq))
    
  }

}