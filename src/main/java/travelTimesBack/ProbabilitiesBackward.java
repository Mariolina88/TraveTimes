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
package travelTimesBack;

import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import java.util.ArrayList;
import java.util.HashMap;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;
import oms3.io.CSTable;
import oms3.io.DataIO;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
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
 * The Class ProbabilitiesBackward.
 *
 * @author Marialaura Bancheri
 * @description The Class ProbabilitiesBackword computes the backward probabilities
 * and, then, the outflows (discharge and ET).
 * The inputs of the class are the water storage, the discharge and
 * the ET time series, the SAS functions and their parameters 
 */
public class ProbabilitiesBackward extends JGTModel{


	@Description("Precipitation: Precipitation value for the given time considered") 
	double precipitation;

	@Description("Input Precipitation Hashmap")
	@In
	public HashMap<Integer, double[]> inPrecipvalues;


	@Description("The path  of the simulated Storage")
	@In
	public String inPathToStorage;

	@Description("S: S value for the considered time step")
	double S_t;
	

	@Description("S: S at time step t+1")
	double S_t1;


	@Description("[] inWaterStorageValues: time series array of all simulated S")
	public Double [] inWaterStoragevalues;
	

	@Description("The path  of the simulated discharge")
	@In
	public String inPathToDischarge;

	@Description("Q: Q value for the considered time step")
	double Q;

	@Description("[] inDischargeValues: time series array of all simulated Q")
	public Double [] inDischargevalues;

	@Description("The path  of the simulated evapotranspiration")
	@In
	public String inPathToET;

	@Description("ET: ET value for the considered time step")
	double ET;


	@Description("[] inETvalues: time series array of all simulated AET")
	public Double [] inETvalues;


	@Description("ODE solver : dp853, Eulero already implemented")
	@In
	public String m_solver;


	@Description("Station identifier")
	@In
	public int ID;


	@Description("First date of the simulation")
	@In
	public String tStartDate;

	@Description("This field is needed in case the water storage, discharge and ET time series "
			+ "start from a different prevous time step, respect to the residence time"
			+ "analysis that we are going to do")
	@In
	public String tStartDateWaterBudget;


	@Description("Last date of the simulation")
	@In
	public String tEndDate;
	
	@Description("Last date of the simulation")
	@In
	public int tTimestep;


	@Description("String defining the SAS function chosen")
	@In
	public String SAStype;

	@Description("SAS  params")
	@In
	public double params [];


	private DateTimeFormatter formatter = JGTConstants.utcDateFormatterYYYYMMDDHHMM;


	@Description("dimension of the output matrix")
	int dim;

	@Description("deltaT is eventual the difference between the start date"
			+ "of the water storage, discharge and ET time series and the"
			+ "date of our simulation")
	int deltaT;

	@Description("injection time")
	int t_i=0;


	@Description("loop time")
	int t;

	@Description("threshold on the minimum value of the pdfs to be considered")
	double threshold=0.0001;


	@Description("SAS value at time t")
	double SASvalue_t;


	@Description("The output residence time backward probabilities matrix (t-ti)")
	@Out
	public HashMap<Integer, double[]> outHMPback= new HashMap<Integer, double[]>();;


	@Description("The output probability at a given time-step")
	double p_i;

	@Description("The output travel time backward probabilities matrix (t-ti)")
	@Out
	public HashMap<Integer, double[]> outHMPQback= new HashMap<Integer, double[]>();


	@Description("The output evapotranspiration time backward probabilities matrix (t-ti)")
	@Out
	public HashMap<Integer, double[]> outHMPETback= new HashMap<Integer, double[]>();


	@Description("The output backward disharge matrix (t-ti)")
	@Out
	public HashMap<Integer, double[]> outHMQback= new HashMap<Integer, double[]>();


	@Description("The output backward ET matrix (t-ti)")
	@Out
	public HashMap<Integer, double[]> outHMETback= new HashMap<Integer, double[]>();


	@Description("The output backward mean travel times matrix (t-ti)")
	@Out
	public HashMap<Integer, double[]> outHMQtt= new HashMap<Integer, double[]>();


	@Description("The output backward mean evapotranspiration times matrix (t-ti)")
	@Out
	public HashMap<Integer, double[]> outHMETtt= new HashMap<Integer, double[]>();


	@Description("arraiFin is the array containing the t_i at the end of the computation")
	ArrayList <Double> arrayFin= new ArrayList <Double>();


