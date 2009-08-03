/**
 * Project Wonderland
 *
 * $RCSfile: AssetDB.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.15 $
 * $Date: 2007/08/07 17:01:12 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client.datamgr;


import java.io.File;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.config.WonderlandConfigUtil;
import org.jdesktop.wonderland.common.AssetType;
import org.jdesktop.wonderland.common.AssetURI;
import org.jdesktop.wonderland.common.ChecksumSha1;

/**
 * The AssetDB class represents the client-side cache of assets. The database
 * itself simply stores the entries found in the cache; the cached assets are
 * actually stored in a corresponding directorly.
 * <p>
 * Each entry in the database is uniquely identified by a variable-length string
 * that includes the repository from which it came and the relative path of the
 * resource within the repository.
 * <p>
 * The resource may be specified in one of several ways, which dictate how it is
 * uniquely identified in the asset database. Typically resources are specified
 * within the configuration of a cell in WFS.
 * <p>
 * (1) An absolute path of the resource is specified within the cell configuration.
 * Here, that resource, and only that resource is used. This is used very rarely.
 * In this case, the full URL uniquely identifies the resource.
 * <p>
 * (2) A relative path of the resource is specified with respect to the default
 * repository for the instance of Wonderland. This case is the legacy case from
 * v0.4 and earlier versions of Wonderland. The resource is uniquely identified
 * by the relative path.
 * <p>
 * (3) A relative path of the resource within some module is specified. This
 * mechanism is new to v0.5 Wonderland and later. The resource is uniquely
 * identified by the relative path and the unique name of the module.
 * <p>
 * The RESOURCE_PATH field gives the unique identification of the resource and
 * takes the form of a URL:
 * <p>
 * Case 1: http://<repository>/<relative path>
 * Case 2: wlr://<relative path>
 * Case 3: wlm://<module name>/<relative path>
 * <p>
 * The FILENAME field gives only the file name. In case (1) and (3), it is the
 * value of <relative path>. In case (2), it is also <relative path>. Note that
 * in case (1) <relative path> includes everything after the machine domain
 * name.
 * <p>
 * The BASE_URL field gives the base URL from which the resource case. In case(1),
 * it is <repository>, case (2) its the URL of the repository currently in use,
 * and case (3), its the URL from which the resource was actually fetched, as
 * defined by the list of available repositories in the model.
 * <p>
 * <p>
 * <h3>Database Version<h3>
 *
 * Each database has a version. To allow multiple database caches with different
 * versions to exist on the same machine, the location of the asset database
 * includes the version. That is, the location of the database is: derby.system.home +
 * version + database name.
 * 
 * RESOURCE_URI: The abstract URI describing the asset
 * URL: The URL from which the asset was fetched.
 * 
 * @author paulby
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class AssetDB {
    
    /*
     * Version history for the asset database:
     * Version 1: Wonderland v0.3 - v0.4
     * Version 2: Wonderland v0.5
     */
    private static final int DB_VERSION = 2;

    /* The default name of the asset database */
    private static final String DB_NAME = "AssetDB";
    
    /* The maximum length of strings in the database */
    private static final int MAX_STRING_LENGTH = 8192;
    
    /* The error logger for this class */
    private static Logger logger = Logger.getLogger(AssetDB.class.getName());
   
    /* The database connection, null if not connected */
    private Connection dbConnection = null;
    
    /* The list of properties that represent the database configuration */
    private Properties dbProperties = null;
    
    /* True if the database is connected, false if not */
    private boolean isConnected = false;
    
    /* The name of the database, initially DB_NAME */
    private String dbName = null;
    
    /**
     * Default constructor
     */
    public AssetDB() {
        /* Initialize the name to some default name */
        this.dbName = AssetDB.DB_NAME;

        /* Log a message saying we are kicking off the database */
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        logger.addHandler(ch);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        logger.fine("AssetDB: Initializing the asset database, name=" + this.dbName);
                
        /*
         * Attempt the set the base directory of the database, which could fail.
         * Log an error if it does and exit. XXX Do we really need to exit? XXX
         */
        if (this.setDBSystemDir() == false) {
            logger.severe("AssetDB: Unable to set database directory");
            System.exit(1);
        }
        
        /*
         * Attempt to open the database, exit with severe error. XXX Do we
         * really need to exit? Perhaps we can just continue without using a
         * cache in fail-safe mode? XXX
         */
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "AssetDB: jdbc EmbeddedDriver not available, exiting.", ex);
            ex.printStackTrace();
            System.exit(1);
        }
        
        /* Create the properties that describe the database */
        dbProperties = new Properties();
        dbProperties.put("user", "assetmgr");
        dbProperties.put("password", "wonderland");    
        dbProperties.put("derby.driver", "org.apache.derby.jdbc.EmbeddedDriver");
        dbProperties.put("derby.url", "jdbc:derby:");
        dbProperties.put("db.table", "ASSET");
        dbProperties.put("db.schema", "APP");   
        
        /*
         * Check to see if the database exists. If it does exist, attempt to
         * connect to it. If it does not exist, then try to create it first. This
         * also handles if a database exists, but is for a previous version.
         */
        if(!dbExists()) {
            try {
                logger.fine("AssetDB does not exist, creating it at location "+getDatabaseLocation());
                createDatabase();
            } catch(Exception e) {
                e.printStackTrace();
            } catch(Error er) {
                er.printStackTrace();
            }
            
            /* Disconnect from the database after creation and attempt to re-connect */
            disconnect();
            if (!connect()) {
                System.out.println("Unable to open AssetDB, exiting");
                System.out.println("Check you don't have a Wonderland client already running");
                System.exit(1);
            }
        } else {            
            if (!connect()) {
                System.out.println("Unable to open AssetDB, exiting");
                System.out.println("Check you don't have a Wonderland client already running");
                System.exit(1);
            }
        }
    }
    
    /**
     * Returns the version of the database currently in use.
     * 
     * @return The current database version
     */
    public int getVersion() {
        return AssetDB.DB_VERSION;
    }
        
    /**
     * Returns the location of the database. This location is the full path name.
     * 
     * @return The full location to the database
     */
    public String getDatabaseLocation() {
        String dbLocation = System.getProperty("derby.system.home") + "/" + dbName;
        return dbLocation;
    }
    
    /**
     * Returns the URL representation of the database
     * 
     * @return The URL representation of the database
     */
    public String getDatabaseUrl() {
        String dbUrl = dbProperties.getProperty("derby.url") + dbName;
        return dbUrl;
    }
    
    /**
     * Returns true if the database is connected, false if not.
     * 
     * @return True if the database is connected, false if not.
     */
    public boolean isConnected() {
        return this.isConnected;
    }

    /**
     * Connect to the database. Return true upon success, false upon falure
     * 
     * @return True upon success, false upon failure.
     */
    public boolean connect() {
        /*
         * Attempt to connect to the database, also compile some SQL statements
         * that we'll use. Set the isConnected flag upon result.
         */
        try {
            dbConnection = DriverManager.getConnection(this.getDatabaseUrl(), dbProperties);
            stmtSaveNewRecord = dbConnection.prepareStatement(strSaveAsset);
            stmtUpdateExistingRecord = dbConnection.prepareStatement(strUpdateAsset);
            stmtGetAsset = dbConnection.prepareStatement(strGetAsset);
            stmtDeleteAsset = dbConnection.prepareStatement(strDeleteAsset);
            
            this.isConnected = dbConnection != null;
        } catch (SQLException ex) {
            isConnected = false;
            dbConnection = null;
            ex.printStackTrace();
        }
        
        logger.fine("AssetDB: Done attempting to connect, ret=" + this.isConnected);
        return isConnected;
    }
    
    /**
     * Disconnects from the database.
     */
    public void disconnect() {
        if(isConnected) {
            String dbUrl = getDatabaseUrl();
            dbProperties.put("shutdown", "true");
            try {
                DriverManager.getConnection(dbUrl, dbProperties);
            } catch (SQLException ex) {
                /*
                 * When shutting down the database, an ERROR 08006 is normal and
                 * indicates that the database has indeed been shutdown. Check
                 * for that here
                 */
                if (ex.getSQLState().equals("08006") == true) {
                    logger.log(Level.INFO, "AssetDB: Shutdown was normal");
                }
                else {
                    logger.log(Level.WARNING, "Failed to disconnect from AssetDB " + ex.getMessage(), ex);
                }
            }
            isConnected = false;
            dbConnection = null;
        }
    }
    
    /**
     * Adds a new asset to database. Returns true upon success, false upon
     * failure. If the asset already exists, this method logs an exception and
     * returns false.
     * 
     * @param asset The asset to add to the database
     * @return True if the asset was added successfully, false if not
     */
    public boolean addAsset(Asset asset) {
        boolean isSaved = false;
        synchronized(stmtSaveNewRecord) {
            try {
                logger.fine("AssetDB: Saving asset to database, uri=" + asset.getAssetURI().toString());
                stmtSaveNewRecord.clearParameters();
                stmtSaveNewRecord.setString(1, asset.getAssetURI().toString());
                stmtSaveNewRecord.setString(2, asset.getURL());
                stmtSaveNewRecord.setString(3, asset.getLocalChecksum().toString());
                stmtSaveNewRecord.setString(4, asset.getType().toString());
                int row = stmtSaveNewRecord.executeUpdate();
                logger.fine("AssetDB: Saving asset, row=" + row);
                isSaved = true;            
            } catch (java.sql.SQLException sqle) {
                logger.log(Level.SEVERE, "AssetDB: SQL Error saving record for " + asset.getAssetURI().toString());
                sqle.printStackTrace();
            }
        }
        return isSaved;
    }
    
    /**
     * Returns true if the asset database already exist. The asset database is
     * considered to exist, if the proper version of the database exists. If
     * not, a new one is created.
     * <p>
     * The version of the database is encoded in the path of the database. In
     * this way, multiple database versions may exist on a system at once.
     * 
     * @return True if the database exists, false if not
     */
    private boolean dbExists() {
        return new File(this.getDatabaseLocation()).exists();
    }
    
    /**
     * Sets up the directory in which the database resides, creating the directory
     * if it does not exist. Returns true if successfull, false if not. Also
     * checks to see that we are able to do this operation, logs an error if not
     * and returns false.
     */
    private boolean setDBSystemDir() {
        try {
            /*
             * Fetch the database home configuration property. The database home
             * is the value of the configuration directory plus an encoding for
             * the version number.
             */
            String systemDir = WonderlandConfigUtil.getWonderlandDir() +
                    File.separator + "v" + this.getVersion();
            System.setProperty("derby.system.home", systemDir);

            /* Log a message with this directory */
            logger.fine("AssetDB: Database home directory=" + systemDir);
            
            /*
             * Create the directories. Note: an odd thing happens here, if the
             * directories already exist, then mkdirs() return false. So we
             * should not rely upon the return value of mkdirs()
             */
            File fileSystemDir = new File(systemDir);
            fileSystemDir.mkdirs();
            return true;
        } catch (java.lang.SecurityException excp) {
            /* Log an error and return null */
            logger.severe("AssetDB: Not allowed to setup database: " + excp.toString());
            return false;
        }
    }
    
    /**
     * Create the tables in the database, takes an open connection to the database.
     * Returns true upon success, false upon failure.
     * 
     * @param dbConnection The open connection to the database
     * @return True upon success, false upon failure.
     */
    private boolean createTables(Connection dbConnection) {
        try {
            Statement statement = dbConnection.createStatement();
            statement.execute(strCreateAssetTable);
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * Creates the database. Returns true if the database was successfully
     * created, false if not
     * 
     * @return True if the database was successfully created, false if not
     */
    private boolean createDatabase() {
        boolean bCreated = false;

        /*
         * Create the database. Upon exception, print out a message to the log
         * and return false. Otherwise return true.
         */
        dbProperties.put("create", "true");        
        try {
            dbProperties.list(System.out);
            Connection tmpConnection = DriverManager.getConnection(this.getDatabaseUrl(), dbProperties);
            bCreated = createTables(tmpConnection);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Failed to create database "+ex.getMessage(), ex);
            ex.printStackTrace();
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Failed to create database "+e.getMessage(), e);
            e.printStackTrace();
        } catch(Error er) {
            logger.log(Level.SEVERE, "Failed to create database "+er.getMessage(), er);
            er.printStackTrace();
            
        }
        dbProperties.remove("create");
        logger.fine("AssetDB: Created new database at " + this.getDatabaseLocation());
        return bCreated;
    }
    
    /**
     * Updates an existing asset on the database with new information. If the
     * asset does not exist, this method logs an exception and returns false.
     * 
     * @param asset The asset to update
     * @return True upon success, false upon failure
     */
    public boolean updateAsset(Asset asset) {
        boolean bEdited = false;
        synchronized(stmtUpdateExistingRecord) {
            try {
                stmtUpdateExistingRecord.clearParameters();

                stmtUpdateExistingRecord.setString(1, asset.getAssetURI().toString());
                stmtUpdateExistingRecord.setString(2, asset.getURL());
                stmtUpdateExistingRecord.setString(3, asset.getLocalChecksum().toString());
                stmtUpdateExistingRecord.setString(4, asset.getType().toString());
                stmtUpdateExistingRecord.setString(5, asset.getAssetURI().toString());
                stmtUpdateExistingRecord.executeUpdate();
                bEdited = true;
            } catch(SQLException sqle) {
                logger.log(Level.SEVERE, "AssetDB: SQL Error updating record for " + asset.getAssetURI().toString());
                sqle.printStackTrace();
            }
        }
        return bEdited;
    }
    
    /**
     * Removes an asset given its unique identifying URI. Returns true if the
     * asset was successfully removed, false if not.
     * 
     * @param assetURI The unique asset URI
     * @return True upon success, false upon failure
     */
    public boolean deleteAsset(String assetURI) {
        boolean bDeleted = false;
        synchronized(stmtDeleteAsset) {
            try {
                stmtDeleteAsset.clearParameters();
                stmtDeleteAsset.setString(1, assetURI);
                stmtDeleteAsset.executeUpdate();
                bDeleted = true;
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        
        return bDeleted;
    }
    
    /**
     * Return the asset record for the supplied unique asset URI, or null if
     * the asset described by the URI is not in the cache
     * 
     * @param assetURI The unique URI describing the asset
     * @return The asset record in the cache, null if not present.
     */
    public Asset getAsset(String assetFilename) {
        Asset asset = null;
        synchronized(stmtGetAsset) {
            try {
                // XXX Should we check for return status from query? XXX
                stmtGetAsset.clearParameters();
                stmtGetAsset.setString(1, assetFilename);
                ResultSet result = stmtGetAsset.executeQuery();
                if (result.next()) {
                    /* Fetch the information from the database */
                    String uri = result.getString("ASSET_URI");
                    String url = result.getString("URL");
                    String checksum = result.getString("CHECKSUM");
                    AssetType assetType = AssetType.valueOf(result.getString("TYPE"));
           
                    /*
                     * Create an AssetURI class, log and error and return null
                     * if its syntax is invalid.
                     */
                    try {
                        AssetURI assetURI = new AssetURI(uri);
                        asset = AssetManager.getAssetManager().assetFactory(assetType, assetURI);
                        asset.setURL(url);
                        asset.setLocalChecksum(new ChecksumSha1(fromHexString(checksum)));
                    } catch (java.net.URISyntaxException excp) {
                        // Log an error XXX
                    }
                }
            } catch(SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        return asset;
    }


    /**
     * Convert a hex string to a byte array. Each hex value must be 2 digits.
     * @param str
     * @return
     */
    static byte[] fromHexString(String str) {
        byte[] ret = new byte[str.length()/2];
        for(int i=0; i<str.length(); i+=2) {
            ret[i/2] = (byte) Integer.parseInt(str.substring(i,i+2), 16);
        }
        
        return ret;
    }
    
    public void listAssets() {
        try {
            Statement queryStatement = dbConnection.createStatement();
            ResultSet result = queryStatement.executeQuery(strGetListEntries);
            logger.fine("AssetDB: listing assets in database");
            while(result.next()) {
                System.out.print(result.getString("ASSET_URI") + "\t\t");
                System.out.print(result.getString("URL")+"\t\t");
                System.out.print(result.getString("TYPE"));
                System.out.println();
            }
            logger.fine("AssetDB: Done listing assets in database");
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
    }
    
    /**
     * Main method that has a simple command-line interface to test the database.
     * The usage is: java AssetDB [COMMAND] [ARGS], where COMMAND can be:
     * <p>
     * LIST: Lists all of the entries in the database
     * ADD: Add an entry to the database, followed by the required data fields
     */
    public static void main(String[] args) {
        /* Create the database and open a connection */
        AssetDB db = new AssetDB();
        
        /* Print out the essential information */
        logger.fine("AssetDB: Database Location: " + db.getDatabaseLocation());
        logger.fine("AssetDB: Database URL:      " + db.getDatabaseUrl());
        logger.fine("AssetDB: Is Connected?      " + db.isConnected());

        /* Check to see if there are enough arguments 
        if (args.length < 2) {
            logger.fine("AssetDB: So, what do you want to do?");
            System.exit(0);
        }
        
        String cmd = args[1];
        if (cmd.compareTo("LIST") == 0) {
            db.listAssets();
        }
        else if (cmd.compareTo("ADD") == 0) {
            String assetURI = args[2];
            String url = args[3];
            String checksum = args[4];
            String type = args[5];
            
            
        }*/
        
        db.listAssets();
        
        /* Disconnect from the database and exit */
        db.disconnect();
    }
   
    /* The various SQL statements to operate on the database */
    private PreparedStatement stmtSaveNewRecord;
    private PreparedStatement stmtUpdateExistingRecord;
    private PreparedStatement stmtGetListEntries;
    private PreparedStatement stmtGetAsset;
    private PreparedStatement stmtDeleteAsset;
   
    /* Creates the tables in the database */
    private static final String strCreateAssetTable =
            "create table APP.ASSET (" +
            "    ASSET_URI      VARCHAR(" + AssetDB.MAX_STRING_LENGTH + ") not null primary key, " +
            "    URL            VARCHAR(" + AssetDB.MAX_STRING_LENGTH + "), " +
            "    CHECKSUM       VARCHAR(40), " +
            "    TYPE           VARCHAR(10) " +
            ")";
    
    /* Get an asset based upon the unique resource path name */
    private static final String strGetAsset =
            "SELECT * FROM APP.ASSET WHERE ASSET_URI = ?";
    
    /* Save an asset given all of its values */
    private static final String strSaveAsset =
            "INSERT INTO APP.ASSET " +
            "   (ASSET_URI, URL, CHECKSUM, TYPE)" +
            "VALUES (?, ?, ?, ?)";
    
    /* Return all of the entries based upon the unique resource path key */
    private static final String strGetListEntries =
            "SELECT ASSET_URI, URL, CHECKSUM, TYPE " +
            "FROM APP.ASSET ORDER BY ASSET_URI ASC";
    
    /* Updates an entry using its resource path and values */
    private static final String strUpdateAsset =
            "UPDATE APP.ASSET " +
            "SET ASSET_URI = ?, " +
            "    URL = ?, " +
            "    CHECKSUM = ?, " +
            "    TYPE = ? " +
            "WHERE ASSET_URI = ?";
    
    /* Deletes an entry using its unique resource path */
    private static final String strDeleteAsset =
            "DELETE FROM APP.ASSET WHERE ASSET_URI = ?";
}