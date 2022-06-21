import algos.SumSolver
import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._

class TwoSumSolverSpec extends PlaySpec {
  val solver = new SumSolver()
  "2sum solver" should {

    "provide results for 1 sum to find and several parameter sets" in {
      val initialResult = solver.solveSum(7, Seq(Seq(11, -4, 3, 4, 3, 2), Seq(2, 5, 5, 3, 0, 1)))
      val solution = initialResult
        .flatten
        .map(s => if (s._1 < s._2) s else s.swap)

      initialResult should have size 2
      initialResult.head should have size 2

      solution should have size 3
      solution should contain (-4 -> 11)
      solution should contain (3 -> 4)
      solution should contain (2 -> 5)

    }

    "provide no results for non-covered sum" in {
      val initialResult = solver.solveSum(16, Seq(Seq(11, -4, 3, 4, 3, 2)))
      val solution = initialResult
        .flatten
        .map(s => if (s._1 < s._2) s else s.swap)

      initialResult should have size 1
      initialResult.head should have size 0

      solution should have size 0
    }
  }

}
