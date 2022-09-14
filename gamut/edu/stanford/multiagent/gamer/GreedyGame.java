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
 * Return a version of the Greedy Game
 */ 


public class GreedyGame extends GeometricGame
{
    // --------------------------------------------------------

    // Parameters: Geometric games must always have two players
    // and a number of actions based on the available subsets which
    // can be chosen.  Thus the Greedy Game is parameterized
    // first on the number of elements in the set S and the max number
    // of elements which red may choose.  The number of actions for 
    // each player is (|S| choose max number) + (|S| choose max number - 1)  
    // + ... + (|S| choose 1) where the max number for blue is the
    // size of S.

    private static Parameters.ParamInfo sizeOfSet;
    private static Parameters.ParamInfo maxForR;
    private static Parameters.ParamInfo[] ggParam;

    static {
	// When randomizing the size of the set, limit it to
	// a max of 8 so that the number of actions does not
	// get too absurdly out of hand.

	sizeOfSet = new Parameters.ParamInfo("set_size", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(8), "number of elements in set S from which the players choose elements.  Must be > 0 and <= 8 for the sake of keeping the number of actions reasonable.");

	maxForR = new Parameters.ParamInfo("max_r", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(8), "maximum number of elements which player one (the \"red\" player) can choose from S.  Must be > 0 and <= set_size.");

	ggParam = new Parameters.ParamInfo[] {sizeOfSet, maxForR}; 
	Global.registerParams(GreedyGame.class, ggParam);
    }


    // ---------------------------------------------- 


    public GreedyGame()  
	throws Exception 
    { 
	super();
    }



    public void initialize()
	throws Exception
    { 
	super.initialize();
	
	// All geometric games have two players
	setNumPlayers(2);

	// Figure out how many actions each player has and 
	// set these numbers
	int numActions[];
	numActions = new int[2];

	// Red player has a limit on the size of the subset which
	// can be chosen, but blue player does not.
	numActions[0] = (int)getGeoNumActions(getLongParameter(sizeOfSet.name),
					 getLongParameter(maxForR.name));
	numActions[1] = (int)getGeoNumActions(getLongParameter(sizeOfSet.name),
					 getLongParameter(sizeOfSet.name));
	
	setNumActions(numActions); 
    }


    protected void checkParameters() throws Exception 
    {
	if (getLongParameter(sizeOfSet.name) <= 0)
	    throw new Exception("set_size <= 0");

	if (getLongParameter(maxForR.name) <= 0)
	    throw new Exception("max_r <= 0");

	if (getLongParameter(maxForR.name) > getLongParameter(sizeOfSet.name))
	    throw new Exception("max_r > set_size");
    }


    public void randomizeParameters() 
    {
	if (parameters.setByUser(maxForR.name)) {
	    if (!(parameters.setByUser(sizeOfSet.name))) {
		// reset range on the set size and randomize it
		sizeOfSet.low = new Long(getLongParameter(maxForR.name));
		parameters.randomizeParameter(sizeOfSet.name);
	    }
	} else {
	    if (parameters.setByUser(sizeOfSet.name)) {
		// resent the range on the max r and randomize it
		maxForR.high = new Long(getLongParameter(sizeOfSet.name));
		parameters.randomizeParameter(maxForR.name);
	    } else {
		// if neither have been set in advance, randomize
		// max r, then resent the range on set size and randomize
		parameters.randomizeParameter(maxForR.name);
		sizeOfSet.low = new Long(getLongParameter(maxForR.name));
		parameters.randomizeParameter(sizeOfSet.name);
	    }
	}
    }

    protected String getGameHelp()
    {
	return "Creates a 2 player Greedy Game.\n\n" +
	    "In this game, each action represents a chosen subset. " +
	    "Player 2 can choose any subset of set_size elements " +
	    "while Player 1 can only choose subsets up to size max_r.\n\n" +
	    "If the intersection of the \"sets\" chosen by the players " +
	    "is empty then the payoff to Player 2 will be the number of " +
	    "elements in the set he has chosen while the payoff to " +
	    "Player 1 will be the negation of this.  Otherwise both " +
	    "players will receive 0.\n\n" +
	    "To change the range of the payoff values, you may use " +
	    "normalization or integer based payoffs.\n\n" +
	    "Note that the number of actions available to each player " +
	    "is (|S| choose maxnumber) + (|S| choose maxnumber - 1) " +
	    "+ ... + (|S| choose 1) where maxnumber is the maximum " +
	    "number of items in the set that the player can choose.";
    }



    /**
     * All geometric games must provide a function to calculate the
     * payoff for each player based on the sets chosen and the
     * intersection between them.  In this case, if the intersection
     * is nonempty, the payoff to both players will be 0.  If it 
     * is empty, the payoff to player 2 will be the number of 
     * elements in the set player 2 has chosen, while the payoff to 
     * player 1 will be the negation of this. 
     */
    public double calculatePayoff(int player, BitSet R, BitSet B,
				BitSet intersection) {
	double payoff;

	if (R.intersects(B))
	    payoff = 0;
	else if (player == 0) 
	    payoff = -B.cardinality();
	else 
	    payoff = B.cardinality();

	return payoff;
    }


    /**
     * Generate game, set all variables so that payoffs can
     * be calculated when they are needed.
     */
    public void doGenerate()
    {
        setDescription("Greedy Game \n" 
			 + getDescription());
	setName("Greedy Game");

	initVariables(getLongParameter(sizeOfSet.name),
		      getLongParameter(maxForR.name),
		      getLongParameter(sizeOfSet.name));

    } 

} 
