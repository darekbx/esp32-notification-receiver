#include <M5StickC.h>

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

int batteryLevelOffset = 12;

static uint16_t color16(uint16_t r, uint16_t g, uint16_t b) {
  uint16_t _color;
  _color = (uint16_t)(r & 0xF8) << 8;
  _color |= (uint16_t)(g & 0xFC) << 3;
  _color |= (uint16_t)(b & 0xF8) >> 3;
  return _color;
}

void setup() {
  M5.begin();
  
  initBLE();
  
  pinMode(M5_BUTTON_HOME, INPUT_PULLUP);
  pinMode(M5_LED, OUTPUT);

  digitalWrite(M5_LED, HIGH);
  

  M5.Axp.ScreenBreath(8);
}

void loop() {
  M5.Lcd.setTextColor(WHITE);
  
  displayBatteryLevel();
  
  M5BLEloop();
  
  delay(150);
}

void handleInputData(String data) {
  for (int i = 0; i < 4; i++){
    digitalWrite(M5_LED, LOW);
    delay(200);
    digitalWrite(M5_LED, HIGH);
    delay(200);
  }
  M5.Lcd.setCursor(1, 20, 1);
  M5.Lcd.print(data);  
}
