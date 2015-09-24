import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

public class ServerThread extends Thread{
	Socket client;
	BufferedReader buffer;
	BufferedWriter bufferWriter;
	Vector<ServerThread> connectList;
	
	int itsme = 0;
	int myState = 0;
	String dest = null;
	int destIndex = 13;
	String destIP = null;
	
	public ServerThread(Vector<ServerThread> connectList, Socket socket, String dest){
		this.connectList = connectList;
		this.client = socket;
		this.itsme = connectList.size();
		this.dest = dest;
		
		try {
		buffer = new BufferedReader(new InputStreamReader((client.getInputStream())));
		bufferWriter = new BufferedWriter(new OutputStreamWriter((client.getOutputStream())));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		while(true){
			String msg = listen();
			if(msg != null){
				send(msg);
			}
		}
	}
	
	//硫붿떆吏� 泥�痍�
	public String listen(){
		String msg="";
		try {
			msg= buffer.readLine();
			System.out.println("msg:"+msg);
			if(msg.startsWith("dest ")){
				connectList.get(itsme).dest = msg.substring(5);
				System.out.println(connectList.get(itsme).dest);
				// DB!!!!!!!!!!!!!!!!!!!!!!! update!!!!!!
				// find a destination's ip by DB
				if(connectList.get(itsme).dest.equals("01097983036")){
					System.out.println("dest - 01097983036");
					destIP = "211.48.46.156";
				}
				else{
					destIP = "211.48.46.145";
					System.out.println("dest - 01055555555");
				}
				for(int i=0;i<connectList.size();i++){
					if(connectList.get(i).client.getInetAddress().getHostAddress().equals(destIP)){
						destIndex = i;
						System.out.println("i = "+i);
						System.out.println("destIndex = "+destIndex);
						System.out.println("destIP = "+destIP);
						System.out.println(connectList.get(i).client.getInetAddress().getHostAddress());
					}
					//test
					else{
						System.out.println("i = "+i);
						System.out.println("destIP = "+destIP);
						System.out.println(connectList.get(i).client.getInetAddress().getHostAddress());						
					}
				}
				send("StartCall ");
				return null;
			}
			else if(msg.startsWith("StopCall ")){
				send("StopCall ");
			}
			else{
				send(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return msg;
	}
	
	//硫붿떆吏� �쟾�넚
	public void send(String msg){
		if(msg.startsWith("StartCall ")||msg.startsWith("StopCall ")){
			ServerThread st = connectList.get(itsme);
			ServerThread std = connectList.get(destIndex);
			// DB!!!!!!!!!!!!!!!!!!!!!
			try {
				st.bufferWriter.write(msg+std.myState+"\n");
				st.bufferWriter.flush();
				std.bufferWriter.write(msg+st.myState+"\n");
				std.bufferWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			ServerThread st = connectList.get(destIndex);
			try {
				st.bufferWriter.write(msg);
				st.bufferWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}