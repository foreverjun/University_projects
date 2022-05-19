program single_thr_programm

   use init
   use omp_lib
   implicit none 

   real*8, dimension (:,:), allocatable :: array    
   real*8 :: eps,h,dmax,dm,temp    
   integer :: i, j, N,k
   double precision start_time, end_time, time 

   character(len=32) :: arg, name_of_file


   call get_command_argument(1,arg)
   read (unit=arg,fmt=*) N
   call get_command_argument(2,arg)
   read (unit=arg,fmt=*) eps
   call get_command_argument(3,name_of_file)

   h = 1/real((N+1),8)     
         
   allocate ( array(N+2,N+2) )
   call init_matrix(array,N,h)

       k = 0
       start_time = omp_get_wtime()
       do
         dmax = 0
          do i = 2,N+1
            do j=2,N+1
               temp = array(i,j)
               array(i,j) = 0.25*(array(i-1,j)+array(i+1,j)+array(i,j-1)+array(i,j+1))
               dm = abs(temp-array(i,j))
               if(dmax < dm) dmax = dm
            enddo
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
   print *,k,time
   close(1)

   deallocate (array)  
end program single_thr_programm

