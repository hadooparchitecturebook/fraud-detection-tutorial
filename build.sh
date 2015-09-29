#!/bin/bash
mvn clean package
rm -rf webapp.zip
zip -r webapp webapp
