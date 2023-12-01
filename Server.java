  import java.net.*; //socket, serversocket
import java.io.*; //입출력
import java.util.*;

public class TcpIpMultichatServer {
  HashMap<String, DataOutputStream> clients; //HashMap은 내부에 '키'와 '값'을 저장하는 자료 구조

  TcpIpMultichatServer() {
    clients = new HashMap<>();

   //메서드는 MAP을 스레드로부터 안전하게 만들기 위해 래핑
   //    //`Collections.synchronizedMap(clients)`를 호출한 후 반환된 맵을 새 변수에 할당하지 않았습니다. 이렇게 하면 동기화된 맵의 참조가 없어져 원래의 `clients` 맵은 여전히 동기화되지 않은 상태입니다.
   //        //결론적으로, `Collections.synchronizedMap()` 메서드는 주어진 맵을 동기화하여 여러 스레드에서 안전하게 사용할 수 있도록 도와주지만, 복잡한 연산을 수행할 때는 추가적인 동기화 조치가 필요할 수 있습니>다.
    Collections.synchronizedMap(clients);
  }

  public void start() {
    ServerSocket serverSocket = null;
    Socket socket = null;

    try {
      serverSocket = new ServerSocket(7777);       //7777포트와 결합 bind

      System.out.println("서버가 시작되었습니다.");

      while(true) {
        socket = serverSocket.accept(); //클라이언트의 연결요청이 올 때까지 실행을 멈추고 계속 기다린다. 연결 요청이 오면 클라이언트 소켓과 통신할 새로운 소켓을 생성
        System.out.println("["+ socket.getInetAddress() + ":" + socket.getPort() + "]" + "에서 접속하셨습니다."); //socket.getInetAdderss() < 주소 얻기, socket.getPort() < 포트 얻기
        ServerReceiver thread = new ServerReceiver(socket);
        thread.start();
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  } //start()

  void sendToAll(String msg) {
    Iterator it = clients.keySet().iterator(); //keySet()은 [A, B, C]라는 Set형태로 변환, iterator() 메서드는 Set에서 원소들을 순차적으로 조회

    while(it.hasNext()) {
      try {
        DataOutputStream out = (DataOutputStream)clients.get(it.next()); //clients 맵에서 Iterator를 통해 가져온 키에 해당하는 값을 DataOutputStream 객체로 캐스팅하여 out 변수에 저장
        out.writeUTF(msg);
      } catch(IOException e) {}
    } //while
  } //sendToAll

  public static void main(String args[]) {
    new TcpIpMultichatServer().start();
  }

  class ServerReceiver extends Thread {
    Socket socket;
    DataInputStream in; // 데이터 타입들을 입력 스트림에서 읽어들일 때 사용됩니다.
    DataOutputStream out; // 데이터 타입들을 출력 스트림에서 출력할 때 사용됩니다.

    ServerReceiver(Socket socket) {
      this.socket = socket;
      try {
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
      } catch (IOException e) {}
    }

    public void run() {
      String name = "";

      try {
        name = in.readUTF();
        sendToAll("#" + name + "님이 들어오셨습니다.");

        clients.put(name, out); //name을 키로, out을 값으로 사용하여 clients 맵에 저장, DataOutputStream 객체(out)를 검색할 수 있다.
        System.out.println("현재 서버접속자 수는 " + clients.size() + "입니다.");

        while(in!=null) {
          sendToAll(in.readUTF());
        }
      } catch(IOException e) {}
      finally {
        sendToAll("#" + name + "님이 나가셨습니다.");
        clients.remove(name);
        System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "]" + "에서 접속을 종료하셨습니다.");
        System.out.println("현재 서버접속자 수는 " + clients.size() + "입니다.");
      } //try
    } //run
  } //ReceiverThread
} //class
