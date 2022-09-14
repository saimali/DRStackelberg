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
 * Return a uniformly random game
 */

public class RandomGame extends MatrixGame
{
    // -- Parameters: Random games are parameterized only on the
    // number of players and the number of actions
    private static Parameters.ParamInfo[] rgParam;

    static {
	rgParam = new Parameters.ParamInfo[] {Game.players, Game.actions};
	Global.registerParams(RandomGame.class, rgParam);
    }

    // ----------------------------------------------

    /**
     * Construct a new game
     */

    public RandomGame()
	throws Exception
    {
	super();
    }

    public void initialize()
	throws Exception
    {
	super.initialize();

	parsePlayersActions();

	initMatrix();
    }


    /**
     * None to check
     */
    protected void checkParameters() throws Exception
    {
    }



    /** 
     * Return the help screen
     */

    protected String getGameHelp()
    {
	return "Creates a game with the given number of players " +
	    "with payoffs distributed uniformly at random." +
	    getRangeHelp();
    }

    /** 
     * Fill in the payoffs
     */
    public void doGenerate()
    {
	double low = DEFAULT_LOW;
	double high= DEFAULT_HIGH;

	setDescription("A Game With Uniformly Random Payoffs\n" 
			 + getDescription());
	setName("Random Matrix Game");


	Outcome outcome=new Outcome(getNumPlayers(), getNumActions());
	
	for(outcome.reset(); outcome.hasMoreOutcomes(); outcome.nextOutcome())
	    {
		for(int i=0; i<getNumPlayers(); i++)
		    setPayoff(outcome.getOutcome(), i, 
			      Global.randomDouble(low, high));
	    }
    }
}
