package com.smartpark.ui;

import com.smartpark.model.Ride;
import com.smartpark.service.ParkService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Custom-painted Graphics2D revenue bar chart.
 * Requires no external charting library. Supports tooltips and responsive redraws.
 */
public class RevenueChartPanel extends JPanel {
    private final ParkService service;
    private int hoveredBar = -1;

    public RevenueChartPanel(ParkService service) {
        this.service = service;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(380, 160));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
                "Revenue by Ride (Rs.)", 0, 0, new Font("Segoe UI", Font.BOLD, 12), new Color(40, 50, 70)));

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int old = hoveredBar;
                hoveredBar = getBarIndexAt(e.getX());
                if (old != hoveredBar) {
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredBar = -1;
                repaint();
            }
        });
    }

    private int getBarIndexAt(int mouseX) {
        Ride[] rides = service.getRides();
        if (rides == null || rides.length == 0) return -1;
        int w = getWidth() - 50;
        int barW = w / rides.length;
        int idx = (mouseX - 25) / Math.max(1, barW);
        return (idx >= 0 && idx < rides.length) ? idx : -1;
    }

    // Time O(rides), Space O(1)
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Ride[] rides = service.getRides();
        if (rides == null || rides.length == 0) {
            g2.dispose();
            return;
        }

        long[] revenues = new long[rides.length];
        long maxRev = 1000;
        for (int i = 0; i < rides.length; i++) {
            final int rid = rides[i].getId();
            long rSum = service.getBookingHistory().stream()
                    .filter(b -> b.getRideId() == rid && b.getStatus() == com.smartpark.model.BookingStatus.CONFIRMED)
                    .mapToLong(com.smartpark.model.Booking::getTotalAmount).sum();
            revenues[i] = rSum;
            if (rSum > maxRev) maxRev = rSum;
        }

        int h = getHeight() - 55;
        int w = getWidth() - 60;
        int barWidth = Math.max(25, (w / rides.length) - 20);
        int startX = 35;
        Color[] barColors = {
            new Color(41, 128, 185), new Color(142, 68, 173),
            new Color(39, 174, 96),  new Color(230, 126, 34)
        };

        for (int i = 0; i < rides.length; i++) {
            int x = startX + i * (w / rides.length) + 10;
            int barH = (int) ((revenues[i] * (h - 20)) / maxRev);
            int y = h - barH + 20;

            Color fill = (i == hoveredBar) ? barColors[i].brighter() : barColors[i];
            g2.setColor(fill);
            g2.fillRoundRect(x, y, barWidth, barH, 6, 6);

            g2.setColor(new Color(60, 70, 85));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            String name = rides[i].getName().split(" ")[0];
            g2.drawString(name, x, h + 34);

            String revStr = "Rs." + (revenues[i] >= 1000 ? (revenues[i] / 1000 + "k") : revenues[i]);
            g2.drawString(revStr, x - 2, y - 4);
        }

        // Draw baseline
        g2.setColor(new Color(200, 205, 215));
        g2.drawLine(25, h + 20, getWidth() - 25, h + 20);
        g2.dispose();
    }
}
