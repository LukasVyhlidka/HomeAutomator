#include <ArduinoJson.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>

// Include API-Headers
extern "C" {
//#include "ets_sys.h"
//#include "os_type.h"
//#include "osapi.h"
//#include "mem.h"
#include "user_interface.h"
//#include "cont.h"
}

/** For how long to sleep the chip. */
#define SLEEP_SECONDS 120

/* Where in RTC memory to store the deep sleep indication */
#define ESP_RTC_ADDRESS_DEEP_SLEEP_INDICATOR 64

/* PINs of the indication LED diodes */
#define BLUE_LED D2
#define GREEN_LED D1
#define RED_LED D5

/* Wifi connection settings */
const char* ssid = "<ssid>";
const char* ssidPass = "<wifiPass>";

/* Wifi Static configuration. It can be turned off (to obtain it from DHCP). */
#define WIFI_STATIC_CONF false
#define WIFI_STATIC_CONF_IP IPAddress(192, 168, 95, 5)
#define WIFI_STATIC_CONF_DNS IPAddress(192, 168, 95, 254)
#define WIFI_STATIC_CONF_GW IPAddress(192, 168, 95, 254)

const String serverUrl = "http://192.168.95.250:10000/boilers/TestBoiler";

unsigned long startTime;

void setup() {
    startTime = micros();

    Serial.begin(9600);
    //Serial.setDebugOutput(true);

    //handleDeepSleep();

    pinMode(LED_BUILTIN, OUTPUT);
    pinMode(BLUE_LED, OUTPUT);
    pinMode(GREEN_LED, OUTPUT);
    pinMode(RED_LED, OUTPUT);

    digitalWrite(BLUE_LED, LOW);
    digitalWrite(GREEN_LED, LOW);
    digitalWrite(RED_LED, LOW);

    long start = micros();
    bool conned = connectWifi();
    printTime(start, micros(), "Wifi Connect Time");
    
    if (! conned) {
      Serial.println("Wifi connection error. Going to sleep.");
      signalErrorBlick();
      deepSleep(SLEEP_SECONDS);
    }

}

/**
 * Main loop of the program.
 * It connects to the server and figures out whether the Relay should be on or off and do according action.
 */
void loop() {
    Serial.println("loop");

    if (WiFi.status() == WL_CONNECTED) {

        HTTPClient http;

        Serial.print("[HTTP] begin...\n");
        http.begin(serverUrl);

        Serial.print("[HTTP] GET...\n");
        // start connection and send HTTP header
        int httpCode = http.GET();

        // httpCode will be negative on error
        if(httpCode > 0) {
            // HTTP header has been send and Server response header has been handled
            Serial.printf("[HTTP] GET... code: %d\n", httpCode);

            // file found at server
            if(httpCode == HTTP_CODE_OK) {
                String payload = http.getString();
                processServerResponse(payload);
            } else {
              Serial.printf("[HTTP] GET... failed, status: %s\n", http.errorToString(httpCode).c_str());
              signalErrorBlick();
            }
        } else {
            Serial.printf("[HTTP] GET... failed, error: %s\n", http.errorToString(httpCode).c_str());
            signalErrorBlick();
        }

        http.end();
    }

    batteryCheck();

    printTime(startTime, micros(), "Cycle took time");

    deepSleep(SLEEP_SECONDS);
}

void printTime(long uStart, long uEnd, String msg) {
  long uDur = uEnd - uStart;
  double dur = uDur / 1000.0 / 1000.0;
  Serial.println(String(dur) + " - " + msg);
}

void batteryCheck() {
  // Voltage divider
  int analogValue = analogRead(A0);

  float voltage = analogValue * (13.2 / 1023);
  Serial.println((String)"Battery level: " + voltage + " V");

  if (voltage <= 3.6) {
    signalWarnBlick();
    //signalErrorBlick();
  }
}

/**
 * Processes the server response. It parses the JSON response and do an action according to it.
 */
void processServerResponse(String data) {
  Serial.println("data: " + data);

  StaticJsonBuffer<200> jsonBuffer;
  JsonObject& root = jsonBuffer.parseObject(data);

  if (!root.success()) {
    Serial.println("Data JSON processing failed.");
    return;
  }

  String id = root["id"];
  String state = root["state"];

  if (state == "SWITCHED_OFF") {
    Serial.println("It is OFF");
    signalRelayState(false);
  } else if (state == "SWITCHED_ON") {
    Serial.println("It is ON");
    signalRelayState(true);
  } else {
    Serial.println("Unknown state: "+ state);
    signalErrorBlick();
  }
}

