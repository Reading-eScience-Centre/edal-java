name := "EDAL testing with Simple Build Tool"

libraryDependencies += "uk.ac.rdg.resc" % "edal-cdm" % "0.2.0-SNAPSHOT"

// You may need to change this to point to your local Maven repository,
// or perhaps the EDAL snapshots repository
resolvers += "Local Maven Repository" at Path.userHome.asURL + "/.m2/repository"