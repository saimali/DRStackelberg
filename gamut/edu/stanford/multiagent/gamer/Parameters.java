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

package edu.stanford.multiagent.gamer;

import java.util.*;

/**
 * Class to hold parameter array objects and return 
 * parameter info.
 */

public class Parameters
{

    private ParamInfo[] paramInfo;
    private boolean[] paramSet;
    private HashMap parameters;
    private boolean[] userSet; // -- flag indicates param was set by user.

    /**
     * The constructor
     *
     * @param paramInfo an array of parameter information used to set
     * up the hashmap and other necessary variables
     */
    public Parameters(ParamInfo[] paramInfo) {
	this.paramInfo=paramInfo;
	parameters = new HashMap(paramInfo.length);

	paramSet = new boolean[paramInfo.length];
	userSet = new boolean[paramInfo.length];

	for(int i=0; i<paramSet.length; i++)
	    paramSet[i] = userSet[i] = false;
    }



    /**
     * Parses parameters and checks to make sure the required
     * ones are there
     */
    public void setFromParser(ParamParser p)
	throws Exception
    {
	p.setParameters(this);
    }

    /**
     * Parses parameters and checks to make sure the required
     * ones are there
     */
    public void setFromParams(Parameters p)
	throws Exception
    {
	int n=getNParams();

	for(int i=0; i<n; i++)
	    {
	      // -- don't override previously set params
		if(p.isParamSet(getName(i)) && !setByUser(i))
		  setParameter(i, p.getParameter(getName(i)), p.setByUser(getName(i)));
	    }
    }
    
    /**
     * Checks that every parameter has been set somehow
     * 
     * @throws Exception if a required parameter is missing
     */
    public void checkSet()
	throws Exception
    {
	for(int i=0; i<paramInfo.length; i++)
	    if(!paramSet[i])
		throw new Exception ((paramInfo[i].required ? "Required " : "") +
				     "Parameter " + 
				     paramInfo[i].name + " is not set!"
				     );
    }


    /**
     * Returns parameter info in a string with help
     */
    public String getPrintableInfo() {
	String info = new String();

	if (paramInfo.length == 0) {
	    info = "No parameters.";
	}
	else {
	    for(int i=0; i<paramInfo.length; i++)
		info = info + "-" + paramInfo[i].name + 
		    ":\t" + paramInfo[i].help + "\n";
	}

	return info;
    }


    /**
     * Returns name of ith parameter
     */
    public String getName(int i) {
	return paramInfo[i].name;
    }


    /**
     * Returns parameter information
     */
    public ParamInfo[] getParamInfo()
    {
	return paramInfo;
    }
    

    /**
     * Returns parameter set
     */
    public boolean[] getParamSet() 
    {
	return paramSet;
    }


    /**
     * Returns parameters as a hashmap
     */
    public HashMap getParametersAsHash()
    {
	return parameters;
    }

    
    /**
     * Returns number of parameters
     */
    public int getNParams()
    {
	return paramInfo.length;
    }
 

    /**
     * Check if indexed parameter is set
     */
    protected boolean isParamSet(int n)
    {
	return paramSet[n];
    }


    /**
     * Check if parameter with the given name is set
     */
    protected boolean isParamSet(String name)
    {
	return parameters.containsKey(name);
    }

    /**
     * Implement accessors for parameters
     */
    public Object[] getParameters()
    {

	Object[] vals = new Object[paramInfo.length];

	for(int i=0; i<vals.length; i++)
	    vals[i] = getParameter(i);

	return vals;
    }

    public void setParameters(Object[] params)
	throws Exception
    {
	if(params.length!=paramInfo.length)
	    throw new Exception("setParameters: # of parameters differs");

	for(int i=0; i<params.length; i++)
	    setParameter(i, params[i]);
    }

    public Object getParameter(int n)
    {
	return parameters.get(paramInfo[n].name);
    }

    public Object getParameter(String name)
    {
	return parameters.get(name);
    }

    public void setParameter(int n, Object val)
	throws Exception
    {
	setParameter(n, val, false);
    }

    public void setParameter(int n, Object val, boolean byUser)
	throws Exception
    {
	switch(paramInfo[n].type)
	    {
	    case ParamInfo.LONG_PARAM:
		if (! (val instanceof Long) )
		    throw new Exception("setParameter: TYPE ERROR");
		parameters.put(paramInfo[n].name, val);
		paramSet[n] = true;
		userSet[n] = byUser;
		break;
	    case ParamInfo.DOUBLE_PARAM:
		if (! (val instanceof Double) )
		    throw new Exception("setParameter: TYPE ERROR");
		parameters.put(paramInfo[n].name, val);
		paramSet[n] = true;
		userSet[n] = byUser;
		break;
	    case ParamInfo.STRING_PARAM:
		if (! (val instanceof String) )
		    throw new Exception("setParameter: TYPE ERROR");
		parameters.put(paramInfo[n].name, val);
		paramSet[n] = true;
		userSet[n] = byUser;
		break;
	    case ParamInfo.BOOLEAN_PARAM:
		if (! (val instanceof Boolean) )
		    throw new Exception("setParameter: TYPE ERROR");
		parameters.put(paramInfo[n].name, val);
		paramSet[n] = true;
		userSet[n] = byUser;
		break;
	    case ParamInfo.VECTOR_PARAM:
		if (! (val instanceof Vector) )
		    throw new Exception("setParameter: TYPE ERROR");
		parameters.put(paramInfo[n].name, val);
		paramSet[n] = true;
		userSet[n] = byUser;
		break;
	    case ParamInfo.CMDLINE_PARAM:
		if (! (val instanceof ParamParser) )
		    throw new Exception("setParameter: TYPE ERROR");
		parameters.put(paramInfo[n].name, val);
		paramSet[n] = true;
		userSet[n] = byUser;
		break;
	    default:
		throw new Exception("Unknown Param Type " + paramInfo[n].type);
	    }

    }

