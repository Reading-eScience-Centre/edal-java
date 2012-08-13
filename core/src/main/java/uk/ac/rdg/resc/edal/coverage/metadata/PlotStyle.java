package uk.ac.rdg.resc.edal.coverage.metadata;

public enum PlotStyle {
    BOXFILL {
        @Override
        public boolean usesPalette() {
            return true;
        }
    },
    VECTOR {
        @Override
        public boolean usesPalette() {
            return false;
        }
    },
    POINT {
        @Override
        public boolean usesPalette() {
            return true;
        }
    },
    TRAJECTORY {
        @Override
        public boolean usesPalette() {
            return true;
        }
    },
    CONTOUR {
        @Override
        public boolean usesPalette() {
            return false;
        }
    },
    GRIDPOINT {
        @Override
        public boolean usesPalette() {
            return false;
        }
    },
    DEFAULT {
        @Override
        public boolean usesPalette() {
            return false;
        }
    };

    public abstract boolean usesPalette();
    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
