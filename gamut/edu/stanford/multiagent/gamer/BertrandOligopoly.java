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
 * Returns an instance of the Bertrand Oligopoly.  Set-up is
 * similar to that of the Timing Games, but functions used to
 * determine payoffs are arbitrarily more complicated.
 */

public class BertrandOligopoly extends Game
{
    // == ARBITRARY Normalization constant
    private static final double NORM_CONST=10000.0;

    private Function costFunc;
    private Function demandFunc;
    private boolean randomize=false;

    private double costAdd, demandAdd;
    private double costMult, demandMult;

    // -----------------------------------------------------
    // Parameters: The Bertrand Oligopoly is parameterized by
    // the number of players, number of actions (which should
    // be the same for all players), and two functions for
    // cost and demand with their parameters.
    //

    private static Parameters.ParamInfo pCostFunc;
    private static Parameters.ParamInfo pDemFunc;
    private static Parameters.ParamInfo pCostFuncArgs;
    private static Parameters.ParamInfo pDemFuncArgs;
    private static Parameters.ParamInfo[] bertParam;

    static {

	pCostFunc = new Parameters.ParamInfo("cost_func", Parameters.ParamInfo.STRING_PARAM, null, null, "the name of the function class to use for the cost function.");

	pCostFuncArgs = new Parameters.ParamInfo("cost_params", Parameters.ParamInfo.CMDLINE_PARAM, null, null, "parameters to be handed off to the cost function, must be enclosed in [].");

	pDemFunc = new Parameters.ParamInfo("demand_func", Parameters.ParamInfo.STRING_PARAM, null, null, "the name of the function class to use for the demand function.");

	pDemFuncArgs = new Parameters.ParamInfo("demand_params", Parameters.ParamInfo.CMDLINE_PARAM, null, null, "parameters to be handed off to the demand function, must be enclosed in [].");	

	bertParam = new Parameters.ParamInfo[] 
	    {Game.players, Game.symActions, pCostFunc,
	     pCostFuncArgs, pDemFunc, pDemFuncArgs};

	Global.registerParams(BertrandOligopoly.class, bertParam);
    }


    // -------------------------------------------------------



    public BertrandOligopoly() 
	throws Exception
    {
	super();
    }


    /**
     * Sets the parameters for the game itself, and parse and
     * set the parameters for the cost and demand functions
     * and set those respectively.
     */
    public void setParameters(ParamParser p, boolean randomize)
	throws Exception
    {
	super.setParameters(p, randomize);
	this.randomize=randomize;

	// Can parse the players and actions as usual, making sure
	// there are the same number of actions for each player
	parsePlayersSameNumberActions();

    }



    public void initialize()
	throws Exception
    {
	super.initialize();

	// Initialize the cost and demand functions


	// Now get the type of the demand function, create its
	// function object, and parse its parameters

	String dFuncName = parameters.getStringParameter(pDemFunc.name);
	ParamParser dFuncParams = parameters.getParserParameter(pDemFuncArgs.name);

	demandFunc = (Function) Global.getObjectOrDie(dFuncName,
						      Global.FUNC);
	demandFunc.setDomain(0, getNumActions(0));

	try {
	    demandFunc.setParameters(dFuncParams, randomize);
	    demandFunc.initialize();
	    demandFunc.doGenerate();
	    // demand >=0 
	    double demandMin = demandFunc.eval(demandFunc.getDMax());
	    demandAdd = (demandMin < 0 ? - demandMin : 0);
	    double demandMax = demandFunc.eval(demandFunc.getDMin())+demandAdd;
	    demandMult = NORM_CONST / demandMax;

	} catch (Exception e) {
	  System.err.println(getHelp());
	  System.err.println(demandFunc.getHelp());
	  Global.handleError(e, "Error parsing demand function params");
	}

	// Get the type of the cost function, create the function
	// object, and parse its parameters

	String cFuncName = parameters.getStringParameter(pCostFunc.name);
	ParamParser cFuncParams = parameters.getParserParameter(pCostFuncArgs.name);

	costFunc = (Function) Global.getObjectOrDie(cFuncName,
						    Global.FUNC);

	// -- assuming demand is decreasing
	costFunc.setDomain(evalDemand(getNumActions(0)) / getNumPlayers(),
			   evalDemand(0));

	try {
	    costFunc.setParameters(cFuncParams, randomize);
	    costFunc.initialize();
	    costFunc.doGenerate();
	    // -- cost must be non-neg
	    double costMin = costFunc.eval(costFunc.getDMin());
	    costAdd = (costMin < 0 ? -costMin : 0);
	    double costMax = costFunc.eval(costFunc.getDMax()) + costAdd;
	    costMult = NORM_CONST / costMax;

	} catch (Exception e) {
	  System.err.println(getHelp());
	  System.err.println(costFunc.getHelp());
	  Global.handleError(e, "Error parsing cost function params");
	}


    }


