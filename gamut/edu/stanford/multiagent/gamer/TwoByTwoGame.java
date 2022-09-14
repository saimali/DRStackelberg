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

import java.util.Arrays;

/**
 * Return a 2x2 game of a given type from Rapoport's distribution.
 */

public class TwoByTwoGame extends MatrixGame
{
    // -- Parameters: Takes as a parameter the game id number
    // according to Rappoport's classification.

    private static Parameters.ParamInfo pType;
    private static Parameters.ParamInfo[] ttParam;

    static {
	pType = new Parameters.ParamInfo("type", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(85), "type of the 2x2 game in Rappoport's classification, in [1,85]");

	ttParam = new Parameters.ParamInfo[] {pType};
	Global.registerParams(TwoByTwoGame.class, ttParam);
    }

    // ----------------------------------------------

    /**
     * Construct a new game
     */

    public TwoByTwoGame()
	throws Exception
    {
	super();
    }

    public void initialize()
	throws Exception
    {
	super.initialize();

	setNumPlayers(2);
	setNumActions(new int[] {2,2});

	initMatrix();
    }


    /**
     * Checks that parameters are ok
     */
    protected void checkParameters() throws Exception
    {
	if ((getLongParameter(pType.name) < 1) ||
	    (getLongParameter(pType.name) > 85))
	    throw new Exception("parameter type out of range");
    }

    /**
     * Randomizes type
     */
    public void randomizeParameters() 
    {
	parameters.randomizeParameter(parameters.getParamIndex(pType.name));
    }


    /** 
     * Returns the help screen
     */
    protected String getGameHelp()
    {
	return "Creates a game of two actions and two players " +
	    "of a given type according to Rappoport's classification." +
	    getRangeHelp();
    }



    /** 
     * Fills in the payoffs
     */
    public void doGenerate()
    {
	double low = DEFAULT_LOW;
	double high = DEFAULT_HIGH;

	int type = (int)getLongParameter(pType.name);

	setDescription("A Two By Two Game of type " + type + "\n"
			 + getDescription());
	setName("Two by Two Game of type " + type);

	// -- make random payoff vectors
	double[][] payoffs = new double[2][5];
	
	for(int i=0; i<2; i++)
	    {
		for(int j=1; j<=4; j++)
		    payoffs[i][j]=Global.randomDouble(low, high);

		Arrays.sort(payoffs[i]);		
	    }


	Outcome outcome=new Outcome(getNumPlayers(), getNumActions());
	
	for(outcome.reset(); outcome.hasMoreOutcomes(); outcome.nextOutcome())
	    {
		for(int i=0; i<getNumPlayers(); i++)
		    {
			int[] actions=outcome.getOutcome();
			int row=actions[0] - 1;
			int col=actions[1] - 1;
			int ord=allGames[type-1][i][row][col];
			
			setPayoff(outcome.getOutcome(), i,
				  payoffs[i][ord]);
		    }
		
	    }
    }


    /**
     * All of Rappoport's games in one table!
     * indices are: game, player, row, col
     * entries: order of the payoff
     */

