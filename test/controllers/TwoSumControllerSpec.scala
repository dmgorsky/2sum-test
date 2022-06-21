package controllers

import akka.util.ByteString
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.{FileBody, StringBody}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.http.{HeaderNames, Writeable}
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.test.Helpers._
import play.api.test._
import play.api.libs.json.{JsPath, Json}
import play.api.mvc.{Codec, MultipartFormData}
import play.api.mvc.MultipartFormData.{BadPart, FilePart}
import play.libs.Files.TemporaryFileCreator
import upload.ErrorTypes

import java.io.{ByteArrayOutputStream, File, PrintWriter}
import java.util.UUID
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.math.Ordering.Implicits.seqOrdering


/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class TwoSumControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with ScalaFutures with ErrorTypes {

  implicit def writeableOf_multiPartFormData(implicit codec: Codec): Writeable[MultipartFormData[TemporaryFile]] = {
    val builder = MultipartEntityBuilder.create().setBoundary("p@r1m@t4")

    def transform(multipart: MultipartFormData[TemporaryFile]): ByteString = {
      multipart.dataParts.foreach { part =>
        part._2.foreach { p2 =>
          builder.addPart(part._1, new StringBody(p2, ContentType.create("text/csv", "UTF-8")))
        }
      }
      multipart.files.foreach { file =>
        val part = new FileBody(file.ref.path.toFile, ContentType.create(file.contentType.getOrElse("application/octet-stream")), file.filename)
        builder.addPart(file.key, part)
      }

      val outputStream = new ByteArrayOutputStream
      builder.build.writeTo(outputStream)
      ByteString(outputStream.toByteArray)
    }

    new Writeable[MultipartFormData[TemporaryFile]](transform, Some(builder.build.getContentType.getValue))
  }

  "/upload controller" should {
    "fail on invalid input" in {
      val notJsonNumStringBody = "[[\"s11\", -4, 3, 4, 3, 2], [2, 5, 5, 3, 0, 1]]"
      val request = FakeRequest(POST, "/upload/42").withJsonBody(Json.parse(notJsonNumStringBody))
      val solver = route(app, request).get

      status(solver) mustBe NOT_ACCEPTABLE
      contentAsJson(solver).as[ErrorDesc].code must include("wrong.input.type")

    }

    "respond with 2sum problem solution" in {
      val jsonStringBody = "[[11, -4, 3, 4, 3, 2], [2, 5, 5, 3, 0, 1]]"
      val request = FakeRequest(POST, "/upload/7").withJsonBody(Json.parse(jsonStringBody))
      val result = route(app, request).get

      status(result) mustBe OK
      val resultNums = contentAsJson(result).as[Seq[Seq[(Int, Int)]]]
      resultNums must have size 2
      an [Throwable] should be thrownBy (contentAsJson(result).as[ErrorDesc])
    }

  }

  "/upload/file controller" should {
    "fail on invalid input" in {
      val notJsonNumStringBody = "[[\"s11\", -4, 3, 4, 3, 2], [2, 5, 5, 3, 0, 1]]"
      val tempFile: TemporaryFile = SingletonTemporaryFileCreator.create(s"tmp${UUID.randomUUID().toString}", ".csv")
      tempFile.deleteOnExit()
      val writer = new PrintWriter(tempFile.path.toFile)
      writer.write(notJsonNumStringBody)
      writer.close()
      val part = FilePart[TemporaryFile](key = "csv", filename = "upload1.csv", contentType = Some("text/csv"), ref = tempFile)
      val formData = MultipartFormData(dataParts = Map.empty[String, Seq[String]], files = Seq[FilePart[TemporaryFile]](part), badParts = Seq.empty[BadPart])


      val request = FakeRequest[MultipartFormData[TemporaryFile]](POST, "/upload/file/42", play.api.mvc.Headers(HeaderNames.CONTENT_TYPE -> "multipart/form-data"), formData)
        .withHeaders(FakeHeaders())
        .withMultipartFormDataBody(formData)

      val solver = route(app, request).get

      status(solver) mustBe NOT_ACCEPTABLE
      contentAsJson(solver).as[ErrorDesc].code must include("wrong.input.type")

    }
    "solve correct input" in {
      val notJsonNumStringBody = "[[11, -4, 3, 4, 3, 2], [2, 5, 5, 3, 0, 1]]"
      val tempFile: TemporaryFile = SingletonTemporaryFileCreator.create(s"tmp${UUID.randomUUID().toString}", ".csv")
      tempFile.deleteOnExit()
      val writer = new PrintWriter(tempFile.path.toFile)
      writer.write(notJsonNumStringBody)
      writer.close()
      val part = FilePart[TemporaryFile](key = "csv", filename = "upload1.csv", contentType = Some("text/csv"), ref = tempFile)
      val formData = MultipartFormData(dataParts = Map.empty[String, Seq[String]], files = Seq[FilePart[TemporaryFile]](part), badParts = Seq.empty[BadPart])


      val request = FakeRequest[MultipartFormData[TemporaryFile]](POST, "/upload/file/7", play.api.mvc.Headers(HeaderNames.CONTENT_TYPE -> "multipart/form-data"), formData)
        .withHeaders(FakeHeaders())
        .withMultipartFormDataBody(formData)

      val solution = route(app, request).get

      status(solution) mustBe OK
      val resultNums = contentAsJson(solution).as[Seq[Seq[(Int, Int)]]]
      resultNums must have size 2
      resultNums.min.min mustBe(-4, 11)

      an [Throwable] should be thrownBy (contentAsJson(solution).as[ErrorDesc])
    }
  }

  "/upload/csv controller" should {
    "fail on invalid input" in {
      val notJsonNumStringBody = "s11, -4, 3, 4, 3, 2\n2, 5, 5, 3, 0, 1"
      val tempFile: TemporaryFile = SingletonTemporaryFileCreator.create(s"tmp${UUID.randomUUID().toString}", ".csv")
      tempFile.deleteOnExit()
      val writer = new PrintWriter(tempFile.path.toFile)
      writer.write(notJsonNumStringBody)
      writer.close()
      val part = FilePart[TemporaryFile](key = "csv", filename = "upload1.csv", contentType = Some("text/csv"), ref = tempFile)
      val formData = MultipartFormData(dataParts = Map.empty[String, Seq[String]], files = Seq[FilePart[TemporaryFile]](part), badParts = Seq.empty[BadPart])


      val request = FakeRequest[MultipartFormData[TemporaryFile]](POST, "/upload/csv", play.api.mvc.Headers(HeaderNames.CONTENT_TYPE -> "multipart/form-data"), formData)
        .withHeaders(FakeHeaders())
        .withMultipartFormDataBody(formData)

      val solver = route(app, request).get

      status(solver) mustBe NOT_ACCEPTABLE
      contentAsJson(solver).as[ErrorDesc].code must include("wrong.input.type")

    }
    "solve correct input" in {
      val notJsonNumStringBody = "7, 11, -4, 3, 4, 3, 2\n7, 2, 5, 5, 3, 0, 1"
      val tempFile: TemporaryFile = SingletonTemporaryFileCreator.create(s"tmp${UUID.randomUUID().toString}", ".csv")
      tempFile.deleteOnExit()
      val writer = new PrintWriter(tempFile.path.toFile)
      writer.write(notJsonNumStringBody)
      writer.close()
      val part = FilePart[TemporaryFile](key = "csv", filename = "upload1.csv", contentType = Some("text/csv"), ref = tempFile)
      val formData = MultipartFormData(dataParts = Map.empty[String, Seq[String]], files = Seq[FilePart[TemporaryFile]](part), badParts = Seq.empty[BadPart])


      val request = FakeRequest[MultipartFormData[TemporaryFile]](POST, "/upload/csv", play.api.mvc.Headers(HeaderNames.CONTENT_TYPE -> "multipart/form-data"), formData)
        .withHeaders(FakeHeaders())
        .withMultipartFormDataBody(formData)

      val solution = route(app, request).get

      status(solution) mustBe OK
      val resultNums = contentAsJson(solution).as[Seq[Seq[(Int, Int)]]]
      resultNums must have size 2
      resultNums.min.min mustBe(-4, 11)
      an [Throwable] should be thrownBy (contentAsJson(solution).as[ErrorDesc])
    }
  }


}
