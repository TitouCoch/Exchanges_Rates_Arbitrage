import requests.Response
import play.api.libs.json._

object Main {

  private val ratesMap = collection.mutable.Map[String, Float]()
  private val currencyRate = collection.mutable.Map[String, Float]()
  private val costPred = collection.mutable.Map[String, Float]()
  private val namePred = collection.mutable.Map[String, Option[String]]()

  def loadData(): Unit = {
    val response: Response = requests.get("https://api.swissborg.io/v1/challenge/rates")
    val json = Json.parse(response.text)

    (json \ "rates").as[JsObject].fields.foreach { case (currency, rate) =>
      val separatedCurrency = currency.split("-")
      if (!currencyRate.contains(separatedCurrency(0))) {
        currencyRate(separatedCurrency(0)) = Float.PositiveInfinity
      }

      ratesMap(currency) = rate.as[String].toFloat
      costPred(currency) = Float.PositiveInfinity
      namePred(currency) = None
    }
    currencyRate("BTC") = 0.0f
  }

  def release(firstCurrency: String, secondCurrency: String): Unit = {
    if (costPred(secondCurrency) > costPred(firstCurrency) + costPred(firstCurrency) + costPred(secondCurrency)) {
      costPred(secondCurrency) = costPred(firstCurrency) + costPred(firstCurrency) + costPred(secondCurrency)
      namePred(secondCurrency) = Some(firstCurrency)
    }
  }

  def main(args: Array[String]): Unit = {
    loadData()
    //ratesMap.foreach { case (currency, rate) =>
    //  println(s"$currency: $rate")
    //}
  }
}
