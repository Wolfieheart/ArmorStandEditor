package io.github.rypofalem.armorstandeditor.api;

import io.github.rypofalem.armorstandeditor.api.interfaces.OpenMenuEvent;
import io.github.rypofalem.armorstandeditor.menu.EditorMenu;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MainMenuOpenedEvent extends OpenMenuEvent implements Cancellable {

    @Getter
    @Setter
    private boolean isCancelled = false;

    @Getter
    protected final Player player;

    @Getter
    protected final EditorMenu edtMenu;

    private static final HandlerList HANDLERS = new HandlerList();

    public MainMenuOpenedEvent(Player player, EditorMenu edtMenu) {
        super(player, edtMenu);
        this.player = player;
        this.edtMenu = edtMenu;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

}
