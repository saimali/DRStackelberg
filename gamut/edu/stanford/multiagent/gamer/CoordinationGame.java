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

/**
 * Return an instance of a coordination game.
 *
 * Note that our definition of coordination games allows 
 * for the values of different equilibria to be Pareto-ordered
 * but not necessarily all the same.  If it is desired that
 * all equilibria yield the same payoff, the collaboration
 * game should be used.
 *
 * Both the coordination game and the collaboration game
 * are common payoff.
 */ 


public class CoordinationGame extends PureCoordinationMatrix
{

    // Parameters: Just the number of players
    protected static Parameters.ParamInfo[] cgParam;

    static {
	cgParam = new Parameters.ParamInfo[] {Game.players};
	Global.registerParams(CoordinationGame.class, cgParam);
    }


    // ----------------------------------------------


    public CoordinationGame() 
	throws Exception
    {
	super();
    }


    public void initialize()
	throws Exception
    {
	super.initialize();

	// Set the number of players
	if(!parameters.isParamSet(Game.players.name))
	    Global.handleError("Required parameter missing: players");
	setNumPlayers((int)getLongParameter(Game.players.name));
	
	// The number of actions for each player will always
	// be equal to the number of players
	int numActions[];
	numActions = new int[getNumPlayers()];
	for (int i = 0; i < getNumPlayers(); i++)
	    numActions[i] = getNumPlayers();
	setNumActions(numActions);

	// Must be called for all subclasses of 
	// PureCoordinationMatrix
	initMatrix();
    }


    protected void checkParameters() throws Exception 
    {
    }


    public void randomizeParameters() {
    }


    protected String getGameHelp()
    {
	return "Creates a Coordination Game.\n\nBy our definition, " +
	    "coordination games are common payoff yet not always " +
	    "symmetric.  The highest payoffs are for all outcomes " +
	    "in which every player chooses the same action, although " +
	    "it is not always the case that all of these outcomes " +
	    "yield the same payoffs.  (See collaboration games.)\n\n" +
	    "By default, payoffs will be in the range [-100, 100] with " +
	    "coordinated payoffs positive and uncoordinated payoffs " +
	    "negative.  To change this range, use the normalization or " +
	    "integer payoff options.";
    }



    /**
     * Generate one payoff for every outcome, making sure that
     * all of the equilibrium outcomes (those on the diagonal
     * of the matrix) are higher than all of the non-equilibrium 
     * outcomes.
     */
    public void doGenerate()
    {
	double low = DEFAULT_LOW;
	double high = DEFAULT_HIGH;
	double lowEq = high - ((high - low) / 2.0);

	setDescription("Coordination Game\n" + getDescription());
	setName("Coordination Game");

	Outcome outcome=new Outcome(getNumPlayers(), getNumActions());

	for (outcome.reset(); outcome.hasMoreOutcomes(); 
	     outcome.nextOutcome()) {

	    int[] actions = outcome.getOutcome();
	    boolean isEquilib = true;
	    double pay;

	    for (int i = 1; i < getNumPlayers(); i++) {
		if (actions[0] != actions[i]) {
		    isEquilib = false;
		    break;
		}
	    }
	    
	    if (isEquilib) {
		pay = Global.randomDouble(lowEq, high);
	    } else { 
		pay = Global.randomDouble(low, lowEq);
	    }

	    setPayoff(outcome.getOutcome(), pay);
	}
    }

}
