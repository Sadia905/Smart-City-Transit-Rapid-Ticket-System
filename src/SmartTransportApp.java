import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
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

    // Helper Method to Round to Nearest 5 (e.g., 13 -> 15, 27 -> 25)
    protected double roundToNearestFive(double amount) {
        return Math.round(amount / 5.0) * 5;
    }

    public abstract double calculateFare(double distance);
}

class BRTCBus extends Vehicle {
    public BRTCBus() { super("BRTC Bus", 40, 18); }
    @Override 
    public double calculateFare(double distance) { 
        double rawFare = Math.max(15.0, distance * 3.5); 
        return roundToNearestFive(rawFare);
    }
}

class SakuraBus extends Vehicle {
    public SakuraBus() { super("Sakura Paribahan", 36, 4); }
    @Override 
    public double calculateFare(double distance) { 
        double rawFare = Math.max(50.0, distance * 3.2); 
        return roundToNearestFive(rawFare);
    }
}

class RaidaBus extends Vehicle {
    public RaidaBus() { super("Raida Express", 45, 25); }
    @Override 
    public double calculateFare(double distance) { 
        double rawFare = Math.max(15.0, distance * 2.8); 
        return roundToNearestFive(rawFare);
    }
}

class MetroRail extends Vehicle {
    public MetroRail() { super("Rapid Metro Rail", 150, 60); }
    @Override 
    public double calculateFare(double distance) { 
        double rawFare = 20.0 + (distance * 5.0); 
        return roundToNearestFive(rawFare);
    }
}

class TransportRegistry {
    public static final String KIOSK_LOCATION = "Agargaon"; 

    private static final Map<String, Map<String, Integer>> operatorRoutes = new LinkedHashMap<>();
    private static final Map<String, Vehicle> operatorVehicles = new LinkedHashMap<>();

    static {
        operatorVehicles.put("BRTC Bus", new BRTCBus());
        operatorVehicles.put("Rapid Metro Rail", new MetroRail());
        operatorVehicles.put("Sakura Paribahan", new SakuraBus());
        operatorVehicles.put("Raida Express", new RaidaBus());

        Map<String, Integer> brtc = new LinkedHashMap<>();
        brtc.put("Agargaon", 0);
        brtc.put("Bijoy Sarani", 2);
        brtc.put("Farmgate", 5);
        brtc.put("Shahabag", 9);
        brtc.put("Motijheel", 15);
        operatorRoutes.put("BRTC Bus", brtc);

        Map<String, Integer> metro = new LinkedHashMap<>();
        metro.put("Agargaon", 0);
        metro.put("Bijoy Sarani", 2);
        metro.put("Farmgate", 4);
        metro.put("Kawran Bazar", 6);
        metro.put("Shahabag", 8);
        metro.put("Dhaka University", 10);
        metro.put("Bangladesh Secretariat", 12);
        metro.put("Motijheel", 14);
        metro.put("Kamalapur", 16);
        operatorRoutes.put("Rapid Metro Rail", metro);

        Map<String, Integer> sakura = new LinkedHashMap<>();
        sakura.put("Barishal Terminal", 180);
        sakura.put("Padma Bridge Toll Gate", 45);
        sakura.put("Agargaon", 0);
        sakura.put("Gabtoli Counter", 8);
        operatorRoutes.put("Sakura Paribahan", sakura);

        Map<String, Integer> raida = new LinkedHashMap<>();
        raida.put("Agargaon", 0);
        raida.put("Kuril Flyover", 8);
        raida.put("Badda Link Road", 14);
        raida.put("Sayedabad", 25);
        operatorRoutes.put("Raida Express", raida);
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

    private static final Map<Character, int[]> LED_FONT = new LinkedHashMap<>();
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
        Dimension size = new Dimension(220, 220);
        setPreferredSize(size);
        setMaximumSize(size);
        setMinimumSize(size);
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int margin = 8;
            int availWidth = getWidth() - (margin * 2);
            int availHeight = getHeight() - (margin * 2);

            int side = Math.min(availWidth, availHeight);
            int x = (getWidth() - side) / 2;
            int y = (getHeight() - side) / 2;

            g2.drawImage(qrImage, x, y, side, side, this);
        } else {
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();
            String txt = "Connecting to QR Image...";
            g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2, getHeight() / 2);
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

// --- 5. UPDATED MODERN SEAT STATUS UI COMPONENT ---
class SeatStatusBadge extends JPanel {
    private JLabel availLabel;
    private JLabel totalLabel;
    private JPanel dotIndicator;
    private JProgressBar progressBar;
    private JPanel availContainer;

