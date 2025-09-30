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
  const serialPorts = await rfidPlugin.getSerialPorts();
  console.log("Available Serial Ports:", serialPorts);
  const path = serialPorts.ports?.length > 0 ? serialPorts.ports[0]?.path : "/dev/tty";
  const result = await rfidPlugin.startReader({
    serialPort: path,
    baudRate: 115200,
  });
  console.log("Connected:", result.connected);
  if (result.connected) {
    await this.adjustPower();
  }


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
