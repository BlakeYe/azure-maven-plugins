/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.maven.function.handlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;

import com.github.zafarkhaja.semver.Version;
import com.microsoft.azure.maven.function.AbstractFunctionMojo;
import com.microsoft.azure.maven.function.utils.CommandUtils;

public class FunctionCoreToolsHandlerImpl implements FunctionCoreToolsHandler {

    public static final String FUNC_EXTENSIONS_INSTALL_TEMPLATE = "func extensions install -c %s";
    public static final String INSTALL_FUNCTION_EXTENSIONS_FAIL = "Failed to install the Function extensions";
    public static final String OUTDATED_LOCAL_FUNCTION_CORE_TOOLS = "Local Azure Functions Core Tools does not " +
            "support extension auto-install, skip it in the package phase.";
    public static final String NEED_UPDATE_FUNCTION_CORE_TOOLS = "Local version of Azure Functions Core Tools (%s) " +
            "does not match the latest (%s). Please update using 'npm i -g azure-functions-core-tools@core' " +
            "for the best experience.";
    public static final String LEAST_SUPPORTED_VERSION = "2.0.1-beta.26";
    public static final String GET_LATEST_VERSION_CMD = "npm view azure-functions-core-tools dist-tags.core";
    public static final String GET_LATEST_VERSION_FAIL = "Failed to get Azure Functions Core Tools version locally";
    public static final String GET_LOCAL_VERSION_CMD = "npm ls azure-functions-core-tools -g";
    public static final String GET_LOCAL_VERSION_FAIL = "Failed to get Azure Functions Core Tools version locally";
    public static final String VERSION_REGEX = "(?:.*)azure-functions-core-tools@(.*)";

    private AbstractFunctionMojo mojo;
    private CommandHandler commandHandler;

    public FunctionCoreToolsHandlerImpl(final AbstractFunctionMojo mojo, final CommandHandler commandHandler) {
        this.mojo = mojo;
        this.commandHandler = commandHandler;
    }

    @Override
    public void installExtension() {
        String localVersion;
        try {
            final Version supportedVersion = Version.valueOf(LEAST_SUPPORTED_VERSION);
            localVersion = getLocalFunctionCoreToolsVersion();
            if (localVersion == null || supportedVersion.greaterThan(Version.valueOf(localVersion))) {
                throw new MojoExecutionException(OUTDATED_LOCAL_FUNCTION_CORE_TOOLS);
            }

            commandHandler.runCommandWithReturnCodeCheck(
                    String.format(FUNC_EXTENSIONS_INSTALL_TEMPLATE, getProjectBasePath()),
                    true,
                    this.mojo.getDeploymentStageDirectory(),
                    CommandUtils.getDefaultValidReturnCodes(),
                    INSTALL_FUNCTION_EXTENSIONS_FAIL
            );

            final String latestCoreVersion = commandHandler.runCommandAndGetOutput(
                    GET_LATEST_VERSION_CMD,
                    false, /* showStdout */
                    null, /* workingDirectory */
                    CommandUtils.getDefaultValidReturnCodes(),
                    GET_LATEST_VERSION_FAIL
            );

            if (Version.valueOf(localVersion).lessThan(supportedVersion) ||
                    Version.valueOf(localVersion).lessThan(Version.valueOf(latestCoreVersion))) {
                this.mojo.warning(String.format(NEED_UPDATE_FUNCTION_CORE_TOOLS, localVersion, latestCoreVersion));
            }
        } catch (Exception e) {
            this.mojo.warning(e.getMessage());
        }
    }

    protected String getLocalFunctionCoreToolsVersion() throws Exception {
        final String versionInfo = commandHandler.runCommandAndGetOutput(
                GET_LOCAL_VERSION_CMD,
                false, /* showStdout */
                null, /* workingDirectory */
                CommandUtils.getDefaultValidReturnCodes(),
                GET_LOCAL_VERSION_FAIL
        );

        final Pattern regex = Pattern.compile(VERSION_REGEX);
        final Matcher matcher = regex.matcher(versionInfo);

        //Group zero denotes the entire pattern by convention. It is not included in groupCount().
        if (matcher.find() && matcher.groupCount() >= 1) {
            return matcher.group(1).trim();
        } else {
            return null;
        }
    }

    protected String getProjectBasePath() {
        return this.mojo.getProject().getBasedir().getAbsolutePath();
    }
}
