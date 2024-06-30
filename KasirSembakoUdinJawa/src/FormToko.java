import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FormToko extends JFrame {
    DefaultTableModel modelStok, modelPembelian;
    JTable tableStok, tablePembelian;
    JTextField txKodeBarangBeli, txJumlahBarang;
    Connection conn;
    NumberFormat currencyFormatter;

    public FormToko() {
        super("Aplikasi Kasir");
        setSize(800, 500);

        // Formatter untuk format Rupiah
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        // Panel utama
        JPanel panelUtama = new JPanel();
        panelUtama.setLayout(null);

        // Tabel Stok
        modelStok = new DefaultTableModel();
        tableStok = new JTable(modelStok);
        JScrollPane scrollPaneStok = new JScrollPane(tableStok);
        scrollPaneStok.setBounds(20, 20, 360, 300);
        panelUtama.add(scrollPaneStok);

        // Kolom tabel stok
        modelStok.addColumn("Kode Barang");
        modelStok.addColumn("Nama Barang");
        modelStok.addColumn("Harga");
        modelStok.addColumn("Stok");

        // Tabel Pembelian
        modelPembelian = new DefaultTableModel();
        tablePembelian = new JTable(modelPembelian);
        JScrollPane scrollPanePembelian = new JScrollPane(tablePembelian);
        scrollPanePembelian.setBounds(420, 20, 360, 300);
        panelUtama.add(scrollPanePembelian);

        // Kolom tabel pembelian
        modelPembelian.addColumn("Kode Barang");
        modelPembelian.addColumn("Nama Barang");
        modelPembelian.addColumn("Harga");
        modelPembelian.addColumn("Jumlah Barang");

        // TextField untuk input kode barang beli
        JLabel lblKodeBarangBeli = new JLabel("Kode Barang untuk Beli");
        lblKodeBarangBeli.setBounds(20, 330, 150, 20);
        panelUtama.add(lblKodeBarangBeli);
        txKodeBarangBeli = new JTextField();
        txKodeBarangBeli.setBounds(180, 330, 100, 20);
        panelUtama.add(txKodeBarangBeli);

        // TextField untuk input jumlah barang
        JLabel lblJumlahBarang = new JLabel("Jumlah Barang");
        lblJumlahBarang.setBounds(300, 330, 100, 20);
        panelUtama.add(lblJumlahBarang);
        txJumlahBarang = new JTextField();
        txJumlahBarang.setBounds(400, 330, 50, 20);
        panelUtama.add(txJumlahBarang);

        // Buttons
        JButton btnTambahBarang = new JButton("Tambah Barang Beli");
        btnTambahBarang.setBounds(470, 330, 160, 20);
        panelUtama.add(btnTambahBarang);
        btnTambahBarang.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tambahBarangBeli();
            }
        });

        JButton btnHitungTotal = new JButton("Hitung Total Harga");
        btnHitungTotal.setBounds(650, 330, 160, 20);
        panelUtama.add(btnHitungTotal);
        btnHitungTotal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hitungTotalHarga();
            }
        });

        JButton btnKonfirmasiPembelian = new JButton("Konfirmasi Pembelian");
        btnKonfirmasiPembelian.setBounds(20, 360, 160, 20);
        panelUtama.add(btnKonfirmasiPembelian);
        btnKonfirmasiPembelian.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                konfirmasiPembelian();
            }
        });

        JButton btnCetakStruk = new JButton("Cetak Struk");
        btnCetakStruk.setBounds(200, 360, 160, 20);
        panelUtama.add(btnCetakStruk);
        btnCetakStruk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cetakStruk();
            }
        });

        JButton btnHapusBarang = new JButton("Hapus Barang");
        btnHapusBarang.setBounds(380, 360, 160, 20);
        panelUtama.add(btnHapusBarang);
        btnHapusBarang.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hapusBarang();
            }
        });

        // Load data dari database saat aplikasi pertama kali dijalankan
        connectDB();
        loadDataStokFromDatabase();

        getContentPane().add(panelUtama);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    void connectDB() {
        try {
            String url = "jdbc:mysql://localhost:3306/sembako";
            String user = "root";
            String password = "";

            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void loadDataStokFromDatabase() {
        try {
            String query = "SELECT kode_barang, nama_barang, harga_barang, stok_barang FROM stok_barang";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String kode = rs.getString("kode_barang");
                String nama = rs.getString("nama_barang");
                int harga = rs.getInt("harga_barang");
                String hargaFormatted = currencyFormatter.format(harga);
                int stok = rs.getInt("stok_barang");
                Object[] data = {kode, nama, hargaFormatted, stok};
                modelStok.addRow(data);
            }

            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    void tambahBarangBeli() {
        String kodeBarangBeli = txKodeBarangBeli.getText();
        int jumlahBarang = Integer.parseInt(txJumlahBarang.getText());
        boolean found = false;

        for (int i = 0; i < modelStok.getRowCount(); i++) {
            String kode = (String) modelStok.getValueAt(i, 0);
            if (kode.equalsIgnoreCase(kodeBarangBeli)) {
                Object[] data = {modelStok.getValueAt(i, 0), modelStok.getValueAt(i, 1), modelStok.getValueAt(i, 2), jumlahBarang};
                modelPembelian.addRow(data);
                found = true;
                break;
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(this, "Kode barang tidak ditemukan dalam stok.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        txKodeBarangBeli.setText("");
        txJumlahBarang.setText("");
    }

    void hitungTotalHarga() {
        int totalHarga = 0;
        for (int i = 0; i < modelPembelian.getRowCount(); i++) {
            String hargaStr = (String) modelPembelian.getValueAt(i, 2);
            try {
                Number harga = currencyFormatter.parse(hargaStr);
                int jumlahBarang = (int) modelPembelian.getValueAt(i, 3);
                totalHarga += harga.intValue() * jumlahBarang;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        String formattedTotalHarga = currencyFormatter.format(totalHarga);

        JOptionPane.showMessageDialog(this, "Total Harga Semua Barang Pembelian: " + formattedTotalHarga);
    }

    void konfirmasiPembelian() {
        int response = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menyelesaikan pembelian?", "Konfirmasi Pembelian", JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "Terima kasih sudah belanja.");
            resetTabelPembelian();
        }
    }

    void cetakStruk() {
        StringBuilder struk = new StringBuilder();
        struk.append("===== STRUK PEMBELIAN =====\n");
        for (int i = 0; i < modelPembelian.getRowCount(); i++) {
            String kode = (String) modelPembelian.getValueAt(i, 0);
            String nama = (String) modelPembelian.getValueAt(i, 1);
            String harga = (String) modelPembelian.getValueAt(i, 2);
            int jumlah = (int) modelPembelian.getValueAt(i, 3);
            struk.append(String.format("Kode: %s, Nama: %s, Harga: %s, Jumlah: %d\n", kode, nama, harga, jumlah));
        }

        int totalHarga = 0;
        for (int i = 0; i < modelPembelian.getRowCount(); i++) {
            String hargaStr = (String) modelPembelian.getValueAt(i, 2);
            try {
                Number harga = currencyFormatter.parse(hargaStr);
                int jumlahBarang = (int) modelPembelian.getValueAt(i, 3);
                totalHarga += harga.intValue() * jumlahBarang;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        String formattedTotalHarga = currencyFormatter.format(totalHarga);
        struk.append("===========================\n");
        struk.append("Total Harga: ").append(formattedTotalHarga).append("\n");

        // Tampilkan struk dalam notifikasi
        JOptionPane.showMessageDialog(this, struk.toString(), "Struk Pembelian", JOptionPane.INFORMATION_MESSAGE);

        // Simpan struk ke file
        try (FileWriter writer = new FileWriter("struk.txt")) {
            writer.write(struk.toString());
            JOptionPane.showMessageDialog(this, "Struk berhasil dicetak dan disimpan sebagai struk.txt.");
            resetTabelPembelian(); // Reset tabel pembelian setelah mencetak struk
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat mencetak struk.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void hapusBarang() {
        int selectedRow = tablePembelian.getSelectedRow();
        if (selectedRow != -1) {
            modelPembelian.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Pilih barang yang ingin dihapus dari tabel pembelian.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void resetTabelPembelian() {
        modelPembelian.setRowCount(0);
    }

    public static void main(String[] args) {
        new FormToko();
    }
}
