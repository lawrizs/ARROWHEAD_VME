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
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;

import bsh.ConsoleInterface;
import bsh.EvalError;
import bsh.Interpreter;

public class ConsoleRedirectionApp extends WebSocketApplication implements ConsoleInterface {
	private Interpreter interpreter;
	private ConsoleRedirectionStream console_stream;

	public ConsoleRedirectionApp(Interpreter interpreter) {
		this.interpreter = interpreter;
		this.console_stream = new ConsoleRedirectionStream(System.out, this);
		
		System.setOut(this.console_stream);
		System.setErr(this.console_stream);
		this.interpreter.setConsole(this);
	}

	@Override
	public List<String> getSupportedProtocols(List<String> subProtocol) {
		List<String> prot = new ArrayList<String>();
		prot.add("consoleRawProtocol");
		return prot;
	}

	@Override
	public void onMessage(WebSocket socket, String text) {
		super.onMessage(socket, text);

		try {
			this.interpreter.eval(text);
		} catch (EvalError e) {
			e.printStackTrace(this.interpreter.getOut());
		}
	}

	protected void broadcast(byte[] b) {
		for (WebSocket webSocket : getWebSockets())
			webSocket.send(b);
	}

	@Override
	public Reader getIn() {
		return new StringReader("");
	}

	@Override
	public PrintStream getOut() {
		return this.console_stream;
	}

	@Override
	public PrintStream getErr() {
		return this.console_stream;
	}

	@Override
	public void println(Object o) {
		if (o!=null)
			this.console_stream.println(o.toString());		
	}

	@Override
	public void print(Object o) {
		if (o!=null)
			this.console_stream.print(o.toString());		
	}

	@Override
	public void error(Object o) {
		if (o!=null)
			this.console_stream.println("Error:" + o.toString());		
	}
}
