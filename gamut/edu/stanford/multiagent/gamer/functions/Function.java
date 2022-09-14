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
 * An abstract class for single-arity function classes
 */

public abstract class Function extends ParameterizedObject
{
    public Function() 
	throws Exception
    { 
	super();
    }
    
    
    /**
     * Return the help screen
     */
    public String getHelp()
    {
	StringBuffer buff=new StringBuffer();
	buff.append(Global.wrap(getFunctionHelp(), 70));
	
	buff.append("\n\nFunction Parameters:\n");
	buff.append(Global.wrap(parameters.getPrintableInfo(), 70));
	
	
	return buff.toString();
    }
    
    
    /**
     * Returns a help string describing the function and the 
     * parameters taken by the function
     */
    protected  abstract String getFunctionHelp();

    /**
     * Evaluates the function at a single point and return
     * the value
     *
     * @param x the point at which to evaluate the function
     */
    public abstract double eval(double x);


    /**
     * Calls initialize in the super class ParemeterizedObject which 
     * checks parameters
     *
     * @throws Exception if there is a problem with the parameters
     */
    public void initialize()
	throws Exception
    {
	super.initialize();
    }

  
    //-- Domain interval
    protected double dMin=0, dMax=1; // -- domain interval

    /**
     * Sets the domain range.  This range must be set before 
     * randomization happens.
     *
     * @param min minimum value in the domain
     * @param max maximum value in the domain
     */
    
    public void setDomain(double min, double max)
    {
	dMin=min;
	dMax=max;
    }
    
    /**
     * Returns the minimum value in the domain.
     */
    public double getDMin()
    {
	return dMin;
    }
    
    /**
     * Returns the maximum value in the domain.
     */
    public double getDMax()
    {
	return dMax;
    }
}
