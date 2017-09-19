package travelTimesTest;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;
import org.junit.Test;

import integrator.InjectionTimeIntegration;


public class TestInjectionTimeIntegrator{

	@Test
	public void testLinear() throws Exception {

		String startDate = "1994-01-01 00:00";
		String endDate = "1994-03-01 00:00";
		int timeStepMinutes = 60;
		String fId = "ID";

		PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);

		String inpathToQout= "resources/Output/pdfsBack/Q_back.csv";
		String pathToQ= "resources/Output/integrator/Q_back_int.csv";

		OmsTimeSeriesIteratorReader QoutReader = getTimeseriesReader(inpathToQout, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorWriter writer_Qout = new OmsTimeSeriesIteratorWriter();
		
		writer_Qout.file = pathToQ;
		writer_Qout.tStart = startDate;
		writer_Qout.tTimestep = timeStepMinutes;
		writer_Qout.fileNovalue="-9999";

		
		InjectionTimeIntegration outTT= new InjectionTimeIntegration();
		


		while( QoutReader.doProcess ) {
		
			QoutReader.nextRecord();
			outTT.ID=209;
			outTT.tStartDate=startDate;
			outTT.tEndDate=endDate;

	
			HashMap<Integer, double[]> id2ValueMap = QoutReader.outData;
            outTT.inQoutvalues= id2ValueMap;
            

            outTT.pm = pm;
            outTT.process();
            
            HashMap<Integer, double[]> outHM = outTT.outHMQ;
            
			writer_Qout.inData = outHM;
			writer_Qout.writeNextLine();
			
			if (inpathToQout != null) {
				writer_Qout.close();
			}

			
		}
		

		QoutReader.close();

		
		

	}


	private OmsTimeSeriesIteratorReader getTimeseriesReader( String inPath, String id, String startDate, String endDate,
			int timeStepMinutes ) throws URISyntaxException {
		OmsTimeSeriesIteratorReader reader = new OmsTimeSeriesIteratorReader();
		reader.file = inPath;
		reader.idfield = "ID";
		reader.tStart = startDate;
		reader.tTimestep = 60;
		reader.tEnd = endDate;
		reader.fileNovalue = "-9999";
		reader.initProcess();
		return reader;
	}
}
