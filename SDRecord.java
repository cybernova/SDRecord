//########
//#LICENSE                                                   
//########

//# Tool for audio recording with SDR v1.0. Please visit the project's website at: https://github.com/cybernova/SDRecord
//# Copyright (C) 2016 Andrea 'cybernova' Dari (andreadari91@gmail.com)                                   
//#                                                                                                       
//# This shell script is free software: you can redistribute it and/or modify                             
//# it under the terms of the GNU General Public License as published by                                   
//# the Free Software Foundation, either version 2 of the License, or                                     
//# any later version.                                                                   
//#                                                                                                       
//# This program is distributed in the hope that it will be useful,                                       
//# but WITHOUT ANY WARRANTY; without even the implied warranty of                                        
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         
//# GNU General Public License for more details.                                                          
//#                                                                                                       
//# You should have received a copy of the GNU General Public License                                     
//# along with this application.  If not, see <http://www.gnu.org/licenses/>.

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.apache.commons.cli.*;

public class SDRecord {
	
	public static void main(String[] args)  {
		
		boolean recordToInf = false;
		long recordTo = 0, txsize = 0, wr = 0, max = 0;
		int sourcePort = 0, destPort = 0;
		String val;
		OutputStream writer = null;
		InetAddress rhost = null, lhost = null;
		DatagramSocket socket = null;
		
		//Default values
		int buffSize = 1500;
		try { lhost = InetAddress.getByName("0.0.0.0"); } 
		catch (UnknownHostException e1) { System.err.println("Host not reconized"); e1.printStackTrace(); System.exit(3); }
		recordToInf = true;
		sourcePort = 7355;
		
		Options options = new Options();
		
		options.addOption("m", true, "Minutes to record, default is no limit");
		options.addOption("l", true, "Bind to a specific local address, default is 0.0.0.0");
		options.addOption("p", true, "Local port to use, default is 7355");
		options.addOption("r", true, "Remote address where to send data");
		options.addOption("d", true, "Remote port, to use with -r option");
		options.addOption("f", true, "Output file where to save the recording");
		options.addOption("s", true, "Stop recording when reaching specified MBs");
		options.addOption("h", false, "Help");
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options,args);
		} catch (ParseException e1) {
			System.err.println("Error while parsing the command line");
			System.exit(1);
		}
		
		if (cmd.hasOption("m"))
		{
			val = cmd.getOptionValue("m");
			try {
				if (Long.parseLong(val) < 0 )
				{
					System.err.println("-m argument value cannot be negative");
					System.exit(3);
				}
				recordTo = System.currentTimeMillis() + (Long.parseLong(val) * 60000); 
				recordToInf = false; 
				}
			catch (NumberFormatException e ) { System.err.println("-m argument not an integer"); e.printStackTrace(); System.exit(3); }
		}
		
		if (cmd.hasOption("l"))
		{
			val = cmd.getOptionValue("l");
			try { lhost = InetAddress.getByName(val); }
			catch (UnknownHostException e ) { System.err.println("Host not reconized"); e.printStackTrace(); System.exit(3); }
		} 
		
		if (cmd.hasOption("p"))
		{
			val = cmd.getOptionValue("p");
			try { sourcePort = Integer.parseInt(val); }
			catch (NumberFormatException e ) { System.err.println("-p argument not an integer"); e.printStackTrace(); System.exit(3); }
		}

		if (cmd.hasOption("r"))
		{
			val = cmd.getOptionValue("r");
			try { rhost = InetAddress.getByName(val); }
			catch (UnknownHostException e ) { System.err.println("Host not reconized"); e.printStackTrace(); System.exit(3); }
		}
		
		if (cmd.hasOption("d"))
		{
			val = cmd.getOptionValue("d");
			try { destPort = Integer.parseInt(val); }
			catch (NumberFormatException e ) { System.err.println("-d argument not an integer"); e.printStackTrace(); System.exit(3); }
		}
		
		if (cmd.hasOption("f"))
		{
			val = cmd.getOptionValue("f");
			try { writer = new FileOutputStream(val); }
			catch (FileNotFoundException e ) { System.err.println("File not found"); e.printStackTrace(); System.exit(3); }
		}
		
		if (cmd.hasOption("s"))
		{
			val = cmd.getOptionValue("s");
			if (Long.parseLong(val) < 0 )
			{
				System.err.println("-s argument value cannot be negative");
				System.exit(3);
			}
			try { max = Long.parseLong(val) * 1024 * 1000; }
			catch (NumberFormatException e ) { System.err.println("-s argument not an integer"); e.printStackTrace(); System.exit(3); }
		}
		
		if (cmd.hasOption("h"))
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "SDRecord", options );
			System.exit(0);
		}
		
		try {
			socket = new DatagramSocket(sourcePort, lhost);
			//socket options
			socket.setReuseAddress(true);
		} catch (SocketException e) { e.printStackTrace(); System.exit(3); }
		
		byte[] buffer = new byte[buffSize];			
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		
		System.err.println("Listening " + lhost.toString() + " on port " + sourcePort);
			
		while ( recordToInf == true || System.currentTimeMillis() <= recordTo ) 
		{	
			//Stop recording when reaching max bytes
			if ( max != 0 && txsize >= max )
				break;
			
			packet.setData(buffer);
			try { socket.receive(packet); }
			catch (IOException e) { e.printStackTrace(); System.exit(4); }
			
			//Ignoring packets with no data
			if (basicFilter(packet) == null)
				continue;
			
			if (writer == null && rhost == null)
				wr = recordToStdout(packet);
			if (writer != null) 
				wr = recordToFile(packet, writer);
			if (rhost != null)
				wr = recordToSocket(packet, socket, rhost, destPort);
			
			txsize = txsize + wr;
			System.err.print("\r"+formatSize(txsize)+" transferred"+"\033[K"+"\t Press Ctrl+c to terminate");
		}
		//closing socket and exit
		socket.close();
		System.out.println();
		System.exit(0);
	}
	
	private static byte[] basicFilter(DatagramPacket packet)
	{
		for (byte b : packet.getData())
			if ( b != 0 )
			return packet.getData();
		return null;
	}
	
	private static long recordToStdout(DatagramPacket packet) 
	{
		System.out.write(packet.getData(), 0, packet.getLength()); 
		System.out.flush();
		return packet.getLength();
	}
	private static long recordToFile(DatagramPacket packet, OutputStream writer) 
	{
		try { writer.write(packet.getData(), 0, packet.getLength()); writer.flush(); } catch (IOException e) { e.printStackTrace(); System.exit(5); }
		return packet.getLength();
	}
	private static long recordToSocket(DatagramPacket packet, DatagramSocket socket, InetAddress rhost, int destPort)
	{
		packet.setPort(destPort);
		packet.setAddress(rhost);
		try { socket.send(packet); } catch (IOException e) { e.printStackTrace(); System.exit(5); }
		return packet.getLength();
	}
	private static String formatSize(long v) 
	{
	    if (v < 1024) return v + " B";
	    int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
	    return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
	}
}
