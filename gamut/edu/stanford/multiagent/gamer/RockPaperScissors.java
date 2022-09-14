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
 * Return an instance of Rock, Paper, Scissors
 */ 


public class RockPaperScissors extends ZeroSumGame
{

    // Parameters: none
    private static Parameters.ParamInfo[] rpsParam;

    static {
	rpsParam = new Parameters.ParamInfo[] {};
	Global.registerParams(RockPaperScissors.class, rpsParam);
    }


    // ----------------------------------------------


    public RockPaperScissors() 
	throws Exception
    {
	super();
    }



    public void initialize()
	throws Exception
    {
	super.initialize();

	// There will always be two players in this game, and they
	// will always have three actions each.
	setNumPlayers(2);
	
	int numActions[];
	numActions = new int[2];
	numActions[0] = numActions[1] = 3;
	setNumActions(numActions);

	initPayoffs(3, 3);
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
	return "Creates an instance of the Rock, Paper, Scissors Game." +
	    getRangeHelp();
    }


    public void doGenerate()
    {
	double a = DEFAULT_HIGH;

	setDescription("Rock, Paper, Scissors\n" + getDescription());
	setName("Rock, Paper, Scissors");

	Outcome outcome=new Outcome(getNumPlayers(), getNumActions());

	// First column in matrix

	outcome.reset();
	setPayoff(outcome.getOutcome(), 0);

	outcome.nextOutcome();
	setPayoff(outcome.getOutcome(), -a);

	outcome.nextOutcome();
	setPayoff(outcome.getOutcome(), a);

	// Second column in matrix

	outcome.nextOutcome();
	setPayoff(outcome.getOutcome(), a);

	outcome.nextOutcome();
	setPayoff(outcome.getOutcome(), 0);

	outcome.nextOutcome();
	setPayoff(outcome.getOutcome(), -a);

	// Third column in matrix

	outcome.nextOutcome();
	setPayoff(outcome.getOutcome(), -a);

	outcome.nextOutcome();
	setPayoff(outcome.getOutcome(), a);

	outcome.nextOutcome();
	setPayoff(outcome.getOutcome(), 0);
    }

}
