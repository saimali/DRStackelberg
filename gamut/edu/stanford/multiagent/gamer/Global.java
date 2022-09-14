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
import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.jar.*;
import edu.stanford.multiagent.gamer.graphs.*;
import edu.stanford.multiagent.gamer.functions.*;


/**
 * A Class to hold global parameters, utilities, etc.
 */

public class Global
{

    // == NOTE: All static members must be initialized somewhere 
    // == NOTE: In the main class, or here.

    public static String VERSION_STRING = "GAMUT v1.0.1";

    // -- Global Random Number Generator
    public static Random rand;
    public static long randSeed;

    // -- Constants used for creating/randomizing objects
    public static final int GAME = 0;
    public static final int GRAPH = 1;
    public static final int FUNC = 2;
    public static final int OUTPUT = 3;

    // Constant directory names which can be passed to 
    // getObjectOrDie
    private static final String[] DIRS={"gamer", "gamer.graphs",
					"gamer.functions", "gamer"};


    // -- Constant file names that will store lists of objects
    // -- Used for randomization, etc
    private static final String[] FILES = {"games.txt",
					   "graphs.txt",
					   "functions.txt",
					   "outputters.txt"};

    private static final Class[] BASES = {Game.class, Graph.class, Function.class, GameOutput.class};
    
    // -- Properties To hold those
    private static Properties[] props  = new Properties[4];
    
    
    // -- Global Parameters
    public static Parameters params;

  // -- Original command line
  public static String[] gArgs;

    /**
     * Convert array of strings into array of ints
     */
    public static int[] parseIntArray(Vector v)
	throws NumberFormatException
    {
	int n=v.size();
	int[] nInts=new int[n];

	for(int i=0; i<n; i++)
	    nInts[i]=Integer.parseInt((String)v.get(i));

	return nInts;
    }



    //
    // Convert array of strings into array of doubles
    //
    public static double[] parseDoubleArray(Vector v)
	throws NumberFormatException
    {
	int n=v.size();
	double[] nDoubles=new double[n];

	for(int i=0; i<n; i++)
	    nDoubles[i]=Double.parseDouble((String)v.get(i));

	return nDoubles;
    }



    //
    // Return a random long value in the range from low to high
    // inclusively
    //
    public static long randomLong(long low, long high) {
	// For some reason, % yields negative numbers sometimes,
	// so have to remember to take the absolute value
	long nextlong = Global.rand.nextLong();
	return (low + (Math.abs(nextlong) % (high-low+1)));
    }

    // -- same for int
    public static int randomInt(int low, int high) {
	// For some reason, % yields negative numbers sometimes,
	// so have to remember to take the absolute value
	int next = Global.rand.nextInt();
	return (low + (Math.abs(next) % (high-low+1)));
    }

    // -- Same as above but for double
    public static double randomDouble(double low, double high) {
	double nd = Global.rand.nextDouble();
	return low + nd * (high-low);
    }

    public static double randomDouble()
    {
	return Global.rand.nextDouble();
    }

    //
    // Return false or true with equal likeliness
    //
    public static boolean randomBoolean() {
	long randomlong = Global.rand.nextLong();
	if ((Math.abs(randomlong) % 2) == 0) 
	    return false;
	return true;
    }



    //
    // Recursively compute factorial.  Used by NChooseM() function.
    //
    public static long factorial(long x) 
    {
	if (x <= 1)
	    return 1;
	else
	    return (x * factorial(x-1));
    }




    // 
    // Return n choose m
    //
    public static long NChooseM(long n, long m)
	throws Exception
    {
	if (n < m)
	    throw new Exception("In NChooseM, n < m.");

	return (Global.factorial(n) / 
		(Global.factorial(m) * Global.factorial(n-m)));
    }



    //
    // Return the maximum (double, int, long) value
    //

    public static double max(double x, double y)
    { 
	if (x > y) return x;
	else return y;
    }

    public static int max(int x, int y)
    { 
	if (x > y) return x;
	else return y;
    }

    public static long max(long x, long y)
    { 
	if (x > y) return x;
	else return y;
    }




    //
    // Return the minimum (double, int, long) value
    //

