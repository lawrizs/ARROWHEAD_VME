package org.arrowhead.wp5.application.common;

/*-
 * #%L
 * ARROWHEAD::WP5::Application-Common
 * %%
 * Copyright (C) 2016 The ARROWHEAD Consortium
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;


/**
 * Specialization of this class acts as stand-alone Arrowhead WP5 servers 
 *  
 * @author Laurynas
 *
 */
public class StandAloneApp {
	@SuppressWarnings("unused")
	private static final long serialVersionUID = -7864239353813175330L;

	private static String RC_FILE_NAME = ".arrowheadrc";
		
	private Interpreter bsh;	
	// Cmd-line options
	private Options opts = null;
	private Option optHelp = new Option("?", "help", false,
			"Shows the list of all available commands.");
	private Option optScriptFile = new Option("f", "file", true,
			"Execute the script from the provided (.bsh) script file.");
	private Option optCmd = new Option("c", "command", true,
			"Execute the specific command.");

	public StandAloneApp() throws Exception {
		this.bsh = new Interpreter(new CommandLineReader(
								   new InputStreamReader(System.in)), 
								   System.out, 
								   System.err, true);
				
		this.bsh.setShowResults(true);
		this.bsh.eval("printBanner() {};"); // No banner
		this.bsh.getNameSpace().importObject(this);
		this.bsh.eval("help()  { global.printHelp(); }");
//		/* Install default commands */
//		this.bsh.eval("start() { server.start(); }");
//		this.bsh.eval("stop()  { server.stop(); }");		
//		this.bsh.eval("isReady()  { global.isReady(); }");
		
		//this.bsh.set(name, value);
		// Create command line options
		this.opts = new Options();
		this.opts.addOption(this.optHelp);
		this.opts.addOption(this.optScriptFile);
		this.opts.addOption(this.optCmd);
	}
	
	public Interpreter getBsh() {
		return bsh;
	}

	public void run()
	{		
		 this.run(null);
	}	
	
	public static void main(String[] args) {
		try {
			(new StandAloneApp()).run(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Start the server
	 */
	public void start() throws Exception
	{
		System.out.println("Starting the server...");
	}
	
	/**
	 * Stops the server
	 */
	public void stop() throws Exception
	{
		System.out.println("Stopping the server...");
	}

	public void run(String[] args) {
		// create the parser
		CommandLine cmdLine = null;
		CommandLineParser parser = new GnuParser();
		try {
			// parse the command line arguments
			cmdLine = parser.parse(opts, args);
		} catch (ParseException exp) {
			System.err.println("Invalid command line parameters. Problem: "	+ exp.getMessage());
			this.printHelp();
			return;
		}

		if (cmdLine == null || cmdLine.hasOption(this.optHelp.getOpt())) {
			this.printHelp();
			System.out.println("You can run commands at start-up of the shell by adding them to "+RC_FILE_NAME);
			System.exit(0);
		};
		
		// Run start-up scripts
		File rcFile = new File(RC_FILE_NAME);
		if (rcFile.exists()) {
			runCommandsFromFile(RC_FILE_NAME);
		}
		
		System.out.println("This is a shell of ArrowHead WP5 servers (based on BeanShell by Pat Niemeyer).");
		System.out.println("The server to be used:"+this.getClass().getName());
		System.out.println("Type \"help();\" for help.");
		
		if (cmdLine.hasOption(this.optScriptFile.getOpt()))
		{
			String fileName = cmdLine.getOptionValue(this.optScriptFile.getOpt());
			if (fileName != null)
			{
				this.runCommandsFromFile(fileName);
			} else {
				System.out.println("No script file is specified!");
			}
		}
		
		if (cmdLine.hasOption(this.optCmd.getOpt())) {
			String cmd = cmdLine.getOptionValue(this.optCmd.getOpt());
			if (cmd != null)
				try {
					this.bsh.eval(cmd);
				} catch (Exception e) {
					System.out.println(e.toString());
				}
		}

		this.bsh.run();	
	}

	public void printHelp() {	
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(this.getClass().getSimpleName(), this.opts);
		System.out.println("Server-specific commands:");
		System.out.println(" start() \t Starts the server");
		System.out.println(" stop() \t Stops the server");
		System.out.println(" isReady() \t Reports if the server is running");
		System.out.println("");
		System.out.println("Most important Bean Shell commands:");
		System.out.println(" exit() \t Exits the shell;");
		System.out.println(" javap(obj) \t Get detailed information about \"obj\".");
		System.out.println(" source(fn) \t Load file \"fn\" in the current namespace (context).");	
		System.out.println("For more commands, see: http://www.beanshell.org/manual/bshcommands.html#BeanShell_Commands_Documentation");		
	}
	
	private void runCommandsFromFile(String fileName)
	{
		try {
			Object result = this.bsh.source(fileName);
			if ( result instanceof Class )
				try {
					Interpreter.invokeMain((Class<?>)result, new String[0]);
				} catch ( Exception e ) 
				{
					Object o = e;
					if ( e instanceof InvocationTargetException )
						o = ((InvocationTargetException)e)
							.getTargetException();
					System.err.println(
						"Class: "+result+" main method threw exception:"+o);
				}
		} catch ( FileNotFoundException e ) {
			System.out.println("File not found: "+e);
		} catch ( TargetError e ) {
			System.out.println("Script threw exception: "+e);
			if ( e.inNativeCode() )
				e.printStackTrace( false, System.err );
		} catch ( EvalError e ) {
			System.out.println("Evaluation Error: "+e);
		} catch ( IOException e ) {
			System.out.println("I/O Error: "+e);
		}  					
	}
}
