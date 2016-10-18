# EDAL Usage

EDAL is split into a number of different modules for dealing with different tasks.  With the exception of `edal-examples` (which contains the example code used in this guide) all modules are available on the [Central Maven Repository](http://search.maven.org/).  This section gives details of what each module does, and how into include it in your own Maven project.

## EDAL Common
The `edal-common` module contains the core data model used in EDAL, as well as in-memory implementations of this data model, common utility methods, and exceptions.

Maven dependency:
```
<dependency>
    <groupId>uk.ac.rdg.resc</groupId>
    <artifactId>edal-common</artifactId>
    <version>[insert required version]</version>
</dependency>
```

## EDAL Graphics
The `edal-graphics` module contains code for generating images from the core EDAL data types. This includes map images, as well as timeseries, vertical profile, and vertical section charts. Additionally there are custom SLD (Styled Layer Descriptor) handlers to allow for the precise specification of how to assemble a map image, allowing arbitrarily complex plotting of multiple simultaneous data layers.

This module depends on the `edal-common` module.

Maven dependency:
```
<dependency>
    <groupId>uk.ac.rdg.resc</groupId>
    <artifactId>edal-graphics</artifactId>
    <version>[insert required version]</version>
</dependency>
```

## EDAL CDM
The `edal-cdm` module uses the Unidata NetCDF-Java libraries to read data into the core EDAL data model. It reads CF-compliant gridded NetCDF files as well as OPeNDAP, GRIB, and several other formats (see [here](http://www.unidata.ucar.edu/software/thredds/current/netcdf-java/reference/formats/FileTypes.html) for a list). It also includes a data reader capable of reading the UK Met Office [EN3](http://www.metoffice.gov.uk/hadobs/en3/) and [EN4](http://www.metoffice.gov.uk/hadobs/en4/) in-situ datasets.

This modules depends on the `edal-common` module.

Maven dependency:
```
<dependency>
    <groupId>uk.ac.rdg.resc</groupId>
    <artifactId>edal-cdm</artifactId>
    <version>[insert required version]</version>
</dependency>
```

## EDAL XML Catalogue
The `edal-xml-catalogue` module contains an implementation of a data catalogue in an XML format. This allows configuration of a set of datasets through XML for provision to the graphics module.

This module depends on the `edal-common` module and the `edal-graphics` module

Maven dependency:
```
<dependency>
    <groupId>uk.ac.rdg.resc</groupId>
    <artifactId>edal-xml-catalogue</artifactId>
    <version>[insert required version]</version>
</dependency>
```

## EDAL WMS
The `edal-wms` module contains an implementation of the WMS (Web Map Service) standard with a number of custom requests suited to exposing environmental data over the web. This module is not a complete packaged WMS - it supplies all of the required servlet classes, but requires a data catalogue to be implemented to map WMS layer names to the EDAL data objects.

This module depends on the `edal-common` module and the `edal-graphics` module

Maven dependency:
```
<dependency>
    <groupId>uk.ac.rdg.resc</groupId>
    <artifactId>edal-wms</artifactId>
    <version>[insert required version]</version>
</dependency>
```

## Godiva 3
The `edal-godiva` module is a Google Web Toolkit (GWT) based WMS client. It supports all of the extended WMS requests supplied by the `edal-wms` module.

This module does not depend on any others.

Maven dependency:
```
<dependency>
    <groupId>uk.ac.rdg.resc</groupId>
    <artifactId>edal-godiva</artifactId>
    <version>[insert required version]</version>
</dependency>
```

Note that the `gwt-user` dependency is *not* propagated by `edal-godiva`.  Therefore, if you wish to compile Godiva3 as part of the build process for a webapp, you will need to include the `gwt-user` library in addition to `edal-godiva`.