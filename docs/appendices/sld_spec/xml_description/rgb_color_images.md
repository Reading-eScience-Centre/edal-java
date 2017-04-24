## RGB Color Images {#rgb-color-images}

RGB Images may be specified using the `<resc:RasterRGBSymbolizer>` tag.  This is an experimental feature which will take 3 data fields along with a specification of their value ranges, and will produce a false RGB color image from them.

An example of the required XML for this symbolizer can be seen below:

```
<resc:RasterRGBSymbolizer>
    <se:Opacity>1.0</se:Opacity>
    <se:ColorMap>
        <resc:RedBand>
            <resc:BandName>dataset/redbandvar</resc:BandName>
            <resc:Range>
                <resc:Minimum>0</resc:Minimum>
                <resc:Maximum>100</resc:Maximum>
            </resc:Range>
        </resc:RedBand>
        <resc:GreenBand>
            <resc:BandName>dataset/greenbandvar</resc:BandName>
            <resc:Range>
                <resc:Minimum>0</resc:Minimum>
                <resc:Maximum>100</resc:Maximum>
            </resc:Range>
        </resc:GreenBand>
        <resc:BlueBand>
            <resc:BandName>dataset/bluebandvar</resc:BandName>
            <resc:Range>
                <resc:Minimum>1</resc:Minimum>
                <resc:Maximum>1000</resc:Maximum>
                <resc:Spacing>logarithmic</resc:Spacing>
            </resc:Range>
        </resc:BlueBand>
    </se:ColorMap>
</resc:RasterRGBSymbolizer>
```