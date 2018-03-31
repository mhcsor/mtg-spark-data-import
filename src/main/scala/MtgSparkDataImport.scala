import java.util.UUID

import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.common.SolrInputDocument
import play.api.libs.json.Json
import play.api.libs.json.Reads._

import scala.beans.BeanProperty
import scala.collection.JavaConverters._
import scala.io.Source

object MtgSparkDataImport {

  case class Legality(format: String, legality: String)

  case class Card(
  @BeanProperty layout: String = "",
  @BeanProperty name: String = "",
  @BeanProperty cmc: Int = -1,
  @BeanProperty manaCost: String = "",
  @BeanProperty colors: Seq[String] = Seq.empty[String],
  @BeanProperty nominalType: String = "",
  @BeanProperty types: Seq[String] = Seq.empty[String],
  @BeanProperty subtypes: Seq[String] = Seq.empty[String],
  @BeanProperty text: String = "",
  @BeanProperty power: String = "",
  @BeanProperty toughness: String = "",
  @BeanProperty imageName: String = "",
  @BeanProperty printings: Seq[String] = Seq.empty[String],
  @BeanProperty colorIdentity: Seq[String] = Seq.empty[String],
  @BeanProperty legalities: Seq[Legality] = Seq.empty[Legality])

  implicit def legalityFormat = Json.using[Json.WithDefaultValues].format[Legality]
  implicit def cardFormat = Json.using[Json.WithDefaultValues].format[Card]

  def main(args: Array[String]): Unit = {
    val cardsStr = Source.fromResource("all-cards.json").getLines.mkString("\n")
    val cardsJson = Json.parse(cardsStr)
    val cards = (cardsJson \ "cards").validate[Seq[Card]].get
    val client = solrClient()
    val documents = cards.map(card => cardToSolrInputDocument(card)).asJavaCollection

    client.add(documents)
    client.commit()

  }

  def cardToSolrInputDocument(caseClass: Card) = {
    val map = classOf[Card].getDeclaredFields
      .map(_.getName) // all field names
      .zip(caseClass.productIterator.to).toMap // zipped with all values

    val document = new SolrInputDocument()
    import shapeless._
    val legalitySeq = TypeCase[Seq[Legality]]
    val stringSeq    = TypeCase[Seq[String]]

    map.map(entry => {
      entry._2 match {
        case legalitySeq(vs) => document.addField(entry._1, vs.map(legality => legality.format.concat("=").concat(legality.legality)).asJavaCollection)
        case stringSeq(vs) => document.addField(entry._1, vs.asJavaCollection)
        case _ => document.addField(entry._1, entry._2)
      }
    })

    document
  }

  def solrClient(): SolrClient = {
    val solrUrl = "http://localhost:8983/solr/cards"

    return new HttpSolrClient.Builder(solrUrl)
      .withConnectionTimeout(10000)
      .withSocketTimeout(60000)
      .build
  }


}
