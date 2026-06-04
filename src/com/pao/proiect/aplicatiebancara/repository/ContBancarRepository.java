package com.pao.proiect.aplicatiebancara.repository;

import com.pao.proiect.aplicatiebancara.model.Client;
import com.pao.proiect.aplicatiebancara.model.ContBancar;
import com.pao.proiect.aplicatiebancara.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class ContBancarRepository implements Repository<ContBancar, String> {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }
    private LocalDate safeDate(ResultSet rs, String col) throws SQLException {
        Date d = rs.getDate(col);
        return d != null ? d.toLocalDate() : LocalDate.now();
    }
    @Override
    public void save(ContBancar cont) throws SQLException {
        String sql = "INSERT INTO conturi (iban, sold, valuta, data_deschidere, cnp_titular) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, cont.getIBAN());
            ps.setDouble(2, cont.getSold());
            ps.setString(3, "RON");
            ps.setDate  (4, Date.valueOf(LocalDate.now()));
            ps.setString(5, cont.getTitular().getCNP());
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<ContBancar> findById(String iban) throws SQLException {
        throw new UnsupportedOperationException(
            "Folositi findById(iban, clientMap) pentru a rezolva titularul.");
    }


    public Optional<ContBancar> findById(String iban, Map<String, Client> clientMap) throws SQLException {
        String sql = "SELECT * FROM conturi WHERE iban = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, iban);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs, clientMap));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<ContBancar> findAll() throws SQLException {
        throw new UnsupportedOperationException(
            "Folositi findAll(clientMap) pentru a rezolva titularii.");
    }

    public List<ContBancar> findAll(Map<String, Client> clientMap) throws SQLException {
        String sql = "SELECT * FROM conturi";
        List<ContBancar> result = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs, clientMap));
        }
        return result;
    }

    @Override
    public void update(ContBancar cont) throws SQLException {
        String sql = "UPDATE conturi SET sold = ? WHERE iban = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDouble(1, cont.getSold());
            ps.setString(2, cont.getIBAN());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(String iban) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement(
                "DELETE FROM conturi WHERE iban = ?")) {
            ps.setString(1, iban);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> findConturiCuDateTitular() throws SQLException {
        String sql = "SELECT co.iban, co.sold, co.valuta, co.data_deschidere, " +
                     "       c.id_client, c.cnp, c.nume, c.prenume, c.status_client " +
                     "FROM conturi co " +
                     "INNER JOIN clienti c ON co.cnp_titular = c.cnp " +
                     "ORDER BY c.nume, c.prenume, co.iban";
        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("iban",           rs.getString("iban"));
                row.put("sold",           rs.getDouble("sold"));
                row.put("valuta",         rs.getString("valuta"));
                row.put("dataDeschidere", rs.getDate("data_deschidere"));
                row.put("idClient",       rs.getInt("id_client"));
                row.put("cnp",            rs.getString("cnp"));
                row.put("numeClient",     rs.getString("nume") + " " + rs.getString("prenume"));
                row.put("status",         rs.getString("status_client"));
                result.add(row);
            }
        }
        return result;
    }

    private ContBancar mapRow(ResultSet rs, Map<String, Client> clientMap) throws SQLException {
        String cnpTitular  = rs.getString("cnp_titular");
        Client titular     = clientMap.get(cnpTitular);
        if (titular == null) {
            throw new SQLException("Titularul cu CNP " + cnpTitular + " nu a fost gasit in Map.");
        }
        String    iban    = rs.getString("iban");
        double    sold    = rs.getDouble("sold");
        LocalDate dataD = safeDate(rs, "data_deschidere");
        return new ContBancar(iban, sold, "RON", dataD, titular);
    }
}
