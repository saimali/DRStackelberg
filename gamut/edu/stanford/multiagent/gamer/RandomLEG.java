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
 * structure and a given function.
 *
 */


public class RandomLEG extends LocalEffectGame
{
    boolean randomize;

    // --------------------------------------------------

    // Parameters: The parameters taken by the random LEG
    // are the name of the graph and function classes to be
    // used, the numbers of players, as well as the parameters 
    // required by the particular graph and function classes.
    //

    protected static Parameters.ParamInfo pGraph;
    protected static Parameters.ParamInfo pGraphParams;
    protected static Parameters.ParamInfo pFunc;
    protected static Parameters.ParamInfo pFuncParams;
    protected static Parameters.ParamInfo[] rlegParam;

    static {

	pGraph = new Parameters.ParamInfo("graph", Parameters.ParamInfo.STRING_PARAM, null, null, "the name of the graph class to use");

	pGraphParams = new Parameters.ParamInfo("graph_params", Parameters.ParamInfo.CMDLINE_PARAM, null, null, "parameters to be handed off to the graph, must be enclosed in [].");

	pFunc = new Parameters.ParamInfo("func", Parameters.ParamInfo.STRING_PARAM, null, null, "the name of the function class to use");

	pFuncParams = new Parameters.ParamInfo("func_params", Parameters.ParamInfo.CMDLINE_PARAM, null, null, "parameters to be handed off to the function, must be enclosed in [].");

	rlegParam = new Parameters.ParamInfo[] {Game.players, Game.symActions,
						pGraph, pGraphParams,
						pFunc, pFuncParams};
	Global.registerParams(RandomLEG.class, rlegParam);
    }

    
    // -------------------------------------------------------


    public RandomLEG() 
	throws Exception
    {
	super();
    }



    /**
     * Set parameters for the LEG.  The parameters for the function 
     * and graph will be taken care of later.
     */
    public void setParameters(ParamParser p, boolean randomize)
	throws Exception
    {
	super.setParameters(p, randomize);	
	this.randomize=randomize;
    }



    /**
     * Initialized the random local effect game
     */
    public void initialize()
	throws Exception
    {
	// The LocalEffectGame class takes care of initializing
	// the number of players
	super.initialize();

	// Get number of players and actions
	parsePlayersSameNumberActions();

	// Initialize the graph
	initGraph();

	// The number of actions is equal to the number of
	// nodes in the graph
	if( graph.getNNodes() != getNumActions(0) )
	  throw new Exception("Number of nodes in the graph must be the same as the number of actions!");
	if (!graph.hasSymEdges())
	  throw new Exception("Graph used by LEG must be undirected!");
	if (graph.reflexEdgesOk())
	  throw new Exception("Reflexive edges not allowed in LEG graphs!");
    }



    protected void checkParameters() throws Exception 
    {
      // -- if need to randomize graphs parameters, then must
      // -- be able to set number of nodes
      if(randomize && parameters.isParamSet(pGraph.name))
	if(!Global.isPartOf(Global.GRAPH, getStringParameter(pGraph.name), 
			   "GraphWithNodeParam"))
	  throw new Exception("ERROR: Cannot randomize graph parameters unless graph is an instance of GraphWithNodeParam!");
    }


    // 
    // Randomize parameters not provided by the user
    //
    public void randomizeParameters() {
      	try {

	if(!parameters.setByUser(pGraph.name))
	    parameters.setParameter(pGraph.name, 
				    Global.getRandomClass(Global.GRAPH,
				    "GraphWithNodeParam"));

	if(!parameters.setByUser(pFunc.name))
	    parameters.setParameter(pFunc.name, 
				    Global.getRandomClass(Global.FUNC));

	} catch (Exception e) {
	    Global.handleError(e, "Randomizing Graphs/Function");
	}

	// Deal with the CMDLINE_PARAM
	super.randomizeParameters();
    }


    protected String getGameHelp()
    {
	return "Creates a Local-Effect Game using the specified " +
	    "graph class and specified function class.\n\nPlease note " +
	    "that you should be careful when you choose the graph " +
	    "class and set the graph parameters here.  The graph " +
	    "chosen should be symmetric (i.e. whenever there is an " +
	    "edge from a to b there is also an edge from b to a) and " +
	    "should not have reflexive edges.  Set the parameters for " +
	    "the graph accordingly!";
    }



    /**
     * Initialize a graph with the number of nodes
     * equal to the number of actions.
     */ 
    protected void initGraph()
    {
	// Instantiate a graph of the type selected by the user
	String graphName = parameters.getStringParameter(pGraph.name);
	graph = (ALGraph) Global.getObjectOrDie(graphName,
						Global.GRAPH);
	ParamParser graphParams = 
	    parameters.getParserParameter(pGraphParams.name);

	try {
	  if(randomize)
	      {
		// -- manually set everything if randomizing
		graph.setParameter("nodes", new Long(getNumActions(0)), true);

		// -- try to override reflexivity/symmetry params
		try {graph.setParameter("reflex_ok", Boolean.FALSE, true);}
		catch (Exception e) {}
		
		try {graph.setParameter("sym_edges", Boolean.TRUE, true);}
		catch (Exception e) {}
	      }
	    
	  // -- set other parameters from command line.
	  graph.setParameters(graphParams, randomize);
	  graph.initialize();

	} catch (Exception e) {
	  System.err.println(getHelp());
	  System.err.println(graph.getHelp());
	  e.printStackTrace();
	    Global.handleError(e, "Error initializing graph: ");
	}

	/*
	// Make sure that the graph in use has symmetric edges
	// and does not allow reflexive edges
	if (!graph.hasSymEdges()) {
	    Global.handleError("It is required that the graph used " +
			       "in a Random LEG has symmetric edges.");
	}

	if (graph.reflexEdgesOk()) {
	    Global.handleError("It is required that the graph used " +
			       "in a Random LEG does not allow " +
			       "reflexive edges.");
	}
	*/
    }
	
    



    /** 
     * Generate the graph and for each node and edge of the graph,
     * a function.
     */
    public void doGenerate()
    {
	setDescription("Random Local-Effect Game\n"  + getDescription());
	setName("Random Local-Effect Game");

	// Generate the graph for the game.
	graph.doGenerate();

	setDescription(getDescription() + "\nGraph Params:\n" + graph.getDescription());

	String funcName = parameters.getStringParameter(pFunc.name);
	ParamParser funcParams = parameters.getParserParameter(pFuncParams.name);
  
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
		Function fEdge = (Function) Global.getObjectOrDie(funcName, 
								  Global.FUNC);
		
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
	    }
	}
    }
}
