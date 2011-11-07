#!/bin/bash

project_dir="`dirname $0`/.."

version=( `cut -d"=" -s -f2  $project_dir/build_info.properties | xargs` )

echo "<project> 
         <modelVersion>4.0.0</modelVersion>
         <groupId>cudl</groupId>
         <artifactId>cudl</artifactId>
         <version>${version[0]}.${version[1]}.${version[2]}</version>
      </project>" > "$project_dir/target/pom.xml"


