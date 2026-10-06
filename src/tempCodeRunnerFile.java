import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

// --- 1. OOP Core: Route Registry & Transport Models ---
abstract class Vehicle {
    private String name;
    private int totalSeats;
    private int availableSeats;

    public Vehicle(String name, int totalSeats, int availableSeats) {
        this.name = name;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
    }

    public String getName() { return name; }
    public int getTotalSeats() { return totalSeats; }
    public int getAvailableSeats() { return availableSeats; }

    public boolean bookSeats(int count) {
        if (count <= availableSeats) {
            availableSeats -= count;
            return true;
        }
        return false;
    }

    public abstract double calculateFare(double distance);
}

class BRTCBus extends Vehicle {
    public BRTCBus() { super("BRTC Bus", 40, 18); }
    @Override public double calculateFare(double distance) { return Math.max(15.0, distance * 3.5); }
}

class SakuraBus extends Vehicle {
    public SakuraBus() { super("Sakura Paribahan", 36, 12); }
    @Override public double calculateFare(double distance) { return Math.max(250.0, distance * 3.2); }
}

class RaidaBus extends Vehicle {
    public RaidaBus() { super("Raida Express", 45, 25); }
    @Override public double calculateFare(double distance) { return Math.max(10.0, distance * 2.8); }
}

class MetroRail extends Vehicle {
    public MetroRail() { super("Rapid Metro Rail", 150, 60); }
    @Override public double calculateFare(double distance) { return 20.0 + (distance * 3.5); }
}

class TransportRegistry {
    private static final Map<String, Map<String, Integer>> operatorRoutes = new HashMap<>();
    private static final Map<String, Vehicle> operatorVehicles = new HashMap<>();

    static {
        operatorVehicles.put("BRTC Bus", new BRTCBus());
        operatorVehicles.put("Sakura Paribahan", new SakuraBus());
        operatorVehicles.put("Raida Express", new RaidaBus());
        operatorVehicles.put("Rapid Metro Rail", new MetroRail());

        Map<String, Integer> brtc = new HashMap<>();
        brtc.put("Uttara House Building", 0);
        brtc.put("Airport", 4);
        brtc.put("Farmgate", 14);
        brtc.put("Motijheel", 20);
        operatorRoutes.put("BRTC Bus", brtc);

        Map<String, Integer> sakura = new HashMap<>();
        sakura.put("Gabtoli Counter", 0);
        sakura.put("Padma Bridge Toll Gate", 45);
        sakura.put("Barishal Terminal", 180);
        operatorRoutes.put("Sakura Paribahan", sakura);

        Map<String, Integer> raida = new HashMap<>();
        raida.put("Diabari Uttara", 0);
        raida.put("Kuril Flyover", 8);
        raida.put("Badda Link Road", 14);
        raida.put("Sayedabad", 25);
        operatorRoutes.put("Raida Express", raida);

        Map<String, Integer> metro = new HashMap<>();
        metro.put("Uttara North", 0);
        metro.put("Uttara Center", 2);
        metro.put("Uttara South", 4);
        metro.put("Pallabi", 6);
        metro.put("Mirpur 11", 8);
        metro.put("Mirpur 10", 10);
        metro.put("Kazipara", 12);
        metro.put("Shewrapara", 14);
        metro.put("Agargaon", 16);
        metro.put("Bijoy Sarani", 18);
        metro.put("Farmgate", 20);
        metro.put("Kawran Bazar", 22);
        metro.put("Shahabag", 24);
        metro.put("Dhaka University", 26);
        metro.put("Bangladesh Secretariate", 28);
        metro.put("Motijheel", 30);
        operatorRoutes.put("Rapid Metro Rail", metro);
    }

    public static String[] getOperators() {
        return operatorRoutes.keySet().toArray(new String[0]);
    }

    public static Vehicle getVehicleForOperator(String operator) {
        return operatorVehicles.get(operator);
    }

    public static String[] getStationsForOperator(String operator) {
        if (operatorRoutes.containsKey(operator)) {
            return operatorRoutes.get(operator).keySet().toArray(new String[0]);
        }
        return new String[0];
    }

    public static double calculateDistance(String operator, String start, String end) {
        if (operatorRoutes.containsKey(operator)) {
            Map<String, Integer> stations = operatorRoutes.get(operator);
            if (stations.containsKey(start) && stations.containsKey(end)) {
                return Math.abs(stations.get(start) - stations.get(end));
            }
        }
        return 0;
    }
}

