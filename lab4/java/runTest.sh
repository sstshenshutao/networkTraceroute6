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

function uploadFile(){
	for dir in "${testFileDir[@]}"
	do 
		scp -r ./$dir network:$workDir
	done
}
function runMake(){
	ssh $hostName $makeSyntax
}
function runTest(){
	#this is what i need to change
	ssh $hostName "cd ~/dir/;./asciiclient -m \"Helloworld\" \"6969696\" 2a00:4700:0:9:f::c"
	#==end this is what i need to change==
}
function uploadEnvironmentFile(){
	echo "====uploadEnvironmentFile====";
	for dir in "${environmentDir[@]}"
	do 
		scp -r ./$dir network:$workDir
	done
	echo "====uploadEnvironmentFile successful!====";
}
function showHelp(){
	echo 'how to use? \nfirst time use "sh ./runTest.sh $initStr" to upload Environment Files \nthen for every test, just use "sh ./runTest.sh"';
}
#this is what i need to change
hostName="network";
workDir="~/dir/";
#==end this is what i need to change==
environmentDir=(deps Makefile);
testFileDir=(src);
makeSyntax="cd $workDir;make";
testSyntax="cd $workDir;./asciiclient";
initStr="--init";
helpStrB="--help";
helpStrS="-h";
conbool="0";
zero="0";
	for arg in "$@"
  	do
     	if [ "$arg" == "$initStr" ];
     	then 
     		conbool="1";	
     		uploadEnvironmentFile
     	fi
     	if [ "$arg" == "$helpStrB" ] || [ "$arg" == "$helpStrS" ];
     	then 
     		conbool="1";
     		showHelp
     	fi
 	done
if [ "$conbool" == "$zero" ];
then 
	uploadFile
	# runTestWithPara "$*"
	runMake
	runTest
fi

