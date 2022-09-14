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

import edu.stanford.multiagent.gamer.*;

/**
 * A Wrapper that takes an arbitrary increasing function,
 * and turns it into a decreasing one (by negating)
 */


public class DecreasingWrapper extends Function
{
    // Parameters: The name of the function to wrap
 
    private static Parameters.ParamInfo pBase;
    private static Parameters.ParamInfo pMin;
    private static Parameters.ParamInfo pFuncParams;
    private static Parameters.ParamInfo[] dcrParams;

    static {
    
	pBase = new Parameters.ParamInfo("base_func", Parameters.ParamInfo.STRING_PARAM, null, null, "name of the function to wrap. must be increasing.");
    
	pMin = new Parameters.ParamInfo("min", Parameters.ParamInfo.DOUBLE_PARAM, new Double(-1000), new Double(1000), "the minimum of the function", false, new Double(0));

	pFuncParams = new Parameters.ParamInfo("base_params", Parameters.ParamInfo.CMDLINE_PARAM, null, null, "parameters to be handed off to the base function, must be enclosed in [].", false, ParamParser.emptyParser);

	dcrParams = new Parameters.ParamInfo[] {pBase, pMin, pFuncParams};
	Global.registerParams(DecreasingWrapper.class, dcrParams);
    }


    // --------------------------------------------------
  
    boolean randomize=false;

    public DecreasingWrapper()
	throws Exception
    {
	super();
    }
  
    protected String getFunctionHelp()
    {
	return "DecreasingWrapper: Takes an increasing base function, and makes it descreasing by negating and shifting up.";
    }

    public void initialize()
	throws Exception
    {
	super.initialize();
    }

  
    public void setParameters(ParamParser p, boolean randomize)
	throws Exception
    {
	super.setParameters(p, randomize);
	this.randomize=randomize;
    }
  
    /**
     * Randomize things
     */
    public void randomizeParameters()
    {
	// -- First, base function
	try {
	    if(!parameters.setByUser(pBase.name))
		parameters.setParameter(pBase.name, Global.getRandomClass(Global.FUNC, "IncreasingFunction"));
	} catch (Exception e) {
	    Global.handleError(e, "Randomizing Decreasing Wrapper");
	}
    
	parameters.randomizeParameter(pMin.name);
    }  


    protected void checkParameters() 
	throws Exception
    {
	if(dMax <= dMin)
	    throw new Exception("Bad Range!");

	String fName = getStringParameter(pBase.name);
	if(!Global.isPartOf(Global.FUNC, fName, "IncreasingFunction"))
	    throw new Exception("Base function must be increasing!");
    }
	    
    // -- Things used to compute value
    Function fBase;
    double add;

    /**
     * Computes the additive constant to satisfy the min parameter.
     */
    public void doGenerate()
    {
	fBase = (Function)Global.getObjectOrDie(getStringParameter(pBase.name), Global.FUNC);
	try {
	    fBase.setDomain(getDMin(), getDMax());
	    fBase.setParameters(parameters.getParserParameter(pFuncParams.name),randomize);
	    fBase.initialize();
	    fBase.doGenerate();
	} catch (Exception e) {
	    System.err.println(getHelp());
	    System.err.println(fBase.getHelp());
	    Global.handleError(e, "Base Function Parameters");
	}


	double min=getDoubleParameter(pMin.name);
	double act=-fBase.eval(dMax); // -f is smallest at dMax
	add = min - act;
    }


    public double eval(double x)
    {
	return - fBase.eval(x) + add;
    }

    public String getDescription()
    {
	StringBuffer buff=new StringBuffer();
	buff.append("DecreasingWrapper(");
	buff.append(getParamDescription(", "));
	buff.append(")\nWrapped::");
	buff.append(fBase.getDescription());
	buff.append("\t");
	return buff.toString();
    }
}
