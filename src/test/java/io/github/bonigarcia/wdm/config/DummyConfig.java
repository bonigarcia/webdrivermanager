package io.github.bonigarcia.wdm.config;

public class DummyConfig extends Config {
  private final OperatingSystem operatingSystem;
  private final Architecture arch;

  public DummyConfig(OperatingSystem operatingSystem, Architecture arch) {
    this.operatingSystem = operatingSystem;
    this.arch = arch;
  }

  @Override
  public OperatingSystem getOperatingSystem() {
    return operatingSystem;
  }

  @Override
  public Architecture getArchitecture() {
    return arch;
  }
}