    public SeatStatusBadge() {
        setLayout(new BorderLayout(8, 6));
        setOpaque(false);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        infoPanel.setOpaque(false);

        availContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        availContainer.setBackground(new Color(240, 253, 244));
        availContainer.setBorder(BorderFactory.createCompoundBorder(
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

        availLabel = new JLabel("-- Available");
        availLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        availLabel.setForeground(new Color(21, 128, 61));

        availContainer.add(dotIndicator);
        availContainer.add(availLabel);

        totalLabel = new JLabel("Total: -- Seats");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        totalLabel.setForeground(new Color(100, 116, 139));

        infoPanel.add(availContainer);
        infoPanel.add(totalLabel);

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(220, 4));
        progressBar.setForeground(new Color(22, 163, 74));
        progressBar.setBackground(new Color(226, 232, 240));
        progressBar.setBorderPainted(false);

        add(infoPanel, BorderLayout.WEST);
        add(progressBar, BorderLayout.SOUTH);
    }

    public void updateStatus(int available, int total) {
        availLabel.setText(available + " Available");
        totalLabel.setText("Total: " + total + " Seats");

        int percent = (int) (((double) available / total) * 100);
        progressBar.setValue(percent);

        if (available <= 5) {
            availContainer.setBackground(new Color(254, 242, 242));
            availContainer.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(254, 202, 202), 1, true),
                    new EmptyBorder(2, 8, 2, 10)
            ));
            availLabel.setForeground(new Color(185, 28, 28));
            dotIndicator.setForeground(new Color(220, 38, 38));
            progressBar.setForeground(new Color(220, 38, 38));
        } else {
            availContainer.setBackground(new Color(240, 253, 244));
            availContainer.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(187, 247, 208), 1, true),
                    new EmptyBorder(2, 8, 2, 10)
            ));
            availLabel.setForeground(new Color(21, 128, 61));
            dotIndicator.setForeground(new Color(22, 163, 74));
            progressBar.setForeground(new Color(22, 163, 74));
        }
        repaint();
    }
}

// --- 6. OCTILINEAR TRANSIT ROUTE MAP PANEL ---
class InteractiveRouteMapPanel extends JPanel {
    private String operator;
    private String startStation;
    private String endStation;

