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
 * Class for parsing command line parameters
 * Keeps track of bookkeeping as well
 */

public class ParamParser
{
    
    public static ParamParser emptyParser = new ParamParser();

    // ------------------------------------------


    // -- Data to hold semi-parsed arguments
    private HashMap argmap;
    private String description = null;

    private static class ParamPair 
    {
	Vector val;
	boolean used=false;
	
	public ParamPair(Vector val)
	{
	    this.val=val;
	    this.used=false;
	}
    }

    private int usedCount=0;


    public ParamParser()
    {
	argmap=new HashMap();
	usedCount=0;
    }

    /**
     * Constructor to use if the arguments are coming from the 
     * command line and are thus already parsed into an array.
     * Fills in the argmap.
     */
    public ParamParser(String[] args) throws Exception
    {
	argmap=new HashMap();
	usedCount=0;
	
	if (args == null) {
	  description = "[]";
	    return;
	}

	description = "[ " + GameOutput.arrayToString(args, " ") + " ]";

	int i=0;
	while(i<args.length)
	    {
		if(args[i].charAt(0)!='-')
		    throw new Exception("Error Parsing Parameters: " + args[i]);
		
		String key=args[i].substring(1);
		
		Vector val=new Vector();
		while((++i < args.length))
		{
		    // -- Case 1: doesn't start with -
		    // -- So it's either a normal value, or start of "parser" value
		    if(args[i].charAt(0)!='-')
			{
			    // -- If it's not a parser
			    if(args[i].charAt(0)!='[')
				{
				    val.add(args[i]);
				    continue;
				}

			    //-- else deal with parser param.
			    Vector cmdline=new Vector();
			    
			    //-first argument
			    if(args[i].length()!=1) // -- if no space after [
				{
				//cmdline.add(args[i].substring(1));
				    args[i] = args[i].substring(1);
				    i--;
				}

			    // -- add things till last closing ]
			    int openCount=1;
			    while( (openCount > 0) && (++i < args.length) )
				{
				    if(args[i].charAt(0)=='[')
					openCount++;

				    int clcc=closeCount(args[i]);
				    if(clcc>0)
					{
					    openCount-=clcc;
					    if(args[i].length()!=1)
						cmdline.add(args[i].substring(0,args[i].length()-1));
					}
				    else
					cmdline.add(args[i]);
				}
			    if(openCount>0)
				throw new Exception("Unbalanced []!");

			    val.add(cmdline);
			    continue;
			}
		 
		    // -- Case 2: start's with -
		    // -- so it's either next param, or a negative number

		    if(args[i].length()==1)
			throw new Exception("Bad Parameter: -");

		    if(Character.isDigit(args[i].charAt(1)))
			{
			    val.add(args[i]);
			    continue;
			}

		    break; //it start's with -, not a number, so next param
		}
		
                argmap.put(key,  new ParamPair(val));
	    }
    }


    /**
     * Checks number of closing braces ] in a string.
     */
    private static int closeCount(String s)
    {
	int count=0;
	int i=s.length();
	while( (--i>=0) && s.charAt(i)==']')
	    count++;

	return count;
    }

    /**
     * Checks if all arguments are used
     */
    public boolean hasUnusedArgs()
    {
	return (argmap.size() > usedCount);
    }

    /**
     * Returns unused arguments
     */
    public String[] getUnusedArgs()
    {
	String[] args=new String[argmap.size()-usedCount];
	
	Iterator it=argmap.entrySet().iterator();
	String key="";
	int i=0;
	while(it.hasNext())
	    {
		Map.Entry e = (Map.Entry)it.next();
		ParamPair p = (ParamPair)e.getValue();
		if(!p.used)
		    args[i++]=(String)e.getKey();
	    }

	return args;
    }


