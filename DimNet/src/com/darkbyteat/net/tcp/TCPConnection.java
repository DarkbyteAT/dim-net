package com.darkbyteat.net.tcp;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.darkbyteat.net.NetworkListener;
import com.darkbyteat.net.Packet;

public class TCPConnection extends Thread {
	
	//Constructor for the connection, initialises the connection to the server using the socket channel
	//Takes in the listener to output received packets to
	public TCPConnection(SocketChannel channel, NetworkListener listener, String id) throws SocketException {
		this.channel = channel;
		this.listener = listener;
		this.id = id;
		//Starts the thread for the connection
		this.start();
	}
	
	//Stores the size of packets
	public static final int PACKET_SIZE = 1024;
	
	//Stores the ID of the connection
	public volatile String id;
	//Stores the ping of the client (RTT ping) as well as the time the ping was sent
	public volatile long ping, timeSincePing;
	
	//Stores the channel
	protected volatile SocketChannel channel;
	//Stores the connector
	protected volatile NetworkListener listener;
	
	//Sends data to the client
	public void send(final String data) throws IOException {
		//Writes the packet as a byte buffer, using the data from the packet and converting in to bytes
		//Adds an end-of-packet footer to the data
		if(channel.isConnected()) {
			channel.write(ByteBuffer.wrap((data + "%EOP%").getBytes()));
		}
	}
	
	//Sends a ping to the server
	public void ping() throws IOException {
		//Pings the server
		this.send("!ping");
		//Updates the time since the ping
		timeSincePing = System.currentTimeMillis();
		//System.out.println("Ping!");
	}
	
	//Ticks from the client
	@Override
	public void run() {
		//Tries to initialise a connection
		try {
			//Runs as long as the channel is open
			while(channel.isOpen()) {
				//Creates a new byte buffer to create the input
				ByteBuffer buffer = ByteBuffer.allocate(PACKET_SIZE);
				//Stores the result, -1 by default to assume that the channel couldn't be read
				//Checks which type of supported channel the channel is (DatagramChannel or SocketChannel - UDP vs TCP)
				//Uses this to determine where to read the result
				//Creates a string from the buffer and passes the string into the connector's function
				//Only passes the data if there's info to read (if the result isn't -1)
				if(channel.isOpen() && channel.read(buffer) != -1) {
					//Creates the packet from the received data and id
					String packet = new String(buffer.array()).trim();
					
					//If the packet is a ping, returns a pong back
					if(packet.contains("!ping%EOP%")) {
						send("!pong");
					} else if(packet.contains("!pong%EOP%")) { //If the packet is a pong, calculates the ping
						//Calculates the ping from the current time and the time since the ping was sent
						ping = System.currentTimeMillis() - timeSincePing;
						//System.out.println("Pong!\nPING: " + ping + "ms");
					} else if(!packet.trim().isEmpty()) { //Checks if the packet's data isn't just a blank string or whitespace
						//Sends the packet to the listener
						//Finds all the packets received by the listener by looping through as long as the packet contains an end-of-packet footer
						while(packet.contains("%EOP%")) {
							//Passes in the portion before the end-of-packet footer
							listener.onReceive(new Packet(id, packet.substring(0, packet.indexOf("%EOP%"))));
							//Removes that portion from the packet string
							packet = packet.substring(packet.indexOf("%EOP%") + "%EOP%".length());
						}
					}
				}
			}
			
			//Closes the connection
			close();
		} catch (IOException e) {
			System.err.println("Error receiving data over TCP!");
			e.printStackTrace();
		}
	}
	
	//Closes the connection
	public void close() throws IOException {
		//Closes the channel
		channel.close();
	}
	
	//Getters
	public SocketChannel getChannel() {
		return this.channel;
	}
}