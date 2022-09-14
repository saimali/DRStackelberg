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
 * Return an instance of the Cournot Duopoly using user
 * specified functions for the cost and inverse demand.
 * Allows the cost functions for both players to be 
 * parameterized separately.
 *
 * Note that although the Cournot Duopoly could be extended
 * in a somewhat straight forward way to more than two
 * players, this game is not generally studied in economics.
 * Therefore we limit the duopoly to a two player game.
 */

public class CournotDuopoly extends Game
{

    private Function[] costFuncs;
    private Function pFunc;

    private boolean randomize=false;

    // -----------------------------------------------------
    // Parameters: The Cournot Duopoly is parameterized by
    // specifying the function classes and function params for
    // both players' cost functions and the inverse demand 
    // function as well as the range of actions allowed.
    //

    private static Parameters.ParamInfo pCostFunc1;
    private static Parameters.ParamInfo pCostFunc2;
    private static Parameters.ParamInfo pCostArgs1;
    private static Parameters.ParamInfo pCostArgs2;
    private static Parameters.ParamInfo pPFunc;
    private static Parameters.ParamInfo pPArgs;
    private static Parameters.ParamInfo[] courParam;

    static {

	pCostFunc1 = new Parameters.ParamInfo("cost_func1", Parameters.ParamInfo.STRING_PARAM, null, null, "the name of the function class to use for the cost function for the first player.");

	pCostArgs1 = new Parameters.ParamInfo("cost_params1", Parameters.ParamInfo.CMDLINE_PARAM, null, null, "parameters to be handed off to the cost function for player 1, must be enclosed in [].");

	pCostFunc2 = new Parameters.ParamInfo("cost_func2", Parameters.ParamInfo.STRING_PARAM, null, null, "the name of the function class to use for the cost function for the second player.");

	pCostArgs2 = new Parameters.ParamInfo("cost_params2", Parameters.ParamInfo.CMDLINE_PARAM, null, null, "parameters to be handed off to the cost function for player 2, must be enclosed in [].");

	pPFunc = new Parameters.ParamInfo("p_func", Parameters.ParamInfo.STRING_PARAM, null, null, "the name of the function class to use for the inverse demand function P.");

	pPArgs = new Parameters.ParamInfo("p_params", Parameters.ParamInfo.CMDLINE_PARAM, null, null, "parameters to be handed off to the inverse demand function, must be enclosed in [].");

	courParam = new Parameters.ParamInfo[] 
	    {Game.symActions, pCostFunc1, pCostArgs1, pCostFunc2, pCostArgs2,
	     pPFunc, pPArgs};

	Global.registerParams(CournotDuopoly.class, courParam);
    }


    // -------------------------------------------------------



    public CournotDuopoly() 
	throws Exception
    {
	super();
    }



    /**
     * Set the parameters for the game itself, and parse and
     * set the parameters for the cost and inverse demand 
     * functions.
     */
    public void setParameters(ParamParser p, boolean randomize)
	throws Exception
    {
	super.setParameters(p, randomize);
	this.randomize=randomize;
    }


