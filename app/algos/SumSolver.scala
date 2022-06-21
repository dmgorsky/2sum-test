package algos

class SumSolver {
  type FitPair = (Int, Int)

  private def findPairs(sumToFind: Int, arr: Seq[Int]) = {
    arr.combinations(2).collect {
      case seq if seq.sum == sumToFind => (seq.head, seq.tail.head)
    }.toList.map(s => if (s._1 < s._2) s else s.swap)
  }

  def solveSum(sumToFind: Int, candidates: Seq[Seq[Int]]): Seq[Seq[FitPair]] = {
    candidates map { arr =>
      findPairs(sumToFind, arr)
    }
  }


  def solveLocalSum(candidates: Seq[Seq[Int]]): Seq[Seq[FitPair]] = {
    candidates map { arr =>
      val sumToFind = arr.head
      findPairs(sumToFind, arr.tail)
    }
  }
}
