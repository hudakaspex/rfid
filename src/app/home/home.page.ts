import { Component } from '@angular/core';
import { IonHeader, IonToolbar, IonTitle, IonContent } from '@ionic/angular/standalone';
import { rfidPlugin } from '../plugins/rfid-plugin';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
  imports: [IonHeader, IonToolbar, IonTitle, IonContent],
})
export class HomePage {
  constructor() {
    this.initRFID();
  }

  async initRFID() {
  const result = await rfidPlugin.startReader({
    serialPort: "/dev/ttyS0",
    baudRate: 115200,
  });
  console.log("Connected:", result.connected);

  // Listen for tag reads
  rfidPlugin.addListener("onRFIDRead", (data: any) => {
    console.log("Tag Read:", data.epc, "RSSI:", data.rssi);
  });
}

async adjustPower() {
  await rfidPlugin.setPower({ power: 80 });
  const power = await rfidPlugin.getPower();
  console.log("Current Power:", power.power);
}
}
