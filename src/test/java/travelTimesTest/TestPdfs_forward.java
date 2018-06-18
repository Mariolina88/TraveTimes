package travelTimesTest;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;

import travelTimesFor.ProbabilitiesForward;


public class TestPdfs_forward{

	@Test
	public void testLinear() throws Exception {

		String startDateWB = "1994-01-01 00:00";
		String startDate = "1994-01-01 00:00";
		String endDate = "1994-01-31 00:00";
		int timeStepMinutes = 60;
		String fId = "ID";


		String inPathToPrec = "resources/Input/rainfall.csv";
		String inPathToDischarge= "resources/Input/R_drain_rz.csv";
		String inPathToET= "resources/Input/ET_rz.csv";
		String inpathToP_Qback= "resources/Output/pdfsBack/pQ_back.csv";
		String inpathToP_ETback= "resources/Output/pdfsBack/pET_back.csv";
		String inpathToQback= "resources/Output/pdfsBack/Q_back.csv";
		String inpathToETback= "resources/Output/pdfsBack/ET_back.csv";
		String pathToTheta= "resources/Output/pdfsFor/Theta.csv";
		String pathToP_Qfor= "resources/Output/pdfsFor/pQ_for.csv";
		String pathToP_ETfor= "resources/Output/pdfsFor/pET_for.csv";
		String pathToQfor= "resources/Output/pdfsFor/Q_for.csv";
		String pathToETfor= "resources/Output/pdfsFor/ET_for.csv";


		OmsTimeSeriesIteratorReader JReader = getTimeseriesReader(inPathToPrec, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader pQReader = getTimeseriesReader(inpathToP_Qback, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader pETReader = getTimeseriesReader(inpathToP_ETback, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader QReader = getTimeseriesReader(inpathToQback, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader ETReader = getTimeseriesReader(inpathToETback, fId, startDate, endDate, timeStepMinutes);
		
		
		OmsTimeSeriesIteratorWriter writer_theta = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writer_pQ = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writer_pET = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writer_Q = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writer_ET = new OmsTimeSeriesIteratorWriter();
		
		writer_theta.file = pathToTheta;
		writer_theta.tStart = startDate;
		writer_theta.tTimestep = timeStepMinutes;
		writer_theta.fileNovalue="-9999";
		
		writer_pQ.file = pathToP_Qfor;
		writer_pQ.tStart = startDate;
		writer_pQ.tTimestep = timeStepMinutes;
		writer_pQ.fileNovalue="-9999";
		
		writer_pET.file = pathToP_ETfor;
		writer_pET.tStart = startDate;
		writer_pET.tTimestep = timeStepMinutes;
		writer_pET.fileNovalue="-9999";
		
		writer_Q.file = pathToQfor;
		writer_Q.tStart = startDate;
		writer_Q.tTimestep = timeStepMinutes;
		writer_Q.fileNovalue="-9999";
		
		writer_ET.file = pathToETfor;
		writer_ET.tStart = startDate;
		writer_ET.tTimestep = timeStepMinutes;
		writer_ET.fileNovalue="-9999";
		

		
		ProbabilitiesForward pdfs= new ProbabilitiesForward();
	
		while( JReader.doProcess  ) { 
			
			pdfs.ID=209;
			pdfs.tStartDate=startDate;
			pdfs.tEndDate=endDate;
			pdfs.tStartDateWaterBudget=startDateWB;
			pdfs.inPathToDischarge=inPathToDischarge;
			pdfs.inPathToET=inPathToET;
			
			JReader.nextRecord();	
			HashMap<Integer, double[]> id2ValueMap = JReader.outData;
			pdfs.inPrecipvalues = id2ValueMap;
			
			pQReader.nextRecord();
            id2ValueMap = pQReader.outData;
            pdfs.inPQ_backValues = id2ValueMap;
            
            pETReader.nextRecord();
            id2ValueMap = pETReader.outData;
            pdfs.inPET_backValues = id2ValueMap;
            
            QReader.nextRecord();
            id2ValueMap = QReader.outData;
            pdfs.inQ_tiValues = id2ValueMap;
            
            ETReader.nextRecord();
            id2ValueMap = ETReader.outData;
            pdfs.inET_tiValues = id2ValueMap;
            
            
            pdfs.process();
            
          
            HashMap<Integer, double[]> outHM = pdfs.outHMtheta;
            HashMap<Integer, double[]> outHMpQ = pdfs.outHMPQfor;
            HashMap<Integer, double[]> outHMpET = pdfs.outHMPETfor;
            HashMap<Integer, double[]> outHMQ = pdfs.outHMQfor;
            HashMap<Integer, double[]> outHMET = pdfs.outHMETfor;
            
			writer_theta.inData = outHM;
			writer_theta.writeNextLine();
			
			if (pathToTheta != null) {
				writer_theta.close();
			}
            
			writer_pQ.inData = outHMpQ;
			writer_pQ.writeNextLine();
			
			if (pathToP_Qfor != null) {
				writer_pQ.close();
			}
			
			writer_pET.inData = outHMpET;
			writer_pET.writeNextLine();
			
			if (pathToP_ETfor != null) {
				writer_pET.close();
			}
			
			writer_Q.inData = outHMQ;
			writer_Q.writeNextLine();
			
			if (pathToQfor != null) {
				writer_Q.close();
			}
			
			writer_ET.inData = outHMET;
			writer_ET.writeNextLine();
			
			if (pathToETfor != null) {
				writer_ET.close();
			}
			

         }
		JReader.close();
		
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