/** Iperfer.java
 * 
 *  ** CS640 - Lab 1 **
 *  
 *  Authors: Cade Wormington, Michael Grube
 *  Date: 10/1/2021
 *  Description: The main class used for our Lab 1 program. Defines an object called Iperfer
 *  	which conducts a bandwidth performance test between two Java Sockets. The main
 *  	method handles the arguments, as the program is meant to be used in the command line.
 *  
 *  How to run:
 *  	Use Makefile for compilation. The Makefile simply runs 'javac Iperfer.java'.
 *  	
 *  	java Iperfer [-c | -s] [-h hostname] [-p portnum] [-t seconds]
 *  	Note: hostname and seconds are not viable args in server mode.
 *  
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Iperfer
{
	// Primary constructor for Iperfer object
	public Iperfer(boolean client, String host, int port, int sec)
	{
		// Set fields to correct settings
		this.clientMode = client;
		this.hostname = host;
		this.portNum = port;
		this.seconds = sec;
		this.emptyData = new byte[1000];
		
		for (int i = 0; i < 1000; i++)
		{
			emptyData[i] = 0;
		}
	}

	// Starts the Iperfer object in either server or client mode.
	public void startTest()
	{
		// Check clientMode field and determine if running in server or client mode.
		//
		// Server must open a ServerSocket.
		// Client must open a Socket.
		if (clientMode)
		{
			try
			{
				runClient();
			} catch (IOException e)
			{
				System.out.println("Error: IOException creating client socket.\n" + e.getLocalizedMessage());
			}
		} else
		{
			try
			{
				runServer();
			} catch (IOException e)
			{
				System.out.println("Error: IOException creating listen socket.\n" + e.getLocalizedMessage());
				System.exit(1);
			}
		}
	}

	// The startTest method calls runServer to run Iperfer in server mode.
	// Can throw IOException because of ServerSocket creation.
	private void runServer() throws IOException
	{
		// Defines stat tracking vars.
		long byteCount = 0;
		long tranTimeMillis = 0;

		// Attempt to bind ServerSocket to specified port and accept connections.
		// Can generate IOException.
		ServerSocket listenSocket = new ServerSocket(portNum);
		Socket clientConn = listenSocket.accept();

		// Gets the data stream from the socket. Do not need to worry about closing
		// the InputStream because it was created by the Socket.
		InputStream in = clientConn.getInputStream();

		// Declares vars for tracking time.
		long startTime = 0;
		long endTime = 0;

		// Loops on the condition that the Socket in use has data to read. Ends
		// when the buffer reaches EOF (Socket is done receiving data).
		//
		// This loop tracks the amount of bytes received by the Socket and
		// the amount of time that the Socket was receiving data.
		byte[] buffer = new byte[1000];
		int temp = 0;
		while ((temp = in.read(buffer)) != -1)
		{
			if (byteCount == 0)
			{
				startTime = System.currentTimeMillis();
			}
			byteCount += temp;
		}
		endTime = System.currentTimeMillis();

		// Determines total data transmission time using the difference between
		// the start and end times.
		tranTimeMillis = endTime - startTime;

		// After test is complete, close the ServerSocket and Socket.
		clientConn.close();
		listenSocket.close();

		// Calculations for output
		double KBCount = (byteCount / 1000.0);
		double mbps = (KBCount / 125.0) / (tranTimeMillis / 1000);
		
		// Output
		System.out.printf("KB received = %.3f     Mbps = %.3f%n", KBCount, mbps);

	}

	// The startTest method calls runClient to run Iperfer in client mode.
	// Can throw IOException because of Socket creation.
	private void runClient() throws IOException
	{
		// Define stat tracking var
		long byteCount = 0;

		// Creates a socket that connects to hostname on port# portNum.
		// Can generate IOException.
		Socket clientSock = new Socket(hostname, portNum);
		// Gets the OutputStream for this socket to send data over.
		OutputStream out = clientSock.getOutputStream();

		// Calculates the time at which we must stop sending data.
		long targetTime = System.currentTimeMillis() + (seconds * 1000);


		// Loops until the stopping time is reached.
		//
		// During a loop, puts 1000 bytes of 0's into the output buffer
		// and flushes it immediately after. It then increments the byteCount
		// by 1000.
		while (System.currentTimeMillis() < targetTime)
		{
			out.write(emptyData);
			out.flush();
			byteCount += 1000;
		}

		// Closes the socket we opened.
		clientSock.close();

		// Calculations for output.
		double KBCount = (byteCount / 1000.0);
		double mbps = (KBCount / 125.0) / seconds;
		
		// output
		System.out.printf("KB sent = %.3f     Mbps = %.3f%n", KBCount, mbps);
	}

	// Instance vars for storing the settings for the Iperfer object.
	private boolean clientMode;
	private String hostname;
	private int portNum;
	private int seconds;
	
	// An array of empty data to send.
	private byte[] emptyData;

	
	//
	// End of class
	//
	// Start of main method
	// 
	
	// Handles command line args when program is ran.
	public static void main(String[] args)
	{
		String host = "";
		int port = 0;
		int time = 0;
		boolean client = false;
		
		if (args.length != 7 && args.length != 3)
			{
				System.out.println("Error: invalid arguments");
				return;
			}
		
		// Test for client
		if (args[0].compareTo("-c") == 0)
		{
			// Test for client
			if (args[1].compareTo("-h") != 0)
			{
				// Condition was not met
				System.out.println("Error: invalid arguments");
			}
			// Test for reasonable length host name
			if (args[2].length() > 32)
			{
				System.out.println("Error: Please use reasonable hostname");
				return;
			}
			host = args[2];

			if (args[3].compareTo("-p") != 0)
			{
				// Condition was not met
				System.out.println("Error: invalid arguments");
				return;
			}
			try
			{
				port = Integer.valueOf(args[4]);
			} catch (NumberFormatException e)
			{
				System.out.println("Error: invalid arguments");
				return;
			}
			port = Integer.valueOf(args[4]);

			if ((port < 1024) || (port > 65535))
			{
				System.out.println("Error: port number must be in the range 1024 to 65535");
				return;
			}

			if (args[5].compareTo("-t") != 0)
			{
				// Condition was not met
				System.out.println("Error: invalid arguments");
				return;
			}

			try
			{
				time = Integer.valueOf(args[6]);
			} catch (NumberFormatException e)
			{
				System.out.println("Error: invalid arguments");
				return;
			}
			time = Integer.valueOf(args[6]);

			// Test for reasonable time
			if (time > 120)
			{
				System.out.println("Error: Please use reasonable time");
			}
			client = true;

		}
		// Test for server
		else if (args[0].compareTo("-s") == 0)
		{
			if (args[1].compareTo("-p") != 0)
			{
				// Condition was not met
				System.out.println("Error: invalid arguments");
				return;
			}
			try
			{
				port = Integer.valueOf(args[2]);
			} catch (NumberFormatException e)
			{
				System.out.println("Error: invalid arguments");
				return;
			}
			port = Integer.valueOf(args[2]);

			if ((port < 1024) || (port > 65535))
			{
				System.out.println("Error: port number must be in the range 1024 to 65535");
				return;
			}
			client = false;
		} else {
			System.out.println("Error: invalid arguments");
			return;
		}
		
		// Makes an Iperfer object after the args screen. The client parameter
		// is checked before anything occurs, so passing an empty string in host
		// when in server mode is okay.
		Iperfer conn = new Iperfer(client, host, port, time);
		conn.startTest();

	}
}
