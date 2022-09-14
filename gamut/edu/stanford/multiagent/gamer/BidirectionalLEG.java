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
import edu.stanford.multiagent.gamer.graphs.*;
import edu.stanford.multiagent.gamer.functions.*;

/**
 * Generate a Local Effect Game with a given graphical 
 * structure and a given function structure such that
 * the function stored on an edge a to b is the same as
 * the function stored on an edge b to a.
 *
 * Very similar to RandomLEG except for the generation
 * function.
 */


public class BidirectionalLEG extends RandomLEG
{

    // --------------------------------------------------
    // Parameters: Parameters carry over from RandomLEG
    // and from the graph and function being used.  No new
    // parameters are introduced here.
    //

    static {
	Global.registerParams(BidirectionalLEG.class, rlegParam);
    }
    
    // -------------------------------------------------------


    public BidirectionalLEG() 
	throws Exception
    {
	super();
    }



    protected String getGameHelp()
    {
	return "Creates a Bidirectional Local-Effect Game using the " +
	    "specified graph class and specified function class.\n\n" +
	    "A Bidirectional Local-Effect Game is a LEG with a graphical " +
	    "structure in which every edge from b to a has the same " +
	    "local-effect function as the edge from a to b.\n\n" +
	    "Please note " +
	    "that you should be careful when you choose the graph " +
	    "class and set the graph parameters here.  The graph " +
	    "chosen should be symmetric (i.e. whenever there is an " +
	    "edge from a to b there is also an edge from b to a) and " +
	    "should not have reflexive edges.  (Each node will have a " +
	    "local effect on itself, but this is handled outside of the " +
	    "graph.) Set the parameters for the graph accordingly!";
    }




    /** 
     * Generate the graph and for each node and edge of the graph,
     * a function, making sure that functions are the same on every
     * edge from a to b as they are on the edge from b to a.
     */
    public void doGenerate()
    {
	setDescription("Bidirectional Local-Effect Game\n"  + 
		       getDescription());
	setName("Bidirectional Local-Effect Game");

	// Generate the graph for the game.
	graph.doGenerate();

	setDescription(getDescription() + "\nGraph Params:\n" + graph.getDescription());

	String funcName = parameters.getStringParameter("func");
	ParamParser funcParams = parameters.getParserParameter("func_params");

	// Add a function of the appropriate type to each edge
	// of the graph and to each node of the graph
	for (int i = 0; i < graph.getNNodes(); i++) {
	    Function fNode = (Function) Global.getObjectOrDie(funcName,
							      Global.FUNC);
	    try {
		fNode.setDomain(0, getNumPlayers());
		fNode.setParameters(funcParams, randomize);
		fNode.initialize();
		fNode.doGenerate();
	    } catch (Exception e) {
	      System.err.println(getHelp());
	      System.err.println(fNode.getHelp());
	      Global.handleError(e, "Error initializing function");
	    }

	    graph.setNodeData(i, fNode);

	    Iterator edgeIter = graph.getEdges(i);
	    while (edgeIter.hasNext()) {
		Edge edge = (Edge) edgeIter.next();		

		// Make sure we only generate one function for each
		// edge pair and use it for both -- shouldn't have any
		// reflexive edges in the graph, but handle it anyway
		if (i <= edge.getDest()) {

		    Function fEdge = (Function) 
			Global.getObjectOrDie(funcName, Global.FUNC);
		
		    try {
			fEdge.setDomain(0, getNumPlayers());
			fEdge.setParameters(funcParams, randomize);
			fEdge.initialize();
			fEdge.doGenerate();
		    } catch (Exception e) {
			System.err.println(getHelp());
			System.err.println(fEdge.getHelp());
			Global.handleError(e, "Error initializing function");
		    }
		    
		    edge.setData(fEdge);

		    if (i != edge.getDest()) {
			Edge reverse = graph.getEdge(edge.getDest(), i);
			reverse.setData(fEdge);
		    }
		}
	    }
	}
    }
}
