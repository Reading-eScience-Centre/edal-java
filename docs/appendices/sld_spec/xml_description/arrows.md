## Arrows {#arrows}

Arrows are designed to represent the direction of vectors. The `<resc:ArrowSymbolizer>` element is used to specify the styling information for a layer of arrows. It requires one variable. This data should be a set of directions given as an angular compass bearing (degrees clockwise from North). The following tags can be used within this element:

| Tag name | Type of contents | Default value | Description |
| --- | --- | --- | --- |
| `<resc:ArrowSize>` | Integer | 8 | Size of arrows |
| `<resc:ArrowColour>` | String | #FF000000 (Black) | Colour of arrows |
| `<resc:ArrowBackground>` | String | transparent | Colour of the background |
| `<resc:ArrowStyle>` | String | THIN_ARROW | Style of the arrows to plot.  Accepts the values "THIN_ARROW" (a normal arrow), "FAT_ARROW" (a wider version), "TRI_ARROW" (a long isosceles triangle), "UPSTREAM" (dots with a line pointing in the desired direction) |
| `<resc:ArrowDirectionConvention>` | String | METEOROLOGICAL | Convention for the arrows direction. Accepts the values "METEOROLOGICAL", "OCEANOGRAPHIC" |

For example:

```
<resc:ArrowSymbolizer>
    <se:Opacity>1.0</se:Opacity>
    <resc:ArrowSize>8</resc:ArrowSize>
    <resc:ArrowColour>#FF000000</resc:ArrowColour>
    <resc:ArrowStyle>UPSTREAM</resc:ArrowStyle>
    <resc:ArrowBackground>transparent</resc:ArrowBackground>
    <resc:ArrowDirectionConvention>METEOROLOGICAL</resc:ArrowDirectionConvention>
</resc:ArrowSymbolizer>
```