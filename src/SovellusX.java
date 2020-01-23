import java.util.*;
import java.net.*;
import java.io.*;

public class SovellusX extends Thread {

     public static void main(String[] args) throws Exception {
    	 
    	 LaskeLuvut laskuri = null;		//LASKELUVUT LUOKKAINEN laskuri LUODAAN
         Socket Soketti = Yhdistä();		//YHDISTÄ METODI LUO YHTEYDEN JA LÄHETTÄÄ HALUTUN PAKETIN
         									//JONKA AVULLA LUODAAN STREAMIT
         ObjectInputStream inpStream = new ObjectInputStream(Soketti.getInputStream());
         ObjectOutputStream outpStream = new ObjectOutputStream(Soketti.getOutputStream());
         Soketti.setSoTimeout(50000);
         int porttienMäärä=lueStream(inpStream, outpStream);	//NAPATAAN VIRRASTA HALUTTUJEN SOKETTIEN MÄÄRÄ
         int[] portnumbers =new int[porttienMäärä];				//LUODAAN NIILLE LISTA 
         ArrayList<Socket> sok = new ArrayList<Socket>();		//SOKETEILLE LISTA
         boolean yhteys=true;
         int kysymys= 0;		
         
         if (porttienMäärä == -1) {		//JOS STREAMI LÄHETTÄÄ -1 NIIN SULJETAAN YHTEYS
        	 Soketti.close();
        	 return;}
         else {							//LUODAAN PORTIT JOTKA LÄHETETÄÄN SERVERILLE
        	 for (int i = 0; i < porttienMäärä; i++ ) {
        		 
        		 try{
        			 int p = 1234+i;
        			 portnumbers[i]=p;
        			 outpStream.writeInt(portnumbers[i]);
        			 outpStream.flush();
        		 
        		 } catch (Exception e) {
        			 return;
        		 }
        	 }
         }
         								//LUODAAN SOKETIT JA YHDISTETÄÄN NE SERVERIIN JA TALLENNETAAN
         for (int i = 0; i<porttienMäärä; i++) {
         		ServerSocket ServSoket = new ServerSocket(portnumbers[i]);
         		ServSoket.setSoTimeout(5000);
				Socket soket = new Socket();
				soket = ServSoket.accept();
				sok.add(i, soket);
				ServSoket.setSoTimeout(5000);
				ServSoket.close();
         }
         								//LISTA-LUOKAN METODI JOKA LISÄÄ PAIKKOJA SIELLÄ OLEVAAN LISTAAN,
         								//VASTAANOTTAVILLE SOKETEILLE JOHON NE TALLENTAVAT LUVUT
         Lista.kasvatalistaa(porttienMäärä); 
         								//KÄYNNISTETÄÄN JOKAISELLE PAIKALLE OMA LASKURI
         for (int paikka = 0; paikka<porttienMäärä; paikka++) {
        	 laskuri= new LaskeLuvut(paikka, sok);
        	 laskuri.start();}
         							
       
         while(yhteys){
        	 kysymys=inpStream.readInt();
        	 							//LUETAAN STREAMIN KYSYMYKSET JA OTETAAN LISTASTA OIKEA VASTAUS
        	 if (kysymys==1){
        		 
        		 Thread.sleep(300);		//JOTTA LISTA OLISI KYSYMYKSEN KANSSA SAMALLA TASOLLA
        		 int sum=0;
        		 sum =Lista.koksum();
        		 outpStream.writeInt(sum);
        		 outpStream.flush();
        		 System.out.println("Kysymys: 1 ,Vastaus: "+sum);
        	 }else if (kysymys==2){
        		 Thread.sleep(300);
        		
        		 outpStream.writeInt(Lista.isoinSumma());
        		 outpStream.flush();
        		 System.out.println("Kysymys: 2 ,Vastaus: "+Lista.isoinSumma());
        		 
        	 }else if (kysymys==3) { 
        		 Thread.sleep(300);
        		 outpStream.writeInt(Lista.returnkaikki());
        		 outpStream.flush();
        		 System.out.println("Kysymys: 3 ,Vastaus: "+Lista.returnkaikki());
        	 }else if (kysymys==0) {
        		 System.out.println("Sulkemispyyntö");
        		 yhteys=false;
         		 Soketti.close();			//SULJETAAN KAIKKI MAHDOLLISET SOKETIT JA laskuri	
         		 outpStream.close();
         		 inpStream.close();
         		 laskuri.close();
         		 break;
         		 
        	 }
         }
     }
     	 
   
 		
     
     
     public static class LaskeLuvut extends Thread{
    	 //laskuri KÄYTTÄÄ TÄTÄ LUOKKAA JA NÄITÄ MUUTTUJIA
    	 private int paikka;
    	 private int saatuLuku=0;
    	 Lista lis=new Lista();
    	 ArrayList<Socket> sok ;
		 boolean c;
		 private Socket soketti;
		 private ObjectInputStream inStream;
		