    public static double min(double x, double y)
    { 
	if (x < y) return x;
	else return y;
    }

    public static int min(int x, int y)
    { 
	if (x < y) return x;
	else return y;
    }

    public static long min(long x, long y)
    { 
	if (x < y) return x;
	else return y;
    }
		       


    // 
    // Prints out an error message and exception stack trace
    // and then dies.  Separating this into its own function
    // so that if we decide to handle errors differently, it
    // will be easy to change
    //
    public static void handleError(Exception e, String s)
    {
	System.err.println();
	System.err.println("FATAL ERROR: " + s);
	System.err.println(e.toString());
	System.err.println();
	System.exit(1);
    }



    //
    // Same but with no explicit Exception
    //
    public static void handleError(String s)
    { 
	System.err.println();
	System.err.println("FATAL ERROR: " + s);

	System.err.println();
	System.exit(1);
    }



    // 
    // Instantiate a class given its name and the name of
    // the directory
    //
    public static Object getObjectOrDie(String name, int type)
    {
	try {
	    // -- First, make sure it's ground
	    if(isKnown(type, name) && !isGround(type, name))
		name = getRandomClass(type, name);

	    // -- if it has parameters, then try to parse them 

	    ParamParser pars=null;

	    if ( hasPresets(type, name) )
	      {
		// -- First, remove []
		String val = getPresetParams(type, name);

		val=val.substring(1,val.length()-1).trim();
		String[] tmp = val.split("\\p{Space}+");
		// -- name is now the first one there
		name = tmp[0];
		String[] cmdLine=new String[tmp.length-1];
		for(int i=0; i<cmdLine.length; i++)
		  cmdLine[i]=tmp[i+1];
		pars=new ParamParser(cmdLine);
	      }


	    // -- Then try to instantiate it;

	    Class cl=Class.forName("edu.stanford.multiagent." + DIRS[type] +
				   "." + name);
	    Object obj=cl.newInstance();

	    // -- this should be a parameterized object now,
	    // -- i.e. not an output, so can do parameters
	    if(pars!=null)
	      ((ParameterizedObject)obj).setParameters(pars, false);

	    // -- If it's not known, we'll add it, unless we're sealed in jar
	    if(!runningFromJar && !isKnown(type, name))
		{
		    if(!BASES[type].isInstance(obj))
			throw new Exception(name  +  " has wrong class");

		    props[type].setProperty(name, ""); // -- it's ground
		    props[type].store(new FileOutputStream(FILES[type]),
				      VERSION_STRING);				      
		}

	    return obj;
	} catch (ClassNotFoundException e) {
	    System.err.println("ERROR: " + name + 
			       " is not a recognized class in " + DIRS[type]);
	    String help=getHelp() +"\n\n" + "Known classes are:\n" + getClassList(type);
	    System.err.println(help);
	    System.exit(1);
	} catch (Exception e) {
	    System.err.println("ERROR Instantiating " + name);
	    System.err.println("Specified class could not be instantiated.");
	    System.err.println (getHelp() + "\n\n" + "Known classes are:\n" + getClassList(type));;
	    System.exit(1);
	}

	return null;
    }




    //
    // Return the generic help screen
    //
    public static String getHelp()
    {
	StringBuffer buff=new StringBuffer();

	buff.append("\n" + VERSION_STRING + "\n");
	buff.append("Eugene Nudelman, Jennifer Wortman, " +
		    "Kevin Leyton-Brown, Yoav Shoham\n" +
		    "Stanford University\n");
	buff.append("================================================" +
		    "==================\n\n");

	buff.append("Global Parameters:\n");
	for(int i=0; i< Main.globalParamInfo.length; i++)
	    buff.append("-" + Main.globalParamInfo[i].name + ":\t" 
			+ Main.globalParamInfo[i].help + "\n");

	return buff.toString();
    }



