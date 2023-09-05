module YXConfig
  def self.deployment_target
    "10.0"
  end

  def self.swift_version
    "5.0"
  end

  def self.homepage
    "http://netease.im"
  end

  def self.author
    "yunxin engineering department"
  end

  def self.pod_target_xcconfig(s)
    s.pod_target_xcconfig = {
      "EXCLUDED_ARCHS[sdk=iphonesimulator*]" => "arm64",
      "BUILD_LIBRARY_FOR_DISTRIBUTION" => "YES",
      "APPLICATION_EXTENSION_API_ONLY" => "NO",
    }
  end

  def self.license
    { :'type' => "Copyright", :'text' => " Copyright 2022 Netease " }
  end
end
