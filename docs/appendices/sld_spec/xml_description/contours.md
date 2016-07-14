## Contours {#contours}

A variable can be plotted as contours by specifying the `<resc:ContourSymbolizer>` XML element. This requires one variable. It can contain the following tags:

| Tag name | Type of contents | Default value | Description |
| --- | --- | --- | --- |
| `<resc:NumberOfContours>` | Integer | 10 | The number of contour levels |
| `<resc:ContourLineColour>` | String specifying colour | #FF000000 (Black) | The colour of the contours |
| `<resc:ContourLineWidth>` | Integer | 1 | The width of the contour lines, in pixels.  This is ignored for the "HIGHLIGHT" style |
| `<resc:ContourLineStyle>` | String | SOLID | The style of the contour lines.  Three styles are available - "SOLID" (solid lines), "DASHED" (dashed lines), and "HIGHLIGHT" (lines surrounded by a contrasting colour).  If "HIGHLIGHT" is used, the `<resc:ContourLineWidth>` parameter is ignored. |
| `<resc:Range>` | XML element | No default - must be specified | The scale range for the contours |

The `<resc:Range>` element contains the following tags similar to the `<resc:Range>` tag in the Segment function:

| Tag name | Type of contents | Default value | Description |
| --- | --- | --- | --- |
| `<resc:Minimum>` | Floating point number | Must be specified | The minimum of the range. |
| `<resc:Maximum>` | Floating point number | Must be specified | The maximum of the range. |
| `<resc:Spacing>` | String | linear | Whether the contours will be spaced linearly ("linear") or logarithmically ("logarithmic") |

It is possible to specify XML tags containing further styling information, but these are not currently supported and have no effect. An example of a specification for contours is:

```
<resc:ContourSymbolizer>
    <se:Opacity>1.0</se:Opacity>
    <resc:NumberOfContours>10</resc:NumberOfContours>
    <resc:ContourLineColour>black</resc:ContourLineColour>
    <resc:ContourLineStyle>SOLID</resc:ContourLineStyle>
    <resc:ContourLineWidth>1</resc:ContourLineWidth>
    <resc:Range>
        <resc:Minimum>0.5</resc:Minimum>
        <resc:Maximum>2.5</resc:Maximum>
        <resc:Spacing>linear</resc:Spacing>
    </resc:Range>
</resc:ContourSymbolizer>
```