    // 
    // Found this wrapping function at 
    // http://www.reed.edu/~mcphailb/applets/other/src
    //                          /shakespeare/IOStream.java
    // It is useful for printing out help screens in a more
    // readable format.
    // 
    public static String wrap(String s, int width) {
	StringBuffer text = new StringBuffer();
	StringBuffer line = new StringBuffer();
	StringBuffer word = new StringBuffer();
	
	for (int i=0, li = 0; i < s.length(); i++, li++) {
	    switch (s.charAt(i)) {
	    case ' ':
		line.append(word + " ");
		word = new StringBuffer();
		break;
	    case '\n':
		line.append(word);
		text.append(line + "\n");
		word = new StringBuffer();
		line = new StringBuffer();
		li = 0;
		break;
	    case '\t':
		line.append(word + "    ");
		word = new StringBuffer();
		break;
	    default:
		word.append(s.charAt(i));
		break;
	    }
	    if (li > width) {
		text.append(line + "\n");
		line = new StringBuffer();
		li = 0;
	    }
	}
	if (!line.toString().endsWith((word + "\n").toString())) {
	    line.append(word);
	    text.append(line);
	}
	
	return text.toString();
    }




    // --------------------------------------------------------


    private static HashMap paramRegistry=new HashMap();

    /**
     *Function must be called by every class to register its parameters
     */
    public static final void registerParams(Class cl, Parameters.ParamInfo[] pi)
    {
	paramRegistry.put(cl, pi);
    }

    /**
     *Returns the parameter info array for a given class name
     */
    public static final Parameters.ParamInfo[] getClassParamInfo(Class cl)
	throws Exception
    {
	if(!paramRegistry.containsKey(cl))
	    throw new Exception("No parameters found for class: " + cl.getName());
	return (Parameters.ParamInfo[])paramRegistry.get(cl);
    }

    public static final Parameters.ParamInfo[] getClassParamInfo(String name)
	throws Exception
    {
	Class cl=Class.forName(name);
	return getClassParamInfo(cl);
    }



    // --Deal with function/graph randomization
    
    /**
     * Check whether name corresponds to a ground classs
     */
    public static boolean isGround(int type, String name)
    {
        String val=props[type].getProperty(name);
	if(val==null)
	    return false;

	return (val.length()==0) || (val.startsWith("[") && val.endsWith("]"));
    }

  /**
   *Check whether has preset Params
   */
  public static boolean hasPresets(int type, String name)
  {
    String val=props[type].getProperty(name);
    if(val==null)
      return false;

    return (val.startsWith("[") && val.endsWith("]"));
  }

  /**
   *Get the preset parameter values
   */
  public static String getPresetParams(int type, String name)
  {
    if(hasPresets(type, name))
      return props[type].getProperty(name);
    
    return null;
  }

    /**
     * Check whether it's known
     */
    public static boolean isKnown(int type, String name)
    {
	return props[type].getProperty(name)!=null;
    }

    /**
     * Construct a list of ground classes that are subsets of a given typ
     * Puts them into v
     */
    public static void getGroundClasses(int type, String name, Set v)
    {
	if(!isKnown(type, name))
	    return;

	if(isGround(type, name))
	    {
		// -- ground class
		v.add(name.trim());
		return;
	    }

	String val=props[type].getProperty(name);

	// -- Otherwise it better be a comma separated list
	StringTokenizer st=new StringTokenizer(val,", ");
	while(st.hasMoreTokens())
	    getGroundClasses(type, st.nextToken().trim(), v);
    }

    public static void getGroundClasses(int type, Set v)
    {
	Enumeration e = props[type].propertyNames();
	while(e.hasMoreElements())
	    getGroundClasses(type, (String)(e.nextElement()), v);
    }

    /**
     *Return a help string listing all known ground/non-ground classes
     */
    public static String getClassList(int type)
    {
	StringBuffer buff=new StringBuffer();
	Enumeration e=props[type].propertyNames();
	while(e.hasMoreElements())
	    {
		buff.append((String)e.nextElement()).append("\n");
	    }

	return buff.toString();
    }

    /**
     *Check whether one class of objects is a subtype of another 
     * (i.e. a is in b)
     */
    public static boolean isPartOf(int type, String a, String b)
    {
	Set bSet=new TreeSet();
	getGroundClasses(type, b, bSet);
	Set aSet=new TreeSet();
	getGroundClasses(type, a, aSet);

	return bSet.containsAll(aSet);
    }

