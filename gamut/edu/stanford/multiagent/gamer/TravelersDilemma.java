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
 * Return an instance of the game Traveler's Dilemma
 */ 


public class TravelersDilemma extends TimingGame
{

    // Parameters: Traveler's Dilemma is parameterized by the number
    // of players and actions (all players must have same number
    // of actions as these represent the "dollar" amounts that 
    // it is possible to claim), the reward units for the player who 
    // claims the smallest amount.
    private static Parameters.ParamInfo pReward;
    private static Parameters.ParamInfo[] tdParam;

    static {
	// When the reward is randomized, its range is reset after 
	// the other params are read in to be from 1 to the number of
	// actions.  The reward can be outside this range if it is
	// set by hand.
	pReward = new Parameters.ParamInfo("reward", Parameters.ParamInfo.DOUBLE_PARAM, new Double(1), new Double(100.0), "the amount of the reward for the player who claims the lowest dollar amount.  Must be > 0 and <= 100.");

	tdParam = new Parameters.ParamInfo[] {Game.players,
					       Game.symActions, pReward};
	Global.registerParams(TravelersDilemma.class, tdParam);
    }


    // ----------------------------------------------


    public TravelersDilemma() 
	throws Exception
    {
	super();
    }



    public void initialize()
	throws Exception
    {
	super.initialize();

	// Both players and number of actions are extensible, 
	// but the number of actions must be the same for
	// all players
	parsePlayersSameNumberActions();
    }


    //
    // Make sure that the parameters are in the proper range
    //
    protected void checkParameters() throws Exception 
    {
	if (getDoubleParameter(pReward.name) <= 0)
	    throw new Exception("reward <= 0");
    }


    // 
    // Randomize any params not set by the user
    //
    public void randomizeParameters() 
    {
	// Reset the range of the reward based on the number of
	// actions.
	int val[] = Global.parseIntArray((Vector)
					 getParameter(Game.actions.name));
	pReward.high = new Double((double) val[0]);
	parameters.randomizeParameter(pReward.name);
    }



    protected String getGameHelp()
    {
	return "Creates an instance of Traveler's Dilemma game.\n\n" +
	    "In order to make the game interesting, the parameters " +
	    "should be set up so that the reward is larger than one " +
	    "(but usually smaller than the number of actions)." +
	    "  When this holds, the unique " +
	    "Nash equilibrium will be the unsatisfying equilibrium " +
	    "in which everyone chooses the smallest dollar amount.\n\n" +
	    "When randomization is used, the reward will automatically " +
	    "be chosen from somewhere in this range.";
    }


    public void doGenerate()
    {
	setDescription("Traveler's Dilemma\n" + getDescription());
	setName("Traveler's Dilemma");

	int players = getNumPlayers();

	// Get reward amount
	double reward = getDoubleParameter(pReward.name);

	// Create TimingGameParams objects which will be 
	// necessary to hold the function parameters for each player
	TimingGameParams low[] = new TimingGameParams[players];
	TimingGameParams notLow[] = new TimingGameParams[players];
	TimingGameParams tie[] = new TimingGameParams[players];

	// All players will receive a base payoff of the lowest
	// action chosen times the value of a "dollar."  The
	// player(s) choosing the lowest action will receive
	// an additional reward of the reward amount.
	for (int i = 0; i < players; i++) {
	    // Set up function parameters for when player chooses
	    // the unique lowest time and gets the reward
	    low[i] = new TimingGameParams(0, 1, reward);

	    // Function params for when player does not have
	    // the unique low time
	    tie[i] = new TimingGameParams(0, 1, 0);
	    notLow[i] = new TimingGameParams(0, 1, 0);
	}

	// Finally, set these parameters so that the payoff can be
	// calculated using the timing game class
	setParamsWithTie(low, tie, notLow);
    }

}
