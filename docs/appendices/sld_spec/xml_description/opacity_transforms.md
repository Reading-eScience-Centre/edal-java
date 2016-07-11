## Opacity transforms {#opacity-transforms}

### Flat {#flat}

A flat opacity transform can be specified in any symbolizer with the `<se:Opacity>` tag. By default there is 100% opacity. The opacity tag can contain a floating point number between 0.0 for transparent and 1.0 for opaque. See the above sections for examples.

### Opacity map {#opacity-map}

Instead of a flat opacity transform an opacity map can be specified using the XML element `<resc:OpacityMap>`. Within the latter is a function in which opacity is represented by a floating point number between 0.0 for transparent and 1.0 for opaque. This requires one variable, which is specified within the `<se:LookupValue>` tag in the function. The transform will vary spatially between entirely opaque and transparent based on the value of the variable at a particular point in space. Any function may be used. In the case of the Interpolate function only two interpolation points may be specified. For example:

```
<resc:OpacityTransform>
    <se:Interpolate fallbackValue="1.0">
        <se:LookupValue>OSTIA/analysis_error</se:LookupValue>
        <se:InterpolationPoint>
            <se:Data>0.0</se:Data>
            <se:Value>1.0</se:Value>
        </se:InterpolationPoint>
        <se:InterpolationPoint>
            <se:Data>2.5</se:Data>
            <se:Value>0.0</se:Value>
        </se:InterpolationPoint>
    </se:Interpolate>
</resc:OpacityTransform>
```