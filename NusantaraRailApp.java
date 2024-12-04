import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class NusantaraRailApp extends JFrame {
    private JTextField nameField, birthDateField, departureDateField;
    private JComboBox<String> departureBox, arrivalBox, departureTimeBox, tripClassBox;
    private JTable ticketsTable;
    private DefaultTableModel tableModel;
    private JButton createButton, readButton, updateButton, deleteButton, printButton;
    private JLabel priceLabel;
    private final int BASE_PRICE = 1000000;
    private final int PRICE_INCREMENT = 100000;
    private JComboBox<String> paymentMethodBox;
    private JButton payButton;
    private JButton backButton;

    // Array untuk provinsi berurutan sesuai lokasi geografis
    private final String[] PROVINCES = {
        // Sumatera (dari utara ke selatan)
        "Aceh", "Sumatera Utara", "Sumatera Barat", "Riau", "Kepulauan Riau",
        "Jambi", "Bengkulu", "Sumatera Selatan", "Lampung", "Bangka Belitung",
        // Jawa (dari barat ke timur)
        "DKI Jakarta", "Jawa Barat", "Banten", "Jawa Tengah", "Yogyakarta",
        "Jawa Timur",
        // Indonesia Timur (dari barat ke timur)
        "Bali", "Nusa Tenggara Barat", "Nusa Tenggara Timur",
        // Kalimantan (dari barat ke timur)
        "Kalimantan Barat", "Kalimantan Timur", "Kalimantan Tengah",
        "Kalimantan Selatan", "Kalimantan Utara",
        // Sulawesi (dari utara ke selatan)
        "Sulawesi Utara", "Sulawesi Tengah", "Sulawesi Selatan", "Sulawesi Tenggara",
        "Sulawesi Barat", "Gorontalo",
        // Maluku & Papua (dari barat ke timur)
        "Maluku", "Maluku Utara", "Papua", "Papua Barat", "Papua Selatan",
        "Papua Tengah", "Papua Pegunungan"
    };

    private final String[] TRIP_CLASSES = {
        "Economy", "Premium Economy", "Business", "First Class"
    };

    private final String[] DEPARTURE_TIMES = {
        "06:00", "08:00", "10:00", "12:00", "14:00", "16:00", "18:00"
    };

    private final String[] PAYMENT_METHODS = {
        "Credit Card", "Debit Card", "Bank Transfer", "E-Wallet"
    };

    private JPanel homePanel;

    public NusantaraRailApp() {
        Color backgroundColor = new Color(255, 92, 0);

        setTitle("Nusantara Rail - Booking System");
        setSize(1024, 768);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(backgroundColor);

        Border shadowBorder = BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(20, 20, 20, 20),
        BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true)
        );
    
        // Initialize components
        initializeComponents();
        
        // Create main container with CardLayout
        JPanel mainContainer = new JPanel(new CardLayout());
        mainContainer.setBorder(shadowBorder);
        mainContainer.setBackground(backgroundColor);
        

        homePanel = createHomePanel(mainContainer);

        JPanel headerPanel = createHeaderPanel();
    
        // Booking Form Panel
        JPanel bookingPanel = createBookingFormPanel();
    
        // Table Panel with action buttons
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout(10, 10));
    
        // Table for ticket details
        JScrollPane tableScrollPane = new JScrollPane(ticketsTable);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
    
        // Panel untuk tombol aksi
        JPanel actionButtonPanel = new JPanel();
        actionButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionButtonPanel.setBackground(new Color(174, 209, 233));
        actionButtonPanel.add(updateButton); // Tombol "Edit"
        actionButtonPanel.add(deleteButton); // Tombol "Batalkan Pesanan"
        actionButtonPanel.add(payButton);    // Tombol "Bayar"
        actionButtonPanel.add(printButton);

        // Inisialisasi tombol "Kembali"
        backButton = new JButton("Kembali");
        backButton.addActionListener(e -> {
            CardLayout cardLayout = (CardLayout) mainContainer.getLayout();
            cardLayout.show(mainContainer, "HomePanel");
        });

        styleButton(backButton, new Color(0, 59, 174));  // Terapkan gaya

        // Tambahkan tombol "Kembali" ke panel tombol
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        backButtonPanel.setBackground(new Color(174, 209, 233));
        backButtonPanel.add(backButton);

        // Gabungkan kedua panel ke panel bawah
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(actionButtonPanel, BorderLayout.WEST);
        bottomPanel.add(backButtonPanel, BorderLayout.EAST);
        bottomPanel.setBackground(new Color(174, 209, 233));

        // Tambahkan bottomPanel ke tablePanel
        tablePanel.add(bottomPanel, BorderLayout.SOUTH);
        tablePanel.setBackground(new Color(174, 209, 233));

        // Add panels to CardLayout
        mainContainer.add(homePanel, "HomePanel");
        mainContainer.add(bookingPanel, "BookingPanel");
        mainContainer.add(tablePanel, "TablePanel");
    
        // Add headerPanel to NORTH
        add(headerPanel, BorderLayout.NORTH);

        // Add main container to CENTER
        add(mainContainer, BorderLayout.CENTER);
    
        // Initialize database and load tickets
        loadTickets();
    
        // Add action listener to the "Rincian" button to switch to TablePanel
        readButton.addActionListener(e -> {
            CardLayout cardLayout = (CardLayout) mainContainer.getLayout();
            cardLayout.show(mainContainer, "TablePanel"); // Show the table panel
        });

        updateButton.addActionListener(e -> {
            int selectedRow = ticketsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Pilih tiket yang ingin diedit!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
        
            // Format tanggal
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
            // Mengisi data ke form
            nameField.setText((String) tableModel.getValueAt(selectedRow, 1)); // Nama
        
            // Tanggal lahir (konversi dari java.sql.Date ke String)
            Date birthDate = (Date) tableModel.getValueAt(selectedRow, 2);
            birthDateField.setText(dateFormat.format(birthDate));
        
            // Kelas Perjalanan
            tripClassBox.setSelectedItem(tableModel.getValueAt(selectedRow, 3));
        
            // Dari dan Ke (disabled untuk mencegah perubahan)
            departureBox.setSelectedItem(tableModel.getValueAt(selectedRow, 4));
            arrivalBox.setSelectedItem(tableModel.getValueAt(selectedRow, 5));
        
            // Tanggal Keberangkatan (konversi dari java.sql.Date ke String)
            Date departureDate = (Date) tableModel.getValueAt(selectedRow, 6);
            departureDateField.setText(dateFormat.format(departureDate));
        
            // Waktu keberangkatan
            departureTimeBox.setSelectedItem(tableModel.getValueAt(selectedRow, 7));
        
            // Tampilkan kembali panel BookingPanel
            CardLayout cardLayout = (CardLayout) mainContainer.getLayout();
            cardLayout.show(mainContainer, "BookingPanel");
        
            // Ubah tombol "Buat Pesanan" menjadi "Perbarui Pesanan"
            createButton.setText("Perbarui Pesanan");
        
            // Hapus action listener sebelumnya untuk menghindari duplikasi
            for (ActionListener al : createButton.getActionListeners()) {
                createButton.removeActionListener(al);
            }
        
            // Tambahkan action listener baru untuk memperbarui pesanan
            createButton.addActionListener(updateEvent -> {
                // Perbarui data di tabel
                tableModel.setValueAt(nameField.getText(), selectedRow, 1); // Nama
                tableModel.setValueAt(birthDateField.getText(), selectedRow, 2); // Tanggal Lahir
                tableModel.setValueAt(tripClassBox.getSelectedItem(), selectedRow, 3); // Kelas Perjalanan
                // Tidak mengupdate departure dan arrival, nilai tetap
                tableModel.setValueAt(departureDateField.getText(), selectedRow, 6); // Tanggal Keberangkatan
                tableModel.setValueAt(departureTimeBox.getSelectedItem(), selectedRow, 7); // Waktu
        
                JOptionPane.showMessageDialog(this, "Pesanan berhasil diperbarui!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        
                // Kembalikan teks tombol menjadi "Buat Pesanan"
                createButton.setText("Buat Pesanan");
        
                // Reset action listener tombol untuk kembali ke fungsi awal
                for (ActionListener al : createButton.getActionListeners()) {
                    createButton.removeActionListener(al);
                }
                createButton.addActionListener(evt -> createTicket());
        
                // Kembalikan departure dan arrival box ke mode aktif
                departureBox.setEnabled(true);
                arrivalBox.setEnabled(true);
        
                // Kembali ke panel rincian
                cardLayout.show(mainContainer, "TablePanel");
            });
        });
        
        pack();
        setLocationRelativeTo(null);
    }

    class BackgroundPanel extends JPanel {
        private BufferedImage backgroundImage;
    
        public void setBackgroundImage(String imagePath) {
            try {
                backgroundImage = ImageIO.read(new File(imagePath));
                repaint();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                
                double scaleX = (double) getWidth() / backgroundImage.getWidth();
                double scaleY = (double) getHeight() / backgroundImage.getHeight();
                double scale = Math.max(scaleX, scaleY);
                
                int scaledWidth = (int) (backgroundImage.getWidth() * scale);
                int scaledHeight = (int) (backgroundImage.getHeight() * scale);
                
                int x = (getWidth() - scaledWidth) / 2;
                int y = (getHeight() - scaledHeight) / 2;
                
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(backgroundImage, x, y, scaledWidth, scaledHeight, this);
                
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                g2d.setColor(new Color(98, 141, 189, 180));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.dispose();
            }
        }
    }
    
    private JPanel createHomePanel(JPanel mainContainer) {
        BackgroundPanel homePanel = new BackgroundPanel();
        homePanel.setLayout(new BorderLayout());
        
        homePanel.setOpaque(true);
        homePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel welcomeLabel = new JLabel("Nusantara Rail Ticketing", JLabel.CENTER) {
            {
                setFont(new Font("Segoe UI", Font.BOLD, 50));
                setForeground(new Color(255, 255, 255));
            }
        
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
        
                // Mengatur grafik 2D
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
                // Mengambil FontMetrics untuk mendapatkan ukuran teks
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 5;
        
                // Menggambar outline teks
                g2d.setColor(Color.BLACK); // Warna outline
                for (int i = -2; i <= 2; i++) {
                    for (int j = -2; j <= 2; j++) {
                        if (i != 0 || j != 0) { // Hindari menggambar ulang teks utama
                            g2d.drawString(text, x + i, y + j);
                        }
                    }
                }
        
                // Menggambar teks utama di atas outline
                g2d.setColor(getForeground());
                g2d.drawString(text, x, y);
            }
        };
        
        JLabel subtitleLabel = new JLabel("Perjalanan Nyaman, Tepat Waktu", JLabel.CENTER) {
            {
                setFont(new Font("Segoe UI", Font.PLAIN, 18));
                setForeground(Color.BLACK);
            }
        };
        
        JButton bookTicketButton = new JButton("Pesan Tiket Sekarang") {
            {
                setFont(new Font("Segoe UI", Font.BOLD, 16));
                setBackground(new Color(0, 59, 174));
                setForeground(Color.WHITE);
                setFocusPainted(false);
                setBorderPainted(false);
                setPreferredSize(new Dimension(250, 50));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        };
        
        bookTicketButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                bookTicketButton.setBackground(new Color(25, 118, 210));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                bookTicketButton.setBackground(new Color(0, 59, 174));
            }
        });
        
        bookTicketButton.addActionListener(e -> {
            CardLayout cardLayout = (CardLayout) mainContainer.getLayout();
            cardLayout.show(mainContainer, "BookingPanel");
        });
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(10, 0, 10, 0);
        
        centerPanel.add(welcomeLabel, gbc);
        centerPanel.add(subtitleLabel, gbc);
        centerPanel.add(Box.createVerticalStrut(20), gbc);
        centerPanel.add(bookTicketButton, gbc);
        
        homePanel.add(centerPanel, BorderLayout.CENTER);
        
        homePanel.setBackgroundImage("D:\\Tubes PBOL\\src\\resources\\background2.jpg");
        
        return homePanel;
    }
    
    private void initializeComponents() {
        // Initialize text fields
        nameField = new JTextField(20);
        birthDateField = new JTextField(10);
        departureDateField = new JTextField(10);

        // Initialize combo boxes
        departureBox = new JComboBox<>(PROVINCES);
        arrivalBox = new JComboBox<>(PROVINCES);
        departureTimeBox = new JComboBox<>(DEPARTURE_TIMES);
        tripClassBox = new JComboBox<>(TRIP_CLASSES);
        paymentMethodBox = new JComboBox<>(PAYMENT_METHODS);

        // Initialize price label
        priceLabel = new JLabel("Rp 0");

        // Initialize buttons
        createButton = new JButton("Buat Pesanan");
        readButton = new JButton("Rincian");
        updateButton = new JButton("Edit");
        deleteButton = new JButton("Batalkan Pesanan");
        payButton = new JButton("Bayar");
        printButton = new JButton("Cetak Tiket");

        // Initialize table
        String[] columnNames = {
            "Kode Perjalanan", "Nama", "Tanggal Lahir", "Kelas Perjalanan", "Dari", "Ke",
            "Tanggal Keberangkatan", "Waktu", "No Tempat Duduk", "Harga", "Status Pembayaran"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ticketsTable = new JTable(tableModel);

        // Add action listeners
        createButton.addActionListener(e -> createTicket());
        readButton.addActionListener(e -> loadTickets());
        updateButton.addActionListener(e -> updateTicket());
        deleteButton.addActionListener(e -> deleteTicket());
        payButton.addActionListener(e -> processPayment());
        printButton.addActionListener(e -> {
            int selectedRow = ticketsTable.getSelectedRow();
            if (selectedRow != -1) {
                String ticketNumber = (String) tableModel.getValueAt(selectedRow, 0);
                int id = Integer.parseInt(ticketNumber.replaceAll("[^0-9]", ""));
                printTicket(id); // Panggil metode untuk mencetak tiket
            } else {
                JOptionPane.showMessageDialog(this, "Pilih Tiket yang Ingin Dicetak");
            }
        });

        // Add change listeners for price updates
        departureBox.addActionListener(e -> updatePrice());
        arrivalBox.addActionListener(e -> updatePrice());
        tripClassBox.addActionListener(e -> updatePrice());
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 59, 174));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 100));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Nusantara Rail");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Booking System");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(200, 200, 200));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(new Color(0, 59, 174));
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private JPanel createBookingFormPanel() {
        JPanel bookingPanel = new JPanel(new BorderLayout(10, 10)) {
            private final Image backgroundImage = Toolkit.getDefaultToolkit().getImage("D:\\Tubes PBOL\\src\\resources\\background3.jpg");
    
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
    
                // Gambar latar belakang
                if (backgroundImage != null) {
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
    
                // Tambahkan lapisan semi-transparan di atas gambar
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                g2d.setColor(new Color(98, 141, 189, 180)); // Warna layer semi-transparan
                g2d.fillRect(0, 0, getWidth(), getHeight()); // Tutup seluruh panel
    
                g2d.dispose();
            }
        };
        bookingPanel.setBorder(createRoundedBorder("Informasi Pesanan"));
    
        // Panel form dengan GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setOpaque(false); // Agar transparan mengikuti latar belakang panel utama
            }
        };
    
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
    
        // Detail Penumpang
        addSectionHeader(formPanel, "Detail Penumpang", gbc, 0);
        addFormField(formPanel, "Nama:", nameField, gbc, 1);
        addFormField(formPanel, "Tanggal Lahir (YYYY-MM-DD):", birthDateField, gbc, 2);
    
        // Detail Perjalanan
        addSectionHeader(formPanel, "Detail Perjalanan", gbc, 3);
        addFormField(formPanel, "Kelas Perjalanan:", tripClassBox, gbc, 4);
    
        // Panel Rute
        JPanel routePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        routePanel.setOpaque(false); // Transparan agar mengikuti latar belakang
        addFormField(formPanel, "Rute:", routePanel, gbc, 5);
    
        JPanel fromPanel = new JPanel(new BorderLayout(5, 0));
        fromPanel.setBackground(Color.WHITE);
        fromPanel.add(new JLabel("Dari:"), BorderLayout.WEST);
        fromPanel.add(departureBox);
    
        JPanel toPanel = new JPanel(new BorderLayout(5, 0));
        toPanel.setBackground(Color.WHITE);
        toPanel.add(new JLabel("Ke:"), BorderLayout.WEST);
        toPanel.add(arrivalBox);
    
        routePanel.add(fromPanel);
        routePanel.add(toPanel);
    
        // Jadwal Keberangkatan
        JPanel dateTimePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        dateTimePanel.setOpaque(false);
        addFormField(formPanel, "Jadwal Keberangkatan:", dateTimePanel, gbc, 6);
        dateTimePanel.add(departureDateField);
        dateTimePanel.add(departureTimeBox);
    
        // Total Harga dan Metode Pembayaran
        addSectionHeader(formPanel, "Detail Pembayaran", gbc, 7);
        addFormField(formPanel, "Total Harga:", priceLabel, gbc, 8);
        addFormField(formPanel, "Metode Pembayaran:", paymentMethodBox, gbc, 9);
    
        // Tombol Aksi
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false); // Transparan agar mengikuti latar belakang
        styleButton(createButton, new Color(76, 175, 80));
        styleButton(readButton, new Color(255, 92, 0));
        styleButton(updateButton, new Color(255, 152, 0));
        styleButton(deleteButton, new Color(244, 67, 54));
        styleButton(payButton, new Color(103, 58, 183));
        styleButton(printButton, new Color(25, 118, 210));
    
        buttonPanel.add(createButton);
        buttonPanel.add(readButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(payButton);
        buttonPanel.add(printButton);
    
        // Tambahkan panel form dan tombol ke panel utama
        bookingPanel.add(formPanel, BorderLayout.CENTER);
        bookingPanel.add(buttonPanel, BorderLayout.SOUTH);
    
        return bookingPanel;
    }

    private void addSectionHeader(JPanel panel, String text, GridBagConstraints gbc, int gridy) {
        JLabel header = new JLabel(text);
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setForeground(new Color(0, 59, 174));
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 5, 10);
        panel.add(header, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 10, 5, 10);
    }

    private void addFormField(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int gridy) {
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.weightx = 0.3;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    private Border createRoundedBorder(String title) {
        Border lineBorder = BorderFactory.createLineBorder(new Color(200, 200, 200));
        Border titleBorder = BorderFactory.createTitledBorder(lineBorder, title,
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14));
        Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        return BorderFactory.createCompoundBorder(titleBorder, emptyBorder);
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(createRoundedBorder("Pesanan"));
    
        // Atur JTable
        ticketsTable.setFillsViewportHeight(true);
        ticketsTable.setShowGrid(true);
        ticketsTable.setGridColor(new Color(230, 230, 230));
        ticketsTable.setRowHeight(25);
        ticketsTable.setOpaque(true);
    
        // Atur header JTable
        ticketsTable.getTableHeader().setBackground(new Color(25, 118, 210));;
    
        // Atur JScrollPane
        JScrollPane scrollPane = new JScrollPane(ticketsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(new Color(174, 209, 233));
        scrollPane.getViewport().setBackground(new Color(174, 209, 233)); // Ubah warna viewport
    
        // Tambahkan JScrollPane ke panel
        tablePanel.add(scrollPane, BorderLayout.CENTER);
    
        // Panel untuk tombol
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(174, 209, 233)); // Warna tombol
        JButton printButton = new JButton("Cetak Tiket");
        buttonPanel.add(printButton);
    
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);
    
        return tablePanel;
    }
    
    private JPanel createPrintTicketPanel() {
        JPanel printPanel = new JPanel(new BorderLayout());
        printPanel.setBackground(Color.WHITE);
    
        // Label untuk informasi tiket
        JLabel printLabel = new JLabel("Detail Tiket");
        printLabel.setFont(new Font("Arial", Font.BOLD, 24));
        printPanel.add(printLabel, BorderLayout.NORTH);
    
        // Area untuk menampilkan detail tiket
        JTextArea ticketDetailsArea = new JTextArea();
        ticketDetailsArea.setEditable(false);
        printPanel.add(new JScrollPane(ticketDetailsArea), BorderLayout.CENTER);
    
        // Tombol kembali
        JButton backButton = new JButton("Kembali");
        backButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) getContentPane().getLayout();
            cl.show(getContentPane(), "RincianPanel"); // Kembali ke panel rincian
        });
        printPanel.add(backButton, BorderLayout.SOUTH);
    
        return printPanel;
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(100, 35));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }

    private int calculateDistance(String from, String to) {
        // Mencari indeks provinsi dalam array
        int fromIndex = -1;
        int toIndex = -1;
        
        for (int i = 0; i < PROVINCES.length; i++) {
            if (PROVINCES[i].equals(from)) {
                fromIndex = i;
            }
            if (PROVINCES[i].equals(to)) {
                toIndex = i;
            }
        }
        
        if (fromIndex == -1 || toIndex == -1) {
            return 0;
        }
        
        // Menghitung jarak berdasarkan selisih indeks
        return Math.abs(fromIndex - toIndex);
    }

    private int calculatePrice(String from, String to, String tripClass) {
        int distance = calculateDistance(from, to);
        int basePrice = BASE_PRICE + (distance * PRICE_INCREMENT);
        
        // Menambahkan multiplier berdasarkan kelas Perjalanan
        switch (tripClass) {
            case "Economy":
                return basePrice;
            case "Premium Economy":
                return (int)(basePrice * 1.5);
            case "Business":
                return basePrice * 2;
            case "First Class":
                return basePrice * 3;
            default:
                return basePrice;
        }
    }

    private void updatePrice() {
        String from = departureBox.getSelectedItem().toString();
        String to = arrivalBox.getSelectedItem().toString();
        String tripClass = tripClassBox.getSelectedItem().toString();
        
        int price = calculatePrice(from, to, tripClass);
        priceLabel.setText(String.format("Rp %,d", price));
    }

    private String gettripCode(int id, String tripClass) {
        String classCode;
        switch (tripClass) {
            case "Economy":
                classCode = "E";
                break;
            case "Premium Economy":
                classCode = "PE";
                break;
            case "Business":
                classCode = "B";
                break;
            case "First Class":
                classCode = "FC";
                break;
            default:
                classCode = "X";
        }
        return String.format("%s%03d", classCode, id);
    }

    private String getTicketDetails(int ticketId) {
        StringBuilder details = new StringBuilder();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM tickets WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, ticketId);
            ResultSet rs = ps.executeQuery();
    
            if (rs.next()) {
                details.append("Nama: ").append(rs.getString("name")).append("\n");
                details.append("Tanggal Lahir: ").append(rs.getDate("birth_date")).append("\n");
                details.append("Kelas Perjalanan: ").append(rs.getString("trip_class")).append("\n");
                details.append("Dari: ").append(rs.getString("departure")).append("\n");
                details.append("Ke: ").append(rs.getString("arrival")).append("\n");
                details.append("Tanggal Keberangkatan: ").append(rs.getDate("departure_date")).append("\n");
                details.append("Waktu: ").append(rs.getTime("departure_time")).append("\n");
                details.append("No Tempat Duduk: ").append(rs.getString("seat_number")).append("\n");
                details.append("Harga: Rp ").append(calculatePrice(rs.getString("departure"), rs.getString("arrival"), rs.getString("trip_class"))). append("\n");
                details.append("Status Pembayaran: ").append(rs.getString("payment_status")).append("\n");
            } else {
                details.append("Tiket tidak ditemukan.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            details.append("Error saat mengambil detail tiket: ").append(e.getMessage());
        }
        return details.toString();
    }
    
    private String generateSeatNumber(String tripClass) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT COUNT(*) as count FROM tickets WHERE trip_class = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, tripClass);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt("count") + 1;
                String row;
                int seatNum;
                
                switch (tripClass) {
                    case "Economy":
                        row = String.valueOf((char)('A' + (count-1) / 6));
                        seatNum = (count-1) % 6 + 1;
                        return row + seatNum;
                    case "Premium Economy":
                        row = String.valueOf((char)('A' + (count-1) / 4));
                        seatNum = (count-1) % 4 + 1;
                        return row + seatNum;
                    case "Business":
                        row = String.valueOf((char)('A' + (count-1) / 2));
                        seatNum = (count-1) % 2 + 1;
                        return row + seatNum;
                    case "First Class":
                        return "A" + count;
                    default:
                        return "XX";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "XX";
    }

    private String[] getProvinces() {
        return PROVINCES;
    }

    private void createTicket() {
        String name = nameField.getText().trim();
        String birthDate = birthDateField.getText().trim();
        String departureDate = departureDateField.getText().trim();
        String departureTime = departureTimeBox.getSelectedItem().toString();
        String tripClass = tripClassBox.getSelectedItem().toString();
        String departure = departureBox.getSelectedItem().toString();
        String arrival = arrivalBox.getSelectedItem().toString();
        String seatNumber = generateSeatNumber(tripClass);

        // Validation
        if (name.isEmpty() || birthDate.isEmpty() || departureDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Anda Belum Mengisi Seluruh Fields!");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO tickets (name, birth_date, trip_class, departure, arrival, " +
                          "departure_date, departure_time, seat_number, payment_status) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setDate(2, Date.valueOf(birthDate));
            ps.setString(3, tripClass);
            ps.setString(4, departure);
            ps.setString(5, arrival);
            ps.setDate(6, Date.valueOf(departureDate));
            ps.setTime(7, Time.valueOf(departureTime + ":00"));
            ps.setString(8, seatNumber);
            ps.setString(9, "UNPAID");  // Set initial payment status
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Pesanan Berhasil Dibuat!");
            clearFields();
            loadTickets();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saat Pembuatan Pesanan: " + e.getMessage());
        }
    }

    private void loadTickets() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM tickets ORDER BY id DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
    
            while (rs.next()) {
                String name = rs.getString("name");
                Date birthDate = rs.getDate("birth_date");
                String tripClass = rs.getString("trip_class");
                String departure = rs.getString("departure");
                String arrival = rs.getString("arrival");
                Date departureDate = rs.getDate("departure_date");
                Time departureTime = rs.getTime("departure_time");
                String seatNumber = rs.getString("seat_number");
                String paymentStatus = rs.getString("payment_status");
    
                int price = calculatePrice(departure, arrival, tripClass);
    
                tableModel.addRow(new Object[]{
                    gettripCode(rs.getInt("id"), tripClass), name, birthDate, tripClass,
                    departure, arrival, departureDate, departureTime, seatNumber, 
                    String.format("Rp %,d", price), paymentStatus
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading tickets: " + e.getMessage());
        }
    }    

    private void updateTicket() {
        int selectedRow = ticketsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih Pesanan yang Ingin Diedit");
            return;
        }

        String ticketNumber = (String) tableModel.getValueAt(selectedRow, 0);
        int id = Integer.parseInt(ticketNumber.replaceAll("[^0-9]", ""));
        
        String departureDate = departureDateField.getText().trim();
        String departureTime = departureTimeBox.getSelectedItem().toString();
        String tripClass = tripClassBox.getSelectedItem().toString();
        String departure = departureBox.getSelectedItem().toString();
        String arrival = arrivalBox.getSelectedItem().toString();

        if (departureDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tolong Isi Tanggal Keberangkatan");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE tickets SET trip_class = ?, departure = ?, arrival = ?, departure_date = ?, departure_time = ? " +
                          "WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, tripClass);
            ps.setString(2, departure);
            ps.setString(3, arrival);
            ps.setDate(4, Date.valueOf(departureDate));
            ps.setTime(5, Time.valueOf(departureTime + ":00"));
            ps.setInt(6, id);
            ps.executeUpdate();

            loadTickets();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saat Mengedit Pesanan: " + e.getMessage());
        }
    }

    private void deleteTicket() {
        int selectedRow = ticketsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih Tiket yang Ingin Dibatalkan");
            return;
        }

        String ticketNumber = (String) tableModel.getValueAt(selectedRow, 0);
        int id = Integer.parseInt(ticketNumber.replaceAll("[^0-9]", ""));

        int confirm = JOptionPane.showConfirmDialog(this,
            "Yakin Ingin Membatalkan Tiket?",
            "Konfirmasi Pembatalan",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM tickets WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setInt(1, id);
                ps.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Tiket Berhasil Dibatalkan!");
                loadTickets();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saat Membatalkan: " + e.getMessage());
            }
        }
    }

    private void processPayment() {
        int selectedRow = ticketsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih Tiket yang Ingin Dibayar");
            return;
        }
    
        String ticketNumber = (String) tableModel.getValueAt(selectedRow, 0);
        String paymentStatus = (String) tableModel.getValueAt(selectedRow, 10);
        
        if ("PAID".equals(paymentStatus)) {
            JOptionPane.showMessageDialog(this, "Tiket Ini Telah Dibayar!");
            return;
        }
    
        int id = Integer.parseInt(ticketNumber.replaceAll("[^0-9]", ""));
        String paymentMethod = paymentMethodBox.getSelectedItem().toString();
        
        // Show payment confirmation dialog
        String price = (String) tableModel.getValueAt(selectedRow, 9);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Konfirmasi pembayaran sebesar " + price + " menggunakan " + paymentMethod + "?",
            "Payment Confirmation",
            JOptionPane.YES_NO_OPTION
        );
    
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Update payment status in database
                String query = "UPDATE tickets SET payment_status = ?, payment_method = ?, payment_date = ? WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, "PAID");
                ps.setString(2, paymentMethod);
                ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                ps.setInt(4, id);
                ps.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Pembayaran Berhasil!");
                loadTickets();
                
                // Cetak tiket setelah pembayaran berhasil
                printTicket(id);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saat Proses Pembayaran: " + e.getMessage());
            }
        }
    }
    
    private void printTicket(int ticketId) {
    try (Connection conn = DatabaseConnection.getConnection()) {
        String query = "SELECT * FROM tickets WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Retrieve ticket details
                    String name = rs.getString("name");
                    Date birthDate = rs.getDate("birth_date");
                    String tripClass = rs.getString("trip_class");
                    String departure = rs.getString("departure");
                    String arrival = rs.getString("arrival");
                    Date departureDate = rs.getDate("departure_date");
                    Time departureTime = rs.getTime("departure_time");
                    String seatNumber = rs.getString("seat_number");
                    String paymentStatus = rs.getString("payment_status");
                    int price = calculatePrice(departure, arrival, tripClass);

                    // Create modern ticket view
                    JFrame ticketFrame = new JFrame("Trip Ticket");
                    ticketFrame.setSize(500, 700);
                    ticketFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    ticketFrame.setLocationRelativeTo(null);

                    // Main panel with gradient background
                    JPanel mainPanel = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            Graphics2D g2d = (Graphics2D) g;
                            GradientPaint gradient = new GradientPaint(
                                0, 0, new Color(63, 81, 181), 
                                getWidth(), getHeight(), new Color(100, 181, 246)
                            );
                            g2d.setPaint(gradient);
                            g2d.fillRect(0, 0, getWidth(), getHeight());
                        }
                    };
                    mainPanel.setLayout(new BorderLayout(20, 20));
                    mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

                    // Ticket header
                    JPanel headerPanel = new JPanel(new BorderLayout());
                    headerPanel.setOpaque(false);
                    JLabel titleLabel = new JLabel("Nusantara Rail Ticket", SwingConstants.CENTER);
                    titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
                    titleLabel.setForeground(Color.WHITE);
                    headerPanel.add(titleLabel, BorderLayout.CENTER);

                    // Ticket details panel
                    JPanel detailPanel = new JPanel();
                    detailPanel.setLayout(new GridLayout(0, 2, 10, 15));
                    detailPanel.setOpaque(false);
                    
                    // Custom method to add styled detail rows
                    addTicketDetail(detailPanel, "Passenger Name:", name);
                    addTicketDetail(detailPanel, "Birth Date:", 
                        new SimpleDateFormat("dd MMM yyyy").format(birthDate));
                    addTicketDetail(detailPanel, "Trip Class:", tripClass);
                    addTicketDetail(detailPanel, "Departure:", departure);
                    addTicketDetail(detailPanel, "Arrival:", arrival);
                    addTicketDetail(detailPanel, "Departure Date:", 
                        new SimpleDateFormat("dd MMM yyyy").format(departureDate));
                    addTicketDetail(detailPanel, "Departure Time:", 
                        new SimpleDateFormat("HH:mm").format(departureTime));
                    addTicketDetail(detailPanel, "Seat Number:", seatNumber);
                    addTicketDetail(detailPanel, "Total Price:", 
                        String.format("Rp %,d", price));
                    addTicketDetail(detailPanel, "Payment Status:", paymentStatus);

                    // Assemble the layout
                    mainPanel.add(headerPanel, BorderLayout.NORTH);
                    mainPanel.add(detailPanel, BorderLayout.CENTER);

                    ticketFrame.add(mainPanel);
                    ticketFrame.setVisible(true);

                    // Save ticket as JPG after the main panel is fully laid out
                    SwingUtilities.invokeLater(() -> {
                        BufferedImage image = new BufferedImage(
                            mainPanel.getWidth(), mainPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2d = image.createGraphics();
                        mainPanel.print(g2d);
                        g2d.dispose();
                        File file = new File("tiket.jpg");
                        try {
                            ImageIO.write(image, "jpg", file);
                            JOptionPane.showMessageDialog(ticketFrame, 
                                "Tiket berhasil disimpan sebagai file 'tiket.jpg'.", 
                                "Simpan Tiket", 
                                JOptionPane.INFORMATION_MESSAGE);
                        } catch (IOException e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(null, 
                                "Error saving ticket: " + e.getMessage(),
                                "Save Ticket",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    });

                } else {
                    JOptionPane.showMessageDialog(null, 
                        "Ticket not found.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, 
            "Error retrieving ticket details: " + e.getMessage(), 
            "Database Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}

    private void addTicketDetail(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setForeground(Color.WHITE);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel valueComponent = new JLabel(value);
        valueComponent.setForeground(Color.WHITE);
        valueComponent.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(labelComponent);
        panel.add(valueComponent);
    }

    private void clearFields() {
        nameField.setText("");
        birthDateField.setText("");
        departureDateField.setText("");
        tripClassBox.setSelectedIndex(0);
        departureBox.setSelectedIndex(0);
        arrivalBox.setSelectedIndex(0);
        departureTimeBox.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        try {
            // Set System Look and Feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Customize component fonts
            UIManager.put("Label.font", new Font("Segoe UI", Font.BOLD, 18));
            UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 15));
            UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 15));
            UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("Viewport.background", new Color(174, 209, 233));

        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            NusantaraRailApp app = new NusantaraRailApp();
            app.setVisible(true);
        });
    }
}