
#!/bin/sh
touch = test.txt

i=0
echo "Номер     N       Итерации        время" >> test.txt
for var in 128 256 512 1024
do
	i=$((i+1))
	echo "Single" >> test.txt
	echo -n "$i	$var	" >> test.txt
	./single $var 0.1 "$var.outsingle" >> test.txt
	echo "Parallel" >> test.txt
	echo -n "$i     $var    " >> test.txt
	./parallel $var 64 0.1 "$var.outparallel" >> test.txt

	if cmp -s $var.outsingle $var.outparallel ; then
		echo "Многопоточный и однопоточный алгоритмы дают одинаковый результат : эксперимент $i"
	else
		echo "Многопоточный и одноподочный алгоритмы дали разные результаты : эксперимент $i"
	fi
done