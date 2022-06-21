package upload

import play.api.libs.json._



trait ErrorTypes {
  case class ErrorDesc(code: String, message: String)
  implicit val errorDeskFormat: Format[ErrorDesc] = Json.format[ErrorDesc]

  def encodeError(ex: Throwable): JsValue = {
    val (errCode, errMsg) = ex match {
      //todo any mappings here
      case nfe: NumberFormatException => ("wrong.input.type", nfe.toString)
      case nfe: JsResultException => ("wrong.input.type", nfe.toString)
      case _: Throwable => (ex.getLocalizedMessage, ex.toString)
    }
    Json.toJson(ErrorDesc(errCode, errMsg))
  }
}
