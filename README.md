This project contains the source code for reproducing the experimental results of GraphX in our SIGMOD paper 
*"Generalizing Bulk-Synchronous Parallel Processing: From Data to Threads and Agent-Based Simulations"*.

### Workloads
The workloads contain three simulation applications: population dynamics (gameOfLife), economics (stockMarket), and epidemics. 
The source code for implementing each of these simulations in GraphX as a vertex program can be found in `/src/main/scala/simulations/`.

### Benchmark
Our paper included the following benchmark experiments in GraphX: tuning, scaleup, scaleout, communication frequency, and computation interval. For each of the benchmark, you can find its corresponding configuration in `conf/`, which is necessary for launching a benchmark. The input graphs are not included here due to their sheer size, but you should be able to easily generate them based on our description in the paper.

The driver script for starting a benchmark is `/bin/bench.py`. Prior to running a benchmark, you need to compile and assemble the vertex programs. Our benchmark script automates this for you by passing `-a`. In short, to compile and assemble the vertex program and then running a benchmark named {test}, you should enter the following command (tested with python3.9, but you can easily adjust the driver script to use other versions of Python):

```python3 bin/bench.py -a -t test```.

### Remark
Before running a benchmark, you should already have the Spark cluster up and running. Our experiments are done using Spark 3.3.0, tested on CentOS 7 using a cluster of Xeon servers. Each server has 24 cores and 220 GB RAM. For the best performance, you need to tune the configuration of Spark (this is *not* part of our benchmark configuration). You can find some of our tuning setup in `/src/main/scala/simulations/Simulate.scala`. 