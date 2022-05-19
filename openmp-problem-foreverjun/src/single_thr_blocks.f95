program single_thr_blocks

use init
use omp_lib
implicit none

   real*8, dimension (:,:), allocatable :: array
   real*8, dimension (:), allocatable :: dm
   real*8 :: eps,h,dmax,temp,d
   integer :: i, j, N, k, NB, nx, m,l, block_size,s,t
   double precision start_time, end_time, time

   character(len=32) :: arg,name_of_file

   call get_command_argument(1,arg)
   read (unit=arg,fmt=*) N
   call get_command_argument(2,arg)
   read (unit=arg,fmt=*) eps
   call get_command_argument(3,name_of_file)

   h = 1/real((N+1),8)
   NB = N/block_size
   k = 0   

         
   allocate ( array(N+2,N+2) )
   allocate (dm(NB))
   call init_matrix(array,N,h)
   do i = 1, NB
     dm(i) = 0
   enddo
   start_time = omp_get_wtime()
   do
     dmax = 0
     do nx = 1, NB
      dm(nx) = 0
     	do i = 1, nx
        j = nx +1 - i
     		do m = 2,block_size+1
           		do l=2,block_size+1
                s = (i-1)*block_size + m
                t = (j-1)*block_size + l
                temp = array(s,t)
                array(s,t) = 0.25*(array(s-1,t)+array(s+1,t)+array(s,t-1)+array(s,t+1))
                d = abs(array(s,t) - temp)
               	if(dm(i) < d) dm(i) = d
            	enddo
         	enddo
     	enddo
     enddo

     nx = NB - 1
     do while(nx>0)
        do i = NB - nx +1,NB
          j=2*NB-nx-i+1
        	do m = 2,block_size+1
           		do l=2,block_size+1
                s = (i-1)*block_size + m
                t = (j-1)*block_size + l
                temp = array(s,t)
                array(s,t) = 0.25*(array(s-1,t)+array(s+1,t)+array(s,t-1)+array(s,t+1))
                d= abs(temp-array(s,t))
               	if(dm(i) < d) dm(i) = d
            	enddo
         	enddo
        enddo
      nx = nx-1
     enddo

     do i = 1, NB
     	if (dmax<dm(i)) dmax = dm(i)
     enddo

     k = k+1
     if (.not.(dmax > eps)) exit
   enddo
   end_time = omp_get_wtime()
   time = end_time - start_time

   open (unit = 1,file = name_of_file)
   do i=1, N+2
    write(1,*) array(i,:)
   enddo
   print *, time,k
   close(1)

   deallocate (array)
   deallocate (dm)

end program single_thr_blocks