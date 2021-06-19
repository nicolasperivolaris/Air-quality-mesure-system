#include <LiquidCrystal.h>
#include <DHT.h>
//#include <DHT_U.h>
#include <SoftwareSerial.h>
//#include <stdlib.h>

///////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////// INITIALISATION //////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////

unsigned long t_prec = 0;     //Pour géré la vitesse d'envoie des donnée

///////////////////////////// MODULE HUMIDITE/TEMPARATURE /////////////////////////////////

#define DHTTYPE DHT11    // Defini le type de capteur
int HC=2;                // Pin Humidity Captor
float hum;               // hum Variable dans laquelle on va placer la valeur de l'humidité relevé pas le capteur
float TC;                // TC Variable dans laquelle on va placer la valeur de la temperature relevé pas le capteur
DHT dht(HC, DHTTYPE);    // Initialize DHT sensor
  

/////////////////////////////// Parametrage de l'Ecran LCD //////////////////////////////////

LiquidCrystal lcd(4, 5, 6, 7, 8, 9);                      // Pins où on va connecter l'écran (RS, E, D4, D5, D6, D7)


////////////////////////// Parametrage entre et sortie Bluetooth ////////////////////////////

SoftwareSerial BTSerial(11, 12);                           // RX | TX


/////////////////////////////// Capteur de luminosité //////////////////////////////////////

int DL=3;                                                  // Pin DL output luminosity captor
int lumos;                                                 // lumos variable dans laquelle on va placer la valeur de la luminosité relevé pas le capteur,


////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// SETUP ///////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

void setup()
  {
  pinMode(A1, INPUT);                                            // Input luminosity sensor
  pinMode(HC, INPUT);                                            // Input humidity/Temperature sensor
  pinMode(DL, OUTPUT);                                           // Output diode luminosity sensor

  dht.begin();                                                   // Initialise module humidite
  Serial.begin(9600);                                            // Ouvre la voie série avec l'ordinateur
  lcd.begin(16, 2);                                              // Initialise l'écran LCD


////////////////////////////////Parametrage du Bluetooth/////////////////////////////////////

  pinMode(10, OUTPUT);                                          // Met l'entrer de validation en haut pour passer en mode AT Commande
  digitalWrite(10, HIGH);
  //Serial.println("Enter AT commands:");
  BTSerial.begin(9600);                                         // HC-05 default speed in AT command mode
}


///////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////// LOOP ////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////

void loop() 
  {
    
  hum=dht.readHumidity();                                               // La varialble hum, prend une valeur en pourcentage relever par le capteur d'humidite
  
  TC=dht.readTemperature();                                             // La varialble TC, prend une valeur en degré relever par le capteur de temperature

  lumos=analogRead(A1);                                                 // La varialble lumos, prend une valeur en Lux relever par la photoresistance

///////////////////////// Led Branche sur sotie PWN + Capteur de Luminosité ///////////////

if( lumos <= 100)
  digitalWrite(DL,255);
  
if(lumos>100 && lumos<300)
  digitalWrite(DL,50);

if(lumos>=300)
  digitalWrite(DL,0);
  
  
///////////////////////// Ecriture et Affichage sur l'ecran LCD ////////////////////////////

  lcd.setCursor(0, 0);                                    //Place le pointeur sur la colonne 0, ligne 0
  lcd.print("T: " + String(TC) + "C");                    //écrit le message 
  lcd.setCursor(0, 1);                                    //Place le pointeur sur la colonne 0, ligne 1
  lcd.print("Humdite: " + String(hum) + "%");             //écrit le message
  //lcd.clear();                                          //efface le message affiché  


///////////////////////// Ecriture et lecture par le Bluetooth ////////////////////////////

   String HM = String(hum, 2);
   String T = String(TC,2);
   String LUX = String(lumos);


    String test ="type\n";
    int i=0;
    while(i<10)
    {
    test.concat("temp\n");
    test.concat(T + "\n");
    test.concat("data\n");
    
    test=test+test;
    i++;
    }
    BTSerial.print(test);            //transmet les data qui doivent être envoyer au bluetooth qui sont trasmit

    Serial.print(test);    
}
