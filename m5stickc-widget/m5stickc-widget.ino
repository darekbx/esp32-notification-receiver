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
  M5.Lcd.setRotation(3);
}

void loop() {
  M5.Lcd.setTextColor(WHITE);
  
  displayBatteryLevel();
  
  M5BLEloop();

  if (digitalRead(M5_BUTTON_HOME) == LOW) {
    resetNotification();
  }
  
  if (digitalRead(BUTTON_B_PIN) == LOW) {
    ESP.restart();
  }
  
  
  delay(100);
}

void resetNotification() {
  M5.Lcd.fillRect(0, batteryLevelOffset + 1, 160, 80, BLACK);
}

void handleInputData(String data) {
  for (int i = 0; i < 4; i++){
    digitalWrite(M5_LED, LOW);
    delay(200);
    digitalWrite(M5_LED, HIGH);
    delay(200);
  }
  resetNotification();
  
  if (data.indexOf(',') == -1) {
    M5.Lcd.setCursor(1, 20, 4);
    M5.Lcd.print(data);  
  } else {
    int first = data.indexOf(',');
    String application = data.substring(0, first);
    int second = data.indexOf(',', first + 1);

    String title = "";
    String subTitle = "";

    if (second == -1) {
        title = data.substring(first + 1);
    } else {
        title = data.substring(first + 1, second);
        subTitle = data.substring(second + 1);
    }
        
    M5.Lcd.setCursor(1, 20, 4);
    M5.Lcd.print(application);
    
    M5.Lcd.setCursor(1, 42, 2);
    if (title.length() > 20) {
      title = title.substring(0, 20) + "...";
    }
    M5.Lcd.print(title);

    if (subTitle.length() > 0) {
      M5.Lcd.setCursor(1, 58, 2);
      M5.Lcd.print(subTitle);
    }
  }
}
