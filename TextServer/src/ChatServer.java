import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer {
	
	//서버소켓 - 클라이언트의 접속을 받아들이는 클래스
	int port=8080;
	ServerSocket server;
	
	Vector<ServerThread> connectList;
	
	public ChatServer() {
		connectList = new Vector<ServerThread>();
		
		try {
			//서버객체 생성
			server = new ServerSocket(port);
			
			while(true){
				//접속자가 접속하는지 확인
				//접속자가 있을때까지 대기,지연상태에 있다.
				Socket client = server.accept();
				InetAddress inet = client.getInetAddress();
				String ip = inet.getHostAddress();
				System.out.println(ip+"-접속자 발견");
				
				//대화용 쓰레드 생성 및 소켓
				ServerThread serverThread = new ServerThread(connectList, client);
				System.out.println("start");
				serverThread.start();
				
				connectList.add(serverThread);
				System.out.println("현재 접속자수:"+connectList.size());
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		new ChatServer();
		//server = new ServerSocket(port);
			
	}
}
