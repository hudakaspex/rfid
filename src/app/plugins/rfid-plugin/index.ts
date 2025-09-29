import { registerPlugin } from '@capacitor/core';
import type { RfidPluginDefinition } from './definition';

const rfidPlugin = registerPlugin<RfidPluginDefinition>('OrcaRfidReader');
export * from './definition';
export { rfidPlugin };