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
 * Return a version of the game Grab the Dollar
 */ 


public class GrabTheDollar extends TimingGame
{

    // Parameters: Grab the dollar is parameterized on the number
    // of actions (i.e. highest number of time steps)
    private static Parameters.ParamInfo[] gtdParam;

    static {
	gtdParam = new Parameters.ParamInfo[] {Game.symActions};
	Global.registerParams(GrabTheDollar.class, gtdParam);
    }


    // ----------------------------------------------


    public GrabTheDollar() 
	throws Exception
    {
	super();
    }



    public void initialize()
	throws Exception
    {
	super.initialize();

	// The number of players must always be two, but the number
	// of actions is extensible
	setNumPlayers(2);
	parseSameNumberActions();
    }


    //
    // None to check
    //
    protected void checkParameters() throws Exception 
    {
    }


    //
    // None which can be randomized
    //
    public void randomizeParameters() 
    {
    }


    protected String getGameHelp()
    {
	return "Creates an instance of game Grab the Dollar.\n\n" +
	    "In this game, there is a prize (or \"dollar\") that both " +
	    "players are free to grab at any time, where actions represent " +
	    "the chosen times.  If both players grab for it at " +
	    "the same time, they will rip the price and both will receive " +
	    "the low payoff.  If one chooses a time earlier than " +
	    "the other (i.e. chooses a strictly lower action number " +
	    "number) then he will receive the price (and thus the high " +
	    "payoff) and the opposing player will receive a payoff " +
	    "somewhere between the high and the low." +
	    getRangeHelp();
    }


    public void doGenerate()
    {
	double lowPay = DEFAULT_LOW;
	double highPay = DEFAULT_HIGH;

	setDescription("Grab the Dollar\n" + getDescription());
	setName("Grab the Dollar");

	int players = getNumPlayers();

	SortedAndRandomSet payoffValues = new SortedAndRandomSet();

	// Generate unique payoffs a, b, and c in the range from
	// lowPay to highPay
	for (int i = 0; i < 3; i++) {
	    Double randomPayoffAsDouble;
	    do {
		double randomPayoff = Global.randomDouble(lowPay, highPay);
		randomPayoffAsDouble = new Double(randomPayoff);
	    } while (!(payoffValues.add(randomPayoffAsDouble)));
	}

	Double a = (Double) payoffValues.removeLargest();
	Double b = (Double) payoffValues.removeLargest();
	Double c = (Double) payoffValues.removeLargest();

	// Create TimingGameParams objects which will be 
	// necessary to hold the function parameters for each player
	TimingGameParams low[] = new TimingGameParams[players];
	TimingGameParams notLow[] = new TimingGameParams[players];
	TimingGameParams tie[] = new TimingGameParams[players];

	// Both players will always receive a for uniquely choosing
	// the lowest time, b for choosing the highest time, and c
	// if they both choose the same, with a > b > c
	for (int i = 0; i < players; i++) {
	    // Set up function parameters for when player chooses
	    // lowest time
	    low[i] = new TimingGameParams(0, 0, a.doubleValue());

	    // Function params for when player ties for low time
	    tie[i] = new TimingGameParams(0, 0, c.doubleValue());

	    // Function params for when player does not have low time
	    notLow[i] = new TimingGameParams(0, 0, b.doubleValue());
	}

	// Finally, set these parameters so that the payoff can be
	// calculated using the timing game class
	setParamsWithTie(low, tie, notLow);
    }

}
