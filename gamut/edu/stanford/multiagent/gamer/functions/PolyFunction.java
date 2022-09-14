/*
 * Copyright (C) 2004 Jennifer Wortman, Eugene Nudelman, Kevin Leyton-Brown, Yoav Shoham.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.stanford.multiagent.gamer.functions;

import java.util.*;
import edu.stanford.multiagent.gamer.*;


/**
 * Class implements a polynomial function of a single argument
 */

public class PolyFunction extends Function
{
    // -- Constants for minimization
    protected static final double EPSILON = 0.00001;
    protected static final int MAX_ITER = 100000;
    protected static final int N_RESTARTS = 50;

    // -- Parameters

    private static Parameters.ParamInfo pDegree;
    private static Parameters.ParamInfo pCoefs;
    private static Parameters.ParamInfo pCoefMin;
    private static Parameters.ParamInfo pCoefMax;
    protected static Parameters.ParamInfo[] pfParam;

    static {

	pDegree = new Parameters.ParamInfo("degree", Parameters.ParamInfo.LONG_PARAM, new Long(0), new Long(10), "degree of the polynomial function.");

	// The range on this variable is meaningless since it is a vector.
	pCoefs = new Parameters.ParamInfo("coefs", Parameters.ParamInfo.VECTOR_PARAM, new Double(0), new Double(0), "coefficients of polynomial (should be a list of d+1 numbers where d is the degree), in the increasing order of degree.");

	pCoefMax = new Parameters.ParamInfo("coef_max", Parameters.ParamInfo.DOUBLE_PARAM, new Double(-1000), new Double(1000), "upper bound on polynomial coefficients, used only for randomizing.  Can be anywhere from -1000 to 1000 but defaults to 10.",false, new Double(10));

	pCoefMin = new Parameters.ParamInfo("coef_min", Parameters.ParamInfo.DOUBLE_PARAM, new Double(-1000), new Double(1000), "lower bound on polynomial coefficients, used only for randomizing.  Can be anywhere from -1000 to 1000 but defaults to -10.",false, new Double(-10));

	pfParam = new Parameters.ParamInfo[] {pDegree, pCoefs, pCoefMin, pCoefMax};

	Global.registerParams(PolyFunction.class, pfParam);

    }


    // --------------------------------------------------

    protected  String getFunctionHelp()
    {
	return "PolyFunction: Represents a general polynomial. Coefficients and degree can either be specified explicitely, or randomized to lie within given ranges.";
    }

    protected double[] coefs;

    public PolyFunction() 
	throws Exception
    {
	super();
    }
    
    public PolyFunction(double[] coefs, double dMin, double dMax)
	throws Exception
    {
	this.coefs=coefs;
	this.dMin=dMin;
	this.dMax=dMax;
    }
 


    public void initialize()
	throws Exception
    {
	super.initialize();
    }


    protected void checkParameters() 
	throws Exception
    {
	this.coefs = Global.parseDoubleArray((Vector)getParameter("coefs"));

	if (coefs.length != getLongParameter("degree") + 1) 
	    throw new Exception("The number of coefficients must be " +
				"equal to the degree of the polynomial + 1.");

	if(dMin > dMax)
	    throw new Exception("min > max in polynomial domain: " + 
				"[" + dMax + ", " + dMin + "]");

    }
	    

    /**
     * The only thing involved in generating the polynomial is
     * storing the coefficients in a more easily accessable variable.
     */
    public void doGenerate()
    {
	coefs = Global.parseDoubleArray((Vector) getParameter("coefs"));
    }


    /**
     * Evaluate the polynomial
     */
    public double eval(double x)
    {
	double val=coefs[0];
	double y=x;

	for(int i=1; i<coefs.length; i++)
	    {
		val+=coefs[i]*y;
		y*=x;
	    }

	return val;
    }


    /**
     *Randomize things
     */
    public void randomizeParameters()
    {
	// -- First, degree
	if(!parameters.setByUser(pDegree.name))
	    parameters.randomizeParameter(pDegree.name);

	// -- Then, coefficients
	double coefMin = getDoubleParameter(pCoefMin.name);
	double coefMax = getDoubleParameter(pCoefMax.name);

	if(!parameters.setByUser(pCoefs.name))
	    {
		int degree = (int)getLongParameter(pDegree.name);
		coefs=new double[degree+1];

		Vector coefVector=new Vector();

		for(int i=0; i<=degree; i++)
		    {
			coefs[i]=Global.randomDouble(coefMin, coefMax);
			coefVector.add(String.valueOf(coefs[i]));
		    }

		try {
		    parameters.setParameter(pCoefs.name, coefVector);
		} catch (Exception e) {
		    Global.handleError(e, "Randomizing polynomial!");
		}
	    }
	
    }

