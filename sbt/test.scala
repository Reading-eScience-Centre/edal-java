import uk.ac.rdg.resc.edal.cdm.coverage.NcGridCoverage

object Test {
  def main(args: Array[String]) = {
     val cov = new NcGridCoverage("c:\\Godiva2_data\\FOAM_ONE\\FOAM_20100130.0.nc", "TMP")
     println("Number of grid points in coverage: " + cov.size)
  }
}