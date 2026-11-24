package com.smartpark.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.smartpark.db.SupabaseClient;
import com.smartpark.exceptions.*;
import com.smartpark.model.*;
import com.smartpark.service.ParkService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Modern Multi-Pane Management Console for Amusement Park Operations.
 * Features:
 * 1. Deep navy navigation sidebar.
 * 2. Unchanged main operational dashboard (KPIs, live booking form, ride progress bars,
 *    custom Graphics2D chart, and permanent history) with "Add to Waitlist" removed.
 * 3. Ride Management: Add/subtract rides, edit ticket price, view live bookings per ride.
 * 4. Visitor Management: Sequential boarding queue order per attraction (#1, #2...)
 *    with "Board Next Visitor" dispatch.
 * 5. Staff Management: "Assigned Work" tracking, add/subtract staff, edit details & toggle duty status.
 * 6. Payments & Revenue: Today's revenue, total revenue, revenue by ride, and payment history.
 * 7. Integrated 3D Park Experience: 1-click launcher for OpenGL CG Task 4.
 * 8. Cloud History: Supabase connection status, guide, and live sync.
 */
public class ParkDashboard extends JFrame {
    private final ParkService service;
    private final SupabaseClient dbClient;

    // Card Layout Navigation
    private CardLayout cardLayout;
    private JPanel contentCardsPanel;
    private Map<String, JButton> navButtons;
    private String activeNav = "DASHBOARD";

    // Card Identifiers
    public static final String CARD_DASHBOARD = "DASHBOARD";
    public static final String CARD_RIDES = "RIDES";
    public static final String CARD_VISITORS = "VISITORS";
    public static final String CARD_STAFF = "STAFF";
    public static final String CARD_PAYMENTS = "PAYMENTS";
    public static final String CARD_MAINTENANCE = "MAINTENANCE";
    public static final String CARD_REPORTS = "REPORTS";
    public static final String CARD_3D_PARK = "3D_PARK";
    public static final String CARD_CLOUD = "CLOUD";

    // Header Components
    private JLabel clockLabel;
    private JLabel supabaseStatusChip;

    // Main Dashboard KPI Labels
    private JLabel lblTotalRevenue;
    private JLabel lblTotalVisitors;
    private JLabel lblTotalBookings;
    private JLabel lblCancelledCount;
    private JLabel lblTodayRevenue;

    // Booking Form Components (Waitlist removed)
    private JTextField txtVisitorName;
    private JSpinner spinAge;
    private JComboBox<Ride> cmbRides;
    private JSpinner spinGroupSize;
    private JLabel lblUnitPrice;
    private JLabel lblDiscountLine;
    private JLabel lblTotalPayable;
    private JLabel lblAvailableSeats;
    private JButton btnBook;
    private JButton btnUndo;

    // Ride Cards & Chart
    private JPanel rideCardsPanel;
    private RevenueChartPanel chartPanel;

    // History Table (Main Dashboard)
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JTextField txtSearch;
    private JComboBox<String> cmbStatusFilter;

    // Ride Management Components
    private DefaultTableModel ridesTableModel;
    private JTable ridesTable;

    // Visitor Queue Components
    private JComboBox<String> cmbQueueRideFilter;
    private DefaultTableModel visitorQueueTableModel;
    private JTable visitorQueueTable;
    private JLabel lblNextInLineInfo;
    private JTextField txtVisitorQueueSearch;

    // Staff Management Components
    private DefaultTableModel staffTableModel;
    private JTable staffTable;

    // Payments & Revenue Components
    private JLabel lblPayTodayRev;
    private JLabel lblPayTotalRev;
    private JLabel lblPayTotalTx;
    private DefaultTableModel payRidesTableModel;
    private DefaultTableModel paymentsHistoryTableModel;
    private JTextField txtPaySearch;

    public ParkDashboard() {
        this.service = new ParkService();
        File configFile = new File("config/supabase.properties");
        File queueFile = new File("build/pending_offline_bookings.csv");
        this.dbClient = new SupabaseClient(configFile, queueFile);
        this.navButtons = new HashMap<>();

        initLaf();
        initComponents();
        loadSeedData();
        startBackgroundClockAndSync();
    }

    private void initLaf() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {}
    }

    private void initComponents() {
        setTitle("Smart Amusement Park Management System (v2)");
        setSize(1540, 890);
        setMinimumSize(new Dimension(1350, 780));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(245, 247, 250));

        // 1. Left Navigation Sidebar (Ticket Management removed as requested)
        root.add(createSidebarPanel(), BorderLayout.WEST);

        // 2. Main Right Container (Header + CardLayout Content)
        JPanel mainContainer = new JPanel(new BorderLayout(0, 0));
        mainContainer.setOpaque(false);
        mainContainer.add(createHeaderPanel(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentCardsPanel = new JPanel(cardLayout);
        contentCardsPanel.setOpaque(false);

        // Add all distinct section views into CardLayout
        contentCardsPanel.add(createMainDashboardCard(), CARD_DASHBOARD);
        contentCardsPanel.add(createRideManagementCard(), CARD_RIDES);
        contentCardsPanel.add(createVisitorManagementCard(), CARD_VISITORS);
        contentCardsPanel.add(createStaffManagementCard(), CARD_STAFF);
        contentCardsPanel.add(createPaymentsRevenueCard(), CARD_PAYMENTS);
        contentCardsPanel.add(createMaintenanceCard(), CARD_MAINTENANCE);
        contentCardsPanel.add(createReportsAnalyticsCard(), CARD_REPORTS);
        contentCardsPanel.add(create3DParkCard(), CARD_3D_PARK);
        contentCardsPanel.add(createCloudHistoryCard(), CARD_CLOUD);

        mainContainer.add(contentCardsPanel, BorderLayout.CENTER);
        root.add(mainContainer, BorderLayout.CENTER);

        setContentPane(root);
        setActiveNavigation(CARD_DASHBOARD);
    }

    /**
     * Left Navigation Sidebar Panel
     */
    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 0));
        sidebar.setPreferredSize(new Dimension(235, 880));
        sidebar.setBackground(new Color(15, 27, 44)); // Deep dark navy
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(30, 48, 75)));

        // Top Brand Header
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(22, 20, 20, 20));

        JLabel brandTitle = new JLabel("SMART PARK");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brandTitle.setForeground(Color.WHITE);

        JLabel brandSubtitle = new JLabel("AMUSEMENT PARK");
        brandSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        brandSubtitle.setForeground(new Color(140, 160, 185));

        brandPanel.add(brandTitle);
        brandPanel.add(Box.createVerticalStrut(3));
        brandPanel.add(brandSubtitle);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Middle Navigation Menu Items (Ticket Management removed as requested)
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(5, 12, 10, 12));

        addNavItem(menuPanel, "Dashboard", CARD_DASHBOARD, "📊");
        addNavItem(menuPanel, "Ride Management", CARD_RIDES, "🎡");
        addNavItem(menuPanel, "Visitor Management", CARD_VISITORS, "👥");
        addNavItem(menuPanel, "Staff Management", CARD_STAFF, "👔");
        addNavItem(menuPanel, "Payments & Revenue", CARD_PAYMENTS, "💳");
        addNavItem(menuPanel, "Maintenance", CARD_MAINTENANCE, "🔧");
        addNavItem(menuPanel, "Reports & Analytics", CARD_REPORTS, "📈");
        addNavItem(menuPanel, "3D Park Experience", CARD_3D_PARK, "🎮");
        addNavItem(menuPanel, "Cloud History", CARD_CLOUD, "☁️");

        JScrollPane menuScroll = new JScrollPane(menuPanel);
        menuScroll.setOpaque(false);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.setBorder(null);
        menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sidebar.add(menuScroll, BorderLayout.CENTER);

        // Bottom Footer Label
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel footerLbl = new JLabel("SMART PARK MANAGEMENT");
        footerLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        footerLbl.setForeground(new Color(100, 122, 150));
        footerPanel.add(footerLbl, BorderLayout.WEST);
        sidebar.add(footerPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavItem(JPanel container, String text, String key, String icon) {
        JButton btn = new JButton(icon + "  " + text);
        btn.setMaximumSize(new Dimension(210, 38));
        btn.setPreferredSize(new Dimension(210, 38));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(new Color(175, 192, 215));
        btn.setBackground(new Color(15, 27, 44));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!key.equals(activeNav)) {
                    btn.setBackground(new Color(25, 42, 68));
                    btn.setForeground(Color.WHITE);
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!key.equals(activeNav)) {
                    btn.setBackground(new Color(15, 27, 44));
                    btn.setForeground(new Color(175, 192, 215));
                }
            }
        });

        btn.addActionListener(e -> setActiveNavigation(key));
        navButtons.put(key, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(3));
    }

    private void setActiveNavigation(String key) {
        this.activeNav = key;
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton b = entry.getValue();
            if (entry.getKey().equals(key)) {
                b.setBackground(new Color(30, 58, 95));
                b.setForeground(Color.WHITE);
                b.setFont(new Font("Segoe UI", Font.BOLD, 12));
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(52, 152, 219)),
                        new EmptyBorder(8, 10, 8, 14)));
            } else {
                b.setBackground(new Color(15, 27, 44));
                b.setForeground(new Color(175, 192, 215));
                b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                b.setBorder(new EmptyBorder(8, 14, 8, 14));
            }
        }
        cardLayout.show(contentCardsPanel, key);

        // Refresh specific view data on tab selection
        if (CARD_RIDES.equals(key)) refreshRideManagementTable();
        else if (CARD_VISITORS.equals(key)) refreshVisitorQueueView();
        else if (CARD_PAYMENTS.equals(key)) refreshPaymentsView();
        else if (CARD_STAFF.equals(key)) refreshStaffTable();
    }

    /**
     * Top Header Panel
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(new Color(20, 32, 54));
        header.setPreferredSize(new Dimension(1280, 68));
        header.setBorder(new EmptyBorder(10, 24, 10, 24));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        JLabel title = new JLabel("SMART AMUSEMENT PARK MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Integrated Operational Control Console (v2)");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(160, 175, 200));
        left.add(title);
        left.add(sub);
        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        right.setOpaque(false);

        // Quick 3D Park Launcher Button in Header (Runs Task 4)
        JButton btnQuick3D = new JButton("🎮 Run 3D Park (CG Task 4)");
        btnQuick3D.setBackground(new Color(41, 128, 185));
        btnQuick3D.setForeground(Color.WHITE);
        btnQuick3D.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnQuick3D.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnQuick3D.addActionListener(e -> launchCgTask(4));

        JLabel badge = new JLabel(" PARK OPEN: 09:00 - 20:00 ");
        badge.setOpaque(true);
        badge.setBackground(new Color(39, 174, 96));
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setBorder(new EmptyBorder(4, 8, 4, 8));

        supabaseStatusChip = new JLabel(" Cloud: Checking... ");
        supabaseStatusChip.setOpaque(true);
        supabaseStatusChip.setBackground(new Color(108, 117, 125));
        supabaseStatusChip.setForeground(Color.WHITE);
        supabaseStatusChip.setFont(new Font("Segoe UI", Font.BOLD, 11));
        supabaseStatusChip.setBorder(new EmptyBorder(4, 8, 4, 8));

        clockLabel = new JLabel("00:00:00");
        clockLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clockLabel.setForeground(Color.WHITE);

        right.add(btnQuick3D);
        right.add(badge);
        right.add(supabaseStatusChip);
        right.add(clockLabel);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    /**
     * CARD 1: Main Operational Dashboard
     * 100% UNCHANGED interface & working: KPI cards, booking form (without waitlist),
     * ride occupancy progress bars, Graphics2D revenue chart, search & history table.
     */
    private JPanel createMainDashboardCard() {
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(12, 16, 16, 16));

        centerPanel.add(createKpiPanel(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createBookingFormPanel(), createOverviewAndHistoryPanel());
        splitPane.setDividerLocation(380);
        splitPane.setResizeWeight(0.28);
        splitPane.setBorder(null);
        centerPanel.add(splitPane, BorderLayout.CENTER);

        return centerPanel;
    }

    private JPanel createKpiPanel() {
        JPanel kpiRow = new JPanel(new GridLayout(1, 5, 12, 0));
        kpiRow.setOpaque(false);
        kpiRow.setPreferredSize(new Dimension(1280, 85));

        lblTotalRevenue = new JLabel("Rs. 0");
        lblTotalVisitors = new JLabel("0");
        lblTotalBookings = new JLabel("0");
        lblCancelledCount = new JLabel("0");
        lblTodayRevenue = new JLabel("Rs. 0");

        kpiRow.add(buildKpiCard("TOTAL REVENUE", lblTotalRevenue, new Color(41, 128, 185)));
        kpiRow.add(buildKpiCard("TOTAL VISITORS", lblTotalVisitors, new Color(39, 174, 96)));
        kpiRow.add(buildKpiCard("TOTAL BOOKINGS", lblTotalBookings, new Color(142, 68, 173)));
        kpiRow.add(buildKpiCard("CANCELLED", lblCancelledCount, new Color(192, 57, 43)));
        kpiRow.add(buildKpiCard("TODAY'S REVENUE", lblTodayRevenue, new Color(211, 84, 0)));

        return kpiRow;
    }

    private JPanel buildKpiCard(String title, JLabel valueLbl, Color accent) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                new EmptyBorder(10, 14, 10, 14)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(110, 120, 135));

        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLbl.setForeground(accent);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBookingFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel formTitle = new JLabel("VISITOR ENTRY & TICKET BOOKING");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formTitle.setForeground(new Color(30, 40, 60));
        panel.add(formTitle, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 4, 5, 4);

        txtVisitorName = new JTextField();
        spinAge = new JSpinner(new SpinnerNumberModel(20, 1, 100, 1));
        cmbRides = new JComboBox<>(service.getRides());
        cmbRides.setRenderer((list, val, idx, isSel, cellHasFocus) -> {
            JLabel lbl = new JLabel(val != null ? val.getName() + " (Rs. " + val.getUnitPrice() + ")" : "");
            lbl.setBorder(new EmptyBorder(3, 4, 3, 4));
            return lbl;
        });

        spinGroupSize = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        lblUnitPrice = new JLabel("Rs. 100");
        lblDiscountLine = new JLabel("Standard Adult");
        lblTotalPayable = new JLabel("Rs. 100");
        lblAvailableSeats = new JLabel("20");

        int row = 0;
        addFormRow(fields, c, row++, "Visitor Full Name:", txtVisitorName);
        addFormRow(fields, c, row++, "Visitor Age:", spinAge);
        addFormRow(fields, c, row++, "Select Attraction:", cmbRides);
        addFormRow(fields, c, row++, "Group Size (1-10):", spinGroupSize);

        // Preview Sub-panel
        JPanel preview = new JPanel(new GridLayout(4, 2, 6, 4));
        preview.setBackground(new Color(248, 250, 253));
        preview.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 235), 1),
                new EmptyBorder(8, 10, 8, 10)));
        preview.add(new JLabel("Unit Price:")); preview.add(lblUnitPrice);
        preview.add(new JLabel("Fare Concession:")); preview.add(lblDiscountLine);
        preview.add(new JLabel("Total Payable:")); preview.add(lblTotalPayable);
        preview.add(new JLabel("Seats Available:")); preview.add(lblAvailableSeats);

        c.gridx = 0; c.gridy = row++; c.gridwidth = 2;
        fields.add(preview, c);

        // Action Buttons: ADD TO WAITLIST REMOVED AS REQUESTED
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        btnPanel.setOpaque(false);

        btnBook = new JButton("Book Tickets");
        btnBook.setBackground(new Color(39, 174, 96));
        btnBook.setForeground(Color.WHITE);
        btnBook.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBook.setPreferredSize(new Dimension(280, 34));

        JPanel subBtnPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        subBtnPanel.setOpaque(false);
        btnUndo = new JButton("Undo Last Action");
        JButton btnReset = new JButton("Reset");
        subBtnPanel.add(btnUndo);
        subBtnPanel.add(btnReset);

        btnPanel.add(btnBook);
        btnPanel.add(subBtnPanel);

        c.gridy = row++;
        fields.add(btnPanel, c);

        // Privacy Consent Notice
        JTextArea txtConsent = new JTextArea(
                "Privacy Consent: Personal data (Name & Age) is recorded strictly for capacity safety " +
                "and ticket verification. Financial booking history is immutable and cannot be wiped.");
        txtConsent.setWrapStyleWord(true);
        txtConsent.setLineWrap(true);
        txtConsent.setEditable(false);
        txtConsent.setOpaque(false);
        txtConsent.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        txtConsent.setForeground(new Color(120, 130, 145));

        c.gridy = row++;
        fields.add(txtConsent, c);

        panel.add(fields, BorderLayout.CENTER);

        // Hook Live Preview Listeners
        cmbRides.addActionListener(e -> updateFarePreview());
        spinAge.addChangeListener(e -> updateFarePreview());
        spinGroupSize.addChangeListener(e -> updateFarePreview());

        btnBook.addActionListener(e -> onBookClicked());
        btnUndo.addActionListener(e -> onUndoClicked());
        btnReset.addActionListener(e -> resetBookingForm());

        updateFarePreview();
        return panel;
    }

    private void addFormRow(JPanel p, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridwidth = 1; c.gridx = 0; c.gridy = row; c.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(lbl, c);
        c.gridx = 1; c.weightx = 0.65;
        p.add(comp, c);
    }

    private JPanel createOverviewAndHistoryPanel() {
        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setOpaque(false);

        // Top: Ride Overview Cards + Revenue Chart
        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);

        rideCardsPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        rideCardsPanel.setOpaque(false);
        top.add(rideCardsPanel, BorderLayout.CENTER);

        chartPanel = new RevenueChartPanel(service);
        top.add(chartPanel, BorderLayout.EAST);
        right.add(top, BorderLayout.NORTH);

        // Bottom: History Table
        right.add(createHistoryTablePanel(), BorderLayout.CENTER);

        refreshRideCards();
        return right;
    }

    private JPanel createHistoryTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                new EmptyBorder(10, 12, 10, 12)));

        // Table toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JLabel lblHist = new JLabel("PERMANENT BOOKING HISTORY");
        lblHist.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHist.setForeground(new Color(40, 50, 70));
        toolbar.add(lblHist, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        controls.setOpaque(false);
        txtSearch = new JTextField(12);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search Name/ID...");
        txtSearch.addCaretListener(e -> filterTable());

        cmbStatusFilter = new JComboBox<>(new String[] {"ALL", "CONFIRMED", "WAITLISTED", "CANCELLED"});
        cmbStatusFilter.addActionListener(e -> filterTable());

        JButton btnCancel = new JButton("Cancel Selected");
        btnCancel.setBackground(new Color(231, 76, 60));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.addActionListener(e -> onCancelSelectedClicked());

        JButton btnExport = new JButton("Export CSV");
        btnExport.addActionListener(e -> onExportCsvClicked());

        JButton btnRefresh = new JButton("Sync Cloud");
        btnRefresh.addActionListener(e -> triggerCloudSync());

        controls.add(new JLabel("Filter:"));
        controls.add(cmbStatusFilter);
        controls.add(txtSearch);
        controls.add(btnCancel);
        controls.add(btnExport);
        controls.add(btnRefresh);

        toolbar.add(controls, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);

        // History Table Setup
        String[] cols = {"ID", "Visitor Name", "Age", "Ride", "Group", "Total (Rs.)", "Status", "Timestamp"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(26);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableSorter = new TableRowSorter<>(tableModel);
        historyTable.setRowSorter(tableSorter);

        historyTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String st = (val != null) ? val.toString() : "";
                if ("CONFIRMED".equalsIgnoreCase(st)) {
                    lbl.setForeground(new Color(39, 174, 96));
                    lbl.setText("● CONFIRMED");
                } else if ("WAITLISTED".equalsIgnoreCase(st)) {
                    lbl.setForeground(new Color(230, 126, 34));
                    lbl.setText("▲ WAITLISTED");
                } else if ("CANCELLED".equalsIgnoreCase(st)) {
                    lbl.setForeground(new Color(192, 57, 43));
                    lbl.setText("✖ CANCELLED");
                }
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 234, 240)));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    /**
     * CARD 2: RIDE MANAGEMENT SECTION
     * Requirements:
     * - Ride names with prices.
     * - Add or subtract any ride.
     * - Change / update the price for any ride.
     * - Total bookings done for each ride.
     */
    private JPanel createRideManagementCard() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Header Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("🎡 RIDE & ATTRACTION MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(new Color(25, 35, 55));
        JLabel sub = new JLabel("Manage attraction catalogue, dynamic pricing, capacity, and live bookings count.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(110, 125, 145));
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(sub, BorderLayout.SOUTH);
        p.add(titlePanel, BorderLayout.NORTH);

        // Rides Table Setup
        String[] cols = {"Ride ID", "Ride Name", "Type", "Unit Price (Rs.)", "Capacity (Seats)", "Min Age", "Total Bookings Done", "Total Revenue (Rs.)"};
        ridesTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        ridesTable = new JTable(ridesTableModel);
        ridesTable.setRowHeight(30);
        ridesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ridesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane sp = new JScrollPane(ridesTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 238)));
        p.add(sp, BorderLayout.CENTER);

        // Action Toolbar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomBar.setOpaque(false);

        JButton btnChangePrice = new JButton("✏️ Change Price for Selected Ride");
        btnChangePrice.setBackground(new Color(41, 128, 185));
        btnChangePrice.setForeground(Color.WHITE);
        btnChangePrice.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnChangePrice.addActionListener(e -> onChangeRidePriceClicked());

        JButton btnAddRide = new JButton("➕ Add New Ride");
        btnAddRide.setBackground(new Color(39, 174, 96));
        btnAddRide.setForeground(Color.WHITE);
        btnAddRide.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddRide.addActionListener(e -> onAddNewRideClicked());

        JButton btnDeleteRide = new JButton("➖ Remove / Subtract Ride");
        btnDeleteRide.setBackground(new Color(192, 57, 43));
        btnDeleteRide.setForeground(Color.WHITE);
        btnDeleteRide.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDeleteRide.addActionListener(e -> onRemoveRideClicked());

        JButton btnRefreshRides = new JButton("🔄 Refresh Data");
        btnRefreshRides.addActionListener(e -> refreshRideManagementTable());

        bottomBar.add(btnChangePrice);
        bottomBar.add(btnAddRide);
        bottomBar.add(btnDeleteRide);
        bottomBar.add(btnRefreshRides);

        p.add(bottomBar, BorderLayout.SOUTH);
        return p;
    }

    private void refreshRideManagementTable() {
        if (ridesTableModel == null) return;
        ridesTableModel.setRowCount(0);
        for (Ride r : service.getRides()) {
            String type = r.getClass().getSimpleName().replace("Ride", " Ride");
            int bookings = service.getBookingCountForRide(r.getId());
            long rev = service.getRevenueForRide(r.getId());
            ridesTableModel.addRow(new Object[] {
                r.getId(),
                r.getName(),
                type,
                "Rs. " + r.getUnitPrice(),
                r.getCapacity() + " seats",
                r.getMinAge() + " yrs",
                bookings + " bookings",
                "Rs. " + rev
            });
        }
    }

    private void onChangeRidePriceClicked() {
        int selected = ridesTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a ride from the table to change its price.",
                    "Select Ride", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int rideId = (Integer) ridesTableModel.getValueAt(selected, 0);
        String name = (String) ridesTableModel.getValueAt(selected, 1);
        Ride r = service.findRideById(rideId);
        if (r == null) return;

        String input = JOptionPane.showInputDialog(this,
                "Enter new ticket price (in Rs.) for " + name + ":",
                String.valueOf(r.getUnitPrice()));
        if (input != null && !input.trim().isEmpty()) {
            try {
                int newPrice = Integer.parseInt(input.trim());
                if (newPrice <= 0) throw new NumberFormatException();
                service.updateRidePrice(rideId, newPrice);
                refreshAllViews();
                refreshRideManagementTable();
                updateFarePreview();
                JOptionPane.showMessageDialog(this,
                        "Price for " + name + " updated to Rs. " + newPrice + " successfully!",
                        "Price Updated", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid positive integer price.",
                        "Invalid Price", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAddNewRideClicked() {
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField("150");
        JTextField capField = new JTextField("15");
        JTextField minAgeField = new JTextField("8");
        JComboBox<String> typeBox = new JComboBox<>(new String[] {"Family Ride", "Thrill Ride", "Water Ride", "Kart Ride"});

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Ride Name:")); form.add(nameField);
        form.add(new JLabel("Ride Category:")); form.add(typeBox);
        form.add(new JLabel("Ticket Price (Rs.):")); form.add(priceField);
        form.add(new JLabel("Seating Capacity:")); form.add(capField);
        form.add(new JLabel("Minimum Age:")); form.add(minAgeField);

        int res = JOptionPane.showConfirmDialog(this, form, "Add New Attraction", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) throw new IllegalArgumentException("Ride name cannot be empty.");
                int price = Integer.parseInt(priceField.getText().trim());
                int cap = Integer.parseInt(capField.getText().trim());
                int minAge = Integer.parseInt(minAgeField.getText().trim());

                // Generate new ID (e.g. 105, 106...)
                int maxId = 100;
                for (Ride r : service.getRides()) {
                    if (r.getId() > maxId) maxId = r.getId();
                }
                int newId = maxId + 1;

                Ride newRide;
                String cat = (String) typeBox.getSelectedItem();
                if ("Thrill Ride".equals(cat)) newRide = new ThrillRide(newId, name, cap, price, minAge);
                else if ("Water Ride".equals(cat)) newRide = new WaterRide(newId, name, cap, price, minAge);
                else if ("Kart Ride".equals(cat)) newRide = new KartRide(newId, name, cap, price, minAge);
                else newRide = new FamilyRide(newId, name, cap, price, minAge);

                boolean added = service.addRide(newRide);
                if (added) {
                    refreshCmbRides();
                    refreshAllViews();
                    refreshRideManagementTable();
                    JOptionPane.showMessageDialog(this, "Attraction '" + name + "' added successfully!",
                            "Ride Added", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add ride. Maximum ride limit (10) reached.",
                            "Limit Reached", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid inputs: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onRemoveRideClicked() {
        int selected = ridesTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a ride to remove.", "Select Ride", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int rideId = (Integer) ridesTableModel.getValueAt(selected, 0);
        String name = (String) ridesTableModel.getValueAt(selected, 1);

        int conf = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove ride '" + name + "' (ID: " + rideId + ")?\n" +
                "Note: A minimum of 1 attraction must remain active.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            boolean removed = service.removeRide(rideId);
            if (removed) {
                refreshCmbRides();
                refreshAllViews();
                refreshRideManagementTable();
                JOptionPane.showMessageDialog(this, "Ride '" + name + "' removed.", "Removed", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Cannot remove ride. At least one ride must remain.", "Action Blocked", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void refreshCmbRides() {
        DefaultComboBoxModel<Ride> model = new DefaultComboBoxModel<>(service.getRides());
        cmbRides.setModel(model);
        if (cmbQueueRideFilter != null) {
            String cur = (String) cmbQueueRideFilter.getSelectedItem();
            DefaultComboBoxModel<String> qModel = new DefaultComboBoxModel<>();
            qModel.addElement("All Rides (Global View)");
            for (Ride r : service.getRides()) {
                qModel.addElement(r.getName() + " (Queue)");
            }
            cmbQueueRideFilter.setModel(qModel);
            if (cur != null) cmbQueueRideFilter.setSelectedItem(cur);
        }
    }

    /**
     * CARD 3: VISITOR MANAGEMENT SECTION
     * Requirements:
     * - See the sequence in which the visitor booked the ticket.
     * - Leave / board that visitor for enjoying the ride in that exact sequence.
     * - Queue sequence is independent per ride: e.g. Sneha is #1 in Roller Coaster,
     *   Subhi is #1 in Go-Kart.
     */
    private JPanel createVisitorManagementCard() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Title & Description
        JPanel topHeader = new JPanel(new BorderLayout(10, 6));
        topHeader.setOpaque(false);

        JLabel title = new JLabel("👥 VISITOR BOARDING QUEUE & SEQUENCE MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(new Color(25, 35, 55));

        JLabel sub = new JLabel("First-Come, First-Served Boarding Dispatcher: Tracks the exact order in which visitors booked each ride.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(110, 125, 145));

        topHeader.add(title, BorderLayout.NORTH);
        topHeader.add(sub, BorderLayout.CENTER);

        // Station Control Strip
        JPanel stationPanel = new JPanel(new BorderLayout(10, 0));
        stationPanel.setBackground(Color.WHITE);
        stationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                new EmptyBorder(10, 14, 10, 14)));

        lblNextInLineInfo = new JLabel("Next in Line to Board: Select an attraction to view the queue.");
        lblNextInLineInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNextInLineInfo.setForeground(new Color(41, 128, 185));
        stationPanel.add(lblNextInLineInfo, BorderLayout.CENTER);

        JButton btnBoardNext = new JButton("🚀 Board Next Visitor Group (Enjoy Ride)");
        btnBoardNext.setBackground(new Color(39, 174, 96));
        btnBoardNext.setForeground(Color.WHITE);
        btnBoardNext.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBoardNext.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBoardNext.addActionListener(e -> onBoardNextVisitorClicked());
        stationPanel.add(btnBoardNext, BorderLayout.EAST);

        topHeader.add(stationPanel, BorderLayout.SOUTH);
        p.add(topHeader, BorderLayout.NORTH);

        // Filter Strip
        JPanel filterStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterStrip.setOpaque(false);

        cmbQueueRideFilter = new JComboBox<>();
        cmbQueueRideFilter.addItem("All Rides (Global View)");
        for (Ride r : service.getRides()) {
            cmbQueueRideFilter.addItem(r.getName() + " (Queue)");
        }
        cmbQueueRideFilter.addActionListener(e -> refreshVisitorQueueView());

        txtVisitorQueueSearch = new JTextField(15);
        txtVisitorQueueSearch.putClientProperty("JTextField.placeholderText", "Search Visitor Name / ID...");
        txtVisitorQueueSearch.addCaretListener(e -> refreshVisitorQueueView());

        JButton btnRefreshQueue = new JButton("🔄 Refresh Queue");
        btnRefreshQueue.addActionListener(e -> refreshVisitorQueueView());

        filterStrip.add(new JLabel("Attraction Queue:"));
        filterStrip.add(cmbQueueRideFilter);
        filterStrip.add(txtVisitorQueueSearch);
        filterStrip.add(btnRefreshQueue);

        // Queue Table Setup
        String[] qCols = {"Queue Turn", "Attraction / Ride", "Booking ID", "Visitor Name", "Age", "Group Size", "Booking Time", "Boarding Status"};
        visitorQueueTableModel = new DefaultTableModel(qCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        visitorQueueTable = new JTable(visitorQueueTableModel);
        visitorQueueTable.setRowHeight(28);
        visitorQueueTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        visitorQueueTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        visitorQueueTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, row, col);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String s = (val != null) ? val.toString() : "";
                if (s.contains("#1")) {
                    lbl.setForeground(new Color(230, 126, 34)); // Highlight 1st in line
                } else {
                    lbl.setForeground(new Color(50, 65, 85));
                }
                return lbl;
            }
        });

        visitorQueueTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String s = (val != null) ? val.toString() : "";
                if (s.contains("ENJOYING")) {
                    lbl.setForeground(new Color(39, 174, 96));
                } else {
                    lbl.setForeground(new Color(230, 126, 34));
                }
                return lbl;
            }
        });

        JPanel tableHolder = new JPanel(new BorderLayout());
        tableHolder.setOpaque(false);
        tableHolder.add(filterStrip, BorderLayout.NORTH);
        tableHolder.add(new JScrollPane(visitorQueueTable), BorderLayout.CENTER);
        p.add(tableHolder, BorderLayout.CENTER);

        return p;
    }

    private void refreshVisitorQueueView() {
        if (visitorQueueTableModel == null) return;
        visitorQueueTableModel.setRowCount(0);

        String selectedFilter = (String) cmbQueueRideFilter.getSelectedItem();
        String search = (txtVisitorQueueSearch != null) ? txtVisitorQueueSearch.getText().trim().toLowerCase() : "";

        // Track queue order per ride
        Map<Integer, Integer> rideSeqCounters = new HashMap<>();
        Booking nextInLineBooking = null;

        for (Booking b : service.getBookingHistory()) {
            if (b.getStatus() != BookingStatus.CONFIRMED) continue;

            Ride r = service.findRideById(b.getRideId());
            String rideName = (r != null) ? r.getName() : "Ride " + b.getRideId();

            int seq = rideSeqCounters.getOrDefault(b.getRideId(), 0) + 1;
            rideSeqCounters.put(b.getRideId(), seq);

            boolean isBoarded = service.isBoarded(b.getBookingId());

            // Check filter
            boolean matchesRide = (selectedFilter == null || selectedFilter.startsWith("All") || selectedFilter.contains(rideName));
            boolean matchesSearch = (search.isEmpty() || b.getVisitorName().toLowerCase().contains(search) || String.valueOf(b.getBookingId()).contains(search));

            if (matchesRide && matchesSearch) {
                String queueTurnStr = (seq == 1 && !isBoarded) ? "#1 (Next to Board)" : ("#" + seq + " in Line");
                String statusStr = isBoarded ? "✅ BOARDED - ENJOYING RIDE" : "⏳ WAITING IN LINE";

                visitorQueueTableModel.addRow(new Object[] {
                    queueTurnStr,
                    rideName,
                    b.getBookingId(),
                    b.getVisitorName(),
                    b.getAge(),
                    b.getGroupSize() + " seats",
                    b.getCreatedAt(),
                    statusStr
                });

                if (nextInLineBooking == null && !isBoarded && (selectedFilter != null && !selectedFilter.startsWith("All"))) {
                    nextInLineBooking = b;
                }
            }
        }

        if (nextInLineBooking != null) {
            Ride r = service.findRideById(nextInLineBooking.getRideId());
            String rName = (r != null) ? r.getName() : "";
            lblNextInLineInfo.setText("🟢 Next in Line for " + rName + ": " + nextInLineBooking.getVisitorName() +
                    " (Group of " + nextInLineBooking.getGroupSize() + ", Booking #" + nextInLineBooking.getBookingId() + ")");
        } else {
            lblNextInLineInfo.setText("All queued visitors for this view have boarded or no active visitors in line.");
        }
    }

    private void onBoardNextVisitorClicked() {
        String selectedFilter = (String) cmbQueueRideFilter.getSelectedItem();
        if (selectedFilter != null && selectedFilter.startsWith("All")) {
            JOptionPane.showMessageDialog(this,
                    "Please select a specific attraction queue (e.g. Roller Coaster) from the dropdown\n" +
                    "to board the next visitor in sequence for that ride.",
                    "Select Attraction", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Find the target ride ID
        int targetRideId = -1;
        for (Ride r : service.getRides()) {
            if (selectedFilter != null && selectedFilter.contains(r.getName())) {
                targetRideId = r.getId();
                break;
            }
        }
        if (targetRideId == -1) return;

        // Find first non-boarded confirmed booking in chronological order
        Booking toBoard = null;
        for (Booking b : service.getBookingHistory()) {
            if (b.getRideId() == targetRideId && b.getStatus() == BookingStatus.CONFIRMED && !service.isBoarded(b.getBookingId())) {
                toBoard = b;
                break;
            }
        }

        if (toBoard != null) {
            service.markBoarded(toBoard.getBookingId());
            refreshVisitorQueueView();
            Ride r = service.findRideById(targetRideId);
            JOptionPane.showMessageDialog(this,
                    "Visitor Group Boarded Successfully!\n" +
                    "Visitor: " + toBoard.getVisitorName() + " (Booking #" + toBoard.getBookingId() + ")\n" +
                    "Attraction: " + (r != null ? r.getName() : "") + "\n" +
                    "Group Size: " + toBoard.getGroupSize() + " visitors\n\n" +
                    "The group has entered the boarding gate to enjoy the ride!",
                    "Visitor Boarded", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No waiting visitors found in this ride queue to board.",
                    "Queue Empty", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * CARD 4: STAFF MANAGEMENT SECTION
     * Requirements:
     * - Column changed from "Assigned Ride" to "Assigned Work".
     * - Duty status should be changeable.
     * - Add or subtract any staff member.
     * - Update / edit staff details.
     */
    private JPanel createStaffManagementCard() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Header
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("👔 PARK STAFF & OPERATOR MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(new Color(25, 35, 55));
        JLabel sub = new JLabel("Manage park staff rosters, assigned work duties, shift schedules, and live operational status.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(110, 125, 145));
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(sub, BorderLayout.SOUTH);
        p.add(titlePanel, BorderLayout.NORTH);

        // Staff Table (Column "Assigned Work" as requested)
        String[] cols = {"Staff ID", "Staff Name", "Assigned Work", "Shift Timings", "Role / Certification", "Duty Status"};
        staffTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        staffTable = new JTable(staffTableModel);
        staffTable.setRowHeight(30);
        staffTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        staffTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        staffTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String st = (val != null) ? val.toString() : "";
                if ("ON DUTY".equalsIgnoreCase(st)) {
                    lbl.setForeground(new Color(39, 174, 96));
                    lbl.setText("● ON DUTY");
                } else if ("ON BREAK".equalsIgnoreCase(st)) {
                    lbl.setForeground(new Color(230, 126, 34));
                    lbl.setText("☕ ON BREAK");
                } else {
                    lbl.setForeground(new Color(120, 130, 145));
                    lbl.setText("○ OFF DUTY");
                }
                return lbl;
            }
        });

        JScrollPane sp = new JScrollPane(staffTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 238)));
        p.add(sp, BorderLayout.CENTER);

        // Action Buttons Strip
        JPanel btnStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnStrip.setOpaque(false);

        JButton btnToggleStatus = new JButton("🔄 Toggle Duty Status (On Duty / Break / Off Duty)");
        btnToggleStatus.setBackground(new Color(41, 128, 185));
        btnToggleStatus.setForeground(Color.WHITE);
        btnToggleStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnToggleStatus.addActionListener(e -> onToggleStaffStatusClicked());

        JButton btnAddStaff = new JButton("➕ Add Staff Member");
        btnAddStaff.setBackground(new Color(39, 174, 96));
        btnAddStaff.setForeground(Color.WHITE);
        btnAddStaff.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddStaff.addActionListener(e -> onAddStaffClicked());

        JButton btnEditStaff = new JButton("✏️ Update Staff Details");
        btnEditStaff.addActionListener(e -> onUpdateStaffClicked());

        JButton btnRemoveStaff = new JButton("➖ Remove Staff Member");
        btnRemoveStaff.setBackground(new Color(192, 57, 43));
        btnRemoveStaff.setForeground(Color.WHITE);
        btnRemoveStaff.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRemoveStaff.addActionListener(e -> onRemoveStaffClicked());

        btnStrip.add(btnToggleStatus);
        btnStrip.add(btnAddStaff);
        btnStrip.add(btnEditStaff);
        btnStrip.add(btnRemoveStaff);

        p.add(btnStrip, BorderLayout.SOUTH);
        refreshStaffTable();
        return p;
    }

    private void refreshStaffTable() {
        if (staffTableModel == null) return;
        staffTableModel.setRowCount(0);
        for (StaffMember s : service.getStaffList()) {
            staffTableModel.addRow(new Object[] {
                s.getId(),
                s.getName(),
                s.getAssignedWork(),
                s.getShift(),
                s.getCertification(),
                s.getStatus()
            });
        }
    }

    private void onToggleStaffStatusClicked() {
        int row = staffTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a staff member from the table to change status.",
                    "Select Staff", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) staffTableModel.getValueAt(row, 0);
        service.toggleStaffStatus(id);
        refreshStaffTable();
    }

    private void onAddStaffClicked() {
        JTextField idField = new JTextField("OP-" + (100 + service.getStaffList().size() + 1));
        JTextField nameField = new JTextField();
        JTextField workField = new JTextField("Roller Coaster Dispatch");
        JTextField shiftField = new JTextField("09:00 - 15:00");
        JTextField certField = new JTextField("Certified Safety Operator");
        JComboBox<String> statusBox = new JComboBox<>(new String[] {"ON DUTY", "ON BREAK", "OFF DUTY"});

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.add(new JLabel("Staff ID:")); form.add(idField);
        form.add(new JLabel("Staff Full Name:")); form.add(nameField);
        form.add(new JLabel("Assigned Work:")); form.add(workField);
        form.add(new JLabel("Shift Timings:")); form.add(shiftField);
        form.add(new JLabel("Role / Certification:")); form.add(certField);
        form.add(new JLabel("Duty Status:")); form.add(statusBox);

        int res = JOptionPane.showConfirmDialog(this, form, "Add New Staff Member", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Staff name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            StaffMember sm = new StaffMember(idField.getText().trim(), name, workField.getText().trim(),
                    shiftField.getText().trim(), certField.getText().trim(), (String) statusBox.getSelectedItem());
            service.addStaff(sm);
            refreshStaffTable();
            JOptionPane.showMessageDialog(this, "Staff member '" + name + "' added successfully!", "Staff Added", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onUpdateStaffClicked() {
        int row = staffTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a staff member to update.", "Select Staff", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) staffTableModel.getValueAt(row, 0);
        String name = (String) staffTableModel.getValueAt(row, 1);
        String work = (String) staffTableModel.getValueAt(row, 2);
        String shift = (String) staffTableModel.getValueAt(row, 3);
        String cert = (String) staffTableModel.getValueAt(row, 4);
        String status = (String) staffTableModel.getValueAt(row, 5);

        JTextField nameField = new JTextField(name);
        JTextField workField = new JTextField(work);
        JTextField shiftField = new JTextField(shift);
        JTextField certField = new JTextField(cert);
        JComboBox<String> statusBox = new JComboBox<>(new String[] {"ON DUTY", "ON BREAK", "OFF DUTY"});
        statusBox.setSelectedItem(status);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Staff Name:")); form.add(nameField);
        form.add(new JLabel("Assigned Work:")); form.add(workField);
        form.add(new JLabel("Shift Timings:")); form.add(shiftField);
        form.add(new JLabel("Certification:")); form.add(certField);
        form.add(new JLabel("Duty Status:")); form.add(statusBox);

        int res = JOptionPane.showConfirmDialog(this, form, "Update Staff Member " + id, JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            service.updateStaff(id, nameField.getText().trim(), workField.getText().trim(),
                    shiftField.getText().trim(), certField.getText().trim(), (String) statusBox.getSelectedItem());
            refreshStaffTable();
            JOptionPane.showMessageDialog(this, "Staff member " + id + " updated.", "Updated", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onRemoveStaffClicked() {
        int row = staffTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a staff member to remove.", "Select Staff", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) staffTableModel.getValueAt(row, 0);
        String name = (String) staffTableModel.getValueAt(row, 1);

        int conf = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove staff member '" + name + "' (" + id + ")?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            service.removeStaff(id);
            refreshStaffTable();
            JOptionPane.showMessageDialog(this, "Staff member removed.", "Removed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * CARD 5: PAYMENTS & REVENUE SECTION
     * Requirements:
     * - Today's Revenue
     * - Total Revenue
     * - Revenue by Ride
     * - Payment History (Visitor Name, Ride Name booked, and Price)
     */
    private JPanel createPaymentsRevenueCard() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Header Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("💳 PAYMENTS & REVENUE CONSOLE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(new Color(25, 35, 55));
        JLabel sub = new JLabel("Financial auditing, per-attraction collections, and permanent payment transaction records.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(110, 125, 145));
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(sub, BorderLayout.SOUTH);
        p.add(titlePanel, BorderLayout.NORTH);

        // Top KPI Row: Today's Revenue & Total Revenue
        JPanel kpiRow = new JPanel(new GridLayout(1, 3, 14, 0));
        kpiRow.setOpaque(false);
        kpiRow.setPreferredSize(new Dimension(1280, 75));

        lblPayTodayRev = new JLabel("Rs. 0");
        lblPayTotalRev = new JLabel("Rs. 0");
        lblPayTotalTx = new JLabel("0");

        kpiRow.add(buildKpiCard("TODAY'S REVENUE", lblPayTodayRev, new Color(211, 84, 0)));
        kpiRow.add(buildKpiCard("TOTAL RECORDED REVENUE", lblPayTotalRev, new Color(41, 128, 185)));
        kpiRow.add(buildKpiCard("TOTAL PAID TRANSACTIONS", lblPayTotalTx, new Color(39, 174, 96)));

        // Split Area: Top = Revenue by Ride, Bottom = Payment History
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(kpiRow, BorderLayout.NORTH);

        // Revenue by Ride Table
        JPanel rideRevPanel = new JPanel(new BorderLayout(0, 6));
        rideRevPanel.setBackground(Color.WHITE);
        rideRevPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                new EmptyBorder(10, 14, 10, 14)));

        JLabel lblRevTitle = new JLabel("REVENUE BY RIDE");
        lblRevTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRevTitle.setForeground(new Color(35, 45, 65));
        rideRevPanel.add(lblRevTitle, BorderLayout.NORTH);

        String[] rideRevCols = {"Ride Name", "Ticket Price", "Capacity", "Bookings Done", "Total Revenue Collected (Rs.)", "Revenue Share (%)"};
        payRidesTableModel = new DefaultTableModel(rideRevCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable payRidesTable = new JTable(payRidesTableModel);
        payRidesTable.setRowHeight(26);
        payRidesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        payRidesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        rideRevPanel.add(new JScrollPane(payRidesTable), BorderLayout.CENTER);

        // Payment History Table (Visitor Name, Ride Name booked, Price)
        JPanel histPanel = new JPanel(new BorderLayout(0, 6));
        histPanel.setBackground(Color.WHITE);
        histPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                new EmptyBorder(10, 14, 10, 14)));

        JPanel histToolbar = new JPanel(new BorderLayout());
        histToolbar.setOpaque(false);
        JLabel lblHistTitle = new JLabel("PAYMENT HISTORY");
        lblHistTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHistTitle.setForeground(new Color(35, 45, 65));
        histToolbar.add(lblHistTitle, BorderLayout.WEST);

        txtPaySearch = new JTextField(15);
        txtPaySearch.putClientProperty("JTextField.placeholderText", "Search Visitor / Ride...");
        txtPaySearch.addCaretListener(e -> refreshPaymentsView());
        histToolbar.add(txtPaySearch, BorderLayout.EAST);
        histPanel.add(histToolbar, BorderLayout.NORTH);

        String[] payHistCols = {"Tx ID", "Visitor Name", "Ride Name Booked", "Price (Total Paid)", "Payment Status", "Timestamp"};
        paymentsHistoryTableModel = new DefaultTableModel(payHistCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable payHistTable = new JTable(paymentsHistoryTableModel);
        payHistTable.setRowHeight(26);
        payHistTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        payHistTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        payHistTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String s = (val != null) ? val.toString() : "";
                if (s.contains("PAID")) lbl.setForeground(new Color(39, 174, 96));
                else lbl.setForeground(new Color(192, 57, 43));
                return lbl;
            }
        });

        histPanel.add(new JScrollPane(payHistTable), BorderLayout.CENTER);

        JSplitPane paySplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rideRevPanel, histPanel);
        paySplit.setDividerLocation(180);
        paySplit.setBorder(null);
        center.add(paySplit, BorderLayout.CENTER);

        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private void refreshPaymentsView() {
        if (lblPayTotalRev == null) return;
        long totalRev = service.getTotalRevenue();
        lblPayTodayRev.setText("Rs. " + totalRev);
        lblPayTotalRev.setText("Rs. " + totalRev);

        int totalTx = 0;
        for (Booking b : service.getBookingHistory()) {
            if (b.getStatus() == BookingStatus.CONFIRMED) totalTx++;
        }
        lblPayTotalTx.setText(String.valueOf(totalTx));

        // Refresh Revenue by Ride
        payRidesTableModel.setRowCount(0);
        for (Ride r : service.getRides()) {
            int bookings = service.getBookingCountForRide(r.getId());
            long rev = service.getRevenueForRide(r.getId());
            int share = (totalRev > 0) ? (int)((rev * 100) / totalRev) : 0;
            payRidesTableModel.addRow(new Object[] {
                r.getName(),
                "Rs. " + r.getUnitPrice(),
                r.getCapacity() + " seats",
                bookings + " bookings",
                "Rs. " + rev,
                share + "%"
            });
        }

        // Refresh Payment History
        paymentsHistoryTableModel.setRowCount(0);
        String q = (txtPaySearch != null) ? txtPaySearch.getText().trim().toLowerCase() : "";

        for (Booking b : service.getBookingHistory()) {
            Ride r = service.findRideById(b.getRideId());
            String rideName = (r != null) ? r.getName() : "Ride " + b.getRideId();

            if (!q.isEmpty()) {
                if (!b.getVisitorName().toLowerCase().contains(q) &&
                    !rideName.toLowerCase().contains(q) &&
                    !String.valueOf(b.getBookingId()).contains(q)) {
                    continue;
                }
            }

            String payStatus = (b.getStatus() == BookingStatus.CONFIRMED) ? "PAID" : ("CANCELLED (" + b.getStatus().name() + ")");
            paymentsHistoryTableModel.addRow(new Object[] {
                "TX-" + b.getBookingId(),
                b.getVisitorName(),
                rideName,
                "Rs. " + b.getTotalAmount(),
                payStatus,
                b.getCreatedAt()
            });
        }
    }

    /**
     * CARD 6: 3D Park Experience & CG Showcase (Task 4 Runner)
     */
    private JPanel create3DParkCard() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Hero Header Banner
        JPanel hero = new JPanel(new BorderLayout(15, 0));
        hero.setBackground(new Color(24, 38, 64));
        hero.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(41, 128, 185), 1),
                new EmptyBorder(18, 22, 18, 22)));

        JPanel heroLeft = new JPanel(new GridLayout(2, 1, 0, 4));
        heroLeft.setOpaque(false);
        JLabel heroTitle = new JLabel("🎮 3D AMUSEMENT PARK SIMULATION (COMPUTER GRAPHICS LAB)");
        heroTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        heroTitle.setForeground(Color.WHITE);

        JLabel heroSubtitle = new JLabel("FreeGLUT 3.4 & OpenGL 4.6 Engine • Interactive Orbit Camera • Phong Illumination & Procedural Textures");
        heroSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        heroSubtitle.setForeground(new Color(160, 185, 215));
        heroLeft.add(heroTitle);
        heroLeft.add(heroSubtitle);
        hero.add(heroLeft, BorderLayout.CENTER);

        JButton btnPrimaryLaunch = new JButton("🚀 Launch 3D Simulation (Task 4)");
        btnPrimaryLaunch.setBackground(new Color(46, 204, 113));
        btnPrimaryLaunch.setForeground(Color.WHITE);
        btnPrimaryLaunch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPrimaryLaunch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPrimaryLaunch.setBorder(new EmptyBorder(10, 18, 10, 18));
        btnPrimaryLaunch.addActionListener(e -> launchCgTask(4));
        hero.add(btnPrimaryLaunch, BorderLayout.EAST);

        panel.add(hero, BorderLayout.NORTH);

        // Content Area: 3 Cards for CG Lab Tasks
        JPanel cardsRow = new JPanel(new GridLayout(1, 3, 14, 0));
        cardsRow.setOpaque(false);

        cardsRow.add(buildCgTaskCard("Task 1: 2D Primitives",
                "Bresenham & Midpoint Circle",
                "• Integer-only Bresenham Line Drawing\n" +
                "• Midpoint Circle Algorithm (8-way symmetry)\n" +
                "• Architectural 2D blueprint layout\n" +
                "• UI dashboard frame & buttons",
                1));

        cardsRow.add(buildCgTaskCard("Task 2: Transformations",
                "Hierarchical 3D & Clipping",
                "• Rotating Ferris Wheel with upright gondolas\n" +
                "• Piecewise polynomial Roller Coaster\n" +
                "• Cohen-Sutherland line clipping engine\n" +
                "• Dual glViewport secondary revenue display",
                2));

        cardsRow.add(buildCgTaskCard("Task 3: Lighting & Textures",
                "Phong Illumination & Textures",
                "• Phong reflection (Ambient, Diffuse, Specular)\n" +
                "• 4 Procedural bitmapped textures (Grass/Water)\n" +
                "• Keyboard toggles: [L] Light, [T] Texture\n" +
                "• Orthographic vs Perspective switch [P]",
                3));

        JPanel centerArea = new JPanel(new BorderLayout(0, 14));
        centerArea.setOpaque(false);
        centerArea.add(cardsRow, BorderLayout.CENTER);

        // Bottom Controls Guide Card
        JPanel controlsCard = new JPanel(new BorderLayout(10, 8));
        controlsCard.setBackground(Color.WHITE);
        controlsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                new EmptyBorder(14, 18, 14, 18)));

        JLabel lblCtrlTitle = new JLabel("🎮 INTERACTIVE CAMERA & VIEWPORT HOTKEYS");
        lblCtrlTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCtrlTitle.setForeground(new Color(40, 50, 75));
        controlsCard.add(lblCtrlTitle, BorderLayout.NORTH);

        JPanel hotkeysGrid = new JPanel(new GridLayout(2, 3, 14, 6));
        hotkeysGrid.setOpaque(false);
        hotkeysGrid.add(new JLabel("🖱️ Left-Click + Drag: Orbit Camera (Pitch / Yaw)"));
        hotkeysGrid.add(new JLabel("🔄 Scroll Wheel: Camera Zoom In / Out"));
        hotkeysGrid.add(new JLabel("💡 Key [L]: Toggle Dynamic Phong Lighting"));
        hotkeysGrid.add(new JLabel("🎨 Key [T]: Toggle Procedural Surface Textures"));
        hotkeysGrid.add(new JLabel("📐 Key [P]: Toggle Perspective / Orthographic"));
        hotkeysGrid.add(new JLabel("❌ Key [ESC]: Exit 3D Simulation"));
        controlsCard.add(hotkeysGrid, BorderLayout.CENTER);

        centerArea.add(controlsCard, BorderLayout.SOUTH);
        panel.add(centerArea, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildCgTaskCard(String badge, String title, String bulletPoints, int taskNum) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setOpaque(false);
        JLabel lblBadge = new JLabel(badge);
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBadge.setForeground(new Color(41, 128, 185));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(30, 40, 60));
        top.add(lblBadge, BorderLayout.NORTH);
        top.add(lblTitle, BorderLayout.CENTER);
        card.add(top, BorderLayout.NORTH);

        JTextArea txt = new JTextArea(bulletPoints);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txt.setForeground(new Color(80, 95, 115));
        txt.setEditable(false);
        txt.setOpaque(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        card.add(txt, BorderLayout.CENTER);

        JButton btnLaunch = new JButton("Run Task " + taskNum);
        btnLaunch.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLaunch.setBackground(new Color(245, 248, 252));
        btnLaunch.setForeground(new Color(41, 128, 185));
        btnLaunch.setBorder(BorderFactory.createLineBorder(new Color(210, 225, 245), 1));
        btnLaunch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLaunch.addActionListener(e -> launchCgTask(taskNum));
        card.add(btnLaunch, BorderLayout.SOUTH);

        return card;
    }

    /**
     * Launch CG task executable or run.bat in background with MSYS2 in PATH
     */
    public void launchCgTask(int taskNum) {
        SwingUtilities.invokeLater(() -> {
            try {
                File rootDir = new File(".").getCanonicalFile();
                File cgDir;
                if (new File(rootDir, "cg").exists()) {
                    cgDir = new File(rootDir, "cg");
                } else if (new File(rootDir.getParentFile(), "cg").exists()) {
                    cgDir = new File(rootDir.getParentFile(), "cg");
                } else {
                    cgDir = new File("C:/Users/SNEHA/.gemini/antigravity/scratch/SmartPark_v2/cg");
                }

                File taskDir;
                switch (taskNum) {
                    case 1: taskDir = new File(cgDir, "task1_primitives"); break;
                    case 2: taskDir = new File(cgDir, "task2_transform_clip"); break;
                    case 3: taskDir = new File(cgDir, "task3_texture_light"); break;
                    case 4: default: taskDir = new File(cgDir, "task4_final_render"); break;
                }

                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "call", "run.bat");
                Map<String, String> env = pb.environment();
                env.put("PATH", "C:\\msys64\\ucrt64\\bin;" + env.get("PATH"));
                pb.directory(taskDir);
                pb.start();

                JOptionPane.showMessageDialog(this,
                        "Launching Computer Graphics Simulation (Task " + taskNum + ")...\n" +
                        "The interactive FreeGLUT OpenGL window will open shortly on your screen.",
                        "3D Park Experience", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to launch CG simulation: " + ex.getMessage(),
                        "Launch Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * CARD 7: Ride Maintenance & Safety Checklists
     */
    private JPanel createMaintenanceCard() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 20, 20));

        JLabel title = new JLabel("🔧 RIDE MAINTENANCE & MECHANICAL INSPECTION LOGS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(25, 35, 55));
        p.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setOpaque(false);

        grid.add(buildMaintenanceCard("Ferris Wheel (ID: 101)", "Wheel Axis & Motor Torque",
                "• Structural steel cabling check: PASSED\n" +
                "• Hydraulic motor pressure: 180 PSI (Optimal)\n" +
                "• Emergency stop brake test: 0.8s reaction\n" +
                "• Last Inspected: Today 08:30 AM • Certified"));

        grid.add(buildMaintenanceCard("Roller Coaster (ID: 102)", "Track & Magnetic Brakes",
                "• Pneumatic lap bar locking sensors: 100%\n" +
                "• Magnetic eddy-current brakes: PASSED\n" +
                "• Chain lift tension & lubrication: PASSED\n" +
                "• Last Inspected: Today 08:15 AM • Certified"));

        grid.add(buildMaintenanceCard("Water Ride (ID: 103)", "Pumps & Flume Channel",
                "• Water flow rate: 450 gpm (Normal)\n" +
                "• Splash barrier deceleration sensor: OK\n" +
                "• Secondary pump redundancy: ACTIVE\n" +
                "• Last Inspected: Today 08:45 AM • Certified"));

        grid.add(buildMaintenanceCard("Go-Kart (ID: 104)", "Governors & Track Perimeter",
                "• Speed governor set to 25 km/h: VERIFIED\n" +
                "• Tire shock barriers intact: 100%\n" +
                "• Remote engine cut-off radio link: PASSED\n" +
                "• Last Inspected: Today 08:00 AM • Certified"));

        p.add(grid, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildMaintenanceCard(String rideName, String subtitle, String details) {
        JPanel c = new JPanel(new BorderLayout(0, 8));
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel t = new JLabel(rideName);
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setForeground(new Color(30, 40, 60));

        JLabel st = new JLabel(subtitle + " • STATUS: 100% OPERATIONAL");
        st.setFont(new Font("Segoe UI", Font.BOLD, 11));
        st.setForeground(new Color(39, 174, 96));

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 2));
        top.setOpaque(false);
        top.add(t);
        top.add(st);
        c.add(top, BorderLayout.NORTH);

        JTextArea ta = new JTextArea(details);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ta.setForeground(new Color(75, 85, 105));
        ta.setEditable(false);
        ta.setOpaque(false);
        c.add(ta, BorderLayout.CENTER);

        return c;
    }

    /**
     * CARD 8: Reports & Footfall Analytics
     */
    private JPanel createReportsAnalyticsCard() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 20, 20));

        JLabel title = new JLabel("📈 FOOTFALL & PARK OPERATIONS ANALYTICS (CO1 - Task 1)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(25, 35, 55));
        p.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);

        String[] cols = {"Hour of Day", "Footfall Count", "Occupancy Band", "Primary Attraction Demand"};
        Object[][] data = {
            {"09:00 - 10:00", "120 visitors", "Normal (25%)", "Family Ride (Ferris Wheel)"},
            {"10:00 - 11:00", "240 visitors", "Moderate (50%)", "Family Ride / Go-Kart"},
            {"11:00 - 12:00", "310 visitors", "High (65%)", "Roller Coaster"},
            {"12:00 - 13:00", "280 visitors", "Lunch Shift (58%)", "Water Ride"},
            {"13:00 - 14:00", "330 visitors", "High (70%)", "Water Ride / Roller Coaster"},
            {"14:00 - 15:00", "350 visitors", "Peak Window (74%)", "All Attractions"},
            {"15:00 - 16:00", "360 visitors", "MAX PEAK HOUR", "Roller Coaster (Sold Out)"},
            {"16:00 - 17:00", "340 visitors", "Peak Window (72%)", "Go-Kart & Water Ride"},
            {"17:00 - 18:00", "310 visitors", "Evening Concourse", "Ferris Wheel"},
            {"18:00 - 19:00", "250 visitors", "Sunset Period", "Ferris Wheel (Illuminated)"},
            {"19:00 - 20:00", "145 visitors", "Park Closure Prep", "All Rides Last Calls"}
        };

        JTable table = new JTable(new DefaultTableModel(data, cols));
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 238)));
        center.add(sp, BorderLayout.CENTER);

        JPanel summary = new JPanel(new GridLayout(1, 3, 14, 0));
        summary.setOpaque(false);
        summary.add(buildKpiCard("TOTAL RECORDED FOOTFALL", new JLabel("3,345 Visitors"), new Color(41, 128, 185)));
        summary.add(buildKpiCard("MAXIMUM PEAK HOUR", new JLabel("15:00 (360 Visitors)"), new Color(230, 126, 34)));
        summary.add(buildKpiCard("CHILD CONCESSION BENEFIT", new JLabel("50% Applied"), new Color(39, 174, 96)));
        center.add(summary, BorderLayout.SOUTH);

        p.add(center, BorderLayout.CENTER);
        return p;
    }

    /**
     * CARD 9: Cloud Synchronization & Supabase Guide
     */
    private JPanel createCloudHistoryCard() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 20, 20));

        JLabel title = new JLabel("☁️ SUPABASE CLOUD SYNCHRONIZATION & DEMO GUIDE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(25, 35, 55));
        p.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                new EmptyBorder(18, 20, 18, 20)));

        JTextArea ta = new JTextArea(
                "=== HOW TO CONNECT & DEMONSTRATE SUPABASE IN YOUR VIVA ===\n\n" +
                "1. WHAT IS SUPABASE IN THIS PROJECT?\n" +
                "   Supabase is a cloud-hosted PostgreSQL database with instant REST APIs.\n" +
                "   In this project, it maintains an immutable, tamper-resistant cloud history of all bookings.\n\n" +
                "2. HOW TO CONNECT (3 Simple Steps):\n" +
                "   Step A: Go to https://supabase.com and create a free project.\n" +
                "   Step B: In your Supabase Dashboard, click 'SQL Editor' -> paste the content from 'supabase/schema.sql' -> click RUN.\n" +
                "           (This creates the 'bookings' and 'rides' tables with row locks and permanent status auditing).\n" +
                "   Step C: Go to Project Settings -> API -> copy your 'Project URL' and 'anon public key'.\n" +
                "           Create 'java/config/supabase.properties' with:\n" +
                "           supabase.url=https://YOUR_PROJECT_REF.supabase.co\n" +
                "           supabase.key=eyJhbGciOiJIUzI1NiIsInR5cCI6...\n\n" +
                "3. HOW TO DEMONSTRATE LIVE TO THE EXAMINER:\n" +
                "   • Demo 1 (Live Cloud Sync): The chip at the top will turn green [Cloud: Online].\n" +
                "     Book a ticket in the Java Dashboard -> open Supabase Table Editor in your browser ->\n" +
                "     show that the exact new booking row appeared instantly in the cloud!\n" +
                "   • Demo 2 (Ethics & Permanent Audit Trail): Cancel a booking in the Java Dashboard.\n" +
                "     Show in Supabase that the record is NEVER deleted (no DELETE query allowed);\n" +
                "     instead, its status safely updates to 'CANCELLED' with UTC timestamp.\n" +
                "   • Demo 3 (Offline Resilience): If there is no internet, the dashboard never crashes!\n" +
                "     Bookings are cached safely in 'build/pending_offline_bookings.csv'.\n" +
                "     Click 'Sync Cloud' when reconnected, and all offline bookings flush automatically.\n");
        ta.setFont(new Font("Consolas", Font.PLAIN, 12));
        ta.setForeground(new Color(40, 50, 70));
        ta.setEditable(false);
        content.add(new JScrollPane(ta), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);

        JButton btnSyncNow = new JButton("Sync Offline Queue to Cloud Now");
        btnSyncNow.setBackground(new Color(41, 128, 185));
        btnSyncNow.setForeground(Color.WHITE);
        btnSyncNow.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSyncNow.addActionListener(e -> triggerCloudSync());
        btnRow.add(btnSyncNow);

        content.add(btnRow, BorderLayout.SOUTH);
        p.add(content, BorderLayout.CENTER);

        return p;
    }

    private void updateFarePreview() {
        Ride r = (Ride) cmbRides.getSelectedItem();
        if (r == null) return;
        int age = (Integer) spinAge.getValue();
        int grp = (Integer) spinGroupSize.getValue();
        int avail = service.getAvailableSeats(r.getId());

        lblUnitPrice.setText("Rs. " + r.getUnitPrice());
        lblAvailableSeats.setText(avail > 0 ? String.valueOf(avail) : "0 (Full)");

        if (age < Discountable.CHILD_AGE_LIMIT) {
            lblDiscountLine.setText("Child 50% Off (Rs. " + (r.getUnitPrice() / 2) + ")");
            lblDiscountLine.setForeground(new Color(39, 174, 96));
        } else {
            lblDiscountLine.setText("Standard Adult (Full Price)");
            lblDiscountLine.setForeground(new Color(100, 110, 125));
        }

        int total = r.calculateFare(grp, age);
        lblTotalPayable.setText("Rs. " + total);

        boolean isFull = (grp > avail);
        btnBook.setEnabled(!isFull);
        if (isFull) {
            lblAvailableSeats.setText(avail + " (Not Enough Seats!)");
            lblAvailableSeats.setForeground(new Color(231, 76, 60));
        } else {
            lblAvailableSeats.setForeground(new Color(40, 50, 70));
        }
    }

    private void onBookClicked() {
        String name = txtVisitorName.getText().trim();
        int age = (Integer) spinAge.getValue();
        Ride r = (Ride) cmbRides.getSelectedItem();
        int grp = (Integer) spinGroupSize.getValue();
        if (r == null) return;

        try {
            Booking b = service.bookTicket(name, age, r.getId(), grp);
            syncBookingAsync(b);
            refreshAllViews();
            JOptionPane.showMessageDialog(this,
                    String.format("Booking #%d Confirmed!\nVisitor: %s\nAttraction: %s\nGroup: %d seats\nTotal Payable: Rs. %d",
                            b.getBookingId(), b.getVisitorName(), r.getName(), b.getGroupSize(), b.getTotalAmount()),
                    "Booking Successful", JOptionPane.INFORMATION_MESSAGE);
            resetBookingForm();
        } catch (AgeRestrictionException ex) {
            JOptionPane.showMessageDialog(this,
                    String.format("Age Restriction: Visitor age (%d) is below ride minimum (%d).",
                            ex.getVisitorAge(), ex.getMinAge()),
                    "Age Restriction", JOptionPane.WARNING_MESSAGE);
        } catch (RideFullException ex) {
            JOptionPane.showMessageDialog(this,
                    String.format("Ride Full: Requested %d seats, only %d available.",
                            ex.getRequested(), ex.getAvailable()),
                    "Ride Full", JOptionPane.WARNING_MESSAGE);
            updateFarePreview();
        } catch (InvalidVisitorException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancelSelectedClicked() {
        int selected = historyTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the history table to cancel.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = historyTable.convertRowIndexToModel(selected);
        long bid = (Long) tableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel booking #" + bid + "?\n" +
                "Note: In compliance with financial ethics, records are kept permanently and status is updated.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Booking b = service.cancelBooking(bid);
            if (b != null) {
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        dbClient.syncCancel(bid);
                        return null;
                    }
                }.execute();
                refreshAllViews();
                JOptionPane.showMessageDialog(this, "Booking #" + bid + " cancelled successfully.",
                        "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void onUndoClicked() {
        com.smartpark.model.Action action = service.undoLastAction();
        if (action == null) {
            JOptionPane.showMessageDialog(this, "Undo stack is empty.", "Nothing to Undo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        refreshAllViews();
        JOptionPane.showMessageDialog(this, "Undid last action: " + action.getType().name(),
                "Action Undone", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onExportCsvClicked() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("data/bookings_export.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                service.exportBookingsCsv(chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "History exported successfully to:\n" + chooser.getSelectedFile().getPath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetBookingForm() {
        txtVisitorName.setText("");
        spinAge.setValue(20);
        spinGroupSize.setValue(1);
        updateFarePreview();
    }

    private void refreshAllViews() {
        refreshKpis();
        refreshRideCards();
        refreshHistoryTable();
        if (chartPanel != null) chartPanel.repaint();
        refreshRideManagementTable();
        refreshVisitorQueueView();
        refreshPaymentsView();
    }

    private void refreshKpis() {
        lblTotalRevenue.setText("Rs. " + service.getTotalRevenue());
        lblTotalVisitors.setText(String.valueOf(service.getTotalConfirmedVisitors()));
        lblTotalBookings.setText(String.valueOf(service.getBookingHistory().size()));
        lblCancelledCount.setText(String.valueOf(service.getCancelledCount()));
        lblTodayRevenue.setText("Rs. " + service.getTotalRevenue());
    }

    private void refreshRideCards() {
        if (rideCardsPanel == null) return;
        rideCardsPanel.removeAll();
        for (Ride r : service.getRides()) {
            JPanel card = new JPanel(new BorderLayout(5, 4));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                    new EmptyBorder(6, 10, 6, 10)));

            JLabel nameLbl = new JLabel(r.getName());
            nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));

            int avail = service.getAvailableSeats(r.getId());
            int booked = r.getCapacity() - avail;
            int pct = (r.getCapacity() > 0) ? (booked * 100) / r.getCapacity() : 0;

            JLabel statsLbl = new JLabel(String.format("Rs. %d | %d/%d seats (%d left)",
                    r.getUnitPrice(), booked, r.getCapacity(), avail));
            statsLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            statsLbl.setForeground(new Color(90, 100, 115));

            JProgressBar pb = new JProgressBar(0, 100);
            pb.setValue(pct);
            pb.setStringPainted(true);
            pb.setString(pct + "%");
            if (pct <= 60) pb.setForeground(new Color(39, 174, 96));
            else if (pct <= 85) pb.setForeground(new Color(243, 156, 18));
            else pb.setForeground(new Color(231, 76, 60));

            card.add(nameLbl, BorderLayout.NORTH);
            card.add(statsLbl, BorderLayout.CENTER);
            card.add(pb, BorderLayout.SOUTH);
            rideCardsPanel.add(card);
        }
        rideCardsPanel.revalidate();
        rideCardsPanel.repaint();
    }

    private void refreshHistoryTable() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        for (Booking b : service.getBookingHistory()) {
            Ride r = service.findRideById(b.getRideId());
            String rideName = (r != null) ? r.getName() : "Ride " + b.getRideId();
            tableModel.addRow(new Object[] {
                b.getBookingId(),
                b.getVisitorName(),
                b.getAge(),
                rideName,
                b.getGroupSize(),
                b.getTotalAmount(),
                b.getStatus().name(),
                b.getCreatedAt()
            });
        }
    }

    private void filterTable() {
        String text = txtSearch.getText().trim();
        String status = (String) cmbStatusFilter.getSelectedItem();
        RowFilter<DefaultTableModel, Object> rf = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                if (status != null && !status.equals("ALL")) {
                    if (!entry.getStringValue(6).equalsIgnoreCase(status)) return false;
                }
                if (!text.isEmpty()) {
                    String name = entry.getStringValue(1).toLowerCase();
                    String id = entry.getStringValue(0);
                    return name.contains(text.toLowerCase()) || id.contains(text);
                }
                return true;
            }
        };
        tableSorter.setRowFilter(rf);
    }

    private void loadSeedData() {
        File seed = new File("data/sample_bookings.csv");
        if (seed.exists()) {
            try {
                service.loadFromCsv(seed);
                refreshAllViews();
            } catch (Exception e) {
                System.err.println("Could not load sample data: " + e.getMessage());
            }
        }
    }

    private void syncBookingAsync(Booking b) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                dbClient.syncBooking(b);
                return null;
            }
        }.execute();
    }

    private void triggerCloudSync() {
        supabaseStatusChip.setText(" Cloud: Syncing... ");
        supabaseStatusChip.setBackground(new Color(243, 156, 18));
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return dbClient.flushPendingQueue();
            }
            @Override
            protected void done() {
                updateCloudChip();
                try {
                    int n = get();
                    JOptionPane.showMessageDialog(ParkDashboard.this,
                            "Cloud sync completed. Flushed " + n + " pending offline bookings.",
                            "Cloud Sync", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void updateCloudChip() {
        if (!dbClient.isConfigured()) {
            supabaseStatusChip.setText(" Cloud: Prototype Mode (Local Offline) ");
            supabaseStatusChip.setBackground(new Color(108, 117, 125));
        } else if (dbClient.isOnline()) {
            supabaseStatusChip.setText(" Cloud: Online ");
            supabaseStatusChip.setBackground(new Color(39, 174, 96));
        } else {
            supabaseStatusChip.setText(" Cloud: Offline (Cached) ");
            supabaseStatusChip.setBackground(new Color(211, 84, 0));
        }
    }

    private void startBackgroundClockAndSync() {
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
            clockLabel.setText(LocalDateTime.now().format(timeFmt));
        });
        timer.start();

        javax.swing.Timer syncTimer = new javax.swing.Timer(30000, e -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    dbClient.flushPendingQueue();
                    return null;
                }
                @Override
                protected void done() {
                    updateCloudChip();
                }
            }.execute();
        });
        syncTimer.start();
        updateCloudChip();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ParkDashboard dashboard = new ParkDashboard();
            dashboard.setVisible(true);
        });
    }
}