// --- 2. Custom 5x7 Dot Matrix LED Marquee Engine ---
class ScrollingLEDBoard extends JPanel {
    private String message = "LIVE BUS SCHEDULE: BRTC BUS (MOTIJHEEL) - 09:00 PM  |  SAKURA PARIBAHAN (BARISHAL) - 09:30 PM  |  RAIDA EXPRESS (SAYEDABAD) - 09:15 PM  |  RAPID METRO RAIL - EVERY 5 MINS ";
    private int scrollX = 900;
    private Timer timer;

    private static final Map<Character, int[]> LED_FONT = new HashMap<>();
    static {
        LED_FONT.put(' ', new int[]{0x00, 0x00, 0x00, 0x00, 0x00});
        LED_FONT.put('0', new int[]{0x3E, 0x51, 0x49, 0x45, 0x3E});
        LED_FONT.put('1', new int[]{0x00, 0x42, 0x7F, 0x40, 0x00});
        LED_FONT.put('2', new int[]{0x42, 0x61, 0x51, 0x49, 0x46});
        LED_FONT.put('3', new int[]{0x21, 0x41, 0x45, 0x4B, 0x31});
        LED_FONT.put('4', new int[]{0x18, 0x14, 0x12, 0x7F, 0x10});
        LED_FONT.put('5', new int[]{0x27, 0x45, 0x45, 0x45, 0x39});
        LED_FONT.put('6', new int[]{0x3C, 0x4A, 0x49, 0x49, 0x30});
        LED_FONT.put('7', new int[]{0x01, 0x71, 0x09, 0x05, 0x03});
        LED_FONT.put('8', new int[]{0x36, 0x49, 0x49, 0x49, 0x36});
        LED_FONT.put('9', new int[]{0x06, 0x49, 0x49, 0x29, 0x1E});
        LED_FONT.put('A', new int[]{0x7C, 0x12, 0x11, 0x12, 0x7C});
        LED_FONT.put('B', new int[]{0x7F, 0x49, 0x49, 0x49, 0x36});
        LED_FONT.put('C', new int[]{0x3E, 0x41, 0x41, 0x41, 0x22});
        LED_FONT.put('D', new int[]{0x7F, 0x41, 0x41, 0x22, 0x1C});
        LED_FONT.put('E', new int[]{0x7F, 0x49, 0x49, 0x49, 0x41});
        LED_FONT.put('F', new int[]{0x7F, 0x09, 0x09, 0x09, 0x01});
        LED_FONT.put('G', new int[]{0x3E, 0x41, 0x49, 0x49, 0x7A});
        LED_FONT.put('H', new int[]{0x7F, 0x08, 0x08, 0x08, 0x7F});
        LED_FONT.put('I', new int[]{0x00, 0x41, 0x7F, 0x41, 0x00});
        LED_FONT.put('J', new int[]{0x20, 0x40, 0x41, 0x3F, 0x01});
        LED_FONT.put('K', new int[]{0x7F, 0x08, 0x14, 0x22, 0x41});
        LED_FONT.put('L', new int[]{0x7F, 0x40, 0x40, 0x40, 0x40});
        LED_FONT.put('M', new int[]{0x7F, 0x02, 0x0C, 0x02, 0x7F});
        LED_FONT.put('N', new int[]{0x7F, 0x04, 0x08, 0x10, 0x7F});
        LED_FONT.put('O', new int[]{0x3E, 0x41, 0x41, 0x41, 0x3E});
        LED_FONT.put('P', new int[]{0x7F, 0x09, 0x09, 0x09, 0x06});
        LED_FONT.put('Q', new int[]{0x3E, 0x41, 0x51, 0x21, 0x5E});
        LED_FONT.put('R', new int[]{0x7F, 0x09, 0x19, 0x29, 0x46});
        LED_FONT.put('S', new int[]{0x26, 0x49, 0x49, 0x49, 0x32});
        LED_FONT.put('T', new int[]{0x01, 0x01, 0x7F, 0x01, 0x01});
        LED_FONT.put('U', new int[]{0x3F, 0x40, 0x40, 0x40, 0x3F});
        LED_FONT.put('V', new int[]{0x1F, 0x20, 0x40, 0x20, 0x1F});
        LED_FONT.put('W', new int[]{0x3F, 0x40, 0x38, 0x40, 0x3F});
        LED_FONT.put('X', new int[]{0x63, 0x14, 0x08, 0x14, 0x63});
        LED_FONT.put('Y', new int[]{0x07, 0x08, 0x70, 0x08, 0x07});
        LED_FONT.put('Z', new int[]{0x61, 0x51, 0x49, 0x45, 0x43});
        LED_FONT.put(':', new int[]{0x00, 0x36, 0x36, 0x00, 0x00});
        LED_FONT.put('-', new int[]{0x08, 0x08, 0x08, 0x08, 0x08});
        LED_FONT.put('|', new int[]{0x00, 0x00, 0x7F, 0x00, 0x00});
        LED_FONT.put('(', new int[]{0x00, 0x1C, 0x22, 0x41, 0x00});
        LED_FONT.put(')', new int[]{0x00, 0x41, 0x22, 0x1C, 0x00});
    }

