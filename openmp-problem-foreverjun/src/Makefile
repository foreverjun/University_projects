OPTIONS = -O3 -fopenmp
CC = gfortran

all: parallel_blocks single_thr_algo

parallel_blocks : initialization_matrix.o parallel_blocks.o
	$(CC) initialization_matrix.o parallel_blocks.o -o $@ $(OPTIONS)


single_thr_algo : initialization_matrix.o single_thr_algo.o
	$(CC) initialization_matrix.o single_thr_algo.o -o $@ $(OPTIONS)


initialization_matrix.o : initialization_matrix.f95
	$(CC) -c initialization_matrix.f95

parallel_blocks.o : parallel_blocks.f95
	$(CC) -c parallel_blocks.f95 $(OPTIONS)

single_thr_algo.o : single_thr_algo.f95
	$(CC) -c single_thr_algo.f95 $(OPTIONS)

clean :
	rm -rf	*.o init.mod parallel_blocks single_thr_algo

clean_out:
	rm -rf *.out

