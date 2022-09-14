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
import java.io.*;

/**
 * Abstract class that can be extended by any class
 * which needs to use the parameter set-up.
 */

public abstract class ParameterizedObject
{
    protected Parameters parameters;

    // ----------------------------------------------------
    // Shortcuts to parameter functions

    /**
     * Returns the Parameters object of the class.
     */
    public Parameters getParameters() {
	return parameters;
    }

    /**
     * Returns the value of the parameter with the given name
     * as an Object.
     *
     * @param name parameter to return
     */
    public Object getParameter(String name) {
	return parameters.getParameter(name);
    }
    
    /**
     * Returns the value of the parameter with the given name
     * as a long (assuming the parameter is of type long).
     *
     * @param name parameter to return
     */
    public long getLongParameter(String name) {
	return parameters.getLongParameter(name);
    }

    /**
     * Returns the value of the parameter with the given name
     * as a boolean (assuming the parameter is of type boolean).
     *
     * @param name parameter to return
     */
    public boolean getBooleanParameter(String name) {
	return parameters.getBooleanParameter(name);
    }

    /**
     * Returns the value of the parameter with the given name
     * as a double (assuming the parameter is of type double).
     *
     * @param name parameter to return
     */
    public double getDoubleParameter(String name) {
	return parameters.getDoubleParameter(name);
    }

    /**
     * Returns the value of the parameter with the given name
     * as a String (assuming the parameter is of type String).
     *
     * @param name parameter to return
     */
    public String getStringParameter(String name) {
	return parameters.getStringParameter(name);
    }

    /**
     * Sets the value of a parameter.
     *
     * @param name parameter to set the value of
     * @param val value to set
     * @throws Exception
     */
    public void setParameter(String name, Object val)
	throws Exception
    {
	parameters.setParameter(name, val);
    }

    /**
     * Sets the value of a parameter.
     *
     * @param name parameter to set the value of
     * @param val value to set
     * @param byUser should be true if the parameter was set by the
     *               user on the command line
     * @throws Exception
     */
    public void setParameter(String name, Object val, boolean byUser)
	throws Exception
    {
	parameters.setParameter(name, val, byUser);
    }

    // ------------------------------------------------------


    /**
     * The consctructor.  Must be called by all subclasses
     * It sets all parameter names/types and parses the parameter 
     * values passed to it
     */
    protected ParameterizedObject()
	throws Exception
    {
	Parameters.ParamInfo[] paramInfo=Global.getClassParamInfo(getClass());
	parameters = new Parameters(paramInfo);
    }


    //
    // Set Parameters in batch mode
    //

    /**
     * Sets multiple parameters at once using a ParamParser.
     *
     * @param p the ParamParser containing the parameters and 
     *          their values
     * @param randomize should be set to true if it is desired
     *                  that any unset parameters be randomized
     * @throws Exception
     */
    public void setParameters(ParamParser p, boolean randomize)
	throws Exception
    {
	parameters.setFromParser(p);

	if(randomize)
	    randomizeParameters();
    }

    /**
     * Sets multiple parameters at once using a Parameters variable.
     *
     * @param p the Parameters variable containing the parameters and 
     *          their values
     * @param randomize should be set to true if it is desired
     *                  that any unset parameters be randomized
     * @throws Exception
     */

    public void setParameters(Parameters p, boolean randomize)
	throws Exception
    {
	parameters.setFromParams(p);

	if(randomize)
	    randomizeParameters();
    }

    /**
     * Initializes using preset parameter values.
     *
     * @throws Exception
     */
    public void initialize()
	throws Exception
    {
	// -- Assumes That Parameters Are already set
	parameters.checkSet();
	checkParameters();
    }

    
    /**
     * Sets values of any unset parameters randomly.  Can and should be 
     * overridden in subclasses to handle a non-uniform distribution
     * and for constraints on parameters.
     */  
    public void randomizeParameters()
    {
	parameters.randomizeParameters();
    }


    /**
     * Constructs a string with all parameter values.
     *
     * @param sep seperator to be placed between parameter values
     */
    public String getParamDescription(String sep)
    {
	StringBuffer buff = new StringBuffer();
	
	int n = parameters.getNParams();
	for(int i=0; i<n; i++)
	    buff.append(parameters.getName(i) + ":\t" + 
			parameters.getParameter(i) + (i < n-1 ? sep : ""));
	
	return buff.toString();
    }
    
    
    /**
     * Returns description of the class.  Subclasses should override.
     */
    public String getDescription()
    {
	// -- by default, just spit out parameters
	return "{ " + getParamDescription(", ") + " }";
    }
    
    /**
     * Checks if Parameter values are consistent.  Must be
     * implemented by every non-abstract subclass of
     * ParameterizedObject.
     *
     * @throws Exception if anything is wrong with the parameter
     *                   values
     */
    protected abstract void checkParameters() throws Exception;


    /**
     * Generate a (random) instance of the subclass based on 
     * input parameters.
     */
    public abstract void doGenerate();

    /**
     * Return the help screen
     */
    public abstract String getHelp();
}



