#!/bin/bash

BASEDIR=$(dirname "$0")
cd $BASEDIR

cd ../
ant ivy.retrieve
ant package

cp dist/geniuswiki.war ../docker/geniuswiki

cd docker/geniuswiki
docker build -t edgenius/geniuswiki:latest .
docker tag edgenius/geniuswiki:latest geniuswiki:latest

rm geniuswiki.war
