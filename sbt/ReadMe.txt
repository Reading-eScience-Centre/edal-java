Uses the Simple Build Tool (https://github.com/harrah/xsbt/wiki) to provide a command-line REPL interface to EDAL, mainly for scripting and testing purposes.  You'll need to install SBT (see the above Wiki link) before this will work for you, then:

1. Change into the directory containing this ReadMe file
2. Open a console window
3. Run "sbt"
   [The first time you run this will be slow because it will download a bunch of dependencies]
4. Enter "run" to run the test program (in test.scala) ...
5. ... or enter "console" to get a Scala REPL prompt.

This should download all the dependencies and add them to the classpath.  If the dependencies aren't found, look at build.sbt.
