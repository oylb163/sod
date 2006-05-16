#!/bin/sh
run(){
    mkdir $1
    cd $1
    echo $1
    sod $2 > ../$1.out
    cd ../
}
runrecipe(){
    run $1 "-f ${SOD_HOME}/recipes/tutorial/$1.xml"
}

generateDemoDoc(){
mkdir documentation/demo/
head -10 demo.out > documentation/demo/head
echo '...' >> documentation/demo/head
head -1 demo.out > documentation/demo/events
grep Channel demo.out | head -1 > documentation/demo/channels
grep seismograms demo.out | head -1 > documentation/demo/seismograms
}

generateNetworkDoc(){
DOCDIR=documentation/network
mkdir $DOCDIR
cat simpleNetwork.out | wc -l > ${DOCDIR}/all
cat subsettingNetwork.out | wc -l > ${DOCDIR}/subset
cat network.out | wc -l > ${DOCDIR}/completeSubset
}

VERSION=2.1.2rc1
DIR=sod-${VERSION}
sod.py -o . --tar
tar xzf ${DIR}.tar.gz
export SOD_HOME=`pwd`/${DIR}
echo $SOD_HOME
export PATH=${SOD_HOME}/bin:$PATH

run demo -demo
runrecipe waveform
runrecipe network
runrecipe event
runrecipe simpleNetwork
runrecipe subsettingNetwork
runrecipe simpleEvent
runrecipe simpleWaveform
runrecipe subsettingWaveform

mkdir documentation
generateDemoDoc
generateNetworkDoc
