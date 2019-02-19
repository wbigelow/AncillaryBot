package com.wbigelow.ancillary.modules;

import com.google.common.collect.ImmutableList;
import com.wbigelow.ancillary.Command;
import com.wbigelow.ancillary.Module;
import com.wbigelow.ancillary.PermissionLevel;
import lombok.NoArgsConstructor;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;

import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * An Ancillary Module that adds basic quote functionality.
 * @author Jacob Siegelman (/u/tenebre55)
 *
 */
public class QuoteModule implements Module {

    //The number of characters is ">quote ", ">addquote " and ">delquote ".
    private final int QUOTE_LENGTH = 7;
    private final int ADD_QUOTE_LENGTH = 10;
    private final int DEL_QUOTE_LENGTH = 10;

    
    public QuoteModule() {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:db")){
            Statement s = con.createStatement();
            s.execute("CREATE TABLE IF NOT EXISTS quotes (\n"
                    + "id integer PRIMARY KEY,\n"
                    + "quote text NOT NULL,\n"
                    + "user text NOT NULL);");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public List<Command> getCommands() {
        return ImmutableList.of(
                new GetQuoteCommand(),
                new AddQuoteCommand(),
                new RemoveQuoteCommand()
        );
    }

    @NoArgsConstructor
    final class GetQuoteCommand implements Command {

        @Override
        public String getName() {
            return "quote";
        }

        @Override
        public String getDescription() {
            return "View a saved quote. >quote, >quote Tenebre55, or >quote 10. ";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        private String getQuote(final Message message) {
            String content = message.getContent(); // remove command
            
            // special case of getting quote by ID
            if(content.length() >= QUOTE_LENGTH && content.substring(QUOTE_LENGTH).matches("\\d+")) {
                try (Connection con = DriverManager.getConnection("jdbc:sqlite:db");
                        PreparedStatement ps = con.prepareStatement("SELECT id, quote, user FROM quotes WHERE id = ?")){
                    ps.setString(1, content.substring(QUOTE_LENGTH).trim());
                    ResultSet rs = ps.executeQuery();
                    if(rs.next()) {
                        return "[#" + rs.getString("id") + "] \"" + rs.getString("quote") + "\" -" + rs.getString("user");
                    } else {
                        return "No quote found with id " + content.substring(QUOTE_LENGTH);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    return "An error occurred while accessing the database.";
                }
            }
            
            // get quotes from db by username or randomly
            String nameQuery = "SELECT id, quote, user FROM quotes WHERE user = ? ORDER BY RANDOM() LIMIT 1";
            String stdQuery = "SELECT id, quote, user FROM quotes ORDER BY RANDOM() LIMIT 1";
            try (Connection con = DriverManager.getConnection("jdbc:sqlite:db");
                    PreparedStatement ps = con.prepareStatement(content.length() >= QUOTE_LENGTH ? nameQuery : stdQuery)){
                if(content.length() >= QUOTE_LENGTH) {
                    ps.setString(1, content.substring(QUOTE_LENGTH).trim());
                }
                ResultSet rs = ps.executeQuery();
                
                if(rs.next()) {
                    return "[#" + rs.getString("id") + "] \"" + rs.getString("quote") + "\" -" + rs.getString("user");
                } else {
                    return content.length() > QUOTE_LENGTH ? "No quote from user " + content.substring(QUOTE_LENGTH) : "No quote found.";
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "An error occurred while accessing the database.";
            }
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            new MessageBuilder()
                    .append(getQuote(message))
                    .send(message.getChannel());
        }
    }
    
    @NoArgsConstructor
    final class AddQuoteCommand implements Command {

        @Override
        public String getName() {
            return "addquote";
        }

        @Override
        public String getDescription() {
            return "Add a new quote. >addquote \"This is a test of the addquote command\" Tenebre55";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.ANY;
        }

        private String addQuote(final Message message) {
            String[] content = message.getContent().substring(ADD_QUOTE_LENGTH).split("\""); // remove command
            if(content.length < 3) {
                return "Please use the correct format: >addquote \"quote\" user";
            }
            // add quote to db
            try (Connection con = DriverManager.getConnection("jdbc:sqlite:db");
                    PreparedStatement ps = con.prepareStatement("INSERT INTO quotes(quote, user) VALUES(?,?)")){
                ps.setString(1, content[1].trim());
                ps.setString(2, content[2].trim());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                
                if(rs.next()) {
                    return "Added quote" + content[0] + " from user " + content[1] + " with ID: " + rs.getString("last_insert_rowid()");
                } else {
                    return "Could not find generated ID";
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "An error occurred while accessing the database.";
            }
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            new MessageBuilder()
                    .append(addQuote(message))
                    .send(message.getChannel());
        }
    }
    @NoArgsConstructor
    final class RemoveQuoteCommand implements Command {

        @Override
        public String getName() {
            return "delquote";
        }

        @Override
        public String getDescription() {
            return "Delete a quote. >delquote 10";
        }

        @Override
        public PermissionLevel getRequiredPermissionLevel() {
            return PermissionLevel.MOD;
        }

        private String deleteQuote(final Message message) {
            String content = message.getContent();
            try (Connection con = DriverManager.getConnection("jdbc:sqlite:db");
                    PreparedStatement ps1 = con.prepareStatement("SELECT id, quote FROM quotes WHERE id = ?");
                    PreparedStatement ps2 = con.prepareStatement("DELETE FROM quotes WHERE id = ?")){
                ps1.setString(1, content.substring(DEL_QUOTE_LENGTH));
                ResultSet rs = ps1.executeQuery();
                if(rs.next()) {
                    ps2.setString(1, content.substring(DEL_QUOTE_LENGTH));
                    int id = rs.getInt("id");
                    String quote = rs.getString("quote");
                    int res = ps2.executeUpdate();
                    if(res > 0) {
                        return "Deleted quote #" + id + ": \"" + quote + "\"";
                    } else {
                        return "Quote found, but delete failed.";
                    }
                } else {
                    return "Could not find quote #" + content.substring(DEL_QUOTE_LENGTH);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "An error occurred while accessing the database.";
            }
        }

        @Override
        public void execute(final Message message, final DiscordApi discordApi) {
            new MessageBuilder()
                    .append(deleteQuote(message))
                    .send(message.getChannel());
        }
    }
}
