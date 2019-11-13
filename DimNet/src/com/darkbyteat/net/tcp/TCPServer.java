package com.darkbyteat.net.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

import com.darkbyteat.net.Packet;
import com.darkbyteat.net.ServerListener;

public class TCPServer extends Thread {
	
	//Constructor for the server, takes in the port for the server to listen on for the TCP connection
	public TCPServer(ServerListener listener, final int port) throws IOException {
		this.listener = listener;
		//Opens the channel and binds the port
		this.channel = ServerSocketChannel.open();
		this.channel.bind(new InetSocketAddress(port));
		this.port = port;
		this.connections = new ConcurrentHashMap<>();
		this.running = true;
		this.start();
	}
	
	//Stores whether the server is running or not
	public volatile boolean running;
	//Stores the TCP port
	public final int port;
	
	//Stores the listener for the server
	protected volatile ServerListener listener;
	//Stores the server socket for connecting to the clients on TCP
	protected volatile ServerSocketChannel channel;
	//Stores the TCP and UDP connections in conjunction with a unique key for each connection
	protected volatile ConcurrentHashMap<String, TCPConnection> connections;
	
	//Closes the server
	public void close() throws IOException {
		//Sets the running flag to false
		running = false;
		
		//Closes all the connections
		for(TCPConnection con : connections.values()) {
			con.close();
		}
		
		//Clears the connections
		connections.clear();
		//Closes the server channel
		channel.close();
	}
	
	//Sends a message to a client
	public void send(final String id, final String data) throws IOException {
		//Loops through each client
		for(final TCPConnection con : connections.values()) {
			//Checks if the connection id matches
			if(con.id.equals(id)) {
				//Sends the packet to that connection
				con.send(data);
			}
		}
	}
	
	//Broadcasts a message to all clients
	public void broadcast(final String data) throws IOException {
		//Loops through each client
		for(final TCPConnection con : connections.values()) {
			//Sends the packet to that connection
			con.send(data);
		}
	}
	
	//Runs the server
	@Override
	public void run() {
		//Adds any connections to the TCP connections hash map
		//Gets the TCP server channel and creates the connection from it
		//Creates the channel to listen on the new thread
		while(running && channel.isOpen()) {
			try {
				//Checks if the channel is still open
				if(channel.isOpen()) {
					//Gets the client depending on the type of server channel being used
					SocketChannel client = channel.accept();
					//Generates an id for the client using the IP the socket is connected to
					String id = Packet.hash(((InetSocketAddress) client.getLocalAddress()).getAddress().getHostAddress());
					//Only adds the client if there was one accepted (i.e the client isn't null)
					if(client != null) {
						//Disables Nagle's algorithm for the packets
						client.setOption(StandardSocketOptions.TCP_NODELAY, true);
						//Opens the channel
						client.configureBlocking(false);
						connections.put(id, new TCPConnection(client, listener, id));
						//Runs the server listener join method
						listener.onJoin(id);
					}
				}
			} catch (IOException e) {
				//Prints an error message to the console.
				System.err.println("Error receiving data over TCP server!");
				e.printStackTrace();
			}
		}
	}
	
	//Gets the current TCP connections with their ID as the keys
	public ConcurrentHashMap<String, TCPConnection> getConnections() {
		return this.connections;
	}
}