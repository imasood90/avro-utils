package com.avro.test;

import static org.mockito.MockitoAnnotations.*;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Spring Unit Tests
 *
 *
 */
public abstract class AbstractTest {

    /**
     * Log variable for all child classes. Uses LoggerFactory.getLogger(getClass()) from
     * slf4j Logging
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void before() throws Exception {
        initMocks(this);
    }
}
