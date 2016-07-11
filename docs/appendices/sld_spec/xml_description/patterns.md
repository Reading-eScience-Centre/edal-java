## Stippling {#stippling}

Stippling is designed to represent uncertainty by varying degrees of black stippling. The `<resc:StippleSymbolizer>` element is used to specify the styling information for a layer of stippling. It requires one variable. It must contain one function in which the density of stippling is represented by a floating point number between 0.0 for no stippling and 1.0 for solid black. Any function may be used. In the case where the interpolate function is used the stippling is varied uniformly between 65 possible different levels of stippling and only two interpolation points may be specified. Using the segment function for example:

```
<resc:StippleSymbolizer>
    <se:Opacity>1.0</se:Opacity>
    <resc:Segment fallbackValue="0.0">
        <se:LookupValue>Rasterdata</se:LookupValue>
        <resc:ValueList>
            <se:Value>0.0</se:Value>
            <se:Value>1.0</se:Value>
        </resc:ValueList>
        <resc:Range>
            <resc:Minimum>0.0</resc:Minimum>
            <resc:Maximum>2.5</resc:Maximum>
            <resc:Spacing>linear</resc:Spacing>
        </resc:Range>
        <resc:NumberOfSegments>5</resc:NumberOfSegments>
    </resc:Segment>
</resc:StippleSymbolizer>
```