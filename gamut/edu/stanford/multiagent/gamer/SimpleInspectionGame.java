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
 * Return an instance of the Simple Inspection game
 */ 


public class SimpleInspectionGame extends GeometricGame
{

    // --------------------------------------------------------

    // Parameters: Geometric games must always have two players
    // and a number of actions based on the available subsets which
    // can be chosen.  Thus the SimpleInspection Game is parameterized
    // first on the number of elements in the set S and the max number
    // which each of the two players can choose for their sets.  The
    // number of actions for each player is (|S| choose max number)
    // + (|S| choose max number - 1)  + ... + (|S| choose 1)

    private static Parameters.ParamInfo sizeOfSet;
    private static Parameters.ParamInfo maxForR;
    private static Parameters.ParamInfo maxForB;
    private static Parameters.ParamInfo[] sigParam;

    static {
	sizeOfSet = new Parameters.ParamInfo("set_size", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(8), "number of elements in set S from which the players choose elements.  Must be > 0 but <= 8 in order o keep the number of actions reasonable.");

	maxForR = new Parameters.ParamInfo("max_r", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(8), "maximum number of elements which player one (the \"red\" player) can choose from S.  Must be > 0 and <= set_size.");

	maxForB = new Parameters.ParamInfo("max_b", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(8), "maximum number of elements which player two (the \"blue\" player) can choose from S.  Must be > 0 and <= set_size.");

	sigParam = new Parameters.ParamInfo[] {sizeOfSet, maxForR, maxForB}; 
	Global.registerParams(SimpleInspectionGame.class, sigParam);
    }


    // ---------------------------------------------- 


    public SimpleInspectionGame()  
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

	numActions[0] = (int)getGeoNumActions(getLongParameter(sizeOfSet.name),
					 getLongParameter(maxForR.name));
	numActions[1] = (int)getGeoNumActions(getLongParameter(sizeOfSet.name),
					 getLongParameter(maxForB.name));
	
	setNumActions(numActions); 
    }



    //
    // Make sure that the parameters are in the proper range
    //
    protected void checkParameters() throws Exception 
    {
	if (getLongParameter(sizeOfSet.name) <= 0)
	    throw new Exception("set_size <= 0");

	if (getLongParameter(maxForR.name) <= 0)
	    throw new Exception("max_r <= 0");

	if (getLongParameter(maxForR.name) > getLongParameter(sizeOfSet.name))
	    throw new Exception("max_r > set_size");

	if (getLongParameter(maxForB.name) <= 0)
	    throw new Exception("max_b <= 0");

	if (getLongParameter(maxForB.name) > getLongParameter(sizeOfSet.name))
	    throw new Exception("max_b > set_size");
    }



    //
    // Randomize parameters that were not set by the user
    //
    public void randomizeParameters() {

	// Require that the user sets either all or none of 
	// set_size, max_r, and max_b.
	if ((!parameters.setByUser(sizeOfSet.name)) 
	    && (!parameters.setByUser(maxForR.name))
	    && (!parameters.setByUser(maxForB.name))) {
	 
	    parameters.randomizeParameter(sizeOfSet.name);
	    maxForR.high = new Long(getLongParameter(sizeOfSet.name));
	    maxForB.high = new Long(getLongParameter(sizeOfSet.name));
	    parameters.randomizeParameter(maxForR.name);
	    parameters.randomizeParameter(maxForB.name);
   
	} else if ((!parameters.setByUser(sizeOfSet.name)) 
		   || (!parameters.setByUser(maxForR.name))
		   || (!parameters.setByUser(maxForB.name))) {
	    Global.handleError("Randomization error.  User must set " +
			       "values for all or none of set_size, " +
			       "max_r, and max_b parameters.");
	}
    }



    protected String getGameHelp()
    {
	return "Creates a 2 player Simple Inspection Game\n\n" +
	    "This game is very similar to the Greedy Game. " +
	    "Each action represents a chosen subset from a set of " +
	    "total size set_size. " +
	    "Player 1 can choose any subset of up to max_r elements " +
	    "while Player 1 can only choose subsets up to size max_b.\n\n" +
	    "If the intersection of the \"sets\" chosen by the players " +
	    "is empty then the payoff to Player 2 will be " + DEFAULT_HIGH +
	    " while the payoff to Player 1 will be " + DEFAULT_LOW +
	    " Otherwise both players will receive 0.\n\n" +
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
     * is not empty, the payoff to both players will be 0.  If it 
     * is empty, the payoff to player 2 will be DEFAULT_HIGH and the
     * payoff to player 1 will be DEFAULT_LOW. 
     */
    public double calculatePayoff(int player, BitSet R, BitSet B,
				BitSet intersection) {
	double payoff;

	if (R.intersects(B))
	    payoff = 0;
	else if (player == 0) 
	    payoff = DEFAULT_LOW;
	else 
	    payoff = DEFAULT_HIGH;

	return payoff;
    }


    /**
     * Generate game, set all variables so that payoffs can
     * be calculated when they are needed
     */
    public void doGenerate()
    {
        setDescription("Simple Inspection Game \n" + getDescription());
	setName("Simple Inspection Game");

	initVariables(getLongParameter(sizeOfSet.name),
		      getLongParameter(maxForR.name),
		      getLongParameter(maxForB.name));

    } 

} 
