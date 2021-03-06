/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.dita.dost.exception.DITAOTException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Key definition.
 */
public class KeyDef {
    
    public static final String ELEMENT_STUB = "stub";
    private static final String ATTRIUBTE_SOURCE = "source";
    private static final String ATTRIBUTE_HREF = "href";
    private static final String ATTRIBUTE_SCOPE = "scope";
    private static final String ATTRIBUTE_KEYS = "keys";
    private static final String ELEMENT_KEYDEF = "keydef";
    
    public final String keys;
    public final URI href;
    public final String scope;
    public final URI source;
    
    /**
     * Construct new key definition.
     * 
     * @param keys key name
     * @param href href URI, may be {@code null}
     * @param scope link scope, may be {@code null}
     * @param source key definition source, may be {@code null}
     */
    public KeyDef(final String keys, final URI href, final String scope, final URI source) {
        this.keys = keys;
        this.href = href;
        this.scope = scope;
        this.source = source;
    }
    
    /**
     * Construct new key definition.
     * 
     * @param keys key name
     * @param href href URI, may be {@code null}
     * @param scope link scope, may be {@code null}
     * @param source key definition source, may be {@code null}
     */
    @Deprecated
    public KeyDef(final String keys, final String href, final String scope, final String source) {
        this.keys = keys;
//        try {
            this.href = href != null ? toURI(href) : null;
//        } catch (final URISyntaxException e) {
//            throw new IllegalArgumentException(e.getMessage(), e);
//        }
        this.scope = scope;
//        try {
            this.source = source != null ? toURI(source) : null;
//        } catch (final URISyntaxException e) {
//            throw new IllegalArgumentException(e.getMessage(), e);
//        }
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder().append(keys).append(EQUAL);
        if (href != null) {
            buf.append(href.toString());
        }
        if (scope != null) {
            buf.append(LEFT_BRACKET).append(scope).append(RIGHT_BRACKET);
        }
        if (source != null) {
            buf.append(LEFT_BRACKET).append(source.toString()).append(RIGHT_BRACKET);
        }
        return buf.toString();
    }
    
    /**
     * Read key definition XML configuration file
     * 
     * @param keydefFile key definition file
     * @return list of key definitions
     * @throws DITAOTException if reading configuration file failed
     */
    public static Collection<KeyDef> readKeydef(final File keydefFile) throws DITAOTException {
        final Collection<KeyDef> res = new ArrayList<KeyDef>();
        try {
            final XMLReader parser = StringUtils.getXMLReader();
            parser.setContentHandler(new DefaultHandler() {
                @Override
                public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
                    final String n = localName != null ? localName : qName;
                    if (n.equals(ELEMENT_KEYDEF)) {
                        res.add(new KeyDef(atts.getValue(ATTRIBUTE_KEYS),
                                           atts.getValue(ATTRIBUTE_HREF),
                                           atts.getValue(ATTRIBUTE_SCOPE),
                                           atts.getValue(ATTRIUBTE_SOURCE)));
                    }
                }
            });
            parser.parse(keydefFile.toURI().toString());
        } catch (final Exception e) {
            throw new DITAOTException("Failed to read key definition file " + keydefFile + ": " + e.getMessage(), e);
        }
        return res;
    }

    /**
     * Write key definition XML configuration file
     * 
     * @param keydefFile key definition file
     * @param keydefs list of key definitions
     * @throws DITAOTException if writing configuration file failed
     */
    public static void writeKeydef(final File keydefFile, final Collection<KeyDef> keydefs) throws DITAOTException {
        OutputStream out = null;
        XMLStreamWriter keydef = null;
        try {
            out = new FileOutputStream(keydefFile);
            keydef = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
            keydef.writeStartDocument();
            keydef.writeStartElement(ELEMENT_STUB);
            for (final KeyDef k: keydefs) {
                keydef.writeStartElement(ELEMENT_KEYDEF);
                keydef.writeAttribute(ATTRIBUTE_KEYS, k.keys);
                if (k.href != null) {
                    keydef.writeAttribute(ATTRIBUTE_HREF, k.href.toString());
                }
                if (k.scope != null) {
                    keydef.writeAttribute(ATTRIBUTE_SCOPE, k.scope);
                }
                if (k.source != null) {
                    keydef.writeAttribute(ATTRIUBTE_SOURCE, k.source.toString());
                }
                keydef.writeEndElement();
            }        
            keydef.writeEndDocument();
        } catch (final XMLStreamException e) {
            throw new DITAOTException("Failed to write key definition file " + keydefFile + ": " + e.getMessage(), e);
        } catch (final IOException e) {
            throw new DITAOTException("Failed to write key definition file " + keydefFile + ": " + e.getMessage(), e);
        } finally {
            if (keydef != null) {
                try {
                    keydef.close();
                } catch (final XMLStreamException e) {}
            }
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {}
            }
        }
    }
}