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
 * Abstract class implements a normal form game which is 
 * common payoff and therefore only requires that the payoff
 * for each outcome be stored once.
 */


public abstract class PureCoordinationMatrix extends Game
{
    private DoubleTensor payoffs;

    // -----------------------------------------------

    /**
     * Constructor for a new pure coordination matrix game.
     */
    public PureCoordinationMatrix()
	throws Exception
    {
	super();
    }


    /**
     * Initializes the payoff array assuming the numbers of players
     * and actions have already been set.
     */
    protected void initMatrix()
    {
	payoffs = new DoubleTensor(getNumActions());
    }



    /**
     * Returns the payoff for a player at a given outcome.
     *
     * @param outcome an array holding the action choices for each player
     * @param player the player whose payoff should be returned, irrelevant
     * since the game is common payoff
     */
    public double getPayoff(int[] outcome, int player)
    {
	return payoffs.getValue(outcome);
    }



    /**
     * Sets the payoff for all players for a given outcome.
     *
     * @param outcome an array holding the action choices for each player
     * @param value the amount of the payoff for this outcome 
     */
    protected  void setPayoff(int[] outcome, double value)
    {
	payoffs.setValue(value, outcome);
    }
}

