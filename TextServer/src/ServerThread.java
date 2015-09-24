import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
	
	//메시지 청취
	public String listen(){
		String msg="";
		try {
			msg= buffer.readLine();
			System.out.println("msg:"+msg);
			if(msg.startsWith("dest ")){
				connectList.get(itsme).dest = msg.substring(5);
				System.out.println(connectList.get(itsme).dest);
				
				// find a destination's ip address from DB
				try {
					Class.forName("com.mysql.jdbc.Driver");
					Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_ttong", "root", "");
					
					PreparedStatement pStmt = conn.prepareStatement("select ip_address from user_info where phone_number=?");
					pStmt.setString(1, dest);
					
					ResultSet rset = pStmt.executeQuery();
					while(rset.next()) {
						destIP = rset.getString("ip_address");
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				for(int i=0;i<connectList.size();i++){
					if(connectList.get(i).client.getInetAddress().getHostAddress().equals(destIP)){
						destIndex = i;
						System.out.println("destIP = "+destIP);
						System.out.println(connectList.get(i).client.getInetAddress().getHostAddress());
					}
					//test
					else{
						System.out.println("destIP = "+destIP);
						System.out.println(connectList.get(i).client.getInetAddress().getHostAddress());						
					}
				}
				send("StartCall ");
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return msg;
	}
	
	//메시지 전송
	public void send(String msg){
		if(msg.startsWith("StartCall ")){
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