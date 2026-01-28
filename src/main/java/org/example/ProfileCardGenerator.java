package org.example;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.imageio.ImageIO;

public class ProfileCardGenerator {

    private final HttpClient client;

    public ProfileCardGenerator() {
        this.client = HttpClient.newHttpClient();
    }

    // Stáhne obrázek z URL
    public BufferedImage downloadImage(String imageUrl) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .GET()
                .build();

        HttpResponse<InputStream> response = client.send(request,
                HttpResponse.BodyHandlers.ofInputStream());

        return ImageIO.read(response.body());
    }

    // Vytvoří profile card
    public void generateCard(PlayerProfile profile, String outputPath) throws Exception {
        int width = 1200;
        int height = 400;
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();

        // Antialiasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Gradient pozadí (tmavě modrá jako Overwatch)
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(25, 35, 50),
                0, height, new Color(15, 25, 40)
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, width, height);

        // Stáhni a vykresli avatar (s rámečkem)
        BufferedImage avatar = downloadImage(profile.summary.avatar);
        int avatarSize = 180;
        int avatarX = 40;
        int avatarY = 40;

        // Bílý rámeček kolem avataru
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(4));
        g.drawRect(avatarX - 2, avatarY - 2, avatarSize + 4, avatarSize + 4);

        // Avatar
        g.drawImage(avatar, avatarX, avatarY, avatarSize, avatarSize, null);

        // Username (velké bílé písmeno)
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 56));
        g.drawString(profile.summary.username.toUpperCase(), 250, 110);

        // Title (oranžové písmeno)
        g.setColor(new Color(255, 153, 0));
        g.setFont(new Font("Arial", Font.BOLD, 32));
        g.drawString(profile.summary.title.toUpperCase(), 40, 280);

        // Endorsement level
        int endorsementX = 40;
        int endorsementY = 320;

        // Oranžový kroužek s číslem
        g.setColor(new Color(255, 153, 0));
        g.fillOval(endorsementX, endorsementY, 60, 60);
        g.setColor(new Color(25, 35, 50));
        g.setFont(new Font("Arial", Font.BOLD, 32));
        String endorsement = String.valueOf(profile.summary.endorsement.level);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(endorsement);
        g.drawString(endorsement, endorsementX + 30 - textWidth/2, endorsementY + 42);

        // Ranky - vykreslení po řadě
        CompetitiveData comp = profile.summary.competitive.pc;
        int rankStartX = 150;
        int rankY = 310;
        int rankSpacing = 250;

        // Tank
        if (comp.tank != null) {
            drawRank(g, comp.tank, "Tank", rankStartX, rankY);
        }

        // Damage
        if (comp.damage != null) {
            drawRank(g, comp.damage, "Damage", rankStartX + rankSpacing, rankY);
        }

        // Support
        if (comp.support != null) {
            drawRank(g, comp.support, "Support", rankStartX + rankSpacing * 2, rankY);
        }

        g.dispose();

        // Ulož
        ImageIO.write(canvas, "PNG", new File(outputPath));
        System.out.println("Card saved to: " + outputPath);
    }

    private void drawRank(Graphics2D g, Rank rank, String roleName, int x, int y) throws Exception {
        // Stáhni rank ikonu
        BufferedImage rankIcon = downloadImage(rank.rankIcon);

        // Bílý štítek (pozadí)
        g.setColor(Color.WHITE);
        g.fillRect(x, y, 180, 80);

        // Rank ikona
        g.drawImage(rankIcon, x + 10, y + 10, 60, 60, null);

        // Division text
        g.setColor(new Color(25, 35, 50));
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString(rank.division.toUpperCase(), x + 80, y + 35);

        // Tier číslo
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Tier " + rank.tier, x + 80, y + 55);
    }
}
