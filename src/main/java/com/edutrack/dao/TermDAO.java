package com.edutrack.dao;

import com.edutrack.model.Term;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** DAO for academic terms (shared). */
public interface TermDAO {
    List<Term> findAll(Connection conn) throws SQLException;

    Term findById(Connection conn, long id) throws SQLException;

    Term findActive(Connection conn) throws SQLException;

    Term insert(Connection conn, Term term) throws SQLException;

    void update(Connection conn, Term term) throws SQLException;
}
