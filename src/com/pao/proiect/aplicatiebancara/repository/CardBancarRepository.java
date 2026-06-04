package com.pao.proiect.aplicatiebancara.repository;

import com.pao.proiect.aplicatiebancara.exception.InvalidPINException;
import com.pao.proiect.aplicatiebancara.model.CardBancar;
import com.pao.proiect.aplicatiebancara.model.ContBancar;
import com.pao.proiect.aplicatiebancara.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CardBancarRepository implements Repository<CardBancar, String> {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }
    private LocalDate safeDate(ResultSet rs, String col) throws SQLException {
        Date d = rs.getDate(col);
        return d != null ? d.toLocalDate() : LocalDate.now().plusYears(4);
    }

    @Override
    public void save(CardBancar card) throws SQLException {
        String sql = "INSERT INTO carduri (numar_card, pin, data_expirare, tip_card, iban_cont, blocat) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString (1, card.getNumarCard());
            ps.setString (2, card.getPIN());
            ps.setDate   (3, Date.valueOf(LocalDate.now().plusYears(4)));
            ps.setString (4, card.getTipCard().name());
            ps.setString (5, card.getContAsociat().getIBAN());
            ps.setBoolean(6, card.esteBlocat());
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<CardBancar> findById(String numarCard) throws SQLException {
        throw new UnsupportedOperationException(
            "Folositi findById(numarCard, contMap) pentru a rezolva contul asociat.");
    }

    public Optional<CardBancar> findById(String numarCard, Map<String, ContBancar> contMap) throws SQLException {
        String sql = "SELECT * FROM carduri WHERE numar_card = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, numarCard);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs, contMap));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<CardBancar> findAll() throws SQLException {
        throw new UnsupportedOperationException(
            "Folositi findAll(contMap) pentru a rezolva conturile asociate.");
    }

    public List<CardBancar> findAll(Map<String, ContBancar> contMap) throws SQLException {
        String sql = "SELECT * FROM carduri";
        List<CardBancar> result = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs, contMap));
        }
        return result;
    }

    @Override
    public void update(CardBancar card) throws SQLException {
        String sql = "UPDATE carduri SET blocat = ? WHERE numar_card = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setBoolean(1, card.esteBlocat());
            ps.setString (2, card.getNumarCard());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(String numarCard) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement(
                "DELETE FROM carduri WHERE numar_card = ?")) {
            ps.setString(1, numarCard);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> findCarduriCuDateContSiClient() throws SQLException {
        String sql = "SELECT ca.numar_card, ca.tip_card, ca.blocat, ca.data_expirare, " +
                     "       co.iban, co.sold, co.valuta, " +
                     "       c.cnp, c.nume, c.prenume " +
                     "FROM carduri ca " +
                     "INNER JOIN conturi  co ON ca.iban_cont     = co.iban " +
                     "INNER JOIN clienti  c  ON co.cnp_titular   = c.cnp " +
                     "ORDER BY c.nume, c.prenume, co.iban";
        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("numarCard",    rs.getString("numar_card"));
                row.put("tipCard",      rs.getString("tip_card"));
                row.put("blocat",       rs.getBoolean("blocat"));
                row.put("dataExpirare", rs.getDate("data_expirare"));
                row.put("iban",         rs.getString("iban"));
                row.put("sold",         rs.getDouble("sold"));
                row.put("valuta",       rs.getString("valuta"));
                row.put("cnp",          rs.getString("cnp"));
                row.put("numeClient",   rs.getString("nume") + " " + rs.getString("prenume"));
                result.add(row);
            }
        }
        return result;
    }

    private CardBancar mapRow(ResultSet rs, Map<String, ContBancar> contMap) throws SQLException {
        try {
            String     numarCard    = rs.getString("numar_card");
            String     pin          = rs.getString("pin");
            LocalDate dataExpirare = safeDate(rs, "data_expirare");
            CardBancar.TipCard tip  = CardBancar.TipCard.valueOf(rs.getString("tip_card"));
            String     ibanCont     = rs.getString("iban_cont");
            boolean    blocat       = rs.getBoolean("blocat");

            ContBancar cont = contMap.get(ibanCont);
            if (cont == null) {
                throw new SQLException("Contul cu IBAN " + ibanCont + " nu a fost gasit in Map.");
            }
            CardBancar card = new CardBancar(numarCard, pin, dataExpirare, tip, cont, blocat);
            if (blocat) card.blocheazaCard();
            return card;
        } catch (InvalidPINException e) {
            throw new SQLException("PIN invalid in baza de date: " + e.getMessage(), e);
        }
    }
}
