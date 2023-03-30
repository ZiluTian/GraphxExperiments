
package simulations

import org.apache.spark.graphx._
import scala.util.Random

// With a clock vertex to track idle interval
object GameOfLife3 { 
  import Simulate.sc

  def main(args: Array[String]): Unit = {
    val edgeListFile: String = args(0)
    val cfreq: Int = args(1).toInt
    val interval: Int = args(2).toInt
    val totalIterations: Int = 200
    val t1 = System.currentTimeMillis()

    // $example on$
    // Msg(A): List[Int]
    // VD: Int
    // ED: List[Int]
    // A graph with edge attributes containing distances
    val graph: Graph[List[Int], List[Int]] = GraphLoader.edgeListFile(sc, edgeListFile)
      .mapVertices((_, _) => if (Random.nextBoolean()){ List(1, interval) } else { List(0, interval) })
      .mapEdges(_ => List(0))
    // Initialize the graph such that each vertex is either alive or dead.
    // val initialGraph = graph.mapVertices((id, _) => if (Random.nextBoolean()){1} else {0})
    val gol = graph.pregel(List[Int](0), maxIterations = totalIterations)(
      (id, state, receivedMsgs) => {
          var alive: Int = state(0)
          var idleCountDown: Int = state(1)

          if (id != 0) {
            if (idleCountDown > 1) {
              idleCountDown -= 1
            } else {
              // println("Total received messages " + receivedMsgs.size)
              if (receivedMsgs.size == 8) {
                val aliveNeighbors = receivedMsgs.filter(i => i == 1).size
                if ((alive==1) && ((aliveNeighbors > 3*cfreq) || (aliveNeighbors < 2*cfreq))) {
                    alive = 0
                } else if ((alive==0) && (aliveNeighbors < 3*cfreq) && (aliveNeighbors > 2*cfreq)){
                    alive = 1
                } 
              } 
              idleCountDown = interval
            }
          } 
          List(alive, idleCountDown)
        }, // Vertex Program
      triplet => {  // Send Message
        if (triplet.srcId == 0) {
          Iterator((triplet.dstId, List(0)))
        } else {
          val idleCountDown = triplet.srcAttr(1)
          if (idleCountDown <= 1) {
            Range(0, cfreq).map(_ => (triplet.dstId, List(triplet.srcAttr(0)))).toIterator
          } else {
            Iterator.empty
          }
        }
        },
      (a, b) => a ::: b // Merge Message
    )
    gol.vertices.collect
    // println(gol.vertices.collect.mkString("\n"))
    // $example off$
    println(f"Average time per iteration ${(System.currentTimeMillis() - t1)/totalIterations}")
    sc.stop()
  }
}