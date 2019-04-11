import {CallbackID} from '@capacitor/core';

declare global {
  interface PluginRegistry {
    FusedLocation?: FusedLocationPlugin;
  }
}

export interface FusedLocationPosition {
  coords: {
    latitude: number;
    longitude: number;
    accuracy: number;
    altitude?: number;
    speed?: number;
    heading?: number;
  };
}

export interface FusedLocationOptions {
  enableHighAccuracy?: boolean; // default: false
  timeout?: number; // default: 10000,
  maximumAge?: number; // default: 0
  requireAltitude?: boolean; // default: false
}

export type FusedLocationWatchCallback = (position: FusedLocationPosition, err?: any) => void;

export interface FusedLocationPlugin {
  getCurrentPosition(options?: FusedLocationOptions): Promise<FusedLocationPosition>;

  watchPosition(options: FusedLocationOptions, callback: FusedLocationWatchCallback): CallbackID;

  clearWatch(options: { id: string }): Promise<void>;
}
