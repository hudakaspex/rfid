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

    rfidPlugin.addListener('tagReceived', (tag) => {
      console.log('Tag received:', tag);
    });
  }
}
