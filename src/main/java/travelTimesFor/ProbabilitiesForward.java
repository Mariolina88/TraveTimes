/*
 * GNU GPL v3 License
 *
 * Copyright 2015 Marialaura Bancheri
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package travelTimesFor;

import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import java.util.ArrayList;
import java.util.HashMap;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;
import oms3.io.CSTable;
import oms3.io.DataIO;

import org.geotools.feature.SchemaException;
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;


// TODO: Auto-generated Javadoc
/**
 * The Class ProbabilitiesForward.
 *
 * @author Marialaura Bancheri
 * @description The Class ProbabilitiesForward computes the forward probabilities
 * and the outflows (discharge and ET) , given the backward residence time pdfs.
 * The component also computes \Theta (ti) as defined in..
 * The inputs of the class are the water storage, discharge and
 * ET time series. 
 */
public class ProbabilitiesForward  extends JGTModel {


	@Description("The path  of the simulated discharge")
	@In
	public String inPathToDischarge;
	
	@Description("Qti: Q value for the given time considered and a given ti"
			+ "obtained from the probabilities backward theory")
	double Q_ti;

	@Description("Input Qti hashmap")
	@In
	public HashMap<Integer, double[]> inQ_tiValues;

	@Description("The path  of the simulated evapotranspiration")
	@In
	public String inPathToET;

	@Description("Et_ti: ET value for the given time considered and a given ti"
			+ "obtained from the probabilities backward theory")
	double ET_ti;

	
	@Description("Input ET_ti hashmap")
	@In
	public HashMap<Integer, double[]> inET_tiValues;


	@Description("pQ_back: travel time probability  value "
			+ "for the given time, obtained from the probabilities backward theory")
	double pQ_back;


	@Description("Input travel time probabilites hashmap")
	@In
	public HashMap<Integer, double[]> inPQ_backValues;


	@Description("pET_back: ET time probability  value "
			+ "for the given time, obtained from the probabilities backward theory")
	double pET_back;


	@Description("Input ET time probabilites hashmap")
	@In
	public HashMap<Integer, double[]> inPET_backValues;


	@Description("J: Precipitation value for the given time considered") 
	double J;


	@Description("Input Precipitation Hashmap")
	@In
	public HashMap<Integer, double[]> inPrecipvalues;


	@Description("Q: Q value for the given time considered"
			+ "obtained from the solution of the water budget eqz")
	double Q;


	@Description("[] inDischargeValues: time series array of all simulated Q")
	public Double [] inDischargevalues;




	@Description("ET: ET value for the given time considered"
			+ "obtained from the solution of the water budget eqz")
	double ET;

	@Description("[] inETvalues: time series array of all simulated AET")
	public Double [] inETvalues;

	@Description("integration time")
	double dt=1E-4;
	
	@Description("Last date of the simulation")
	@In
	public int tTimestep;


	private DateTimeFormatter formatter = JGTConstants.utcDateFormatterYYYYMMDDHHMM;


	@Description("dimension of the output matrix")
	int dim;


	@Description("injection time")
	int t_i=0;

	@Description("Station identifier")
	@In
	public int ID;


	@Description("First date of the simulation")
	@In
	public String tStartDate;


	@Description("Last date of the simulation")
	@In
	public String tEndDate;

	@Description("This field is needed in case the water storage, discharge and ET time series "
			+ "start from a different prevous time step, respect to the residence time"
			+ "analysis that we are going to do")
	@In
	public String tStartDateWaterBudget;
	
	@Description("deltaT is eventual the difference between the start date"
			+ "of the water storage, discharge and ET time series and the"
			+ "date of our simulation")
	int deltaT;


	@Description("The output HashMap with the Theta values"
			+ "given ti and t")
	@Out
	public HashMap<Integer, double[]> outHMtheta;


	@Description("The output HashMap, with the forward "
			+ "travel times pdfs")
	@Out
	public HashMap<Integer, double[]> outHMPQfor;


	@Description("The output HashMap, with the forward "
			+ "ET times pdfs")
	@Out
	public HashMap<Integer, double[]> outHMPETfor;


	@Description("The output HashMap with the discharge"
			+ "computed with the forward theory")
	@Out
	public HashMap<Integer, double[]> outHMQfor;


	@Description("The output HashMap with the ET"
			+ "computed with the forward theory")
	@Out
	public HashMap<Integer, double[]> outHMETfor;
	
	
	double intQ;
	double intQET;
	
	@Description("arraiFin is the final array needed for the rigth computationif the ID in the"
			+ "output hashmap")
	ArrayList <Double> arrayFin= new ArrayList <Double>();

	/**
	 * Process.
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception {
		
		/** ArrayList with the values of the computed variables  */
	    ArrayList <Double> thetaV= new ArrayList <Double>();
		ArrayList <Double> pQ= new ArrayList <Double>();
		ArrayList <Double> pET= new ArrayList <Double>();
		ArrayList <Double> Q_out= new ArrayList <Double>();
		ArrayList <Double> ET_out= new ArrayList <Double>();
	

		/** computation of the dimension of the array, given startDate and endDate */
		DateTime start = formatter.parseDateTime(tStartDate);
		DateTime end = formatter.parseDateTime(tEndDate);
		dim=(tTimestep==60)?Hours.hoursBetween(start, end).getHours()+1:Days.daysBetween(start, end).getDays()+1;

		if (tStartDateWaterBudget==null) deltaT=0;
		else {DateTime startWaterBudget= formatter.parseDateTime(tStartDateWaterBudget);
		deltaT=Hours.hoursBetween(startWaterBudget,start).getHours();}

