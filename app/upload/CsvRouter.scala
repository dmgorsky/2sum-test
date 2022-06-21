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
import scala.util.{Failure, Success, Try, Using}

class CsvRouter @Inject()(controller: HomeController,
                          action: DefaultActionBuilder,
                          parser: PlayBodyParsers,
                          solver: SumSolver,
                          implicit val exCtx: ExecutionContext) extends SimpleRouter with Logging with ErrorTypes {

  private def fileHandler: FilePartHandler[File] = {
    case fi@FileInfo(partName, filename, contentType, _) =>
      val tempFile = {
        val f = new java.io.File("./target/file-upload-data/uploads", UUID.randomUUID().toString).getAbsoluteFile
        f.getParentFile.mkdirs()
        f
      }
      val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(tempFile.toPath)
      val acc: Accumulator[ByteString, IOResult] = Accumulator(sink)
      acc.map {
        case akka.stream.IOResult(_, _) =>
          FilePart(partName, filename, contentType, tempFile)
      }

  }

  override def routes: Routes = {
    case POST(p"/") =>
      action(parser.multipartFormData(fileHandler)) { request =>
        Try {
          val files = request.body.files.map(_.ref.getAbsolutePath)
          val solution = files map { file =>
            logger.info(file)
            val fileHandler = scala.io.Source.fromFile(file)
            val contents = fileHandler.getLines()
            val nums = contents.toSeq.map(row => row.split(",").map(_.strip.toInt).toSeq)
            fileHandler.close()
            solver.solveLocalSum(nums)
          }
          solution.head // assignment should have 1 file

        } match {
          case Failure(exception) => Results.NotAcceptable(encodeError(exception))
          case Success(value) => Results.Ok(Json.toJson(value))
        }
      }
  }
}
