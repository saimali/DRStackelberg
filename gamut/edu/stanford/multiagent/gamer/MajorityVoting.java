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
 * Generates an instance of the Majority Voting game.  Subclasses
 * Game directly without an intermediate layer.
 *
 * Note that in this version of the Majority Voting game, players'
 * utilities for each candidate are arbitrary and it is possible that
 * a player would be indifferent between two or more candidates.
 *
 * Also, in this version if two candidates have the same number of
 * votes, the lower indexed candidate is declared winner.
 */

public class MajorityVoting extends Game
{

    //---------------------------------------------------------

    // Parameters: The Majority Voting game is parameterized by
    // the numbers of players and candidates (actions).
    private static Parameters.ParamInfo[] mvParam;

    static {
	mvParam = new Parameters.ParamInfo[] {Game.players, Game.symActions};
	Global.registerParams(MajorityVoting.class, mvParam);
    }

    // -----------------------------------------------------

    // Keep track of the utility for each player for each 
    // candidate winning.  Note that candidates are numbered
    // starting at 0 in the preferences while actions are numbered
    // starting at 0, so adjustments need to be made.
    double preferences[][];

    // -----------------------------------------------------


    public MajorityVoting() 
	throws Exception
    {
	super();
    }


    //
    // Set the numbers of players and actions for the game,
    // initilaize variables, and so on.
    //
    public void initialize()
	throws Exception
    {
	super.initialize();

	// Set the number of players and the number of actions (time
	// steps) from the parameters
	parsePlayersSameNumberActions();
    }


    //
    // None to check.
    //
    protected void checkParameters() throws Exception 
    {
    }


    //
    // None to randomize
    //
    public void randomizeParameters() 
    {
    }


    /**
     * Counts the votes of each player on the particular outcome
     * and returns the action number of the candidate with the
     * most votes.
     */
    private long determineWinner(int[] outcome) {

	int players = getNumPlayers();
	int actions = getNumActions(0); // same for all players

	long voteCounter[] = new long[actions];
	for (int i = 0; i < actions; i++)
	    voteCounter[i] = 0;
	for (int i = 0; i < players; i++)
	    // Remember that actions start at 1, while
	    // preferences start at 0
	    voteCounter[outcome[i] - 1] = 
		voteCounter[outcome[i] - 1] + 1;

	long winner = 0;
	for (int i = 1; i < actions; i++)
	    if (voteCounter[i] > voteCounter[(int)winner])
		winner = i;

	return winner;
    }



    /**
     * Return the payoff for the given player in the given
     * outcome.  Assumes everything has already been set up.
     */    
    public double getPayoff(int[] outcome, int player) {
	long winner = determineWinner(outcome);
	return (preferences[player][(int)winner]);
    }


    /**
     * Return a Vector with all players' utilities at the
     * given outcome
     */
    public Vector getPayoff(int[] outcome)
    {
	long winner = determineWinner(outcome);
	int players = getNumPlayers();

	Vector payoffVector = new Vector(players);
	for (int i = 0; i < players; i++) {
	    Double pref = new Double(preferences[i][(int)winner]);
	    payoffVector.add(i, pref);
	}

	return payoffVector;
    }


    protected String getGameHelp()
    {
	return "Creates an instance of the Majority Voting Game.\n\n" +
	    "In this version of the Majority Voting Game, " +
	    "players' utilities for each candidate (i.e. action) " +
	    "being declared the winner are arbitrary and it is " +
	    "possible that a player would be indifferent between " +
	    "two or more candidates.\n\n" +
	    "If multiple candidates have the same number of votes and " +
	    "this number is higher than the number of votes any other " +
	    "candidate has, then the candidate with the lowest number " +
	    "is declared winner." + getRangeHelp();
    }


    /**
     * Generate the game and store all of the players' utilities
     * for each candidate in preferences matrix
     */
    public void doGenerate() {
	double low = DEFAULT_LOW;
	double high = DEFAULT_HIGH;

	setDescription("Majority Voting\n" + getDescription());
	setName("Majority Voting");

	int players = getNumPlayers();
	int actions = getNumActions(0); // same for all players

	preferences = new double[players][actions];

	for (int i = 0; i < players; i ++) {
	    for (int j = 0; j < actions; j++) {
		preferences[i][j] = Global.randomDouble(low, high);
	    }
	}
    }


}

