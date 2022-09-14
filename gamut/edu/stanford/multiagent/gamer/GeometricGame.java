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
 * Class implements shared details of Geometric Games.  
 *
 * It is assumed that all Geometric Games have two players, and
 * that each chooses a subset of elements from a set S as their
 * action.  There may be limits on the max number of elements from S
 * which each may choose, but otherwise choices are unrestricted.
 *
 * The payoff will always be calculated based on the set which the
 * the first ("red") player chooses from S, the set which the second
 * ("blue") player chooses from S, and the intersection of these sets.
 *
 * The payoff function is abstract and must be filled in by each 
 * subclass of Geometric Game.
 *
 */

public abstract class GeometricGame extends Game
{
    // Size of the set S
    private long setSize;
    // Max number of elements players are allowed to choose
    private long maxInSubset[];

    
    /**
     * Constructor for new geometric games.
     */
    public GeometricGame()
	throws Exception
    {
	super();
    }


    /**
     * Initializes all variables common to geometric games.
     *
     * @param sizeOfSet the total number of items in the set from which
     * players will choose
     * @param maxInR the maximum number of items that the first player
     * will be able to choose from the set
     * @param maxInB the maximum number of items that the second player
     * will be able to choose from the set
     */
    public void initVariables(long sizeOfSet, long maxInR, long maxInB) 
    {
	setSize = sizeOfSet;

	// Set the maximum numbers of elements each player is
	// allowed to select from his set of choices
	maxInSubset = new long[2];
	maxInSubset[0] = maxInR;
	maxInSubset[1] = maxInB;
    }


    /**
     * Returns the payoff for the given player at the given
     * outcome, which must first be translated into subsets of
     * the elements which are chosen by each player.
     *
     * @param outcome an array holding the actions chosen by 
     * each player
     * @param player the player whose payoff to return
     */
    public double getPayoff(int[] outcome, int player) {

	// First get the sets chosen by each player
	BitSet R = actionToSubset(outcome[0], maxInSubset[0]);
	BitSet B = actionToSubset(outcome[1], maxInSubset[1]);

	// Get a set containing the intersection of the
	// sets chosen by the two players
	BitSet intersection = (BitSet) R.clone();
	intersection.and(B);

	double payoff = calculatePayoff(player, R, B, intersection);

	return payoff;
    }


    /**
     * Calculates the payoff for a player based on the set chosen
     * by player one, the set chosen by player 2, and the intersection
     * of these two sets.  This function must be overwritten by
     * every child class.
     *
     * @param player the player whose payoff should be returned
     * @param R the set chosen by player 1
     * @param B the set chosen by player 2
     * @param intersection the intersection of the sets chosen by
     * players 1 and 2
     */
    public abstract double calculatePayoff(int player, BitSet R, BitSet B,
					   BitSet intersection);



    /**
     * Translates an integer action number into a subset of S
     * containing at most maxElements elements
     *
     * @param action the integer index of the action 
     * @param maxElements the maximum number of elements that the
     * player who has chosen the given action is allowed to choose
     * from the set
     */
    private BitSet actionToSubset(int action, long maxElements) 
    {      
	BitSet chosenSet = new BitSet((int) setSize);
	
	int actionCounter = action;
	int numElements = 1;

	// first figure out how many elements should be in the 
	// set based on the action number
	try {
	    while (actionCounter > Global.NChooseM(setSize, numElements)) {
		actionCounter -= Global.NChooseM(setSize, numElements);
		numElements++;
	    }
	} catch (Exception e) {
	    System.out.println("Error in actionToSubset");
	    chosenSet.clear();
	    return chosenSet;
	}
	
	chosenSet.set(0, numElements, true);

	// Cycle through the different possible sets with 
	// numElements elements selected until we reach the
	// proper action counter index
	while (actionCounter > 1) {
	    actionCounter--;

	    boolean foundClear = false;
	    int toReset = 0;
	    int index = 0;

	    while (!foundClear) {
		index = chosenSet.nextSetBit(index) + 1;
		if (index < setSize) {
		    if (!chosenSet.get(index)) {
			foundClear = true;
		    } else { 
			toReset++;
		    }
		} else {
		    System.out.println("Error in actionToSubset!!");
		    chosenSet.clear();
		    return chosenSet;
		}
	    }

	    chosenSet.set(0, index, false);
	    chosenSet.set(0, toReset, true);
	    chosenSet.set(index, true);
	}
	return chosenSet;
    }


    /**
     * Returns the number of possible actions for a player choosing
     * a set of maximum size maxSize from a set of size fullSetSize.
     * 
     * @param fullSetSize size of the full set
     * @param maxSize the maximum number of items that can be chosen
     */
    public long getGeoNumActions(long fullSetSize, long maxSize) 
	throws Exception
    {
	long totalActions = 0;
	for (long i = 1; i <= maxSize; i++)
	    totalActions += Global.NChooseM(fullSetSize, i);
	return totalActions;
    }
}

