package uk.ac.rdg.resc.edal.cdm;

// TODO Document
public class FilenameVarIdTimeIndex {
        private final String filename;
        private final String varId;
        private final int tIndex;
    
        public FilenameVarIdTimeIndex(String filename, String varId, int tIndex) {
            this.filename = filename;
            this.varId = varId;
            this.tIndex = tIndex;
        }
    
        public String getFilename() {
            return filename;
        }
    
        public int getTIndex() {
            return tIndex;
        }

        public String getVarId() {
            return varId;
        }
}
