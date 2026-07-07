package com.sispro3d.unam.core.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface ParameterBinder {
    void bind(PreparedStatement ps) throws SQLException;
}
