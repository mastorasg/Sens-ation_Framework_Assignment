package de.cmlab.ubicomp;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.xmlrpc.*;

/**
 * Created by Georgios Mastoras on 3/12/2018.
 * 
 * A server that process XML-RPC messages.
 * It makes use of org.apache.xmlrpc.*
 * It communicates with Sens-ation framework
 * subscribing to MySensor and waiting
 * messages notifying changes of MySensor's value
 * 
 */
public class ClientRPC {

	/**
	 * Validates ip String input
	 * given by user
	 * 
	 * @return the validated ip string
	 */
	public static String getIp() {
		Scanner i = new Scanner(System.in);
		String inputStream = "127.0.0.1";
		Pattern PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

		for (boolean test = false; test == false;) {
			try {
				System.out.print("enter an ip: ");

				inputStream = i.nextLine(); // reading from user
				test = PATTERN.matcher(inputStream).matches();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return inputStream;
	}

	/**
	 * Validates integer input
	 * given by user
	 * 
	 * @return validated integer
	 */
	public static int getInteger() {
		Scanner i = new Scanner(System.in);
		int value = 0;

		for (boolean test = false; test == false;) {
			try {
				System.out.print("enter an integer: ");
				value = i.nextInt();

				test = true;
				return value;
			} catch (InputMismatchException e) {
				System.out.println("Invalid input");
			}
			i.nextLine();
		}
		return value;
	}

	/**
	 * The procedure notify 
	 * is implemented as a public method
	 * that is called by Sens-ation framework.
	 * It takes the value of the subscribed sensor,
	 * prints that value 
	 * and returns the String "done"
	 * 
	 * @param sensorID the sensor's name
	 * @param dateStamp the timestamp
	 * @param value the value
	 * @return the String "done"
	 */
	public String notify(String sensorID, String dateStamp,	String value) {
		System.out.println("I have just received a value " + value + ", for sensor " + sensorID);
		return "done";
	}
	
	/**
	 * Returns the local ip
	 * 
	 * @return the local ip
	 */
	public static String WhatIsMyIp() {
		String ip;
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				// filters out 127.0.0.1 and inactive interfaces like Virtual Machines
				if (iface.isLoopback() || !iface.isUp() || iface.getDisplayName().contains("VMware")
						|| iface.getDisplayName().contains("Virtual"))
					continue;

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					ip = addr.getHostAddress();
					if (!ip.contains(":")) { // if not ipv6
						return(ip);
					}
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		return("127.0.0.1");
	}
	
	/**
	 * The main method of the JavaServer class
	 * 
	 * @param args (here not used)
	 */
	public static void main(String[] args) {
		String clientPort = "8086";
		int sensationPort = 5000;
		String ip = "127.0.0.1";
		String message;
		Object result;
		String el1, el2, el3;
		XmlRpcClient client = null;
		Vector<String> params = new Vector<String>();
		try {
			System.out.println("Please enter the ip of Sens-ation server...");
			ip = getIp();
			System.out.println("and for the port number ");
			sensationPort = getInteger();

			// Connect
			client = new XmlRpcClient("http://" + ip + ":" + String.valueOf(sensationPort) + "/");
			System.out.println();
			System.out.println("Trying to establish connectioon with Sens-ation Server...");
			System.out.println();

			// Ping Sens-ation
			params.clear();
			result = client.execute("SensorPort.ping", params);
			message = ((String) result).toString();
			System.out.println("Server has returned the message: " + message);
			System.out.println();

			// Subscribing to MySensor
			params.clear();
			el1 = WhatIsMyIp();
			el2 = "MySensor";
			el3 = clientPort;
			params.addElement(new String(el1));
			params.addElement(new String(el2));
			params.addElement(new String(el3));
			result = client.execute("GatewayXMLRPC.register", params);
			message = ((String) result).toString();
			System.out.println("Server has returned the message: " + message);
			System.out.println();
		} catch (Exception exception) {
			System.err.println("JavaClient: " + exception);
		}

		try {
			// start a web server at a certain port, usually above 1000
			System.out.println("Attempting to start XML-RPC Server...");
			System.out.println("on ip: " + WhatIsMyIp() + " port " + clientPort);

			/*
			 * The package org.apache.xmlrpc 
			 * contains the class WebServer 
			 * for a XML-RPC Server implementation.
			 * 
			 * The server is initialized by the port number (here: 80).
			 */
			WebServer server = new WebServer(Integer.parseInt(clientPort));
			/*
			 * An instance of the same server class 
			 * is then associated with a handler (here "sample")
			 * that is accessible by the client.
			 */
			server.addHandler("StableXMLRPCClient", new ClientRPC()); 
			server.start();

			System.out.println("... started successfully.");
			System.out.println();
			System.out.println("Accepting requests. (Halt program to stop.)");

		} catch (Exception exception) {
			System.err.println("JavaServer: " + exception);
		}
	}
}
