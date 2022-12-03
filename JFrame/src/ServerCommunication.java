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
	private static String message;  // socket ���� �޼���
	private static Socket link = null;
	private static BufferedReader input= null;
	private static PrintWriter output = null;
	
	static ArrayList<String> list = null;
	static int position = 0;
	static boolean isReceive = false;
	
	ServerCommunication(Socket link, InetAddress host, JTextArea textArea) {
		this.link = link;
		
		// tab Ű�� ������ ���� ����
		accessServer1(host);

		// ����ڰ� �Է��� Ŀ�� �� text�� �����´�.
		position = textArea.getCaretPosition();
		message = textArea.getText();
		message = message.substring(0, position);
		
		// text�� ���� ��ȯ
		int textLength = message.length();
			
		// text�� ���̸� �ϳ��� ���ڿ��� ��ģ��.
		String sendMessage = "" + textLength + " True";
		
		// �������� �ؽ�Ʈ ���� ����
		output.print(sendMessage);
		output.flush();
		
		// Ŀ�� �� �ؽ�Ʈ ����, true ���� �� ���� ���� ����
		closingConnecting1();

		
		// �ٽ� ���� ����, Ŀ�� ���� �ؽ�Ʈ ������
		accessServer1(host);

		output.print(message);
		output.flush();
		
		// ���� ����
		closingConnecting1();
		
		
		// ���� ����, Ŀ�� ���� �ؽ�Ʈ�� ������.
		accessServer1(host);
		
		message = textArea.getText();
		message = message.substring(position, textArea.getText().length());
		
		output.print(message);
		output.flush();
		
		
		// ���� ����
		closingConnecting1();

		
		// �����κ��� ���ڿ� ���� �� ���
		accessServer1(host);
		
		list = new ArrayList<>();
		String receiveMessage = "";
		isReceive = false;
		
		try {
			while((receiveMessage = input.readLine()) != null) {
				// ���� ���ڿ��� �����̸� break
				if("SuccessfullyParsed".equals(receiveMessage)) {
					isReceive = false;
					break;
				}
				else if("LexError".equals(receiveMessage)) {
					
				}
				else if("ParseError".equals(receiveMessage)) {
					
				}
				
				isReceive = true;
				
				 // white ���ڿ� ����
				receiveMessage = receiveMessage.replace("white ", "");
				list.add(receiveMessage);
				
			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		closingConnecting1();
	}
	
	private static void closingConnecting1() {
		// ���� ����
		try {
			// System.out.println("\n* Closing connection... *");
			link.close();
			link = null;
		} catch(IOException ioEx) {
			System.out.println("Connecting error");
		}
	}
	
	// ������ ����, ���� inputstream, outputstream ��ȯ
	private static void accessServer1(InetAddress host) {
		try {
			// ������ ȣ��Ʈ ����
			link = new Socket(host, PORT);
			
			// �������κ��� ���ڿ��� �д� �� ����� �� �ִ� InputStream ��ȯ
			input = new BufferedReader(new InputStreamReader(link.getInputStream()));
			
			// �������� write�� outputStream ��ȯ
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
