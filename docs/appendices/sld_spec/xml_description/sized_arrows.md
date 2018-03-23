## Sized Arrows {#sized-arrows}

Whilst the most common way of representing vectors is to use a raster layer for the magnitude and an arrow layer for the direction, we also provide a way to control the size of the arrows using the value of another field (usually the magnitude).  The `<resc:SizedArrowSymbolizer>` element is used to achieve this.  As per the `<resc:ArrowSymbolizer>`, the data field should be a set of directions given as an angular compass bearing (degrees clockwise from North). The following tags can be used within this element:

| Tag name | Type of contents | Default value | Description |
| --- | --- | --- | --- |
| `<resc:ArrowSizeField>` | String | Must be specified | The data layer named used to scale the arrows |
| `<resc:ArrowMinSize>` | Integer | 4 | The minimum size (in pixels) of the arrows |
| `<resc:ArrowMaxSize>` | Integer | 12 | The maximum size (in pixels) of the arrows |
| `<resc:ArrowColour>` | String | #FF000000 (Black) | Colour of arrows |
| `<resc:ArrowBackground>` | String | transparent | Colour of the background |
| `<resc:ArrowStyle>` | String | THIN_ARROW | Style of the arrows to plot.  Accepts the values "THIN_ARROW" (a normal arrow), "FAT_ARROW" (a wider version), "TRI_ARROW" (a long isosceles triangle), "UPSTREAM" (dots with a line pointing in the desired direction) |
| `<resc:ArrowDirectionConvention>` | String | DEFAULT | Convention for the arrows direction. Accepts the values "DEFAULT", "METEOROLOGICAL" (not supported for styles "UPSTREAM", "WIND_BARBS") |
| `<resc:Range>` | XML element | Must be specified | Specifies the range of the data used to scale the arrows |

For example:

```
<resc:SizedArrowSymbolizer>
    <resc:ArrowSizeField>wind_speed_magnitude</resc:ArrowSizeField>
    <resc:ArrowMinSize>4</resc:ArrowMinSize>
    <resc:ArrowMaxSize>12</resc:ArrowMaxSize>
    <resc:ArrowColour>#FF000000</resc:ArrowColour>
    <resc:ArrowStyle>FAT_ARROW</resc:ArrowStyle>
    <resc:ArrowDirectionConvention>DEFAULT</resc:ArrowDirectionConvention>
    <resc:Range>
        <resc:Minimum>0</resc:Minimum>
        <resc:Maximum>5</resc:Maximum>
        <resc:Spacing>linear</resc:Spacing>
    </resc:Range>
</resc:SizedArrowSymbolizer>
```