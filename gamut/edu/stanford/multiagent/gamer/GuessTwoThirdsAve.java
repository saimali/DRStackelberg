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
 * Generates an instance of the game in which players try to
 * guess two thirds of the average of the amounts chosen by
 * other players.
 */

public class GuessTwoThirdsAve extends Game
{

    //---------------------------------------------------------
    // Parameters: This game is only parameterized by the number
    // of players and the number of actions.

    private static Parameters.ParamInfo[] gttaParam;

    static {
	gttaParam = new Parameters.ParamInfo[] {Game.players, Game.symActions};
	Global.registerParams(GuessTwoThirdsAve.class, gttaParam);
    }


    public GuessTwoThirdsAve() 
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

	// Set the number of players and the number of actions
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
     * Return two-thirds of the average of the amounts in outcome
     */
    public double getTwoThirdsAve(int[] outcome) 
    {
	double ave = 0;
	for (int i = 0; i < getNumPlayers(); i++)
	    ave += (double) outcome[i];
	ave /= getNumPlayers();
	return (2.0 * ave / 3.0);
    }




    /**
     * Return the payoff for the given player in the given outcome.
     */    
    public double getPayoff(int[] outcome, int player) 
    {
	double ave = getTwoThirdsAve(outcome);
	int numClosest = 1;
	double distOfClosest = Math.abs(outcome[0] - ave);

	for (int i = 1; i < getNumPlayers(); i++) {
	    double dist = Math.abs(outcome[i] - ave);
	    if (dist == distOfClosest) 
		numClosest++;
	    else if (dist < distOfClosest) {
		numClosest = 1;
		distOfClosest = dist;
	    }
	}

	if (Math.abs(outcome[player] - ave) == distOfClosest)
	    return DEFAULT_HIGH / numClosest;

	return 0.0;
    }


    /**
     * Return a Vector with all players' utilities at the
     * given outcome
     */
    public Vector getPayoff(int[] outcome)
    {
	double ave = getTwoThirdsAve(outcome);
	int numClosest = 1;
	double distOfClosest = Math.abs(outcome[0] - ave);
	
	for (int i = 1; i < getNumPlayers(); i++) {
	    double dist = Math.abs(outcome[i] - ave);
	    if (dist == distOfClosest) 
		numClosest++;
	    else if (dist < distOfClosest) {
		numClosest = 1;
		distOfClosest = dist;
	    }
	}
	
	Vector payoffVector = new Vector(getNumPlayers());

	for (int i = 0; i < getNumPlayers(); i++) {
	    if (Math.abs(outcome[i] - ave) == distOfClosest) {
		Double pay = new Double(DEFAULT_HIGH / numClosest);
		payoffVector.add(i, pay);
	    } else {
		Double pay = new Double(0.0);
		payoffVector.add(i, pay);
	    }
	}

	return payoffVector;
    }


    protected String getGameHelp()
    {
	return "Creates an instance of the game in which all players " +
	    "guess a number trying to come as close as possible to " +
	    "two thirds of the average of the numbers guessed by all " +
	    "players.\n\n" +
	    "By default, the payoffs for this game are in the range from " +
	    "0 to " + DEFAULT_HIGH + ", where the player whose guess comes " +
	    "closest to two thirds of the average receives " + DEFAULT_HIGH + 
	    " and the others receive 0.  If more there is a tie, the payoff " +
	    "amount is split.  To change the range of payoffs you can " +
	    "use the normalization or integer payoff options.";
    }



    /**
     * Not really anything to generate
     */
    public void doGenerate() {
	setDescription("Guess Two Thirds of the Average\n" + getDescription());
	setName("Guess Two Thirds of the Average");
    }


}

