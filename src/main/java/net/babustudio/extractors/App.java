package net.babustudio.extractors;

import net.babustudio.models.Article;
import net.babustudio.utils.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Hello world!
 */


public class App implements AppAncestor {
    private final String sentence = "select articleID,title,paragraphDetail,btype from tbl_toefl_paragraph order by articleID;";
    private Connection conn = null;
    String connectionProperties = "";
    String outputDirectory = "";
    final ArrayList<Article> articles = new ArrayList<Article>();

    App() {
        try { //get the connection to the local database file.
            System.out.println("Getting properties...");
            this.getProperties();
            this.conn = DriverManager.getConnection(this.connectionProperties);
            System.out.println("Successfully connected to the database!");

        } catch (SQLException e) {
            System.err.println("Failed to get the connection! Error(s):\n" + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Failed to get properties...\n" + e.getMessage());
            System.exit(1);
        }
    }

    public void getProperties() throws IOException {
        //InputStream settings = new FileInputStream("settings.properties");
        //Legacy approach for getting the properties is reserved for stability considerations. The one currently being used as shown below:
        InputStream settings = new Thread().getContextClassLoader().getResourceAsStream("settings.properties");
        Properties properties = new Properties();
        properties.load(settings);

        this.connectionProperties = properties.getProperty("databaseUrl");
        this.outputDirectory = properties.getProperty("outputDirectory");
        System.out.println("properties' got.");
    }

    public void getContent() throws SQLException {
        ResultSet result = this.getResult();
        try {
            result.next();
            String content = "";
            String title = "";
            String temp = result.getString("articleID");
            String type = "";
            int i = 1;
            while (true) {
                if (result.getString("articleID").hashCode() != temp.hashCode()) {
                    Article article = new Article();
                    article.type = Integer.valueOf(result.getString("btype"));
                    article.content = Util.replace(content);
                    article.title = title;
                    article.articleID = String.valueOf(i++);
                    articles.add(article);
                    content = "";
                    temp = result.getString("articleID");
                }
                content += result.getString("paragraphDetail");
                temp = result.getString("articleID");
                title = result.getString("title");
                result.next();
            }
        } catch (SQLException e) {
            System.out.println("Contents completely collected. Closing statment.");
            String out = String.format(
                    "Article Collected : %d\n" +
                            "Output Directory  : %s", this.articles.size(), this.outputDirectory);
            System.out.println(out);
        } finally {
            result.close();
            this.conn.close();
        }
    }

    public ResultSet getResult() {
        PreparedStatement statement = null;
        try {
            statement = this.conn.prepareStatement(this.sentence);
        } catch (SQLException e) {
            System.err.println("Errors occurred during Statement creating, aborting!\n " + e.getMessage());
            System.exit(1);
        }
        ResultSet result = null;
        try {
            result = statement.executeQuery();
        } catch (SQLException e) {
            System.err.println("Errors occurred during query, aborting!\n " + e.getMessage());
            System.exit(1);
        }
        return result;
    }

    public void output() throws IOException {
        File dir = new File(this.outputDirectory);
        if (!dir.exists() || !dir.isDirectory()) {
            IOException ioException = new IOException("Failed to access the directory.");
        }
        dir.createNewFile();
        System.out.println("Exporting...");
        int realSequence = 0;
        for (Article article : this.articles) {
            if (article.content.length() < 10) continue;
            realSequence++;
            File mapping = new File(this.outputDirectory + "/" + "[" + realSequence + "] " + article.getTitle() + ".txt");
            mapping.createNewFile();
            FileWriter fileWriter = new FileWriter(mapping);
            fileWriter.write(article.getContent());
            fileWriter.close();
        }
        System.out.println("Done");
    }

}