 			LaskeLuvut(int paikka,  ArrayList<Socket> sok){
 				this.sok=sok;
 				this.paikka=paikka;			
 			}
 			
 			
 			public void run()  {
	 			try {
	 				boolean c = true;
 					soketti = sok.get(paikka);
 					InputStream uS = soketti.getInputStream();
            		inStream = new ObjectInputStream(uS);
            		//OTTAA SOKETTI LISTALTA OIKEALTA PAIKALTA SOKETIN JA VASTAANOTTAA SIELLÄ OLEVIA LUKUJA
            		while (c) { 
            			saatuLuku=inStream.readInt();
            			
            			if (saatuLuku==0) {
            				inStream.close();
            				break;}
            			
            			lis.lisää(paikka,saatuLuku); //lis ON TAAS YHTEYS LISTA LUOKKAAN
            			lis.setkaikki();
            		}
		
	 			}catch (Exception e) {
	 				System.out.println(e);
	 				//KUN SOVELLUS SULKEUTUU NIIN EI EHDI SULKEMAAN KAIKKIA PORTTEJA/STREAMEJA
	 				// VAAN OSA "KAATUU"
	 			}	
 			}	
 		
 		public void close() throws IOException {
 			c=false;
 			soketti.close();
 			inStream.close();
 		}
     }
 		
	private static Socket Yhdistä() throws IOException {
		//YHDISTÄ METODI YHDISTYY SERVERIN PORTTIIN
		int porttiNo = 1234;
		int yrityskerta = 0;
		ServerSocket Ssocket = new ServerSocket(porttiNo);
		Socket soketti = new Socket();
		
		while (yrityskerta < 5) {
			try {
				sendUDP(); // LÄHETTÄÄ PAKETIN
				Ssocket.setSoTimeout(50000); 
				soketti = Ssocket.accept();
				Ssocket.close();
				System.out.println("TCP muodostettu");
				return soketti;
			} catch (SocketException e) {
				yrityskerta++;
				System.out.println("Ei onnistunut");
			}
		} 
		return null;
	} 
	private static void sendUDP() {
		try {	//LUODAAN PAKETTI JA YHDISTETÄÄN SERVERIN OIKEAAN PORTTIIN
			InetAddress targetAddress = InetAddress.getLoopbackAddress();
	        DatagramSocket socket = new DatagramSocket(); 
	        byte[] data = Integer.toString(1234).getBytes(); 
	        DatagramPacket packet = new DatagramPacket (data, data.length, targetAddress, 3126); 
	        socket.send(packet); 
	        socket.close();
	    } catch (Exception e) {
			e.printStackTrace();
		}
	}
	//LUKEE PORTTIEN MÄÄRÄN
	private static int lueStream(ObjectInputStream i, ObjectOutputStream o) {
		try {
			return i.readInt();
		} catch (Exception e){
			try {
				o.writeInt(-1);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return -1;
		}
		
		
	}
	public static class Lista{
		//LISTA LUOKKA PERUSTUU SIELLÄ OLEVAAN LISTAAN JOHON TALLENNETAAN LUKUJEN SUMMA
		
		static ArrayList<Integer> lista = new ArrayList<Integer>();
		static int laskuri=0;
		static int kokonaisSumma=0;
		 
		//LISÄKSI SAATETAAN KYSYÄ KAIKKIEN NRO- MÄÄRÄÄ TAI SUMMAA
		
		public static ArrayList<Integer> getluvut(){
			return lista;
			
		}
		//METODISSA LISÄTÄÄN LISTAN SAMALLE PAIKALLE SAMAN laskurin SAAMAT LUVUT JA LUKU LISÄTÄÄN KOKONAISSUMMAAN
 		public void lisää(int ID, int l) {
 			int summa = l;
 			summa=summa+lista.get(ID);
 			lista.set(ID, summa);
 			kokonaisSumma=kokonaisSumma+l;
 			System.out.println(lista+"  Tässä on lisätty: "+l+"  paikalle: "+ID);
 	
 		}
 		public static void kasvatalistaa(int x) { //METODI LOI LISTALLE JOKAISELLE PORTILLE OMAN PAIKAN
 			for(int i=0; i<x;i++) {
 				int paikka=0;
 				lista.add(paikka);
 			}
 			
 		}
 		
 		public static int isoinSumma() {
 			int suurin=0;
 			int tmp=lista.get(0);
 			for(int i=0; i<lista.size(); i++) { //METODI VALITSEE SUMMISTA SUURIMMAN JA LÄHETTÄÄ OIKEAN VASTAUKSEN
 				if (lista.get(i)>=tmp) {	//JOS SYÖTTEESSÄ ON 2 YHTÄSUURTA LÄHETTÄÄ SEN JOKA ON INDEKSILTÄÄN SUUREMPI
 												//JOKA TAAS ON VÄÄRÄ VASTAUS
 					tmp=lista.get(i); 
 					suurin=i+1;
 				}
 			}
 			return suurin;
 		}
 		//KASVATTAA LASKURIA JOKAISEN LISÄTYN LUVUN JÄLKEEN
 		public static void setkaikki() {
 			laskuri++;	
 		}
 		public static int returnkaikki() {
 			return laskuri;
 		}
 		public static int koksum() {
 			return kokonaisSumma;
 		}
	}
	
}

