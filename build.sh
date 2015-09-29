#!/bin/bash
mvn package
rm -rf webapp.zip
zip -r webapp webapp
