import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class TextEditor {
	private JFrame frame;
	private JTextArea textArea;  // 텍스트 편집 영역, 여러 줄을 입력할 수 있는 컴포넌트(자체 스크롤 없음)
	private JFileChooser fileChooser = new JFileChooser();  // 파일 선택 열기
	private File file;
	private JTextArea lines;  // line number
	
	private static String message;  // socket 전송 메세지
	private static final int PORT = 50000;
	private static InetAddress host;
	static Socket link = null;
	static Scanner input= null;
	static PrintWriter output = null;
	static int position = 0;  // 사용자의 커서 위치
	private String receiveMessage;  // 서버로부터 받은 메세지
	private ArrayList<String> list = new ArrayList<>();
	
	public static void main(String[] args) {
		System.setProperty( "https.protocols", "TLSv1.1,TLSv1.2" );  // connection reset 문제 해결

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// host의 ip주소를 가져온다.
					host = InetAddress.getLocalHost();
				} 
				catch(UnknownHostException uhEx) {
					// 호스트 ip를 찾지 못하면 메시지 창을 띄우면서 종료
					JOptionPane.showMessageDialog(null, "Host ID not found!", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}
				try {
					
					TextEditor window = new TextEditor();
					window.frame.setVisible(true);
				} 
				catch(Exception e) {
					
				}
			}
		});
	}
	
	private static void closingConnecting() {
		// 접속 끊기
		try {
			System.out.println("\n* Closing connection... *");
			link.close();
			link = null;
		} catch(IOException ioEx) {
			System.out.println("Connecting error");
		}
	}
	
	// 서버와 연결, 소켓 inputstream, outputstream 반환
	private static void accessServer() {
		try {
			// 서버와 호스트 연결
			link = new Socket(host, PORT);
			
			// 소켓으로부터 바이트를 읽는 데 사용할 수 있는 InputStream 반환
			input = new Scanner(link.getInputStream());
			
			// 소켓으로 write할 outputStream 반환
			output = new PrintWriter(link.getOutputStream());
			
			// socekt 연결이 됐는지 확인하기 위한 필드
			boolean result = link.isConnected();
			if(result) System.out.println("Connecting Server");
			else System.out.println("Fail Connecting Server");
		}
		catch(IOException ioEx) {
			System.out.println("Connection refused");
		}
	}
	
	// Create TextEditor
	public TextEditor() {
		initialize();  // 내용 초기화
	}
	
	private void initialize() {
		frame = new JFrame();  // 타이틀이 없는 프레임 생성
		frame.setBounds(100,100,450,300);  // 위치, 너비, 높이 지정(x,y,w,h)
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 종료 이벤트 처리
		
		JMenuBar menuBar = new JMenuBar();  // 메뉴바 객체 생성
		frame.setJMenuBar(menuBar);  // 프레임에 메뉴바 적용
		
		JMenu mnFile = new JMenu("File");  // 메뉴 표시줄에 보이게 될 File 메뉴 생성
		menuBar.add(mnFile);  // 메뉴바에 File 메뉴 추가
		
		JMenuItem mntmNew = new JMenuItem("New");  // 메뉴에 포함될 세부 메뉴 생성
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int retval = fileChooser.showDialog(frame, "New");  // Dialog 생성
				if(retval == JFileChooser.APPROVE_OPTION) {  // 예 확인을 선택했을 때 리턴되는 값
					file = fileChooser.getSelectedFile();  // 생성한 파일의 경로를 반환, file에 저장
					textArea.setText("");  // 공백으로 초기화
				}
			}
		});
		
		mnFile.add(mntmNew);  // 이벤트 정의 후 File 메뉴에 세부 메뉴인 New 적용
		
		JMenuItem mntmOpen = new JMenuItem("Open");  // 메뉴에 포함될 세부 메뉴 생성
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Dialog 창에서 열기가 정상적으로 수행된 경우 0을 반환, 취소를 누른 경우 1을 반환
				int retval = fileChooser.showOpenDialog(frame);  // 열기용 창 오픈
				
				// 파일이 잘 열렸을 경우
				if(retval == JFileChooser.APPROVE_OPTION) {
					file = fileChooser.getSelectedFile();  // 선택한 파일의 경로 저장
					// 선택한 파일을 읽어
					// JTextArea에 추가
					// 검사 예외 처리하는 코드 작성
					try {
						FileReader fr = new FileReader(file);  // 지정한 파일로부터 읽어올 객체 생성
						BufferedReader br = new BufferedReader(fr);  // 버퍼를 이용해서 사용(효율적)
						
						String str = "";
						String textLine = "";
						
						textLine = br.readLine();  // BufferedReader가 가지고 있는 문자열 한 줄 읽기
						
						// BufferedLine의 readLine은 읽을 데이터가 없을 때 null 리턴
						while((str = br.readLine()) != null) {
							textLine = textLine + "\n" + str;  // 한 줄씩 끊어서 저장
						}
						textArea.setText(textLine);  // 지정한 파일의 text를 textArea로 set
					} catch(Exception e) {
						JOptionPane.showMessageDialog(frame, e.getMessage());  //예외 처리
					}
				} else {
					// 파일이 제대로 열리지 않은 경우 or 그냥 닫은 경우
					// Not working 메시지 띄우기
					JOptionPane.showMessageDialog(frame, "User canced the operation");
				}
			}
		});
		mnFile.add(mntmOpen);  // 이벤트 정의 후 File 메뉴에 세부 메뉴인 Open 적용
		
		JMenuItem mntmSave = new JMenuItem("Save");  // 메뉴에 포함될 세부 메뉴 생성
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(file != null) {
					// 선택한 파일에 저장
					try {
						FileWriter wr = new FileWriter(file, false);  // 문자 스트림을 이용하여 file 객체 생성
						BufferedWriter bw = new BufferedWriter(wr);
						bw.write(textArea.getText());  // 파일에 버퍼를 통해서 text 저장(효율적)
						bw.flush();  // 버퍼에 남은 text 전송
						bw.close();  // 닫기
					} catch(Exception e) {
						// 예외 처리
						JOptionPane.showMessageDialog(frame, e.getMessage());
					}
				}	
			}
		});
		mnFile.add(mntmSave);  // 이벤트 정의 후 File 메뉴에 세부 메뉴인 Save 적용
		
		JMenuItem mntmClose = new JMenuItem("Close");  // 메뉴에 포함될 세부 메뉴 생성
		mntmClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// 텍스트 편집 영역을 공백으로 초기화하고, 현재 선택된 파일을 초기화
				file = null;  // 현재 선택된 파일 초기화
				// JTextArea를 비움
				textArea.setText("");  // 공백으로 초기화
			
			}
		});
		mnFile.add(mntmClose);  // 이벤트 정의 후 File 메뉴에 세부 메뉴인 Close 적용
		
		JMenuItem mntmQuit = new JMenuItem("Quit");  // 메뉴에 포함될 세부 메뉴 생성
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// 프로그램을 종료. System 클래스의 exit 메소드 사용.
				// 프로그램 종료
				System.exit(0);  // 정상 종료
			}
		});
		mnFile.add(mntmQuit);  // 이벤트 정의 후 File 메뉴에 세부 메뉴인 Quit 적용
		
		JMenu mnAbout = new JMenu("About");  // 메뉴 표시줄에 보이게 될 About 메뉴 생성
		menuBar.add(mnAbout);  // 메뉴바에 About 메뉴 추가
		
		JMenuItem mntmAboutTextEditor = new JMenuItem("About Text Editor");  // About 메뉴에 포함할 세부 메뉴
		mntmAboutTextEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(frame, "Text Editor Sample");  // 단순한 알림창을 띄우는 함수
			}
		});
		mnAbout.add(mntmAboutTextEditor);  // 이벤트 정의 후 About 메뉴에 세부 메뉴인 About Text Editor 추가
		// 컨텐츠 영역을 가져온다.
		// 컨텐츠 영역에 부착되는 구성요소들을 어떻게 배치할 것인지 정해준다.
		// x 방향으로 컴포넌트들을 배치
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
	
		// 텍스트 에디터에 스크롤 추가
		JScrollPane scrollPane = new JScrollPane();
		// frame에서 컨텐츠 영역을 가져온다.
		// 컨텐츠 영역에 scroll을 추가
		frame.getContentPane().add(scrollPane);
		
		textArea = new JTextArea();
		// scrollpane에 text 추가(텍스트에 스크롤 추가)
		scrollPane.setViewportView(textArea);
		
		// JTextArea에 line number 추가
		lines = new JTextArea("1");  // 시작 번호
		lines.setBackground(Color.LIGHT_GRAY); // line number 배경 색
		lines.setEditable(false);  // line number 이므로 편집(수정) 불가
		
		// 문서의 내용이 변경되었을 때의 이벤트 설정
		textArea.getDocument().addDocumentListener(new DocumentListener(){
			public String getText(){
				int caretPosition = textArea.getDocument().getLength();
				
				javax.swing.text.Element root = textArea.getDocument().getDefaultRootElement();
				String text = "1" + System.getProperty("line.separator");
				
				for(int i = 2; i < ((javax.swing.text.Element) root).getElementIndex( caretPosition ) + 2; i++){
					text += i + System.getProperty("line.separator");
				}
				return text;
			}
			@Override
			public void changedUpdate(DocumentEvent de) {
				lines.setText(getText());
			}
 
			@Override
			public void insertUpdate(DocumentEvent de) {  // 라인 추가될 때
				lines.setText(getText());  // 추가된 만큼 line number 재설정
			}
 
			@Override
			public void removeUpdate(DocumentEvent de) {  // 라인 사라질 때
				lines.setText(getText());  // 사라진 만큼 line number 재설정
			}
 
		});
		
		// viewport: 기본이 되는 정보를 보기 위해 사용하는 뷰포트(창)
		scrollPane.getViewport().add(textArea);
		scrollPane.setRowHeaderView(lines);  // 행 헤더로 추가
		// 항상 스크롤바가 보이도록 설정
		// setVerticalScrollBarPolicy: 수직방향의 정책 설정 혹은 읽기
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
		frame.add(scrollPane);  // frame에 scollPane 객체 추가
		
		// textArea에서 tab키를 누르면 이벤트 발생 (서버로 데이터 전달)
		textArea.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {

			}

			// 키가 눌려진 상태일 때의 동작 정의
			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				
				if(keyCode == KeyEvent.VK_ENTER) {
					// 사용자가 입력한 text를 가져온다. (한줄?)
					
				}
				else if(keyCode == KeyEvent.VK_TAB) {
					// tab 키가 눌리면 서버 접속
					accessServer();
				
					// 사용자가 입력한 커서 앞 text를 가져온다.
					position = textArea.getCaretPosition();
					message = textArea.getText();
					message = message.substring(0, position);
					
					// text의 길이 반환
					int textLength = message.length();
						
					// text와 길이를 하나의 문자열로 합친다.
					String sendMessage = "" + textLength + " True";
					
					// 서버에게 텍스트 길이 전달
					output.println(sendMessage);
					System.out.println(sendMessage);
					
					// 커서 앞 텍스트 길이, true 전달 후 서버 접속 끊기
					closingConnecting();
					
					// 연결 확인용 나중에 삭제
					try {
						boolean result = link.isConnected();
						System.out.println("-------------------------");
						if(result) System.out.println("Connecting Server");
					}
					catch(Exception ioEx) {
						System.out.println("Connection refused");
						System.out.println("-------------------------");
					}
					
					// 다시 서버 접속, 커서 앞의 텍스트 보내기
					accessServer();
					System.out.println("\n" + message);
					output.println(message);
					// 접속 끊기
					closingConnecting();
					
					// 연결 확인용 나중에 삭제
					try {
						boolean result = link.isConnected();
						System.out.println("-------------------------");
						if(result) System.out.println("Connecting Server");
					}
					catch(Exception ioEx) {
						System.out.println("Connection refused");
						System.out.println("-------------------------");
					}
					
					// 서버 접속, 커서 뒤의 텍스트를 보낸다.
					message = textArea.getText();
					message = message.substring(position, textArea.getText().length());
					
					accessServer();
					output.println(message);
					
					System.out.println(message);
					
					// 연결 끊기
					closingConnecting();
					
					// 연결 확인용 나중에 삭제
					try {
						boolean result = link.isConnected();
						System.out.println("-------------------------");
						if(result) System.out.println("Connecting Server");
					}
					catch(Exception ioEx) {
						System.out.println("Connection refused");
						System.out.println("-------------------------");
					}
					
					
					accessServer();
					
					// 서버로부터 문자열을 받아온다.  이부분이 노트북 window에서 안된다.(문자열이 비어있음)
					try {
						receiveMessage = input.nextLine();
						
						System.out.println(receiveMessage);
						
					} catch (Exception e1) {
						System.out.println(e1.getMessage());
					}
					
					closingConnecting();
					
					// pattern1 : 출력 문자열
					// pattern2 : ... 문자열
					Pattern pattern1 = Pattern.compile("[\nwhite](.*?)[white]");
					Pattern pattern2 = Pattern.compile("[white](.*?)[\nwhite]");
					
					Matcher matcher1 = pattern1.matcher(receiveMessage);
					Matcher matcher2 = pattern2.matcher(receiveMessage);
					
					// 결과를 배열에 저장
					String result = "";
					
					// 일치하는게 있다면 출력
					while(matcher1.find()) {
						int i = 0;
						
						// 만약 찾을 수 없으면 break
						if(matcher1.group(1) == null) break;
						
						System.out.println(matcher1.group(1) + " " + matcher2.group(1));
						// 찾은 문자열을 배열에 추가 (콤마를 기준으로 추가)
						result = matcher1.group(1) + "," + matcher2.group(2);
						list.add(result);
					}
					
					// 마지막 배열에 마지막 문자인 EndFor 추가
					int lastIndex = list.size() - 1;
					
					String temp = list.get(lastIndex) + "EndFor";
					
					list.set(lastIndex, temp);
					
					// 확인용 출력
					for(int i = 0; i < list.size(); i++) {
						System.out.println(list.get(i));
					}
				}
			}

			// 키를 눌렀다 뗐을 때의 동작 정의
			@Override
			public void keyReleased(KeyEvent e) {
				// 서버로부터 받은 문자열을 context menu로 출력
				int keyCode = e.getKeyCode();
				
			}
			});
		
		frame.pack();  // pack() : JRrame의 내용물에 알맞게 윈도우 크기 조절
		frame.setSize(500,500);  // 윈도우 창 크기 지정
		frame.setVisible(true);  // 윈도우 창 디스플레이
 
	}
	
}