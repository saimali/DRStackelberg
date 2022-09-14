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
 *Class implements a table lookup incr. function (for truly random table functions)
 *
 */

public class IncreasingTableFunction extends TableFunction
{

  // -- Parameters
 
  static {
    Global.registerParams(IncreasingTableFunction.class, tfParam); 
  }


  // --------------------------------------------------


  protected  String getFunctionHelp()
  {
    return "IncreasingTableFunction: Represents a general increasing function as a table of points. Function is evaluated by looking up nearest point to the x value. No interpolation is done.";
  }
  

  public IncreasingTableFunction() 
    throws Exception
  {
    super();
  }
    
  
  public void initialize()
    throws Exception
  {
    super.initialize();
  }
  

  // -- Convert a derivative into a function
  protected void intAndNorm(double[] deriv)
  {
    double min = getDoubleParameter(pMin.name);
    double max = getDoubleParameter(pMax.name);
    int nPoints=(int)getLongParameter(pPoints.name);

    // -- Generate unnormalized points
    table = new double[nPoints];
    table[0] = 0;
    double tMin=0, tMax=0;
    for(int i=1; i<table.length; i++)
      {
	table[i] = table[i-1] + deriv[nPoints - i];

	if(table[i] < tMin)
	  tMin = table[i];
	if(table[i] > tMax)
	  tMax = table[i];
      }

    //    System.err.println(pMin.name);
    //System.err.println(min + ", " + max + "\t" + tMin + ", " + tMax);

    // -- Stretch to match max and min
    double f=(max-min) / (tMax-tMin);
    for(int i=0; i<table.length; i++)
      table[i] = (table[i]-tMin) * f + min;
  }


  // -- Recusively generate increasing function
  protected void recGenerate(int minIdx, int maxIdx, double min, double max)
  {
    // -- assume boundary elements are set
    
    // -- base case
    if(maxIdx - minIdx <=1)
      return; //-- nothing to do
    
    // -- otherwise
    int idx = Global.randomInt(minIdx+1, maxIdx-1);
    table[idx] = Global.randomDouble(min, max);

    // -- left
    recGenerate(minIdx, idx, min, table[idx]);
    // --right
    recGenerate(idx, maxIdx, table[idx], max);
  }

  // -- generate the table
  public void doGenerate()
  {
    int nPoints=(int)getLongParameter(pPoints.name);
    double min = getDoubleParameter(pMin.name);
    double max = getDoubleParameter(pMax.name);

    table = new double[nPoints];
    table[0] = min;
    table[nPoints-1] = max;
    recGenerate(0,nPoints-1,min,max);
  }

    // == Just for testing

    public static void main(String[] args)
	throws Exception
    {

	Global.rand=new Random(    System.currentTimeMillis() );

	IncreasingTableFunction p = new IncreasingTableFunction();
	p.setParameters(new ParamParser(args), false);
	p.initialize();
	p.doGenerate();

	for(int i=0; i<p.table.length; i++)
	  System.out.println(p.table[i]);
	System.out.println("\n--------------------------");
	System.out.println(p.eval(0));
	System.out.println(p.eval(1));
	System.out.println(p.eval(0.5));
	System.out.println(p.eval(0.7));
    }
}

