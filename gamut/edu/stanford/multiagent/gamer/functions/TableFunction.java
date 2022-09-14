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
 * Class implements a table lookup function for truly random 
 * table functions.
 */

public class TableFunction extends Function
{

    // -- Parameters
  
    protected static Parameters.ParamInfo pMin;
    protected static Parameters.ParamInfo pMax;
    protected static Parameters.ParamInfo pPoints;
    protected static Parameters.ParamInfo[] tfParam;
  
    static {

	pMin = new Parameters.ParamInfo("min", Parameters.ParamInfo.DOUBLE_PARAM, new Double(-1000), new Double(1000), "minimum of the function", false, new Double(-1));
    
	pMax = new Parameters.ParamInfo("max", Parameters.ParamInfo.DOUBLE_PARAM, new Double(-1000), new Double(1000), "maximum of the function", false, new Double(1));
    
	pPoints = new Parameters.ParamInfo("points", Parameters.ParamInfo.LONG_PARAM, new Long(2), new Long(10000), "number of points in the table lookup",false, new Long(100));

	tfParam = new Parameters.ParamInfo[] {pMin, pMax, pPoints};

	Global.registerParams(TableFunction.class, tfParam);
    
    }


    // --------------------------------------------------


    protected  String getFunctionHelp()
    {
	return "TableFunction: Represents a general function as a random table of points. Function is evaluated by looking up nearest point to the x value. No interpolation is done.";
    }
  
    // -- the table
    protected double[] table;

    public TableFunction() 
	throws Exception
    {
	super();
    }
    
  
    public void initialize()
	throws Exception
    {
	super.initialize();
    }
  

    protected void checkParameters() 
	throws Exception
    {
	if(getDoubleParameter(pMin.name) >= getDoubleParameter(pMax.name))
	    throw new Exception("min>=max!");

	if(dMin >= dMax)
	    throw new Exception("min >= max in domain domain!");

    }


    /**
     * Randomizes things
     */
    public void randomizeParameters()
    {
	parameters.randomizeParameter(pPoints.name);

	if(parameters.setByUser(pMin.name))
	    {
		pMax.low = parameters.getParameter(pMin.name);
		parameters.randomizeParameter(pMax.name);
	    }
	else
	    {
		parameters.randomizeParameter(pMax.name);
		pMin.high=parameters.getParameter(pMax.name);
		parameters.randomizeParameter(pMin.name);
	    }
    }	    

    /**
     * Generates the table.
     */
    public void doGenerate()
    {
	table = new double[(int)getLongParameter(pPoints.name)];
	double min = getDoubleParameter(pMin.name);
	double max = getDoubleParameter(pMax.name);
	
	for(int i=0; i<table.length; i++)
	    table[i]=Global.randomDouble(min, max);
    }
    

    /**
     * Evaluates the table, by finding nearest point.
     */
    public double eval(double x)
    {
	if(x<=dMin)
	    return table[0];
	
	if(x>=dMax)
	    return table[table.length-1];
	
	// -- get index
	double r = (x-dMin)/(dMax-dMin);
	
	int ind=(int)(r*table.length);
	return table[ind];
    }


    /**
     * Used only for testings
     */
    public static void main(String[] args)
	throws Exception
    {

	Global.rand=new Random(    System.currentTimeMillis() );

	TableFunction p = new TableFunction();
	p.setParameters(new ParamParser(args), true);
	p.initialize();
	p.doGenerate();

	for(int i=0; i<p.table.length; i++)
	    System.out.print(p.table[i] + "  ");
	System.out.println("\n--------------------------");
	System.out.println(p.eval(0));
	System.out.println(p.eval(1));
	System.out.println(p.eval(0.5));
	System.out.println(p.eval(0.7));
    }
}

