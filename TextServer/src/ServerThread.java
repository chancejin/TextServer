
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
	int myState = 1;
	String myName = null;
	String myPhone = null;
	String mySubnetIP = null;
	
	String dest = null;
	int destIndex = 13;
	String destIP = null;
	int destState = 0;
	
	public ServerThread(Vector<ServerThread> connectList, Socket socket){
		this.connectList = connectList;
		this.client = socket;
		this.itsme = connectList.size();
		
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
			if(msg != null && msg.startsWith("StopCall ")){
				send(msg);
				break;
			}
			else if(msg != null){
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
					
					PreparedStatement pStmt = conn.prepareStatement("select ip_address, is_disabled from user_info where phone_number=?");
					pStmt.setString(1, dest);
					
					ResultSet rset = pStmt.executeQuery();
					while(rset.next()) {
						destIP = rset.getString("ip_address");
						destState = Integer.parseInt(rset.getString("is_disabled"));
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for(int i=0;i<connectList.size();i++){
					if(connectList.get(i).myPhone.equals(dest)){
						destIndex = i;
						connectList.get(destIndex).destIndex = itsme;
						
						System.out.println("destIP = "+destIP);
					}
				}
			}
			else if(msg.startsWith("MyName ")){
				this.myName = msg.substring(7);
				return null;
			}
			else if(msg.startsWith("MyPhone ")){
				this.myPhone = msg.substring(8);
			// DB에서 myPhone을 키값으로 subnetIP를 가져와서 this.mySubnetIP에 저장한다.
				try {
                                        Class.forName("com.mysql.jdbc.Driver");
                                        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_ttong", "root", "");

                                        PreparedStatement pStmt = conn.prepareStatement("select ip_address_sub from user_info where phone_number=?");
                                        pStmt.setString(1, this.myPhone);

                                        ResultSet rset = pStmt.executeQuery();
                                        while(rset.next()) {
                                                this.mySubnetIP = rset.getString("ip_address_sub");
                                        }
                                } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                }
	
				return null;
			}
			else if(msg.startsWith("MyState ")){
				this.myState = Integer.valueOf(msg.substring(8));
				return null;
			}
			else if(msg.startsWith("StopApp ")){
				this.connectList.remove(itsme);
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return msg;
	}
	
	//메시지 전송
	public void send(String msg){
		if(msg.startsWith("StopCall ")){
			ServerThread st = connectList.get(itsme);
			ServerThread std = connectList.get(destIndex);
			// DB!!!!!!!!!!!!!!!!!!!!!
			try {
				std.bufferWriter.write(msg+"\n");
				std.bufferWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(destIndex>itsme){
				this.connectList.remove(destIndex);
				this.connectList.remove(itsme);
			}
			else{
				this.connectList.remove(itsme);
				this.connectList.remove(destIndex);
			}
		}
		else if(msg.startsWith("StartCall ")){
			ServerThread st = connectList.get(itsme);
			ServerThread std = connectList.get(destIndex);
			// DB!!!!!!!!!!!!!!!!!!!!!
			try{
				std.bufferWriter.write(msg+"/"+st.myState+"/"+st.myName+"/"+st.myPhone+"/"+st.mySubnetIP+"\n");
				std.bufferWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(msg.startsWith("OkayCall ")){
			ServerThread st = connectList.get(itsme);
			ServerThread std = connectList.get(destIndex);
			try {
				std.bufferWriter.write(msg+"/"+st.mySubnetIP+"\n");
				std.bufferWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			ServerThread std = connectList.get(destIndex);
			try {
				std.bufferWriter.write(msg+"\n");
				std.bufferWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	

}
