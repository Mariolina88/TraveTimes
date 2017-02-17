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
package integrator;

import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import java.util.*;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;

import org.geotools.feature.SchemaException;
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.format.DateTimeFormatter;


// TODO: Auto-generated Javadoc
/**
 * The Class InjectionTimeIntegration computes the integral
 * of the outflows obtained from the the residence time theory, in the
 * injection time dimension (dti)
 * @author Marialaura Bancheri
 */
public class InjectionTimeIntegration extends JGTModel{


	@Description("The quantity to be integrated")
	double Q;

	
	@Description("The hashmap with the quantity to be integrated")
	@In	
	public HashMap<Integer, double[]> inQoutvalues;


	@Description("Station ID")
	@In
	public int ID;
	
	@Description("dimension of the innput array for each time step") 
	int dim_i;

	@Description("the injection time") 
	int t_i=0;


	@Description("integration time")
	double dt=1E-4;


	@Description("The output Q time series, "
			+ "integrated in dt_i ")
	@Out
	public HashMap<Integer, double[]> outHMQ;


	@Description("the vector with the results of Q integration ") 
	Double[] vectorQ;
	
	private DateTimeFormatter formatter = JGTConstants.utcDateFormatterYYYYMMDDHHMM;
	
	@Description("First date of the simulation")
	@In
	public String tStartDate;


	@Description("Last date of the simulation")
	@In
	public String tEndDate;


	/**
	 * Process.
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception {

		/** ArrayList containing the time series of Q and ET values*/
		ArrayList<Double> valueQ = new ArrayList<Double>();
		
		/** since the dimension of the input hashmap varies in time
		 * dim_i computes it for the following loop */
		DateTime start = formatter.parseDateTime(tStartDate);
		DateTime end = formatter.parseDateTime(tEndDate);
		int dim_max=Hours.hoursBetween(start, end).getHours()+1;
		
		dim_i=inQoutvalues.size();
		
		

		/** this loop is necessary for the data input:
		*t_i is the injection time, t is the general time-step
		*/
		for (int t=0;t<dim_i;t++){

			Integer basinId = t;

			Q = inQoutvalues.get(basinId)[0]*dt;
			if (isNovalue(Q)) Q= 0;

			valueQ.add(Q);

		}

		/** Q values are stored in two fixed arrays (valueQ and valueET)
		*for the first injection-time (t_i==0) 
		* and then the sums of Q  are computed for all the other injection times
		*/
		if (t_i<dim_max-1){
		vectorQ = (t_i== 0) ? toArray(valueQ, dim_max) : computeSum(valueQ, vectorQ);
		/** storage of results in hashmaps*/
		storeResult(t_i,vectorQ);

		}else {
			outHMQ.put(ID, new double[]{0});
		}

		
		
		t_i++;
	}

	/**
	 * To array:trasformation from the ArrayList to the Array
	 *
	 * @param value: arrayList with the values at t=0
	 * @param dim: dimension of the vector 
	 * @return the double[] vector with the values at t=0
	 */
	public Double[] toArray(ArrayList<Double> value, int dim) {
		Double[] vector = new Double[dim];
		value.toArray(vector);

		return vector;
	}


	/**
	 * Compute sum: integral of the values along t_i
	 *
	 * @param value: arraylist with the values at t>0
	 * @param vector: vactor with the values at t>0
	 * @return the double[] vector with the sum for each t_i
	 */
	public Double[] computeSum(ArrayList<Double> value, Double[] vector) {

		Double[] tmpVector = new Double[dim_i];
		value.toArray(tmpVector);
		
		for (int i=0; i<tmpVector.length; i++) {
			if(vector[i+t_i]==null) vector[i+t_i]=0.0;
			vector[i+t_i] += tmpVector[i];
		}

		return vector;

	}


	/**
	 * 	Storage of the results in time series at a given injection-time
	 *
	 * @param t_i: injection time
	 * @param vectorQ: the vector of integrated discharge values
	 * @param vectorET the vector of integrated et values
	 * @throws SchemaException the schema exception
	 */
	private void storeResult(int t_i, Double[] vectorQ) throws SchemaException {
		outHMQ = new HashMap<Integer, double[]>();

		outHMQ.put(ID, new double[]{vectorQ[t_i]});
	


	}	
	
	

}
