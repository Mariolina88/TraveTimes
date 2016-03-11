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

// TODO: Auto-generated Javadoc
/**
 * A simple factory pattern that create concrete functions
 * for the storage selection behavior: beta distribution and uniform (non yet 
 * implemented) . Inputs are:the string type containing the chosen function, 
 * the vector containing the parameters of the 
 * distribution, the independent variable, whose density is calculated.
 * @author Marialaura Bancheri
 */
public class SimpleDistributionFactory {
	
	/**
	 * Creates a new SimpleDistribution object.
	 *
	 * @param type: the string type with the chosen function
	 * @param params: vector containing the parameters of the 
	 * distribution
	 * @param x: is the independent variable, whose density function is calculated
	 * @return the storage selection function
	 */
	public static StorageSelectionFunction createSAS(String type,final double[] params, 
												final double x){
		StorageSelectionFunction SAS=null;
		
		if (type.equals("BetaDistribution")){
			SAS=new BetaD(params,x);
		} 
		
		if (type.equals("UniformDistribution")){
			SAS=new UniformD();
		}
			
		return SAS;
	}

}
