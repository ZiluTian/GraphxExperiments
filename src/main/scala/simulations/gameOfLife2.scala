
package simulations

import org.apache.spark.graphx._
import scala.util.Random

object GameOfLife2 { 
  import Simulate.sc
  
  def main(args: Array[String]): Unit = {
    val edgeListFile: String = args(0)
    val cfreq: Int = args(1).toInt
    val totalIterations: Int = 200
    val t1 = System.currentTimeMillis()

    // $example on$
    // Msg(A): List[Int]
    // VD: Int
    // ED: List[Int]
    // A graph with edge attributes containing distances
    val graph: Graph[Int, List[Int]] = GraphLoader.edgeListFile(sc, edgeListFile).mapVertices((_, _) => if (Random.nextBoolean()){ 1 } else { 0 }).mapEdges(e => List(e.attr))
    // Initialize the graph such that each vertex is either alive or dead.
    // val initialGraph = graph.mapVertices((id, _) => if (Random.nextBoolean()){1} else {0})
    val gol = graph.pregel[List[Int]](List(0), maxIterations = totalIterations)(
      (_, alive, receivedMsgs) => {
        // println("Total received messages " + receivedMsgs.size)
        // println("Total received messages are " + receivedMsgs)
        if (receivedMsgs.size == 8) {
            val aliveNeighbors = receivedMsgs.filter(i => i == 1).size
            if ((alive==1) && ((aliveNeighbors > 3*cfreq) || (aliveNeighbors < 2*cfreq))) {
                0
            } else if ((alive==0) && (aliveNeighbors < 3*cfreq) && (aliveNeighbors > 2*cfreq)){
                1
            } else {
                alive
            }
        } else {  // drop the initialization message
            alive
        }}, // Vertex Program
      triplet => {  // Send Message
          Range(0, cfreq).map(_ => (triplet.dstId, List(triplet.srcAttr))).toIterator
          // Iterator(Range(0, cfreq).map(i => (triplet.dstId, List(triplet.srcAttr))))
        },
      (a, b) => a ::: b // Merge Message
    )
    gol.vertices.count
    // println(gol.vertices.collect.mkString("\n"))
    // $example off$
    println(f"Average time per iteration ${(System.currentTimeMillis() - t1)/totalIterations}")
    
    sc.stop()
  }
}