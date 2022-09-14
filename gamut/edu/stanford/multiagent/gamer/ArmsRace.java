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
import edu.stanford.multiagent.gamer.functions.*;

/**
 * Return an instance of a discretized Arms Race game using 
 * user specified functions.  For now it is left to the user
 * to make sure that the C function is smooth and the B
 * function is smooth and concave.
 *
 * Although the arms races game could be extended to more
 * that two players, we will limit it at two as is the general
 * case that is studied in economics.
 *
 */

public class ArmsRace extends Game
{

    private Function cFunc;
    private Function bFunc;
    private boolean randomize;


    // -----------------------------------------------------
    // Parameters: The Arms Race game is parameterized by
    // specifying the function classes and function params for
    // the C and B functions, as well as the range of actions 
    // allowed with the number of actions equal to 
    // 1 + high_act - low_act.
    //

    private static Parameters.ParamInfo pCFunc;
    private static Parameters.ParamInfo pBFunc;
    private static Parameters.ParamInfo pCArgs;
    private static Parameters.ParamInfo pBArgs;
    private static Parameters.ParamInfo pActLow;
    private static Parameters.ParamInfo[] arParam;

    static {

       	pCFunc = new Parameters.ParamInfo("c_func", Parameters.ParamInfo.STRING_PARAM, null, null, "the name of the function class to use for the cost function C.  The function supplied should be SMOOTH in order to stick to the strict definition of an Arms Race.");

	pCArgs = new Parameters.ParamInfo("c_params", Parameters.ParamInfo.CMDLINE_PARAM, null, null, "parameters to be handed off to the cost function C, must be enclosed in [].");

	pBFunc = new Parameters.ParamInfo("b_func", Parameters.ParamInfo.STRING_PARAM, null, null, "the name of the function class to use for the B function.  The function supplied should be SMOOTH AND CONCAVE in order to stick to the strict definition of an Arms Race.");

	pBArgs = new Parameters.ParamInfo("b_params", Parameters.ParamInfo.CMDLINE_PARAM, null, null, "parameters to be handed off to the B function, must be enclosed in [].");

	pActLow = new Parameters.ParamInfo("low_act", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(1000), "lower bound on the players' action range.  Must be > 0 and <= 1000.  The upper bound on the players' action range will be low_act + actions - 1.");

	arParam = new Parameters.ParamInfo[] 
	    {Game.symActions, pCFunc, pCArgs, pBFunc, pBArgs, pActLow};

	Global.registerParams(ArmsRace.class, arParam);
    }


    // -------------------------------------------------------



    public ArmsRace() 
	throws Exception
    {
	super();
    }


    /** 
     * Set the parameters for the game itself -- the parameters for
     * the functions will be parsed in initialize().
     */
    public void setParameters(ParamParser p, boolean randomize)
	throws Exception
    {
	super.setParameters(p, randomize);
	this.randomize = randomize;
    }



    public void initialize()
	throws Exception
    {
	super.initialize();	
	
	// The number of players will always be 2 in the arms race.
	setNumPlayers(2);
	parseSameNumberActions();
	
 	// Get the types of the C and B functions, create the
	// function objects, and parse the function parameters

	String cFuncName = parameters.getStringParameter(pCFunc.name);
	ParamParser cParams = parameters.getParserParameter(pCArgs.name);

	cFunc = (Function) Global.getObjectOrDie(cFuncName, Global.FUNC);
	cFunc.setDomain(getLongParameter(pActLow.name), getNumActions(0) + getLongParameter(pActLow.name));

	try { 
	    cFunc.setParameters(cParams, randomize);
	} catch (Exception e) {
	  System.err.println(getHelp());
	  System.err.println(cFunc.getHelp());
	    Global.handleError(e, "Error parsing C function params");
	}


	String bFuncName = parameters.getStringParameter(pBFunc.name);
	ParamParser bParams = parameters.getParserParameter(pBArgs.name);

	bFunc = (Function) Global.getObjectOrDie(bFuncName, Global.FUNC);
	bFunc.setDomain(-getNumActions(0), +getNumActions(0));

	try { 
	    bFunc.setParameters(bParams, randomize);
	} catch (Exception e) {
	  System.err.println(bFunc.getHelp());
	    Global.handleError(e, "Error parsing B function params");
	}

	// Initialize all functions
	cFunc.initialize();
	bFunc.initialize();
    }



    /**
     * Randomize the low action bound and functions
     */
    public void randomizeParameters() 
    {
	parameters.randomizeParameter(pActLow.name);

	try {
	  if(!parameters.setByUser(pCFunc.name))
	      parameters.setParameter(pCFunc.name, 
				      Global.getRandomClass(Global.FUNC));

	  if(!parameters.setByUser(pBFunc.name))
	      parameters.setParameter(pBFunc.name, 
				      Global.getRandomClass(Global.FUNC, 
							    "ConcaveFunction"));
	} catch (Exception e) {
	    Global.handleError(e, "Randomizing Functions!");
	}

	// Deal with the CMDLINE_PARAM
	super.randomizeParameters();
    }



    /**
     * Make sure that the parameters are in the proper range
     */
    protected void checkParameters() throws Exception 
    {
	if (getLongParameter(pActLow.name) < 0)
	    throw new Exception ("low_act < 0");
    }



    protected String getGameHelp()
    {
	return "Create an instance of an Arms Race game.  Payoffs " +
	    "in this game are symmetric and calculated by using the " +
	    "formula -C(x)+B(x-y) where x is the level of arms the " +
	    "player in question has chosen, y is the level of arms " +
	    "his opponent has chosen, and C and B are user-specified " +
	    "functions.\n\n" +
	    "Please note that in order for the game to meet the " +
	    "definition of an Arms Race common in economics literature " +
	    "it must be the case that B is smooth and concave and C is " +
	    "at least smooth.  Choose your functions accordingly.";
    }



    /**
     * Returns the payoff for the given player in the given outcome.
     * If x is the level of arms the player has chosen and y is the
     * level the opponent has chosen then the payoff for the
     * player is:
     * 
     *        -C(x) + B(x-y)
     *
     * Note that since our actions always begin at 1, the y values
     * are really the action + the low action value - 1.
     */
    public double getPayoff(int[] outcome, int player) 
    {
	int offset = (int) getLongParameter(pActLow.name) - 1;
	int x[] = new int[2];

	for (int i = 0; i < 2; i++)
	    x[i] = outcome[i] + offset;

	double payoff = (double) ((-(cFunc.eval(x[player]))) +
				  (bFunc.eval(x[player] - x[1-player])));

	return payoff;
    }



    /**
     * Generates the functions.
     */
    public void doGenerate()
    {
	setDescription("Two Player Arms Race Game\n" + getDescription());
	setName("Arms Race");

	cFunc.doGenerate();
	bFunc.doGenerate();
    }
    

}