/**
 * Sets the diodes to the state of either open relay or closed relay.
 * This is temporal indication because I do not have the relay so far :)
 */
void signalRelayState(bool open) {
  int pin;
  if (open) {
    pin = GREEN_LED;
  } else {
    pin = BLUE_LED;
  }

  digitalWrite(pin, HIGH);
  delay(25);
  digitalWrite(pin, LOW);

  /*for (int i = 0; i < 3; i++) {
    digitalWrite(pin, HIGH);
    delay(400);
    digitalWrite(pin, LOW);
    delay(100);
  }*/
}

/**
 * Connect the Wifi
 */
bool connectWifi() {
    int connWait = 10; //seconds
    int delayMs = 50; //ms
    int ticksWait = connWait * 1000 / delayMs;

    Serial.println("\nChecking the wifi.");

    if (WiFi. SSID() != ssid) {
      Serial.println("\nConnecting the wifi.");
      WiFi.disconnect();
      WiFi.mode(WIFI_OFF);
      WiFi.mode(WIFI_STA);

      if (WIFI_STATIC_CONF) {
        //Do static configuration
        WiFi.config(WIFI_STATIC_CONF_IP, WIFI_STATIC_CONF_DNS, WIFI_STATIC_CONF_GW);
      }

      WiFi.begin(ssid, ssidPass);
    }

    int i = 0;
    while (ticksWait-- > 0 && WiFi.status() != WL_CONNECTED) {
      Serial.print(".");
      Serial.print(WiFi.status());

      if ((i++ % 10) == 0) {
        digitalWrite(LED_BUILTIN, LOW);
        delay(5);
        digitalWrite(LED_BUILTIN, HIGH);
      }

      delay(delayMs);
    }

    if (WiFi.status() != WL_CONNECTED) {
      Serial.println("WiFi not connected");
      return false;
    }

    Serial.println("");
    Serial.println("WiFi connected");
    Serial.println("IP address: ");
    Serial.println(WiFi.localIP());
    return true;
}

/**
 * After a WAKE_RF_DISABLED deep sleep, the wifi module is somehow turned off and can be turned on
 * only by another DeepSleep in mode WAKE_RF_DEFAULT.
 *
 * This method uses the boolean value stored in RTC memory (that is not wiped out after the sleep)
 * to figure out whether there is need to do this WAKE_RF_DEFAULT sleep to turn the wifi back on or not.
 */
void handleDeepSleep() {
  bool deepSleeped = false;
  system_rtc_mem_read(ESP_RTC_ADDRESS_DEEP_SLEEP_INDICATOR, &deepSleeped, 1);
  if (deepSleeped) {
    Serial.println("Last sleep was a deep one. Let's resleep to make Wifi back online.");

    deepSleeped = false;
    system_rtc_mem_write(ESP_RTC_ADDRESS_DEEP_SLEEP_INDICATOR, &deepSleeped, 1);

    ESP.deepSleep(1, WAKE_RF_DEFAULT);
  } else {
    Serial.println("Last sleep was not a deep one.");
  }
}

/**
 * Makes a Deep Sleep. Stores the boolean value into the RTC memory to be able to turn the wifi back on
 * again (take a look on handleDeepSleep method that reads that value).
 */
void deepSleep(int seconds) {
  bool deepSleeped = true;
  system_rtc_mem_write(ESP_RTC_ADDRESS_DEEP_SLEEP_INDICATOR, &deepSleeped, 1);

  //ESP.deepSleep(seconds * 1000000, WAKE_RF_DISABLED);
  ESP.deepSleep(seconds * 1000000);
}

void signalWarnBlick() {
  for (int i = 0; i < 3; i++) {
    blick(RED_LED, 50);
    delay(50);
  }
}

/**
 * In case of an error, blink with the RED diode. SOS Morse Code
 */
void signalErrorBlick() {
  // S morse code


  for (int i = 0; i < 3; i++) {
    blick(RED_LED, 50);
    delay(100);
  }

  delay(100);

  // O morse code
  for (int i = 0; i < 3; i++) {
    blick(RED_LED, 150);
    delay(100);
  }

  delay(100);

  // S morse code
  for (int i = 0; i < 3; i++) {
    blick(RED_LED, 50);
    delay(100);
  }
}

void blick(int pin, int onTime) {
  digitalWrite(pin, HIGH);
  delay(onTime);
  digitalWrite(pin, LOW);
}
