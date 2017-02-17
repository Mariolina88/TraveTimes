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

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

// TODO: Auto-generated Javadoc
/**
 * The Class PbackODE implements the first order differential equation 
 * for the computation of the residence time pdf, given the water storage,
 * the discharge and AET. Omega_Q and omega_ET are the values of the 
 * storage selection functions. The output is p_back(t-ti|ti). 
 */
public class PbackODE implements FirstOrderDifferentialEquations{
	
	public double  S_t;

	public double  S_t1;
	
	public double Q;
	
	public double ET;
	
	public double omega_Q;
	
	public double omega_ET;
	
	public double p_back;
	
	public double dt;
	
	
	/**
	 * Instantiates the first order differential equation for the
	 * the computation of p_back.
	 *
	 * @param S_t: the water storage at time t
	 * @param S_t1: the water storage at time t+1
	 * @param Q: the discharge
	 * @param ET: the AET
	 * @param omega_Q: the SAS for the discharge
	 * @param omega_ET:the SAS for the ET
	 * @param p_back: backward probability at time t-1
	 * @param dt: integration time
	 */
	public PbackODE(double S_t, double S_t1, double Q, double ET, 
			double omega_Q, double omega_ET, double p_back, double dt){
		this.S_t=S_t;
		this.S_t1=S_t1;
		this.Q=Q;
		this.ET=ET;
		this.omega_Q=omega_Q;
		this.omega_ET=omega_ET;
		this.p_back=p_back;
		this.dt=dt;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.math3.ode.FirstOrderDifferentialEquations#getDimension()
	 */
	public int getDimension() {
		return 2;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.math3.ode.FirstOrderDifferentialEquations#computeDerivatives(double, double[], double[])
	 */
	public void computeDerivatives(double t, double[] y, double[] yDot)
			throws MaxCountExceededException, DimensionMismatchException {
		yDot[0] = 1/S_t1*(-Q*omega_Q-ET*omega_ET)*p_back-(S_t1-S_t)/dt*p_back/S_t1;
			
	}

}
