
package simulations

import org.apache.spark.graphx._
import scala.util.Random

object GameOfLife { 
  import Simulate.sc

  def main(args: Array[String]): Unit = {
    val t1 = System.currentTimeMillis()
    val totalIterations: Int = 200
    val edgeListFile: String = args(0)
    
    // $example on$
    // A graph with edge attributes containing distances
    val graph: Graph[Int, Int] = GraphLoader.edgeListFile(sc, edgeListFile)
    // Initialize the graph such that each vertex is either alive or dead.
    val initialGraph = graph.mapVertices((id, _) =>
        if (Random.nextBoolean()){1} else {0})
    val gol = initialGraph.pregel(0, maxIterations = totalIterations)(
      (id, alive, aliveNeighbors) => {
        if ((alive==1) && (aliveNeighbors > 3 || aliveNeighbors < 2)) {
            0
        } else {
            1
        }}, // Vertex Program
      triplet => {  // Send Message
          Iterator((triplet.dstId, triplet.attr))
        },
      (a, b) => a + b // Merge Message
    )
    gol.vertices.collect
    // println(gol.vertices.collect.mkString("\n"))
    println(f"Average time per iteration ${(System.currentTimeMillis() - t1)/totalIterations}")
    // $example off$
    sc.stop()
  }
}