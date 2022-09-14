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
 * Class implements a congestion game in which there
 * is a set of "facilities" and each player chooses some
 * subset of these.  The payoffs for players are based only
 * on which facilities they have chosen and how many other
 * players have chosen the same facilities.
 *
 */


public class CongestionGame extends Game
{
    // Need to store functions for each player for each
    // facility
    private Function[][] payFuncs;

    private boolean randomize;


    //-----------------------------------------------------
    // Parameters: The congestion game is parameterized by
    // the number of players, the number of facilities, 
    // the function type (should be nonincreasing) and
    // parameters, and a symmetric boolean indicating
    // whether or not all players should have the same
    // payoff functions.

    private static Parameters.ParamInfo pFacilities;
    private static Parameters.ParamInfo pFunc;
    private static Parameters.ParamInfo pFuncArgs;
    private static Parameters.ParamInfo pSym;
    private static Parameters.ParamInfo[] cgParam;

    static {

	pFacilities = new Parameters.ParamInfo("facilities", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(5), "number of facilities in set.  Since each player chooses a subset of the facilities, the number of actions available to each player is 2 to the number of facilities.  A maximum of five facilities is allowed because of this extremely fast growth in matrix size.");

	pFunc = new Parameters.ParamInfo("func", Parameters.ParamInfo.STRING_PARAM, null, null, "the name of the function class to use for the payoff functions. Should either be a class which always creates decreasing functions, or a class which can be parameterized to create decreasing functions.");

	pFuncArgs = new Parameters.ParamInfo("func_params", Parameters.ParamInfo.CMDLINE_PARAM, null, null, "parameters to be handed off to the function, must be enclosed in [].  If the function class in use does not always create decreasing functions, the parameters should be set so that the function is decreasing.");

	pSym = new Parameters.ParamInfo("sym_funcs", Parameters.ParamInfo.BOOLEAN_PARAM, null, null, "should be true if it is desired that all players have the same set of payoff functions.");
	
	cgParam = new Parameters.ParamInfo[] {Game.players, pFacilities,
					      pFunc, pFuncArgs, pSym};
	Global.registerParams(CongestionGame.class, cgParam);

    }


    public CongestionGame()
	throws Exception
    {
	super();
    }



    /**
     * Set the parameters for the game itself.  The parameters for
     * the payoff functions will be set later. 
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

	// The number of players is specified by the user
	int players = (int) getLongParameter(Game.players.name);
	setNumPlayers(players);

	// The number of actions for each player is equal to
	// (2 to the number of facilities) - 1
	int facilities = (int) getLongParameter(pFacilities.name);
	setNumActions((int) Math.pow(2, facilities) - 1);
	
	// Create and initialize the appropriate number of 
	// functions in the function array
	initializeFunctionArray();
    }


    

    /**
     * Create the array of functions and initialize them
     * all with the appropriate parameters.
     */
    protected void initializeFunctionArray() {

	String funcName = parameters.getStringParameter(pFunc.name);
	ParamParser funcParams = parameters.getParserParameter(pFuncArgs.name);
	
	int facs = (int) getLongParameter(pFacilities.name);

	// If all players have the same set of utility
	// functions, only need to create one for each 
	// facility.  Otherwise need to create players * facs
	// functions total.

	if (getBooleanParameter(pSym.name)) {

	    payFuncs = new Function[1][facs];

	    for (int i = 0; i < facs; i++) {
		payFuncs[0][i] = (Function) 
		    Global.getObjectOrDie(funcName, Global.FUNC);
		try {
		    payFuncs[0][i].setDomain(0, getNumPlayers());
		    payFuncs[0][i].setParameters(funcParams, randomize);
		    payFuncs[0][i].initialize();
		} catch (Exception e) {
		  System.err.println(getHelp());
		  System.err.println(payFuncs[0][i].getHelp());
		    Global.handleError(e, "Couldn't initialize functions.");
		}
	    }

	} else {

	    int players = getNumPlayers();
	    payFuncs = new Function[players][facs];

	    for (int j = 0; j < players; j++) {
		for (int i = 0; i < facs; i++) {
		    payFuncs[j][i] = (Function)
			Global.getObjectOrDie(funcName, Global.FUNC);
		    try {
			payFuncs[j][i].setDomain(0, getNumPlayers());
			payFuncs[j][i].setParameters(funcParams, randomize);
			payFuncs[j][i].initialize();
		    } catch (Exception e) {
			Global.handleError(e, "Couldn't initialize functions");
		    }
		}
	    }
	}
    }
    

