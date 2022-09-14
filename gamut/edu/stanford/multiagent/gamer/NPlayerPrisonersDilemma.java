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
 * Generate an instance of N-Player Prisoner's Dilemma game as
 * follows.
 *
 * Let C(i) be the payoff if you cooperate and i others cooperate, 
 * and let D(i) be the payoff if you defect while i others cooperate,
 * such that 
 *
 *    1) D(i) > C(i) for 0 <= i <= n-1
 *    2) D(i+1) > D(i) and also C(i+1) > C(i) for 0 <= i < n-1
 *    3) C(i) > (D(i) + C(i-1)) / 2 for 0 < i <= n-1
 *
 * For now this is done by using a linear function
 *    C(i) = Xc + Y
 *    D(i) = Xc + Z
 * where 0 < Z - Y < X
 */


public class NPlayerPrisonersDilemma extends Game
{
    private static final long COOPERATE = 1;
    private static final long DEFECT = 2;

    private double X, Y, Z;


    //---------------------------------------------------------


    // Parameters: N-player PD is parameterized by
    // the numbers of players, and high and low bounds on
    // the possible payoffs.

    private static Parameters.ParamInfo paramX, paramY, paramZ;
    private static Parameters.ParamInfo[] npdParam;

    static {

	// The upper bound of 100 on the randomization values 
	// of these variables was chosen somewhat arbitrarily.

	paramX = new Parameters.ParamInfo("function_X", Parameters.ParamInfo.DOUBLE_PARAM, new Double (1), new Double(100000), "X in payoff functions (see description).  Must be set such that 0 < Z - Y < X and all parameters must be less than 100,000.");

	paramY = new Parameters.ParamInfo("function_Y", Parameters.ParamInfo.DOUBLE_PARAM, new Double (1), new Double(100000), "Y in payoff functions (see description).  Must be set such that 0 < Z - Y < X and all parameters must be less than 100,000.");

	paramZ = new Parameters.ParamInfo("function_Z", Parameters.ParamInfo.DOUBLE_PARAM, new Double (1), new Double(100000), "Z in payoff functions (see description).  Must be set such that 0 < Z - Y < X and all parameters must be less than 100,000.");
	
	npdParam = new Parameters.ParamInfo[] {Game.players, paramX, 
						paramY, paramZ}; 
	Global.registerParams(NPlayerPrisonersDilemma.class, npdParam);
    }


    // -----------------------------------------------------


    public NPlayerPrisonersDilemma() 
	throws Exception
    {
	super();
    }


    /**
     * Set the numbers of players and actions for the game,
     * initilaize variables, and so on.
     */
    public void initialize()
	throws Exception
    {
	super.initialize();

	// The number of players is extensible but the number
	// of actions must always be 2
	int players = (int) getLongParameter(Game.players.name);
	setNumPlayers(players);
	
	int numActions[];
	numActions = new int[players];
	for (int i = 0; i < players; i++)
	    numActions[i] = 2;
	setNumActions(numActions);
    }


    /**
     * Make sure that the parameters are in the proper range
     */
    protected void checkParameters() throws Exception 
    {
	if (getDoubleParameter(paramX.name) < 2)
	    throw new Exception ("function_X < 2");

	if (getDoubleParameter(paramY.name) >=
	    getDoubleParameter(paramZ.name))
	    throw new Exception ("function_Y >= function_Z");

	if (getDoubleParameter(paramZ.name) -
	    getDoubleParameter(paramY.name) >=
	    getDoubleParameter(paramX.name))
	    throw new Exception ("function_Z - function_Y >= function_X");
    }	