    public void randomizeParameters()  {
	try {

	if(!parameters.setByUser(pCostFunc.name))
	    parameters.setParameter(pCostFunc.name, 
				    Global.getRandomClass(Global.FUNC, "SlowlyIncreasingFunction"));

	if(!parameters.setByUser(pDemFunc.name))
	    parameters.setParameter(pDemFunc.name, 
				    Global.getRandomClass(Global.FUNC, "DecreasingFunction"));

	} catch (Exception e) {
	    Global.handleError(e, "Randomizing Function");
	}

	// Deal with the CMDLINE_PARAM
	super.randomizeParameters(); 
   }


    //
    // Make sure that the parameters are in the proper range
    //
    protected void checkParameters() throws Exception 
    {
	if(!Global.isPartOf(Global.FUNC, getStringParameter(pDemFunc.name), 
			    "DecreasingFunction"))
	    throw new Exception("Demand must be decreasing!");
	
	if(!Global.isPartOf(Global.FUNC, getStringParameter(pCostFunc.name), 
			    "IncreasingFunction"))
	    throw new Exception("Cost must be increasing!");
    }
    


    protected String getGameHelp()
    {
	return "Creates an instance of a Bertrand Oligopoly using " +
	    "arbitrary cost and demand functions.\n\n" +
	    "In the Bertrand Oligopoly, each player offering " +
	    "the object at the lowest price p will receive a " +
	    "payoff of \n\n" +
	    "     p*(D(p)/m) - C(D(p)/m) \n\n" +
	    "where D is the demand function, C is the cost function, " +
	    "and m is the number of players who offered the object " +
	    "at this price.\n\n" +
	    "Please note that the demand function should " +
	    "be non-negative and decreasing.";
    }



    /**
     * Evaluates demand.
     */
    double evalDemand(double x)
    {
	return demandMult*(demandFunc.eval(x)+demandAdd);
    }

    /**
     * Evaluates cost.
     */
    double evalCost(double x)
    {
	return costMult*(costFunc.eval(x)+costAdd);
    }


    /**
     * Return the payoff for the given player in the given
     * outcome using the parameterized cost and demand functions.
     * If a firm is one of the m firms with the lowest price (action)
     * then payoff is given by
     *
     *           p * (D(p)/m) - C(D(p)/m)
     *
     * Otherwise the payoff is 0.
     *
     */
    public double getPayoff(int[] outcome, int player) 
    {
	long lowAction = outcome[0];
	long m = 0;

	for (int i = 0; i < getNumPlayers(); i++) {
	    if (outcome[i] < lowAction) {
		lowAction = outcome[i];
		m = 1;
	    } else if (outcome[i] == lowAction) {
		m++;
	    }
	}
	
	// If player does not offer the item at the lowest
	// price of everyone, immediately return 0 for the payoff.
	if (outcome[player] > lowAction)
	    return 0;

	double demandOfP = evalDemand(lowAction);
	if (demandOfP < 0) demandOfP = 0;

	// Otherwise return p * (D(p)/m) - C(D(p)/m)
       	return (((double) lowAction * demandOfP / (double) m) -
		evalCost(demandOfP / (double) m));
    }



    /**
     * Generates the demand and cost functions which are
     * needed to determine payoffs.
     */
    public void doGenerate()
    {

	setName("Bertrand Oligopoly");
	setDescription("Bertrand Oligopoly\n" + 
		       getDescription() +
		       "\nDemand Func: " + demandFunc.getDescription() + 
		       "\nCost Func: " + costFunc.getDescription());

	/*	demandFunc.doGenerate();
	costFunc.doGenerate();
	*/
    }
    

}

