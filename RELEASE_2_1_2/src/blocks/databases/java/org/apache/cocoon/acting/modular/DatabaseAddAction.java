/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/

package org.apache.cocoon.acting.modular;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.cocoon.components.modules.database.AutoIncrementModule;

/**
 * Adds record in a database. The action can update one or more
 * tables, and can add more than one row to a table at a time. See
 * {@link DatabaseAction} for details.
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: DatabaseAddAction.java,v 1.2 2003/07/01 11:23:19 haul Exp $
 */
public class DatabaseAddAction extends DatabaseAction {

    /**
     * set all necessary ?s and execute the query
     */
    protected int processRow ( Map objectModel, Connection conn, PreparedStatement statement, String outputMode,
                               Configuration table, CacheHelper queryData, Object[][] columnValues,
                               int rowIndex, Map results )
        throws SQLException, ConfigurationException, Exception {

        int currentIndex = 1;
        for (int i = 0; i < queryData.columns.length; i++) {
            Column col = queryData.columns[i];
            if ( col.isAutoIncrement && col.isKey ) {
                currentIndex += setKeyAuto( table, col, currentIndex, rowIndex,
                                            conn, statement, objectModel, outputMode, results );
            } else {
                this.setOutput( objectModel, outputMode, results, table, col.columnConf, rowIndex,
                                columnValues[ i ][ ( col.isSet ? rowIndex : 0 ) ]);
                this.setColumn( statement, currentIndex, col.columnConf, 
                                columnValues[ i ][ ( col.isSet ? rowIndex : 0 ) ]);
                currentIndex++;
            }
        }
        int rowCount = statement.executeUpdate();
        // get resulting ids for autoincrement columns
        for (int i = 0; i < queryData.columns.length; i++) {
            if ( queryData.columns[i].isAutoIncrement && queryData.columns[i].isKey ) {
                storeKeyValue( table, queryData.columns[i], rowIndex,
                               conn, statement, objectModel, outputMode, results );
            }
        }
        return rowCount;
    }


    /**
     * Sets the key value on the prepared statement for an autoincrement type.
     *
     * @param table the table's configuration object
     * @param column the key's configuration object
     * @param currentIndex the position of the key column
     * @param rowIndex the position in the current row set
     * @param conn the database connection
     * @param statement the insert statement
     * @param objectModel the objectModel object
     * @param outputMode name of the requested output module
     * @param results sitemap result object
     * @return the number of columns by which to increment the currentIndex
     */
    protected int setKeyAuto ( Configuration table, Column column, int currentIndex, int rowIndex,
                               Connection conn, PreparedStatement statement, Map objectModel, String outputMode, Map results )
        throws ConfigurationException, SQLException, ComponentException, Exception {

        int columnCount = 0;
        ComponentSelector autoincrSelector = null;
        AutoIncrementModule autoincr = null;
        try {
            autoincrSelector=(ComponentSelector) this.manager.lookup(DATABASE_MODULE_SELECTOR);
            if (column.mode != null && autoincrSelector != null && autoincrSelector.hasComponent(column.mode)){
                autoincr = (AutoIncrementModule) autoincrSelector.select(column.mode);
            }

            if ( autoincr.includeInQuery() ) {
                if ( autoincr.includeAsValue() ) {
                    Object value = autoincr.getPreValue( table, column.columnConf, column.modeConf, conn, objectModel );
                    this.setColumn(objectModel, outputMode, results, table, column.columnConf, rowIndex, value, statement, currentIndex);
                    columnCount = 1;
                }
            } else {
                if (getLogger().isDebugEnabled())
                    getLogger().debug( "Automatically setting key" );
            }

        } finally {
            if (autoincrSelector != null) {
                if (autoincr != null)
                    autoincrSelector.release(autoincr);
                this.manager.release(autoincrSelector);
            }
         }

        return columnCount;
    }



    /**
     * Put key values into request attributes. Checks whether the
     * value needs to be retrieved from the database module first.
     *
     */
    protected void storeKeyValue( Configuration tableConf, Column key, int rowIndex, Connection conn,
                                  Statement statement, Map objectModel, String outputMode, Map results )
        throws SQLException, ConfigurationException, ComponentException {

        ComponentSelector autoincrSelector = null;
        AutoIncrementModule autoincr = null;
        try {
            autoincrSelector=(ComponentSelector) this.manager.lookup(DATABASE_MODULE_SELECTOR);
            if (key.mode != null && autoincrSelector != null && autoincrSelector.hasComponent(key.mode)){
                autoincr = (AutoIncrementModule) autoincrSelector.select(key.mode);
            }

            if (!autoincr.includeAsValue()) {
                Object value = autoincr.getPostValue( tableConf, key.columnConf, key.modeConf, conn, statement, objectModel );
                this.setOutput(objectModel, outputMode, results, tableConf, key.columnConf, rowIndex, value);
            }

        } finally {
            if (autoincrSelector != null) {
                if (autoincr != null)
                    autoincrSelector.release(autoincr);
                this.manager.release(autoincrSelector);
            }
         }

    }


