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
 * Class implements an increasing polynomial function of a single argument
 */

public class IncreasingPoly extends PolyFunction
{
    static {
	Global.registerParams(IncreasingPoly.class, PolyFunction.pfParam);
    }


    protected  String getFunctionHelp()
    {
	return "IncreasingPoly: Represents an increasing polynomial. Coefficients and degree can either be specified explicitely, or randomized to lie within given ranges.";
    }

    public IncreasingPoly() 
	throws Exception
    {
	super();
    }
    
    public IncreasingPoly(double[] coefs, double dMin, double dMax)
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
	super.checkParameters();

	if(parameters.setByUser("coefs"))
	    {
		PolyFunction deriv=getDerivative();
		double min=deriv.getMinimum();
		
		
		if(min<0)
		    throw new Exception("This polynomial doesn't seem to be increasing on its domain!");
	    }
	
    }

    /**
     * Randomize things
     * 
     * This is not the most efficient algorithm. It works, but 
     * something more robust can replace it later.
     */
    public void randomizeParameters()
    {
	try {

	    // -- First, degree
	    if(!parameters.setByUser("degree"))
		parameters.randomizeParameter("degree");


	    // -- Then, coefficients
	    double coefMin = getDoubleParameter("coef_min");
	    double coefMax = getDoubleParameter("coef_max");
	    int degree = (int)getLongParameter("degree");

	    if(!parameters.setByUser("coefs"))
		{
		
		    if(degree==0)
			{
			    // -- Nothing to do, it's flat
			    Vector v=new Vector();
			    double c=Global.randomDouble(coefMin, coefMax);
			    v.add(String.valueOf(c));
			    setParameter("coefs", v);
			    return;
			}

		    // -- else need a random derivative

		    PolyFunction deriv=new PolyFunction();
		    deriv.setDomain(dMin, dMax);
		    deriv.setParameters(this.parameters, false);
		    deriv.setParameter("degree", new Long(degree-1), true);
		    deriv.randomizeParameters();
		    deriv.initialize();
		    deriv.doGenerate();

		    double min=deriv.getMinimum();


		    if(min<=0)
			{
			    deriv.coefs[0]-=min;
			    deriv.coefs[0]+=Global.randomDouble(0,coefMax);
			}
		
		    PolyFunction integral=deriv.integrate();
		    setParameter("coefs", integral.getParameter("coefs"));
		    this.coefs=integral.coefs;
		    this.dMin=integral.dMin;
		    this.dMax=integral.dMax;

		}

	} catch (Exception e) {
	    Global.handleError(e, "Random Increasing Poly");
	}
    }


    /**
     * Used only for testing
     */
    public static void main(String[] args)
	throws Exception
    {
	Global.rand=new Random(    System.currentTimeMillis() );

	PolyFunction p = new IncreasingPoly();
	p.setDomain(1,3);
	p.setParameters(new ParamParser(args), true);

	p.initialize();
	p.doGenerate();
	
	for(int i=p.coefs.length-1; i>=0; i--)
	    System.out.print(p.coefs[i] + " ");
	System.out.println("");
    }
}
