package io.quarkiverse.embedded.postgresql.it;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.sql.DataSource;

import io.quarkus.arc.profile.IfBuildProfile;

@Dependent
@IfBuildProfile("jdbc-multiple-datasources")
public class JDBCEmbeddedRepositoryMultipleDataSources implements EmbeddedRepository {

    private static final String FIND_ALL = "SELECT id, name FROM inmemory_postgresql ORDER BY name ASC";
    private static final String FIND_BY_ID = "SELECT id, name FROM inmemory_postgresql WHERE id = ?";
    private static final String SAVE = "INSERT INTO inmemory_postgresql (name) VALUES (?) RETURNING id";
    private static final String SAVE_OR_UPDATE = "INSERT INTO inmemory_postgresql (id, name) VALUES (?, ?)  ON CONFLICT(id) DO UPDATE SET name = ? returning (xmax = 0) AS inserted";
    private static final String DELETE = "DELETE FROM inmemory_postgresql WHERE id = ?";

    private DataSource dataSource;

    public JDBCEmbeddedRepositoryMultipleDataSources(@io.quarkus.agroal.DataSource("database2") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Collection<EmbeddedVO> findAll() {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(FIND_ALL)) {
                Collection<EmbeddedVO> result = new ArrayList<>();
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        result.add(from(rs));
                    }
                }
                return result;
            }
        } catch (SQLException io) {
            throw new IllegalStateException(io);
        }
    }

    private EmbeddedVO from(ResultSet rs) throws SQLException {
        return new EmbeddedVO(rs.getLong(1), rs.getString(2));
    }

    @Override
    public Optional<EmbeddedVO> findById(Long id) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? Optional.of(from(rs)) : Optional.empty();
                }
            }
        } catch (SQLException io) {
            throw new IllegalStateException(io);
        }
    }

    @Override
    public long create(EmbeddedBody body) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(SAVE)) {
                stmt.setString(1, body.getName());
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    return rs.getLong(1);
                }
            }
        } catch (SQLException io) {
            throw new IllegalStateException(io);
        }
    }

    @Override
    public boolean createOrUpdate(EmbeddedVO vo) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(SAVE_OR_UPDATE)) {
                stmt.setLong(1, vo.getId());
                stmt.setString(2, vo.getName());
                stmt.setString(3, vo.getName());
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    return rs.getBoolean("inserted");
                }
            }
        } catch (SQLException io) {
            throw new IllegalStateException(io);
        }
    }

    @Override
    public boolean delete(long id) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(DELETE)) {
                stmt.setLong(1, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException io) {
            throw new IllegalStateException(io);
        }
    }
}
