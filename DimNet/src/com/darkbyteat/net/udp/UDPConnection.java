package com.darkbyteat.net.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.darkbyteat.net.NetworkListener;
import com.darkbyteat.net.Packet;

public class UDPConnection extends Thread {

	//Constructor for the UDP connection, takes in the UDP socket as well as the IP and port to send data to
	//Takes in a listener, passes any received data to the listener
	public UDPConnection(DatagramSocket socket, NetworkListener listener, String ip, int port) throws UnknownHostException {
		this.socket = socket;
		this.listener = listener;
		this.ip = ip;
		this.address = InetAddress.getByName(ip);
		this.port = port;
		//Starts the thread for the connection
		this.start();
	}
	
	//Stores the size of packets
	public static final int PACKET_SIZE = 1024;
	
	//Stores the IP to send data to
	public volatile String ip;
	//Stores the port to receive and send data on
	public volatile int port;
	//Stores the ping of the client (RTT ping) as well as the time the ping was sent
	public volatile long ping, timeSincePing;
	
	//Stores the UDP socket
	protected volatile DatagramSocket socket;
	//Stores the listener
	protected volatile NetworkListener listener;
	//Stores the inet address that the IP references
	protected volatile InetAddress address;
	
	//Sends data to the specified IP
	public void send(final String data) throws IOException {
		//Checks if the socket is open to send
		if(!socket.isClosed()) {
			//Sends the data in a datagram packet to the correct location
			//Fits the data to the correct size
			socket.send(new DatagramPacket(fitPacket((data + "%EOP%").getBytes(), PACKET_SIZE), PACKET_SIZE, address, port));
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
	
	//Ticks the connection by receiving data
	@Override
	public void run() {
		//Runs as long as the thread is alive
		while(this.isAlive()) {
			try {
				//Checks if the socket is open before receiving
				if(!socket.isClosed()) {
					//Creates a datagram packet to store the received data
					DatagramPacket received = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
					//Receives the packet from the socket
					socket.receive(received);
					//Creates a string from the received data
					String packet = new String(received.getData()).trim();
				
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
							listener.onReceive(new Packet(received.getAddress().getHostAddress(), packet.substring(0, packet.indexOf("%EOP%"))));
							//Removes that portion from the packet string
							packet = packet.substring(packet.indexOf("%EOP%") + "%EOP%".length());
						}
					}
				}
			} catch (IOException e) {
				System.err.println("Error receiving data over UDP!");
				e.printStackTrace();
			}
		}
	}
	
	//Closes the connection
	public void close() throws IOException {
		//Closes the channel
		socket.close();
	}
	
	//Fits a byte array to the correct size to be sent
	public static byte[] fitPacket(byte[] data, int size) {
		//Creates a new byte[] to store the output
		byte[] out = new byte[size];
		//Loops through each of the indexes in the output array
		for(int i = 0; i < out.length; i++) {
			//If the data given fits in the array at this point, adds it
			if(i < data.length) {
				out[i] = data[i];
			}
		}
		
		//Returns the output array
		return out;
	}
	
	//Getters
	public DatagramSocket getSocket() {
		return this.socket;
	}
}