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

  M5.Axp.ScreenBreath(8);
}

void loop() {
  M5.Lcd.setTextColor(WHITE);
  
  displayBatteryLevel();
  
  M5BLEloop();
  
  delay(150);
}

void handleInputData(String data) {
  M5.Lcd.fillRect(0, 18, 80, 160, WHITE);
  delay(500);
  M5.Lcd.fillRect(0, 18, 80, 160, BLACK);
  M5.Lcd.setCursor(1, 20, 1);
  M5.Lcd.print(data);  
}
