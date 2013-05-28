package common.util

/** common.util.io a set of common utility classes for io
  * Right now this is just a dumping ground for functions which seem useful enough
  * to put into their own library.
  *
  *  @todo implement download using tempfile, and when done, delete tempfile
  *  @todo implement conversion from compressed to uncompressed file (with cleanup)
  */
package object io {

  /** withURLSource: perform a function on a URL, which is automatically closed
    * This function takes a URL as a string, and a function which operates on a
    * scala.io.BufferedSource (which is what Source.fromURL produces), and automatically
    * closes the URL once the operation is complete or fails.
    *
    * For more help do a search for scala loan pattern, a common design pattern in
    * functional languages.
    *
    * @todo add some kind of exception logging
    */
  def withURLSource[A](url: String)(f: (scala.io.BufferedSource => A)): A = {
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