## Confidence interval triangles {#confidence-interval-triangles}

Confidence interval triangles are designed to show variables with an upper and lower bound. It requires two machine readable variable names that represent the upper and lower bound. These must be specified within coverage constraints in a user layer. The data is subsampled into squares, which are bisected into two triangles. The upper bound is shown in an upper triangle and the lower bound in the lower triangle. The contrast between the upper and lower triangles should indicate the uncertainty in the variable. Where there is no contrast the appearance is similar to a single variable colour map. They are specified by the XML element `<resc:ConfidenceIntervalSymbolizer>`. This can contain the following tags:

| Tag name | Type of contents | Default value | Description |
| --- | --- | --- | --- |
| `<resc:GlyphSize>` | Integer | 10 | The size of the glyphs |
| `<se:ColorMap>` | XML element | Must be specified | [Colour map](colour_maps.md) for colouring the glyphs according to the value of the variable |

For example:

```
<resc:ConfidenceIntervalSymbolizer>
    <se:Opacity>1.0</se:Opacity>
    <resc:GlyphSize>10</resc:GlyphSize>
    <se:ColorMap>
        <resc:Palette fallbackValue="#FF006400">
            <se:LookupValue>Rasterdata</se:LookupValue>
            <resc:PaletteDefinition>redblue</resc:PaletteDefinition>
            <resc:NumberOfColorBands>254</resc:NumberOfColorBands>
            <resc:BelowMinColor>#FF0000FF</resc:BelowMinColor>
            <resc:AboveMaxColor>#FFFF0000</resc:AboveMaxColor>
            <resc:ColorScale>
                <resc:ScaleMin>-1.5</resc:ScaleMin>
                <resc:ScaleMax>1.5</resc:ScaleMax>
                <resc:Logarithmic>false</resc:Logarithmic>
            </resc:ColorScale>
        </resc:Palette>
    </se:ColorMap>
</resc:ConfidenceIntervalSymbolizer>
```