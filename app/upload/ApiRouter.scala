package upload

import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Sink}
import akka.util.ByteString
import algos.SumSolver
import controllers.HomeController
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._
import play.core.parsers.Multipart.{FileInfo, FilePartHandler}

import java.io.File
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class ApiRouter @Inject()(action: DefaultActionBuilder,
                          solver: SumSolver,
                          implicit val exCtx: ExecutionContext) extends SimpleRouter with Logging with ErrorTypes {

  override def routes: Routes = {
    case POST(p"/${int(sumToFind)}") =>
      logger.info(s"got number $sumToFind")
      action { request =>
        Try {
          val nums = request.body.asJson.get.as[List[List[Int]]]
          solver.solveSum(sumToFind, nums)
        } match {
          case Failure(exception) => Results.NotAcceptable(encodeError(exception))
          case Success(value) => Results.Ok(Json.toJson(value))
        }
      }

  }
}
