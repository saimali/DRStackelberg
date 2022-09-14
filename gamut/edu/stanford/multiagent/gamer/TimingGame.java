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
 * Abstract class implements common features among timing games.
 *
 * For the extents and purposes of this class, "timing
 * game" can refer to any game in which the payoff is
 * computed using a function of the "earliest" (lowest
 * numbered) action chosen, your own action, and whether
 * your action came first, tied for first, or came later.
 */

public abstract class TimingGame extends Game
{

    // --------------------------------------------------------

    /**
     * TimingGameParams is a simple helper class used to store 
     * all of the parameters for the Timing Game function for 
     * one particular player.
     *
     * This can be extended if timing games with more complicated
     * payoff functions are added.  If any of the parameters are
     * set to 0, their corresponding terms are ignored.
     */

    public static class TimingGameParams {
	private double multYourTime;
	private double multLowTime;
	private double additional;
	
	/**
	 * Constructor for TimingGameParams objects.
	 *
	 * @param yourTime amount to be multiplied by a player's individual
	 * time and added to a payoff
	 * @param lowTime amount to be multiplied by the lowest time chosen
	 * by any player and added to a payoff
	 * @param added additional amount to be added to a payoff
	 */
	public TimingGameParams(double yourTime, double lowTime,
				double added)
	{
	    this.multYourTime = yourTime;
	    this.multLowTime = lowTime;
	    this.additional = added;
	}

	/**
	 * Returns the multiplier for a player's own time.
	 */
	public double getMultYourTime() {
	    return multYourTime;
	}
	
	/**
	 * Sets the multiplier for a player's own time.
	 */
	public void setMultYourTime(double value) {
	    multYourTime = value;
	}
	
	/**
	 * Returns the multiplier for the lowest time of all players.
	 */
	public double getMultLowTime() {
	    return multLowTime;
	}
	
	/**
	 * Sets the multiplier for the lowest time of all players.
	 */

	public void setMultLowTime(double value) {
	    multLowTime = value;
	}
	
	/**
	 * Returns the extra amount to be added to the payoff.
	 */
	public double getAdditional() {
	    return additional;
	}
		
	/**
	 * Sets the extra amount to be added to the payoff.
	 */
	public void setAdditional(double value) {
	    additional = value;
	}    
    }

 
    // -----------------------------------------------------


    private TimingGameParams lowPlayerParams[];
    private TimingGameParams tiedForLowParams[];
    private TimingGameParams notLowestParams[];

    // allowTies should be true for games in which it is possible
    // for people to make moves that end the game at the same time
    // (for example, war of attrition), and false for games in which 
    // only one player really ends the game (like centipede)
    private boolean allowsTies;


    // -----------------------------------------------------


    /**
     * Constructor for a new timing game.
     */
    public TimingGame()
	throws Exception
    {
	super();
    }


    /**
     * Sets the parameters for the payoff functions for the case
     * when the player has the lowest time, the case when the player
     * is tied, and the case when the player does not have the lowest 
     * time.  If ties are not allowed, use setParamsNoTies instead.
     *
     * @param lowParams parameter settings for calculating payoff in
     * the case when a player is the only one with the lowest time
     * @param tiedParams parameter settings for calculating payoff in
     * the case when a player is tied for the lowest time
     * @param notLowestParams parameter settings for calculating payoff in
     * the case when a player does not have the lowest time
     */
    public void setParamsWithTie(TimingGameParams lowParams[],
				 TimingGameParams tiedParams[],
				 TimingGameParams notLowParams[])
    {
	int players = getNumPlayers();

	lowPlayerParams = new TimingGameParams[players];
	tiedForLowParams = new TimingGameParams[players];
	notLowestParams = new TimingGameParams[players];

	for (int i = 0; i < players; i++) {
	    lowPlayerParams[i] = lowParams[i];
	    tiedForLowParams[i] = tiedParams[i];
	    notLowestParams[i] = notLowParams[i];
	}
	allowsTies = true;
    }


    /** 
     * Set the parameters for the payoff functions for the case
     * when the player has the lowest time and for when the player
     *does not.  Use this function if ties are not allowed.
     *
     * @param lowParams parameter settings for calculating payoff in
     * the case when a player is the only one with the lowest time
     * @param notLowestParams parameter settings for calculating payoff in
     * the case when a player does not have the lowest time
     */
    public void setParamsNoTies(TimingGameParams lowParams[],
				TimingGameParams notLowParams[])
    {
	int players = getNumPlayers();

	lowPlayerParams = new TimingGameParams[players];
	notLowParams = new TimingGameParams[players];
	tiedForLowParams = null;

	for (int i = 0; i < players; i++) {
	    lowPlayerParams[i] = lowParams[i];
	    notLowestParams[i] = notLowParams[i];
	}

	allowsTies = false;
    }


    /**
     * Returns the payoff for the given player in the given
     * outcome using the common timing game parameterized
     * function.  Assumes all parameters have already been
     * properly set up.
     *
     * @param outcome an array holding the actions of each player
     * @param player the player whose payoff should be returned
     */
    public double getPayoff(int[] outcome, int player) {

	boolean playerLowest = true;
	boolean otherChoseSame = false;
	boolean lowerChoseSame = false;
	int lowestTime = outcome[player];

	// Determine if this player chose the lowest time,
	// if others chose the same time, and what the 
	// lowest time was
	for (int i = 0; i < getNumPlayers(); i++)
	    if (i != player)
		if (outcome[i] < lowestTime) {
		    playerLowest = false;
		    lowestTime = outcome[i];
		} else if (outcome[i] == outcome[player]) {
		    otherChoseSame = true;
		    if (i < player)
			lowerChoseSame = true;
		}

	// Choose the correct function parameters based on
	// whether or not the player chose the lowest time
	TimingGameParams params;
	if (playerLowest) {
	    if (otherChoseSame) {
		if (allowsTies) {
		    // Player's move ended the game simultaneously
		    // with other players' ending moves
		    params = tiedForLowParams[player];
		} else {
		    if (lowerChoseSame) {
			// Player's move did not end the game
			// because an "earlier" player ended on
			// the same turn
			params = notLowestParams[player];
		    } else {
			// Player's move did end the game even
			// though "later" players would have 
			// ended on the same turn
			params = lowPlayerParams[player];
		    }
		}
	    } else {
		// Player's move did end the game
		params = lowPlayerParams[player];
	    } 
	} else {
	    // Player's move did not end the game
	    params = notLowestParams[player];
	}

	// Calculate the payoff based on the parameters that
	// have been set above, the time tha the game was ended,
	// and the time that the player chose / would have
	// chosen to end the game

        return (params.getMultYourTime() * outcome[player] +
		params.getMultLowTime() * lowestTime +
		params.getAdditional());
    }


}

