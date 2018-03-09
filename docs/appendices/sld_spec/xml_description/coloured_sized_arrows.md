## Coloured Sized Arrows {#coloured-sized-arrows}

The [sized arrows](sized_arrows.md) style provides a means of representing the magnitude and direction of a vector using differently sized arrows.  Whilst useful, it can difficult to accurately differentiate magnitudes based on a limited range of arrow sizes.  For data where this could be an issue, we also provide the `<resc:ColouredSizedArrowSymbolizer>` element.  This builds on the existing `<resc:SizedArrowSymbolizer>`, and also allows the colour to be varied.  Note that whilst the common use case is to augment the magnitude information by colouring the arrows, it is equally possible to use the size and colour of the arrows to represent different quantities (e.g. size - magnitude, colour - uncertainty):

| Tag name | Type of contents | Default value | Description |
| --- | --- | --- | --- |
| `<resc:ArrowSizeField>` | String | Must be specified | The data layer named used to scale the arrows |
| `<resc:ArrowColourField>` | String | Must be specified | The data layer named used to colour the arrows |
| `<resc:ArrowMinSize>` | Integer | 4 | The minimum size (in pixels) of the arrows |
| `<resc:ArrowMaxSize>` | Integer | 12 | The maximum size (in pixels) of the arrows |
| `<resc:ArrowBackground>` | String | transparent | Colour of the background |
| `<resc:ArrowStyle>` | String | THIN_ARROW | Style of the arrows to plot.  Accepts the values "THIN_ARROW" (a normal arrow), "FAT_ARROW" (a wider version), "TRI_ARROW" (a long isosceles triangle), "UPSTREAM" (dots with a line pointing in the desired direction) |
| `<resc:ArrowDirectionConvention>` | String | METEOROLOGICAL | Convention for the arrows direction. Accepts the values "METEOROLOGICAL", "OCEANOGRAPHIC" |
| `<resc:Range>` | XML element | Must be specified | Specifies the range of the data used to scale the arrows |
| `<se:ColorMap>` | XML element | Must be specified | Specifies the colour mapping between data values and arrow colour |

For example:

```
<resc:ColoredSizedArrowSymbolizer>
    <resc:ArrowSizeField>wind_speed_magnitude</resc:ArrowSizeField>
    <resc:ArrowColourField>wind_speed_uncertainty</resc:ArrowColourField>
    <resc:ArrowMinSize>4</resc:ArrowMinSize>
    <resc:ArrowMaxSize>12</resc:ArrowMaxSize>
    <resc:ArrowStyle>FAT_ARROW</resc:ArrowStyle>
    <resc:ArrowDirectionConvention>METEOROLOGICAL</resc:ArrowDirectionConvention>
    <resc:Range>
        <resc:Minimum>0</resc:Minimum>
        <resc:Maximum>50</resc:Maximum>
        <resc:Spacing>linear</resc:Spacing>
    </resc:Range>
    <se:ColorMap>
        <resc:Segment fallbackValue="transparent">
            <se:LookupValue>Rasterdata</se:LookupValue>
            <resc:BelowMinValue>extend</resc:BelowMinValue>
            <resc:ValueList>
                <se:Name>div-BuRd</se:Name>
            </resc:ValueList>
            <resc:AboveMaxValue>extend</resc:AboveMaxValue>
            <resc:Range>
                <resc:Minimum>0</resc:Minimum>
                <resc:Maximum>5</resc:Maximum>
                <resc:Spacing>linear</resc:Spacing>
            </resc:Range>
            <resc:NumberOfSegments>50</resc:NumberOfSegments>
        </resc:Segment>
    </se:ColorMap>
</resc:ColoredSizedArrowSymbolizer>
```