    /**
     * Randomize parameters that were not set by the user
     */
    public void randomizeParameters() {

	// Force that either all of X, Y, and Z are set by the
	// user, or none of them are set.
	if ((!parameters.setByUser(paramX.name)) 
	    && (!parameters.setByUser(paramY.name))
	    && (!parameters.setByUser(paramZ.name))) {

	    // randomize such that 0 < Z - Y < X
	    parameters.randomizeParameter(paramY.name);
	    paramZ.low = new Double(getDoubleParameter(paramY.name));
	    parameters.randomizeParameter(paramZ.name);
	    paramX.low = new Double(getDoubleParameter(paramZ.name) -
				    getDoubleParameter(paramY.name));
	    parameters.randomizeParameter(paramX.name);

	} else if  ((!parameters.setByUser(paramX.name)) 
		    || (!parameters.setByUser(paramY.name))
		    || (!parameters.setByUser(paramZ.name))) {
	    Global.handleError("Randomization error.  User must set values " +
			       "for all or none of function_X, function_Y, " +
			       "and function_Z parameters.");
	}
    }



    /**
     * Return the number of players who have chosen "cooperate"
     * (i.e. action #1) in a given outcome
     */
    private long numberOfCs(int[] outcome) {
	long numCs = 0;

	for (int i = 0; i < getNumPlayers(); i++) {
	    if (outcome[i] == COOPERATE) {
		numCs++;
	    }
	}

	return numCs;
    }



    /**
     * Evaluates payoff function.  Note that numCs should include
     * the current player if he is cooperating.  This will be
     * adjusted for automatically.
     */
    private double evaluatePayoffFunction(long numCs, long action) {
	
	if (action == COOPERATE) {
	    return (X * (numCs - 1)) + Y;
	}

	return (X * numCs) + Z;
    }




    /**
     * Return the payoff for the given player in the given
     * outcome.  Assumes everything has already been set up.
     */    
    public double getPayoff(int[] outcome, int player) {

	long numCs = numberOfCs(outcome);
	double payoff = evaluatePayoffFunction(numCs, outcome[player]);

	return (payoff);
    }



    /**
     * Return a Vector with all players' utilities at the
     * given outcome
     */
    public Vector getPayoff(int[] outcome)
    {
	int players = getNumPlayers();
	Vector payoffVector = new Vector(players);

	long numCs = numberOfCs(outcome);

	for (int i = 0; i < players; i++) {
	    Double payoff = new Double
		(evaluatePayoffFunction(numCs, outcome[i]));
	    payoffVector.add(i, payoff);
	}

	return payoffVector;
    }
    


    protected String getGameHelp()
    {
	return "Creates an instance of the N-Player Prisoner's " +
	    "Dilemma Game.  In the N-Player Prisoner's Dilemma, " +
	    "the payoff to each player is based on the number of " +
	    "players who cooperate not including the player himself.\n\n" +
	    "If the number of other players who cooperate is i, then we " +
	    "say that C(i) is the payoff for cooperating and D(i) is the " +
	    "payoff for defecting." +
	    "In order for this payoff scheme to result in a Prisoner's " +
	    "Dilemma, it must be the case that: \n" +
	    "   1) D(i) > C(i) for 0 <= i <= n-1 \n" +
	    "   2) D(i+1) > D(i) and also C(i+1) > C(i) for 0 <= i < n-1 \n" +
	    "   3) C(i) > (D(i) + C(i-1)) / 2 for 0 < i <= n-1 \n\n" +
	    "We guarantee these conditions are met by using linear " +
	    "functions for which you may provide the parameters: \n" +
	    "   C(i) = Xc + Y\n" +
	    "   D(i) = Xc + Z\n" +
	    "where 0 < Z - Y < X.";
    }



    /**
     * Generate the game
     */
    public void doGenerate() {

	// -- get Parameter values
	X = getDoubleParameter(paramX.name);
	Y = getDoubleParameter(paramY.name);
	Z = getDoubleParameter(paramZ.name);

	int players = getNumPlayers();
	int actions = getNumActions(0); // always 2 for all players

	setDescription("N-Player Prisoner's Dilemma\n" 
		       + getDescription());
	setName("N-Player Prisoner's Dilemma");
    }

}

