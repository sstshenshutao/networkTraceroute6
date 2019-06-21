#!/bin/bash
# ----------------------------------------------------
# Filename:	runTest
# version:	1.0
# Date:		2019/06/18
# Author:	shenshutao
# Email: 	sstbage@gmail.com
# Website:	cybertaotao.com
# Description:	run Java Test on the virtual mashine of grnvs
# Copyright:	2019(c)cybertaotao.com
# ----------------------------------------------------
dir='/Users/shenshutao/Desktop/u0847/';
sourceDir="$PWD"
# echo $sourceDir
cd $dir;
git checkout master;
cp -r $sourceDir"/src/" $dir"assignment3/";
for arg in "$@"
  	do
		git add *
		git commit -m "$arg"
		git push
		git checkout result
		git pull
 	done
 

