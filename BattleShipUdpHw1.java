
package NetworkHw1Udp;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Ozgur  Akdeniz ve Seyma Yaldız
 */
public class BattleShipUdpHw1 {
    
    static int[][] board = new int[5][5]; // önce oyun alanının alanını tanımladık  /////ÇABUK BİTSİN DİYE BEŞEBEŞLİK AÇTIK

        public static void gameServer() throws IOException{

            int port = 9876;// Servera sabit bir port numarası atadık
                    try {   // olası bir hata yaratabilicek kısmı try bloğunun içine yazıyoruz
                        DatagramSocket serverSocket = new DatagramSocket(port);        // Server için socket oluşturduk bu socketi verileri gönderip alabilmek için oluşturuyoruz

                        byte[] receiveData = new byte[1024];   //alınacak data
                        byte[] sendData = new byte[1024];       //gönderilecek data

                                while (true) {
                                    DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);  //alınacak datalar için datagram packeti oluşturuyoruz
                                    System.out.println("Clientin bağlanması bekleniyor " + port);
                                    System.out.println("\n Lütfen Bekleyiniz ...");
                                    
                                    serverSocket.receive(receivePacket);                            //alınacak paketin socketini ıp adresini ve portunu eşitliyoruz
                                    InetAddress IPAddress = receivePacket.getAddress();    //********VARSAYILAN IP ADRESİ LOCALHOSTTUR
                                    port = receivePacket.getPort();
                                                     
                                    clearConsole(); 
                                                                        // Console u temizleyip alınacak paketin bilgilerini çekiyoruz getData ile
                                   receivePacket.getData();

                                    System.out.println("Client'e bağlanıyor");
                                    

                                    System.out.println("Oyun alanı oluşturuluyor");
                                    //OYun alanı hazırlanıyor
                                    createBoard(board);

                                    setRandomShip();
                                    setRandomShip();
                                    setRandomShip();

                                    showBoard(board);


                                    System.out.println("Client oyun alanı oluşturulurken bekleyin");

                                    sendData = new String(" ").getBytes(); 
                                    
                                    DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, IPAddress, port);  //gönderilecek bilgiler için bir datagram paketi oluşturuyoruz
                                    serverSocket.send(sendPacket);
                                    
                                    waitYourTurn();

                                    serverSocket.receive(receivePacket);

                                    //Oyuna başla
                                    clearConsole();
                                    showBoard(board);

                                    BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
                                    System.out.println("İlk vuruşu senin ;)");

                                    boolean running = true;
                                            while(running){     // Oyun buradan başlıyor

                                                //Atışı hazırla
                                                int[] shot = new int[2];        // İki atış verilik atış arrayi

                                                System.out.println("X koordinatını girin: ");
                                                int x = new Integer(inFromUser.readLine());

                                                System.out.println("Y koordinatını girin: ");
                                                int y = new Integer(inFromUser.readLine());
                                                shot[0] = y;                    //y koordinat türü
                                                shot[1] = x;                    //x koordinat türü

                                                System.out.println("Pozisyonunu vurdun " + shot[1] + "," + shot [0] );

                                                
                                                sendData = intsToBytes(shot);
                                                sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);    // vuruşun bilgileri gönderiliyor
                                                serverSocket.send(sendPacket);

                                                
                                                receivePacket = new DatagramPacket(receiveData,receiveData.length);                     // vuruşun bilgileri alınıyor
                                                serverSocket.receive(receivePacket);

                                                String hit = new String(receivePacket.getData());           // gelen bilgiler tanımlanıyor
                                                        if(hit.contains("vurdun")){
                                                            System.out.print("atış isabetli!");
                                                        }else if(hit.contains("gameover")){
                                                            System.out.print("Düşmanın son gemisini vurdun!! Oyunu kazandın ^.^");
                                                            break;
                                                        }/*else{
                                                            System.out.print("olmadı  >.< ");}*/
                                                        
                                                System.out.println(" ");
                                                showBoard(board);
                                                
                                                System.out.println("Clientin ateş etmesi bekleniyor...");
                                                
                                                serverSocket.receive(receivePacket);         
                                                shot = bytesToInts(receivePacket.getData());
                                                clearConsole();
                                                
                                                System.out.println("Client pozisyonunu vurdu " + shot[1] + "," + shot [0] );

                                                //Geminin vurulup vurulmadığını kontrol et
                                                String doYouHit = shotOnBoard(shot);
                                                showBoard(board);

                                                //Vuruş yapıldığında oyuncu bilgilendiriliyor
                                                sendData = doYouHit.getBytes();
                                                sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                                                serverSocket.send(sendPacket);

                                                        if(doYouHit.equals("gameover")){
                                                            System.out.println("Son gemini yok etti! T.T  ");
                                                            break;


                                                        }
                                                serverSocket.receive(receivePacket);

                                                sendPacket = null;
                                                        if(isGameOver()) running = false; 

                                                    }
                                    System.out.println("  Oyun bitti !!!  ");
                                    new Integer(inFromUser.readLine());
                                    serverSocket.close();
                                }

                    } catch (SocketException ex) {
                        Logger.getLogger(BattleShipUdpHw1.class.getName()).log(Level.SEVERE, null, ex);
                    }
        }
    
         public static void gameClient() throws SocketException, IOException{

                clearConsole();

                BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in)); 
                //Veri paketini sunucuya göndermek için kullanılacak soket.
                DatagramSocket clientSocket = new DatagramSocket();    //******DATAGRAM SOKETİ OLUŞTURULDU

                System.out.println(" sunucu adını girin(localhost): ");//"localhost olarak belirlediğimiz için başka bişey giremiyor
                String server = inFromUser.readLine();

                        //Sunucu adresini al

                InetAddress clientIPAddress = InetAddress.getByName(server);      // ***** IP adresi kullanıcının girmesini istedik ama serverda tanımladığımız için serverinki dışında giremez

                System.out.println(" Sunucu portunu girin (9876): ");//"9876 olarak belirledik
                int port = new Integer(inFromUser.readLine());

        // Sunucu verilerini göndermek ve almak için kullanılacak değişkenlerin başlatılması
                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];

                sendData = new String("Client bağlandı").getBytes();

        // Sunucuya gönderilecek paketi hazırlayın
                DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, clientIPAddress, port);        //*******DATAGRAM PAKETİ OLUŞTURULDU GÖNDERİLECEK PAKET
                clientSocket.send(sendPacket);

                        clearConsole();
                        System.out.println("Başarıyla bağlandı!");

                        System.out.println("Client oyuna bağlanırken bekleyin");
                        DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);       //********* DATAGRAM PAKETİ ALINACAK PAKET KARŞI OYUNCUDADN GELEN BİLGİLER
                        clientSocket.receive(receivePacket);                           //****** RECEİVE KOMUTU ALMA KOMUTU
                      

                        System.out.println("Sıra sizde!");
                        createBoard(board);

                        setRandomShip();
                        setRandomShip();
                        setRandomShip();
                        

                        showBoard(board);


                        sendData = "alan hazır".getBytes();
                        sendPacket = new DatagramPacket(sendData, sendData.length, clientIPAddress, port);
                        clientSocket.send(sendPacket);                            //*****BİLGİLERİ GÖNDERİYORUZ

                        boolean running = true;
        // oyunu başlat
                        clearConsole();
                       
                        showBoard(board);   //*****OYUN ALANI OLUŞTURULUYOR ARTIK OYUNA BAŞLİYACAĞIZ
                                    while(running){

                    // Vuruşu al

                                        System.out.println("Serverın vuruş için hazırlanmasını bekle");
                                        receivePacket = new DatagramPacket(receiveData,receiveData.length);         //****** İLK ATIŞI SERVER YAPICAK SERVERIN GİRDİĞİ BİLGİLERİ ALIYORUZ
                                        clientSocket.receive(receivePacket);

                                        int[] shot = new int[2];                    // VURUŞLAR TANIMLANDI
                                        shot = bytesToInts(receivePacket.getData());
                                        clearConsole();       //****** EKRANA YAZDIRMADAN ÖNCE CONSOLE U DÜZENLİYORUZ
                                        System.out.println("Server şu pozisyonu vurdu " + shot[1] + "," + shot [0] + " ");       //****** SERVERIN ATIŞ VERİSİ ALINDI VE EKRANA YAZDIRIYORUZ

                    // Geminin vurulup vurulmadığını kontrol et
                                        String doYouHit = shotOnBoard(shot);
                                        showBoard(board);


                    // Vuruş başarılıysa oyuncuyu rapor et
                                        sendData = doYouHit.getBytes();       //******** SERVERIN VURUŞUNDAN SONRA BİZİM OYUN ALANIMIZDAKİ ETKİSİNİ GÖNDERİYORUZ
                                        sendPacket = new DatagramPacket(sendData, sendData.length, clientIPAddress, port);
                                        clientSocket.send(sendPacket);

                                                    if(doYouHit.equals("gameover")){  //***** ATIŞ İSABETLİ VE OYUNU BİTİREN ATIŞ DEĞİLSE OYUN DEVAM EDİYOR
                                                        System.out.println(" Son gemini yok etti! T.T ");
                                                        break;
                                                    }

                                        //atış pozisyonu
                                        System.out.println("Vuruşunun x koordinatını gir: ");
                                        int x = new Integer(inFromUser.readLine());       //******* KULLANICIDAN ALINACAK BİLGİNİN GİRİLMESİNİ SAĞLAR BUFFERREADER İLE BİRLİKTE

                                        System.out.println("Vuruşunun y koordinatını gir: ");                //*****BİZİM VURUŞUMUZ
                                        int y = new Integer(inFromUser.readLine());
                                        shot[1] = x;
                                        shot[0] = y;

                                        System.out.println("Pozisyonunu vurdun " + shot[1] + "," + shot [0] );

                                        sendData = intsToBytes(shot);
                                        sendPacket = new DatagramPacket(sendData, sendData.length, clientIPAddress, port);   //******VURUŞUMUZ GÖNDERİLİYOR
                                        clientSocket.send(sendPacket);
                                        
                    //Client bekleniyor
                                        receivePacket = new DatagramPacket(receiveData,receiveData.length);   //*** VURUŞ SONRASI SERVERIN VERDİĞİ TEPKİ ALINIR
                                        clientSocket.receive(receivePacket);
                                        String hit = new String (receivePacket.getData());

                                                    if(hit.contains("Vurdun")){
                                                        System.out.print("atış isabetli");                  //************VURDUYSAK
                                                    }else if(hit.contains("gameover")){
                                                        System.out.print("Düşmanın son gemisini vurdun! Oyunu kazandın ^.^");            //******** OYUN BİTİREN VURUŞSA
                                                        break;
                                                    }/*else{                      //*****BUNU DAH GÜZEL OLSUN DİYE EKLEDİM AMA UMDUĞUM GİBİ ÇALIŞMADIĞI İÇİN DEVREDIŞI BIRAKTIM
                                                                System.out.print("olmadı  >.< ");
                                                        }*/
                                        System.out.println(" ");

                                        showBoard(board);
                                        clientSocket.send(sendPacket);    //// DATALARI YOLLUYO
                                        
                                        if(isGameOver()) running = false;                    ////OYUN BİTMEDİYSE DEVAM
                                    }
                        System.out.println("  Oyun bitti !!!  "); //// BİTTİYSE İLETİŞİMİ KESİYOR
                        clientSocket.close();
            }
    
    
    public static void breakLine(){
            System.out.println(" ");
            System.out.println("-------------------------------------------------------------------------------------------------");
            System.out.println(" ");
        }
    
    
    
    private static void createBoard(int[][] board){
                for(int row=0 ; row < /*board.length*/ 5; row++ ){
                    for(int column=0 ; column </*board.length*/5 ; column++ ){
                        board[row][column]=-1;
                         if(board[row][column] == -1){
                            System.out.print("\t"+"~"); 
                 }
            }
        }
    }
    
    
    private static void showBoard(int[][] board){
     
       breakLine();
       
         for (int row = 0; row < 5; row++) {
            
                for (int column= 0; column< 5 ; column++) {
                            switch (board[row][column]) {
                             case -1:
                                 System.out.print("\t"+" -- ");
                                 break;
                             case 0:
                                 System.out.print("\t"+" X ");
                                 break;
                             case 1:
                                 System.out.print("\t"+" O ");
                                 break;
                             default:
                                 break;
                          }

                   }
             System.out.println();
             System.out.println();
            }
                for (int row= 0; row < 5 ; row++) {

                    for (int column= 0;column < 5 ;column++) {

                 //      System.out.println( "  "+board[row][column] +"  ");

                    }
                } breakLine();  
      }     
               
             
 
    private static String shotOnBoard(int[] shot){
                if(board[shot[0]][shot[1]] == 1){

                    System.out.print("Gemilerinden biri vuruldu!");
                    board[shot[0]][shot[1]] = 0;

                    System.out.println(" ");
                    if(isGameOver()){
                        return "gameover";
                    }
                    return "vurdu";

                }else{

                    System.out.println(" Iska ");
                    return "kaçti";
                }
    }
  

    private static boolean setNewShip(int coordX, int coordY, int size, boolean isHorizontal){
         	 
        if(!isHorizontal){ 
        	//gemiyi dikeyde oluşturup oyun alanını aşmamasını sağlamak Y dikey
        	if((coordY+size)< /*board.length */5){
	    		//Seçilen pozisyonun uygun olup olmadığını kontrol edin
		    	for(int i = 0; i < size; i++){
		    		
		        	if(board[coordX][coordY+i] != -1){
		        		return false;
		        	}
		        }
		    	for(int i = 0; i < size; i++){
		    		board[coordX][coordY+i] = 1;  // gemiyi oluşturduk
		    	}
        	}else return false;
                
        }else{
            
        	//gemiyi yatayda oluşturup oyun alanını aşmamasını sağlamak X yatay
        	if((coordX+size)< /*board.length */5){
	    		//Seçilen pozisyonun uygun olup olmadığını kontrol edin
		    	for(int i = 0; i < size; i++){
		        	if(board[coordX+i][coordY] == -1){
		        		return false;
		        	}
		        }
		    	for(int i = 0; i < size; i++){
		    		board[coordX+i][coordY] = 1;
		    	}
        	}else return false;
        }
        return true;
    }
    
    private static void setRandomShip(){
    	Random randomize = new Random();
    	boolean tryAgain = false;
    	while(!tryAgain){
            
    		 int x = randomize.nextInt(/*board.length*/5);
                 int y = randomize.nextInt(/*board.length*/5);
                 boolean z = randomize.nextBoolean();
                 tryAgain = setNewShip(x, y, 2, z); // gemiyi bire tanımladığımız için gemiye bir yazdık
    	}
    }
  
    private static boolean isGameOver(){
    
    
    	for(int i=0;i < /*board.length*/5 ;i++){
    		for(int j=0;j < /*board.length*/5 ;j++){
    			if(board[i][j] == 1){
    				return false;
    			}
    		}
    	}
    return true;
    }

    public static byte[] intsToBytes(int[] ints) {
        
        ByteBuffer bb = ByteBuffer.allocate(ints.length * 4);
        IntBuffer ib = bb.asIntBuffer();
        for (int i : ints) ib.put(i);
        return bb.array();
    }

    public static int[] bytesToInts(byte[] bytes) {
        
        int[] ints = new int[bytes.length / 4];
        ByteBuffer.wrap(bytes).asIntBuffer().get(ints);
        return ints;
    }
    
    private static void waitYourTurn(){
        
        System.out.println("Lütfen rakibin hareketini bekleyin ...");
        System.out.println(" ");
    }
    
    private static void clearConsole(){  // iki el arasındaki boşluğu console u düzenlemeye yarar
        
        String n = "\r\n";
        for (int i = 0; i < 10; i++) {
            n = n+"\r\n";
        }
        System.out.println(n);
    }
}