import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JTextArea;

public class ServerCommunication {
	private static final int PORT = 50000;
	private static String message;  // socket 전송 메세지
	private static Socket link = null;
	private static BufferedReader input= null;
	private static PrintWriter output = null;
	
	static ArrayList<String> list = null;
	static int position = 0;
	static boolean isReceive = false;
	
	ServerCommunication(Socket link, InetAddress host, JTextArea textArea) {
		this.link = link;
		
		// tab 키가 눌리면 서버 접속
		accessServer1(host);

		// 사용자가 입력한 커서 앞 text를 가져온다.
		position = textArea.getCaretPosition();
		message = textArea.getText();
		message = message.substring(0, position);
		
		// text의 길이 반환
		int textLength = message.length();
			
		// text와 길이를 하나의 문자열로 합친다.
		String sendMessage = "" + textLength + " True";
		
		// 서버에게 텍스트 길이 전달
		output.print(sendMessage);
		output.flush();
		
		// 커서 앞 텍스트 길이, true 전달 후 서버 접속 끊기
		closingConnecting1();

		
		// 다시 서버 접속, 커서 앞의 텍스트 보내기
		accessServer1(host);

		output.print(message);
		output.flush();
		
		// 접속 끊기
		closingConnecting1();
		
		
		// 서버 접속, 커서 뒤의 텍스트를 보낸다.
		accessServer1(host);
		
		message = textArea.getText();
		message = message.substring(position, textArea.getText().length());
		
		output.print(message);
		output.flush();
		
		
		// 연결 끊기
		closingConnecting1();

		
		// 서버로부터 문자열 수신 및 출력
		accessServer1(host);
		
		list = new ArrayList<>();
		String receiveMessage = "";
		isReceive = false;
		
		try {
			while((receiveMessage = input.readLine()) != null) {
				// 이전 문자열이 공백이면 break
				if("SuccessfullyParsed".equals(receiveMessage)) {
					isReceive = false;
					break;
				}
				else if("LexError".equals(receiveMessage)) {
					
				}
				else if("ParseError".equals(receiveMessage)) {
					
				}
				
				isReceive = true;
				
				 // white 문자열 제거
				receiveMessage = receiveMessage.replace("white ", "");
				list.add(receiveMessage);
				
			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		closingConnecting1();
	}
	
	private static void closingConnecting1() {
		// 접속 끊기
		try {
			// System.out.println("\n* Closing connection... *");
			link.close();
			link = null;
		} catch(IOException ioEx) {
			System.out.println("Connecting error");
		}
	}
	
	// 서버와 연결, 소켓 inputstream, outputstream 반환
	private static void accessServer1(InetAddress host) {
		try {
			// 서버와 호스트 연결
			link = new Socket(host, PORT);
			
			// 소켓으로부터 문자열을 읽는 데 사용할 수 있는 InputStream 반환
			input = new BufferedReader(new InputStreamReader(link.getInputStream()));
			
			// 소켓으로 write할 outputStream 반환
			output = new PrintWriter(link.getOutputStream());
			
			/*
			boolean result = link.isConnected();
			if(result) System.out.println("Connecting Server");
			else System.out.println("Fail Connecting Server");
			*/
		}
		catch(IOException ioEx) {
			System.out.println("Connection refused");
		}
	}
}
