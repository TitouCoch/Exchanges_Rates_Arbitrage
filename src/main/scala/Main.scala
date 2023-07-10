import play.api.libs.json.{JsObject, Json}

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
    currencyRate("EUR") = currencyRate("EUR").copy(costPred = 1.0f)
  }

  private def release(firstCurrency: String, secondCurrency: String): CurrencyData = {
    val newCostPred = currencyRate(firstCurrency).costPred + ratesMap(s"$firstCurrency-$secondCurrency")

    println(newCostPred)
    println(currencyRate(secondCurrency))

    if (currencyRate(secondCurrency).costPred > newCostPred ) {

      currencyRate(secondCurrency).copy(costPred = newCostPred, namePred = Some(firstCurrency))
    } else {
      currencyRate(secondCurrency)
    }

  }

  def bellmanFord(): List[String] = {
    val updatedCurrencyRate = (0 until currencyRate.size - 1).foldLeft(currencyRate) { (_, _) =>
      currencyRate.keys.foldLeft(currencyRate) { (acc, firstCurrency) =>
        currencyRate.keys.foldLeft(acc) { (innerAcc, secondCurrency) =>
          innerAcc(secondCurrency) = release(firstCurrency, secondCurrency)
          println(currencyRate(secondCurrency))
          println(" ")
          innerAcc

        }
      }
    }


    println(currencyRate)
    val opportunities = updatedCurrencyRate.keys.flatMap { firstCurrency =>
      updatedCurrencyRate.keys.flatMap { secondCurrency =>
        if (updatedCurrencyRate(secondCurrency).costPred > updatedCurrencyRate(firstCurrency).costPred + ratesMap(s"$firstCurrency-$secondCurrency")) {
          Some(s"$firstCurrency-$secondCurrency")
        } else {
          None
        }
      }
    }.toList

    opportunities
  }


  def main(args: Array[String]): Unit = {
    var previousOpportunities: List[String] = List.empty

    for (_ <- 0 until 3) {
      Thread.sleep(1000)
      loadData()
      println(currencyRate)
      println(ratesMap)
      previousOpportunities = bellmanFord()
      println(previousOpportunities)
    }
  }

}
