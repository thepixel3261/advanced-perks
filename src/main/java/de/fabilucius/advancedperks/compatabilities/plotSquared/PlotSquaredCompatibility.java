// path: de/fabilucius/advancedperks/compatabilities/plotSquared/PlotSquaredCompatibility.java

package de.fabilucius.advancedperks.compatabilities.plotSquared;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.events.PlayerEnterPlotEvent;
import com.plotsquared.core.events.PlayerLeavePlotEvent;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.FlyFlag;
import de.fabilucius.advancedperks.AdvancedPerks;
import de.fabilucius.advancedperks.compatabilities.AbstractPerkCompatability;
import de.fabilucius.advancedperks.data.PerkDataRepository;
import de.fabilucius.advancedperks.perk.defaultperks.listener.BirdPerk;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class PlotSquaredCompatibility extends AbstractPerkCompatability {

    @Inject
    private PerkDataRepository perkDataRepository;

    private final PlotAPI plotAPI = new PlotAPI();

    @Inject
    public PlotSquaredCompatibility(AdvancedPerks advancedPerks) {
        super(advancedPerks);
        plotAPI.registerListener(this);
    }

    @Subscribe
    public void onEnterPlot(PlayerEnterPlotEvent event) {
        AdvancedPerks.instance.getLogger().info("ENTER EVENT FIRED");
        Player player = Bukkit.getPlayer(event.getPlotPlayer().getUUID());
        if (player == null) return;

        refresh(player);
        for (long i = 1L; i<=3L; i++) {
            Bukkit.getScheduler().runTaskLater(AdvancedPerks.instance, () -> refresh(player), i);
        }
    }

    @Subscribe
    public void onLeavePlot(PlayerLeavePlotEvent event) {
        AdvancedPerks.instance.getLogger().info("LEAVE EVENT FIRED");
        Player player = Bukkit.getPlayer(event.getPlotPlayer().getUUID());
        if (player == null) return;

        refresh(player);
        Bukkit.getScheduler().runTaskLater(AdvancedPerks.instance, () -> refresh(player), 3L);
    }

    private boolean hasFlyActive(Player player) {
        return perkDataRepository
                .getPerkDataByPlayer(player)
                .getEnabledPerks()
                .stream()
                .anyMatch(perk -> perk.getClass() == BirdPerk.class);
    }

    public void refresh(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        boolean disableFly = !isPlotFlyEnabled(player);

        if (disableFly) {
            player.setAllowFlight(hasFlyActive(player));
            AdvancedPerks.instance.getLogger().info("PlotFly=" + isPlotFlyEnabled(player) + " Perk=" + hasFlyActive(player));
        }
    }

    private boolean isPlotFlyEnabled(Player player) {
        PlotPlayer<?> plotPlayer = plotAPI.wrapPlayer(player.getUniqueId());
        if (plotPlayer == null) return false;

        PlotArea area = plotAPI.getPlotSquared().getPlotAreaManager().getPlotArea(plotPlayer.getLocation());
        if (area == null) return false;

        Plot plot = area.getPlot(plotPlayer.getLocation());
        if (plot == null) return false;

        FlyFlag.FlyStatus flag = plot.getFlag(FlyFlag.class);
        return flag == FlyFlag.FlyStatus.ENABLED;
    }
}