    private static final int[][][][] allGames  =
    {
	{ { {4, 3}, {2, 1} }, { {4, 3}, {2, 1} } },
	{ { {4, 3}, {1, 2} }, { {4, 3}, {2, 1} } },
	{ { {4, 3}, {2, 1} }, { {4, 2}, {3, 1} } },
	{ { {4, 3}, {1, 2} }, { {4, 2}, {3, 1} } },
	{ { {4, 3}, {1, 2} }, { {4, 1}, {3, 2} } },
	{ { {4, 2}, {3, 1} }, { {4, 3}, {2, 1} } },
	{ { {3, 4}, {2, 1} }, { {3, 2}, {4, 1} } },
	{ { {3, 4}, {1, 2} }, { {3, 2}, {4, 1} } },
	{ { {3, 4}, {1, 2} }, { {3, 1}, {4, 2} } },
	{ { {2, 4}, {1, 3} }, { {3, 2}, {4, 1} } },
	{ { {2, 4}, {1, 3} }, { {3, 1}, {4, 2} } },
	{ { {2, 4}, {1, 3} }, { {2, 1}, {4, 3} } },
	{ { {3, 4}, {2, 1} }, { {4, 2}, {3, 1} } },
	{ { {3, 4}, {1, 2} }, { {4, 2}, {3, 1} } },
	{ { {3, 4}, {2, 1} }, { {4, 1}, {3, 2} } },
	{ { {3, 4}, {1, 2} }, { {4, 1}, {3, 2} } },
	{ { {2, 4}, {1, 3} }, { {4, 2}, {3, 1} } },
	{ { {2, 4}, {1, 3} }, { {4, 1}, {3, 2} } },
	{ { {3, 4}, {1, 2} }, { {4, 3}, {2, 1} } },
	{ { {3, 4}, {2, 1} }, { {4, 3}, {2, 1} } },
	{ { {2, 4}, {1, 3} }, { {4, 3}, {2, 1} } },
	{ { {4, 3}, {2, 1} }, { {4, 3}, {1, 2} } },
	{ { {4, 3}, {1, 2} }, { {4, 3}, {1, 2} } },
	{ { {4, 3}, {2, 1} }, { {4, 2}, {1, 3} } },
	{ { {4, 3}, {1, 2} }, { {4, 2}, {1, 3} } },
	{ { {4, 2}, {3, 1} }, { {4, 3}, {1, 2} } },
	{ { {4, 2}, {3, 1} }, { {4, 2}, {1, 3} } },
	{ { {4, 3}, {2, 1} }, { {4, 1}, {2, 3} } },
	{ { {4, 3}, {1, 2} }, { {4, 1}, {2, 3} } },
	{ { {4, 2}, {3, 1} }, { {4, 1}, {2, 3} } },
	{ { {3, 2}, {1, 4} }, { {4, 2}, {3, 1} } },
	{ { {3, 2}, {1, 4} }, { {4, 1}, {3, 2} } },
	{ { {3, 1}, {2, 4} }, { {4, 2}, {3, 1} } },
	{ { {3, 1}, {2, 4} }, { {4, 1}, {3, 2} } },
	{ { {2, 3}, {1, 4} }, { {4, 2}, {3, 1} } },
	{ { {2, 3}, {1, 4} }, { {4, 1}, {3, 2} } },
	{ { {3, 2}, {1, 4} }, { {4, 3}, {2, 1} } },
	{ { {3, 1}, {2, 4} }, { {4, 3}, {2, 1} } },
	{ { {2, 3}, {1, 4} }, { {4, 3}, {2, 1} } },
	{ { {3, 4}, {2, 1} }, { {4, 1}, {2, 3} } },
	{ { {3, 4}, {1, 2} }, { {4, 1}, {2, 3} } },
	{ { {3, 4}, {2, 1} }, { {3, 1}, {2, 4} } },
	{ { {3, 4}, {1, 2} }, { {3, 1}, {2, 4} } },
	{ { {2, 4}, {1, 3} }, { {4, 1}, {2, 3} } },
	{ { {3, 4}, {2, 1} }, { {2, 1}, {3, 4} } },
	{ { {3, 4}, {1, 2} }, { {2, 1}, {3, 4} } },
	{ { {2, 4}, {1, 3} }, { {3, 1}, {2, 4} } },
	{ { {2, 4}, {1, 3} }, { {2, 1}, {3, 4} } },
	{ { {3, 4}, {2, 1} }, { {4, 3}, {1, 2} } },
	{ { {3, 4}, {1, 2} }, { {4, 3}, {1, 2} } },
	{ { {3, 4}, {2, 1} }, { {4, 2}, {1, 3} } },
	{ { {3, 4}, {1, 2} }, { {4, 2}, {1, 3} } },
	{ { {3, 4}, {2, 1} }, { {3, 2}, {1, 4} } },
	{ { {3, 4}, {1, 2} }, { {3, 2}, {1, 4} } },
	{ { {2, 4}, {1, 3} }, { {4, 3}, {1, 2} } },
	{ { {2, 4}, {1, 3} }, { {4, 2}, {1, 3} } },
	{ { {2, 4}, {1, 3} }, { {3, 2}, {1, 4} } },
	{ { {4, 2}, {1, 3} }, { {4, 3}, {1, 2} } },
	{ { {4, 2}, {1, 3} }, { {4, 2}, {1, 3} } },
	{ { {4, 2}, {1, 3} }, { {4, 1}, {2, 3} } },
	{ { {4, 1}, {3, 2} }, { {4, 3}, {1, 2} } },
	{ { {4, 1}, {3, 2} }, { {4, 2}, {1, 3} } },
	{ { {4, 1}, {2, 3} }, { {4, 2}, {1, 3} } },
	{ { {3, 2}, {1, 4} }, { {4, 1}, {2, 3} } },
	{ { {2, 3}, {1, 4} }, { {4, 1}, {2, 3} } },
	{ { {3, 2}, {4, 1} }, { {3, 4}, {2, 1} } },
	{ { {2, 3}, {4, 1} }, { {3, 4}, {2, 1} } },
	{ { {2, 3}, {4, 1} }, { {2, 4}, {3, 1} } },
	{ { {2, 4}, {3, 1} }, { {2, 3}, {4, 1} } },
	{ { {3, 2}, {4, 1} }, { {4, 1}, {2, 3} } },
	{ { {3, 2}, {4, 1} }, { {3, 1}, {2, 4} } },
	{ { {3, 2}, {4, 1} }, { {2, 1}, {3, 4} } },
	{ { {2, 4}, {3, 1} }, { {4, 1}, {2, 3} } },
	{ { {2, 3}, {4, 1} }, { {4, 1}, {2, 3} } },
	{ { {2, 4}, {3, 1} }, { {3, 1}, {2, 4} } },
	{ { {2, 3}, {4, 1} }, { {3, 1}, {2, 4} } },
	{ { {2, 4}, {3, 1} }, { {2, 1}, {3, 4} } },
	{ { {2, 3}, {4, 1} }, { {2, 1}, {3, 4} } },
	{ { {2, 1}, {2, 1} }, { {2, 2}, {1, 1} } },
	{ { {1, 1}, {1, 1} }, { {4, 3}, {2, 1} } },
	{ { {2, 3}, {2, 1} }, { {2, 1}, {2, 3} } },
	{ { {3, 4}, {2, 1} }, { {4, 1}, {2, 2} } },
	{ { {3, 1}, {1, 2} }, { {3, 1}, {1, 2} } },
	{ { {3, 2}, {1, 3} }, { {1, 2}, {3, 1} } },
	{ { {2, 1}, {1, 2} }, { {2, 1}, {1, 2} } }
    };
}
