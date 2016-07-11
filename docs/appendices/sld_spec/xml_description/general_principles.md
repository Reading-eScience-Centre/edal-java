## General principles {#general-principles}

Each SLD document contains a number of layers, which correspond to a layer in the output image that results when the document is parsed. Each layer has an associated style. The latter defines how the data is plotted in the image layer. Each layer can either be named or a user layer. In the former case the name of the layer is the unique, machine-readable Name of the layer in the WMS server (which usually maps to a variable within an underlying NetCDF file in ncWMS). More than one variable can be plotted within the same layer, for example as a bivariate colour map, by specifying coverage constraints within a user layer, which may be named arbitrarily.

An outline for the general structure of an SLD document containing named layers is as follows:

```
<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.1.0"
    xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd"
    xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
    xmlns:se="http://www.opengis.net/se" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:resc="http://www.resc.reading.ac.uk">
    <NamedLayer>
        <se:Name>OSTIA/analysed_sst</se:Name>
        <UserStyle>
            <se:Name>sea_surface_temperature_style</se:Name>
            <se:CoverageStyle>
                <se:Rule>
                    <se:RasterSymbolizer>
                        <se:Opacity>1.0</se:Opacity>
                        <se:ColorMap>
                            <se:Interpolate
                                fallbackValue="#00000000">
                                ...
                            </se:Interpolate>
                        </se:ColorMap>
                    </se:RasterSymbolizer>
                </se:Rule>
            </se:CoverageStyle>
        </UserStyle>
    </NamedLayer>
    <NamedLayer>
        <se:Name>OSTIA/analysis_error</se:Name>
        <UserStyle>
            <se:Name>analysis_error_style</se:Name>
            <se:CoverageStyle>
                <se:Rule>
                    <se:RasterSymbolizer>
                        <se:Opacity>1.0</se:Opacity>
                        <se:ColorMap>
                            <se:Categorize fallbackValue="#00000000">
                                <se:LookupValue>Rasterdata</se:LookupValue>
                                ...
                            </se:Categorize>
                        </se:ColorMap>
                    </se:RasterSymbolizer>
                </se:Rule>
            </se:CoverageStyle>
        </UserStyle>
    </NamedLayer>
</StyledLayerDescriptor>
```

The root element of the document is an <sld:StyledLayerDescriptor> tag. Within this are contained one or more `<sld:NamedLayer>` elements, which specify how to plot the layers of the image. Our convention is that the layers are plotted in order from the top to the bottom of the document. In the outline above the sea surface temperature would be plotted first and then the error would be plotted on top of it.

Each layer element contains an <se:Name> tag specifying the variable to be plotted and a `<sld:UserStyle>` tag, which specifies the styling information for the layer. The style can also have a name, which may be human readable and is ignored by the parser. In the case of a two or more variables being plotted in the same layer a user layer can be specified, which does not require a name. The variables to be plotted are then determined by coverage constraints. The general structure is as in the following example:

```
<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.1.0"
    xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd"
    xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
    xmlns:se="http://www.opengis.net/se" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:resc="http://www.resc.reading.ac.uk">
    <UserLayer>
        <se:Name>OSTIA/analysed_sst_bivariate_colourmap</se:Name>
        <LayerCoverageConstraints>
            <CoverageConstraint>
                <se:CoverageName>OSTIA/analysis_error</se:CoverageName>
            </CoverageConstraint>
            <CoverageConstraint>
                <se:CoverageName>OSTIA/analysed_sst</se:CoverageName>
            </CoverageConstraint>
        </LayerCoverageConstraints>
        <UserStyle>
            <se:Name>2D thresholded colour scheme</se:Name>
            <se:CoverageStyle>
                <se:Rule>
                    <resc:Raster2DSymbolizer>
                        …
                    </resc:Raster2DSymbolizer>
                </se:Rule>
            </se:CoverageStyle>
        </UserStyle>
    </UserLayer>
</StyledLayerDescriptor>
```

Each style element must contain one `<se:CoverageStyle>` element containing one `<se:Rule>` element. The latter must contain one symbolizer element. SE provides the `<se:RasterSymbolizer>` element for specifying the style of raster data. It contains a function, which in SE can either be the categorize or interpolate function. We support all the functionality of `<se:RasterSymbolizer>` and additional functionality. Most of this is provided by additional types of symbolizer element. There is also an extension to the `<se:RasterSymbolizer>` tag with the addition of the segment function.

The next section describes the types of functions that we support. The following sections describe the different types of symbolizers. In the tables of XML tags if an element has a default value it is optional otherwise it must be specified. Colours are encoded by 3 or 4 byte hexadecimal strings. The last 3 bytes specify the red, green and blue values in that order. In the case of a 4 byte string, the first byte specifies the opacity. For example #000000 and #FF000000 both specify opaque black. Finally, each symbolizer may contain one opacity transform, either flat or an opacity map based on a function. Opacity transforms are discussed in their own section at the end. Currently opacity transforms can only be applied to a layer not the whole image.