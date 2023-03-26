# -*- coding: utf-8 -*-
import json
import subprocess
import os
import sys
from datetime import datetime

def run(cfreqs, cintervals):
    # Comm. freq experiments
    for cfreq in cfreqs:
        for cint in cintervals:
            for experiment in EXPERIMENTS:
                for input_file in config[experiment]['input_graph']:
                    print(f"Running {experiment} communication frequency is {cfreq} computation interval is {cint}")
                    cp = config[experiment]["classpath"]
                    now = datetime.now()
                    current_time = now.strftime("%H%M%S")
                    log_file = open(f"{LOG_DIR}/{experiment}_cfreq{cfreq}_cint{cint}_{current_time}", 'a')
                    process = subprocess.run([f'{SPARK_HOME}/bin/spark-submit', '--master', SPARK_MASTER, '--executor-cores', str(SPARK_EXECUTOR_CORES), '--driver-memory', str(SPARK_DRIVER_MEM), '--executor-memory', str(SPARK_EXECUTOR_MEM), '--class', cp, '/root/GraphxExperiments/target/scala-2.12/GraphxExperiments-assembly-1.0-SNAPSHOT.jar', input_file, str(cfreq), str(1)], text=True, stdout=subprocess.PIPE, check=True)
                    print(process.stdout, file=log_file)
                    os.system('echo 3 > /proc/sys/vm/drop_caches')
                    log_file.flush()
                    log_file.close()

if (__name__ == "__main__"):
    assemble = False
    experiment = ""

    for i in range(1, len(sys.argv)):
        arg = sys.argv[i]
        if arg == '-t':
            experiment = sys.argv[i+1]
        elif arg == '-a':
            assemble = True
    
    if (experiment == ""):
        print("Please input what benchmark to run")
        exit(1)

    f = open(f"bin/conf.json")
    config = json.load(f)

    EXPERIMENTS = config['experiments']
    REPEAT = config['repeat']
    LOG_DIR = config['log_dir']
    CFRES = config['cfreqs']
    CINT = config['cinterval']

    SPARK_HOME=config['spark']['home']
    SPARK_MASTER=config['spark']['master']
    SPARK_EXECUTOR_CORES=config['spark']['executor_cores']
    SPARK_DRIVER_MEM=config['spark']['driver_mem']
    SPARK_EXECUTOR_MEM=config['spark']['executor_mem']

    subprocess.run(['mkdir', '-p', LOG_DIR], text=True, stdout=subprocess.PIPE, check=True)
    
    if (assemble):
        subprocess.run(['sbt', '-DsparkDeploy=Cluster', 'assembly'], text=True, stdout=subprocess.PIPE, check=True)
        print(f"Assemble jar file for deployment completed")

    if (experiment == "cfreq"):
        run(CFRES, [1])
    elif (experiment == "cint"):
        run([1], CINT)

