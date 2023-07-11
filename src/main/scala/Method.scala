import play.api.libs.json._

protected class Method {
  case class CurrencyData(costPred: Float, namePred: Option[String])

  protected val ratesMap = collection.mutable.Map[String, Float]()
  protected val currencyRate = collection.mutable.Map[String, CurrencyData]()

  protected def loadData(startCurrency: String): Unit = {
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
    currencyRate(startCurrency) = currencyRate(startCurrency).copy(costPred = 0.0f)
  }

  protected def release(firstCurrency: String, secondCurrency: String): CurrencyData = {
    val newCostPred = currencyRate(firstCurrency).costPred + ratesMap(s"$firstCurrency-$secondCurrency")

    if (newCostPred < currencyRate(secondCurrency).costPred) {
      currencyRate(secondCurrency).copy(costPred = newCostPred, namePred = Some(firstCurrency))
    } else {
      currencyRate(secondCurrency)
    }
  }

  protected def findArbitragePath(startCurrency: String, currentCurrency: String, visited: Set[String], path: List[String], wallet: Float): Option[(Float, List[String])] = {
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

  protected def runArbitrage(startCurrency: String, wallet: Float): Option[(Float, List[String])] = {
    loadData(startCurrency)
    findArbitragePath(startCurrency, startCurrency, Set(startCurrency), List.empty, wallet)
  }
}