    public ScrollingLEDBoard() {
        setPreferredSize(new Dimension(900, 65));
        setBackground(new Color(10, 10, 12));

        timer = new Timer(25, e -> {
            scrollX -= 3;
            if (scrollX < -2500) scrollX = getWidth();
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int dotSize = 4;
        int dotGap = 2;
        int startY = 10;
        int currentX = scrollX;

        Color activeAmber = new Color(255, 170, 0);
        Color inactiveDot = new Color(30, 30, 35);

        char[] chars = message.toUpperCase().toCharArray();
        for (char c : chars) {
            int[] fontData = LED_FONT.getOrDefault(c, LED_FONT.get(' '));

            for (int col = 0; col < 5; col++) {
                int colBitmask = fontData[col];
                for (int row = 0; row < 7; row++) {
                    boolean isLit = ((colBitmask >> row) & 1) == 1;

                    int x = currentX + col * (dotSize + dotGap);
                    int y = startY + row * (dotSize + dotGap);

                    if (x >= -10 && x <= getWidth() + 10) {
                        g2.setColor(isLit ? activeAmber : inactiveDot);
                        g2.fillRect(x, y, dotSize, dotSize);
                    }
                }
            }
            currentX += 5 * (dotSize + dotGap) + (dotSize + dotGap * 2);
        }
    }
}

// --- 3. External Web Image Loader Panel ---
class URLQRCodePanel extends JPanel {
    private BufferedImage qrImage;

    public URLQRCodePanel(String imageUrl) {
        setPreferredSize(new Dimension(200, 200));
        setBackground(Color.WHITE);
        setBorder(new LineBorder(new Color(226, 19, 110), 2));

        try {
            qrImage = ImageIO.read(URI.create(imageUrl).toURL());
        } catch (Exception e) {
            qrImage = null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (qrImage != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int margin = 10;
            g2.drawImage(qrImage, margin, margin, getWidth() - (margin * 2), getHeight() - (margin * 2), this);
        } else {
            g2.setColor(Color.GRAY);
            g2.drawString("Connecting to QR Image...", 20, getHeight() / 2);
        }
    }
}

// --- 4. Real Vector Barcode Graphic Component ---
class BarcodePanel extends JPanel {
    private String code;

    public BarcodePanel(String code) {
        this.code = code;
        setPreferredSize(new Dimension(300, 55));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLACK);

        int startX = 25;
        int height = 35;
        int seed = Math.abs(code.hashCode());

        for (int i = 0; i < 60; i++) {
            int thickness = ((seed + i * 13) % 3) + 1;
            int gap = ((seed + i * 7) % 3) + 1;
            g2.fillRect(startX, 5, thickness, height);
            startX += thickness + gap;
            if (startX > getWidth() - 25) break;
        }

        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2.drawString("* " + code + " *", getWidth() / 2 - 45, height + 16);
    }
}

// --- 5. Modern Seat Status Badge UI Component ---
class SeatStatusBadge extends JPanel {
    private JLabel textLabel;
    private JPanel dotIndicator;
    private JProgressBar progressBar;
    private JPanel badgeContainer;

    public SeatStatusBadge() {
        setLayout(new BorderLayout(8, 6));
        setOpaque(false);

        badgeContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        badgeContainer.setBackground(new Color(240, 253, 244));
        badgeContainer.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(187, 247, 208), 1, true),
                new EmptyBorder(2, 8, 2, 10)
        ));

        dotIndicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getForeground());
                g2.fillOval(0, 0, 8, 8);
            }
        };
        dotIndicator.setPreferredSize(new Dimension(8, 8));
        dotIndicator.setOpaque(false);
        dotIndicator.setForeground(new Color(22, 163, 74));

        textLabel = new JLabel("Available: -- / --");
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        textLabel.setForeground(new Color(21, 128, 61));

        badgeContainer.add(dotIndicator);
        badgeContainer.add(textLabel);

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(210, 4));
        progressBar.setForeground(new Color(22, 163, 74));
        progressBar.setBackground(new Color(226, 232, 240));
        progressBar.setBorderPainted(false);

        add(badgeContainer, BorderLayout.WEST);
        add(progressBar, BorderLayout.SOUTH);
    }

    public void updateStatus(int available, int total) {
        textLabel.setText("Available: " + available + " / " + total + " Seats");
        int percent = (int) (((double) available / total) * 100);
        progressBar.setValue(percent);

        if (available <= 5) {
            badgeContainer.setBackground(new Color(254, 242, 242));
            badgeContainer.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(254, 202, 202), 1, true),
                    new EmptyBorder(2, 8, 2, 10)
            ));
            textLabel.setForeground(new Color(185, 28, 28));
            dotIndicator.setForeground(new Color(220, 38, 38));
            progressBar.setForeground(new Color(220, 38, 38));
        } else {
            badgeContainer.setBackground(new Color(240, 253, 244));
            badgeContainer.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(187, 247, 208), 1, true),
                    new EmptyBorder(2, 8, 2, 10)
            ));
            textLabel.setForeground(new Color(21, 128, 61));
            dotIndicator.setForeground(new Color(22, 163, 74));
            progressBar.setForeground(new Color(22, 163, 74));
        }
        repaint();
    }
}

// --- 6. REALISTIC DYNAMIC ROUTE MAP WITH UNIQUE CURVE PER OPERATOR ---
class InteractiveRouteMapPanel extends JPanel {
    private String operator;
    private String startStation;
    private String endStation;

    public InteractiveRouteMapPanel() {
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
    }

