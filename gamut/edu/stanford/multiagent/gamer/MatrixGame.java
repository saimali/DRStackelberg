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
 * Class implements the straightforward normal form game.
 */

public abstract class MatrixGame extends Game
{
    private DoubleTensor[] payoffs;

    /**
     * Constructor for a new matrix game.
     */
    public MatrixGame()
	throws Exception
    {
	super();
    }


    /**
     * Initializes the payoff array.  Assumes that the numbers of
     * players and actions have already been set.
     */
    protected void initMatrix()
    {
	payoffs = new DoubleTensor[getNumPlayers()];

	for(int i=0; i<getNumPlayers(); i++)
	    payoffs[i] = new DoubleTensor(getNumActions());
    }

    /**
     * Returns the payoff for a player for a given outcome.
     *
     * @param outcome an array containing the actions chosen by each player
     * @param player the player whose payoff should be returned.
     */
    public double getPayoff(int[] outcome, int player)
    {
	return payoffs[player].getValue(outcome);
    }


    /**
     * Sets the payoff for a player for a given outcome.
     *
     * @param outcome an array containing the actions chosen by each player
     * @param player the player whose payoff should be returned.
     * @param value the amount of the payoff
     */
    protected  void setPayoff(int[] outcome, int player, double value)
    {
	payoffs[player].setValue(value, outcome);
    }
}

