package object util {
  // function to implement the loan pattern on a Url
    def doLoanURLCall[T](url: String)(f:(scala.io.BufferedSource=>T)):T = {
    val src = scala.io.Source.fromURL(url)
    try {
      f(src)
      } catch {
        case ex: Exception => {
          //todo: add some logging here!!!
          println("Exception: " + ex.getMessage)
          throw ex
        }
      } finally {
        src.close
        }
      }
}