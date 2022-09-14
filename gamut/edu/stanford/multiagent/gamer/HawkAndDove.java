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
 * Return a version of the two player Hawk and Dove game.
 * 
 * Note that we are using a narrow definition of the hawk
 * and dove game that only returns games of the form
 *
 *          B,B   C,A
 *          A,C   D,D
 *
 * with A > B > C > D rather than the more broad definition
 * proposed in some papers which allows Prisoners Dilemma and
 * Chicken Games to count as Hawk and Dove.
 *
 */


public class HawkAndDove extends MatrixGame
{
    // Parameters: none
    private static Parameters.ParamInfo[] hawkParam;

    static {
	hawkParam = new Parameters.ParamInfo[] {};
	Global.registerParams(HawkAndDove.class, hawkParam);
    }


    // ----------------------------------------------


    public HawkAndDove() 
	throws Exception
    {
	super();
    }


    public void initialize()
	throws Exception
    {
	super.initialize();

	// The simple hawk and dove game will always have
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
	return "Creates a 2x2 Hawk and Dove.\n\nUses the more narrow " +
	    "definition of Hawk and Dove which does not, for example, " +
	    "allow games which would be classified as Prisoners " +
	    "Dilemmas or Chicken Games to qualify as Hawk and Dove." +
	    getRangeHelp();
    }


    public void doGenerate()
    {
	double low = DEFAULT_LOW;
	double high = DEFAULT_HIGH;

	setDescription("Two by Two Hawk and Dove Game\n" 
			 + getDescription());
	setName("Hawk and Dove");

	SortedAndRandomSet payoffValues = new SortedAndRandomSet();

	Double a, b, c, d;

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

	a = (Double) payoffValues.removeLargest();
	b = (Double) payoffValues.removeLargest();
	c = (Double) payoffValues.removeLargest();
	d = (Double) payoffValues.removeLargest();

	Outcome outcome=new Outcome(getNumPlayers(), getNumActions());

	outcome.reset();
	setPayoff(outcome.getOutcome(), 0, b.doubleValue());
	setPayoff(outcome.getOutcome(), 1, b.doubleValue());

	outcome.nextOutcome();
	setPayoff(outcome.getOutcome(), 0, a.doubleValue());
	setPayoff(outcome.getOutcome(), 1, d.doubleValue());

	outcome.nextOutcome();
	setPayoff(outcome.getOutcome(), 0, d.doubleValue());
	setPayoff(outcome.getOutcome(), 1, a.doubleValue());

	outcome.nextOutcome();
	setPayoff(outcome.getOutcome(), 0, c.doubleValue());
	setPayoff(outcome.getOutcome(), 1, c.doubleValue());
    }

}
