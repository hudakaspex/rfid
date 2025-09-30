export interface RfidPluginDefinition {
  startReader(options: {
    serialPort: string;
    baudRate: number;
  }): Promise<{ connected: boolean }>;
  stopReader(): Promise<void>;
  getPower(): Promise<{ power: number }>;
  setPower(options: { power: number }): Promise<void>;
  addListener(
    eventName: 'onRFIDRead',
    listenerFunc: (data: { epc: string; rssi: number }) => void
  ): Promise<void>;

  getSerialPorts(): Promise<{ ports: { name: string; path: string }[] }>;
}
