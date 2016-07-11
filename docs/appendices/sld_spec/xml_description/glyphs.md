## Glyphs {#glyphs}

### In-situ Coloured Glyphs {#in-situ}

Currently only in situ coloured glyphs are supported using the `<resc:ColoredGlyphSymbolizer>` tag. It requires one variable. The following XML tags must be specified:

| Tag name | Type of contents | Default value | Description |
| --- | --- | --- | --- |
| `<resc:IconName>` | String | "circle" | Name of the symbol to use for glyphs ("circle", "square", or "dot") |
| `<se:ColorMap>` | XML element | Must be specified | Colour map for colouring the glyphs according to the value of the variable, as for colour maps above |

For example:

```
<resc:ColoredGlyphSymbolizer>
    <se:Opacity>1.0</se:Opacity>
    <resc:IconName>circle</resc:IconName>
    <se:ColorMap>
        <resc:Segment fallbackValue="#FF006400">
            <se:LookupValue>Rasterdata</se:LookupValue>
            <resc:BelowMinValue>#FF0000FF</resc:BelowMinValue>
            <resc:ValueList>
                <se:Name>redblue</se:Name>
            </resc:ValueList>
            <resc:AboveMaxValue>#FFFF0000</resc:AboveMaxValue>
            <resc:Range>
                <resc:Minimum>-5.0</resc:Minimum>
                <resc:Maximum>40.0</resc:Maximum>
                <resc:Spacing>linear</resc:Spacing>
            </resc:Range>
            <resc:NumberOfSegments>250</resc:NumberOfSegments>
        </resc:Segment>
    </se:ColorMap>
</resc:ColoredGlyphSymbolizer>
```