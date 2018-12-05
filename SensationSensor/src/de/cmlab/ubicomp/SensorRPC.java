package de.cmlab.ubicomp;

import java.util.Date;
import java.util.Enumeration;
import java.util.InputMismatchException;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Pattern;
import java.net.*;
import java.text.SimpleDateFormat;

import org.apache.xmlrpc.*;


import de.cmlab.ubicomp.lib.SensorUDPReceiver;
import de.cmlab.ubicomp.lib.model.AndroidSensor;

/**
 * It is testing the Sensor client of XMLRPC Sens-ation framework
 * ONLY for button[0] and button[1] UDPReceiver
 * @author Mastoras Georgios
 */
public class SensorRPC {

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
     * This is the main method
     * to show the server Address and port
     * and initiate the SensorUDPReceiver
     * @param args not used
     */
    public static void main(String[] args) {
        int portUDPServer = 5555;
        String ipUDPServer;
		int port = 5000;
		String ip = "127.0.0.1";
		String message;
		Object result;
		String el1;
		XmlRpcClient client = null;
		Vector<String> params = new Vector<String>();
		try {
			System.out.println("Please enter the ip and port number of Sens-ation server...");
			ip = getIp();
			System.out.println("and for the port number ");
			port = getInteger();

			client = new XmlRpcClient("http://" + ip + ":" + String.valueOf(port) + "/");
			System.out.println();
			System.out.println("Trying to establish connectioon with Sens-ation Server...");
			System.out.println();

			params.clear();
			result = client.execute("SensorPort.ping", params);
			message = ((String) result).toString();
			System.out.println("Sens-ation Server has returned the message: " + message);
			System.out.println();

			params.clear();
			el1 = "  <Sensor id=\"MySensor\" class=\"Other\">\r\n" + 
					"    <Description>Temperature like Sensor.</Description>\r\n" + 
					"    <HardwareID />\r\n" + 
					"    <Command />\r\n" + 
					"    <LocationID>WE5/01.045</LocationID>\r\n" + 
					"    <Owner>Georgios Mastoras</Owner>\r\n" + 
					"    <Comment>This sensor is created via the XML description</Comment>\r\n" + 
					"    <AvailableSince>2018-01-01 10:00:00</AvailableSince>\r\n" + 
					"    <AvailableUntil>2028-12-31 12:00:00</AvailableUntil>\r\n" + 
					"    <SensorActivity activity=\"active\" />\r\n" + 
					"    <NativeDataType>Integer</NativeDataType>\r\n" + 
					"    <MaximumValue>100.0</MaximumValue>\r\n" + 
					"    <MinimumValue>0.0</MinimumValue>\r\n" + 
					"  </Sensor>\r\n";
			params.addElement(new String(el1));
			result = client.execute("SensorPort.updateSensor", params);
			message = ((String) result).toString();
			System.out.println("Sens-ation server has created/updated the sensor: " + message);
			System.out.println();
        
		} catch (Exception exception) {
			System.err.println("JavaClient: " + exception);
		}
        

        System.out.println("Receiving sensor UDP data on ip: " + WhatIsMyIp() + " port " + portUDPServer);

	    /*initiate a receiver by defining a port 
		number that will be sent to the receiver from the app*/
        SensorUDPReceiver receiver = new SensorUDPReceiver(portUDPServer);
		/*create a listener as shown below and let it implement Observer to
		to get the app updates*/
        SensorUDPListener listener = new SensorUDPListener(client);
        receiver.addObserver(listener);
    }

    /**
     * Constructs and activates the SensorUDPReceiver
     */
    public static class SensorUDPListener implements Observer {

    	public static String getCurrentTimeStamp() {
    		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		Date now = new Date();
    	    String strDate = sdfDate.format(now);
    	    return strDate;
    	}
    	
    	float[] orientation = new float[3]; // only the orientation value is inspecetd
        int watch = 0; // the number of characters in Actuator bar
        static int previous = 0;
		String message;
		Object result;
		String el1, el2, el3;
		XmlRpcClient localClient;
		Vector<String> params = new Vector<String>();

        /**
         * constructor
         */
        SensorUDPListener(XmlRpcClient client) {
        	super();
        	localClient = client;
        }

        /**
         * update method is called automatically
         * on any message from android device
         * @param o the Server object
         * @param arg the Sensor object (Android device values)
         */
        @Override
        public void update(Observable o, Object arg) {
            /*check SensorUDPReceiver Java Docs for the AndroidSensor API*/
            AndroidSensor sensorValues = (AndroidSensor) arg;
//            orientation = sensorValues.getOrientation();
            if (sensorValues.getTouchedButtons()[1] && watch < 100) watch++;
            if (sensorValues.getTouchedButtons()[0] && watch > 0) watch--;
//            String out = String.format("Orientation: X:%f,Y:%f,Z:%f", orientation[0], orientation[1], orientation[2]);
//            System.out.println(out);
			if (watch != previous) {
				System.out.println();
	            for (int i = 0; i < watch; i++) {
	                System.out.print(">");
	            }

	    		try {
    				previous = watch;
 		            params.clear();
					el1 = "MySensor";
					el2 = getCurrentTimeStamp();
					el3 = Integer.toString(watch);
					params.addElement(new String(el1));
					params.addElement(new String(el2));
					params.addElement(new String(el3));
					result = localClient.execute("SensorPort.notify", params);
					if( !((Boolean) result).booleanValue()) {
						System.out.println("RPC Server couldn't receive the value of MySensor");
	    			}
	    		} catch (Exception exception) {
	    			System.err.println("JavaClient: " + exception);
	    		}
				System.out.println();
				System.out.println();
	            System.out.println("============================");
			}
        }
    }
}