    /**
     *Return a random ground subclass
     */

    public static String getRandomClass(int type, String ancestor)
    {
	try {

	Set s=new TreeSet();
	if(ancestor==null)
	    getGroundClasses(type, s);
	else
	    getGroundClasses(type, ancestor, s);

	int n=s.size();

	if(n==0)
	    throw new Exception("ERROR: Nothing to Randomize From!");

	int r=(int)randomLong(1, n);

	int i=1;
	for(Iterator it = s.iterator(); it.hasNext(); i++)
	    {
		String cl=(String)it.next();
		if(i==r)
		    return cl;
	    }

	// -- mustn't get here
	throw new Exception("FATAL: Internal Error in Random Class!");
	} catch (Exception e) {
	    handleError(e, "Getting random class!");
	}

	return null; // -- This is really a runtime exception
    }

    /**
     *Return a random ground subclass from an intersection
     */

    public static String getRandomClassInt(int type, Vector ancestors)
    {
      try {
	
	Set[] s=new TreeSet[ancestors.size()];
	for(int i=0; i<s.length; i++)
	  {
	    s[i] = new TreeSet();
	    getGroundClasses(type, (String)(ancestors.elementAt(i)), s[i]);
	  }
	// -- get intersection
	for(int i=1; i<s.length; i++)
	  s[0].retainAll(s[i]);

	int n=s[0].size();

	if(n==0)
	    throw new Exception("ERROR: Nothing to Randomize From!");

	int r=(int)randomLong(1, n);

	int i=1;
	for(Iterator it = s[0].iterator(); it.hasNext(); i++)
	    {
		String cl=(String)it.next();
		if(i==r)
		    return cl;
	    }

	// -- mustn't get here
	throw new Exception("FATAL: Internal Error in Random Class!");
	} catch (Exception e) {
	    handleError(e, "Getting random Class!");
	}

	return null; // -- This is really a runtime exception
    }



    public static String getRandomClass(int type)
    {
	return getRandomClass(type, null);
    }

    // -------------------------------------------------------------

  /**
   *Return the name of the currently executing jar file
   */
  private static String getJarFileName ()
    {
      //      String myClassName = theClass.getName() + ".class";
      String myClassName = "edu/stanford/multiagent/gamer/Global.class";

      URL urlJar =
	Global.class.getClassLoader().getResource(myClassName);
	//	Global.class.getClassLoader().getSystemResource(myClassName);

      String urlStr = urlJar.toString();

      if(!urlStr.startsWith("jar:file:"))
	return null;
      int from = "jar:file:".length();
      int to = urlStr.indexOf("!/");
      return urlStr.substring(from, to);
    }

  /**
   *Return an input stream for reading from jar file
   */
  private static InputStream getJarInputStream(String fName)
    throws IOException
  {

    JarFile jf=new JarFile(getJarFileName());
    JarEntry jr=jf.getJarEntry(fName);

    return jf.getInputStream(jr);
  }

  /**
   *Field indicating whether we're running from a jar file
   */
  private static boolean runningFromJar=true;

    // -------------------------------------------------------------

    // -- Static constructor
    static {

      //-- Initialize random here, can be reset later in main
      Global.randSeed = System.currentTimeMillis();
      Global.rand=new Random(Global.randSeed);

	// -- Initialize file properties
      runningFromJar = (getJarFileName()!=null);
      
      // see if user has alternative path
      String path=System.getProperty("gamer.class.path");

	for(int i=0; i<props.length; i++)
	    {
		props[i] = new Properties();
		
		try { 
		  InputStream fIn;
		  if(path!=null) // -- takes precedence
		    fIn = new FileInputStream(path + FILES[i]);
		  else if (runningFromJar)
		    fIn = getJarInputStream("edu/stanford/multiagent/gamer/" +
					    FILES[i]);
		  else
		    fIn = new FileInputStream(FILES[i]); // -- last resort
		  props[i].load(fIn);
		 } catch (Exception e) {
		    System.err.println("ERROR: Failed to read property file!");
		    System.err.println("WARNING: Randomization might not work correctly!");
		    System.err.println(e.getMessage());
		}
	    }

    }


}
