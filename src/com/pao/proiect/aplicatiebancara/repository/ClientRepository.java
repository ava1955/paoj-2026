package com.pao.proiect.aplicatiebancara.repository;

import com.pao.proiect.aplicatiebancara.exception.InvalidCNPException;
import com.pao.proiect.aplicatiebancara.model.Client;
import com.pao.proiect.aplicatiebancara.model.ClientPremium;
import com.pao.proiect.aplicatiebancara.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClientRepository implements Repository<Client, Integer> {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }
    private LocalDate safeDate(ResultSet rs, String col) throws SQLException {
        Date d = rs.getDate(col);
        return d != null ? d.toLocalDate() : LocalDate.now();
    }
    @Override
    public void save(Client client) throws SQLException {
        String sql = "INSERT INTO clienti " +
                     "(id_client, cnp, nume, prenume, adresa, data_nastere, data_inregistrare, status_client, tip_client) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt   (1, client.getIDClient());
            ps.setString(2, client.getCNP());
            ps.setString(3, client.getNume());
            ps.setString(4, client.getPrenume());
            ps.setString(5, client.getAdresa());
            ps.setDate  (6, Date.valueOf(client.getDataNastere()));
            ps.setDate(7, Date.valueOf(client.getDataInregistrare()));
            ps.setString(8, client.getStatusClient());
            ps.setString(9, client instanceof ClientPremium ? "PREMIUM" : "NORMAL");
            ps.executeUpdate();
        }

        if (client instanceof ClientPremium cp) {
            savePremiumExtension(cp);
        }
    }

    private void savePremiumExtension(ClientPremium cp) throws SQLException {
        String sql = "INSERT INTO clienti_premium " +
                     "(cnp, limita_maxima_tranzactii, dobanda_bonus, acces_credit_rapid) " +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString (1, cp.getCNP());
            ps.setDouble (2, cp.getLimitaMaximaTranzactii());
            ps.setDouble (3, cp.getDobandaBonus());
            ps.setBoolean(4, cp.areAccesLaCreditRapid());
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Client> findById(Integer idClient) throws SQLException {
        String sql = "SELECT c.*, cp.limita_maxima_tranzactii, cp.dobanda_bonus, cp.acces_credit_rapid " +
                     "FROM clienti c " +
                     "LEFT JOIN clienti_premium cp ON c.cnp = cp.cnp " +
                     "WHERE c.id_client = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idClient);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Client> findAll() throws SQLException {
        String sql = "SELECT c.*, cp.limita_maxima_tranzactii, cp.dobanda_bonus, cp.acces_credit_rapid " +
                     "FROM clienti c " +
                     "LEFT JOIN clienti_premium cp ON c.cnp = cp.cnp";
        List<Client> result = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    @Override
    public void update(Client client) throws SQLException {
        String sql = "UPDATE clienti SET nume=?, prenume=?, adresa=?, status_client=? WHERE id_client=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, client.getNume());
            ps.setString(2, client.getPrenume());
            ps.setString(3, client.getAdresa());
            ps.setString(4, client.getStatusClient());
            ps.setInt   (5, client.getIDClient());
            ps.executeUpdate();
        }

        if (client instanceof ClientPremium cp) {
            String sqlP = "UPDATE clienti_premium " +
                          "SET limita_maxima_tranzactii=?, dobanda_bonus=?, acces_credit_rapid=? " +
                          "WHERE cnp=?";
            try (PreparedStatement ps = getConn().prepareStatement(sqlP)) {
                ps.setDouble (1, cp.getLimitaMaximaTranzactii());
                ps.setDouble (2, cp.getDobandaBonus());
                ps.setBoolean(3, cp.areAccesLaCreditRapid());
                ps.setString (4, cp.getCNP());
                ps.executeUpdate();
            }
        }
    }

    @Override
    public void delete(Integer idClient) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement(
                "DELETE FROM clienti WHERE id_client = ?")) {
            ps.setInt(1, idClient);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> findClientiCuNrConturi() throws SQLException {
        String sql = "SELECT c.id_client, c.cnp, c.nume, c.prenume, " +
                     "       COUNT(co.iban) AS nr_conturi " +
                     "FROM clienti c " +
                     "LEFT JOIN conturi co ON c.cnp = co.cnp_titular " +
                     "GROUP BY c.id_client, c.cnp, c.nume, c.prenume " +
                     "ORDER BY c.nume, c.prenume";
        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("idClient",   rs.getInt("id_client"));
                row.put("cnp",        rs.getString("cnp"));
                row.put("nume",       rs.getString("nume"));
                row.put("prenume",    rs.getString("prenume"));
                row.put("nrConturi",  rs.getInt("nr_conturi"));
                result.add(row);
            }
        }
        return result;
    }

    private Client mapRow(ResultSet rs) throws SQLException {
        try {
            String  cnp              = rs.getString("cnp");
            String  nume             = rs.getString("nume");
            String  prenume          = rs.getString("prenume");
            String  adresa           = rs.getString("adresa");
            LocalDate dataNastere = safeDate(rs, "data_nastere");
            LocalDate dataInreg   = safeDate(rs, "data_inregistrare");
            int     idClient         = rs.getInt("id_client");
            String  tipClient        = rs.getString("tip_client");
            String  status           = rs.getString("status_client");

            Client client;
            if ("PREMIUM".equalsIgnoreCase(tipClient)) {
                ClientPremium cp = new ClientPremium(cnp, nume, prenume, adresa, dataNastere, idClient, dataInreg);
                double limita  = rs.getDouble("limita_maxima_tranzactii");
                double dobanda = rs.getDouble("dobanda_bonus");
                boolean acces  = rs.getBoolean("acces_credit_rapid");
                cp.setLimitaMaximaTranzactii(limita);
                cp.setDobandaBonus(dobanda);
                cp.setAccesLaCreditRapid(acces);
                client = cp;
            }
            else {
                client = new Client(cnp, nume, prenume, adresa, dataNastere, idClient, dataInreg);
            }
            client.setStatusClient(status);
            return client;

        }
        catch (InvalidCNPException e) {
            throw new SQLException("CNP invalid in baza de date: " + e.getMessage(), e);
        }
    }
}
