/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.thread;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.easymock.MockControl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;


/**
 * The $classType$ class ...
 *
 * @author <a href="mailto:giacomo.at.apache.org">Giacomo Pati </a>
 * @version $Id$
 */
public class DefaultRunnableManagerTestCase
    extends TestCase
{
    //~ Instance fields --------------------------------------------------------

    /** DOCUMENT ME! */
    private List m_controls;

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor for DefaultRunnableManagerTestCase.
     *
     * @param name
     */
    public DefaultRunnableManagerTestCase( String name )
    {
        super( name );
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final void testConfigureDaemonPool(  )
        throws Exception
    {
        final MockControl threadPoolConfigControl =
            createStrictControl( Configuration.class );
        final Configuration threadPoolConfig =
            (Configuration)threadPoolConfigControl.getMock(  );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "name" ),
                                                 createValueConfigMock( "daemon" ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "queue-size" ),
                                                 createIntegerConfigMock( 2 * DefaultRunnableManager.DEFAULT_QUEUE_SIZE,
                                                                          DefaultRunnableManager.DEFAULT_QUEUE_SIZE ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "max-pool-size" ),
                                                 createIntegerConfigMock( 2 * DefaultRunnableManager.DEFAULT_MAX_POOL_SIZE,
                                                                          DefaultRunnableManager.DEFAULT_MAX_POOL_SIZE ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "min-pool-size" ),
                                                 createIntegerConfigMock( DefaultRunnableManager.DEFAULT_MIN_POOL_SIZE / 3,
                                                                          DefaultRunnableManager.DEFAULT_MIN_POOL_SIZE ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "priority" ),
                                                 createValueConfigMock( "LOW",
                                                                        DefaultRunnableManager.DEFAULT_THREAD_PRIORITY ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "daemon" ),
                                                 createBooleanConfigMock( false,
                                                                          DefaultRunnableManager.DEFAULT_DAEMON_MODE ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "keep-alive-time-ms" ),
                                                 createLongConfigMock( DefaultRunnableManager.DEFAULT_KEEP_ALIVE_TIME / 2,
                                                                       DefaultRunnableManager.DEFAULT_KEEP_ALIVE_TIME ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "block-policy" ),
                                                 createValueConfigMock( "WAIT",
                                                                        DefaultThreadPool.POLICY_DEFAULT ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "shutdown-graceful" ),
                                                 createBooleanConfigMock( true,
                                                                          DefaultRunnableManager.DEFAULT_SHUTDOWN_GRACEFUL ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "shutdown-wait-time-ms" ),
                                                 createIntegerConfigMock( DefaultRunnableManager.DEFAULT_SHUTDOWN_WAIT_TIME / 2,
                                                                          DefaultRunnableManager.DEFAULT_SHUTDOWN_WAIT_TIME ) );
        threadPoolConfigControl.replay(  );

        final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration []
                                                                     {
                                                                         threadPoolConfig
                                                                     } ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerDaemonControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDaemon =
            (Logger)childLoggerDaemonControl.getMock(  );
        childLoggerDaemonControl.replay(  );

        final MockControl childLoggerDefaultControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDefault =
            (Logger)childLoggerDefaultControl.getMock(  );
        childLoggerDefaultControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        logger.warn( "Unknown thread priority \"LOW\". Set to \"NORM\"." );
        loggerControl.expectAndReturn( logger.getChildLogger( "daemon" ),
                                       childLoggerDaemon );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"daemon\" created with maximum queue-size=2147483647,max-pool-size=10,min-pool-size=1,priority=0,isDaemon=false,keep-alive-time-ms=30000,block-policy=\"WAIT\",shutdown-wait-time-ms=0" );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLoggerDefault );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=0,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool daemon" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool daemon disposed" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );
        runnableManager.enableLogging( logger );

        try
        {
            runnableManager.configure( mainConfig );
        }
        catch( final ConfigurationException ce )
        {
            assertTrue( "Throw unexpected ConfigurationException", false );
        }

        runnableManager.dispose(  );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testConfigureMinimal(  )
    {
        final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerDefaultControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDefault =
            (Logger)childLoggerDefaultControl.getMock(  );
        childLoggerDefaultControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLoggerDefault );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=0,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );
        runnableManager.enableLogging( logger );

        try
        {
            runnableManager.configure( mainConfig );
        }
        catch( final ConfigurationException ce )
        {
            assertTrue( "Throw unexpected ConfigurationException", false );
        }

        runnableManager.dispose(  );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public final void testConfigureMyPool(  )
        throws Exception
    {
        final MockControl threadPoolConfigControl =
            createStrictControl( Configuration.class );
        final Configuration threadPoolConfig =
            (Configuration)threadPoolConfigControl.getMock(  );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "name" ),
                                                 createValueConfigMock( "mypool" ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "queue-size" ),
                                                 createIntegerConfigMock( 2 * DefaultRunnableManager.DEFAULT_QUEUE_SIZE,
                                                                          DefaultRunnableManager.DEFAULT_QUEUE_SIZE ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "max-pool-size" ),
                                                 createIntegerConfigMock( 2 * DefaultRunnableManager.DEFAULT_MAX_POOL_SIZE,
                                                                          DefaultRunnableManager.DEFAULT_MAX_POOL_SIZE ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "min-pool-size" ),
                                                 createIntegerConfigMock( DefaultRunnableManager.DEFAULT_MIN_POOL_SIZE / 3,
                                                                          DefaultRunnableManager.DEFAULT_MIN_POOL_SIZE ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "priority" ),
                                                 createValueConfigMock( "MIN",
                                                                        DefaultRunnableManager.DEFAULT_THREAD_PRIORITY ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "daemon" ),
                                                 createBooleanConfigMock( false,
                                                                          DefaultRunnableManager.DEFAULT_DAEMON_MODE ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "keep-alive-time-ms" ),
                                                 createLongConfigMock( DefaultRunnableManager.DEFAULT_KEEP_ALIVE_TIME / 2,
                                                                       DefaultRunnableManager.DEFAULT_KEEP_ALIVE_TIME ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "block-policy" ),
                                                 createValueConfigMock( "WAIT",
                                                                        DefaultThreadPool.POLICY_DEFAULT ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "shutdown-graceful" ),
                                                 createBooleanConfigMock( true,
                                                                          DefaultRunnableManager.DEFAULT_SHUTDOWN_GRACEFUL ) );
        threadPoolConfigControl.expectAndReturn( threadPoolConfig.getChild( "shutdown-wait-time-ms" ),
                                                 createIntegerConfigMock( DefaultRunnableManager.DEFAULT_SHUTDOWN_WAIT_TIME / 2,
                                                                          DefaultRunnableManager.DEFAULT_SHUTDOWN_WAIT_TIME ) );
        threadPoolConfigControl.replay(  );

        final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration []
                                                                     {
                                                                         threadPoolConfig
                                                                     } ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerDefaultControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDefault =
            (Logger)childLoggerDefaultControl.getMock(  );
        childLoggerDefaultControl.replay(  );

        final MockControl childLoggerMyPoolControl =
            createStrictControl( Logger.class );
        final Logger childLoggerMyPool =
            (Logger)childLoggerMyPoolControl.getMock(  );
        childLoggerMyPoolControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "mypool" ),
                                       childLoggerMyPool );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"mypool\" created with maximum queue-size=2147483647,max-pool-size=10,min-pool-size=1,priority=0,isDaemon=false,keep-alive-time-ms=30000,block-policy=\"WAIT\",shutdown-wait-time-ms=0" );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLoggerDefault );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=0,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool mypool" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool mypool disposed" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );
        runnableManager.enableLogging( logger );

        try
        {
            runnableManager.configure( mainConfig );
        }
        catch( final ConfigurationException ce )
        {
            assertTrue( "Throw unexpected ConfigurationException", false );
        }

        runnableManager.dispose(  );
    }

    /**
     * Class under test for void createPool(String, int, int, int, int,
     * boolean, long, String, boolean, int)
     */
    public final void testCreatePoolStringintintintintbooleanlongStringbooleanint(  )
    {
        final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerDefaultControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDefault =
            (Logger)childLoggerDefaultControl.getMock(  );
        childLoggerDefaultControl.replay(  );

        final MockControl childLoggerMyPoolControl =
            createStrictControl( Logger.class );
        final Logger childLoggerMyPool =
            (Logger)childLoggerMyPoolControl.getMock(  );
        childLoggerMyPoolControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLoggerDefault );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=0,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.getChildLogger( "mypool" ),
                                       childLoggerMyPool );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"mypool\" created with maximum queue-size=230,max-pool-size=15,min-pool-size=12,priority=0,isDaemon=false,keep-alive-time-ms=15500,block-policy=\"DISCARD\",shutdown-wait-time-ms=22200" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool mypool" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool mypool disposed" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );
        runnableManager.enableLogging( logger );

        try
        {
            runnableManager.configure( mainConfig );
        }
        catch( final ConfigurationException ce )
        {
            assertTrue( "Throw unexpected ConfigurationException", false );
        }

        runnableManager.createPool( "mypool", 230, 15, 12, Thread.MIN_PRIORITY,
                                    false, 15500, "DISCARD", false, 22200 );
        runnableManager.dispose(  );
    }

    /**
     * Class under test for ThreadPool createPool(int, int, int, int, boolean,
     * long, String, boolean, int)
     */
    public final void testCreatePoolintintintintbooleanlongStringbooleanint(  )
    {
        final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerDefaultControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDefault =
            (Logger)childLoggerDefaultControl.getMock(  );
        childLoggerDefaultControl.replay(  );

        final MockControl childLoggerAnonControl =
            createStrictControl( Logger.class );
        final Logger childLoggerAnon =
            (Logger)childLoggerAnonControl.getMock(  );
        childLoggerAnonControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLoggerDefault );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=0,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.getChildLogger( "anon-xxx" ),
                                       childLoggerAnon );
        loggerControl.setMatcher( MockControl.ALWAYS_MATCHER );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"anon-xxx\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=0,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.setMatcher( MockControl.ALWAYS_MATCHER );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool anon-xxx" );
        loggerControl.setMatcher( MockControl.ALWAYS_MATCHER );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool anon-xxx disposed" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.setMatcher( MockControl.ALWAYS_MATCHER );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );
        runnableManager.enableLogging( logger );

        try
        {
            runnableManager.configure( mainConfig );
        }
        catch( final ConfigurationException ce )
        {
            assertTrue( "Throw unexpected ConfigurationException", false );
        }

        final ThreadPool threadPool =
            runnableManager.createPool( 200, 5, 2, Thread.MAX_PRIORITY, true,
                                        15000, "ABORT", true, 22000 );
        assertEquals( "queue-size", 200, threadPool.getMaximumQueueSize(  ) );
        runnableManager.dispose(  );
    }

    /**
     * Class under test for void execute(Runnable)
     */
    public final void testExecuteRunnable(  )
    {
        final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerControl =
            createStrictControl( Logger.class );
        final Logger childLogger = (Logger)childLoggerControl.getMock(  );
        childLoggerControl.expectAndReturn( childLogger.isDebugEnabled(  ), true );
        childLogger.debug( "Executing Command: org.apache.cocoon.components.thread.DefaultRunnableManager" );
        childLoggerControl.setMatcher( MockControl.ALWAYS_MATCHER );
        childLoggerControl.expectAndReturn( childLogger.isDebugEnabled(  ), true );
        childLogger.debug( "Executing Command: org.apache.cocoon.components.thread.DefaultRunnableManager" );
        childLoggerControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLogger );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=0,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "starting heart" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Entering loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Command entered: EasyMock for interface java.lang.Runnable,pool=default,delay=0,interval=0" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Hand over Command EasyMock for interface java.lang.Runnable to pool \"default\" with delay=0 and interval=0" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Exiting loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );
        runnableManager.enableLogging( logger );

        try
        {
            runnableManager.configure( mainConfig );
        }
        catch( final ConfigurationException ce )
        {
            assertTrue( "Throw unexpected ConfigurationException", false );
        }

        final MockControl runnableControl =
            createStrictControl( Runnable.class );
        final Runnable runnable = (Runnable)runnableControl.getMock(  );
        runnable.run(  );
        runnableControl.replay(  );

        try
        {
            runnableManager.start(  );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.execute( runnable );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.stop(  );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.dispose(  );
            Thread.sleep( 20 );
        }
        catch( final Throwable ex )
        {
            ex.printStackTrace(  );
            assertTrue( "Unexpected Exception", false );
        }
    }

    /**
     * Class under test for void execute(Runnable, long)
     */
    public final void testExecuteRunnablelong(  )
    {
        final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerControl =
            createStrictControl( Logger.class );
        final Logger childLogger = (Logger)childLoggerControl.getMock(  );
        childLoggerControl.expectAndReturn( childLogger.isDebugEnabled(  ), true );
        childLogger.debug( "Executing Command: org.apache.cocoon.components.thread.DefaultRunnableManager" );
        childLoggerControl.setMatcher( MockControl.ALWAYS_MATCHER );
        childLoggerControl.expectAndReturn( childLogger.isDebugEnabled(  ), true );
        childLogger.debug( "Executing Command: org.apache.cocoon.components.thread.DefaultRunnableManager" );
        childLoggerControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLogger );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=0,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "starting heart" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Entering loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Command entered: EasyMock for interface java.lang.Runnable,pool=default,delay=100,interval=0" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Hand over Command EasyMock for interface java.lang.Runnable to pool \"default\" with delay=100 and interval=0" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Exiting loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );
        runnableManager.enableLogging( logger );

        try
        {
            runnableManager.configure( mainConfig );
        }
        catch( final ConfigurationException ce )
        {
            assertTrue( "Throw unexpected ConfigurationException", false );
        }

        final MockControl runnableControl =
            createStrictControl( Runnable.class );
        final Runnable runnable = (Runnable)runnableControl.getMock(  );
        runnable.run(  );
        runnableControl.replay(  );

        try
        {
            runnableManager.start(  );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.execute( runnable, 100, 0 );
            Thread.yield(  );
            Thread.sleep( 200 );
            runnableManager.stop(  );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.dispose(  );
            Thread.sleep( 20 );
        }
        catch( final Throwable ex )
        {
            ex.printStackTrace(  );
            assertTrue( "Unexpected Exception", false );
        }
    }

    /**
     * Class under test for void execute(Runnable, long, long)
     */
    public final void testExecuteRunnablelonglong(  )
    {
        final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerControl =
            createStrictControl( Logger.class );
        final Logger childLogger = (Logger)childLoggerControl.getMock(  );
        childLoggerControl.expectAndReturn( childLogger.isDebugEnabled(  ), true );
        childLogger.debug( "Executing Command: org.apache.cocoon.components.thread.DefaultRunnableManager" );
        childLoggerControl.setMatcher( MockControl.ALWAYS_MATCHER );
        childLoggerControl.expectAndReturn( childLogger.isDebugEnabled(  ), true );
        childLogger.debug( "Executing Command: org.apache.cocoon.components.thread.DefaultRunnableManager" );
        childLoggerControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLogger );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=0,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "starting heart" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Entering loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Command entered: EasyMock for interface java.lang.Runnable,pool=default,delay=100,interval=100" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Hand over Command EasyMock for interface java.lang.Runnable to pool \"default\" with delay=100 and interval=100" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Exiting loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );
        runnableManager.enableLogging( logger );

        try
        {
            runnableManager.configure( mainConfig );
        }
        catch( final ConfigurationException ce )
        {
            assertTrue( "Throw unexpected ConfigurationException", false );
        }

        final MockControl runnableControl =
            createStrictControl( Runnable.class );
        final Runnable runnable = (Runnable)runnableControl.getMock(  );
        runnable.run(  );
        runnableControl.setVoidCallable( MockControl.ONE_OR_MORE );
        runnableControl.replay(  );

        try
        {
            runnableManager.start(  );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.execute( runnable, 100, 100 );
            Thread.yield(  );
            Thread.sleep( 200 );
            runnableManager.stop(  );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.dispose(  );
            Thread.sleep( 20 );
        }
        catch( final Throwable ex )
        {
            ex.printStackTrace(  );
            assertTrue( "Unexpected Exception", false );
        }
    }

    /**
     * Class under test for void execute(String, Runnable)
     */
    public final void testExecuteStringRunnable(  )
    {
        final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerDefaultControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDefault =
            (Logger)childLoggerDefaultControl.getMock(  );
        childLoggerDefaultControl.expectAndReturn( childLoggerDefault.isDebugEnabled(  ),
                                                   true );
        childLoggerDefault.debug( "Executing Command: org.apache.cocoon.components.thread.DefaultRunnableManager" );
        childLoggerDefaultControl.setMatcher( MockControl.ALWAYS_MATCHER );
        childLoggerDefaultControl.replay(  );

        final MockControl childLoggerMyPoolControl =
            createStrictControl( Logger.class );
        final Logger childLoggerMyPool =
            (Logger)childLoggerMyPoolControl.getMock(  );
        childLoggerMyPoolControl.expectAndReturn( childLoggerMyPool.isDebugEnabled(  ),
                                                  true );
        childLoggerMyPool.debug( "Executing Command: EasyMock for interface java.lang.Runnable,pool=mypool" );
        childLoggerMyPoolControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLoggerDefault );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=0,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "starting heart" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Entering loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.getChildLogger( "mypool" ),
                                       childLoggerMyPool );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"mypool\" created with maximum queue-size=230,max-pool-size=15,min-pool-size=12,priority=0,isDaemon=false,keep-alive-time-ms=15500,block-policy=\"DISCARD\",shutdown-wait-time-ms=22200" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Command entered: EasyMock for interface java.lang.Runnable,pool=mypool,delay=0,interval=0" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Hand over Command EasyMock for interface java.lang.Runnable to pool \"mypool\" with delay=0 and interval=0" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Exiting loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool mypool" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool mypool disposed" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );
        runnableManager.enableLogging( logger );

        try
        {
            runnableManager.configure( mainConfig );
        }
        catch( final ConfigurationException ce )
        {
            assertTrue( "Throw unexpected ConfigurationException", false );
        }

        final MockControl runnableControl =
            createStrictControl( Runnable.class );
        final Runnable runnable = (Runnable)runnableControl.getMock(  );
        runnable.run(  );
        runnableControl.replay(  );

        try
        {
            runnableManager.start(  );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.createPool( "mypool", 230, 15, 12,
                                        Thread.MIN_PRIORITY, false, 15500,
                                        "DISCARD", false, 22200 );
            runnableManager.execute( "mypool", runnable );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.stop(  );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.dispose(  );
            Thread.sleep( 20 );
        }
        catch( final Throwable ex )
        {
            ex.printStackTrace(  );
            assertTrue( "Unexpected Exception", false );
        }
    }

    /**
     * Class under test for void execute(String, Runnable, long)
     */
    public final void testExecuteStringRunnablelong(  )
    {
        final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerDefaultControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDefault =
            (Logger)childLoggerDefaultControl.getMock(  );
        childLoggerDefaultControl.expectAndReturn( childLoggerDefault.isDebugEnabled(  ),
                                                   true );
        childLoggerDefault.debug( "Executing Command: org.apache.cocoon.components.thread.DefaultRunnableManager" );
        childLoggerDefaultControl.setMatcher( MockControl.ALWAYS_MATCHER );
        childLoggerDefaultControl.replay(  );

        final MockControl childLoggerMyPoolControl =
            createStrictControl( Logger.class );
        final Logger childLoggerMyPool =
            (Logger)childLoggerMyPoolControl.getMock(  );
        childLoggerMyPoolControl.expectAndReturn( childLoggerMyPool.isDebugEnabled(  ),
                                                  true );
        childLoggerMyPool.debug( "Executing Command: EasyMock for interface java.lang.Runnable,pool=mypool" );
        childLoggerMyPoolControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLoggerDefault );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=0,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "starting heart" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Entering loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.getChildLogger( "mypool" ),
                                       childLoggerMyPool );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"mypool\" created with maximum queue-size=230,max-pool-size=15,min-pool-size=12,priority=0,isDaemon=false,keep-alive-time-ms=15500,block-policy=\"DISCARD\",shutdown-wait-time-ms=22200" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Command entered: EasyMock for interface java.lang.Runnable,pool=mypool,delay=100,interval=0" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Hand over Command EasyMock for interface java.lang.Runnable to pool \"mypool\" with delay=100 and interval=0" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Exiting loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool mypool" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool mypool disposed" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );
        runnableManager.enableLogging( logger );

        try
        {
            runnableManager.configure( mainConfig );
        }
        catch( final ConfigurationException ce )
        {
            assertTrue( "Throw unexpected ConfigurationException", false );
        }

        final MockControl runnableControl =
            createStrictControl( Runnable.class );
        final Runnable runnable = (Runnable)runnableControl.getMock(  );
        runnable.run(  );
        runnableControl.replay(  );

        try
        {
            runnableManager.start(  );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.createPool( "mypool", 230, 15, 12,
                                        Thread.MIN_PRIORITY, false, 15500,
                                        "DISCARD", false, 22200 );
            runnableManager.execute( "mypool", runnable, 100, 0 );
            Thread.yield(  );
            Thread.sleep( 200 );
            runnableManager.stop(  );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.dispose(  );
            Thread.sleep( 20 );
        }
        catch( final Throwable ex )
        {
            ex.printStackTrace(  );
            assertTrue( "Unexpected Exception", false );
        }
    }

    /**
     * Class under test for void execute(String, Runnable, long, long)
     */
    public final void testExecuteStringRunnablelonglong(  )
    {
        final MockControl mainConfigControl =
            createStrictControl( Configuration.class );
        final Configuration mainConfig =
            (Configuration)mainConfigControl.getMock(  );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-factory" ),
                                           createValueConfigMock( DefaultRunnableManager.DEFAULT_THREAD_FACTORY,
                                                                  DefaultRunnableManager.DEFAULT_THREAD_FACTORY ) );
        mainConfigControl.expectAndReturn( mainConfig.getChild( "thread-pools" ),
                                           createChildrenConfigMock( "thread-pool",
                                                                     new Configuration[ 0 ] ) );
        mainConfigControl.replay(  );

        final MockControl childLoggerDefaultControl =
            createStrictControl( Logger.class );
        final Logger childLoggerDefault =
            (Logger)childLoggerDefaultControl.getMock(  );
        childLoggerDefaultControl.expectAndReturn( childLoggerDefault.isDebugEnabled(  ),
                                                   true );
        childLoggerDefault.debug( "Executing Command: org.apache.cocoon.components.thread.DefaultRunnableManager" );
        childLoggerDefaultControl.setMatcher( MockControl.ALWAYS_MATCHER );
        childLoggerDefaultControl.replay(  );

        final MockControl childLoggerMyPoolControl =
            createStrictControl( Logger.class );
        final Logger childLoggerMyPool =
            (Logger)childLoggerMyPoolControl.getMock(  );
        childLoggerMyPoolControl.expectAndReturn( childLoggerMyPool.isDebugEnabled(  ),
                                                  true );
        childLoggerMyPool.debug( "Executing Command: EasyMock for interface java.lang.Runnable,pool=mypool" );
        childLoggerMyPoolControl.replay(  );

        final MockControl loggerControl = createStrictControl( Logger.class );
        final Logger logger = (Logger)loggerControl.getMock(  );
        loggerControl.expectAndReturn( logger.getChildLogger( "default" ),
                                       childLoggerDefault );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"default\" created with maximum queue-size=2147483647,max-pool-size=5,min-pool-size=5,priority=0,isDaemon=false,keep-alive-time-ms=60000,block-policy=\"RUN\",shutdown-wait-time-ms=-1" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "starting heart" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Entering loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "No commands available. Will just wait for one" );
        loggerControl.expectAndReturn( logger.getChildLogger( "mypool" ),
                                       childLoggerMyPool );
        loggerControl.expectAndReturn( logger.isInfoEnabled(  ), true );
        logger.info( "ThreadPool named \"mypool\" created with maximum queue-size=230,max-pool-size=15,min-pool-size=12,priority=0,isDaemon=false,keep-alive-time-ms=15500,block-policy=\"DISCARD\",shutdown-wait-time-ms=22200" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Command entered: EasyMock for interface java.lang.Runnable,pool=mypool,delay=100,interval=100" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Hand over Command EasyMock for interface java.lang.Runnable to pool \"mypool\" with delay=100 and interval=100" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Exiting loop" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing all thread pools" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool mypool" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool mypool disposed" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Disposing thread pool default" );
        loggerControl.expectAndReturn( logger.isDebugEnabled(  ), true );
        logger.debug( "Thread pool default disposed" );
        loggerControl.replay(  );

        final DefaultRunnableManager runnableManager =
            new DefaultRunnableManager(  );
        runnableManager.enableLogging( logger );

        try
        {
            runnableManager.configure( mainConfig );
        }
        catch( final ConfigurationException ce )
        {
            assertTrue( "Throw unexpected ConfigurationException", false );
        }

        final MockControl runnableControl =
            createStrictControl( Runnable.class );
        final Runnable runnable = (Runnable)runnableControl.getMock(  );
        runnable.run(  );
        runnableControl.replay(  );

        try
        {
            runnableManager.start(  );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.createPool( "mypool", 230, 15, 12,
                                        Thread.MIN_PRIORITY, false, 15500,
                                        "DISCARD", false, 22200 );
            runnableManager.execute( "mypool", runnable, 100, 100 );
            Thread.yield(  );
            Thread.sleep( 200 );
            runnableManager.stop(  );
            Thread.yield(  );
            Thread.sleep( 20 );
            runnableManager.dispose(  );
            Thread.sleep( 20 );
        }
        catch( final Throwable ex )
        {
            ex.printStackTrace(  );
            assertTrue( "Unexpected Exception", false );
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp(  )
        throws Exception
    {
        super.setUp(  );
        m_controls = new ArrayList(  );
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown(  )
        throws Exception
    {
        for( Iterator i = m_controls.iterator(  ); i.hasNext(  ); )
        {
            final MockControl control = (MockControl)i.next(  );
            control.verify(  );
        }

        m_controls = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param value DOCUMENT ME!
     * @param defaultValue DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Configuration createBooleanConfigMock( final boolean value,
                                                   final boolean defaultValue )
    {
        final MockControl valueConfigControl =
            createStrictControl( Configuration.class );
        final Configuration valueConfig =
            (Configuration)valueConfigControl.getMock(  );
        valueConfig.getValueAsBoolean( defaultValue );
        valueConfigControl.setReturnValue( value );
        valueConfigControl.replay(  );

        return valueConfig;
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     * @param value DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Configuration createChildConfigMock( final String name,
                                                 final Configuration value )
    {
        final MockControl childConfigControl =
            createStrictControl( Configuration.class );
        final Configuration childConfig =
            (Configuration)childConfigControl.getMock(  );
        childConfig.getChild( name );
        childConfigControl.setReturnValue( value );
        childConfigControl.replay(  );

        return childConfig;
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     * @param value DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Configuration createChildrenConfigMock( final String name,
                                                    final Configuration [] value )
    {
        final MockControl childrenConfigControl =
            createStrictControl( Configuration.class );
        final Configuration childrenConfig =
            (Configuration)childrenConfigControl.getMock(  );
        childrenConfig.getChildren( name );
        childrenConfigControl.setReturnValue( value );
        childrenConfigControl.replay(  );

        return childrenConfig;
    }

    /**
     * DOCUMENT ME!
     *
     * @param value DOCUMENT ME!
     * @param defaultValue DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Configuration createIntegerConfigMock( final int value,
                                                   final int defaultValue )
    {
        final MockControl valueConfigControl =
            createStrictControl( Configuration.class );
        final Configuration valueConfig =
            (Configuration)valueConfigControl.getMock(  );
        valueConfig.getValueAsInteger( defaultValue );
        valueConfigControl.setReturnValue( value );
        valueConfigControl.replay(  );

        return valueConfig;
    }

    /**
     * DOCUMENT ME!
     *
     * @param value DOCUMENT ME!
     * @param defaultValue DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Configuration createLongConfigMock( final long value,
                                                final long defaultValue )
    {
        final MockControl valueConfigControl =
            createStrictControl( Configuration.class );
        final Configuration valueConfig =
            (Configuration)valueConfigControl.getMock(  );
        valueConfig.getValueAsLong( defaultValue );
        valueConfigControl.setReturnValue( value );
        valueConfigControl.replay(  );

        return valueConfig;
    }

    /**
     * DOCUMENT ME!
     *
     * @param clazz DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private MockControl createStrictControl( final Class clazz )
    {
        final MockControl control = MockControl.createStrictControl( clazz );
        m_controls.add( control );

        return control;
    }

    /**
     * DOCUMENT ME!
     *
     * @param value DOCUMENT ME!
     * @param defaultValue DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Configuration createValueConfigMock( final String value,
                                                 final String defaultValue )
    {
        final MockControl valueConfigControl =
            createStrictControl( Configuration.class );
        final Configuration valueConfig =
            (Configuration)valueConfigControl.getMock(  );
        valueConfig.getValue( defaultValue );
        valueConfigControl.setReturnValue( value );
        valueConfigControl.replay(  );

        return valueConfig;
    }

    /**
     * DOCUMENT ME!
     *
     * @param value DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws ConfigurationException DOCUMENT ME!
     */
    private Configuration createValueConfigMock( final String value )
        throws ConfigurationException
    {
        final MockControl valueConfigControl =
            createStrictControl( Configuration.class );
        final Configuration valueConfig =
            (Configuration)valueConfigControl.getMock(  );
        valueConfig.getValue(  );
        valueConfigControl.setReturnValue( value );
        valueConfigControl.replay(  );

        return valueConfig;
    }
}