    public void updateMap(String operator, String startStation, String endStation) {
        this.operator = operator;
        this.startStation = startStation;
        this.endStation = endStation;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- SUBTLE GIS MAP BACKGROUND TEXTURE ---
        // 1. Grid Lines
        g2.setColor(new Color(241, 245, 249));
        for (int i = 0; i < getWidth(); i += 24) g2.drawLine(i, 0, i, getHeight());
        for (int i = 0; i < getHeight(); i += 24) g2.drawLine(0, i, getWidth(), i);

        // 2. Realistic River Vector Overlay
        g2.setColor(new Color(226, 232, 240));
        g2.setStroke(new BasicStroke(18, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D river = new Path2D.Double();
        river.moveTo(getWidth() * 0.8, 0);
        river.curveTo(getWidth() * 0.5, getHeight() * 0.35, getWidth() * 0.9, getHeight() * 0.65, getWidth() * 0.6, getHeight());
        g2.draw(river);

        // 3. Secondary Highway Roads
        g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(238, 242, 246));
        g2.drawLine(0, (int)(getHeight() * 0.3), getWidth(), (int)(getHeight() * 0.42));
        g2.drawLine(0, (int)(getHeight() * 0.72), getWidth(), (int)(getHeight() * 0.6));

        if (operator == null) return;

        String[] stations = TransportRegistry.getStationsForOperator(operator);
        if (stations == null || stations.length == 0) return;

        int count = stations.length;
        int startY = 40;
        int endY = getHeight() - 40;
        int availableHeight = endY - startY;
        int stepY = count > 1 ? availableHeight / (count - 1) : 0;

        int startIdx = -1, endIdx = -1;
        for (int i = 0; i < count; i++) {
            if (stations[i].equals(startStation)) startIdx = i;
            if (stations[i].equals(endStation)) endIdx = i;
        }

        int minIdx = Math.min(startIdx, endIdx);
        int maxIdx = Math.max(startIdx, endIdx);

        int[] xPos = new int[count];
        int[] yPos = new int[count];

        int baseX = 100;
        int seed = Math.abs(operator.hashCode());

        // --- UNIQUE GEOMETRY GENERATOR PER OPERATOR ---
        for (int i = 0; i < count; i++) {
            double factor = (double) i / Math.max(1, count - 1);
            int curveOffset = 0;

            if (operator.contains("Metro")) {
                // METRO RAIL: Authentic 45-degree geometric transit turns
                if (factor < 0.25) {
                    curveOffset = (int) (factor * 4 * 40);
                } else if (factor < 0.6) {
                    curveOffset = 40;
                } else if (factor < 0.85) {
                    curveOffset = 40 - (int) ((factor - 0.6) / 0.25 * 60);
                } else {
                    curveOffset = -20;
                }
            } else if (operator.contains("Raida")) {
                // RAIDA EXPRESS: Double S-curve highway drift
                curveOffset = (int) (Math.sin(factor * Math.PI * 2.5) * 45 + Math.cos(factor * Math.PI * 1.2) * 20);
            } else if (operator.contains("Sakura")) {
                // SAKURA PARIBAHAN: Wide long-distance sweeping arc
                curveOffset = (int) (Math.sin(factor * Math.PI) * 75);
            } else {
                // BRTC BUS & OTHERS: Unique seed-based wave pattern
                double freq = 1.2 + (seed % 3) * 0.6;
                int amp = 30 + (seed % 35);
                curveOffset = (int) (Math.sin(factor * Math.PI * freq) * amp);
            }

            xPos[i] = baseX + curveOffset;
            yPos[i] = startY + i * stepY;
        }

        Color tealTrack = new Color(13, 148, 136);      // Vibrant Teal Line
        Color inactiveTrack = new Color(148, 163, 184); // Unused Track Gray
        Color orangeNode = new Color(245, 158, 11);     // Active Pin Orange

        // Build Curved Path
        Path2D fullTrack = new Path2D.Double();
        fullTrack.moveTo(xPos[0], yPos[0]);
        for (int i = 0; i < count - 1; i++) {
            int cx1 = xPos[i];
            int cy1 = (yPos[i] + yPos[i + 1]) / 2;
            int cx2 = xPos[i + 1];
            int cy2 = (yPos[i] + yPos[i + 1]) / 2;
            fullTrack.curveTo(cx1, cy1, cx2, cy2, xPos[i + 1], yPos[i + 1]);
        }

        // 1. Draw Inactive Track
        g2.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(inactiveTrack);
        g2.draw(fullTrack);

        // 2. Draw Active Path Segment
        if (startIdx != -1 && endIdx != -1 && startIdx != endIdx) {
            Path2D activeTrack = new Path2D.Double();
            activeTrack.moveTo(xPos[minIdx], yPos[minIdx]);
            for (int i = minIdx; i < maxIdx; i++) {
                int cx1 = xPos[i];
                int cy1 = (yPos[i] + yPos[i + 1]) / 2;
                int cx2 = xPos[i + 1];
                int cy2 = (yPos[i] + yPos[i + 1]) / 2;
                activeTrack.curveTo(cx1, cy1, cx2, cy2, xPos[i + 1], yPos[i + 1]);
            }
            g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(tealTrack);
            g2.draw(activeTrack);
        } else {
            g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(tealTrack);
            g2.draw(fullTrack);
        }

        // 3. Render Station Nodes & Text Labels
        for (int i = 0; i < count; i++) {
            boolean isStart = (i == startIdx);
            boolean isEnd = (i == endIdx);
            boolean isSelected = isStart || isEnd;

            if (isSelected) {
                int size = 20;
                g2.setColor(orangeNode);
                g2.fillOval(xPos[i] - size / 2, yPos[i] - size / 2, size, size);
                g2.setStroke(new BasicStroke(3));
                g2.setColor(tealTrack);
                g2.drawOval(xPos[i] - size / 2, yPos[i] - size / 2, size, size);
            } else {
                int size = 14;
                g2.setColor(Color.WHITE);
                g2.fillOval(xPos[i] - size / 2, yPos[i] - size / 2, size, size);
                g2.setStroke(new BasicStroke(3));
                g2.setColor(tealTrack);
                g2.drawOval(xPos[i] - size / 2, yPos[i] - size / 2, size, size);
            }

            g2.setFont(new Font("Segoe UI", isSelected ? Font.BOLD : Font.BOLD, 12));
            g2.setColor(isSelected ? new Color(15, 23, 42) : new Color(51, 65, 85));
            g2.drawString(stations[i], xPos[i] + 20, yPos[i] + 4);
        }
    }
}

// --- 7. Main Application GUI ---
public class SmartTransportApp extends JFrame {

