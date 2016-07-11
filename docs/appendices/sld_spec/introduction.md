# The EDAL SLD Specification

Styling Specification for EDAL (Environmental Data Abstraction Library)

Information for specifying the style of image layers generated using a web map service (WMS) may be specified as an XML document using the existing standards of styled layer descriptor (SLD) and symbology encoding (SE) defined by the Open Geospatial Consortium (OGC). Those parts of the standard specific to a WMS are contained in SLD and those parts more generally applicable to specifying symbology within SE. The latter only supports representing raster data by colour maps defined by the following methods:

1. Thresholds – a list of thresholds and values representing colours.
2. Interpolation – a list of interpolation points.

We wish to stay within the SLD framework as it is the existing standard, but need to support more styling methods in order to represent quantitative uncertainty and other kinds of data e.g. vectors.