    public InteractiveRouteMapPanel() {
        setBackground(Color.WHITE);
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
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (operator == null) return;

        String[] stations = TransportRegistry.getStationsForOperator(operator);
        if (stations == null || stations.length == 0) return;

        int count = stations.length;
        int startIdx = -1, endIdx = -1;
        for (int i = 0; i < count; i++) {
            if (stations[i].equals(startStation)) startIdx = i;
            if (stations[i].equals(endStation)) endIdx = i;
        }

        int minIdx = Math.min(startIdx, endIdx);
        int maxIdx = Math.max(startIdx, endIdx);

        int[] xPos = new int[count];
        int[] yPos = new int[count];

        int panelHeight = getHeight() > 0 ? getHeight() : 600;
        int maxAllowedStep = 105;
        int minStep = 55;
        
        int stepY = count > 1 ? Math.min(maxAllowedStep, Math.max(minStep, (panelHeight - 140) / (count - 1))) : minStep;
        int totalRouteHeight = (count - 1) * stepY;
        int startY = Math.max(50, (panelHeight - totalRouteHeight) / 2);

        for (int i = 0; i < count; i++) {
            yPos[i] = startY + (i * stepY);

            if (i == 0) xPos[i] = 180;                            
            else if (i == 1) xPos[i] = 180;                       
            else if (i == 2) xPos[i] = 230;                        
            else if (i == 3) xPos[i] = 230;                        
            else if (i == 4) xPos[i] = 230;                        
            else if (i == 5) xPos[i] = 280;                        
            else if (i == 6) xPos[i] = 330;                        
            else if (i == 7) xPos[i] = 380;                        
            else xPos[i] = 380 + (i - 7) * 40;
        }

        Color metroGreen = new Color(0, 102, 68);        
        Color activeHighlight = new Color(245, 158, 11); 
        Color inactiveTrack = new Color(203, 213, 225);   

        Path2D fullPath = new Path2D.Double();
        fullPath.moveTo(xPos[0], yPos[0]);
        for (int i = 1; i < count; i++) {
            fullPath.lineTo(xPos[i], yPos[i]);
        }

        g2.setStroke(new BasicStroke(9, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(inactiveTrack);
        g2.draw(fullPath);

        if (startIdx != -1 && endIdx != -1 && startIdx != endIdx) {
            Path2D activePath = new Path2D.Double();
            activePath.moveTo(xPos[minIdx], yPos[minIdx]);
            for (int i = minIdx + 1; i <= maxIdx; i++) {
                activePath.lineTo(xPos[i], yPos[i]);
            }
            g2.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(metroGreen);
            g2.draw(activePath);
        } else {
            g2.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(metroGreen);
            g2.draw(fullPath);
        }

        Vehicle vehicle = TransportRegistry.getVehicleForOperator(operator);

        for (int i = 0; i < count; i++) {
            boolean isStart = (i == startIdx);
            boolean isEnd = (i == endIdx);
            boolean isSelected = isStart || isEnd;

            int px = xPos[i];
            int py = yPos[i];
            String stationName = stations[i];

            g2.setStroke(new BasicStroke(3));
            g2.setColor(metroGreen);
            g2.drawLine(px - 22, py, px, py);

            if (isSelected) {
                int nodeRadius = 22;
                g2.setColor(activeHighlight);
                g2.fillOval(px - nodeRadius / 2, py - nodeRadius / 2, nodeRadius, nodeRadius);
                g2.setStroke(new BasicStroke(4));
                g2.setColor(metroGreen);
                g2.drawOval(px - nodeRadius / 2, py - nodeRadius / 2, nodeRadius, nodeRadius);
            } else {
                int nodeRadius = 18;
                g2.setColor(Color.WHITE);
                g2.fillOval(px - nodeRadius / 2, py - nodeRadius / 2, nodeRadius, nodeRadius);
                g2.setStroke(new BasicStroke(4));
                g2.setColor(metroGreen);
                g2.drawOval(px - nodeRadius / 2, py - nodeRadius / 2, nodeRadius, nodeRadius);
            }

            g2.setColor(isSelected ? new Color(15, 23, 42) : new Color(51, 65, 85));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            int nameWidth = fm.stringWidth(stationName);

            g2.drawString(stationName, px - 30 - nameWidth, py + 5);

            double distFromAgargaon = TransportRegistry.calculateDistance(operator, TransportRegistry.KIOSK_LOCATION, stationName);

            String fareText;
            if (stationName.equalsIgnoreCase(TransportRegistry.KIOSK_LOCATION)) {
                fareText = "Start";
            } else {
                int fare = (int) Math.round(vehicle != null ? vehicle.calculateFare(distFromAgargaon) : 0);
                fareText = fare + " ৳";
            }

            int badgeX = px + 18;
            int badgeY = py - 13;
            int badgeW = 60;
            int badgeH = 26;

            boolean isCurrentStation = stationName.equalsIgnoreCase(TransportRegistry.KIOSK_LOCATION);
            g2.setColor(isCurrentStation ? activeHighlight : metroGreen);
            g2.fill(new RoundRectangle2D.Double(badgeX, badgeY, badgeW, badgeH, 12, 12));

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fmBadge = g2.getFontMetrics();
            int textX = badgeX + (badgeW - fmBadge.stringWidth(fareText)) / 2;
            g2.drawString(fareText, textX, badgeY + 17);
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
    private final Color actionGreen = new Color(22, 163, 74);    
    private final Color bkashPink = new Color(226, 19, 110);    
    private final Color bgLight = new Color(241, 245, 249);     
    private final Color cardBg = Color.WHITE;
    private final Color textDark = new Color(30, 41, 59);

    private final String BRITANNICA_QR_URL = "https://cdn.britannica.com/17/155017-050-9AC96FC8/Example-QR-code.jpg";

    public SmartTransportApp() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        setTitle("Smart Transit Kiosk Terminal - Station Kiosk #04 (" + TransportRegistry.KIOSK_LOCATION + ")");
        setSize(1080, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(bgLight);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(primaryNavy);

        JPanel headerWidget = new JPanel(new BorderLayout());
        headerWidget.setOpaque(false);
        headerWidget.setBorder(new EmptyBorder(12, 20, 10, 20));

        JLabel appBrand = new JLabel("SMART TRANSIT KIOSK | KIOSK #04 (" + TransportRegistry.KIOSK_LOCATION + " Station)");
        appBrand.setFont(new Font("Segoe UI", Font.BOLD, 16));
        appBrand.setForeground(Color.WHITE);

        JPanel statusWidget = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statusWidget.setOpaque(false);

        headerWidget.add(appBrand, BorderLayout.WEST);
        headerWidget.add(statusWidget, BorderLayout.EAST);

        ScrollingLEDBoard ledBoard = new ScrollingLEDBoard();

        topContainer.add(headerWidget, BorderLayout.NORTH);
        topContainer.add(ledBoard, BorderLayout.SOUTH);

        JPanel mainWorkspace = new JPanel(new GridLayout(1, 2, 18, 0));
        mainWorkspace.setOpaque(false);
        mainWorkspace.setBorder(new EmptyBorder(15, 20, 10, 20));

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
        operatorBox.setSelectedItem("BRTC Bus"); 

        addFormRow(formCard, gbc, 1, "Select Operator:", operatorBox);

        startStationBox = new JComboBox<>();
        startStationBox.setPreferredSize(fieldSize);
        startStationBox.setEnabled(false); 

        endStationBox = new JComboBox<>();
        endStationBox.setPreferredSize(fieldSize);

        addFormRow(formCard, gbc, 2, "From Station (Locked):", startStationBox);
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

        JButton payBtn = createCustomButton("Proceed to Digital Payment", actionGreen, Color.WHITE);
        payBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        payBtn.setPreferredSize(new Dimension(0, 45));
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        formCard.add(payBtn, gbc);

        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setOpaque(false);

        mapPanel = new InteractiveRouteMapPanel();
        rightContainer.add(mapPanel, BorderLayout.CENTER);

        mainWorkspace.add(formCard);
        mainWorkspace.add(rightContainer);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 8));
        footerPanel.setBackground(primaryNavy);
        
        JLabel helpDeskLabel = new JLabel("Need Assistance or Inquiries?");
        helpDeskLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        helpDeskLabel.setForeground(new Color(203, 213, 225));

        JLabel phoneLabel = new JLabel("Helpline: 16247 / +880 9612-000111");
        phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        phoneLabel.setForeground(Color.WHITE);

        JLabel emailLabel = new JLabel("Email: support@smarttransit.bd");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailLabel.setForeground(Color.WHITE);

        footerPanel.add(helpDeskLabel);
        footerPanel.add(phoneLabel);
        footerPanel.add(emailLabel);

        add(topContainer, BorderLayout.NORTH);
        add(mainWorkspace, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

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

        startStationBox.setSelectedItem(TransportRegistry.KIOSK_LOCATION);

        if (stations.length > 1) {
            endStationBox.setSelectedIndex(1);
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
            JOptionPane.showMessageDialog(this, "Please enter a valid passenger count!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String operator = (String) operatorBox.getSelectedItem();
        String from = (String) startStationBox.getSelectedItem();
        String to = (String) endStationBox.getSelectedItem();

        if (from == null || to == null || from.equals(to)) {
            JOptionPane.showMessageDialog(this, "Source and Destination cannot be the same!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Vehicle vehicle = TransportRegistry.getVehicleForOperator(operator);

        if (passengers > vehicle.getAvailableSeats()) {
            JOptionPane.showMessageDialog(this,
                    "Sorry! " + operator + " has only " + vehicle.getAvailableSeats() + " seats available.",
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
        payDialog.setSize(420, 540);
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
        confirmPayBtn.setPreferredSize(new Dimension(320, 44));
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
        JButton printBtn = createCustomButton("Save PDF & Print", actionGreen, Color.WHITE);
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