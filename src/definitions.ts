declare global {
  interface PluginRegistry {
    FusedLocation?: FusedLocationPlugin;
  }
}

export interface FusedLocationPlugin {
  echo(options: { value: string }): Promise<{value: string}>;
}
