import {GeolocationPlugin} from '@capacitor/core';

declare global {
  interface PluginRegistry {
    FusedLocation?: GeolocationPlugin;
  }
}
