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
 * Generate an instance of the N-Player Chicken game.  Subclasses
 * Game directly without an intermediate layer.
 *
 */

public class NPlayerChicken extends Game
{

    //---------------------------------------------------------

    // Parameters: N-player Chicken is parameterized by
    // the numbers of players, the number of players who must 
    // cooperate to get the higher payoffs.

    private static Parameters.ParamInfo cut;
    private static Parameters.ParamInfo[] npcParam;

    static {
	// The randomization max for the cut will be reset to
	// the number of players and is therefore unimportant
	
	cut = new Parameters.ParamInfo("cutoff", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(100), "the number of players who need to cooperate to get the reward.  Must be > 0 and <= players.");

	npcParam = new Parameters.ParamInfo[] {Game.players, cut};
	Global.registerParams(NPlayerChicken.class, npcParam);
    }


    // -----------------------------------------------------

    // The following variables must be set when the game is generated.
    // They are later used to determine the payoffs for each player
    // at each scenario

    private long cutoff;
    private double reward;
    private double cost;

    // -----------------------------------------------------


    public NPlayerChicken() 
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
	if(getLongParameter(cut.name) <= 0)
	    throw new Exception("cutoff <= 0");
	
	if(getLongParameter(cut.name)
	   > getLongParameter(Game.players.name))
	    throw new Exception("cutoff > players");
    }



    /**
     * Randomize the parameters which were not filled in by the user.
     */
    public void randomizeParameters() 
    {
	cut.high = new Long(getLongParameter(Game.players.name));
	parameters.randomizeParameter(cut.name);
    }



    /**
     * Determine whether the number of players who chose action
     * one is high enough that everyone gets the reward.
     */
    private boolean allGetReward(int[] outcome) {

	int numChoseOne = 0;
	int players = getNumPlayers();

	for (int i = 0; i < getNumPlayers(); i++) 
	    if (outcome[i] == 1)
		numChoseOne++;

	if (numChoseOne >= cutoff)
	    return true;
	
	return false;
    }


    /**
     * Return the payoff for the given player in the given
     * outcome.  Assumes everything has already been set up.
     */    
    public double getPayoff(int[] outcome, int player) {
	double payoff;

	if (outcome[player] == 1) {
	    if (allGetReward(outcome)) {
		payoff = reward - cost;
	    } else {
		payoff = -cost;
	    } 
	} else {
	    if (allGetReward(outcome)) {
		payoff = reward;
	    } else {
		payoff = 0;
	    }
	}

	return payoff;
    }


    /**
     * Return a Vector with all players' utilities at the
     * given outcome
     */
    public Vector getPayoff(int[] outcome)
    {
	int players = getNumPlayers();
	Vector payoffVector = new Vector(players);

	if (allGetReward(outcome)) {
	    for (int i = 0; i < players; i++) {
		Double payoff;

		if (outcome[i] == 1) {
		    payoff = new Double (reward - cost);
		} else {
		    payoff = new Double (reward);
		}

		payoffVector.add (i, payoff);
	    }
	} else {
	    for (int i = 0; i < players; i++) {
		Double payoff;

		if (outcome[i] == 1) {
		    payoff = new Double (-cost);
		} else {
		    payoff = new Double (0);
		}

		payoffVector.add (i, payoff);
	    }
	}

	return payoffVector;
    }
    

    protected String getGameHelp()
    {
	return "Creates an instance of the N-Player Chicken Game.\n\n" +
	    "In N-Player Chicken, just as in the typical two player " +
	    "version of the game, players may cooperate or defect. " +
	    "There is a cost for choosing to cooperate.  However, if " +
	    "a certain number of players choose to cooperate, then " +
	    "all players receive a reward.\n\n" +
	    "The cost and reward amounts are always chosen between 1 " +
	    "and 100 (with reward > cost).  To change this range, " +
	    "use normalization.";
    }



    /**
     * Generate the game.
     */
    public void doGenerate() {

	cutoff = getLongParameter(cut.name);

	double lowCandR = 1;
	double highCandR = 100;

	// Choose the reward and cost amounts, such that the cost
	// is less than the reward
	SortedAndRandomSet values = new SortedAndRandomSet();

	for (int i = 0; i < 2; i++) {
	    Double randomAsDouble;
	    do {
		double random = Global.randomDouble(lowCandR, highCandR);
		randomAsDouble = new Double(random);
	    } while (!(values.add(randomAsDouble)));
	}

	Double b = (Double) values.removeLargest();
	Double c = (Double) values.removeLargest();

	reward = b.doubleValue();
	cost = c.doubleValue();

	setDescription("N-Player Chicken\n" + getDescription());
	setName("N-Player Chicken");

	int players = getNumPlayers();
	int actions = getNumActions(0); // always 2 for all players
    }

}

