package net.babustudio.tpoExtractor.extractors;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface AppAncestor {

    void getProperties() throws IOException;

    void getContent() throws SQLException;

    ResultSet getResult();

    void output() throws IOException;

    String getOutputDirectory();
}