    public void setParameter(String name, Object val)
	throws Exception
    {
	setParameter(name, val, false);
    }

    public void setParameter(String name, Object val, boolean byUser)
	throws Exception
    {
	int i=getParamIndex(name);
	if(i==-1)
	    throw new Exception("setParameter: Unknown parameter " + name);

	setParameter(i, val, byUser);
    }

    // 
    // Return the index of the given parameter
    // == NOTE: Could be actually done as a hashmap!
    // == NOTE: But this seems to be working, 
    // == NOTE: and currently not a bottleneck.
    //
    protected int getParamIndex(String name)
    {
	for(int i=0; i<paramInfo.length; i++)
	    if(paramInfo[i].name.equals(name))
		return i;
	return -1;
    }

    //
    // For efficiency/convenience, 
    //
    public long getLongParameter(String name)
    {
	return ((Long)getParameter(name)).longValue();
    }

    public double getDoubleParameter(String name)
    {
	return ((Double)getParameter(name)).doubleValue();
    }

    public String getStringParameter(String name)
    {
	return (String)getParameter(name);
    }

    public Vector getVectorParameter(String name)
    {
	return (Vector)getParameter(name);
    }

    public boolean getBooleanParameter(String name)
    {
	return ((Boolean)getParameter(name)).booleanValue();
    }

    public ParamParser getParserParameter(String name)
    {
	return (ParamParser)getParameter(name);
    }


    /**
     * Return true if the parameter was set by the user
     * and false otherwise
     */
    public boolean setByUser(String name) {
	
	int i = getParamIndex(name);
	if(i == -1)
	    {
		System.err.println("FATAL ERROR: Invalid parameter name " 
				   + name);
		System.exit(1);
	    }
	
	return userSet[i];
    }

  public boolean setByUser(int i) {
    return userSet[i];
  }

    /**
     * Randomize a single parameter based on its range.  Note that this will
     * result in the same status as set by user (i.e. non-default).
     */
    public void randomizeParameter(int i)
    {

	try {

	    if(paramInfo[i].required || (paramSet[i] && userSet[i]))
	    	return;
	    /*
	      if( paramSet[i] )
	      {
	      //System.err.println("WARNING: Randomizing Set Parameter " + 
	      //		   paramInfo[i].name);
	      }
	    */
	    switch(paramInfo[i].type)
		{
		case ParamInfo.LONG_PARAM:
		    {
			long high = ((Long)paramInfo[i].high).longValue();
			long low = ((Long)paramInfo[i].low).longValue();
			    
			long val=Math.abs(Global.rand.nextLong());
			val %= (high - low + 1);
			val +=low;
			    
			setParameter(i, new Long(val), true);
		    }
		    break;
		case ParamInfo.DOUBLE_PARAM:
		    {
			double high = ((Double)paramInfo[i].high).doubleValue();
			double low = ((Double)paramInfo[i].low).doubleValue();
			double val=Global.rand.nextDouble();
			val = low + val*(high-low);
			setParameter(i, new Double(val), true);
		    }
		    break;
		case ParamInfo.BOOLEAN_PARAM:
		    setParameter(i, new Boolean(Global.randomBoolean()), true);
		    break;
		case ParamInfo.CMDLINE_PARAM:
		    {
			// Will be set to actual values later
			setParameter(i, ParamParser.emptyParser, true);
		    }
		    break;
		}
		} catch (Exception e) {
	    Global.handleError(e, "Internal error in RandomizeParameters");
	}

    }

    public void randomizeParameter(String name)
    {
	int ind=getParamIndex(name);
	if(ind==-1)
	    {
		System.err.println("FATAL ERROR: Invalid parameter name " 
				   + name);
		System.exit(1);
	    }
	randomizeParameter(ind);
    }

    /**
     * Sets parameters uniformly at random.  Can be overridden in 
     * subclasses for non uniform distribution, and for constraints 
     * on parameters.
     */ 
    public void randomizeParameters()
    {
	for(int i=0; i<paramInfo.length; i++) {
	    randomizeParameter(i);
	}
    }


    //--------------------------------------------------------------

    /**
     * Structure to hold parameter information
     */
    public static class ParamInfo
    {
	public static final int LONG_PARAM=1;
	public static final int DOUBLE_PARAM=2;
	public static final int BOOLEAN_PARAM=3;
	public static final int STRING_PARAM=4;
	public static final int VECTOR_PARAM=5;
	public static final int CMDLINE_PARAM=6;

	public String name;
	public int type;
	public Object low;
	public Object high;
	public String help;
	public boolean required=false;
	public Object defaultValue=null;

	public ParamInfo(String name, int type, Object low, Object high, String help)
	{
	    this.name=name;
	    this.type=type;
	    this.low=low;
	    this.high=high;
	    this.help = help;
	    this.defaultValue = null;
	}
	
	public ParamInfo(String name, int type, Object low, Object high, String help, boolean required)
	{
	    this.name=name;
	    this.type=type;
	    this.low=low;
	    this.high=high;
	    this.help = help;
	    this.required = required;
	    this.defaultValue = null;
	}

	public ParamInfo(String name, int type, Object low, Object high, String help, boolean required, Object defaultValue)
	{
	    this.name=name;
	    this.type=type;
	    this.low=low;
	    this.high=high;
	    this.help = help;
	    this.required = required;
	    this.defaultValue = defaultValue;
	}

    }

}
