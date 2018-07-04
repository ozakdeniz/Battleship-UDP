/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networkhw1udp;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import NetworkHw1Udp.BattleShipUdpHw1;
import static NetworkHw1Udp.BattleShipUdpHw1.gameClient;
import static NetworkHw1Udp.BattleShipUdpHw1.gameServer;
import java.io.IOException;

/**
 *
 * @author Ozgur and Seyma
 */
public class Mainclass {

    public static void main(String[] args) throws Exception {
                try{

                             BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                             System.out.println("server icin 1 e basiniz, client icin 2 e basiniz...");

                             int selection = Integer.parseInt(input.readLine()); //input kullanıcıdan alınan girdiyi selectiona eşitledik

                             if(selection == 2) {  // 2 girerse cliente yönleniyor
                                 gameClient();
                             }
                             if(selection == 1){  // 1 girerse servera yönleniyor
                                 gameServer();
                             }
                }catch(IOException e){
                             System.out.println(" Hatalı seçenek girdiniz! "); 
                 }    
        }
}
