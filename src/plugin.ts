import {GeolocationOptions, GeolocationPlugin, GeolocationPosition, PluginListenerHandle, Plugins} from '@capacitor/core';

const {FusedLocation} = Plugins;

export class FusedLocationPlugin implements GeolocationPlugin {
    getCurrentPosition(options?: GeolocationOptions): Promise<GeolocationPosition> {
        return FusedLocation.getCurrentPosition(options);
    }

    watchPosition(options: GeolocationOptions, callback: (position: GeolocationPosition, err?: any) => void): string {
        return FusedLocation.watchPosition(options, callback);
    }


    clearWatch(options: { id: string }): Promise<void> {
        return FusedLocation.clearWatch(options);

    }

    addListener(eventName: string, listenerFunc: Function): PluginListenerHandle {
        return FusedLocation.addListener(eventName, listenerFunc);

    }

}