    /**
     * determine which mode to use as default mode
     * here: INSERT
     * highly specific to operation INSERT / UPDATE / DELETE / SELECT
     */
    protected String selectMode ( boolean isAutoIncrement, Map modes ) {

        if ( isAutoIncrement )
            return (String) modes.get( MODE_AUTOINCR );
        else
            return (String) modes.get( MODE_OTHERS );
    }


    /**
     * determine whether autoincrement columns should be honoured by
     * this operation. This is usually snsible only for INSERTs.
     */
    protected boolean honourAutoIncrement() { return true; }


    /**
     * Fetch all values for all columns that are needed to do the
     * database operation.
     */
    protected Object[][] getColumnValues( Configuration tableConf, CacheHelper queryData,
                                          Map objectModel )
        throws ConfigurationException, ComponentException {

        Object[][] columnValues = new Object[ queryData.columns.length ][];
        for ( int i = 0; i < queryData.columns.length; i++ ){
            columnValues[i] = this.getColumnValue( tableConf, queryData.columns[i], objectModel );
        }
        return columnValues;
    }


    /**
     * Get the String representation of the PreparedStatement.  This is
     * mapped to the Configuration object itself, so if it doesn't exist,
     * it will be created.
     * @param table the table's configuration object
     * @return the insert query as a string
     */
    protected CacheHelper getQuery( Configuration table, Map modeTypes, Map defaultModeNames )
        throws ConfigurationException, ComponentException {

        LookUpKey lookUpKey = new LookUpKey( table, modeTypes );
        CacheHelper queryData = null;
        synchronized( this.cachedQueryData ) {
            queryData = (CacheHelper) this.cachedQueryData.get( lookUpKey );

            if (queryData == null) {
                Configuration[] values = table.getChild("values").getChildren("value");
                Configuration[] keys = table.getChild("keys").getChildren("key");

                queryData = new CacheHelper( keys.length, keys.length + values.length );
                fillModes( keys,   true,  defaultModeNames, modeTypes, queryData );
                fillModes( values, false, defaultModeNames, modeTypes, queryData );

                StringBuffer queryBuffer = new StringBuffer("INSERT INTO ");
                StringBuffer valueBuffer = new StringBuffer(") VALUES (");

                queryBuffer.append(table.getAttribute("name"));
                queryBuffer.append(" (");
                int actualColumns = 0;

                for (int i = 0; i < queryData.columns.length; i++) {
                    if ( actualColumns > 0 ) {
                        queryBuffer.append( ", " );
                        valueBuffer.append( ", " );
                    }
                    if ( queryData.columns[i].isKey && queryData.columns[i].isAutoIncrement ) {

                        ComponentSelector autoincrSelector = null;
                        AutoIncrementModule autoincr = null;
                        try {
                            autoincrSelector=(ComponentSelector) this.manager.lookup(DATABASE_MODULE_SELECTOR); 
                            if (queryData.columns[i].mode != null && 
                                autoincrSelector != null && 
                                autoincrSelector.hasComponent(queryData.columns[i].mode)){
                                autoincr = (AutoIncrementModule) autoincrSelector.select(queryData.columns[i].mode);
                                
                                if ( autoincr.includeInQuery() ) {
                                    actualColumns++;
                                    queryBuffer.append( queryData.columns[i].columnConf.getAttribute( "name" ) );
                                    if ( autoincr.includeAsValue() ) {
                                        valueBuffer.append( "?" );
                                    } else {
                                        valueBuffer.append(
                                                           autoincr.getSubquery( table, queryData.columns[i].columnConf,
                                                                                 queryData.columns[i].modeConf ) );
                                    }
                                }
                            } else {
                                if (getLogger().isErrorEnabled())
                                    getLogger().error("Could not find mode description " 
                                                      + queryData.columns[i].mode + " for column #"+i);
                                if (getLogger().isDebugEnabled()) {
                                    getLogger().debug("Column data "+queryData.columns[i]);
                                }
                                throw new ConfigurationException("Could not find mode description "+queryData.columns[i].mode+" for column "+i);
                            }
                            
                        } finally {
                            if (autoincrSelector != null) {
                                if (autoincr != null) 
                                    autoincrSelector.release(autoincr);
                                this.manager.release(autoincrSelector);
                            }
                        }

                    } else {
                        actualColumns++;
                        queryBuffer.append( queryData.columns[i].columnConf.getAttribute( "name" ) );
                        valueBuffer.append( "?" );
                    }
                }

                valueBuffer.append(")");
                queryBuffer.append(valueBuffer);

                queryData.queryString = queryBuffer.toString();

                this.cachedQueryData.put( lookUpKey, queryData );
            }
        }

        return queryData;
    }


}
