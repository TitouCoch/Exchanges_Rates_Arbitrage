import play.api.libs.json._

object Main {
  case class CurrencyData(costPred: Float, namePred: Option[String])

  private val ratesMap = collection.mutable.Map[String, Float]()
  private val currencyRate = collection.mutable.Map[String, CurrencyData]()

  private def loadData(): Unit = {
    val response: requests.Response = requests.get("https://api.swissborg.io/v1/challenge/rates")
    val json = Json.parse(response.text)

    val newCurrencyRate = (json \ "rates").as[JsObject].fields.flatMap { case (currency, rate) =>
      val separatedCurrency = currency.split("-")
      val rateValue = rate.as[String].toFloat

      if (!currencyRate.contains(separatedCurrency(0))) {
        currencyRate(separatedCurrency(0)) = CurrencyData(Float.PositiveInfinity, None)
      }

      ratesMap(currency) = rateValue
      Some(separatedCurrency(0) -> CurrencyData(Float.PositiveInfinity, None))
    }.toMap

    currencyRate ++= newCurrencyRate
    currencyRate("BTC") = currencyRate("BTC").copy(costPred = 0.0f)
  }

  private def release(firstCurrency: String, secondCurrency: String): CurrencyData = {
    val newCostPred = currencyRate(firstCurrency).costPred + ratesMap(s"$firstCurrency-$secondCurrency")

    if (newCostPred < currencyRate(secondCurrency).costPred) {
      currencyRate(secondCurrency).copy(costPred = newCostPred, namePred = Some(firstCurrency))
    } else {
      currencyRate(secondCurrency)
    }
  }

  private def findArbitragePath(startCurrency: String, currentCurrency: String, visited: Set[String], path: List[String], wallet: Float): Option[(Float, List[String])] = {
    val updatedCurrencyRate = currencyRate.keys.foldLeft(currencyRate) { (acc, firstCurrency) =>
      currencyRate.keys.foldLeft(acc) { (innerAcc, secondCurrency) =>
        innerAcc(secondCurrency) = release(firstCurrency, secondCurrency)
        innerAcc
      }
    }

    val opportunities = updatedCurrencyRate.keys.flatMap { nextCurrency =>
      if (nextCurrency == startCurrency && visited.size == currencyRate.size) {
        val finalPath = path :+ s"$currentCurrency-$nextCurrency"
        val buyingPrice = wallet * ratesMap(finalPath.head)
        val sellingPrice = buyingPrice * ratesMap(finalPath.last)
        val profit = sellingPrice - buyingPrice
        Some((profit, finalPath))
      } else if (!visited.contains(nextCurrency)) {
        val newPath = path :+ s"$currentCurrency-$nextCurrency"
        findArbitragePath(startCurrency, nextCurrency, visited + nextCurrency, newPath, wallet)
      } else {
        None
      }
    }

    opportunities.reduceOption((a, b) => if (a._1 > b._1) a else b)
  }

  def main(args: Array[String]): Unit = {
    loadData()
    val startCurrency = "BTC"
    val wallet: Float = 100.0f
    val result = findArbitragePath(startCurrency, startCurrency, Set(startCurrency), List.empty, wallet)

    result match {
      case Some((profit, path)) =>
        println("Arbitration opportunity found !")
        println("Path : "+ path.mkString(" -> "))
        println("Gain : "+ profit)
      case None =>
        println("No arbitration opportunity found.")
    }
  }
}
