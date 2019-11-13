package com.darkbyteat.net.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import com.darkbyteat.net.Packet;
import com.darkbyteat.net.ServerListener;

public class UDPServer extends Thread {

	//Constructor for the UDP server class, takes in the listener for the server to output received data to and the port to listen on
	public UDPServer(ServerListener listener, final int port) throws IOException {
		this.listener = listener;
		this.port = port;
		this.connections = new ArrayList<>();
		//Creates the socket and instructs it to only liten on the specified port
		this.socket = new DatagramSocket(port);
		this.running = true;
		//Starts the server thread
		this.start();
	}
	
	//Stores whether the server is running or not
	public volatile boolean running;
	//Stores the port to listen on
	public volatile int port;
	//Stores the ping of the client (RTT ping) as well as the time the ping was sent
	public volatile long ping, timeSincePing;
	
	//Stores the UDP socket
	protected volatile DatagramSocket socket;
	//Stores the listener
	protected volatile ServerListener listener;
	//Stores the IPs of all clients connected to the server
	protected volatile ArrayList<String> connections;
	
	//Closes the server
	public void close() throws IOException {
		//Sets the running flag to false
		running = false;
		//Closes the server socket
		socket.close();
	}
	
	//Sends a message to a client
	public void send(final String ip, final String data) throws IOException {
		//Sends the data to that IP over the datagram socket
		//Fits the packet to the correct size
		socket.send(new DatagramPacket(UDPConnection.fitPacket((data + "%EOP%").getBytes(), UDPConnection.PACKET_SIZE), UDPConnection.PACKET_SIZE, InetAddress.getByName(ip), port));
	}
	
	//Broadcasts a message to all clients
	public void broadcast(final String data) throws IOException {
		//Loops through each client
		for(final String ip : connections) {
			//Sends the packet to that connection
			this.send(ip, data);
		}
	}
	
	//Ticks the server
	@Override
	public void run() {
		//Adds any connections to the UDP connections hashmap
		//Creates the connection to listen on the new thread
		while(running && !socket.isClosed()) {
			try {
				//Creates a datagram packet to store the received data
				DatagramPacket received = new DatagramPacket(new byte[UDPConnection.PACKET_SIZE], UDPConnection.PACKET_SIZE);
				//Receives the packet from the socket
				socket.receive(received);
				//Creates a string from the received data
				String packet = new String(received.getData()).trim(),
						host = received.getAddress().getHostAddress();
				
				//Checks if the client is in the arraylist, if not then adds it to the list
				if(!connections.contains(host)) {
					connections.add(host);
					//Runs the server listener's join method
					listener.onJoin(host);
				}
				
				//If the packet is a ping, returns a pong back
				if(packet.contains("!ping%EOP%")) {
					send(host, "!pong");
				} else if(packet.contains("!pong%EOP%")) { //If the packet is a pong, calculates the ping
					//Calculates the ping from the current time and the time since the ping was sent
					ping = System.currentTimeMillis() - timeSincePing;
					//System.out.println("Pong!\nPING: " + ping + "ms");
				} else if(!packet.trim().isEmpty()) { //Checks if the packet's data isn't just a blank string or whitespace
					//Sends the packet to the listener
					//Finds all the packets received by the listener by looping through as long as the packet contains an end-of-packet footer
					while(packet.contains("%EOP%")) {
						//Passes in the portion before the end-of-packet footer
						listener.onReceive(new Packet(Packet.hash(host), packet.substring(0, packet.indexOf("%EOP%"))));
						//Removes that portion from the packet string
						packet = packet.substring(packet.indexOf("%EOP%") + "%EOP%".length());
					}
				}
			} catch (IOException e) {
				//Prints an error message to the console.
				System.err.println("Error receiving data over UDP server!");
				e.printStackTrace();
			}
		}
	}
}