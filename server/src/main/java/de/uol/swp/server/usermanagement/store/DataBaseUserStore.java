package de.uol.swp.server.usermanagement.store;

import com.google.common.base.Strings;
import de.uol.swp.common.game.dto.UserDTO;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.player.PlayerProfile;
import de.uol.swp.server.database.DataBaseConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is a database user store.
 *
 * @implNote This store never returns a user's password!
 * @see AbstractUserStore
 * @see UserStore
 */
@SuppressWarnings("java:S1192")
public class DataBaseUserStore extends AbstractUserStore implements UserStore {

    private static final String EMAIL_ADDRESS_COLUMN = "email_address";
    private static final String ID_COLUMN = "id";
    private static final String HASH_COLUMN = "hash";
    private static final String SALT_COLUMN = "salt";
    private static final String USERNAME_COLUMN = "name";
    private static final Logger LOG = LogManager.getLogger(DataBaseUserStore.class);
    private final DataBaseConnection dataBaseConnection;
    private String tableName = "user";

    /**
     * Constructor for initialize
     *
     * @see DataBaseConnection
     */
    public DataBaseUserStore(boolean testDataBase) {
        if (testDataBase) {
            tableName = "test";
        }
        this.dataBaseConnection = new DataBaseConnection();
    }

    @Override
    public Optional<User> findUser(String username, String password) {
        String valid = "select * from " + tableName + " where name = ?";
        try (Connection connection = dataBaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(valid)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String userId = resultSet.getString(ID_COLUMN);
                byte[] passwordHash = resultSet.getBytes(HASH_COLUMN);
                byte[] passwordSalt = resultSet.getBytes(SALT_COLUMN);
                String eMail = resultSet.getString(EMAIL_ADDRESS_COLUMN);
                if (validatePassword(password, passwordHash, passwordSalt)) {
                    return Optional.of(new UserDTO(userId, username, passwordHash, passwordSalt, eMail));
                }
            }
        } catch (SQLException e) {
            LOG.error("The user was not found: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findUser(String username) {
        String valid = "select * from " + tableName + " where name = ?";
        try (Connection connection = dataBaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(valid)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String userId = resultSet.getString(ID_COLUMN);
                String email = resultSet.getString(EMAIL_ADDRESS_COLUMN);
                User user = UserDTO.createWithoutPassword(userId, username, email);
                return Optional.of(user);
            }
        } catch (SQLException e) {
            LOG.error("The user was not found: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public User createUser(String username, String password, String eMail) {
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalArgumentException("Username must not be null");
        }
        byte[][] passwordHashWithSalt = createHashWithSalt(password);
        String insertFields = "INSERT INTO " + tableName + " (name, email_address, hash, salt, games_won, games_lost)";
        String insertValues = "VALUES (?, ?, ?, ?, ?, ?)";
        String insertToRegister = insertFields + insertValues;
        try (Connection connection = dataBaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(insertToRegister,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, eMail);
            statement.setBytes(3, passwordHashWithSalt[1]);
            statement.setBytes(4, passwordHashWithSalt[0]);
            statement.setShort(5, (short) 0);
            statement.setShort(6, (short) 0);
            statement.execute();

            ResultSet userId = statement.getGeneratedKeys();
            if (userId.next()) {
                return new UserDTO(userId.getString(1), username, passwordHashWithSalt[1], passwordHashWithSalt[0], eMail);
            }
        } catch (SQLException e) {
            LOG.error("Could not insert new user into database: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public String updateUserName(String username, String newUserName) {
        if (Strings.isNullOrEmpty(newUserName)) {
            throw new IllegalArgumentException("Username must not be null");
        }
        final String valid = "Update " + tableName + " set name = ? where name = ?";
        return getUpdatedUser(username, newUserName, valid);
    }

    @Override
    public String updateUserEmail(String username, String eMail) {
        String valid = "Update " + tableName + " set email_address = ? where name = ?";

        return getUpdatedUser(username, eMail, valid);
    }

    /**
     * User is being updated
     * @param username of user
     * @param updatedValue eMail of user
     * @param valid SQL query
     * @return updated user
     */
    private String getUpdatedUser(String username, String updatedValue, String valid) {
        try (Connection connection = dataBaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(valid)) {
            statement.setString(1, updatedValue);
            statement.setString(2, username);
            statement.executeUpdate();
            return updatedValue;
        } catch (SQLException e) {
            LOG.error("The user in the database could not be updated {}", e.getMessage());
        }
        return null;
    }

    @Override
    public boolean updateUserPassword(String username, String newPassword) {
        byte[][] passwordHashWithSalt = createHashWithSalt(newPassword);
        String valid = "Update " + tableName + " set hash = ?, salt = ? where name = ?";
        try (Connection connection = dataBaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(valid)) {
            statement.setBytes(1, passwordHashWithSalt[1]);
            statement.setBytes(2, passwordHashWithSalt[0]);
            statement.setString(3, username);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOG.error("The user in the database could not be updated");
        }
        return false;
    }

    @Override
    public void removeUser(String username) {
        if (Strings.isNullOrEmpty(username)) {
            throw new IllegalArgumentException("username must not be null");
        }
        String valid = "delete from " + tableName + " where name = ?";
        try (Connection connection = dataBaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(valid)) {
            statement.setString(1, username);
            statement.executeUpdate();
        } catch (SQLException e) {
            LOG.error("The user was not deleted from the database: {}", e.getMessage());
        }
    }

    @Override
    public List<User> getAllUsers() {
        List<User> retUsers = new ArrayList<>();
        String valid = "Select * from " + tableName;
        try (Connection connection = dataBaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(valid)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String userId = resultSet.getString(ID_COLUMN);
                String username = resultSet.getString(USERNAME_COLUMN);
                String email = resultSet.getString(EMAIL_ADDRESS_COLUMN);
                User user = UserDTO.createWithoutPassword(userId, username, email);
                retUsers.add(user);
            }
        } catch (SQLException e) {
            LOG.error("Connection failure: {}", e.getMessage());
        }
        return retUsers;
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public List<PlayerProfile> getAllPlayerStatistics() {
        List<PlayerProfile> list = new ArrayList<>();
        String valid = "SELECT name, games_won, games_lost FROM " + tableName;
        try (Connection connection = dataBaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(valid)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                var playerProfile = new PlayerProfile(resultSet.getString("name"), "", resultSet.getString("games_won"),
                        resultSet.getString("games_lost"));
                list.add(playerProfile);
            }
        } catch (SQLException e) {
            LOG.debug("Connection failure: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public PlayerProfile getPlayerInfoData(String username) {
        String valid = "Select name, email_address, games_won, games_lost FROM " + tableName + " where name = ?";
        try (Connection connection = dataBaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(valid)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new PlayerProfile(resultSet.getString("name"),
                        resultSet.getString("email_address"), resultSet.getString("games_won"),
                        resultSet.getString("games_lost"));
            }
        } catch (SQLException e) {
            LOG.debug("Connection failure: {}", e.getMessage());
        }
        return null;
    }

    /**
     * This method is used for tests to insert large batches
     * of users into the database
     */
    public void createUsers(List<? extends User> users) {
        String insertFields = "INSERT INTO " + tableName + " (name, email_address, hash, salt, games_won, games_lost)";
        String insertValues = "VALUES (?, ?, ?, ?, ?, ?)";
        String insertToRegister = insertFields + insertValues;
        try (Connection connection = dataBaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(insertToRegister,
                Statement.RETURN_GENERATED_KEYS)) {
            for (User user : users) {
                byte[][] passwordHashWithSalt = createHashWithSalt(user.getPlainPassword());
                statement.setString(1, user.getUsername());
                statement.setString(2, user.getEMail());
                statement.setBytes(3, passwordHashWithSalt[1]);
                statement.setBytes(4, passwordHashWithSalt[0]);
                statement.setShort(5, (short) 0);
                statement.setShort(6, (short) 0);
                statement.addBatch();
                statement.executeBatch();
            }
        } catch (SQLException e) {
            LOG.error("Could not insert new users into database: {}", e.getMessage());
        }
    }

    @Override
    public boolean updateGamesWon(String username) {
        final String valid = "Update " + tableName + " set games_won = games_won + 1 where name = ?";
        return isUpdated(username, valid);
    }

    @Override
    public boolean updateGamesLost(String username) {
        String valid = "Update " + tableName + " set games_lost = games_lost + 1 where name = ?";
        return isUpdated(username, valid);
    }

    /**
     * Won and Loss is updated
     * @param username of user
     * @param valid SQL query
     * @return true, if updated successfully.
     */
    private boolean isUpdated(String username, String valid) {
        try (Connection connection = dataBaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(valid)) {
            statement.setString(1, username);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOG.debug("Connection failure: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public int getNoOfGamesWon(String playerName) {
        String valid = "Select games_won FROM " + tableName + " where name = ?";
        return getIntegerEntry(playerName, valid);
    }

    @Override
    public int getNoOfGamesLost(String playerName) {
        String valid = "Select games_lost FROM " + tableName + " where name = ?";
        return getIntegerEntry(playerName, valid);
    }

    public int getIntegerEntry(String playerName, String valid) {
        try (Connection connection = dataBaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(valid)) {
            statement.setString(1, playerName);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                return resultSet.getInt(1);
            }
        }catch (SQLException e){
            LOG.debug("Connection failure: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * The TRUNCATE TABLE statement is used to
     * remove all records from a table in MySQL
     * This method is for test cases
     *
     * @see DataBaseConnection
     * @see PreparedStatement
     */
    public void clearTestDataBase() {
        String valid = "Truncate TABLE test";
        try (Connection connection = dataBaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(valid)) {
            statement.execute();
        } catch (SQLException e) {
            LOG.debug("Connection failure: {}", e.getMessage());
        }
    }

}