    /**
     * Finds the minimum value of the function.
     * 
     * Note that this is probably not the best way to go about it.
     * Ideally, should be changed to a better algorithm later.
     */
    public double getMinimum()
    {

	PolyFunction deriv=null;
	try {
	    deriv=getDerivative();
	} catch (Exception e) {
	    Global.handleError(e, "Couldn't make derivative!");
	}

	Vector v = new Vector();
	v.add(new Double(dMin));
	v.add(new Double(dMax));

	getMinima(dMin, dMax, deriv, v);

	double minval = eval(dMin);

	for(int i=0; i<v.size(); i++)
	    {
		double x = ((Double)v.get(i)).doubleValue();
		double val=eval(x);
		minval=Math.min(val, minval);
	    }

	return minval;
    }

    /**
     * Appends all local minima in the specified interval to v.
     */
    public void getMinima(double a, double b, PolyFunction deriv, Vector v)
    {

	double x=getMinimum(a,b, deriv);

	if( Math.abs(a-x) > EPSILON && Math.abs(b-x) > EPSILON )
	    {
		v.add(new Double(x));

		getMinima(x+EPSILON, b, deriv, v);
		getMinima(a, x-EPSILON, deriv, v);
	    }

    }

    /**
     * Does gradient descent to get a local minimum in an interval a,b.
     */
    public double getMinimum(double a, double b, PolyFunction deriv)
    {
	double min=b;
	double minVal=eval(b);
	if(eval(a) <  eval(b))
	    {
		min=a;
		minVal=eval(a);
	    }

	int nIter=0;
	double alpha=(b-a)/10000;

	Random rand=new Random(System.currentTimeMillis());

	for(int n=0; n<N_RESTARTS; n++)
	    {
		double x;
		double newx=a+(b-a)*rand.nextDouble();
		
		nIter=0;
		
		do    
		    {
			x=newx;
			double xval=eval(x);
			if(xval<minVal)
			    {
				min=x;
				minVal=xval;
			    }
		     
			newx=x-alpha*deriv.eval(x);
			if(newx<a) newx=a;
			if(newx>b) newx=b;
		     
			nIter++;

		    } while (Math.abs(newx-x)>EPSILON && nIter <=MAX_ITER);

		//if(nIter >= MAX_ITER)
		    //System.err.println("WARNING: #iterations exceeded in PolyFunction");

		if(eval(newx)<minVal)
		    min=newx;
	    }

	return min;
    }

    /**
     * Returns a derivative polynomial.
     */
    public PolyFunction getDerivative()
	throws Exception
    {
	if(coefs.length==1)
	    return new PolyFunction(new double[] {0.0}, dMin, dMax); // f'=0

	// -- otherwise differntiate
	double[] deriv=new double[coefs.length-1];

	for(int i=1; i<coefs.length; i++)
	    deriv[i-1]=coefs[i]*i;

	return new PolyFunction(deriv, dMin, dMax);
    }

    /**
     * Integrates a polynomial and adds random constant term.
     */
    public PolyFunction integrate()
	throws Exception
    {
	double[] newpoly=new double[coefs.length+1];

	Vector v=new Vector();

	newpoly[0]=Global.randomDouble(getDoubleParameter("coef_min"), getDoubleParameter("coef_max"));

	v.add(String.valueOf(newpoly[0]));
	
	for(int i=1; i<=coefs.length; i++)
	    {
		newpoly[i]=coefs[i-1]/(double)i;
		v.add(String.valueOf(newpoly[i]));
	    }
	    
	PolyFunction integral=new PolyFunction(newpoly, dMin, dMax);
	integral.setParameter("degree", new Long(coefs.length));
	integral.setParameter("coefs", v);

	return integral;
    }


    /**
     * Used only for testing.
     */
    public static void main(String[] args)
	throws Exception
    {
	Global.rand=new Random(    System.currentTimeMillis() );

	PolyFunction p = new PolyFunction();
	p.setParameters(new ParamParser(args), true);
	p.initialize();
	p.doGenerate();

	double min=p.getMinimum();
	
	System.out.println("\n---\n" + min + "\n------\n");
	for(int i=p.coefs.length-1; i>=0; i--)
	    System.out.print(p.coefs[i] + " ");
	System.out.println("");
    }
}