		/** input values of precipitation*/
		J = inPrecipvalues.get(ID)[0];
		if (isNovalue(J)) J= 0;

		/** the WB values are read just once, a t_i=0 */
		if(t_i==0){			
			inDischargevalues=readData(inPathToDischarge,ID);
			inETvalues=	readData(inPathToET,ID);
		}
		
		/** since the dimension of the input hashmap varies in time
		 * dim_i computes it for the following loop */
		double dim_i=inPQ_backValues.size();

		/** loop for the computation of the probabilities and of the output fluxes
		 * it considers all the values from the series, each time from
		 * a different injection time
		 * */
		for (int t=0;t<dim_i;t++){

			Q=inDischargevalues[t_i+t+deltaT];

			ET=inETvalues[t_i+t+deltaT];
		
			pQ_back = inPQ_backValues.get(t)[0];
			if (isNovalue(pQ_back )) pQ_back = 0;

			pET_back = inPET_backValues.get(t)[0];
			if (isNovalue(pET_back )) pET_back = 0;

			Q_ti = inQ_tiValues.get(t)[0];
			if (isNovalue(Q_ti )) Q_ti = 0;

			ET_ti = inET_tiValues.get(t)[0];
			if (isNovalue(ET_ti )) ET_ti = 0;

			thetaV.add(computeTheta());
			pQ.add(computePQ(thetaV.get(t)));
			pET.add(computePET(thetaV.get(t)));
			Q_out.add(computeQ(thetaV.get(t),pQ.get(t)));
			ET_out.add(computeET(thetaV.get(t),pET.get(t)));


		}	
		
		/** for each injection time the arrayFin is filled with a numeber,
		 * needed for the ID of the output hashmap */
		arrayFin.add((double)t_i);

		/** 
		 * storage of results in hashmaps in which:
		 * on the first column there re the injection times (date column)
		 * and on the first row (value_t) are all the time step
		 */
		if (t_i<dim-1)storeResult(thetaV,pQ,pET, Q_out, ET_out);
		else {
			storeResult(arrayFin,arrayFin,arrayFin,arrayFin,arrayFin);   
		}

		/** t increases for considering the next injection time*/
		t_i++;
		intQ=0;
		intQET=0;

	}

	/**
	 * Read data: reader input Data: all values from the time series.
	 *
	 * @param inPath the  path of the  input file
	 * @param i the index of the column
	 * @return the double[] vector with the input values
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Double [] readData(String inPath, int i) throws IOException {	
		CSTable Reader = DataIO.table(new File(inPath));
		Double[] vector =DataIO.getColumnDoubleValues(Reader, "value_"+i);
		return vector;
	}

	/**
	 * Compute theta: computes the partitioning coefficient 
	 * according to..(XXCITEXX).
	 * intQ and intQET are support variable for the computation
	 * @return the double value of theta
	 */
	public double computeTheta(){	
		intQ+=Q_ti;
		intQET+=Q_ti+ET_ti;
		double theta=(Q_ti==0)? 0:intQ/intQET;
		return theta;	
	}

	/**
	 * Compute pQ: computes the forward probabilities from 
	 * the backward pdf and theta.
	 *
	 * @param theta: the partitioning coefficient
	 * @return the double value of the forward Q pdf
	 */
	public double computePQ(double theta){
		return (pQ_back==0)?0:(pQ_back*Q)/(theta*J);
	}

	/**
	 * Compute pET:computes the forward probabilities from 
	 * the backward pdf and theta..
	 *
	 * @param theta: the partitioning coefficient
	 * @return the double value of the forward ET pdf
	 */
	public double computePET(double theta){
		return (theta==1|pET_back==0)?0: (pET_back*ET)/((1-theta)*J);
	}


	/**
	 * Compute Q: computes the value of the discharge, 
	 * from the forward pdf Q(t,ti).
	 *
	 * @param theta: the partitioning coefficient
	 * @param pQ: the forward pdf
	 * @return the double value of the discharge
	 */
	public double computeQ(double theta, double pQ){
		return J*theta*pQ;
	}


	/**
	 * Compute ET:computes the value of the ET, 
	 * from the forward pdf ET(t,ti).
	 *
	 * @param theta: the partitioning coefficient
	 * @param pET: the forward pdf
	 * @return the double value of the ET
	 */
	public double computeET(double theta, double pET){
		return (theta==1)?0:J*(1-theta)*pET;
	}



	/**
	 * Store result.
	 *
	 * @param theta:the partitioning coefficient ArrayList
	 * @param pQ: forward Q pdfs ArrayList
	 * @param pET: forward ET pdfs ArrayList
	 * @param Q: output discharge ArrayList 
	 * @param ET: output ET ArrayList
	 * @throws SchemaException the schema exception
	 */
	private void storeResult(ArrayList<Double> theta,ArrayList<Double> pQ,ArrayList<Double> pET, ArrayList<Double> Q_for, 
			ArrayList<Double> ET_for) throws SchemaException {
		outHMtheta = new HashMap<Integer, double[]>();
		outHMPQfor = new HashMap<Integer, double[]>();
		outHMPETfor = new HashMap<Integer, double[]>();
		outHMQfor = new HashMap<Integer, double[]>();
		outHMETfor = new HashMap<Integer, double[]>();

		for (int k=0;k<theta.size();k++){
			outHMtheta.put(k, new double[]{(Double) theta.get(k)});
			outHMPQfor.put(k, new double[]{(Double) pQ.get(k)});
			outHMPETfor.put(k, new double[]{(Double) pET.get(k)});
			outHMQfor.put(k, new double[]{(Double) Q_for.get(k)});
			outHMETfor.put(k, new double[]{(Double) ET_for.get(k)});
		}

	}


}
