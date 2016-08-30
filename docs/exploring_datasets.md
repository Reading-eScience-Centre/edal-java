# Exploring Datasets

The example code below shows how to create a dataset and access the metadata and data it contains.

[include](../examples/src/main/java/uk/ac/rdg/resc/edal/examples/ExploreDataset.java)

Example output:
```
The following variables are defined in this dataset:
land_cover
u
v
temperature
temperature_uncertainty
u:v-group
u:v-mag
u:v-dir
temperature-upperbound
temperature-lowerbound
temperature-uncertainty_group
The following features are defined in this dataset:
land_cover
u
v
temperature
temperature_uncertainty
u:v-group
u:v-mag
u:v-dir
temperature-upperbound
temperature-lowerbound
temperature-uncertainty_group
The ID of the variable: temperature
CRS: GEOGCS["WGS84(DD)", 
  DATUM["WGS84", 
    SPHEROID["WGS84", 6378137.0, 298.257223563, AUTHORITY["EPSG","7030"]], 
    AUTHORITY["EPSG","6326"]], 
  PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]], 
  UNIT["degree", 0.017453292519943295], 
  AXIS["Geodetic longitude", EAST], 
  AXIS["Geodetic latitude", NORTH]]
BoundingBox: -180.000000, -90.000000 - 180.000000, 90.000000
Grid x-size: 360
Grid y-size: 180
X axis values (range of validity):
-179.5 (-180.0,-179.0)
-178.5 (-179.0,-178.0)
-177.5 (-178.0,-177.0)
...
176.5 (176.0,177.0)
177.5 (177.0,178.0)
178.5 (178.0,179.0)
179.5 (179.0,180.0)
Y axis values (range of validity):
-89.5 (-90.0,-89.0)
-88.5 (-89.0,-88.0)
-87.5 (-88.0,-87.0)
...
87.5 (87.0,88.0)
88.5 (88.0,89.0)
89.5 (89.0,90.0)
The index of the x-dimension is:  1
The index of the y-dimension is:  0
The grid cell at xindex=0, yindex=10: -179.5,-79.5
The cell's parent grid is the HorizontalGrid we extracted it from: true
The footprint of the grid cell: -180.000000, -80.000000 - -179.000000, -79.000000
The coordinates in the parent grid: X:0, Y:10
The following parameters are available in the temp uncert group:
temperature-uncertainty_group
temperature
temperature_uncertainty
temperature-upperbound
temperature-lowerbound
Shape of data: [12, 10, 180, 360]
298.83334
null
```