	/**
	 * Process.
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception {

		/** ArrayList with the values of the computed variables  */
		ArrayList <Double> P= new ArrayList <Double>();
		ArrayList <Double> pQ= new ArrayList <Double>();
		ArrayList <Double> pET= new ArrayList <Double>();
		ArrayList <Double> Q_out= new ArrayList <Double>();
		ArrayList <Double> ET_out= new ArrayList <Double>();
		ArrayList <Double> QttV= new ArrayList <Double>();
		ArrayList <Double> ETttV= new ArrayList <Double>();


		/** computation of the dimension of the array, given startDate and endDate */
		DateTime start = formatter.parseDateTime(tStartDate);
		DateTime end = formatter.parseDateTime(tEndDate);		
		dim=(tTimestep==60)?Hours.hoursBetween(start, end).getHours()+1:Days.daysBetween(start, end).getDays()+1;

		
		/** deltaT is the difference between the beginning of the time series and 
		 * the beginning of the computation, in the case the analysis starts from a different 
		 * time-step */
		if (tStartDateWaterBudget==null) deltaT=0;
		else {DateTime startWaterBudget= formatter.parseDateTime(tStartDateWaterBudget);
		deltaT=Hours.hoursBetween(startWaterBudget,start).getHours();}


		/** input values of precipitation*/
		precipitation = inPrecipvalues.get(ID)[0];
		if (isNovalue(precipitation)) precipitation= 0;

		/** the WB values are read just once, a t_i=0 */
		if(t_i==0){	
			inWaterStoragevalues=readData(inPathToStorage,ID);
			inDischargevalues=readData(inPathToDischarge,ID);
			inETvalues=	(inPathToET==null)?inETvalues:readData(inPathToET,ID);			
		}


		/** initial conditions */		
		p_i=(inWaterStoragevalues[t_i+deltaT]>0)?precipitation/inWaterStoragevalues[t_i+deltaT]:0;
		P.add(p_i);
		pQ.add(distributionValue(0)*P.get(0));
		pET.add(distributionValue(0)*P.get(0));
		Q_out.add(pQ.get(0)*inDischargevalues[t_i+deltaT]);		
		if (inETvalues==null)ET_out.add(0.0);
		else ET_out.add(pET.get(0)*inETvalues[t_i+deltaT]);		
		QttV.add(0.0);
		ETttV.add(0.0);
		


		/** loop for the computation of the probabilities and of the output fluxes
		 * the loop continues until the value of the residence time pdf
		 * is above the fixed threshold. The computation starts from time
		 * step t=1, because of the initial conditions
		 * */
		t=1;

		while(p_i>threshold& t<dim-t_i){
			
			Q=inDischargevalues[t+t_i+deltaT];
			S_t=inWaterStoragevalues[t+t_i-1+deltaT];
			S_t1=inWaterStoragevalues[t+t_i+deltaT];
			ET=(inETvalues==null)?0.0:inETvalues[t+t_i+deltaT];

			/** StorageSelection value at time t */
			SASvalue_t = distributionValue(t);
			

			P.add(computeP(SASvalue_t,p_i));
						
			pQ.add(computePQ(P.get(t),SASvalue_t));
			
			pET.add(computePET(P.get(t),SASvalue_t));
						
			Q_out.add(computeQ(pQ.get(t)));

			ET_out.add(computeET(pET.get(t)));
			
			QttV.add(computeQtt(t,pQ.get(t)));
			
			ETttV.add(computeETtt(t,pET.get(t)));

			t++;
		}

		/** for each injection time the arrayFin is filled with t_i */
		arrayFin.add((double)t_i);


		System.out.println(t_i);
		/** 
		 * storage of results in hashmaps in which:
		 * on the first column there re the injection times (date column)
		 * and on the first row (value_t) are all the time step
		 */
		if (t_i<dim-1)storeResult(P,pQ,pET,Q_out,ET_out,QttV,ETttV);
		else {		
			storeResult(arrayFin,arrayFin,arrayFin,arrayFin,arrayFin,arrayFin,arrayFin);   
		}

		/** t_i increases for considering the next injection time*/
		t_i++;


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
	 * Distribution value:computation of the storage selection values.
	 *
	 * @return the double value of the SAS
	 */
	public double distributionValue(int t){

		StorageSelectionFunction SAS;
		SAS=SimpleDistributionFactory.createSAS(SAStype,params,((double)t+0.0001)/dim);		
		return SAS.SASvalues(); 
	}
	
