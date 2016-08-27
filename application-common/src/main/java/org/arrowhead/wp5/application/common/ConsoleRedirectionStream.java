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


import java.io.PrintStream;

public class ConsoleRedirectionStream extends PrintStream {
	private ConsoleRedirectionApp consoleapp;
	 /** the origin output stream */
	private PrintStream orgSteam;

	public ConsoleRedirectionStream(PrintStream out, ConsoleRedirectionApp consoleapp) {
		super( out, true );
		
		this.orgSteam = out;
		this.consoleapp = consoleapp;
	}	

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}
	
	@Override
	public void print( String s )
	{
	    //do what ever you like
		if (orgSteam != null)
			orgSteam.print( s );
	    
		if (s != null)
			this.consoleapp.broadcast(s.getBytes());
	}
	
	@Override
	public void print(Object obj) {
		if (obj != null)
			print(obj.toString());
	}

    @Override
	public void println( String s )
	{    	
    	if (s != null && !s.endsWith(System.lineSeparator()))
    		print( s + System.lineSeparator());
    	else 
    		print( s + System.lineSeparator());
	}

	public PrintStream getOrgSteam() {
		return orgSteam;
	}

}
