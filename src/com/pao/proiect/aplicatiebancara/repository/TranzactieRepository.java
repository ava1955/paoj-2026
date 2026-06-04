package com.pao.proiect.aplicatiebancara.repository;

import com.pao.proiect.aplicatiebancara.model.ContBancar;
import com.pao.proiect.aplicatiebancara.model.TipTranzactie;
import com.pao.proiect.aplicatiebancara.model.Tranzactie;
import com.pao.proiect.aplicatiebancara.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TranzactieRepository implements Repository<Tranzactie, String> {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }
    private LocalDateTime safeTimestamp(ResultSet rs, String col) throws SQLException {
        Timestamp ts = rs.getTimestamp(col);
        return ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
    }

    @Override
    public void save(Tranzactie t) throws SQLException {
        String sql = "INSERT INTO tranzactii " +
                     "(id_tranzactie, suma, data_si_ora, descriere, tip_tranzactie, iban_sursa, iban_destinatar) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString   (1, t.idTranzactie());
            ps.setDouble   (2, t.suma());
            ps.setTimestamp(3, Timestamp.valueOf(t.dataSiOra()));
            ps.setString   (4, t.descriere());
            ps.setString   (5, t.tipTranzactie().name());
            ps.setString   (6, t.contSursa()     != null ? t.contSursa().getIBAN()     : null);
            ps.setString   (7, t.contDestinatar() != null ? t.contDestinatar().getIBAN() : null);
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Tranzactie> findById(String idTranzactie) throws SQLException {
        throw new UnsupportedOperationException(
            "Folositi findById(id, contMap) pentru a rezolva conturile.");
    }

    public Optional<Tranzactie> findById(String id, Map<String, ContBancar> contMap) throws SQLException {
        String sql = "SELECT * FROM tranzactii WHERE id_tranzactie = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs, contMap));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Tranzactie> findAll() throws SQLException {
        throw new UnsupportedOperationException(
            "Folositi findAll(contMap) pentru a rezolva conturile asociate.");
    }

    public List<Tranzactie> findAll(Map<String, ContBancar> contMap) throws SQLException {
        String sql = "SELECT * FROM tranzactii ORDER BY data_si_ora";
        List<Tranzactie> result = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs, contMap));
        }
        return result;
    }

    @Override
    public void update(Tranzactie entity) {
        throw new UnsupportedOperationException("Tranzactiile nu pot fi modificate.");
    }

    @Override
    public void delete(String idTranzactie) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement(
                "DELETE FROM tranzactii WHERE id_tranzactie = ?")) {
            ps.setString(1, idTranzactie);
            ps.executeUpdate();
        }
    }

    public void efectueazaTransferTranzactional(Tranzactie tranzactie) throws SQLException {
        if (tranzactie.tipTranzactie() != TipTranzactie.TRANSFER) {
            throw new IllegalArgumentException("Metoda accepta doar tranzactii de tip TRANSFER.");
        }

        Connection conn = getConn();
        conn.setAutoCommit(false);

        try {
            String sqlDebiteaza = "UPDATE conturi SET sold = sold - ? WHERE iban = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlDebiteaza)) {
                ps.setDouble(1, tranzactie.suma());
                ps.setString(2, tranzactie.contSursa().getIBAN());
                ps.executeUpdate();
            }
            String sqlCrediteaza = "UPDATE conturi SET sold = sold + ? WHERE iban = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlCrediteaza)) {
                ps.setDouble(1, tranzactie.suma());
                ps.setString(2, tranzactie.contDestinatar().getIBAN());
                ps.executeUpdate();
            }
            String sqlInsert =
                "INSERT INTO tranzactii " +
                "(id_tranzactie, suma, data_si_ora, descriere, tip_tranzactie, iban_sursa, iban_destinatar) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setString   (1, tranzactie.idTranzactie());
                ps.setDouble   (2, tranzactie.suma());
                ps.setTimestamp(3, Timestamp.valueOf(tranzactie.dataSiOra()));
                ps.setString   (4, tranzactie.descriere());
                ps.setString   (5, tranzactie.tipTranzactie().name());
                ps.setString   (6, tranzactie.contSursa().getIBAN());
                ps.setString   (7, tranzactie.contDestinatar().getIBAN());
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("[DB] Transfer persistent cu succes: " + tranzactie.idTranzactie());

        }
        catch (SQLException e) {
            conn.rollback();
            System.err.println("[DB] Transfer esuat, rollback efectuat: " + e.getMessage());
            throw e;
        }
        finally {
            conn.setAutoCommit(true);
        }
    }

    public void salveazaSiActualizeazaSold(Tranzactie t) throws SQLException {
        Connection conn = getConn();
        conn.setAutoCommit(false);
        try {
            String sqlSold = "UPDATE conturi SET sold = ? WHERE iban = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlSold)) {
                ps.setDouble(1, t.contSursa().getSold());
                ps.setString(2, t.contSursa().getIBAN());
                ps.executeUpdate();
            }
            save(t);
            conn.commit();
        }
        catch (SQLException e) {
            conn.rollback();
            throw e;
        }
        finally {
            conn.setAutoCommit(true);
        }
    }

    public List<Map<String, Object>> findTranzactiiCuDateConturi() throws SQLException {
        String sql =
            "SELECT t.id_tranzactie, t.suma, t.data_si_ora, t.tip_tranzactie, t.descriere, " +
            "       cs.iban AS iban_sursa,  cs.sold AS sold_sursa, " +
            "       cl_s.nume AS nume_sursa, cl_s.prenume AS prenume_sursa, " +
            "       cd.iban AS iban_dest, cd.sold AS sold_dest, " +
            "       cl_d.nume AS nume_dest, cl_d.prenume AS prenume_dest " +
            "FROM tranzactii t " +
            "LEFT JOIN conturi cs  ON t.iban_sursa      = cs.iban " +
            "LEFT JOIN clienti cl_s ON cs.cnp_titular   = cl_s.cnp " +
            "LEFT JOIN conturi cd   ON t.iban_destinatar = cd.iban " +
            "LEFT JOIN clienti cl_d ON cd.cnp_titular   = cl_d.cnp " +
            "ORDER BY t.data_si_ora DESC";

        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("idTranzactie",  rs.getString("id_tranzactie"));
                row.put("suma",          rs.getDouble("suma"));
                row.put("dataSiOra",     rs.getTimestamp("data_si_ora"));
                row.put("tip",           rs.getString("tip_tranzactie"));
                row.put("descriere",     rs.getString("descriere"));
                row.put("ibanSursa",     rs.getString("iban_sursa"));
                String numeSursa = rs.getString("nume_sursa");
                String prenumeSursa = rs.getString("prenume_sursa");
                row.put("numeSursa", numeSursa != null ? numeSursa + " " + prenumeSursa : "N/A");
                row.put("ibanDest",      rs.getString("iban_dest"));
                String numeDest = rs.getString("nume_dest");
                String prenumeDest = rs.getString("prenume_dest");
                row.put("numeDest", numeDest != null ? numeDest + " " + prenumeDest : "N/A");
                result.add(row);
            }
        }
        return result;
    }

    private Tranzactie mapRow(ResultSet rs, Map<String, ContBancar> contMap) throws SQLException {
        String        id          = rs.getString("id_tranzactie");
        double        suma        = rs.getDouble("suma");
        LocalDateTime dataSiOra = safeTimestamp(rs, "data_si_ora");
        String        descriere   = rs.getString("descriere");
        TipTranzactie tip         = TipTranzactie.valueOf(rs.getString("tip_tranzactie"));
        String        ibanSursa   = rs.getString("iban_sursa");
        String        ibanDest    = rs.getString("iban_destinatar");

        ContBancar sursa     = ibanSursa != null ? contMap.get(ibanSursa) : null;
        ContBancar destinatar = ibanDest  != null ? contMap.get(ibanDest)  : null;

        return new Tranzactie(id, suma, dataSiOra, descriere, tip, sursa, destinatar);
    }
}
