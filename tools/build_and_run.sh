#! /usr/bin/env bash

ant clean
ant debug
./tools/deploy.sh
./tools/log.sh
