/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.client.datamgr;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.datamgr.AssetManager.ChecksumSha1;
import org.jdesktop.wonderland.common.config.WonderlandConfigUtil;
import org.jdesktop.wonderland.common.AssetType;

/**
 *
 * Adapted from John O'Conner's AddressBook tutorial
 * http://java.sun.com/developer/technicalArticles/J2SE/Desktop/javadb/
 */
public class AssetDB {
    
    private static final int DB_VERSION = 1;

    private Logger logger = Logger.getLogger("org.jdesktop.lg3d.wonderland.scenemanager.AssetDB");
    
    public AssetDB() {
        this.dbName = "AssetDB";
        
        setDBSystemDir();
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "jdbc EmbeddedDriver not available, exiting.", ex);
            ex.printStackTrace();
            System.exit(1);
        }
        
        dbProperties = new Properties();

        dbProperties.put("user", "assetmgr");
        dbProperties.put("password", "wonderland");    
        dbProperties.put("derby.driver", "org.apache.derby.jdbc.EmbeddedDriver");
        dbProperties.put("derby.url", "jdbc:derby:");
        dbProperties.put("db.table", "ASSET");
        dbProperties.put("db.schema", "APP");        
        if(!dbExists()) {
            try {
                logger.fine("AssetDB does not exist, creating it at location "+getDatabaseLocation());
                createDatabase();
            } catch(Exception e) {
                e.printStackTrace();
            } catch(Error er) {
                er.printStackTrace();
            }
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
    
    private boolean dbExists() {
        boolean bExists = false;
        String dbLocation = getDatabaseLocation();
        File dbFileDir = new File(dbLocation);
        if (dbFileDir.exists()) {
            bExists = true;
        }
        
//        File dbVersionFile = new File(dbLocation+File.separator+"db_version");
//        if (!dbVersionFile.exists()) {
//            bExists = false;
//            dbFileDir.d
//        } else {
//            try {
//                InputStreamReader reader = new InputStreamReader(new FileInputStream(dbVersionFile));
//                char[] c = new char[5];
//                reader.read(c);
//                System.out.println("Read - "+new String(c));
//            } catch (FileNotFoundException ex) {
//                ex.printStackTrace();
//                
//            } catch(IOException ex) {
//                ex.printStackTrace();
//            }
            
//        }
        
        return bExists;
    }
    
    private void setDBSystemDir() {
        String systemDir = WonderlandConfigUtil.getWonderlandDir()+File.separator;
        System.setProperty("derby.system.home", systemDir);
        
        logger.fine("AssetDB SystemDir = "+systemDir);
        // create the db system directory
        File fileSystemDir = new File(systemDir);
        fileSystemDir.mkdir();
    }
    
    private boolean createTables(Connection dbConnection) {
        boolean bCreatedTables = false;
        Statement statement = null;
        try {
            statement = dbConnection.createStatement();
            statement.execute(strCreateAssetTable);
            bCreatedTables = true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return bCreatedTables;
    }
    private boolean createDatabase() {
        boolean bCreated = false;
        Connection tmpConnection = null;
        
        String dbUrl = getDatabaseUrl();
        dbProperties.put("create", "true");
        
        try {
            dbProperties.list(System.out);
            tmpConnection = DriverManager.getConnection(dbUrl, dbProperties);
            bCreated = createTables(tmpConnection);
            
//            File dbVersionFile = new File(System.getProperty("derby.system.home")+File.separator+"db_version");
//                try {
//                    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(dbVersionFile));
//                    char[] c = new char[5];
//                    writer.write(Integer.toString(DB_VERSION));
//                    writer.close();
//                } catch (FileNotFoundException ex) {
//                    ex.printStackTrace();
//
//                } catch(IOException ex) {
//                    ex.printStackTrace();
//                }

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
        return bCreated;
    }
    
    
    public boolean connect() {
        String dbUrl = getDatabaseUrl();
        try {
            dbConnection = DriverManager.getConnection(dbUrl, dbProperties);
            stmtSaveNewRecord = dbConnection.prepareStatement(strSaveAsset);
            stmtUpdateExistingRecord = dbConnection.prepareStatement(strUpdateAsset);
            stmtGetAsset = dbConnection.prepareStatement(strGetAsset);
            stmtDeleteAsset = dbConnection.prepareStatement(strDeleteAsset);
            
            isConnected = dbConnection != null;
        } catch (SQLException ex) {
            isConnected = false;
            ex.printStackTrace();
        }
        return isConnected;
    }
    
//    private String getHomeDir() {
//        return System.getProperty("user.home");
//    }
    
    public void disconnect() {
        if(isConnected) {
            String dbUrl = getDatabaseUrl();
            dbProperties.put("shutdown", "true");
            try {
                DriverManager.getConnection(dbUrl, dbProperties);
            } catch (SQLException ex) {
                logger.log(Level.WARNING, "Failed to disconnect from AssetDB "+ex.getMessage(), ex);
            }
            isConnected = false;
        }
    }
    
    public String getDatabaseLocation() {
        String dbLocation = System.getProperty("derby.system.home") + "/" + dbName;
        return dbLocation;
    }
    
    public String getDatabaseUrl() {
        String dbUrl = dbProperties.getProperty("derby.url") + dbName;
        return dbUrl;
    }
    
    
    private void saveRecord(String filename, String baseURL, String checksum, AssetType assetType) {
        synchronized(stmtSaveNewRecord) {
            try {
                stmtSaveNewRecord.clearParameters();

                stmtSaveNewRecord.setString(1, filename);
                stmtSaveNewRecord.setString(2, baseURL);
                stmtSaveNewRecord.setString(3, checksum);
                stmtSaveNewRecord.setString(4, assetType.toString());

    //            try {
    //                String b = "Big test "+filename;
    //                ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
    //                ObjectOutputStream objOut = new ObjectOutputStream(outBuf);
    //                objOut.writeObject(b);
    //                objOut.close();
    //                ByteArrayInputStream in = new ByteArrayInputStream(outBuf.toByteArray());
    //                stmtSaveNewRecord.setBlob(5, in);
    //            } catch(IOException ioe) {
    //                ioe.printStackTrace();
    //            }

                int rowCount = stmtSaveNewRecord.executeUpdate();

                // Using auto commit so we are done.

            } catch(SQLException sqle) {
                logger.log(Level.SEVERE, "SQL Error saveing record for "+filename, sqle);
                sqle.printStackTrace();
                System.exit(1);
            }
        }
    }
    
    /**
     * Add asset to database
     * @param asset
     */
    public void addAsset(Asset asset) {
        saveRecord(asset.getFilename(), 
                asset.getRepository().getOriginalRepository().toExternalForm(), 
                asset.getLocalChecksum().toString(),
                asset.getType());
    }
    
    private boolean updateRecord(String filename, String baseUrl, String checksum, AssetType assetType) {
        boolean bEdited = false;
        synchronized(stmtUpdateExistingRecord) {
            try {
                stmtUpdateExistingRecord.clearParameters();

                stmtUpdateExistingRecord.setString(1, filename);
                stmtUpdateExistingRecord.setString(2, baseUrl);
                stmtUpdateExistingRecord.setString(3, checksum);
                stmtUpdateExistingRecord.setString(4, assetType.toString());
                stmtUpdateExistingRecord.setString(5, filename);
                stmtUpdateExistingRecord.executeUpdate();
                bEdited = true;
            } catch(SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        return bEdited;        
    }
    
//    public boolean editRecord(AssetRecord record) {
//        boolean bEdited = false;
//        synchronized(stmtUpdateExistingRecord) {
//            try {
//                stmtUpdateExistingRecord.clearParameters();
//
//                stmtUpdateExistingRecord.setString(1, record.getFilename());
//                stmtUpdateExistingRecord.setString(2, record.getBaseURL());
//                stmtUpdateExistingRecord.setString(3, record.getChecksum());
//                stmtUpdateExistingRecord.setString(4, record.getAssetType().toString());
//                stmtUpdateExistingRecord.executeUpdate();
//                bEdited = true;
//            } catch(SQLException sqle) {
//                sqle.printStackTrace();
//            }
//        }
//        return bEdited;
//        
//    }
    
    public boolean deleteAsset(String assetFilename) {
        boolean bDeleted = false;
        synchronized(stmtDeleteAsset) {
            try {
                stmtDeleteAsset.clearParameters();
                stmtDeleteAsset.setString(1, assetFilename);
                stmtDeleteAsset.executeUpdate();
                bDeleted = true;
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        
        return bDeleted;
    }
    
//    public boolean deleteRecord(Address record) {
//        int id = record.getId();
//        return deleteRecord(id);
//    }
//    
//    public List<ListEntry> getListEntries() {
//        List<ListEntry> listEntries = new ArrayList<ListEntry>();
//        Statement queryStatement = null;
//        ResultSet results = null;
//        
//        try {
//            queryStatement = dbConnection.createStatement();
//            results = queryStatement.executeQuery(strGetListEntries);
//            while(results.next()) {
//                int id = results.getInt(1);
//                String lName = results.getString(2);
//                String fName = results.getString(3);
//                String mName = results.getString(4);
//                
//                ListEntry entry = new ListEntry(lName, fName, mName, id);
//                listEntries.add(entry);
//            }
//            
//        } catch (SQLException sqle) {
//            sqle.printStackTrace();
//            
//        }
//        
//        return listEntries;
//    }
    
    /**
     * Return the asset record for the supplied file, or null if the file
     * is not in the cache
     * 
     * @param assetFilename
     * @return
     */
    Asset getAsset(String assetFilename) {
        Asset asset = null;
        synchronized(stmtGetAsset) {
            try {
                stmtGetAsset.clearParameters();
                stmtGetAsset.setString(1, assetFilename);
                ResultSet result = stmtGetAsset.executeQuery();
                if (result.next()) {
                    String filename = result.getString("FILENAME");
                    String baseURL = result.getString("BASE_URL");
                    String checksum = result.getString("CHECKSUM");
                    AssetType assetType = AssetType.valueOf(result.getString("TYPE"));

//                    Blob blob = result.getBlob("BOUNDS");
//                    ObjectInputStream objIn;
//                    try {
//                        objIn = new ObjectInputStream(blob.getBinaryStream());
//                        Object o = objIn.readObject();
//                        objIn.close();
//
//    //                    System.out.println("READ "+o);
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    } catch (SQLException ex) {
//                        ex.printStackTrace();
//                    } catch(ClassNotFoundException ex) {
//                        ex.printStackTrace();
//                    }

    //                System.out.println("Got Asset "+assetFilename+"  cs "+checksum);

                    
                    asset = AssetManager.getAssetManager().assetFactory(assetType, new Repository(new URL(baseURL)), 
                            filename);
                    asset.setLocalChecksum(new ChecksumSha1(fromHexString(checksum))); 
                }
            } catch(SQLException sqle) {
                sqle.printStackTrace();
            } catch(MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return asset;
    }
    
    /**
     * Convert a byte[] to a Hex String
     * @param bytes
     * @return
     */
    static String toHexString(byte bytes[]) {
        StringBuffer retString = new StringBuffer();
        for (int i = 0; i < bytes.length; ++i) {
            retString.append(
                    Integer.toHexString(0x0100 + (bytes[i] & 0x00FF)).substring(1));
        }
        return retString.toString();
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
            while(result.next()) {
                System.out.print(result.getString("FILENAME")+"\t\t");
                System.out.print(result.getString("BASE_URL")+"\t\t");
                System.out.print(result.getString("TYPE"));
                System.out.println();
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        AssetDB db = new AssetDB();
        System.out.println(db.getDatabaseLocation());
        System.out.println(db.getDatabaseUrl());
        db.listAssets();
        db.disconnect();
    }
    
    
    private Connection dbConnection;
    private Properties dbProperties;
    private boolean isConnected;
    private String dbName;
    private PreparedStatement stmtSaveNewRecord;
    private PreparedStatement stmtUpdateExistingRecord;
    private PreparedStatement stmtGetListEntries;
    private PreparedStatement stmtGetAsset;
    private PreparedStatement stmtDeleteAsset;
    
    private static final String strCreateAssetTable =
            "create table APP.ASSET (" +
            "    FILENAME    VARCHAR(120) not null primary key, " +
            "    BASE_URL    VARCHAR(120), " +
            "    CHECKSUM    VARCHAR(40), " +
            "    TYPE        VARCHAR(10) " +
            ")";
//    private static final String strCreateAssetTable =
//            "create table APP.ASSET (" +
//            "    FILENAME    VARCHAR(120) not null primary key, " +
//            "    BASE_URL    VARCHAR(120), " +
//            "    CHECKSUM    VARCHAR(40), " +
//            "    TYPE        VARCHAR(10), " +
//            "    BOUNDS      BLOB "+
//            ")";
    
    private static final String strGetAsset =
            "SELECT * FROM APP.ASSET " +
            "WHERE FILENAME = ?";
    
    private static final String strSaveAsset =
            "INSERT INTO APP.ASSET " +
            "   (FILENAME, BASE_URL, CHECKSUM, TYPE)" +
            "VALUES (?, ?, ?, ?)";
//    private static final String strSaveAsset =
//            "INSERT INTO APP.ASSET " +
//            "   (FILENAME, BASE_URL, CHECKSUM, TYPE, BOUNDS)" +
//            "VALUES (?, ?, ?, ?, ?)";
    
    
    private static final String strGetListEntries =
            "SELECT FILENAME, BASE_URL, CHECKSUM, TYPE FROM APP.ASSET "  +
            "ORDER BY FILENAME ASC";
    
    private static final String strUpdateAsset =
            "UPDATE APP.ASSET " +
            "SET FILENAME = ?, " +
            "    BASE_URL = ?, " +
            "    CHECKSUM = ?, " +
            "    TYPE = ? " +
            "WHERE FILENAME = ?";
    
    private static final String strDeleteAsset =
            "DELETE FROM APP.ASSET " +
            "WHERE FILENAME = ?";
    
    
}
