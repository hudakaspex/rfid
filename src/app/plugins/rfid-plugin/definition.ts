export interface RfidPluginDefinition {
  helloWorld(): Promise<string>;
  initialize(): Promise<void>;
  connect(): Promise<{ connected: boolean }>;
  startInventory(): Promise<void>;
  stopInventory(): Promise<void>;
  addListener(
    eventName: 'tagReceived',
    listenerFunc: (tag: {
      epc: string;
      rssi: number;
      antenna: number;
      frequency: number;
    }) => void
  ): Promise<void>;
  addListener(
    eventName: 'usbPermission',
    listenerFunc: (info: { granted: boolean }) => void
  ): Promise<void>;
}