	/**
	 * Compute p: computation of the p_back.
	 *
	 * @param SASvalue_t the SAS value at time t, from the method distributionValue()
	 * @return p_i the double value of the p backward
	 */
	public double computeP(double SASvalue_t, double p_i0){

		p_i= 1/S_t1*(-Q*SASvalue_t-ET*SASvalue_t)*p_i0+S_t*p_i0/S_t1;
		p_i=(p_i<0)?0:p_i;
		return p_i;		
	}


/*	*//** DOESN'T WORK
	 * Compute p: computation of the p_back.
	 *
	 * @param SASvalue_t the SAS value at time t, from the method distributionValue()
	 * @return p_i the double value of the p backward
	 *//*
	public double computePi(double SASvalue_t, double p_i0){

		*//**Initialization of the ode*//*
		FirstOrderDifferentialEquations pOde= new PbackODE(S_t, S_t1, Q, ET, SASvalue_t,
				SASvalue_t, p_i0, dt);

		*//**Boundaries conditions*//*
		double[] y = new double[] { p_i0, 0 };

		*//**Choice of the ODE solver *//*	
		SolverODE solver;
		solver=SimpleIntegratorFactory.createSolver(m_solver, dt, pOde, y);
		p_i=solver.integrateValues();
		//System.out.println("p");

		p_i=(p_i<0)?0:p_i;
		return p_i;		
	}*/


	/**
	 * ComputePQ: computation of the p_Q backward.
	 *
	 * @param p_back the backward probability value at time t
	 * @param SASvalue_t the SAS value at time t, from the method distributionValue()
	 * @return the double value of the p_Q backward
	 */
	public double computePQ(double p_back,double SASvalue_t){
		return p_back*SASvalue_t;		
	}


	/**
	 * ComputePET:computation of the p_ET backward.
	 *
	 * @param p_back the backward probability value at time t
	 * @param SASvalue_t the SAS value at time t, from the method distributionValue()
	 * @return the double value of the p_ET backward
	 */
	public double computePET(double p_back,double SASvalue_t){
		return p_back*SASvalue_t;		
	}
	


	/**
	 * ComputeQ: computation of the output discharge .
	 *
	 * @param pQ_back the q_back
	 * @return the double value of the discharge at time t, given t_i
	 */
	public double computeQ(double pQ_back){
		return pQ_back*Q;
	}


	/**
	 * Compute ET: computation of the output ET.
	 *
	 * @param pET_back the e t_back
	 * @return the double value of the ET at time t, given t_i
	 */
	public double computeET(double pET_back){
		return pET_back*ET;
	}

	/**
	 * Compute Qtt: computes the mean travel times.
	 *
	 * @param t_i: the injection time 
	 * @param pQ_back: is the travel times pdfs
	 * @return the double value of the Q travel time, for each t and ti
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeQtt(int t,double pQ_back) throws IOException {
		return t*pQ_back;
	}

	/**
	 * Compute ETtt: computes the mean evapotranspiration times.
	 *
	 * @param t_i: the injection time 
	 * @param pET_back: is the ET times pdfs
	 * @return the double value of the ET travel time, for each t and ti
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeETtt(int t,double pET_back) throws IOException {
		return t*pET_back;
	}


	/**
	 * Store result:storage of the results in Hashmaps .
	 *
	 * @param P the arrayList with the p backward values for a given injection time
	 * @param pQ the arrayList with the pQ backward values for a given injection time
	 * @param pET the arrayList with the pET backward values for a given injection time
	 * @param Q_back the arrayList with the discharge values for a given injection time
	 * @param ET_back the arrayList with the ET values for a given injection time
	 * @param QttV the arrayList with the mean travel times values for a given injection time
	 * @param ETttV the arrayList with the mean ET times values for a given injection time
	 * @throws SchemaException the schema exception
	 */
	private void storeResult(ArrayList<Double> P,ArrayList<Double> pQ,ArrayList<Double> pET, ArrayList<Double> Q_back, 
			ArrayList<Double> ET_back, ArrayList<Double> QttV, ArrayList<Double> ETttV) throws SchemaException {
		outHMPback = new HashMap<Integer, double[]>();
		outHMPQback = new HashMap<Integer, double[]>();
		outHMPETback = new HashMap<Integer, double[]>();
		outHMQback = new HashMap<Integer, double[]>();
		outHMETback = new HashMap<Integer, double[]>();
		outHMQtt=new HashMap<Integer, double[]>();
		outHMETtt=new HashMap<Integer, double[]>();
		for (int k=0;k<P.size();k++){
			outHMPback.put(k, new double[]{(Double) P.get(k)});
			outHMPQback.put(k, new double[]{(Double) pQ.get(k)});
			outHMPETback.put(k, new double[]{(Double) pET.get(k)});
			outHMQback.put(k, new double[]{(Double) Q_back.get(k)});
			outHMETback.put(k, new double[]{(Double) ET_back.get(k)});
			outHMQtt.put(k, new double[]{(Double) QttV.get(k)});
			outHMETtt.put(k, new double[]{(Double) ETttV.get(k)});
		}

	}

}