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
 * Return a version of the Battle of the Sexes game
 */ 


public class BattleOfTheSexes extends MatrixGame
{

    // Parameters: Battle of the Sexes does not take parameters.

    private static Parameters.ParamInfo[] bosParam;

    static {
	bosParam = new Parameters.ParamInfo[] {};
	Global.registerParams(BattleOfTheSexes.class, bosParam);
    }


    // ----------------------------------------------


    public BattleOfTheSexes() 
	throws Exception
    {
	super();
    }



    public void initialize()
	throws Exception
    {
	super.initialize();

	setNumPlayers(2);
	
	int numActions[];
	numActions = new int[2];
	numActions[0] = numActions[1] = 2;
	setNumActions(numActions);

	initMatrix();
    }



    protected void checkParameters() throws Exception 
    {
    }


    public void randomizeParameters() 
    {
    }


    protected String getGameHelp()
    {
	return "Creates a 2x2 Battle of the Sexes Game" +
	    getRangeHelp();
    }


    public void doGenerate()
    {
	double low = DEFAULT_LOW;
	double high = DEFAULT_HIGH;

	setDescription("Battle of the Sexes\n" 
			 + getDescription());
	setName("Battle of the Sexes");

	SortedAndRandomSet payoffValues = new SortedAndRandomSet();

	// Generate three payoff values in the range from
	// payoff_low to payoff_high.  Make sure that the
	// three payoffs are distinct.
	for (int i = 0; i < 3; i++) {
	    Double randomPayoffAsDouble;
	    do {
		double randomPayoff = Global.randomDouble(low, high);
		randomPayoffAsDouble = new Double(randomPayoff);
	    } while (!(payoffValues.add(randomPayoffAsDouble)));
	}

	Double c = (Double) payoffValues.removeSmallest();
	Double a = (Double) payoffValues.removeRandom();
	Double b = (Double) payoffValues.removeRandom();

	Outcome outcome=new Outcome(getNumPlayers(), getNumActions());

	outcome.reset();
	setPayoff(outcome.getOutcome(), 0, a.doubleValue());
	setPayoff(outcome.getOutcome(), 1, b.doubleValue());

	outcome.nextOutcome();
	setPayoff(outcome.getOutcome(), 0, c.doubleValue());
	setPayoff(outcome.getOutcome(), 1, c.doubleValue());

	outcome.nextOutcome();
	setPayoff(outcome.getOutcome(), 0, c.doubleValue());
	setPayoff(outcome.getOutcome(), 1, c.doubleValue());

	outcome.nextOutcome();
	setPayoff(outcome.getOutcome(), 0, b.doubleValue());
	setPayoff(outcome.getOutcome(), 1, a.doubleValue());
    }
}
