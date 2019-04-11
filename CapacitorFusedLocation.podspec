
  Pod::Spec.new do |s|
    s.name = 'CapacitorFusedLocation'
    s.version = '0.0.1'
    s.summary = 'Provides acces to the fused location api by google play services'
    s.license = 'MIT'
    s.homepage = 'https://github.com/jonoj-team/capacitor-fused-location'
    s.author = 'Johannes Normann Jensen'
    s.source = { :git => 'https://github.com/jonoj-team/capacitor-fused-location', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end