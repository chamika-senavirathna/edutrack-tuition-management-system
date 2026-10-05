package com.edutrack.service;

import com.edutrack.dao.TermDAO;
import com.edutrack.dao.impl.TermDAOImpl;
import com.edutrack.model.Term;
import com.edutrack.util.DB;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Academic terms used by fees, examinations and dashboards. */
public class TermService {

    private final TermDAO termDAO = new TermDAOImpl();

    public List<Term> allTerms() {
        try (Connection conn = DB.getConnection()) {
            return termDAO.findAll(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load terms.", e);
        }
    }

    public Term activeTerm() {
        try (Connection conn = DB.getConnection()) {
            return termDAO.findActive(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load the active term.", e);
        }
    }

    public Term find(long id) {
        try (Connection conn = DB.getConnection()) {
            return termDAO.findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Could not load term.", e);
        }
    }
}
