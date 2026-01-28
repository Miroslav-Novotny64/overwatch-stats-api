package org.example;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.parser.SVGLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ProfileCardGenerator {

    private final HttpClient client;
    private final SVGLoader svgLoader;
    private Font overwatchFont;

    public ProfileCardGenerator() {
        this.client = HttpClient.newHttpClient();
        this.svgLoader = new SVGLoader();
        try {
            InputStream fontStream = getClass().getResourceAsStream("/fonts/BigNoodleTitling.ttf");
            if (fontStream != null) {
                this.overwatchFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(this.overwatchFont);
            } else {
                System.err.println("Could not find font file: /fonts/BigNoodleTitling.ttf");
                this.overwatchFont = new Font("SansSerif", Font.BOLD, 12);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.overwatchFont = new Font("SansSerif", Font.BOLD, 12);
        }
    }

    public BufferedImage downloadImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = client.send(request,
                    HttpResponse.BodyHandlers.ofInputStream());

            return ImageIO.read(response.body());
        } catch (Exception e) {
            System.err.println("Failed to download image: " + imageUrl);
            return null;
        }
    }

    public SVGDocument downloadSvg(String svgUrl) {
        if (svgUrl == null || svgUrl.isEmpty()) return null;
        try {
            URL url = new URL(svgUrl);
            return svgLoader.load(url);
        } catch (Exception e) {
            System.err.println("Failed to download SVG: " + svgUrl);
            return null;
        }
    }

    // Kod pro grafiku generován pomocí umělé inteligence.
    public void generateCard(PlayerProfile profile, String outputPath) throws Exception {
        int width = 1400;
        int height = 1600;

        // Transparent canvas (no background fill)
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // 1. Banner / namecard (less tall, keep aspect ratio)
        int bannerHeight = 180; // ~10% less than 200
        BufferedImage nameCard = downloadImage(profile.summary.namecard);
        if (nameCard != null) {
            int originalW = nameCard.getWidth();
            int originalH = nameCard.getHeight();

            double scale = (double) width / originalW;
            int scaledH = (int) (originalH * scale);

            int yOffset = (bannerHeight - scaledH) / 2;

            g.drawImage(
                    nameCard,
                    0, yOffset,
                    width, yOffset + scaledH,
                    0, 0, originalW, originalH,
                    null
            );
        }

        // Soft fade at bottom of banner
        GradientPaint bannerFade = new GradientPaint(
                0, bannerHeight - 50, new Color(25, 30, 43, 0),
                0, bannerHeight,     new Color(25, 30, 43, 220)
        );
        g.setPaint(bannerFade);
        g.fillRect(0, bannerHeight - 50, width, 50);

        // 2. Avatar (overlapping banner)
        BufferedImage avatar = downloadImage(profile.summary.avatar);
        int avatarSize = 160;
        int avatarX = 60;
        int avatarY = bannerHeight - (avatarSize / 2); // overlaps lower edge

        if (avatar != null) {
            BufferedImage circularAvatar = new BufferedImage(avatarSize, avatarSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D ag = circularAvatar.createGraphics();
            ag.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ag.setClip(new Ellipse2D.Float(0, 0, avatarSize, avatarSize));
            ag.drawImage(avatar, 0, 0, avatarSize, avatarSize, null);
            ag.dispose();

            // Soft outer ring
            g.setColor(new Color(255, 255, 255, 40));
            g.fillOval(avatarX - 6, avatarY - 6, avatarSize + 12, avatarSize + 12);
            // Inner gap (uses fake background color; still transparent outside)
            g.setColor(new Color(25, 30, 43, 230));
            g.fillOval(avatarX - 3, avatarY - 3, avatarSize + 6, avatarSize + 6);

            g.drawImage(circularAvatar, avatarX, avatarY, null);
        }

        // 3. Username & title, aligned with avatar
        int textBaseY = bannerHeight + 20;

        g.setColor(Color.WHITE);
        g.setFont(overwatchFont.deriveFont(Font.PLAIN, 96));
        g.drawString(profile.summary.username, 250, textBaseY);

        if (profile.summary.title != null && !profile.summary.title.isEmpty()) {
            g.setFont(overwatchFont.deriveFont(Font.PLAIN, 52));
            g.setColor(new Color(200, 200, 200));
            g.drawString(profile.summary.title, 250, textBaseY + 40);
        }

        if (profile.summary.endorsement != null) {
            int endLvl = profile.summary.endorsement.level;
            g.setFont(overwatchFont.deriveFont(Font.PLAIN, 56));
            g.setColor(new Color(255, 165, 0));
            g.drawString("ENDORSEMENT LEVEL " + endLvl, 250, textBaseY + 80);
        }

        // 4. Competitive ranks in a clean row
        if (profile.summary.competitive != null && profile.summary.competitive.pc != null) {
            CompetitiveData comp = profile.summary.competitive.pc;
            int startX = 60;
            int startY = textBaseY + 140;
            int cardWidth = 380;
            int spacing = 410;

            if (comp.tank != null) {
                drawRank(g, comp.tank, startX, startY, "Tank", cardWidth);
                startX += spacing;
            }
            if (comp.damage != null) {
                drawRank(g, comp.damage, startX, startY, "Damage", cardWidth);
                startX += spacing;
            }
            if (comp.support != null) {
                drawRank(g, comp.support, startX, startY, "Support", cardWidth);
            }
        }

        g.dispose();

        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        ImageIO.write(canvas, "PNG", outputFile);
        System.out.println("Card saved to: " + outputPath);
    }

    private void drawRank(Graphics2D g, Rank rank, int x, int y, String roleName, int width) {
        int height = 220;

        // Card background (floating panel, rounded corners)
        g.setColor(new Color(25, 30, 43, 230));
        g.fillRoundRect(x, y, width, height, 24, 24);
        g.setColor(new Color(255, 255, 255, 25));
        g.drawRoundRect(x, y, width, height, 24, 24);

        // Role SVG icon
        SVGDocument roleSvg = downloadSvg(rank.roleIcon);
        if (roleSvg != null) {
            int iconSize = 30;
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.translate(x + 20, y + 18);

            FloatSize size = roleSvg.size();
            double scale = iconSize / Math.max(size.width, size.height);
            g2d.scale(scale, scale);

            roleSvg.render(null, g2d);
            g2d.dispose();
        }

        // Role label
        g.setFont(overwatchFont.deriveFont(Font.PLAIN, 48));
        g.setColor(new Color(220, 220, 220));
        g.drawString(roleName.toUpperCase(), x + 60, y + 45);

        // Rank icon
        BufferedImage rankIcon = downloadImage(rank.rankIcon);
        if (rankIcon != null) {
            int rankSize = 110;
            int iconX = x + (width - rankSize) / 2;
            int iconY = y + 55;
            g.drawImage(rankIcon, iconX, iconY, rankSize, rankSize, null);
        }

        // Division text
        if (rank.division != null) {
            String rankText = rank.division.toUpperCase() + " " + rank.tier;

            g.setFont(overwatchFont.deriveFont(Font.PLAIN, 44));
            g.setColor(Color.WHITE);

            FontMetrics metrics = g.getFontMetrics();
            int textWidth = metrics.stringWidth(rankText);
            int textX = x + (width - textWidth) / 2;
            int textY = y + 195;

            g.drawString(rankText, textX, textY);
        }
    }
}
