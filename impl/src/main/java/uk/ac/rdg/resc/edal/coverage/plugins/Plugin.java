package uk.ac.rdg.resc.edal.coverage.plugins;

import java.util.AbstractList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.rdg.resc.edal.coverage.grid.GridAxis;
import uk.ac.rdg.resc.edal.coverage.grid.GridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.impl.AbstractGridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.grid.impl.InMemoryGridValuesMatrix;
import uk.ac.rdg.resc.edal.coverage.impl.AbstractMultimemberDiscreteCoverage;
import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.ScalarMetadata;

/**
 * A generic class defining a plugin for coverages. This can be used with
 * {@link AbstractMultimemberDiscreteCoverage}s to provide new members through
 * others. Plugins can be implemented by subclassing this class and defining the
 * required methods:
 * 
 * {@link Plugin#generateMetadata(String, List, RangeMetadata)}
 * {@link Plugin#generateValue(String, List)}
 * 
 * TODO: Could this be implemented more efficiently using {@link Enum}s?
 * 
 * @author Guy Griffiths
 * 
 */
public abstract class Plugin {
    private final List<String> uses;
    private final Set<String> provides;
    private final String baseName;
    private final String description;

    public Plugin(List<String> uses, List<String> provides, String description) {
        if (provides == null || provides.size() == 0) {
            throw new IllegalArgumentException("A plugin must provide some data");
        }
        this.uses = uses;
        this.description = description;

        StringBuilder build = new StringBuilder();
        for (String component : uses) {
            build.append(component);
        }
        baseName = build.toString();

        this.provides = new HashSet<String>();
        for (String provided : provides) {
            this.provides.add(baseName + "_" + provided);
        }
    }

    public String getDescription() {
        return description;
    }

    public String getParentName() {
        return baseName;
    }

    public List<String> uses() {
        return uses;
    }

    public Set<String> provides() {
        return provides;
    }

    private void checkValidRequest(String memberName, int numberOfValues) {
        if (!provides.contains(memberName) || !memberName.startsWith(baseName)) {
            throw new IllegalArgumentException("This Plugin does not provide the field "
                    + memberName);
        }
        if (numberOfValues != uses.size()) {
            throw new IllegalArgumentException("This Plugin needs " + uses.size()
                    + " fields, but you have provided " + numberOfValues);
        }
    }

    public Object getProcessedValue(String memberName, List<Object> values) {
        checkValidRequest(memberName, values.size());
        String reducedName = memberName.substring(baseName.length() + 1);
        return generateValue(reducedName, values);
    }

    public GridValuesMatrix<?> getProcessedValues(String memberName,
            final List<? extends GridValuesMatrix<?>> gvmInputs) {
        checkValidRequest(memberName, gvmInputs.size());
        final String reducedName = memberName.substring(baseName.length() + 1);

        return new AbstractGridValuesMatrix<Object>() {
            @Override
            public Object doReadPoint(final int[] coords) {
                List<Object> values = new AbstractList<Object>() {
                    @Override
                    public Object get(int index) {
                        return gvmInputs.get(index).readPoint(coords);
                    }

                    @Override
                    public int size() {
                        return gvmInputs.size();
                    }
                };
                return Plugin.this.getProcessedValue(reducedName, values);
            }

            @Override
            public GridValuesMatrix<Object> doReadBlock(int[] mins, int[] maxes) {
                return new InMemoryGridValuesMatrix<Object>() {
                    @Override
                    public Object doReadPoint(int[] coords) {
                        return this.readPoint(coords);
                    }

                    @Override
                    public Class<Object> getValueType() {
                        return this.getValueType();
                    }

                    @Override
                    public GridAxis doGetAxis(int n) {
                        return this.getAxis(n);
                    }

                    @Override
                    public int getNDim() {
                        return this.getNDim();
                    }
                };
            }

            @Override
            @SuppressWarnings("unchecked")
            public Class<Object> getValueType() {
                return (Class<Object>) gvmInputs.get(0).getValueType();
            }

            @Override
            public void close() {
                for (GridValuesMatrix<?> gvmInput : gvmInputs) {
                    gvmInput.close();
                }
            }

            @Override
            public GridAxis doGetAxis(int n) {
                return gvmInputs.get(0).getAxis(n);
            }

            @Override
            public int getNDim() {
                return gvmInputs.get(0).getNDim();
            }
        };
    }

    public RangeMetadata getProcessedMetadata(String memberName, List<ScalarMetadata> metadataList,
            RangeMetadata parentMetadata) {
        checkValidRequest(memberName, metadataList.size());
        String reducedName = memberName.substring(baseName.length() + 1);
        return generateMetadata(reducedName, metadataList, parentMetadata);
    }

    protected abstract RangeMetadata generateMetadata(String component,
            List<ScalarMetadata> metadataList, RangeMetadata parentMetadata);

    protected abstract Object generateValue(String component, List<Object> values);

}
