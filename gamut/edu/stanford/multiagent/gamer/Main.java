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
import java.lang.reflect.*;
import java.io.*;

import edu.stanford.multiagent.gamer.graphs.*;
import edu.stanford.multiagent.gamer.functions.*;

/**
 * The main class called when GAMUT is run.
 */

// == To Create gamut.jar: 
// == jar cvfm gamut.jar edu/stanford/multiagent/gamer/manfile Jama/ edu/

public class Main
{
    // Made public so that the parameter info can be accessed by
    // the help function in Global
    public static Parameters.ParamInfo[] globalParamInfo;
    private static Parameters.ParamInfo pSeed;
    private static Parameters.ParamInfo pGame;
    private static Parameters.ParamInfo pFilename;
    private static Parameters.ParamInfo pRandomize;
    private static Parameters.ParamInfo pOut;
    private static Parameters.ParamInfo pHelpGraph;
    private static Parameters.ParamInfo pHelpFunc;
    private static Parameters.ParamInfo pHelpGame;

    // -- Global Parameters
    static {
	pSeed=new Parameters.ParamInfo("random_seed", Parameters.ParamInfo.LONG_PARAM, new Long(0), new Long(Long.MAX_VALUE), "random seed, uses current time by default.");

	pGame = new Parameters.ParamInfo("g", Parameters.ParamInfo.VECTOR_PARAM, null, null, "the name of the game to generate, or a list of classes from intersection of which a generator will be picked");

	pFilename = new Parameters.ParamInfo("f", Parameters.ParamInfo.STRING_PARAM, null, null, "output file name");

	pRandomize = new Parameters.ParamInfo("random_params", Parameters.ParamInfo.BOOLEAN_PARAM, null, null, "randomize unset parameters in default ranges",false, Boolean.FALSE);

	pOut = new Parameters.ParamInfo("output", Parameters.ParamInfo.STRING_PARAM, null, null, "the name of the outputter to use. (Default: SimpleOutput)",false,"SimpleOutput");

	pHelpGame = new Parameters.ParamInfo("helpgame", Parameters.ParamInfo.STRING_PARAM, null, null, "Print help info for a game.");

	pHelpGraph = new Parameters.ParamInfo("helpgraph", Parameters.ParamInfo.STRING_PARAM, null, null, "Print help info for a graph.");

	pHelpFunc = new Parameters.ParamInfo("helpfunc", Parameters.ParamInfo.STRING_PARAM, null, null, "Print help info for a function.");


	globalParamInfo = new Parameters.ParamInfo[] 
	    {pSeed, pGame, pFilename, pRandomize, pOut, Game.intPayoffs, 
	     Game.intMult, Game.pNormalize, Game.pMinPayoff, Game.pMaxPayoff,
	    pHelpGame, pHelpGraph, pHelpFunc};

	Global.params=new Parameters(globalParamInfo);
    }


  /**
   * Return help for a given name/type
   */
  protected static void printObjectHelp(int type, String name)
  {
    if( !Global.isKnown(type, name) )
      {
	System.out.println("Known classes are: ");
	System.out.println(Global.getClassList(type));
      }
    else if( Global.isGround(type, name) )
      {
	Object obj = Global.getObjectOrDie(name, type);
	// -- all of these have getHelp method
	try {
	  Method mHelp = obj.getClass().getMethod("getHelp", null);
	  System.out.println(mHelp.invoke(obj, null));
	} catch (Exception e) {
	  System.err.println("Couldn't get help for " + name);
	  return;
	}
      }
    else    
      {
	// -- list all subclasses
      	Set s=new TreeSet();
	Global.getGroundClasses(type, name, s);
	System.out.println(name + " consists of:");
	for(Iterator it = s.iterator(); it.hasNext(); )
	  System.out.println((String)it.next());
      }
  }