    /**
     * Fills in hashmap with parameter values
     */
    public void setParameters(Parameters pars) 
	throws Exception
    {
	Parameters.ParamInfo[] info = pars.getParamInfo(); //, HashMap params, boolean[] used

	for(int i=0; i<info.length; i++)
	    {
	      if(pars.isParamSet(i) && !argmap.containsKey(info[i].name))
		continue;

	      // -- Don't override previously set params
	      if(pars.setByUser(i))
		continue;

		boolean used = argmap.containsKey(info[i].name);
		
		if(used)
		    {
		      ParamPair p = (ParamPair)argmap.get(info[i].name);
                      if(!p.used)
			  {
			      p.used=true;
			      argmap.put(info[i].name, p);
			      usedCount++;
			  }
		      Object val=parseParam(p.val, info[i]);
		      pars.setParameter(i, val, true);
		    }
		else if (info[i].defaultValue!=null)
		    pars.setParameter(i, info[i].defaultValue, false);
	    }

    }

    /**
     * Convert String into an appropriate object
     */
    private Object parseParam(Vector val, Parameters.ParamInfo p)
	throws NumberFormatException, Exception
    {
	switch(p.type)
	    {
	    case Parameters.ParamInfo.LONG_PARAM:
		{
		    if(val.size()!=1)
			throw new Exception("Parse error: " + p.name + " takes a single argument!");
		    Long v = Long.valueOf((String)val.get(0));
		    long high=((Long)p.high).longValue();
		    long low=((Long)p.low).longValue();
		    if(v.longValue() < low || v.longValue() > high)
			throw new Exception("Out of range: " + p.name + "=" + v);
		    return v;
		}
		
	    case Parameters.ParamInfo.DOUBLE_PARAM:
		{
		    if(val.size()!=1)
			throw new Exception("Parse error: " + p.name + " takes a single argument!");
		    Double v = Double.valueOf((String)val.get(0));
		    double high=((Double)p.high).doubleValue();
		    double low=((Double)p.low).doubleValue();
		    if(v.doubleValue() < low || v.doubleValue() > high)
			throw new Exception("Out of range: " + p.name + "=" + v);
		    return v;
		}
		
	    case Parameters.ParamInfo.BOOLEAN_PARAM:
		{
		    if(val.size()> 1)
			throw new Exception("Parse error: " + p.name + " takes at most one argument!");
		    if(val.size()==0) // -- used as a flag, indicates true
			return Boolean.TRUE;
		    // -- otherwise need to parse the value
		    try {
			int v = Integer.valueOf((String)val.get(0)).intValue();
			if(v == 0)
			    return Boolean.FALSE;
			if(v == 1)
			    return Boolean.TRUE;
			
			throw new Exception();
		  } catch (Exception e) {
		      throw new Exception("Parse error: " + p.name + " takes 0 or 1 as an argument!");
		  }
		}
		
	    case Parameters.ParamInfo.STRING_PARAM:
		
		if(val.size()!=1)
		    throw new Exception("Parse error: " + p.name + " takes a single argument!");
		return val.get(0); 
		
	    case Parameters.ParamInfo.VECTOR_PARAM:
	      if(val.size()==0)
		throw new Exception("Parse error: " + p.name + " needs at least one argument!");
		return val;
		
	    case Parameters.ParamInfo.CMDLINE_PARAM:
		if(val.size()!=1)
		    throw new Exception("Parse error: " + p.name + " takes a single argument in []!");
		
	      Vector cmdline=(Vector)val.get(0);
	      ParamParser pVal = new ParamParser((String[])cmdline.toArray(new String[]{}));
	      return pVal;
	      
	    default:
	      throw new Exception("INTERNAL EXCEPTION: UNKNOWN PARAM TYPE!");
	    }
    }
    
    // -----------------------------------------------------

    /**
     * Converts to String
     */
    public String toString()
    {
      return description;
    }


    /**
     * Used for debugging only
     */
    public static void main(String[] args) throws Exception
    {
	ParamParser p=new ParamParser(args);
	System.out.println(p);
    }
}

