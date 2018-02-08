package org.jfrog.hudson.pipeline.dsl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jfrog.hudson.pipeline.Utils;
import org.jfrog.hudson.pipeline.types.*;
import org.jfrog.hudson.pipeline.types.buildInfo.BuildInfo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Tamirh on 18/05/2016.
 */
public class ArtifactoryPipelineGlobal implements Serializable {
    private CpsScript cpsScript;

    public ArtifactoryPipelineGlobal(CpsScript script) {
        this.cpsScript = script;
    }

    @Whitelisted
    public ArtifactoryServer server(String serverName) {
        Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put("artifactoryServerID", serverName);
        ArtifactoryServer server = (ArtifactoryServer) cpsScript.invokeMethod("getArtifactoryServer", stepVariables);
        server.setCpsScript(cpsScript);
        return server;
    }

    @Whitelisted
    public Docker docker(String username, String password) {
        return new Docker(cpsScript, username, password, null, null);
    }

    @Whitelisted
    public Docker docker(String username, String password, String host) {
        return new Docker(cpsScript, username, password, null, host);
    }

    @Whitelisted
    public Docker docker() {
        return new Docker(cpsScript, null, null, null, null);
    }

    @Whitelisted
    public Docker docker(Map<String, Object> dockerArguments) {
        List<String> keysAsList = Arrays.asList("username", "password", "credentialsId", "host", "server");
        if (!keysAsList.containsAll(dockerArguments.keySet())) {
            throw new IllegalArgumentException("Only the following arguments are allowed: " + keysAsList);
        }

        // We don't want to handle the deserialization of the ArtifactoryServer.
        // Instead we will remove it and later on set it on the deployer object.
        Object server = dockerArguments.remove("server");

        final ObjectMapper mapper = new ObjectMapper();
        Docker docker = mapper.convertValue(dockerArguments, Docker.class);
        docker.setCpsScript(cpsScript);
        if (server != null) {
            docker.setServer((ArtifactoryServer) server);
        }
        return docker;
    }

    @Whitelisted
    public ArtifactoryServer newServer(String url, String username, String password) {
        Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        stepVariables.put("url", url);
        stepVariables.put("username", username);
        stepVariables.put("password", password);
        ArtifactoryServer server = (ArtifactoryServer) cpsScript.invokeMethod("newArtifactoryServer", stepVariables);
        server.setCpsScript(cpsScript);
        return server;
    }

    @Whitelisted
    public ArtifactoryServer newServer(Map<String, Object> serverArguments) {
        List<String> keysAsList = Arrays.asList("url", "username", "password", "credentialsId");
        if (!keysAsList.containsAll(serverArguments.keySet())) {
            throw new IllegalArgumentException("The newServer method accepts the following arguments only: " + keysAsList);
        }

        ArtifactoryServer server = (ArtifactoryServer) cpsScript.invokeMethod("newArtifactoryServer", serverArguments);
        server.setCpsScript(cpsScript);
        return server;
    }

    @Whitelisted
    public BuildInfo newBuildInfo() {
        BuildInfo buildInfo = (BuildInfo) cpsScript.invokeMethod("newBuildInfo", Maps.newLinkedHashMap());
        buildInfo.setCpsScript(cpsScript);
        return buildInfo;
    }

    @Whitelisted
    public MavenBuild newMavenBuild() {
        MavenBuild mavenBuild = (MavenBuild) cpsScript.invokeMethod("newMavenBuild", Maps.newLinkedHashMap());
        mavenBuild.setCpsScript(cpsScript);
        return mavenBuild;
    }

    @Whitelisted
    public GradleBuild newGradleBuild() {
        GradleBuild gradleBuild = (GradleBuild) cpsScript.invokeMethod("newGradleBuild", Maps.newLinkedHashMap());
        gradleBuild.setCpsScript(cpsScript);
        return gradleBuild;
    }

    @Whitelisted
    public ConanClient newConanClient(Map<String, Object> clientArgs) {
        ConanClient client = new ConanClient();
        String userPath = (String) clientArgs.get("userHome");
        if (StringUtils.isBlank(userPath)) {
            throw new IllegalArgumentException("The newConanClient method expects the 'userHome' argument or no arguments.");
        }
        client.setUserPath(userPath);
        client.setCpsScript(cpsScript);
        LinkedHashMap<String, Object> args = Maps.newLinkedHashMap();
        args.put("client", client);
        cpsScript.invokeMethod("initConanClient", args);
        return client;
    }

    @Whitelisted
    public ConanClient newConanClient() {
        ConanClient client = new ConanClient();
        client.setCpsScript(cpsScript);
        LinkedHashMap<String, Object> args = Maps.newLinkedHashMap();
        args.put("client", client);
        cpsScript.invokeMethod("initConanClient", args);
        return client;
    }

    @Whitelisted
    public MavenDescriptor mavenDescriptor() {
        MavenDescriptor descriptorHandler = new MavenDescriptor();
        descriptorHandler.setCpsScript(cpsScript);
        return descriptorHandler;
    }

    @Whitelisted
    public void addInteractivePromotion(Map<String, Object> promotionArguments) {
        Map<String, Object> stepVariables = Maps.newLinkedHashMap();
        List<String> mandatoryParams = Arrays.asList(ArtifactoryServer.SERVER, "promotionConfig");
        List<String> allowedParams = Arrays.asList(ArtifactoryServer.SERVER, "promotionConfig", "displayName");
        if (!promotionArguments.keySet().containsAll(mandatoryParams)) {
            throw new IllegalArgumentException(mandatoryParams.toString() + " are mandatory arguments");
        }
        if (!allowedParams.containsAll(promotionArguments.keySet())){
            throw new IllegalArgumentException("Only the following arguments are allowed: " + allowedParams.toString());
        }
        stepVariables.put("promotionConfig", Utils.createPromotionConfig((Map<String, Object>) promotionArguments.get("promotionConfig"), false));
        stepVariables.put(ArtifactoryServer.SERVER, promotionArguments.get(ArtifactoryServer.SERVER));
        stepVariables.put("displayName", promotionArguments.get("displayName"));
        cpsScript.invokeMethod("addInteractivePromotion", stepVariables);
    }
}
