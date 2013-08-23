package uk.ac.rdg.resc.edal.dataset.plugins;

import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;

public class VectorPlugin extends VariablePlugin {

    public final static String MAG = "mag";
    public final static String DIR = "dir";
    private String title;

    public VectorPlugin(String xComponentId, String yComponentId, String title) {
        super(new String[] { xComponentId, yComponentId }, new String[] { MAG, DIR });
        this.title = title;
    }

    @Override
    protected VariableMetadata[] doProcessVariableMetadata(VariableMetadata... metadata) {
        VariableMetadata xMetadata = metadata[0];
        VariableMetadata yMetadata = metadata[1];

        VariableMetadata magMetadata = new VariableMetadata(getFullId(MAG), new Parameter(
                getFullId(MAG), "Magnitude of " + title, "Magnitude of components:\n"
                        + xMetadata.getParameter().getDescription() + " and\n"
                        + yMetadata.getParameter().getDescription(), xMetadata.getParameter()
                        .getUnits()), xMetadata.getHorizontalDomain(),
                xMetadata.getVerticalDomain(), xMetadata.getTemporalDomain());
        VariableMetadata dirMetadata = new VariableMetadata(getFullId(DIR), new Parameter(
                getFullId(DIR), "Direction of " + title, "Direction of components:\n"
                        + xMetadata.getParameter().getDescription() + " and\n"
                        + yMetadata.getParameter().getDescription(), "degrees"),
                xMetadata.getHorizontalDomain(), xMetadata.getVerticalDomain(),
                xMetadata.getTemporalDomain());

        VariableMetadata parentMetadata = xMetadata.getParent();

        VariableMetadata containerMetadata = new VariableMetadata(getFullId("-group"),
                new Parameter(getFullId("-group"), title, "Vector fields for " + title, null),
                xMetadata.getHorizontalDomain(), xMetadata.getVerticalDomain(),
                xMetadata.getTemporalDomain(), false);

        xMetadata.setParent(containerMetadata);
        yMetadata.setParent(containerMetadata);
        magMetadata.setParent(containerMetadata);
        dirMetadata.setParent(containerMetadata);

        containerMetadata.setParent(parentMetadata);
        return new VariableMetadata[] { containerMetadata, magMetadata, dirMetadata };
    }

    @Override
    protected Number generateValue(String varSuffix, Number... sourceValues) {
        if (MAG.equals(varSuffix)) {
            return Math.sqrt(Math.pow(sourceValues[0].doubleValue(), 2.0)
                    + Math.pow(sourceValues[1].doubleValue(), 2.0));
        } else if (DIR.equals(varSuffix)) {
            return Math.atan2(sourceValues[1].doubleValue(), sourceValues[0].doubleValue());
        } else {
            assert false;
            return null;
        }
    }

}
