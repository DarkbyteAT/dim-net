package com.darkbyteat.net.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import com.darkbyteat.net.NetworkListener;

public class TCPClient extends TCPConnection {
	
	//Constructor for the client, connects to the IP and port specified
	//Creates a TCP connection using these parameters
	public TCPClient(NetworkListener listener, final String ip, final int port) throws IOException {
		super(SocketChannel.open(new InetSocketAddress(ip, port)), listener, "SERVER");
	}
}