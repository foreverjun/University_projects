module init
implicit none
contains
    
   subroutine init_matrix(array,N,h)
      implicit none

      real*8, intent(in) :: h
      real*8 :: rand
      real*8, dimension (:,:) :: array
      integer :: i, j, k, N

      do k = 1, N + 2
         array(1,k) = -100+200*h*real((k-1),8)
         array(N+3-k,1) = 100 -200*h*real((k-1),8)
         array(N+2,k) = 100-200*h*real((k-1),8)
         array(N+3-k,N+2) = -100 +200*h*real((k-1),8)
      enddo

      do i = 2, N + 1           
         do j = 2, N + 1                              
            array(i,j) = 0
         end do      
      end do
   end subroutine init_matrix
end module init