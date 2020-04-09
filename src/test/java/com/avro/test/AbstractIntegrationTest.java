package com.avro.test;

import org.junit.ClassRule;
import org.junit.Rule;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;

public class AbstractIntegrationTest extends AbstractTest {

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(
            WireMockConfiguration.options().dynamicPort());

    @Rule
    public WireMockClassRule instanceRule = wireMockRule;
}
