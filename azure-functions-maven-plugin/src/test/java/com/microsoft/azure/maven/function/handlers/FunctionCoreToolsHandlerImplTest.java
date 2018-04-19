/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.maven.function.handlers;

import static com.microsoft.azure.maven.function.handlers.FunctionCoreToolsHandlerImpl.LEAST_SUPPORTED_VERSION;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Test;

import com.microsoft.azure.maven.function.AbstractFunctionMojo;

public class FunctionCoreToolsHandlerImplTest {

    @Test
    public void installExtension() throws Exception {
        final AbstractFunctionMojo mojo = mock(AbstractFunctionMojo.class);
        final CommandHandler commandHandler = mock(CommandHandler.class);
        final FunctionCoreToolsHandlerImpl functionCoreToolsHandler =
                new FunctionCoreToolsHandlerImpl(mojo, commandHandler);
        final FunctionCoreToolsHandlerImpl functionCoreToolsHandlerSpy = spy(functionCoreToolsHandler);

        doReturn(LEAST_SUPPORTED_VERSION).when(functionCoreToolsHandlerSpy).getLocalFunctionCoreToolsVersion();
        doNothing().when(commandHandler).runCommandWithReturnCodeCheck(anyString(), anyBoolean(),
                anyString(), any(List.class), anyString());

        functionCoreToolsHandlerSpy.installExtension();
        verify(mojo, never()).warning(anyString());
    }

    @Test
    public void installExtensionWithException() throws Exception {
        final AbstractFunctionMojo mojo = mock(AbstractFunctionMojo.class);
        final CommandHandler commandHandler = mock(CommandHandler.class);
        final FunctionCoreToolsHandlerImpl functionCoreToolsHandler =
                new FunctionCoreToolsHandlerImpl(mojo, commandHandler);
        final FunctionCoreToolsHandlerImpl functionCoreToolsHandlerSpy = spy(functionCoreToolsHandler);

        doReturn("2.0.1-beta.1").when(functionCoreToolsHandlerSpy).getLocalFunctionCoreToolsVersion();
        doNothing().when(commandHandler).runCommandWithReturnCodeCheck(anyString(), anyBoolean(),
                anyString(), any(List.class), anyString());
        doNothing().when(mojo).warning(anyString());

        functionCoreToolsHandlerSpy.installExtension();
        verify(mojo, times(1)).warning(anyString());
    }

    @Test
    public void versionCheckShowWarn() throws Exception {
        final AbstractFunctionMojo mojo = mock(AbstractFunctionMojo.class);
        final CommandHandler commandHandler = mock(CommandHandler.class);
        final FunctionCoreToolsHandlerImpl functionCoreToolsHandler =
                new FunctionCoreToolsHandlerImpl(mojo, commandHandler);
        final FunctionCoreToolsHandlerImpl functionCoreToolsHandlerSpy = spy(functionCoreToolsHandler);

        doNothing().when(commandHandler).runCommandWithReturnCodeCheck(anyString(), anyBoolean(),
                anyString(), any(List.class), anyString());
        doReturn(LEAST_SUPPORTED_VERSION).when(commandHandler).runCommandAndGetOutput(anyString(), anyBoolean(),
                any(), any(List.class), anyString());
        doReturn("2.0.1-beta.1").when(functionCoreToolsHandlerSpy).getLocalFunctionCoreToolsVersion();
        doNothing().when(mojo).warning(anyString());

        functionCoreToolsHandlerSpy.installExtension();
        verify(mojo, times(1)).warning(anyString());
    }

    @Test
    public void versionCheckCanPass() throws Exception {
        final AbstractFunctionMojo mojo = mock(AbstractFunctionMojo.class);
        final CommandHandler commandHandler = mock(CommandHandler.class);
        final FunctionCoreToolsHandlerImpl functionCoreToolsHandler =
                new FunctionCoreToolsHandlerImpl(mojo, commandHandler);
        final FunctionCoreToolsHandlerImpl functionCoreToolsHandlerSpy = spy(functionCoreToolsHandler);

        doNothing().when(commandHandler).runCommandWithReturnCodeCheck(anyString(), anyBoolean(),
                anyString(), any(List.class), anyString());
        doReturn(LEAST_SUPPORTED_VERSION).when(commandHandler).runCommandAndGetOutput(anyString(), anyBoolean(),
                any(), any(List.class), anyString());
        doReturn(LEAST_SUPPORTED_VERSION).when(functionCoreToolsHandlerSpy).getLocalFunctionCoreToolsVersion();

        functionCoreToolsHandlerSpy.installExtension();
        verify(mojo, never()).warning(anyString());
    }

    @Test
    public void getLocalFunctionCoreToolsVersion() throws Exception {
        final AbstractFunctionMojo mojo = mock(AbstractFunctionMojo.class);
        final CommandHandler commandHandler = mock(CommandHandler.class);
        final FunctionCoreToolsHandlerImpl functionCoreToolsHandler =
                new FunctionCoreToolsHandlerImpl(mojo, commandHandler);
        final FunctionCoreToolsHandlerImpl functionCoreToolsHandlerSpy = spy(functionCoreToolsHandler);
        doReturn("C:\\Users\\user\\AppData\\Roaming\\npm\n`-- azure-functions-core-tools@2.0.1-beta.25")
                .when(commandHandler).runCommandAndGetOutput(anyString(),
                anyBoolean(), any(), any(List.class), anyString());
        assertEquals("2.0.1-beta.25", functionCoreToolsHandlerSpy.getLocalFunctionCoreToolsVersion());
    }

    @Test
    public void getLocalFunctionCoreToolsVersionFailed() throws Exception {
        final AbstractFunctionMojo mojo = mock(AbstractFunctionMojo.class);
        final CommandHandler commandHandler = mock(CommandHandler.class);
        final FunctionCoreToolsHandlerImpl functionCoreToolsHandler =
                new FunctionCoreToolsHandlerImpl(mojo, commandHandler);
        final FunctionCoreToolsHandlerImpl functionCoreToolsHandlerSpy = spy(functionCoreToolsHandler);
        doReturn("unexpected output")
                .when(commandHandler).runCommandAndGetOutput(anyString(),
                anyBoolean(), any(), any(List.class), anyString());
        assertNull(functionCoreToolsHandlerSpy.getLocalFunctionCoreToolsVersion());
    }
}
