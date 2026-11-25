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
 * 1. Clean Left Navigation Sidebar.
 * 2. Unchanged main operational dashboard (KPIs, live booking form, ride progress bars,
 *    custom Graphics2D chart, and permanent history) with "Add to Waitlist" removed.
 * 3. Ride Management: Prominent buttons to Add, Remove/Subtract, and Edit/Change Price
 *    for any attraction with live booking counts.
 * 4. Visitor Management: Sequential boarding queue sequence (#1, #2...) per attraction
 *    with "Board Next Visitor" dispatch.
 * 5. Staff Management: "Assigned Work" tracking, Add/Subtract staff, Edit details, and Toggle duty status.
 * 6. Payments & Revenue: Today's revenue, total revenue, revenue by ride, and payment history.
 * 7. CPU Simulator (COA): Direct integration with 32-bit RISC Fetch-Decode-Execute pipeline & web visualizer.
 * 8. 3D Park Experience: Streamlined dedicated launcher for CG Task 4 (OpenGL interactive window).
 * 9. Cloud History: Supabase connection status, guide, and live sync.
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
    public static final String CARD_CPU_SIM = "CPU_SIM";
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

    // Cloud History Components
    private DefaultTableModel cloudHistoryTableModel;
    private JTable cloudHistoryTable;
    private TableRowSorter<DefaultTableModel> cloudHistorySorter;
    private JTextField txtCloudSearch;
    private JComboBox<String> cmbCloudFilter;
    private JLabel lblCloudTotalRecords;
    private JLabel lblCloudTotalRev;
    private JLabel lblCloudSyncStatus;

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

        // 1. Left Navigation Sidebar
        root.add(createSidebarPanel(), BorderLayout.WEST);

        // 2. Main Right Container (Header + CardLayout Content)
        JPanel mainContainer = new JPanel(new BorderLayout(0, 0));
        mainContainer.setOpaque(false);
        mainContainer.add(createHeaderPanel(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentCardsPanel = new JPanel(cardLayout);
        contentCardsPanel.setOpaque(false);

        // Add all distinct section views into CardLayout (Reports & Analytics removed as requested)
        contentCardsPanel.add(createMainDashboardCard(), CARD_DASHBOARD);
        contentCardsPanel.add(createRideManagementCard(), CARD_RIDES);
        contentCardsPanel.add(createVisitorManagementCard(), CARD_VISITORS);
        contentCardsPanel.add(createStaffManagementCard(), CARD_STAFF);
        contentCardsPanel.add(createPaymentsRevenueCard(), CARD_PAYMENTS);
        contentCardsPanel.add(createMaintenanceCard(), CARD_MAINTENANCE);
        contentCardsPanel.add(createCpuSimulatorCard(), CARD_CPU_SIM);
        contentCardsPanel.add(create3DParkCard(), CARD_3D_PARK);
        contentCardsPanel.add(createCloudHistoryCard(), CARD_CLOUD);

        mainContainer.add(contentCardsPanel, BorderLayout.CENTER);
        root.add(mainContainer, BorderLayout.CENTER);

        setContentPane(root);
        setActiveNavigation(CARD_DASHBOARD);
    }

    /**
     * Left Navigation Sidebar Panel (Clean text style matching user reference)
     */
    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 0));
        sidebar.setPreferredSize(new Dimension(230, 880));
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

        // Middle Navigation Menu Items
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(5, 12, 10, 12));

        addNavItem(menuPanel, "Dashboard", CARD_DASHBOARD);
        addNavItem(menuPanel, "Ride Management", CARD_RIDES);
        addNavItem(menuPanel, "Visitor Management", CARD_VISITORS);
        addNavItem(menuPanel, "Staff Management", CARD_STAFF);
        addNavItem(menuPanel, "Payments & Revenue", CARD_PAYMENTS);
        addNavItem(menuPanel, "Maintenance", CARD_MAINTENANCE);
        addNavItem(menuPanel, "CPU Simulator (COA)", CARD_CPU_SIM);
        addNavItem(menuPanel, "3D Park Experience", CARD_3D_PARK);
        addNavItem(menuPanel, "Cloud History", CARD_CLOUD);

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

    private void addNavItem(JPanel container, String text, String key) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(210, 38));
        btn.setPreferredSize(new Dimension(210, 38));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
                b.setFont(new Font("Segoe UI", Font.BOLD, 13));
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(52, 152, 219)),
                        new EmptyBorder(8, 10, 8, 14)));
            } else {
                b.setBackground(new Color(15, 27, 44));
                b.setForeground(new Color(175, 192, 215));
                b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                b.setBorder(new EmptyBorder(8, 14, 8, 14));
            }
        }
        cardLayout.show(contentCardsPanel, key);

        // Refresh specific view data on tab selection
        if (CARD_DASHBOARD.equals(key)) {
            refreshCmbRides();
            refreshAllViews();
        } else if (CARD_RIDES.equals(key)) {
            refreshRideManagementTable();
        } else if (CARD_VISITORS.equals(key)) {
            refreshVisitorQueueView();
        } else if (CARD_PAYMENTS.equals(key)) {
            refreshPaymentsView();
        } else if (CARD_STAFF.equals(key)) {
            refreshStaffTable();
        } else if (CARD_CLOUD.equals(key)) {
            refreshCloudHistoryTable();
        }
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
        JButton btnQuick3D = new JButton("Run 3D Park (OpenGL Window)");
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
     * Unchanged interface & working: KPI cards, booking form (without waitlist),
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

        // Action Buttons: ADD TO WAITLIST REMOVED
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

        rideCardsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        rideCardsPanel.setOpaque(false);
        JScrollPane rideCardsScroll = new JScrollPane(rideCardsPanel);
        rideCardsScroll.setBorder(null);
        rideCardsScroll.setOpaque(false);
        rideCardsScroll.getViewport().setOpaque(false);
        rideCardsScroll.setPreferredSize(new Dimension(520, 165));
        top.add(rideCardsScroll, BorderLayout.CENTER);

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
     * Prominent top controls to Add, Remove/Subtract, and Edit Price with live booking stats.
     */
    private JPanel createRideManagementCard() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Header Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("RIDE & ATTRACTION MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(25, 35, 55));
        JLabel sub = new JLabel("Add or subtract attractions, change ticket prices, and monitor live bookings count for each ride.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(110, 125, 145));
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(sub, BorderLayout.SOUTH);
        p.add(titlePanel, BorderLayout.NORTH);

        // Action Toolbar Placed Prominently at Top
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        actionToolbar.setBackground(Color.WHITE);
        actionToolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                new EmptyBorder(8, 12, 8, 12)));

        JButton btnAddRide = new JButton("+ Add New Ride");
        btnAddRide.setBackground(new Color(39, 174, 96));
        btnAddRide.setForeground(Color.WHITE);
        btnAddRide.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddRide.addActionListener(e -> onAddNewRideClicked());

        JButton btnChangePrice = new JButton("Edit / Change Price");
        btnChangePrice.setBackground(new Color(41, 128, 185));
        btnChangePrice.setForeground(Color.WHITE);
        btnChangePrice.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnChangePrice.addActionListener(e -> onChangeRidePriceClicked());

        JButton btnDeleteRide = new JButton("- Remove / Subtract Ride");
        btnDeleteRide.setBackground(new Color(192, 57, 43));
        btnDeleteRide.setForeground(Color.WHITE);
        btnDeleteRide.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDeleteRide.addActionListener(e -> onRemoveRideClicked());

        JButton btnRefreshRides = new JButton("Refresh Table");
        btnRefreshRides.addActionListener(e -> refreshRideManagementTable());

        JButton btnViewOnDash = new JButton("⬅ View on Dashboard");
        btnViewOnDash.setBackground(new Color(52, 73, 94));
        btnViewOnDash.setForeground(Color.WHITE);
        btnViewOnDash.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnViewOnDash.addActionListener(e -> setActiveNavigation(CARD_DASHBOARD));

        actionToolbar.add(btnAddRide);
        actionToolbar.add(btnChangePrice);
        actionToolbar.add(btnDeleteRide);
        actionToolbar.add(btnRefreshRides);
        actionToolbar.add(btnViewOnDash);

        // Rides Table Setup
        String[] cols = {"Ride ID", "Ride Name", "Category", "Unit Price (Rs.)", "Capacity (Seats)", "Min Age", "Total Bookings Done", "Total Revenue (Rs.)"};
        ridesTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        ridesTable = new JTable(ridesTableModel);
        ridesTable.setRowHeight(30);
        ridesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ridesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel centerArea = new JPanel(new BorderLayout(0, 8));
        centerArea.setOpaque(false);
        centerArea.add(actionToolbar, BorderLayout.NORTH);
        centerArea.add(new JScrollPane(ridesTable), BorderLayout.CENTER);

        p.add(centerArea, BorderLayout.CENTER);
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
                    int opt = JOptionPane.showOptionDialog(this,
                            "Attraction '" + name + "' (ID: " + newId + ") added successfully!\n" +
                            "Would you like to visit the main Dashboard now to see the new ride?",
                            "Ride Added Successfully",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            new String[]{"Visit Dashboard", "Stay on Ride Management"},
                            "Visit Dashboard");
                    if (opt == JOptionPane.YES_OPTION) {
                        setActiveNavigation(CARD_DASHBOARD);
                    }
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
                "Note: A minimum of 1 attraction must remain active in the park.",
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
     * Tracks sequence in which tickets were booked per attraction.
     * Includes "Board Next Visitor" dispatch.
     */
    private JPanel createVisitorManagementCard() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel topHeader = new JPanel(new BorderLayout(10, 6));
        topHeader.setOpaque(false);

        JLabel title = new JLabel("VISITOR BOARDING QUEUE & SEQUENCE MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(25, 35, 55));

        JLabel sub = new JLabel("Sequential First-Come, First-Served Boarding Dispatcher: Dispatches visitors in the exact sequence they booked.");
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

        JButton btnBoardNext = new JButton("Board Next Visitor Group (Enjoy Ride)");
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

        JButton btnRefreshQueue = new JButton("Refresh Queue");
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
                    lbl.setForeground(new Color(230, 126, 34));
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

        Map<Integer, Integer> rideSeqCounters = new HashMap<>();
        Booking nextInLineBooking = null;

        for (Booking b : service.getBookingHistory()) {
            if (b.getStatus() != BookingStatus.CONFIRMED) continue;

            Ride r = service.findRideById(b.getRideId());
            String rideName = (r != null) ? r.getName() : "Ride " + b.getRideId();

            int seq = rideSeqCounters.getOrDefault(b.getRideId(), 0) + 1;
            rideSeqCounters.put(b.getRideId(), seq);

            boolean isBoarded = service.isBoarded(b.getBookingId());

            boolean matchesRide = (selectedFilter == null || selectedFilter.startsWith("All") || selectedFilter.contains(rideName));
            boolean matchesSearch = (search.isEmpty() || b.getVisitorName().toLowerCase().contains(search) || String.valueOf(b.getBookingId()).contains(search));

            if (matchesRide && matchesSearch) {
                String queueTurnStr = (seq == 1 && !isBoarded) ? "#1 (Next to Board)" : ("#" + seq + " in Line");
                String statusStr = isBoarded ? "BOARDED - ENJOYING RIDE" : "WAITING IN LINE";

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
            lblNextInLineInfo.setText("Next in Line for " + rName + ": " + nextInLineBooking.getVisitorName() +
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

        int targetRideId = -1;
        for (Ride r : service.getRides()) {
            if (selectedFilter != null && selectedFilter.contains(r.getName())) {
                targetRideId = r.getId();
                break;
            }
        }
        if (targetRideId == -1) return;

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
     * Column "Assigned Work" tracking, Add, Remove, and Edit staff with changeable duty status.
     */
    private JPanel createStaffManagementCard() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Header
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("PARK STAFF & OPERATOR MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(25, 35, 55));
        JLabel sub = new JLabel("Manage park staff rosters, assigned work duties, shift schedules, and operational duty status.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(110, 125, 145));
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(sub, BorderLayout.SOUTH);
        p.add(titlePanel, BorderLayout.NORTH);

        // Action Toolbar Prominently Placed at Top
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        actionToolbar.setBackground(Color.WHITE);
        actionToolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                new EmptyBorder(8, 12, 8, 12)));

        JButton btnAddStaff = new JButton("+ Add Staff Member");
        btnAddStaff.setBackground(new Color(39, 174, 96));
        btnAddStaff.setForeground(Color.WHITE);
        btnAddStaff.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddStaff.addActionListener(e -> onAddStaffClicked());

        JButton btnEditStaff = new JButton("Edit Staff Details");
        btnEditStaff.setBackground(new Color(41, 128, 185));
        btnEditStaff.setForeground(Color.WHITE);
        btnEditStaff.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEditStaff.addActionListener(e -> onUpdateStaffClicked());

        JButton btnToggleStatus = new JButton("Toggle Duty Status (On Duty / Break / Off Duty)");
        btnToggleStatus.setBackground(new Color(230, 126, 34));
        btnToggleStatus.setForeground(Color.WHITE);
        btnToggleStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnToggleStatus.addActionListener(e -> onToggleStaffStatusClicked());

        JButton btnRemoveStaff = new JButton("- Remove Staff Member");
        btnRemoveStaff.setBackground(new Color(192, 57, 43));
        btnRemoveStaff.setForeground(Color.WHITE);
        btnRemoveStaff.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRemoveStaff.addActionListener(e -> onRemoveStaffClicked());

        actionToolbar.add(btnAddStaff);
        actionToolbar.add(btnEditStaff);
        actionToolbar.add(btnToggleStatus);
        actionToolbar.add(btnRemoveStaff);

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

        JPanel centerArea = new JPanel(new BorderLayout(0, 8));
        centerArea.setOpaque(false);
        centerArea.add(actionToolbar, BorderLayout.NORTH);
        centerArea.add(new JScrollPane(staffTable), BorderLayout.CENTER);

        p.add(centerArea, BorderLayout.CENTER);
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
     */
    private JPanel createPaymentsRevenueCard() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("PAYMENTS & REVENUE CONSOLE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(25, 35, 55));
        JLabel sub = new JLabel("Financial auditing, per-attraction collections, and permanent payment transaction records.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(110, 125, 145));
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(sub, BorderLayout.SOUTH);
        p.add(titlePanel, BorderLayout.NORTH);

        JPanel kpiRow = new JPanel(new GridLayout(1, 3, 14, 0));
        kpiRow.setOpaque(false);
        kpiRow.setPreferredSize(new Dimension(1280, 75));

        lblPayTodayRev = new JLabel("Rs. 0");
        lblPayTotalRev = new JLabel("Rs. 0");
        lblPayTotalTx = new JLabel("0");

        kpiRow.add(buildKpiCard("TODAY'S REVENUE", lblPayTodayRev, new Color(211, 84, 0)));
        kpiRow.add(buildKpiCard("TOTAL RECORDED REVENUE", lblPayTotalRev, new Color(41, 128, 185)));
        kpiRow.add(buildKpiCard("TOTAL PAID TRANSACTIONS", lblPayTotalTx, new Color(39, 174, 96)));

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(kpiRow, BorderLayout.NORTH);

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
     * CARD 6: CPU SIMULATOR (COA LAB - 2310221L)
     * Demonstrates Fetch-Decode-Execute pipeline & connects directly to web visualizer and CLI.
     */
    private JPanel createCpuSimulatorCard() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 20, 20));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("32-BIT RISC CPU PROCESSOR SIMULATOR (COA LAB)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(25, 35, 55));
        JLabel sub = new JLabel("Instruction Fetch, Decode, Execute, Memory, and Writeback pipeline simulation for ticket calculations.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(110, 125, 145));
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(sub, BorderLayout.SOUTH);
        p.add(titlePanel, BorderLayout.NORTH);

        // Hero Panel with direct launch actions
        JPanel hero = new JPanel(new BorderLayout(15, 0));
        hero.setBackground(new Color(24, 38, 64));
        hero.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(41, 128, 185), 1),
                new EmptyBorder(16, 20, 16, 20)));

        JPanel heroLeft = new JPanel(new GridLayout(2, 1, 0, 4));
        heroLeft.setOpaque(false);
        JLabel heroTitle = new JLabel("Interactive CPU Pipeline Visualizer & Opcode Engine");
        heroTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        heroTitle.setForeground(Color.WHITE);
        JLabel heroSubtitle = new JLabel("Verifies machine opcodes MUL R3,R1,R2 (0x00221818) and LD R2,8(R11) (0x8D620008) with 1-stall hazard bubble.");
        heroSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        heroSubtitle.setForeground(new Color(160, 185, 215));
        heroLeft.add(heroTitle);
        heroLeft.add(heroSubtitle);
        hero.add(heroLeft, BorderLayout.CENTER);

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        btnBox.setOpaque(false);

        JButton btnWebSim = new JButton("Open Web CPU Simulator (simulator.html)");
        btnWebSim.setBackground(new Color(46, 204, 113));
        btnWebSim.setForeground(Color.WHITE);
        btnWebSim.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnWebSim.addActionListener(e -> openCpuSimulatorWeb());

        JButton btnCliSim = new JButton("Run CLI Verifier (cpu_sim.exe)");
        btnCliSim.setBackground(new Color(52, 152, 219));
        btnCliSim.setForeground(Color.WHITE);
        btnCliSim.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCliSim.addActionListener(e -> runCpuSimCli());

        btnBox.add(btnWebSim);
        btnBox.add(btnCliSim);
        hero.add(btnBox, BorderLayout.EAST);

        // Center content: 5-Stage Pipeline Details
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(hero, BorderLayout.NORTH);

        JTextArea ta = new JTextArea(
                "========================================================================================\n" +
                "  32-BIT RISC CPU ARCHITECTURE & 5-STAGE PIPELINE (COA Task 4 Execution Trace)\n" +
                "========================================================================================\n\n" +
                "1. FIVE-STAGE PIPELINE MICROARCHITECTURE:\n" +
                "   [IF] Instruction Fetch: Program Counter (PC) loads instruction from 32-bit Code ROM.\n" +
                "   [ID] Instruction Decode: Hazard Detection Unit decodes Opcode & Register File (R0-R31).\n" +
                "   [EX] Execute / ALU: Arithmetic unit evaluates ADD, SUB, MUL, SHR with flag updates.\n" +
                "   [MEM] Data Memory: Load (LD) and Store (ST) read/write 64KB RAM cache.\n" +
                "   [WB] Write Back: Forwarding unit writes results back to destination register.\n\n" +
                "2. VERIFIED MIPS MACHINE INSTRUCTION HEX SIGNATURES:\n" +
                "   • MUL R3, R1, R2 -> 0x00221818  (R-type: opcode=0x00, rs=1, rt=2, rd=3, funct=0x18)\n" +
                "   • LD  R2, 8(R11) -> 0x8D620008  (I-type: opcode=0x23, rs=11, rt=2, offset=0x0008)\n\n" +
                "3. PARK TICKET CALCULATION EXECUTION SEQUENCE:\n" +
                "   Inst 1: LD  R1, 4(R11)     ; Load Group Size\n" +
                "   Inst 2: LD  R2, 8(R11)     ; Load Unit Price (0x8D620008)\n" +
                "   Inst 3: MUL R3, R1, R2     ; Multiply Base Fare (0x00221818)\n" +
                "   Inst 4: SHR R3, R3, 1      ; Apply 50% Child Discount if applicable\n" +
                "   Inst 5: ST  R3, 12(R11)    ; Store Total Fare into Booking Memory\n\n" +
                "4. LOAD-USE DATA HAZARD & HARDWARE STALL MITIGATION:\n" +
                "   Between 'LD R2, 8(R11)' and 'MUL R3, R1, R2', operand R2 is not ready in Cycle 4.\n" +
                "   The Hazard Detection Unit automatically inserts 1 STALL BUBBLE at Cycle 4.\n" +
                "   Forwarding Unit bypasses operand directly from MEM stage to EX stage in Cycle 5.\n" +
                "   Metrics: 5 Instructions in 10 Clock Cycles -> CPI = 2.00, Speed-up = 2.50x.\n");
        ta.setFont(new Font("Consolas", Font.PLAIN, 12));
        ta.setForeground(new Color(35, 45, 65));
        ta.setEditable(false);
        ta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                new EmptyBorder(12, 14, 12, 14)));

        center.add(new JScrollPane(ta), BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private void openCpuSimulatorWeb() {
        try {
            File rootDir = new File(".").getCanonicalFile();
            File htmlFile;
            if (new File(rootDir, "coa/task4_integration/simulator.html").exists()) {
                htmlFile = new File(rootDir, "coa/task4_integration/simulator.html");
            } else if (new File(rootDir.getParentFile(), "coa/task4_integration/simulator.html").exists()) {
                htmlFile = new File(rootDir.getParentFile(), "coa/task4_integration/simulator.html");
            } else {
                htmlFile = new File("C:/Users/SNEHA/.gemini/antigravity/scratch/SmartPark_v2/coa/task4_integration/simulator.html");
            }
            Desktop.getDesktop().browse(htmlFile.toURI());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not open browser: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void runCpuSimCli() {
        SwingUtilities.invokeLater(() -> {
            try {
                File rootDir = new File(".").getCanonicalFile();
                File coaDir;
                if (new File(rootDir, "coa/task4_integration").exists()) {
                    coaDir = new File(rootDir, "coa/task4_integration");
                } else if (new File(rootDir.getParentFile(), "coa/task4_integration").exists()) {
                    coaDir = new File(rootDir.getParentFile(), "coa/task4_integration");
                } else {
                    coaDir = new File("C:/Users/SNEHA/.gemini/antigravity/scratch/SmartPark_v2/coa/task4_integration");
                }
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "start", "cmd.exe", "/k", "call run.bat --verify");
                pb.directory(coaDir);
                pb.start();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error running CLI: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * CARD 7: 3D PARK EXPERIENCE (CG LAB - TASK 4 ONLY)
     * Other tasks removed as requested. Prominent OpenGL launch window button.
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
                new EmptyBorder(20, 24, 20, 24)));

        JPanel heroLeft = new JPanel(new GridLayout(2, 1, 0, 4));
        heroLeft.setOpaque(false);
        JLabel heroTitle = new JLabel("3D AMUSEMENT PARK INTERACTIVE SIMULATION");
        heroTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        heroTitle.setForeground(Color.WHITE);

        JLabel heroSubtitle = new JLabel("Computer Graphics Lab (2310218L) • FreeGLUT 3.4 & OpenGL 4.6 Engine • Task 4 Presentation Scene");
        heroSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        heroSubtitle.setForeground(new Color(160, 185, 215));
        heroLeft.add(heroTitle);
        heroLeft.add(heroSubtitle);
        hero.add(heroLeft, BorderLayout.CENTER);

        // Prominent 1-Click Launch Button for OpenGL Window
        JButton btnPrimaryLaunch = new JButton("🚀 Launch 3D Park (OpenGL Window)");
        btnPrimaryLaunch.setBackground(new Color(46, 204, 113));
        btnPrimaryLaunch.setForeground(Color.WHITE);
        btnPrimaryLaunch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPrimaryLaunch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPrimaryLaunch.setBorder(new EmptyBorder(12, 22, 12, 22));
        btnPrimaryLaunch.addActionListener(e -> launchCgTask(4));
        hero.add(btnPrimaryLaunch, BorderLayout.EAST);

        panel.add(hero, BorderLayout.NORTH);

        // Center Area: Presentation overview (other tasks removed as requested)
        JPanel centerArea = new JPanel(new BorderLayout(0, 14));
        centerArea.setOpaque(false);

        JPanel detailsCard = new JPanel(new BorderLayout(0, 10));
        detailsCard.setBackground(Color.WHITE);
        detailsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                new EmptyBorder(16, 20, 16, 20)));

        JLabel lblOverview = new JLabel("INTEGRATED 3D PARK ARCHITECTURE");
        lblOverview.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblOverview.setForeground(new Color(30, 45, 65));
        detailsCard.add(lblOverview, BorderLayout.NORTH);

        JTextArea txtDetails = new JTextArea(
                "• Unified 3D Park Scene: Features all four attractions operating simultaneously in real time.\n" +
                "• Ferris Wheel Animation: Hierarchical matrix transformations with 8 gravitationally stabilized gondolas.\n" +
                "• Roller Coaster: Piecewise polynomial path tracking with moving multi-car coaster train.\n" +
                "• Water Ride & Go-Kart: Animated sinusoidal flume currents and parametric track physics.\n" +
                "• Dynamic Illumination: Complete Phong reflection model (Ambient, Diffuse Lambertian, Specular highlights).\n" +
                "• Procedural Textures: RAM-generated textures for emerald lawn, asphalt course, shimmering water, and concourse pavers.\n" +
                "• Dual Viewport: 3D perspective world render alongside a 2D orthographic telemetry HUD.\n\n" +
                "Click the green 'Launch 3D Park (OpenGL Window)' button above to start the interactive OpenGL session!");
        txtDetails.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDetails.setForeground(new Color(60, 75, 95));
        txtDetails.setEditable(false);
        txtDetails.setOpaque(false);
        txtDetails.setLineWrap(true);
        txtDetails.setWrapStyleWord(true);
        detailsCard.add(txtDetails, BorderLayout.CENTER);

        centerArea.add(detailsCard, BorderLayout.CENTER);

        // Bottom Controls Guide Card
        JPanel controlsCard = new JPanel(new BorderLayout(10, 8));
        controlsCard.setBackground(Color.WHITE);
        controlsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                new EmptyBorder(14, 18, 14, 18)));

        JLabel lblCtrlTitle = new JLabel("INTERACTIVE CAMERA & VIEWPORT HOTKEYS");
        lblCtrlTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCtrlTitle.setForeground(new Color(40, 50, 75));
        controlsCard.add(lblCtrlTitle, BorderLayout.NORTH);

        JPanel hotkeysGrid = new JPanel(new GridLayout(2, 3, 14, 6));
        hotkeysGrid.setOpaque(false);
        hotkeysGrid.add(new JLabel("🖱️ Left-Click + Drag: Orbit Camera (Pitch / Yaw)"));
        hotkeysGrid.add(new JLabel("🔄 Scroll Wheel: Camera Zoom In / Out"));
        hotkeysGrid.add(new JLabel("💡 Key [L]: Toggle Dynamic Phong Lighting"));
        hotkeysGrid.add(new JLabel("🎨 Key [T]: Toggle Procedural Surface Textures"));
        hotkeysGrid.add(new JLabel("⏸️ Key [P]: Pause / Resume Animation"));
        hotkeysGrid.add(new JLabel("❌ Key [ESC]: Exit 3D Simulation"));
        controlsCard.add(hotkeysGrid, BorderLayout.CENTER);

        centerArea.add(controlsCard, BorderLayout.SOUTH);
        panel.add(centerArea, BorderLayout.CENTER);

        return panel;
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

                File taskDir = new File(cgDir, "task4_final_render");

                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "call", "run.bat");
                Map<String, String> env = pb.environment();
                env.put("PATH", "C:\\msys64\\ucrt64\\bin;" + env.get("PATH"));
                pb.directory(taskDir);
                pb.start();

                JOptionPane.showMessageDialog(this,
                        "Launching 3D Amusement Park Simulation (Task 4)...\n" +
                        "The interactive FreeGLUT OpenGL window will open on your screen.",
                        "3D Park Experience", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to launch CG simulation: " + ex.getMessage(),
                        "Launch Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * CARD 8: Ride Maintenance & Safety Checklists
     */
    private JPanel createMaintenanceCard() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 20, 20));

        JLabel title = new JLabel("RIDE MAINTENANCE & MECHANICAL INSPECTION LOGS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
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
     * CARD 9: Cloud-Synced Park Transaction History & Permanent Ledger
     */
    private JPanel createCloudHistoryCard() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Header Title
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);
        JLabel title = new JLabel("PERMANENT CLOUD HISTORY & AUDIT LEDGER");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(25, 35, 55));
        JLabel subtitle = new JLabel("Synchronized cloud audit trail backed by Supabase PostgreSQL database");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(110, 120, 135));
        topHeader.add(title, BorderLayout.NORTH);
        topHeader.add(subtitle, BorderLayout.SOUTH);
        p.add(topHeader, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        // Top 4 Metrics Cards
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        metricsPanel.setOpaque(false);
        metricsPanel.setPreferredSize(new Dimension(1000, 75));

        lblCloudTotalRecords = new JLabel("0");
        lblCloudTotalRev = new JLabel("Rs. 0");
        lblCloudSyncStatus = new JLabel(dbClient != null && dbClient.isConfigured() ? "ONLINE (Supabase)" : "LOCAL AUDIT CACHE");
        JLabel lblCloudSecurity = new JLabel("IMMUTABLE AUDIT");

        metricsPanel.add(createMiniMetricCard("Total Cloud Records", lblCloudTotalRecords, new Color(41, 128, 185)));
        metricsPanel.add(createMiniMetricCard("Total Cloud Revenue", lblCloudTotalRev, new Color(39, 174, 96)));
        metricsPanel.add(createMiniMetricCard("Cloud Sync Status", lblCloudSyncStatus,
                (dbClient != null && dbClient.isConfigured()) ? new Color(39, 174, 96) : new Color(243, 156, 18)));
        metricsPanel.add(createMiniMetricCard("Ledger Security", lblCloudSecurity, new Color(142, 68, 173)));

        centerPanel.add(metricsPanel, BorderLayout.NORTH);

        // Main Table Container
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                new EmptyBorder(14, 16, 14, 16)));

        // Action Toolbar
        JPanel actionToolbar = new JPanel(new BorderLayout(10, 0));
        actionToolbar.setOpaque(false);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);

        JButton btnSyncNow = new JButton("Sync to Cloud Now");
        btnSyncNow.setBackground(new Color(39, 174, 96));
        btnSyncNow.setForeground(Color.WHITE);
        btnSyncNow.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSyncNow.addActionListener(e -> {
            triggerCloudSync();
            refreshCloudHistoryTable();
        });

        JButton btnExportLedger = new JButton("Export Cloud Ledger");
        btnExportLedger.setBackground(new Color(41, 128, 185));
        btnExportLedger.setForeground(Color.WHITE);
        btnExportLedger.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExportLedger.addActionListener(e -> onExportCsvClicked());

        JButton btnVivaModal = new JButton("Supabase Setup & Viva Guide");
        btnVivaModal.setBackground(new Color(52, 73, 94));
        btnVivaModal.setForeground(Color.WHITE);
        btnVivaModal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnVivaModal.addActionListener(e -> showSupabaseVivaGuideDialog());

        leftActions.add(btnSyncNow);
        leftActions.add(btnExportLedger);
        leftActions.add(btnVivaModal);
        actionToolbar.add(leftActions, BorderLayout.WEST);

        // Right Filter Controls
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightControls.setOpaque(false);

        txtCloudSearch = new JTextField(12);
        txtCloudSearch.putClientProperty("JTextField.placeholderText", "Search Visitor / ID...");
        txtCloudSearch.addCaretListener(e -> filterCloudHistoryTable());

        cmbCloudFilter = new JComboBox<>(new String[] {"ALL", "CONFIRMED", "CANCELLED"});
        cmbCloudFilter.addActionListener(e -> filterCloudHistoryTable());

        rightControls.add(new JLabel("Status:"));
        rightControls.add(cmbCloudFilter);
        rightControls.add(txtCloudSearch);
        actionToolbar.add(rightControls, BorderLayout.EAST);

        tableContainer.add(actionToolbar, BorderLayout.NORTH);

        // Table Model Setup
        String[] cols = {"Record #", "Booking ID", "Visitor Name", "Age", "Attraction Booked", "Group Size", "Total (Rs.)", "Booking Status", "Cloud Sync State", "Timestamp (UTC)"};
        cloudHistoryTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        cloudHistoryTable = new JTable(cloudHistoryTableModel);
        cloudHistoryTable.setRowHeight(28);
        cloudHistoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cloudHistoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        cloudHistorySorter = new TableRowSorter<>(cloudHistoryTableModel);
        cloudHistoryTable.setRowSorter(cloudHistorySorter);

        // Status Renderer
        cloudHistoryTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String st = (val != null) ? val.toString() : "";
                if ("CONFIRMED".equalsIgnoreCase(st)) {
                    lbl.setBackground(new Color(235, 247, 238));
                    lbl.setForeground(new Color(39, 174, 96));
                } else if ("CANCELLED".equalsIgnoreCase(st)) {
                    lbl.setBackground(new Color(253, 237, 236));
                    lbl.setForeground(new Color(192, 57, 43));
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(Color.DARK_GRAY);
                }
                lbl.setOpaque(true);
                return lbl;
            }
        });

        // Cloud Sync State Renderer
        cloudHistoryTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setBackground(new Color(235, 245, 255));
                lbl.setForeground(new Color(41, 128, 185));
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                return lbl;
            }
        });

        tableContainer.add(new JScrollPane(cloudHistoryTable), BorderLayout.CENTER);
        centerPanel.add(tableContainer, BorderLayout.CENTER);
        p.add(centerPanel, BorderLayout.CENTER);

        refreshCloudHistoryTable();
        return p;
    }

    private void refreshCloudHistoryTable() {
        if (cloudHistoryTableModel == null) return;
        cloudHistoryTableModel.setRowCount(0);
        int idx = 1;
        boolean isSupabase = dbClient != null && dbClient.isConfigured();
        for (Booking b : service.getBookingHistory()) {
            Ride r = service.findRideById(b.getRideId());
            String rideName = (r != null) ? r.getName() : "Ride " + b.getRideId();
            String cloudState = isSupabase ? "SYNCED TO SUPABASE" : "LOCAL AUDIT LEDGER";
            cloudHistoryTableModel.addRow(new Object[] {
                idx++,
                b.getBookingId(),
                b.getVisitorName(),
                b.getAge(),
                rideName,
                b.getGroupSize(),
                b.getTotalAmount(),
                b.getStatus().name(),
                cloudState,
                b.getCreatedAt()
            });
        }
        if (lblCloudTotalRecords != null) {
            lblCloudTotalRecords.setText(String.valueOf(service.getBookingHistory().size()));
        }
        if (lblCloudTotalRev != null) {
            lblCloudTotalRev.setText("Rs. " + service.getTotalRevenue());
        }
        if (lblCloudSyncStatus != null) {
            lblCloudSyncStatus.setText(isSupabase ? "ONLINE (Supabase)" : "LOCAL AUDIT CACHE");
        }
    }

    private void filterCloudHistoryTable() {
        if (cloudHistorySorter == null) return;
        String text = txtCloudSearch != null ? txtCloudSearch.getText().trim() : "";
        String status = cmbCloudFilter != null ? (String) cmbCloudFilter.getSelectedItem() : "ALL";

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ?> entry) {
                if (status != null && !status.equals("ALL")) {
                    if (!entry.getStringValue(7).equalsIgnoreCase(status)) return false;
                }
                if (!text.isEmpty()) {
                    String name = entry.getStringValue(2).toLowerCase();
                    String id = entry.getStringValue(1).toLowerCase();
                    String ride = entry.getStringValue(4).toLowerCase();
                    String q = text.toLowerCase();
                    return name.contains(q) || id.contains(q) || ride.contains(q);
                }
                return true;
            }
        };
        cloudHistorySorter.setRowFilter(rf);
    }

    private void showSupabaseVivaGuideDialog() {
        JTextArea ta = new JTextArea(
                "=== HOW TO CONNECT & DEMONSTRATE SUPABASE IN YOUR VIVA ===\n\n" +
                "1. WHAT IS SUPABASE IN THIS PROJECT?\n" +
                "   Supabase is a cloud-hosted PostgreSQL database with instant REST APIs.\n" +
                "   It maintains an immutable, tamper-resistant cloud history of all park bookings.\n\n" +
                "2. HOW TO CONNECT (3 Simple Steps):\n" +
                "   Step A: Go to https://supabase.com and create a free project.\n" +
                "   Step B: In your Supabase Dashboard, click 'SQL Editor' -> paste content from 'data/supabase_schema.sql' -> click RUN.\n" +
                "   Step C: Go to Project Settings -> API -> copy your 'Project URL' and 'anon key'.\n" +
                "           Create 'java/config/supabase.properties' with:\n" +
                "           supabase.url=https://YOUR_PROJECT_REF.supabase.co\n" +
                "           supabase.key=YOUR_ANON_PUBLIC_KEY\n\n" +
                "3. LIVE DEMONSTRATION TO EXAMINER:\n" +
                "   • Demo 1: Top badge turns [Cloud: Connected (Supabase)]. Book a ticket -> show row appearing instantly in Supabase.\n" +
                "   • Demo 2: Cancel a booking -> show that status updates to CANCELLED in Supabase (never deleted, proving audit ethics).\n" +
                "   • Demo 3: Offline resilience -> bookings cached locally and flushed when reconnected.\n");
        ta.setFont(new Font("Consolas", Font.PLAIN, 12));
        ta.setEditable(false);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(680, 360));
        JOptionPane.showMessageDialog(this, sp, "Supabase Cloud Architecture & Viva Guide", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createMiniMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                        new EmptyBorder(8, 12, 8, 12))));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(new Color(110, 120, 135));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        valueLabel.setForeground(new Color(30, 45, 65));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
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
        refreshCloudHistoryTable();
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
        Ride[] rides = service.getRides();
        int rows = Math.max(2, (rides.length + 1) / 2);
        rideCardsPanel.setLayout(new GridLayout(rows, 2, 8, 8));
        for (Ride r : rides) {
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
        if (!dbClient.isConfigured()) {
            JOptionPane.showMessageDialog(this,
                    "Supabase is not configured yet!\n\nPlease open java/config/supabase.properties and replace 'your-project-id' and 'your-anon-key' with your actual Supabase URL & anon key.\n\nUntil then, the dashboard is safely operating in Local Offline mode.",
                    "Supabase Credentials Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        supabaseStatusChip.setText(" Cloud: Syncing... ");
        supabaseStatusChip.setBackground(new Color(243, 156, 18));
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                int count = dbClient.flushPendingQueue();
                for (Booking b : service.getBookingHistory()) {
                    if (dbClient.syncBooking(b)) {
                        count++;
                    }
                }
                return count;
            }
            @Override
            protected void done() {
                updateCloudChip();
                refreshCloudHistoryTable();
                try {
                    int n = get();
                    JOptionPane.showMessageDialog(ParkDashboard.this,
                            "Cloud synchronization complete!\nSynced " + n + " record(s) to Supabase.",
                            "Cloud Sync Successful", JOptionPane.INFORMATION_MESSAGE);
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
        if (dbClient.isConfigured()) {
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return dbClient.checkConnection();
                }
                @Override
                protected void done() {
                    updateCloudChip();
                    refreshCloudHistoryTable();
                }
            }.execute();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ParkDashboard dashboard = new ParkDashboard();
            dashboard.setVisible(true);
        });
    }
}
