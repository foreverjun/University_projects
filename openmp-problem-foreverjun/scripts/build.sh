#!/bin/sh

gfortran -o single ./src/initialization_matrix.f95 ./src/single_thr_algo.f95 -fopenmp -O3
gfortran -o parallel ./src/initialization_matrix.f95 ./src/parallel_blocks.f95 -fopenmp -O3
