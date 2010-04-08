REM can't use -javasource 1.5 due to multiple definitions of getPosList():
REM see http://mail-archives.apache.org/mod_mbox/xmlbeans-user/200511.mbox/%3C437D9E0C.3010305@gmx.de%3E

REM Must set JAVA_HOME before running


REM compile the schemas into Java source files and build class files and JAR
\java_libs\xmlbeans-2.4.0\bin\scomp csmlDataset.xsd ^
    -dl -nopvr -javasource 1.4 ^
    -compiler "%JAVA_HOME%\bin\javac.exe" ^
    -ms 256m -mx 512m ^
    -src .\generated-sources ^
    -out csmlbeans-2.0.0.jar

REM Generate javadoc
REM warning: takes a long time!
"%JAVA_HOME%\bin\javadoc" -sourcepath generated-sources ^
    -J-Xms256m -J-Xmx512m ^
    -link http://xmlbeans.apache.org/docs/2.4.0/reference/index.html ^
    -d javadoc ^
    -subpackages uk:net:org

REM Generate JAR file of sources
"%JAVA_HOME%\bin\jar" -cf csmlbeans-2.0.0-sources.jar -C generated-sources .

REM Generate JAR file of javadoc
"%JAVA_HOME%\bin\jar" -cf csmlbeans-2.0.0-api.jar -C javadoc .

REM install the library, sources and javadoc in the local maven repository
REM for some reason I can't get this to work in a single command

mvn install:install-file -Dfile=csmlbeans-2.0.0.jar ^
                         -DpomFile=csmlbeans-2.0.0.pom ^
                         -Dpackaging=jar ^
                         -DcreateChecksum=true

mvn install:install-file -Dfile=csmlbeans-2.0.0-sources.jar ^
                         -DpomFile=csmlbeans-2.0.0.pom ^
                         -Dpackaging=jar ^
                         -Dclassifier=sources ^
                         -DcreateChecksum=true

mvn install:install-file -Dfile=csmlbeans-2.0.0-api.jar ^
                         -DpomFile=csmlbeans-2.0.0.pom ^
                         -Dpackaging=jar ^
                         -Dclassifier=javadoc ^
                         -DcreateChecksum=true