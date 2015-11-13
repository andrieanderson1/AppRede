//************** Lista de Comandos *************************
String COMANDO_LIGAR_CARRO = "l";
String COMANDO_DESLIGAR_CARRO = "d";
String COMANDO_ABASTECER = "ab";
String COMANDO_ACELERAR = "ac";
String COMANDO_FREAR = "fr";
String COMANDO_ALARME = "a";
//************** Leds e Sensor Temp ************************
int ledBranco = 13;
int ledAmarelo = 12;
int ledVermelho = 11;
int ledVerde = 10;
const int LM35 = 0;
int ADClido = 0;

//*************** Variaveis ******************************** 
float velocidade = 0;
float temperatura = 0;
float distancia = 0;
float distanciaTotal = 0;
float tempo = 0;
float temporario = 0;
float tp = 0;
float hora = 0;
int litros = 0;
boolean carroLigado  = false;
boolean alertaCombustivel = false;

String message;
String comandoEnviado;           //Valor enviado via bluetooth
char lastValue;

void setup(){
// initialize the digital pin as an output:
pinMode(ledBranco, OUTPUT);
pinMode(ledAmarelo, OUTPUT);
pinMode(ledVermelho, OUTPUT);
pinMode(ledVerde, OUTPUT);
analogReference(INTERNAL);
Serial.begin(9600);

}
void loop(){

  if(Serial.available())
  {//if there is data being recieved
    comandoEnviado=Serial.readString(); //Ler comando enviado por bluetooth
  }
  
  if (comandoEnviado == COMANDO_LIGAR_CARRO){
    if(temCombustivel()){
      carroLigado = true;
      Serial.println("Carro foi ligado");
    }else{
      Serial.println("Tanque vazio");
    }
  }else if(comandoEnviado == COMANDO_DESLIGAR_CARRO && carroLigado){
     carroLigado = false;
     Serial.println("Carro foi desligado");
  }else if(comandoEnviado.startsWith(COMANDO_ABASTECER)){
      String comando = comandoEnviado;
      comando.replace(COMANDO_ABASTECER, "");
      Serial.println(comando);
      litros = comando.toInt();
  }else if(comandoEnviado == COMANDO_ACELERAR && carroLigado){
    if(velocidade < 180){  
      Serial.println("acelerou");
      velocidade += 20.0;
    }
  }else if(comandoEnviado == COMANDO_FREAR && carroLigado){
    if(velocidade > 0){
      Serial.println("freou");
      velocidade -= 20.0;
    }
  }


  if(!temCombustivel()){
    carroLigado = false;
    if(alertaCombustivel){
       digitalWrite(ledAmarelo, HIGH);
       alertaCombustivel = false;
    }else{
       digitalWrite(ledAmarelo, LOW);
      alertaCombustivel = true;
    }
  }else{
     digitalWrite(ledAmarelo, LOW);
  }
  
  if(carroLigado){
    digitalWrite(ledVerde, HIGH);
    digitalWrite(ledVermelho, LOW);
    
    ADClido = analogRead(LM35);
    temperatura = ADClido * 0.1075268817204301;
    
    if(velocidade > 0){ 
      distancia = (distancia + 1)*1.3;
      litros= litros-1;
    }
  }else if(!carroLigado){
    digitalWrite(ledVermelho, HIGH);
    digitalWrite(ledVerde, LOW);
    velocidade =0;
  }
  
  comandoEnviado = "";
  informacaoParaEnviar();
  Serial.flush();
  delay(1000); //Atualiza valores a cada 1s
}

/*
float calcularLitrosNoTanque(){
  if(distancia > 0){
    return 20/distancia;
  }
  return litros;
}

float calcularDistancia(){
  //tp = hora
  return velocidade * tp;
}

void atualizarCombustivelNoTanque(){
  litros = distancia/litros;
}*/

void informacaoParaEnviar(){
  message="i;";
   message+=carroLigado ?"1":"0";
  message += ";";
  message+=litros;
  message += ";";
  message+=velocidade;
  message += ";";
  message+=distancia;
  message += ";";
  message+=temperatura;
  message += ";f";
  Serial.print(message);
  message="";
}


boolean temCombustivel(){
  return litros > 0;
}
