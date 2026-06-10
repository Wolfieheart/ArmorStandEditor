package io.github.rypofalem.armorstandeditor;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BasePluginTest {

    protected ServerMock server;
    protected ArmorStandEditorPlugin plugin;

    @BeforeAll
    void setupServer() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(ArmorStandEditorPlugin.class, true);
    }

    @AfterAll
    void tearDownServer() {
        MockBukkit.unmock();
    }
}