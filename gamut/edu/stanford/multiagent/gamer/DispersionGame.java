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
 * Class implements a specific kind of dispersion game:
 * both action symmetric and agent symmetric as defined in
 * the dispersion games literature, also strong,
 * and common payoff.
 *
 * It is not required that the number of players be
 * equal to the number of actions.
 *
 * The dispersion ordering used is a full ordering where
 * one outcome is more dispersed than another if the entropy
 * is higher.  (Other implementations could have been used,
 * such as keeping the partial ordering or using standard
 * deviation, but the entropy-based ordering is easy to
 * implement and equally valid.)
 */


public class DispersionGame extends Game
{
    // Need a data structure to map entropy values to payoffs
    private HashMap payoffs;


    //-----------------------------------------------
    // Parameters: The dispersion game is parameterized
    // by the number of players and the number of actions.
    //
    private static Parameters.ParamInfo[] dgParam;

    static {
	dgParam = new Parameters.ParamInfo[] {Game.players, Game.symActions};
	Global.registerParams(DispersionGame.class, dgParam);
    }


    // ---------------------------------------------------------
   


    public DispersionGame() 
	throws Exception
    {
	super();
    }


    public void setParameters(ParamParser p, boolean randomize)
	throws Exception
    {
	super.setParameters(p, randomize);
    }


    /**
     * Initialize the dispersion game by setting the number
     * of players and the number of actions and creating the
     * hashmap for storing entropy/payoff pairs.
     */
    public void initialize()
	throws Exception
    {
	super.initialize();	
	parsePlayersSameNumberActions();

	payoffs = new HashMap();
    }


    /**
     * Return the entropy of the given array assuming that 
     * the values in the array add up to total
     */
    private double getEntropy(int[] a, long total) {

	double entropy = 0;
	for (int i = 0; i < a.length; i++) {
	    double prob = (double) a[i] / (double) total;
	    if (prob != 0) {
		double logBase2 = Math.log(prob) / Math.log(2);
		entropy -= logBase2 * prob;
	    }
	}

	// Round to the nearest 0.00001 so that doubles which
	// are off by a very small amount will be treated as
	// the same
	entropy = Math.round(entropy / 0.00001) * 0.00001;

	return entropy;
    }



    /** 
     * The payoff will be the same for each player, based
     * on how dispersed the actions are as a whole.
     */
    public double getPayoff(int[] outcome, int player)
    {
	// Make an array which holds the number of players
	// who chose each action
	int actions = getNumActions(0);
	int[] actionDist = new int[actions];
	for (int i = 0; i < actions; i++) actionDist[i] = 0;

	for (int i = 0; i < getNumPlayers(); i++) {
	    int chosen = outcome[i] - 1;
	    actionDist[chosen]++;
	}

	double entropy = getEntropy(actionDist, getNumPlayers());
	Double entDoub = new Double(entropy);

	if (!payoffs.containsKey(entDoub))
	    Global.handleError("Payoff not found for given entropy");

	Double payDouble = (Double) payoffs.get(entDoub);
	return payDouble.doubleValue();
    }



    /**
     * Since the game is common payoff, it is much more
     * efficient to get all payoffs at once.
     */
    public Vector getPayoff(int[] outcome)
    {
	Vector payVector = new Vector();

	// Make an array which holds the number of players
	// who chose each action
	int actions = getNumActions(0);
	int[] actionDist = new int[actions];
	for (int i = 0; i < actions; i++) actionDist[i] = 0;

	for (int i = 0; i < getNumPlayers(); i++) {
	    int chosen = outcome[i] - 1;
	    actionDist[chosen]++;
	}

	double entropy = getEntropy(actionDist, getNumPlayers());
	Double entDoub = new Double(entropy);

	if (!payoffs.containsKey(entDoub))
	    Global.handleError("Payoff not found for given entropy");

	Double payDouble = (Double) payoffs.get(entDoub);
	for (int i = 0; i < getNumPlayers(); i++) {
	    payVector.add(payDouble);
	}

	return payVector;
    }


