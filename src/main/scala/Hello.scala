import requests.Response

object Hello {
  def main(args: Array[String]): Unit = {
    val r: Response = requests.get("https://api.swissborg.io/v1/challenge/rates")
    println(r)
  }
}
