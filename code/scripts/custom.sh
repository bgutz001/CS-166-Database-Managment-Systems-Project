#!/bin/bash

./initdb.sh
./start.sh 7890
./createdb.sh 7890
./createtb.sh 7890

