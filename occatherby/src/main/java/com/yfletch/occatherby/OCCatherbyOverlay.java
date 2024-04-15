package com.yfletch.occatherby;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Polygon;

@Singleton
public class OCCatherbyOverlay extends Overlay
{

    private final Client client;

    private final OCCatherbyPlugin plugin;

    @Inject
    public OCCatherbyOverlay(Client client, OCCatherbyPlugin plugin)
    {
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {

        renderTile(graphics, plugin.fishingWorldPoint, Color.decode("#ff0000"));
        renderTile(graphics, plugin.bankWorldPoint, Color.decode("#00ff00"));
        renderTile(graphics, plugin.cookWorldPoint, Color.decode("#0000ff"));
        return null;
    }

    private void renderTile(Graphics2D graphics, WorldPoint wp, Color color)
    {
        LocalPoint lp = LocalPoint.fromWorld(client, wp);
        if (lp != null)
        {
            Polygon poly = Perspective.getCanvasTilePoly(client, lp);
            if (poly != null)
            {
                OverlayUtil.renderPolygon(graphics, poly, color);
            }
        }
    }

}
