## Colour maps {#colour-maps}

### Common elements {#common-elements}

Colour maps are specified using the `<se:RasterSymbolizer>` tag. The latter must contain a `<se:ColorMap>` tag as well as an optional opacity transform. Within the colour map there must be one of three functions defining it either by a list of thresholds, a list of interpolation points, or a palette. Each colour map requires one variable.

| Tag name | Type of contents | Default value | Description |
| --- | --- | --- | --- |
| `<se:ColorMap>` | XML element | Must be specified | Must contain the function defining the colour map |

### Thresholds (SE compliant) {#thresholds}

A colour map can be defined by a list of thresholds within the [`<se:Categorize>` function](functions.md#categorize). The lists of thresholds and values should be in interleaved to be compliant with SE as in the following example:

```
<se:RasterSymbolizer>
    <se:Opacity>1.0</se:Opacity>
    <se:ColorMap>
        <se:Categorize fallbackValue="#00000000">
            <se:LookupValue>Rasterdata</se:LookupValue>
            <se:Value>#FF0000FF</se:Value>
            <se:Threshold>275.0</se:Threshold>
            <se:Value>#FF00FFFF</se:Value>
            <se:Threshold>280.0</se:Threshold>
            <se:Value>#FF00FF00</se:Value>
            <se:Threshold>285.0</se:Threshold>
            <se:Value>#FFFFFF00</se:Value>
            <se:Threshold>290.0</se:Threshold>
            <se:Value>#FFFFC800</se:Value>
            <se:Threshold>295.0</se:Threshold>
            <se:Value>#FFFFAFAF</se:Value>
            <se:Threshold>300.0</se:Threshold>
            <se:Value>#FFFF0000</se:Value>
        </se:Categorize>
    </se:ColorMap>
</se:RasterSymbolizer>
```

The above example is compliant with SE. Note that the opacity transform must be flat for this to be the case.

### Interpolation (SE compliant) {#interpolation}

A colour map can be defined by a list of interpolation points within the [`<se:Interpolate>` function](functions.md#interpolate). The colour values will be interpolated linearly between the data points. For example:

```
<se:RasterSymbolizer>
    <se:Opacity>1.0</se:Opacity>
    <se:ColorMap>
        <se:Interpolate fallbackValue="#FF006400">
            <se:LookupValue>Rasterdata</se:LookupValue>
            <se:InterpolationPoint>
                <se:Data>265.0</se:Data>
                <se:Value>#FF0000FF</se:Value>
            </se:InterpolationPoint>
            <se:InterpolationPoint>
                <se:Data>285.0</se:Data>
                <se:Value>#FFFFFFFF</se:Value>
            </se:InterpolationPoint>
            <se:InterpolationPoint>
                <se:Data>305.0</se:Data>
                <se:Value>#FFFF0000</se:Value>
            </se:InterpolationPoint>
        </se:Interpolate>
    </se:ColorMap>
</se:RasterSymbolizer>
```

The above example is compliant with SE. Note that the opacity transform must be flat for this to be the case.

### Segment and named palettes {#segments}

A colour map can be defined by a named palette of list of colours comprising a palette within the [`<resc:Segment>` function](functions.md#segment). For example:

```
<se:RasterSymbolizer>
    <se:Opacity>1.0</se:Opacity>
    <se:ColorMap>
        <resc:Segment fallbackValue="#FF006400">
            <se:LookupValue>Rasterdata</se:LookupValue>
            <resc:BelowMinValue>#FF0000FF</resc:BelowMinValue>
            <resc:ValueList>
                <se:Name>redblue</se:Name>
            </resc:ValueList>
            <resc:AboveMaxValue>#FFFF0000</resc:AboveMaxValue>
            <resc:Range>
                <resc:Minimum>270.0</resc:Minimum>
                <resc:Maximum>310.0</resc:Maximum>
                <resc:Spacing>linear</resc:Spacing>
            </resc:Range>
            <resc:NumberOfSegments>250</resc:NumberOfSegments>
        </resc:Segment>
    </se:ColorMap>
</se:RasterSymbolizer>
```