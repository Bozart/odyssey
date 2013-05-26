package common

package object util {
  // loan pattern with a url
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