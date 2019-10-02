# capacitor-fused-location

<p align="center">
    <a href="https://www.npmjs.com/package/@jonoj/capacitor-fused-location">
        <img src="https://badge.fury.io/js/%40jonoj%2Fcapacitor-fused-location.svg" alt="npm version" />
    </a>
        <a href="https://snyk.io/test/npm/%40jonoj%2Fcapacitor-fused-location">
            <img src="https://snyk.io/test/npm/%40jonoj%2Fcapacitor-fused-location/badge.svg" alt="Vulnerabilities" />
        </a>
    <a href="https://opensource.org/licenses/MIT">
        <img src="https://img.shields.io/badge/License-MIT-GREEN.svg" alt="License" />
    </a>
</p>

## Description

Geolocation plugin that uses the fused location service instead of the native API.

Getting a location under android is quite difficult. The standard API implemented now in capacitor returns the GPS provider which results in never getting a position indoors. This is not the case under iOS. A better way under Android is the [FusedLocationProvider](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient) which already handles that.

This plugin currently depends on [RxLocation](https://github.com/patloew/RxLocation) and was initially inspired by [this issue/pull request](https://github.com/ionic-team/capacitor/issues/379)

 ## Supported platforms
 
 * Android
 
## Android setup

- `ionic start my-cap-app --capacitor`
- `cd my-cap-app`
- `npm install --save @jonoj/capacitor-fused-location`
- `mkdir www && touch www/index.html`
- `npx cap add android`
- `npx cap sync android`
- `npx cap open android`
- `[extra step]` in android case we need to tell Capacitor to initialise the plugin:

> on your `MainActivity.java` file add `import com.jonoj.plugin.FusedLocation;` and then inside the init callback `add(FusedLocation.class);`

Now you should be set to go. Try to run your client using `ionic cap run android --livereload`.

> Tip: every time you change a native code you may need to clean up the cache (Build > Clean Project | Build > Rebuild Project) and then run the app again.


## Example

```js
import {FusedLocationPlugin} from '@jonoj/capacitor-fused-location';

const fusedLocation = new FusedLocationPlugin();

class FusedFGeolocationExample {
  async getFusedLocation() {
        if (!(this.platform.is('hybrid') && !this.platform.is('android'))) {
            console.log('only android support!');
            return;
        }
    const coordinates = await fusedLocation.getCurrentPosition();
    console.log('Current', coordinates);
  }

  watchPosition() {
    if (!(this.platform.is('hybrid') && !this.platform.is('android'))) {
        console.log('only android support!');
        return;
    }
    const wait = fusedLocation.watchPosition({}, (position, err) => {
    })
  }
}
```


## Testing

Manually tested against the following platforms:

*  Android emulator 10.0 (API Level 28)
*  Android emulator 7.0 (API Level 24)
*  Android device 6.0 (API Level 23)