    /**
     * Main method for GAMUT.
     */
    public static void main(String[] args)
    {
	if(args.length<1)
	    {
		System.out.println(Global.getHelp());
		System.exit(1);
	    }


	// -- Store the command line for posterity
	Global.gArgs = new String[args.length];
	System.arraycopy(args,0,Global.gArgs,0,args.length);


	// -- pre-parse parameters
	ParamParser p=null;

	try {
	    p = new ParamParser(args);

	    Global.params.setFromParser(p);

	} catch (Exception e) {
	    System.err.println(e.toString());
	    System.err.println(Global.getHelp());
	    System.exit(1);
	}

	// -- First, if any help parameters are present, just print
	// -- help and ignore the rest
	if(Global.params.isParamSet(pHelpGame.name))
	  {
	    printObjectHelp(Global.GAME, 
			    Global.params.getStringParameter(pHelpGame.name));
	    System.exit(0);
	  }

	if(Global.params.isParamSet(pHelpGraph.name))
	  {
	    printObjectHelp(Global.GRAPH, 
			    Global.params.getStringParameter(pHelpGraph.name));
	    System.exit(0);
	  }

	if(Global.params.isParamSet(pHelpFunc.name))
	  {
	    printObjectHelp(Global.FUNC,
			    Global.params.getStringParameter(pHelpFunc.name));
	    System.exit(0);
	  }

	// -- set the random seed
	Global.randSeed = ( Global.params.isParamSet(pSeed.name) ? 
			    Global.params.getLongParameter(pSeed.name) :
			    System.currentTimeMillis() );

	Global.rand=new Random(Global.randSeed);

	System.err.println("GAMUT RANDOM SEED: " + Global.randSeed);

	// -- Get Outputter Name
	String outName;
	outName = Global.params.getStringParameter(pOut.name);

	// -- Instantiate the game

	String gName=null;
	boolean randomizingGame=false;

	if(!Global.params.isParamSet(pGame.name)) {
	    if(!Global.params.getBooleanParameter(pRandomize.name)) {
		System.err.println("ERROR: -g is the required parameter!");
		System.err.println(Global.getHelp());
		System.exit(1);
	    }
	    else
		try {
		    randomizingGame=true;
		    //		    gName =Global.getRandomClass(Global.GAME);
		    System.err.println("WARNING: Randomizing from default class results in a restrictive distribution; this does not include 2-player or 2-action games, or more structured games (e.g. geometric games). See documentation for appropriate classes to use.");
		    Vector v = new Vector();
		    v.add("GameWithActionParam");
		    v.add("GameWithPlayerParam");
		    gName = Global.getRandomClassInt(Global.GAME, v);
		    
		} catch (Exception e) {
		    Global.handleError(e, "Randomizing Game");
		}
	}
	else {
	  Vector gVector=Global.params.getVectorParameter(pGame.name);
	  if(gVector.size()==1)
	    {
	      // -- 1 game only specified
	      gName=(String)gVector.firstElement();
	      randomizingGame = !Global.isGround(Global.GAME, gName);
	    }
	  else
	    {
	      // -- intersection
	      gName = Global.getRandomClassInt(Global.GAME, gVector);
	      randomizingGame = true;
	    }
	}

	String sOutFile=null;
	sOutFile = ( Global.params.isParamSet(pFilename.name) ? 
		     Global.params.getStringParameter(pFilename.name) 
		     : gName + ".game" );

	// Make sure that the min_payoff and max_payoff params are set
	// only if the normalize param is set and that max_payoff
	// is greater than min_payoff if they are set
	if (Global.params.getBooleanParameter(Game.pNormalize.name)) {
	    if (!Global.params.isParamSet(Game.pMinPayoff.name)) {
 		System.err.println(Global.getHelp());
		Global.handleError("Must set param min_payoff when " +
				   "normalization feature is in use.");
	    }
	    if (!Global.params.isParamSet(Game.pMaxPayoff.name)) {
 		System.err.println(Global.getHelp());
		Global.handleError("Must set param max_payoff when " +
				   "normalization feature is in use.");
	    }
	    if (Global.params.getDoubleParameter(Game.pMinPayoff.name) >=
		Global.params.getDoubleParameter(Game.pMaxPayoff.name)) {
 		System.err.println(Global.getHelp());
		Global.handleError("minpayoff >= max_payoff");
	    }
	} else {
	    if (Global.params.isParamSet(Game.pMinPayoff.name)) {
 		System.err.println(Global.getHelp());
		Global.handleError("min_payoff should not be set when " +
				   "normalization is not in use");
	    }
	    if (Global.params.isParamSet(Game.pMaxPayoff.name)) {
 		System.err.println(Global.getHelp());
		Global.handleError("max_payoff should not be set when " +
				   "normalization is not in use");
	    }
	}

	Game g=null;
	g = (Game) Global.getObjectOrDie(gName, Global.GAME);

	// -- set all parameters and initialize
	try {
	    boolean rp = Global.params.getBooleanParameter(pRandomize.name);
	    g.setParameters(p, rp);
	    g.initialize();
	} catch (Exception e) {
	    System.err.println("ERROR: Initializing " + gName);
	    System.err.println(e.toString());
	    System.err.println(Global.getHelp());
	    System.err.println(g.getHelp());
	    if( Global.hasPresets(Global.GAME, gName) )
	      System.err.println("Preset parameters: " + 
				 Global.getPresetParams(Global.GAME,gName));

	    System.exit(1);
	}

	//-- Check that no unknown parameters are passed
	//-- Turn this off for random games - since they might have different
	//-- Arguments
	if(!randomizingGame && p.hasUnusedArgs())
	    {
 		System.err.println("ERROR: Unknown arguments: ");
 		String[] a=p.getUnusedArgs();
 		for(int i=0; i<a.length; i++)
 		    System.err.println(a[i]);
 		System.err.println(Global.getHelp());
 		System.err.println(g.getHelp());
 		System.exit(1);
	    }

 
	// Generate an instance
	try {
	    g.generate();
	} catch (Exception e) {
	    Global.handleError(e, "Failed to generate an instance");
	} 

	PrintWriter out;
	GameOutput outputter = (GameOutput) 
	    Global.getObjectOrDie(outName, Global.OUTPUT);
	try {
	    FileWriter fw = new FileWriter(sOutFile);
	    out = new PrintWriter(fw, true);
	    outputter.writeGame(out, g);
	} catch (Exception e) {
	    Global.handleError(e, "Output failed");
	}
    }
}

