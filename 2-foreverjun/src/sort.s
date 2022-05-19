	.file	"sort.c"
	.text
	.p2align 4
	.globl	array_sorting
	.type	array_sorting, @function
array_sorting:
.LFB0:
	.cfi_startproc
	endbr64
	testl	%edi, %edi
	jle	.L1
	leal	-1(%rdi), %r8d
	testl	%r8d, %r8d
	jle	.L1
	xorl	%edi, %edi
	movslq	%r8d, %r9
	.p2align 4,,10
	.p2align 3
.L5:
	movq	%r9, %rax
	.p2align 4,,10
	.p2align 3
.L4:
	movq	-8(%rsi,%rax,8), %rdx
	movq	(%rsi,%rax,8), %rcx
	cmpq	%rcx, %rdx
	jle	.L7
	movslq	%edx, %rdx
	movq	%rcx, -8(%rsi,%rax,8)
	movq	%rdx, (%rsi,%rax,8)
.L7:
	subq	$1, %rax
	cmpl	%eax, %edi
	jl	.L4
	addl	$1, %edi
	cmpl	%r8d, %edi
	jl	.L5
	ret
	.p2align 4,,10
	.p2align 3
.L1:
	ret
	.cfi_endproc
.LFE0:
	.size	array_sorting, .-array_sorting
	.ident	"GCC: (Ubuntu 9.3.0-17ubuntu1~20.04) 9.3.0"
	.section	.note.GNU-stack,"",@progbits
	.section	.note.gnu.property,"a"
	.align 8
	.long	 1f - 0f
	.long	 4f - 1f
	.long	 5
0:
	.string	 "GNU"
1:
	.align 8
	.long	 0xc0000002
	.long	 3f - 2f
2:
	.long	 0x3
3:
	.align 8
4:
