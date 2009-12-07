/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.vysper.xml.decoder;

import junit.framework.TestCase;

import org.apache.vysper.xml.decoder.DecodingException;
import org.apache.vysper.xml.decoder.XMLParticle;
import org.apache.vysper.xml.decoder.XMLRawToFragmentConverter;
import org.apache.vysper.xml.fragment.Attribute;
import org.apache.vysper.xml.fragment.XMLElement;
import org.apache.vysper.xml.fragment.XMLElementVerifier;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class XMLRawToFragmentConverterConvertTestCase extends TestCase {
    private XMLRawToFragmentConverter xmlRawToFragmentConverter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        xmlRawToFragmentConverter = new XMLRawToFragmentConverter();
    }

    public void testElementSimple() throws DecodingException {
        List<XMLParticle> particles = new ArrayList<XMLParticle>();

        particles.add(new XMLParticle("<balanced/>"));

        XMLElement xmlElement = (XMLElement) xmlRawToFragmentConverter.convert(particles);
        assertNotNull(xmlElement);
        assertEquals("balanced", xmlElement.getName());
    }
    
    public void testElementSimpleAttributes() throws DecodingException {
        List<XMLParticle> particles = new ArrayList<XMLParticle>();

        particles.add(new XMLParticle("<balanced attr1=\"1\" attr2='2' ns:at_tr3=\"\" />"));

        XMLElement xmlElement = (XMLElement) xmlRawToFragmentConverter.convert(particles);
        assertNotNull(xmlElement);
        assertEquals("balanced", xmlElement.getName());
        List<Attribute> attributes = xmlElement.getAttributes();
        assertEquals(3, attributes.size());
        assertEquals("attr1", attributes.get(0).getName());
        assertEquals("1", attributes.get(0).getValue());
        assertEquals("attr2", attributes.get(1).getName());
        assertEquals("2", attributes.get(1).getValue());
        assertEquals("ns:at_tr3", attributes.get(2).getName());
        assertEquals("", attributes.get(2).getValue());
    }
    public void testElementSimpleAttributesMoreWhitespace() throws DecodingException {
        List<XMLParticle> particles = new ArrayList<XMLParticle>();

        particles.add(new XMLParticle("<balanced attr1 = \"1\"   attr2=' 2 ' ns:at_tr3= \" \" />"));

        XMLElement xmlElement = (XMLElement) xmlRawToFragmentConverter.convert(particles);
        assertNotNull(xmlElement);
        assertEquals("balanced", xmlElement.getName());
        List<Attribute> attributes = xmlElement.getAttributes();
        assertEquals(3, attributes.size());
        assertEquals("attr1", attributes.get(0).getName());
        assertEquals("1", attributes.get(0).getValue());
        assertEquals("attr2", attributes.get(1).getName());
        assertEquals(" 2 ", attributes.get(1).getValue());
        assertEquals("ns:at_tr3", attributes.get(2).getName());
        assertEquals(" ", attributes.get(2).getValue());
    }
    
    public void testSimple2() throws DecodingException {
        List<XMLParticle> particles = new ArrayList<XMLParticle>();

        particles.add(new XMLParticle("<balanced>"));
        particles.add(new XMLParticle("</balanced>"));

        XMLElement xmlElement = (XMLElement) xmlRawToFragmentConverter.convert(particles);
        assertNotNull(xmlElement);
    }
    
    public void testNested1() throws DecodingException {
        List<XMLParticle> particles = new ArrayList<XMLParticle>();

        particles.add(new XMLParticle("<balanced>"));
        particles.add(new XMLParticle("<inner>"));
        particles.add(new XMLParticle("</inner>"));
        particles.add(new XMLParticle("</balanced>"));
        
        XMLElement xmlElement = (XMLElement) xmlRawToFragmentConverter.convert(particles);
        assertEquals(1, ((XMLElement)xmlElement).getInnerElements().size());
    }
    
    public void testElementNameColonAtStart() throws DecodingException {
        List<XMLParticle> particles = new ArrayList<XMLParticle>();

        XMLParticle particle = new XMLParticle("<:name />");
        particles.add(particle);
        try {
            xmlRawToFragmentConverter.convert(particles);
            fail("colon at start denotes legal element name (according to XML Spec) -- but we don't support it");
        } catch (DecodingException e) {
            // succeeded 
        }
        assertEquals(":name", particle.getElementName());
    }

    private XMLElement elementFromString(String... xmls) throws DecodingException {
        List<XMLParticle> particles = new ArrayList<XMLParticle>();
        for(String xml : xmls) {
            particles.add(new XMLParticle(xml));
        }
        return (XMLElement) xmlRawToFragmentConverter.convert(particles);
    }

    public void testSimpleNamespacePrefix() throws DecodingException {
        XMLElement element = elementFromString("<p:elm xmlns:p=\"urn:foo\"/>");        
        assertEquals("p", element.getNamespacePrefix());
    }

    public void testDefaultNamespacePrefix() throws DecodingException {
        XMLElement element = elementFromString("<elm xmlns=\"urn:foo\"/>");        
        assertEquals("", element.getNamespacePrefix());
    }

    
    public void testSimpleNamespaceURI() throws DecodingException {
        XMLElement element = elementFromString("<elm xmlns=\"urn:foo\"/>");        
        assertEquals("urn:foo", element.getNamespaceURI());
    }

    public void testSimpleNamespaceURIEmptyElement() throws DecodingException {
        XMLElement element = elementFromString("<elm xmlns=\"urn:foo\">", "</elm>");        
        assertEquals("urn:foo", element.getNamespaceURI());
    }
    
    public void testPrefixedNamespaceURI() throws DecodingException {
        XMLElement element = elementFromString("<p:elm xmlns:p=\"urn:foo\"/>");        
        assertEquals("urn:foo", element.getNamespaceURI());
    }

    public void testDoubleNamespaceURI() throws DecodingException {
        XMLElement element = elementFromString("<p:elm xmlns=\"urn:bar\" xmlns:p=\"urn:foo\"/>");
        assertEquals("urn:foo", element.getNamespaceURI());
    }

//    public void testInheritedNamespaceURI() throws DecodingException {
//        XMLElement element = elementFromString("<elm xmlns=\"urn:foo\">", "<child />", "</elm>");
//        XMLElement child = element.getFirstInnerElement();
//        
//        assertEquals("urn:foo", child.getNamespaceURI());
//    }

    public void testOverriddenNamespaceURI() throws DecodingException {
        XMLElement element = elementFromString("<elm xmlns=\"urn:foo\">", "<child xmlns=\"urn:bar\" />", "</elm>");
        XMLElement child = element.getFirstInnerElement();
        
        assertEquals("urn:bar", child.getNamespaceURI());
    }
    
    public void testOverriddenToDefaultNamespaceURI() throws DecodingException {
        XMLElement element = elementFromString("<elm xmlns=\"urn:foo\">", "<child xmlns=\"\" />", "</elm>");
        XMLElement child = element.getFirstInnerElement();
        
        assertEquals("", child.getNamespaceURI());
    }

}