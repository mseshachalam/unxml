package com.nerdforge.unxml;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import com.nerdforge.unxml.factory.ArrayNodeParserBuilderFactory;
import com.nerdforge.unxml.factory.ObjectArrayParserBuilderFactory;
import com.nerdforge.unxml.factory.ObjectNodeParserFactory;
import com.nerdforge.unxml.parsers.*;
import com.nerdforge.unxml.factory.ArrayNodeParserFactory;
import com.nerdforge.unxml.parsers.builders.ArrayNodeParserBuilder;
import com.nerdforge.unxml.parsers.builders.ObjectArrayParserBuilder;
import com.nerdforge.unxml.xml.LoggingErrorHandler;
import com.nerdforge.unxml.xml.SimpleNamespaceContext;
import com.nerdforge.unxml.xml.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashMap;
import java.util.Map;

public class UnXmlModule extends PrivateModule {
    Map<String, String> namespaces = new HashMap<>();

    public UnXmlModule(){}

    public UnXmlModule(Map<String, String> namespaces){
        this.namespaces = namespaces;
    }

    @Override
    protected void configure() {
        // Generate Factories
        install(new FactoryModuleBuilder()
                .implement(ObjectNodeParser.class, ObjectNodeParser.class)
                .build(ObjectNodeParserFactory.class));

        install(new FactoryModuleBuilder()
                .implement(ArrayNodeParser.class, ArrayNodeParser.class)
                .build(ArrayNodeParserFactory.class));

        install(new FactoryModuleBuilder()
                .implement(ArrayNodeParserBuilder.class, ArrayNodeParserBuilder.class)
                .build(ArrayNodeParserBuilderFactory.class));

        install(new FactoryModuleBuilder()
                .implement(ObjectArrayParserBuilder.class, ObjectArrayParserBuilder.class)
                .build(ObjectArrayParserBuilderFactory.class));

        // Logger
        bind(Logger.class).toInstance(LoggerFactory.getLogger("unXml"));

        // JSON Mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        bind(ObjectMapper.class).annotatedWith(Names.named("json-mapper")).toInstance(mapper);

        // bind xml namespaces
        bind(NamespaceContext.class).toInstance(new SimpleNamespaceContext(namespaces));

        // bind document builder factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        bind(DocumentBuilderFactory.class).toInstance(documentBuilderFactory);

        // bind xml document builder error handler
        bind(ErrorHandler.class).to(LoggingErrorHandler.class);

        bind(Parsing.class);
        expose(Parsing.class);

        bind(XmlUtil.class);
        expose(XmlUtil.class);

        bind(SimpleParsers.class);
        expose(SimpleParsers.class);
    }
}
