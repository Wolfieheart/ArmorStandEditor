package io.github.rypofalem.armorstandeditor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

public abstract class BasePluginTest {

    protected ServerMock server;
    protected ArmorStandEditorPlugin plugin;

    @BeforeEach
    void setupServer() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(ArmorStandEditorPlugin.class, true);
    }

    @AfterEach
    void tearDownServer() {
        MockBukkit.unmock();
    }
}