    public void initialize()
	throws Exception
    {
	super.initialize();
	
	// The number of players will always be 2 in the duopoly.
	setNumPlayers(2);
	parseSameNumberActions();
	
 	// Get the type of the cost functions and the inverse
	// demand function, create the three function objects, 
	// and parse the function parameters
	
	costFuncs = new Function[2];
	
	String costName1 = parameters.getStringParameter(pCostFunc1.name);
	ParamParser costParams1 = parameters.getParserParameter(pCostArgs1.name);
	costFuncs[0] = (Function) Global.getObjectOrDie(costName1,
							Global.FUNC);
	
	try { 
	    costFuncs[0].setDomain(1, getNumActions(0));
	    costFuncs[0].setParameters(costParams1, randomize);
	} catch (Exception e) {
	  System.err.println(getHelp());
	  System.err.println(costFuncs[0].getHelp());
	    Global.handleError(e, "Error parsing cost function params");
	}
	
	
	String costName2 = parameters.getStringParameter(pCostFunc2.name);
	ParamParser costParams2 = parameters.getParserParameter(pCostArgs2.name);
	costFuncs[1] = (Function) Global.getObjectOrDie(costName2,
							Global.FUNC);

	try { 
	    costFuncs[1].setDomain(1, getNumActions(0));
	    costFuncs[1].setParameters(costParams2, randomize);
	} catch (Exception e) {
	  System.err.println(getHelp());
	  System.err.println(costFuncs[1].getHelp());
	    Global.handleError(e, "Error parsing cost function params");
	}


	String pName = parameters.getStringParameter(pPFunc.name);
	ParamParser pParams = parameters.getParserParameter(pPArgs.name);
	pFunc = (Function) Global.getObjectOrDie(pName, Global.FUNC);

	try { 
	    pFunc.setDomain(2, 2 * getNumActions(0)); 
	    pFunc.setParameters(pParams, randomize);
	} catch (Exception e) {
	  System.err.println(getHelp());
	  System.err.println(pFunc.getHelp());
	    Global.handleError(e, "Error parsing p function params");
	}
	
	
	// Initialize all functions
	costFuncs[0].initialize();
	costFuncs[1].initialize();
	pFunc.initialize();
    }

    
    protected void checkParameters() throws Exception 
    {
	if (!Global.isPartOf(Global.FUNC, 
			     getStringParameter(pCostFunc1.name), 
			     "IncreasingFunction") ||
	    !Global.isPartOf(Global.FUNC, 
			     getStringParameter(pCostFunc2.name), 
			     "IncreasingFunction"))
	    throw new Exception("Cost functions must be increasing!");
	
    }
    

    public void randomizeParameters() 
    {
	try {

	    if(!parameters.setByUser(pCostFunc1.name))
		parameters.setParameter(pCostFunc1.name, 
					Global.getRandomClass(Global.FUNC,"IncreasingFunction"));

	    if(!parameters.setByUser(pCostFunc2.name))
		parameters.setParameter(pCostFunc2.name, 
					Global.getRandomClass(Global.FUNC, "IncreasingFunction"));

	    if(!parameters.setByUser(pPFunc.name))
		parameters.setParameter(pPFunc.name, 
					Global.getRandomClass(Global.FUNC, "IncreasingFunction"));

	} catch (Exception e) {
	    Global.handleError(e, "Randomizing Functions");
	}

	// Deal with the CMDLINE_PARAM
	super.randomizeParameters();
    }


    protected String getGameHelp()
    {
	return "Create an instance of the Cournot Duopoly using " +
	    "arbitrary cost and inverse demand functions.\n\nIn " +
	    "order for the problem to make sense, the cost " +
	    "functions used should be increasing.  If C1 and C2 " +
	    "are cost functions and P is the inverse demand function " +
	    "then if player 1 plays y1 and player 2 plays y2, the " +
	    "payoff to player 1 will be \n      P(y1+y2)y1-C1(y1)\nand the " +
	    "payoff to player 2 will be \n      P(y1+y2)y2-C2(y2)\n\n" +
	    "Although this formulation could be extended to more " +
	    "than two players, this is generally not done in " +
	    "practice so we limit the players to 2.";
    }



    /**
     * Return the payoff for the given player in the given outcome.
     * If Ci is a cost function for player i and P is the inverse
     * demand function and each player j chooses action yj then
     * the payoff for player i is
     *
     *           P(y1+y2)yi - Ci(yi)
     */
    public double getPayoff(int[] outcome, int player) 
    {
	int y[] = new int[2];

	for (int i = 0; i < 2; i++)
	    y[i] = outcome[i];

	double payoff = (double) (pFunc.eval(y[0] + y[1]) * y[player] -
	    costFuncs[player].eval(y[player]));

	return payoff;
    }



    /**
     * Generate the inverse demand and cost functions.
     */
    public void doGenerate()
    {
	setDescription("Cournot Duopoly\n" + getDescription());
	setName("Cournot Duopoly");

	pFunc.doGenerate();
	costFuncs[0].doGenerate();
	costFuncs[1].doGenerate();
    }
    

}

