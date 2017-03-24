# Environmental Data Abstraction Layer (EDAL)

[![Build Status](https://travis-ci.org/axiom-data-science/edal-java.svg?branch=dockerize)](https://travis-ci.org/axiom-data-science/edal-java)

- [Documentation](https://reading-escience-centre.gitbooks.io/edal-user-guide/content/)
- [Source code](https://github.com/Reading-eScience-Centre/edal-java)
- [Issues](https://github.com/Reading-eScience-Centre/edal-java/issues)
- [CHANGELOG](https://github.com/Reading-eScience-Centre/edal-java/blob/master/CHANGELOG)


The EDAL project comprises a set of libraries to deal with the manipulation and visualisation of environmental data.  They were originally created as part of [ncWMS](https://github.com/Reading-eScience-Centre/ncwms) but are standalone libraries which ncWMS uses.

EDAL consists of a number of modules, each focused on a different task.  These modules are outlined below

## EDAL Modules

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

### EDAL XML Catalogue
The edal-xml-catalogue module contains an implementation of a data catalogue in an XML format. This allows configuration of a set of datasets through XML for provision to the graphics module.

This module depends on the edal-common module and the edal-graphics module

### Godiva 3
The edal-godiva module is a Google Web Toolkit (GWT) based WMS client.  It supports all of the extended WMS requests supplied by the edal-wms module.

This module does not depend on any others.

EDAL was developed primarily to factor out common functionality from the original ncWMS (http://sourceforge.net/projects/ncwms/)


### Caching of datasets
Caching of datasets to improve performance is implemented using [Ehcache](http://www.ehcache.org/).
Two distinct caches are included in EDAL:

- A cache for datasets "featureCache"
- A cache for maps "meshDatasetCache"

When using ncWMS2 another cache is available:

- A cache for dynamic datasets  "dynamicCache"

Configuration for the caches can be configured using ehcache.xml which can be specified at run-time with the JVM parameter '-Dehcache.config="/path/to/ehcache.xml"'.
The default configuration is specified in /common/src/main/resources/ehcache.xml.

The Ehcache cache can be distributed using Terracotta by specifying the parameters in ehcache.xml.
An example file is provided in /common/src/main/resources/ehcache.terracotta.xml.


## Licence

```
Copyright (c) 2010 The University of Reading
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. Neither the name of the University of Reading, nor the names of the
   authors or contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.
4. If you wish to use, with or without modification, the Godiva web
   interface, the logo of the Reading e-Science Centre must be retained
   on the web page.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```

## Authors and Contributors

The EDAL libraries are developed by the [Reading e-Science Centre](http://www.met.reading.ac.uk/resc/home/) and are maintained by [@guygriffiths](https://github.com/guygriffiths).

Contributors:

- [@yosoyjay](https://github.com/yosoyjay)
- [@kwilcox](https://github.com/kwilcox)
