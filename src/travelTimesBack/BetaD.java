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

import org.apache.commons.math3.distribution.BetaDistribution;

import travelTimesBack.StorageSelectionFunction;

// TODO: Auto-generated Javadoc
/**
 * The Class BetaD is the concrete implementation of the StorageSelectionFunction
 * interface. It implements the Beta distribution for the storage selection function.
 * @author Marialaura Bancheri
 */
public class BetaD implements StorageSelectionFunction {


	final BetaDistribution distribution;
	

	final double alpha;
	

	final double beta;
	

	final double x;

	/**
	 * Instantiates a new beta distribution.
	 *
	 * @param params: vector containing the alpha and beta parameters of the 
	 * distribution
	 * @param x: is the independent variable, whose density function is calculated
	 */
	public BetaD(final double[] params, final double x){
		if (params.length>2) System.out.println("Too many paramneters for the BetaDistribution");
		this.alpha = params[0];
		this.beta = params[1];
		this.x = x;
		this.distribution = new BetaDistribution(alpha, beta);

	}

	/* (non-Javadoc)
	 * @see residenceTimes.StorageSelectionFunction#SASvalues()
	 */
	public double SASvalues() {
		return distribution.density(x);
	}

}
