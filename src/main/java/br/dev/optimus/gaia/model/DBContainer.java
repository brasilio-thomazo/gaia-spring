package br.dev.optimus.gaia.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvFromSource;
import io.fabric8.kubernetes.api.model.EnvFromSourceBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.ResourceRequirementsBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;

public class DBContainer {
    private String name;
    private String image;
    private List<String> command;
    private List<String> args;
    private List<Env> env;
    @JsonProperty("env_from")
    private List<EnvFrom> envFrom;
    private List<Port> ports;
    private List<Volume> volumes;
    private Map<String, String> limits;

    public enum EnvFromType {
        ConfigMap, Secret
    }

    public static class Env {
        private String name;
        private String value;

        public Env() {
        }

        public Env(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class EnvFrom {
        private String name;
        private EnvFromType type;

        public EnvFrom() {
        }

        public EnvFrom(String name, EnvFromType type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public EnvFromType getType() {
            return type;
        }

        public void setType(EnvFromType type) {
            this.type = type;
        }
    }

    public static class Port {
        @JsonProperty("container_port")
        private Integer containerPort;
        @JsonProperty("host_port")
        private Integer hostPort;
        private String name;
        private String protocol;
        @JsonProperty("host_ip")
        private String hostIP;

        public Port() {
        }

        public Integer getContainerPort() {
            return containerPort;
        }

        public void setContainerPort(Integer containerPort) {
            this.containerPort = containerPort;
        }

        public Integer getHostPort() {
            return hostPort;
        }

        public void setHostPort(Integer hostPort) {
            this.hostPort = hostPort;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getHostIP() {
            return hostIP;
        }

        public void setHostIP(String hostIP) {
            this.hostIP = hostIP;
        }
    }

    public static class Volume {
        private String name;
        @JsonProperty("mount_path")
        private String mountPath;
        @JsonProperty("read_only")
        private boolean readOnly;

        public Volume() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMountPath() {
            return mountPath;
        }

        public void setMountPath(String mountPath) {
            this.mountPath = mountPath;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }

    }

    private EnvVar envVar(Env source) {
        return new EnvVarBuilder().withName(source.getName()).withValue(source.getValue()).build();
    }

    private EnvFromSource envFromSource(EnvFrom source) {
        var builder = new EnvFromSourceBuilder();
        return switch (source.getType()) {
            case ConfigMap -> builder.withNewConfigMapRef().withName(source.getName()).endConfigMapRef().build();
            case Secret -> builder.withNewSecretRef().withName(source.getName()).endSecretRef().build();
            default -> throw new UnsupportedOperationException();
        };
    }

    private VolumeMount volumeMount(Volume mount) {
        return new VolumeMountBuilder()
                .withName(mount.getName())
                .withMountPath(mount.getMountPath())
                .withReadOnly(mount.isReadOnly())
                .build();
    }

    private ContainerPort containerPort(Port port) {
        var builder = new ContainerPortBuilder().withProtocol("TCP");
        if (port.getHostIP() != null) {
            builder.withHostIP(port.getHostIP());
        }
        if (port.getHostPort() != null) {
            builder.withHostPort(port.getHostPort());
        }
        if (port.getContainerPort() != null) {
            builder.withContainerPort(port.getContainerPort());
        }
        if (port.getName() != null) {
            builder.withName(port.getName());
        }
        if (port.getProtocol() != null) {
            builder.withProtocol(port.getProtocol());
        }
        return new ContainerPortBuilder()
                .withHostIP(port.getHostIP())
                .withHostPort(port.getHostPort())
                .withContainerPort(port.getContainerPort())
                .withName(port.getName())
                .withProtocol(port.getProtocol())
                .build();
    }

    private ResourceRequirements resourceRequirements() {
        var builder = new ResourceRequirementsBuilder();
        var limits = new HashMap<String, Quantity>();
        if (this.limits.get("cpu") != null) {
            limits.put("cpu", Quantity.parse(this.limits.get("cpu")));
        }
        if (this.limits.get("memory") != null) {
            limits.put("memory", Quantity.parse(this.limits.get("memory")));
        }
        builder.withLimits(limits);
        return builder.build();
    }

    public Container container() {
        var builder = new ContainerBuilder()
                .withName(name)
                .withImage(image);
        if (command != null) {
            builder.withCommand(command);
        }
        if (args != null) {
            builder.withArgs(args);
        }
        if (env != null) {
            builder.withEnv(env.stream().map(this::envVar).toList());
        }
        if (envFrom != null) {
            builder.withEnvFrom(envFrom.stream().map(this::envFromSource).toList());
        }
        if (ports != null) {
            builder.withPorts(ports.stream().map(this::containerPort).toList());
        }
        if (volumes != null) {
            builder.withVolumeMounts(volumes.stream().map(this::volumeMount).toList());
        }
        if (limits != null) {
            builder.withResources(resourceRequirements());
        }
        return builder.build();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getCommand() {
        return command;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public List<Env> getEnv() {
        return env;
    }

    public void setEnv(List<Env> env) {
        this.env = env;
    }

    public List<EnvFrom> getEnvFrom() {
        return envFrom;
    }

    public void setEnvFrom(List<EnvFrom> envFromSources) {
        this.envFrom = envFromSources;
    }

    public List<Port> getPorts() {
        return ports;
    }

    public void setPorts(List<Port> ports) {
        this.ports = ports;
    }

    public List<Volume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<Volume> volumes) {
        this.volumes = volumes;
    }

    public Map<String, String> getLimits() {
        return limits;
    }

    public void setLimits(Map<String, String> limits) {
        this.limits = limits;
    }

}