    private JComboBox<String> operatorBox, startStationBox, endStationBox;
    private JTextField passengerField;
    private SeatStatusBadge seatStatusBadge;
    private InteractiveRouteMapPanel mapPanel;

    private final Color primaryNavy = new Color(15, 23, 42);     
    private final Color accentBlue = new Color(37, 99, 235);    
    private final Color bkashPink = new Color(226, 19, 110);    
    private final Color bgLight = new Color(241, 245, 249);     
    private final Color cardBg = Color.WHITE;
    private final Color textDark = new Color(30, 41, 59);

    private final String BRITANNICA_QR_URL = "https://cdn.britannica.com/17/155017-050-9AC96FC8/Example-QR-code.jpg";

    public SmartTransportApp() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        setTitle("Smart Transit Kiosk Terminal - Station Kiosk #04");
        setSize(1080, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(bgLight);

        // TOP NAVIGATION & MATRIX LED BOARD
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(primaryNavy);

        JPanel headerWidget = new JPanel(new BorderLayout());
        headerWidget.setOpaque(false);
        headerWidget.setBorder(new EmptyBorder(12, 20, 10, 20));

        JLabel appBrand = new JLabel("SMART TRANSIT KIOSK | KIOSK #04");
        appBrand.setFont(new Font("Segoe UI", Font.BOLD, 16));
        appBrand.setForeground(Color.WHITE);

        JPanel statusWidget = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statusWidget.setOpaque(false);

        headerWidget.add(appBrand, BorderLayout.WEST);
        headerWidget.add(statusWidget, BorderLayout.EAST);

        ScrollingLEDBoard ledBoard = new ScrollingLEDBoard();

        topContainer.add(headerWidget, BorderLayout.NORTH);
        topContainer.add(ledBoard, BorderLayout.SOUTH);

        // MAIN WORKSPACE
        JPanel mainWorkspace = new JPanel(new GridLayout(1, 2, 18, 0));
        mainWorkspace.setOpaque(false);
        mainWorkspace.setBorder(new EmptyBorder(15, 20, 15, 20));

        // LEFT PANEL: Booking Form
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(cardBg);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(25, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        JLabel formTitle = new JLabel("Issue Transit Ticket Pass");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        formTitle.setForeground(textDark);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 15, 5);
        formCard.add(formTitle, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 5, 8, 5);

        Dimension fieldSize = new Dimension(220, 35);

        String[] operators = TransportRegistry.getOperators();
        operatorBox = new JComboBox<>(operators);
        operatorBox.setPreferredSize(fieldSize);
        addFormRow(formCard, gbc, 1, "Select Operator:", operatorBox);

        startStationBox = new JComboBox<>();
        startStationBox.setPreferredSize(fieldSize);
        endStationBox = new JComboBox<>();
        endStationBox.setPreferredSize(fieldSize);

        addFormRow(formCard, gbc, 2, "From Station:", startStationBox);
        addFormRow(formCard, gbc, 3, "To Station:", endStationBox);

        seatStatusBadge = new SeatStatusBadge();
        addFormRow(formCard, gbc, 4, "Seat Status:", seatStatusBadge);

        passengerField = new JTextField("1");
        passengerField.setPreferredSize(fieldSize);
        passengerField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passengerField.selectAll();
            }
        });
        addFormRow(formCard, gbc, 5, "Passengers:", passengerField);

        JButton payBtn = createCustomButton("Proceed to Digital Payment", accentBlue, Color.WHITE);
        payBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        payBtn.setPreferredSize(new Dimension(0, 45));
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        formCard.add(payBtn, gbc);

        // RIGHT PANEL: DYNAMIC MAP PANEL
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setOpaque(false);

        mapPanel = new InteractiveRouteMapPanel();
        rightContainer.add(mapPanel, BorderLayout.CENTER);

        mainWorkspace.add(formCard);
        mainWorkspace.add(rightContainer);

        add(topContainer, BorderLayout.NORTH);
        add(mainWorkspace, BorderLayout.CENTER);

        // LISTENERS
        operatorBox.addActionListener(e -> updateStationAndSeatOptions());
        startStationBox.addActionListener(e -> updateMap());
        endStationBox.addActionListener(e -> updateMap());

        payBtn.addActionListener(e -> initiateBKashPayment());

        updateStationAndSeatOptions();
    }

    private void updateStationAndSeatOptions() {
        String selectedOperator = (String) operatorBox.getSelectedItem();
        if (selectedOperator == null) return;

        String[] stations = TransportRegistry.getStationsForOperator(selectedOperator);
        startStationBox.removeAllItems();
        endStationBox.removeAllItems();

        for (String station : stations) {
            startStationBox.addItem(station);
            endStationBox.addItem(station);
        }

        if (stations.length > 1) {
            endStationBox.setSelectedIndex(stations.length - 1);
        }

        updateSeatLabelDisplay(selectedOperator);
        updateMap();
    }

    private void updateMap() {
        String operator = (String) operatorBox.getSelectedItem();
        String start = (String) startStationBox.getSelectedItem();
        String end = (String) endStationBox.getSelectedItem();
        mapPanel.updateMap(operator, start, end);
    }

    private void updateSeatLabelDisplay(String operator) {
        Vehicle vehicle = TransportRegistry.getVehicleForOperator(operator);
        if (vehicle != null) {
            seatStatusBadge.updateStatus(vehicle.getAvailableSeats(), vehicle.getTotalSeats());
        }
    }

    private JButton createCustomButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent comp) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(textDark);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(comp, gbc);
    }

    private void initiateBKashPayment() {
        int passengers = 1;
        try {
            passengers = Integer.parseInt(passengerField.getText().trim());
            if (passengers <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "দয়া করে সঠিক যাত্রীর সংখ্যা লিখুন!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String operator = (String) operatorBox.getSelectedItem();
        String from = (String) startStationBox.getSelectedItem();
        String to = (String) endStationBox.getSelectedItem();

        if (from == null || to == null || from.equals(to)) {
            JOptionPane.showMessageDialog(this, "সোর্স ও ডেস্টিনেশন একই হতে পারবে না!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Vehicle vehicle = TransportRegistry.getVehicleForOperator(operator);

        if (passengers > vehicle.getAvailableSeats()) {
            JOptionPane.showMessageDialog(this,
                    "দুঃখিত! " + operator + " -এ মাত্র " + vehicle.getAvailableSeats() + " টি সিট খালি আছে।",
                    "Seat Overflow Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double distance = TransportRegistry.calculateDistance(operator, from, to);
        double baseFare = vehicle.calculateFare(distance);
        double totalFare = baseFare * passengers;
        long ticketId = (long) (Math.random() * 90000000L) + 10000000L;

        showBKashPaymentModal(ticketId, vehicle, from, to, passengers, distance, totalFare);
    }

    private void showBKashPaymentModal(long ticketId, Vehicle vehicle, String from, String to, int passengers, double dist, double totalFare) {
        JDialog payDialog = new JDialog(this, "bKash Merchant Payment Gateway", true);
        payDialog.setSize(400, 520);
        payDialog.setLocationRelativeTo(this);
        payDialog.setLayout(new BorderLayout());

        JPanel bkashHeader = new JPanel(new GridLayout(2, 1, 2, 2));
        bkashHeader.setBackground(bkashPink);
        bkashHeader.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel logoText = new JLabel("bKash Merchant Pay", SwingConstants.CENTER);
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoText.setForeground(Color.WHITE);

        JLabel merchantName = new JLabel("Merchant: Smart Transit Kiosk Terminal", SwingConstants.CENTER);
        merchantName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        merchantName.setForeground(new Color(255, 230, 240));

        bkashHeader.add(logoText);
        bkashHeader.add(merchantName);

        JPanel bodyContainer = new JPanel();
        bodyContainer.setLayout(new BoxLayout(bodyContainer, BoxLayout.Y_AXIS));
        bodyContainer.setBackground(Color.WHITE);
        bodyContainer.setBorder(new EmptyBorder(15, 20, 20, 20));

        JLabel amountLbl = new JLabel(String.format("Total Payable: ৳ %.2f", totalFare), SwingConstants.CENTER);
        amountLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        amountLbl.setForeground(textDark);
        amountLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instruction = new JLabel("Scan QR Code using bKash App to pay", SwingConstants.CENTER);
        instruction.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        instruction.setForeground(Color.GRAY);
        instruction.setAlignmentX(Component.CENTER_ALIGNMENT);

        URLQRCodePanel qrPanel = new URLQRCodePanel(BRITANNICA_QR_URL);
        qrPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton confirmPayBtn = createCustomButton("Confirm & Complete Payment", bkashPink, Color.WHITE);
        confirmPayBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmPayBtn.setPreferredSize(new Dimension(300, 44));
        confirmPayBtn.setMaximumSize(new Dimension(340, 44));
        confirmPayBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        bodyContainer.add(amountLbl);
        bodyContainer.add(Box.createRigidArea(new Dimension(0, 4)));
        bodyContainer.add(instruction);
        bodyContainer.add(Box.createRigidArea(new Dimension(0, 15)));
        bodyContainer.add(qrPanel);
        bodyContainer.add(Box.createRigidArea(new Dimension(0, 20)));
        bodyContainer.add(confirmPayBtn);

        payDialog.add(bkashHeader, BorderLayout.NORTH);
        payDialog.add(bodyContainer, BorderLayout.CENTER);

        confirmPayBtn.addActionListener(e -> {
            payDialog.dispose();
            completeTransaction(ticketId, vehicle, from, to, passengers, dist, totalFare);
        });

        payDialog.setVisible(true);
    }

    private void completeTransaction(long ticketId, Vehicle vehicle, String from, String to, int passengers, double dist, double totalFare) {
        vehicle.bookSeats(passengers);
        updateSeatLabelDisplay(vehicle.getName());
        showPrintTicketDialog(ticketId, vehicle.getName(), from, to, passengers, dist, totalFare);
    }

    private void showPrintTicketDialog(long ticketId, String mode, String from, String to, int count, double dist, double fare) {
        JDialog printDialog = new JDialog(this, "Printable Digital Ticket Receipt", true);
        printDialog.setSize(380, 520);
        printDialog.setLocationRelativeTo(this);
        printDialog.setLayout(new BorderLayout());

        String receiptContent = String.format(
            "    ==========================================\n" +
            "                  RAPID TRANSIT PASS          \n" +
            "                  STATION KIOSK #04            \n" +
            "    ==========================================\n" +
            "    Issue Time  : %s\n" +
            "    Ticket Ref  : #%d\n" +
            "    Payment     : bKash Merchant Pay (PAID)\n" +
            "    ------------------------------------------\n" +
            "    Operator    : %s\n" +
            "    Origin      : %s\n" +
            "    Destination : %s\n" +
            "    Distance    : %.1f KM\n" +
            "    Passengers  : %d\n" +
            "    ------------------------------------------\n" +
            "    TOTAL FARE  : ৳ %.2f (PAID)\n" +
            "    ------------------------------------------\n" +
            "    Status      : VALID FOR SINGLE ENTRY\n" +
            "    ==========================================",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            ticketId, mode, from, to, dist, count, fare
        );

        JTextArea receiptArea = new JTextArea(receiptContent);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setEditable(false);
        receiptArea.setBorder(new EmptyBorder(10, 10, 5, 10));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(receiptArea, BorderLayout.CENTER);

        BarcodePanel barcodePanel = new BarcodePanel(String.valueOf(ticketId));
        centerPanel.add(barcodePanel, BorderLayout.SOUTH);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton printBtn = createCustomButton("Save PDF & Print", accentBlue, Color.WHITE);
        JButton closeBtn = createCustomButton("Close", Color.GRAY, Color.WHITE);

        printBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(printDialog, "Ticket sent to printer", "Print Success", JOptionPane.INFORMATION_MESSAGE);
            printDialog.dispose();
        });
        closeBtn.addActionListener(e -> printDialog.dispose());

        btnPanel.add(printBtn);
        btnPanel.add(closeBtn);

        printDialog.add(new JScrollPane(centerPanel), BorderLayout.CENTER);
        printDialog.add(btnPanel, BorderLayout.SOUTH);
        printDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SmartTransportApp().setVisible(true));
    }
}