    //
    // None to check
    // 
    protected void checkParameters() throws Exception 
    {
    }


    //
    // Nothing to randomize
    //
    public void randomizeParameters() 
    {
    }


    protected String getGameHelp() 
    {
	return "Returns a strong dispersion game which is both " +
	    "action and player symmetric as well as common payoff. " +
	    "An entropy calculation is used in order to determine " +
	    "when one outcome is more dispersed than another, " +
	    "although this could easily be replaced by standard " +
	    "deviation or a similar test." +
	    getRangeHelp();
    }


   
    /**
     * Generate all groupings of outcomes that will have the
     * same entropy with the given number of players and number
     * of actions.  Generate a list of payoffs of length equal
     * to the number of groups.  Sort the groups by entropy
     * and assign payoffs, always assigning higher payoffs to
     * groups with higher entropies.
     */
    public void doGenerate()
    {

	setDescription("Dispersion Game\n" + getDescription());
	setName("Dispersion Game");

	double low = DEFAULT_LOW;
	double high = DEFAULT_HIGH;

	SortedAndRandomSet entropies = new SortedAndRandomSet();
	int actions = getNumActions(0);
	int players = getNumPlayers();


	// Fill in the entropies vector by generating 
	// the sequence of possible unique outcomes and
	// calculating the entropy for each.
	//
	// For example, the sequence of ways that 9 symmetric
	// players can disperse over 5 outcomes is
	//
	// 90000, 81000, 72000, 71100, 63000, 62100, 61110,
	// 54000, 53100, 52200, 52110, 51111, 44100, 43200,
	// 43110, 42210, 42111, 33300, 33210, 33111, 32220, 
	// 32211, 22221

	int[] outcome = new int[actions];
	outcome[0] = players;
	for (int i = 1; i < actions; i++) outcome[i] = 0;

	// Store the initial entropy before looping through
	// the rest of the sequence
	Double ent = new Double(getEntropy (outcome, players));
	entropies.add(ent);

	int remainder = 0;    

	while (outcome[0] != 1) {
	    int i = actions - 1;


	    // Search for the last spot in the array which is
	    // not 0 or 1.  Add all of the 1s to remainder.

	    while (outcome[i] <= 1) {
		remainder += outcome[i];
		i--;
	    }


	    // When an spot is found that has a value of 2 or
	    // more, subtract one and split the remainder over
	    // the rest of the array making sure that no spot
	    // has a higher value than the one before.

	    outcome[i]--;
	    remainder++;

	    for (int j = i+1; j < actions; j++) {
		outcome[j] = Global.min(remainder, outcome[i]);
		remainder -= outcome[j];
	    }


	    // If there is no remainder left then there were
	    // enough actions available to fit the players in this
	    // particular step of the sequence.  Add it to the
	    // list.  Otherwise, do not add to the sequence and
	    // keep the remainder set to add back in.
	    //
	    // For example, in the sequence described above for
	    // 9 players and 5 actions, 22221 should be added but
	    // 22211 which is generated next should not.
	    
	    if (remainder == 0) {
		ent = new Double(getEntropy (outcome, players));
		entropies.add(ent);
	    }
	}


	int numPayoffs = entropies.size();

	// Use a SortedAndRandomSet for the payoff values since
	// it does not allow repeats of values and keeps the
	// values sorted
	SortedAndRandomSet payoffValues = new SortedAndRandomSet();

	for (int i = 0; i < numPayoffs; i++) {
	    Double randomPayoffAsDouble;
	    do {
		double randomPayoff = Global.randomDouble(low, high);
		randomPayoffAsDouble = new Double(randomPayoff);
	    } while (!(payoffValues.add(randomPayoffAsDouble)));
	}
	
	// Now match each payoff with an entropy value and
	// add each pair to the hashmap
	for (int i = 0; i < numPayoffs; i++) {
	    Double nextPayoff = (Double) payoffValues.removeSmallest();
	    Double entDoub = (Double) entropies.removeSmallest();
	    payoffs.put(entDoub, nextPayoff);
	}
    }
}
