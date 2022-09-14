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
 * Return an instance of the two player Prisoner's Dilemma
 */


public class PrisonersDilemma extends MatrixGame
{
    // Parameters: no parameters
    private static Parameters.ParamInfo[] pdParam;

    static {
	pdParam = new Parameters.ParamInfo[] {};
	Global.registerParams(PrisonersDilemma.class, pdParam);
    }


    // ----------------------------------------------


    public PrisonersDilemma() 
	throws Exception
    {
	super();
    }



    public void initialize()
	throws Exception
    {
	super.initialize();

	// The simple prisoner's dilemma game will always have
	// 2 players with 2 actions each.
	setNumPlayers(2);
	
	int numActions[];
	numActions = new int[2];
	numActions[0] = numActions[1] = 2;
	setNumActions(numActions);

	initMatrix();
    }



    //
    // None to check
    //
    protected void checkParameters() throws Exception 
    {
    }


    //
    // None to randomize
    //
    public void randomizeParameters() 
    {
    }


    protected String getGameHelp()
    {
	return "Creates a 2x2 Prisoner's Dilemma" + getRangeHelp();
    }



    public void doGenerate()
    {
	double low = DEFAULT_LOW;
	double high = DEFAULT_HIGH;

	setDescription("Prisoner's Dilemma\n" 
			 + getDescription());
	setName("Prisoner's Dilemma");

	SortedAndRandomSet payoffValues = new SortedAndRandomSet();

	Double t, r, p, s;

	// Use rejection sampling to make sure the values of the 
	// four random payoffs chosen are such that r > (s+t) / 2
	// where t is the largest payoff, r is second largest,
	// and s is smallest

	do {

	    // Generate four payoff values in the range from
	    // payoff_low to payoff_high.  Make sure that the
	    // four values are distinct.
	    for (int i = 0; i < 4; i++) {
		Double randomPayoffAsDouble;
		do {
		    double randomPayoff = Global.randomDouble(low, high);
		    randomPayoffAsDouble = new Double(randomPayoff);
		} while (!(payoffValues.add(randomPayoffAsDouble)));
	    }

	    t = (Double) payoffValues.removeLargest();
	    r = (Double) payoffValues.removeLargest();
	    p = (Double) payoffValues.removeLargest();
	    s = (Double) payoffValues.removeLargest();

	} while (r.floatValue() <= 
		 (s.floatValue() + t.floatValue()) / 2.0);


	Outcome outcome=new Outcome(getNumPlayers(), getNumActions());

	outcome.reset();
	setPayoff(outcome.getOutcome(), 0, r.doubleValue());
	setPayoff(outcome.getOutcome(), 1, r.doubleValue());

	outcome.nextOutcome();
	setPayoff(outcome.getOutcome(), 0, t.doubleValue());
	setPayoff(outcome.getOutcome(), 1, s.doubleValue());

	outcome.nextOutcome();
	setPayoff(outcome.getOutcome(), 0, s.doubleValue());
	setPayoff(outcome.getOutcome(), 1, t.doubleValue());

	outcome.nextOutcome();
	setPayoff(outcome.getOutcome(), 0, p.doubleValue());
	setPayoff(outcome.getOutcome(), 1, p.doubleValue());
    }

}
