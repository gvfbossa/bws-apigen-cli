# HOW TO BUILD THE CLI #


1- run: mvn clean package

2- Make sure you are using GraalVM to use native-image
- export JAVA_HOME=/etc/graalvm-jdk-21/
- export PATH=$JAVA_HOME/bin:$PATH
- java -version

3- run: native-image \
--enable-url-protocols=http,https \
--enable-http \
--enable-https \
--report-unsupported-elements-at-runtime \
-jar target/bws-apigen-cli-0.0.1-SNAPSHOT.jar

4- run the following commands:
- mkdir -p bws-apigen/DEBIAN
- mkdir -p bws-apigen/usr/local/bin
- cp bws-apigen-cli-0.0.1-SNAPSHOT bws-apigen/usr/local/bin/bws-apigen && chmod +x bws-apigen/usr/local/bin/bws-apigen
- nano bws-apigen/DEBIAN/control

put this on the file contents:

Package: bws-apigen

Version: 1.0.0

Section: base

Priority: optional

Architecture: amd64

Maintainer: Bossa Web Solutions

Description: CLI to generate Spring Boot REST APIs from @Entity classes


- dpkg-deb --build bws-apigen && sudo dpkg -i bws-apigen.deb

5- use it on a Java Spring folder by running: bws-apigen generate
