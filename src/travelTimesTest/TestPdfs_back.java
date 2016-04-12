package travelTimesTest;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;
import org.jgrasstools.hortonmachine.utils.HMTestCase;

import travelTimesBack.ProbabilitiesBackward;


public class TestPdfs_back extends HMTestCase{

	public void testLinear() throws Exception {

		String startDateWB = "1994-01-01 00:00";
		String startDate = "1994-01-01 00:00";
		String endDate = "1994-01-01 15:00";
		int timeStepMinutes = 60;
		String fId = "ID";
		double []params_v={1,1};
	

		PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);


		String inPathToPrec = "resources/Input/rainfall.csv";
		String inPathToStorage= "resources/Input/S.csv";
		String inPathToDischarge= "resources/Input/Q.csv";
		String inPathToET= "resources/Input/ET.csv";
		String pathToP= "resources/Output/pdfsBack/p_back.csv";
		String pathToP_Qback= "resources/Output/pdfsBack/pQ_back.csv";
		String pathToP_ETback= "resources/Output/pdfsBack/pET_back.csv";
		String pathToQback= "resources/Output/pdfsBack/Q_back.csv";
		String pathToETback= "resources/Output/pdfsBack/ET_back.csv";
		String pathToQtt= "resources/Output/pdfsBack/Qtt.csv";
		String pathToETtt= "resources/Output/pdfsBack/Ett.csv";

		OmsTimeSeriesIteratorReader JReader = getTimeseriesReader(inPathToPrec, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorWriter writer_p = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writer_pQ = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writer_pET = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writer_Q = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writer_ET = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writer_Qtt = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writer_ETtt = new OmsTimeSeriesIteratorWriter();
		
		writer_p.file = pathToP;
		writer_p.tStart = startDate;
		writer_p.tTimestep = timeStepMinutes;
		writer_p.fileNovalue="-9999";
		
		writer_pQ.file = pathToP_Qback;
		writer_pQ.tStart = startDate;
		writer_pQ.tTimestep = timeStepMinutes;
		writer_pQ.fileNovalue="-9999";
		
		writer_pET.file = pathToP_ETback;
		writer_pET.tStart = startDate;
		writer_pET.tTimestep = timeStepMinutes;
		writer_pET.fileNovalue="-9999";
		
		writer_Q.file = pathToQback;
		writer_Q.tStart = startDate;
		writer_Q.tTimestep = timeStepMinutes;
		writer_Q.fileNovalue="-9999";
		
		writer_ET.file = pathToETback;
		writer_ET.tStart = startDate;
		writer_ET.tTimestep = timeStepMinutes;
		writer_ET.fileNovalue="-9999";
		
		writer_Qtt.file = pathToQtt;
		writer_Qtt.tStart = startDate;
		writer_Qtt.tTimestep = timeStepMinutes;
		writer_Qtt.fileNovalue="-9999";
		
		writer_ETtt.file = pathToETtt;
		writer_ETtt.tStart = startDate;
		writer_ETtt.tTimestep = timeStepMinutes;
		writer_ETtt.fileNovalue="-9999";

		
		ProbabilitiesBackward pdfs= new ProbabilitiesBackward();
	
		while( JReader.doProcess  ) { 
			
			pdfs.ID=209;
			pdfs.tStartDate=startDate;
			pdfs.tEndDate=endDate;
			pdfs.tStartDateWaterBudget=startDateWB;
			pdfs.inPathToStorage=inPathToStorage;
			pdfs.inPathToDischarge=inPathToDischarge;
			pdfs.inPathToET=inPathToET;
			pdfs.m_solver="dp853";
			pdfs.SAStype="BetaDistribution";
			pdfs.params=params_v;
			
			JReader.nextRecord();	
			HashMap<Integer, double[]> id2ValueMap = JReader.outData;
			pdfs.inPrecipvalues = id2ValueMap;

            pdfs.pm = pm;
            pdfs.process();
            
          
            HashMap<Integer, double[]> outHM = pdfs.outHMPback;
            HashMap<Integer, double[]> outHMpQ = pdfs.outHMPQback;
            HashMap<Integer, double[]> outHMpET = pdfs.outHMPETback;
            HashMap<Integer, double[]> outHMQ = pdfs.outHMQback;
            HashMap<Integer, double[]> outHMET = pdfs.outHMETback;
            
			writer_p.inData = outHM;
			writer_p.writeNextLine();
			
			if (pathToP != null) {
				writer_p.close();
			}
            
			writer_pQ.inData = outHMpQ;
			writer_pQ.writeNextLine();
			
			if (pathToP_Qback != null) {
				writer_pQ.close();
			}
			
			writer_pET.inData = outHMpET;
			writer_pET.writeNextLine();
			
			if (pathToP_ETback != null) {
				writer_pET.close();
			}
			
			writer_Q.inData = outHMQ;
			writer_Q.writeNextLine();
			
			if (pathToQback != null) {
				writer_Q.close();
			}
			
			writer_ET.inData = outHMET;
			writer_ET.writeNextLine();
			
			if (pathToETback != null) {
				writer_ET.close();
			}
			
			HashMap<Integer, double[]> outHMQtt =  pdfs.outHMQtt;
            HashMap<Integer, double[]> outHMETtt =  pdfs.outHMETtt;
            
			writer_Qtt.inData = outHMQtt;
			writer_Qtt.writeNextLine();
			
			if (pathToQtt != null) {
				writer_Qtt.close();
			}
            
			writer_ETtt.inData = outHMETtt;
			writer_ETtt.writeNextLine();
			
			if (pathToETtt != null) {
				writer_ETtt.close();
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