    protected void checkParameters() throws Exception 
    {
      if(!Global.isPartOf(Global.FUNC, getStringParameter(pFunc.name), "DecreasingFunction"))
	 throw new Exception ("Function must be decreasing!");
    }


    public void randomizeParameters() { 
	super.randomizeParameters();
	
	try {
	    if(!parameters.isParamSet(pFunc.name))
		parameters.setParameter(pFunc.name, 
					Global.getRandomClass(Global.FUNC, "DecreasingFunction"));
	    parameters.setParameter(pFuncArgs.name, new ParamParser());
	} catch (Exception e) {
	    Global.handleError(e, "Randomizing Params");
	}

	// Deal with the CMDLINE_PARAM
	super.randomizeParameters();
    }



    protected String getGameHelp()
    {
	return "Creates a congestion game.\n\n" +
	    "In the congestion game, each " +
	    "player chooses a subset from the set of all facilities. " +
	    "Each player then receives a payoff which is the sum " +
	    "of payoff functions for each facility in the chosen " +
	    "subset.  Each payoff function depends only on the " +
	    "number of other players who have chosen the facility.\n\n" +
	    "Functions used with this generator should always be " +
	    "decreasing in order for the resulting game to meet the " +
	    "criteria for being considered a congestion game.";
    }



    /**
     * Returns true if facility fac is selected for action act.
     * Treats numbers as if they are in binary and the digits 
     * set to 1 are facitilies, only is off by 1 since it is not
     * legal for an action to consist of the empty set.
     */
    protected boolean didChooseFac(long action, int fac) {

	if ((((int) action) % ((int) Math.pow(2, fac+1))) >=
	    (int) Math.pow(2, fac)) 
	    return true;

	return false;
    }



    /**
     * Return the payoff for a given player at a given outcome.  This
     * payoff is based on the facilities in the subset the player has
     * chosen and the number of other players who have chosen each
     * of these facilities.
     */
    public double getPayoff(int[] outcome, int player) {

	int players = getNumPlayers();
	int facs = (int) getLongParameter(pFacilities.name);
	int[] numChosen = new int[facs];

	// Get a count of how many players chose each facility
	for (int i = 0; i < facs; i++) {
	    numChosen[i] = 0;
	    for (int j = 0; j < players; j++) {
		if (didChooseFac(outcome[j], i)) {
		    numChosen[i]++;
		}
	    }
	}
		     
	// Sum the functions for all of the facilities chosen
	// by player to get the payoff
	double payoff = 0;
	for (int i = 0; i < facs; i++) {
	    if (didChooseFac(outcome[player], i)) {
		if (getBooleanParameter(pSym.name)) {
		    payoff += payFuncs[0][i].eval(numChosen[i]);
		} else {
		    payoff += payFuncs[player][i].eval(numChosen[i]);
		}
	    }
	}
	
	return payoff;
    }



    /**
     * It is more efficient to calculate all payoffs for all players
     * in a given outcome at once since the number of players who
     * have chosen each facility will only have to be calculated once.
     */
    public Vector getPayoff(int[] outcome) {

	int players = getNumPlayers();
	int facs = (int) getLongParameter(pFacilities.name);
	int[] numChosen = new int[facs];

	// Get a count of how many players chose each facility
	for (int i = 0; i < facs; i++) {
	    numChosen[i] = 0;
	    for (int j = 0; j < players; j++) {
		if (didChooseFac(outcome[j], i)) {
		    numChosen[i]++;
		}
	    }
	}
		     
	// For each player, sum the functions for all of the 
	// facilities chosen by player to get the payoff
	Vector payoffs = new Vector();
	for (int player = 0; player < players; player++) {
	    double payoff = 0;
	    
	    for (int i = 0; i < facs; i++) {
		if (didChooseFac(outcome[player], i)) {
		    if (getBooleanParameter(pSym.name)) {
			payoff += payFuncs[0][i].eval(numChosen[i]);
		    } else {
			payoff += payFuncs[player][i].eval(numChosen[i]);
		    }
		}
	    }

	    payoffs.add(new Double(payoff));
	}
	
	return payoffs;
    }



    /**
     * Generate all of the payoff functions for each player
     * for each facility
     */
    public void doGenerate() {

      setName("Congestion Game");

	int players = getNumPlayers();
	int facs = (int) getLongParameter(pFacilities.name);

	if (getBooleanParameter(pSym.name)) {	    
	    for (int i = 0; i < facs; i++) {
		payFuncs[0][i].doGenerate();
	    }
	} else {
	    for (int j = 0; j < players; j++) {
		for (int i = 0; i < facs; i++) {
		    payFuncs[j][i].doGenerate();
		}
	    }
	}
    }

}
