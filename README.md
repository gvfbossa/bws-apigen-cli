# HOW TO BUILD THE CLI ON LINUX #

## 1- The build proccess
On the project's main folder on Terminal (or PowerShell), run:  
`mvn clean package`

## 2- Make sure you are using GraalVM to use native-image by following the steps bellow:
You MUST have GraalVM
- `export JAVA_HOME=/etc/graalvm-jdk-21/`
- `export PATH=$JAVA_HOME/bin:$PATH`
- `java -version`

## 3- Pre configure your .deb build by running the following command: 
`native-image \`  
`--enable-url-protocols=http,https \`  
`--enable-http \`  
`--enable-https \`  
`--report-unsupported-elements-at-runtime \`  
`-jar target/bws-apigen-cli-0.0.1-SNAPSHOT.jar`

## 4- Prepare the build proccess by running the following command:
`mkdir -p bws-apigen/DEBIAN && mkdir -p bws-apigen/usr/local/bin && cp bws-apigen-cli-0.0.1-SNAPSHOT bws-apigen/usr/local/bin/bws-apigen && chmod +x bws-apigen/usr/local/bin/bws-apigen && nano bws-apigen/DEBIAN/control`  

Put this on the file contents:
`Package: bws-apigen`  
`Version: 1.0.0`  
`Section: base`  
`Priority: optional`  
`Architecture: amd64`  
`Maintainer: Bossa Web Solutions`  
`Description: CLI to generate Spring Boot REST APIs from @Entity classes`  

## 5 - Build your .deb package by running the following command:
`dpkg-deb --build bws-apigen && sudo dpkg -i bws-apigen.deb`

# HOW TO BUILD THE CLI ON WINDOWS #
--Pendente...