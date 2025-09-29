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
    console.log('rfidPlugin', rfidPlugin);
    rfidPlugin.helloWorld().then((result) => {
      console.log(result);
    });

    rfidPlugin.initialize()
    .then(() => {
      console.log('RFID plugin initialized');
      rfidPlugin.connect().then(() => {
        console.log('Connected to RFID reader');
        rfidPlugin.startInventory();
      });
    });

    rfidPlugin.addListener('tagReceived', (tag) => {
      console.log('Tag received:', tag);
    });
  }
}
