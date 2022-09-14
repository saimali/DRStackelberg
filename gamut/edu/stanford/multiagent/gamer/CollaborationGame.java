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
 * Return an instance of a collaboration game.
 *
 * Note that our definition of a regular coordination games 
 * allows  for the values of different equilibria to be 
 * Pareto-ordered but not necessarily all the same.  In contrast,
 * the Collaboration Game demands that all equilibria yield the 
 * same payoff.
 *
 * Both the coordination game and the collaboration game
 * are common payoff.
 */ 


public class CollaborationGame extends CoordinationGame
{

    // Parameters: The collaboration game uses the same
    // parameters as the coordination game.

    static {
	Global.registerParams(CollaborationGame.class, cgParam);
    }


    // ----------------------------------------------


    public CollaborationGame() 
	throws Exception
    {
	super();
    }


    protected String getGameHelp()
    {
	return "Creates a collaboration game.\n\nBy our definition, " +
	    "both coordination and collaboration games are common payoff " +
	    "yet not always symmetric.  The highest payoffs are for " +
	    "all outcomes in which every player chooses the same " +
	    "action.  In the collaboration game (unlike in the general " +
	    "coordiation game) these outcomes will all yield the same " +
	    "payoff.\n\n" +
	    "By default, payoffs will be in the range [-100, 100] with " +
	    "all coordinated payoffs set to 100 for each player. " +
	    "To change this range, use the normalization or " +
	    "integer payoff options.";
    }



    /**
     * Generates one payoff for every outcome, making sure that
     * all of the equilibrium outcomes (those on the diagonal
     * of the matrix) are higher than all of the non-equilibrium 
     * outcomes and all equal.
     */
    public void doGenerate()
    {
	double low = DEFAULT_LOW;
	double high = DEFAULT_HIGH;
	double lowEq = (high - low) / 2.0;

	setDescription("Collaboration Game\n" + getDescription());
	setName("Collaboration Game");

	Outcome outcome=new Outcome(getNumPlayers(), getNumActions());

	// Choose the payoff for all equilibria in advance since
	// this will always be the same.
	double eqPay = Global.randomDouble(lowEq, high);

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
		pay = eqPay;
	    } else { 
		pay = Global.randomDouble(low, lowEq - 1);
	    }

	    setPayoff(outcome.getOutcome(), pay);
	}
    }

}
