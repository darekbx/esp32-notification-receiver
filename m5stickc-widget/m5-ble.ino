
BLEServer *pServer = NULL;
BLECharacteristic * pTxCharacteristic;
bool deviceConnected = false;
bool oldDeviceConnected = false;
uint8_t txValue = 0;

#define SERVICE_UUID           "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
#define CHARACTERISTIC_UUID_RX "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
#define CHARACTERISTIC_UUID_TX "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"

class M5ServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      Serial.println("BT connected");
      deviceConnected = true;
      setConnected();
    };

    void onDisconnect(BLEServer* pServer) {
      Serial.println("BT disconnected");
      deviceConnected = false;
      setDisconnected();
    }
};

class M5ReceiveCallback: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string rxValue = pCharacteristic->getValue();
      char *cstr = new char[rxValue.length() + 1];
      strcpy(cstr, rxValue.c_str());

      Serial.println("Received data");
      Serial.println(cstr);
      handleInputData(cstr);
    }
};

void initBLE() {
  Serial.println("initBLE");
  BLEDevice::init("M5StickC Widget");
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new M5ServerCallbacks());
  
  BLEService *pService = pServer->createService(SERVICE_UUID);
  
  pTxCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID_TX,
                      BLECharacteristic::PROPERTY_NOTIFY | 
                      BLECharacteristic::PROPERTY_READ
                    );
                      
  pTxCharacteristic->addDescriptor(new BLE2902());
  
  BLECharacteristic *pRxCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID_RX,
                                         BLECharacteristic::PROPERTY_WRITE
                                       );
 
  pRxCharacteristic->setCallbacks(new M5ReceiveCallback());

  pService->start();
  pServer->getAdvertising()->start(); 
}

void M5BLEloop() {
  if (deviceConnected) {
    // writeValue("Out data");
  }
}

void writeValue() {
    pTxCharacteristic->setValue("value");
    pTxCharacteristic->notify();
}
