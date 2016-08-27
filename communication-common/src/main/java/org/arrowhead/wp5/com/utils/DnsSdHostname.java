package org.arrowhead.wp5.com.utils;

/*-
 * #%L
 * ARROWHEAD::WP5::Communication Common
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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DnsSdHostname {
    final static Logger logger = LoggerFactory.getLogger(DnsSdHostname.class);
    
	/**
	 * This function will try to update the property "dnssd.hostname" based on the property "dnssd.interface"
	 * and the IP address assigned to that interface. 
	 * 
	 * Set "dnssd.interface" to e.g. vpn_atf0, if the IP on that interface is e.g. 10.200.0.102, then the 
	 * hostname will be set to "rh102.test.benearit.arrowhead.eu".
	 */
	public static void setHostname() {
		String iface = System.getProperty("dnssd.interface");
		String hostname = System.getProperty("dnssd.hostname");
		if (iface != null && !"".equals(iface)) {
//			logger.debug("Interface set, trying to get hostname by IP.");
			String ip = getLastIpDigit();
			if (ip != null) {
			    System.setProperty("dnssd.hostname", "rh" + ip + ".test.bnearit.arrowhead.eu");
			}
		} else if (hostname == null || "".equals(hostname)) {
			System.out.println("Hostname not set!");
		}
	}

	/**
	 * This function will try to fetch the last digit from the IP assigned by the VPN.
	 * Set the System property "dnssd.interface" to the name of the VPN interface, e.g. "vpn_atf0".
	 * 
	 * @return String with last digit of the IP. E.g "102" if the IP is "10.200.0.102".
	 */
	public static String getLastIpDigit() {
		String iface = System.getProperty("dnssd.interface");
		if (iface != null && !"".equals(iface)) {
			Enumeration<NetworkInterface> list;
			try {
				list = NetworkInterface.getNetworkInterfaces();
				while (list.hasMoreElements()) {
					NetworkInterface ni = list.nextElement();
					if (iface.equals(ni.getName())) {
						Enumeration<InetAddress> addresses = ni.getInetAddresses();
						while (addresses.hasMoreElements()) {
							InetAddress ina = addresses.nextElement();
							byte[] add = ina.getAddress();
							if (add.length == 4) { // Is IPv4 address ?
								return Byte.toString(add[3]);
							}
						}
					}
				}
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else {
			logger.warn("Property 'dnssd.interface' not set, can not get last IP digit.");
			return null;
		}
		logger.warn("Unable to find last IP digit!");
		return null;
	}
}
