Environmental Data Abstraction Layer (EDAL)
===========================================

The EDAL project comprises a set of libraries to deal with the manipulation and visualisation of environmental data, as well as the ncWMS Web Map Service for exposing this data.

EDAL consists of a number of modules, each focused on a different task.  These modules are outlined below

EDAL Modules
------------

### EDAL Common
The edal-common module contains the core data model used in EDAL, as well as in-memory implementations of this data model, common utility methods, and exceptions.

### EDAL Graphics
The edal-graphics module contains code for generating images from the core EDAL data types.  This includes map images, as well as timeseries, vertical profile, and vertical section charts.  Additionally there are custom SLD (Styled Layer Descriptor) handlers to allow for the precise specification of how to assemble a map image, allowing arbitrarily complex plotting of multiple simultaneous data layers.

This module depends on the edal-common module.

### EDAL CDM
The edal-cdm module uses the Unidata NetCDF-Java libraries to read data into the core EDAL data model.  It reads CF-compliant gridded NetCDF files as well as OPeNDAP, GRIB, and several other formats (see http://www.unidata.ucar.edu/software/thredds/current/netcdf-java/reference/formats/FileTypes.html for a list).  It also includes a data reader capable of reading the UK Met Office EN3/4 in-situ datasets (http://www.metoffice.gov.uk/hadobs/en3/ and http://www.metoffice.gov.uk/hadobs/en4/).

This modules depends on the edal-common module.

### EDAL WMS
The edal-wms module contains an implementation of the WMS (Web Map Service) standard with a number of custom requests suited to exposing environmental data over the web.  This module is not a complete packaged WMS - it supplies all of the required servlet classes, but requires a data catalogue to be implemented to map WMS layer names to the EDAL data objects.

This module depends on the edal-common module and the edal-graphics module

### Godiva 3
The edal-godiva module is a Google Web Toolkit (GWT) based WMS client.  It supports all of the extended WMS requests supplied by the edal-wms module.

This module does not depend on any others.

### ncWMS
ncWMS is a full implementation of an environmental data WMS.  It uses all of the above modules and adds its own data catalogue and related configuration.  It is packaged as both a WAR file which can be deployed on an application server such as Tomcat or Glassfish, and as a standalone application which runs its own embedded application server.

EDAL was developed primarily to factor out common functionality from the original ncWMS (http://sourceforge.net/projects/ncwms/)

Documentation
-------------
Further documentation on using ncWMS and EDAL can be found in the following locations:
* [EDAL homepage](http://reading-escience-centre.github.io/edal-java/)
* [EDAL code](https://github.com/Reading-eScience-Centre/edal-java)
* [ncWMS user guide](http://reading-escience-centre.github.io/edal-java/ncWMS_user_guide.html)

Authors
-------
The EDAL libraries and ncWMS are developed by the [Reading e-Science Centre](http://www.resc.reading.ac.uk)

