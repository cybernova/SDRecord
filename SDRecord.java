//########
//#LICENSE                                                   
//########

//# Tool for audio recording with SDR. Please visit the project's website at: https://github.com/cybernova/SDRecord
//# Copyright (C) 2015 Andrea 'cybernova' Dari (andreadari91@gmail.com)                                   
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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
//import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class SDRecord {

	//ARGS -> [1]: minutes to record [2]: source port  [3]: destination host [4]: destination port [5]: buffer size
	
	public static void main(String[] args) {
		
		boolean deamon = false;
		long recordTo = 0;
		int sourcePort = 0, destPort = 0;
		long txsize = 0;
		InetAddress host = null;
		//Default buffer size
		int buffSize = 1500;
		DatagramSocket socket = null;

		//Checking the number of arguments
		if (args.length != 4 && args.length != 5)
		{ System.out.println("Usage: SDRecord minutes_to_record source_port host dest_port [buff_size]"); System.exit(1); }
		
		try {
		if (Integer.parseInt(args[0]) != 0)
			//current time + minutes to record (ideal)
			recordTo = System.currentTimeMillis() + (Integer.parseInt(args[0]) * 60000);
		else
			//minutes to record = 0 -> deamon
			deamon = true;
		sourcePort = Integer.parseInt(args[1]);
		host = InetAddress.getByName(args[2]);
		destPort = Integer.parseInt(args[3]);
		if (args.length != 4)
			buffSize = Integer.parseInt(args[4]);
		} catch (NumberFormatException e) { System.out.println("Usage: SDRecord minutes_to_record source_port host dest_port [buff_size]"); System.exit(2); } 
		  catch (UnknownHostException e) { System.out.println("Host not reconized"); System.exit(3); }
		
		try {
			socket = new DatagramSocket(sourcePort);
			//socket options
			socket.setReuseAddress(true);
			//socket.setSoTimeout(60000);
		} catch (SocketException e) { e.printStackTrace(); System.exit(4); }
		
		byte[] buffer = new byte[buffSize];			
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		
		while ( deamon == true  || System.currentTimeMillis() <= recordTo ) 
		{	
			packet.setData(buffer);
			try { socket.receive(packet); } //catch (SocketTimeoutException e) { System.out.println("Timeout reached"); socket.close(); System.exit(5); }
			catch (IOException e) { e.printStackTrace(); System.exit(6); }
			//selecting packets that contains information
			for (byte b : packet.getData())
				if ( b != 0) 
				{
					packet.setPort(destPort);
					packet.setAddress(host);
					try { socket.send(packet); } catch (IOException e) { e.printStackTrace(); System.exit(7); }
					txsize = txsize + packet.getLength();
					System.out.print("\r"+formatSize(txsize)+" transferred"+"\033[K");
					break;
				}
		}
		//closing socket and exit
		socket.close();
		System.exit(0);
	}
	
	private static String formatSize(long v) {
	    if (v < 1024) return v + " B";
	    int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
	    return String.format("%.2f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
	}
}
