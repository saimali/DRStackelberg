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

import java.util.NoSuchElementException;

/**
 * The utility class encapsulates the notion of an outcome
 * Action numbering starts at one
 */

public class Outcome
{
    private int nPlayers;
    private int[] nActions;
    private int nOutcomes;

    private int[] actions;
    private boolean hasMore;

    public Outcome(int nPlayers, int[] nActions)
    {
	this.nPlayers = nPlayers;
	this.nActions = nActions;

	nOutcomes = nActions[0];
	for (int i = 1; i < nPlayers; i++) {
	    nOutcomes *= nActions[i];
	}

	actions = new int[nPlayers];
	hasMore=false;

	reset();
    }

    public int[] getNumActions()
    {
	return nActions;
    }

    public int getNumPlayers()
    {
	return nPlayers;
    }

    public int getNumOutcomes()
    {
	return nOutcomes;
    }

    public void reset()
    {
	// Technically speaking, we do not always have more here, but
	// need to set this to true in all cases to handle the case in
	// which there is only one outcome.  Will be corrected for the
	// next time nextOutcome is called.
	hasMore=true;

	for(int i=0; i<nPlayers; i++)
	    actions[i]=1;
    }

    public void reset(int[] actions) throws ArrayIndexOutOfBoundsException
    {
	hasMore=false;
	for(int i=0; i<nPlayers;i++)
	    {
		if(actions[i]<1 || actions[i]>nActions[i])
		    throw new ArrayIndexOutOfBoundsException("Action out of Bounds");
		this.actions[i]=actions[i];

		if(actions[i] <nActions[i])
		    hasMore=true;
	    }
    }


    public int[] getOutcome()
    {
	return actions;
    }

    public boolean hasMoreOutcomes()
    {
	return hasMore;
    }


    /**
     * In a two by two matrix game, the outcomes are looped over
     * in the order top left, bottom left, top right, bottom right.
     * Can extend this idea of first player's actions being looped
     * through quickly and repeatedly, and last player's actions
     * being looped through slowly and only once to figure out 
     * ordering for games of other sizes.
     */
    public void nextOutcome() throws NoSuchElementException
    {
	if(!hasMore) throw new NoSuchElementException("No More Outcomes");
	
	for(int i=0; i<nPlayers; i++)
	    {
		if(actions[i] < nActions[i])
		    {
			actions[i]++;
			break;
		    }
		else
		    {
			actions[i] = 1;
			if( i == nPlayers - 1 )
			    hasMore = false;
		    }
	    }
    }

    
    public String toString()
    {
	StringBuffer buf = new StringBuffer();
	buf.append("[");
	for(int i=0; i<nPlayers; i++)
	    buf.append(actions[i]).append( (i < nPlayers -1 ? "  " : "]") );

	return buf.toString();
    }


    public static void main(String[] args)
    {
	int[] nActions={4,2,3};

	Outcome out = new Outcome(3, nActions);
	
	for(out.reset(); out.hasMoreOutcomes(); out.nextOutcome())
	    {
		System.out.println(out);
	    }
	//out.nextOutcome();
    }
}

