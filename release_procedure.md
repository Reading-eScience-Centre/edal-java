Release Procedure for EDAL
==========================

Prerequisites
-------------

### GPG
You should have gpg and gpg-agent installed.  You will need to have imported the ReSC key, which can be found on the ReSC drive at ReSC_software/resc_private_gpg.secret.

This can be imported using the command:
```
gpg --allow-secret-key-import --import <ReSC_drive>/ReSC_software/resc_private_gpg.secret
```

The key is passphrase-protected with the standard ReSC password.

### Maven settings
Maven needs to be installed to build the software, but to release it needs some specific settings added to ~/.m2/settings.xml.  This should contain the following:
```xml
<settings>
  <servers>
    <server>
      <id>edal-snapshots</id>
      <username>resc</username>
      <password>the_resc_password</password>
    </server>
    <server>
      <id>edal-java-release</id>
      <username>resc</username>
      <password>the_resc_password</password>
    </server>
  </servers>
</settings>
```

Release Procedure
-----------------

Once all code is ready to be released, and all tests pass, the following steps should be taken:

### Create a branch to do the release on:
```
git checkout -b release-VERSION
```

### Set the release versions in the pom files:
Maven should automatically pick the correct version to relase to - it will be the current development version without the "-SNAPSHOT" suffix

```
mvn versions:set
cd ncwms
mvn versions:set
cd ..
```

### Build the software:
```
mvn clean install
mvn javadoc:aggregate
[build other site docs]
```

### Commit and tag the release:
```
git commit -a -m "Update pom files for release VERSION"
git tag edal-VERSION
```

### Deploy to sonatype:
```
mvn deploy -P release
```
Upon successful completion of this stage, log into [sonatype](http://oss.sonatype.org) with the username "resc", click the "Staging Repositories" link on the left, and scroll down to find the uk.ac.rdg.resc entry.  Select it and then click the "Release" button and enter a short comment.  This will allow the releases to be synchronised to Maven central, and an automated email will be sent once the process is complete.

### Merge the release branch into master:
```
git checkout master
git merge release-VERSION
git push origin master
git push --tags
```

### Create a release on github:
Go to [the project page on github](https://github.com/Reading-eScience-Centre/edal-java) and click the "Releases" link.  Go to the edal-VERSION release and click the "Edit tag" button.  You should now fill in the appropriate boxes and upload the ncWMS.war and the ncWMS-standalone.jar files as binary attachments.

### Upload the site documents

TODO This still needs a description.  The basic idea will be to use the maven-pdf-plugin to generate PDFs for the user guides etc, as well as HTML(?).  See http://maven.40175.n5.nabble.com/Maven-PDF-support-td5778995.html for PDF generation.

Once it's all generated, use the github site plugin to commit to the gh-pages branch (https://github.github.com/maven-plugins/site-plugin/quickstart.html) then push it to the origin.  This should ideally all be attached to the site/site-deploy goals, but we want to configure maven to ignore actual site generation first.

Prepare for next development iteration
--------------------------------------
### Move to the develop branch and update:
```
git checkout develop
git merge master
```

### Set the snapshot versions:
You should update the versions to the next snapshot version.  Usually this will be an update to the minor number.  For example if the last release was 1.2, the next version would be 1.3-SNAPSHOT

```
mvn versions:set
cd ncwms
mvn versions:set
cd ..
```

### Remove the backup files created by setting versions:
```
find ./ -iname "*.versionsBackup" | xargs rm
```

### Commit:
```
git commit -a -m "Prepare for next development version"
git push origin develop
```
