## Functions {#functions}

SE defines two functions that are used in the `<se:RasterSymbolizer>` tag: `<se:Categorize>` and `<se:Interpolate>`. We have created additional functions. So far these are `<resc:Categorize2D>` for bivariate colour maps and `<resc:Segment>`, which creates equally spaced bands, which are either interpolated or subsampled as necessary. All functions must have an attribute named `fallbackValue`, which describes the value to use if there is no data present. Where a 1D function is expected any of the tree functions Categorize, Interpolate or Segment may be used interchangeably. This currently applies to colour maps, stippling and opacity transforms.

### Categorize {#categorize}

The categorize function divides the data for the variable being plotted up into sections based on numerical thresholds, above or below which a different value of either the colour, the density of a pattern or the opacity is plotted. The `<se:Categorize>` function can contain the following tags:

| Tag name | Type of contents | Default value | Description |
| --- | --- | --- | --- |
| `<se:LookupValue>` | String | When used within `<se:RasterSymbolizer>` it must contain the text "Rasterdata" for SE compliance. Must be specified within opacity maps. | Specifies the raster data source to use within an opacity map, otherwise ignored. |
| `<se:Threshold>` | Floating point number | One or more must be specified | The thresholds within the data |
| `<se:Value>` | String specifying a colour or a floating point number specifying a pattern density or opacity. | One more than the number of thresholds must be specified | The value to plot before or after the corresponding threshold(s) within the data. |

This list of thresholds and values must start and end with a value and alternates between a threshold and a value to be compliant with SE. For an example see the section on the use of thresholds with colour maps.

### Categorize2D {#categorize2d}

The categorize2D function is similar to the categorize function, but takes two variables as input and is applicable to bivariate colour maps. The <se:Categorize2D> function can contain the following tags:

| Tag name | Type of contents | Default value | Description |
| --- | --- | --- | --- |
| `<resc:XThreshold>` | Floating point number | 1..n must be specified | List of thresholds applicable to x variable |
| `<resc:YThreshold>` | Floating point number | 1..m must be specified | List of thresholds applicable to y variable |
| `<se:Value>` | String specifying a colour | (n+1)(m+1) must be specified | List of colours to use in each region, with x varying before y |

For an example see the section on bivariate colour maps.

### Interpolate {#interpolate}

The interpolate function interpolates the value to be plotted linearly between a series of points in the range of the variable where the value to be plotted is specified explicitly. Within the `<se:Interpolate>` function the following tags can be used:

| Tag name | Type of contents | Default value | Description |
| --- | --- | --- | --- |
| `<se:LookupValue>` | String | When used within `<se:RasterSymbolizer>` it must contain the text "Rasterdata" for SE compliance. Must be specified within opacity maps. | Within an opacity map specifies the raster data source(s) to use, otherwise ignored. |
| `<se:InterpolationPoint>` | XML element | At list of at least two must be specified | Contains the value and data point of an interpolation point |

The `<se:InterpolationPoint>` must contain the following tags:

| Tag name | Type of contents | Default value | Description |
| --- | --- | --- | --- |
| `<se:Data>` | Floating point value | Must be specified | The data point of the interpolation point |
| `<se:Value>` | String specifying a colour or a floating point number specifying a pattern density or opacity. | Must be specified | The value to be plotted at the interpolation point |

For an example see the section on colour maps.

### Segment {#segment}

The segment function divides the data for the variable to be plotted into uniform bands of different value given either a list of values or the name of a colour palette, the number of bands and a range for the data. If the number of bands is greater than or less than the number of values then these are either interpolated uniformly of subsampled to give the specified number of bands. Up to 250 bands can be specified, which has an effect near to interpolation. The `<resc:Interpolate>` tag contains the following tags:

| Tag name | Type of contents | Default value | Description |
| --- | --- | --- | --- |
| `<se:LookupValue>` | String | Must be used within opacity maps, otherwise ignored. | Within an opacity map specifies the raster data source(s) to use, otherwise ignored. |
| `<resc:ValueList>` | XML element | Must be specified | Specifies the values to interpolate between or subsample. |
| `<resc:BelowMinValue>` | String specifying a colour or a floating point number specifying a pattern density or opacity. | If omitted the same value is used as at the minimum in the range. | Specifies the value to plot below the minimum point in the range. |
| `<resc:AboveMaxValue>` | String specifying a colour or a floating point number specifying a pattern density or opacity. | If omitted the same value as at the maximum of the range is used. | Specifies the value to plot above the maximum point in the range. |
| `<resc:NumberOfSegments>` | _Integer_ | Must be specified | The number of uniformly spaced bands to create, with a maximum or 250. |
| `<resc:Range>` | XML element | Must be specified | Specifies the range of the data |

The `<resc:ValueList>` element contains the following tags:

| Tag name | Type of contents | Default value | Description |
| --- | --- | --- | --- |
| `<se:Value>` | String specifying a colour or a floating point number specifying a pattern density or opacity. | At least one must be specified if `<se:Name>` is omitted, or if patterns or opacities are being plotted. | The values to be interpolated between or subsampled. |
| `<se:Name>` | String | Must be specified if `<se:Value>` is omitted. | The name of a predefined palette, which contains a list of values. |

The `<resc:Range>` element contains the following tags:

| Tag name | Type of contents | Default value | Description |
| --- | --- | --- | --- |
| `<resc:Minimum>` | Floating point number | Must be specified | The minimum of the range. |
| `<resc:Maximum>` | Floating point number | Must be specified | The maximum of the range. |
| `<resc:Spacing>` | String | linear | Whether the bands will be spaced linearly ("linear") or logarithmically ("logarithmic") |

For an example see the next section on colour maps.