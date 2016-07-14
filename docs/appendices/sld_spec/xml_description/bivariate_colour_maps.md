## Bivariate colour maps {#bivariate-colour-maps}

A bivariate colour map may be specified using the `<resc:Raster2DSymbolizer>` tag. Currently only the `<resc:Categorize2D>` function is supported. It is intended to support perceptually linear colour maps in the future. They can be supported now by entering the corresponding sRGB values into the `<resc:Categorize2D>` function. Two variables are required, which must be specified using coverage constraints in a user layer. The following tags can be specified within a `<resc:Raster2DSymbolizer>` tag:

| Tag name | Type of contents | Default value | Description |
| --- | --- | --- | --- |
| `<resc:ColourMap2D>` | XML element | Must be specified | Must contain the function defining the colour map |

For example:

```
<resc:Raster2DSymbolizer>
    <se:Opacity>1.0</se:Opacity>
    <resc:ColorMap2D>
        <resc:Categorize2D fallbackValue="#FF006400">
            <se:LookupValue>Rasterdata</se:LookupValue>
            <!-- x thresholds define columns -->
            <resc:XThreshold>276.7</resc:XThreshold>
            <resc:XThreshold>283.3</resc:XThreshold>
            <resc:XThreshold>290.0</resc:XThreshold>
            <resc:XThreshold>296.7</resc:XThreshold>
            <resc:XThreshold>303.3</resc:XThreshold>
            <!-- y thresholds define rows -->
            <resc:YThreshold>0.5</resc:YThreshold>
            <resc:YThreshold>1.0</resc:YThreshold>
            <resc:YThreshold>1.5</resc:YThreshold>
            <resc:YThreshold>2.0</resc:YThreshold>
            <resc:YThreshold>2.5</resc:YThreshold>
            <!-- row #01 -->
            <se:Value>#FF0000FF</se:Value>
            <se:Value>#FF00FFFF</se:Value>
            <se:Value>#FF00FF00</se:Value>
            <se:Value>#FFFFFF00</se:Value>
            <se:Value>#FFFFC800</se:Value>
            <se:Value>#FFFF0000</se:Value>
            <!-- row #02 -->
            <se:Value>#CC0000FF</se:Value>
            <se:Value>#CC00FFFF</se:Value>
            <se:Value>#CC00FF00</se:Value>
            <se:Value>#CCFFFF00</se:Value>
            <se:Value>#CCFFC800</se:Value>
            <se:Value>#CCFF0000</se:Value>
            <!-- row #03 -->
            <se:Value>#990000FF</se:Value>
            <se:Value>#9900FFFF</se:Value>
            <se:Value>#9900FF00</se:Value>
            <se:Value>#99FFFF00</se:Value>
            <se:Value>#99FFC800</se:Value>
            <se:Value>#99FF0000</se:Value>
            <!-- row #04 -->
            <se:Value>#660000FF</se:Value>
            <se:Value>#6600FFFF</se:Value>
            <se:Value>#6600FF00</se:Value>
            <se:Value>#66FFFF00</se:Value>
            <se:Value>#66FFC800</se:Value>
            <se:Value>#66FF0000</se:Value>
            <!-- row #05 -->
            <se:Value>#330000FF</se:Value>
            <se:Value>#3300FFFF</se:Value>
            <se:Value>#3300FF00</se:Value>
            <se:Value>#33FFFF00</se:Value>
            <se:Value>#33FFC800</se:Value>
            <se:Value>#33FF0000</se:Value>
            <!-- row #06 -->
            <se:Value>#000000FF</se:Value>
            <se:Value>#0000FFFF</se:Value>
            <se:Value>#0000FF00</se:Value>
            <se:Value>#00FFFF00</se:Value>
            <se:Value>#00FFC800</se:Value>
            <se:Value>#00FF0000</se:Value>
        </resc:Categorize2D>
    </resc:ColorMap2D>
</resc:Raster2DSymbolizer>
```