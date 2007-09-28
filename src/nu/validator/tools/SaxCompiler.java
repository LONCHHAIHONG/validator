/*
 * Copyright (c) 2005 Henri Sivonen
 * Copyright (c) 2007 Mozilla Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package nu.validator.tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.parsers.SAXParserFactory;

import nu.validator.java.StringLiteralUtil;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Please refer to http://hsivonen.iki.fi/saxcompiler/
 * 
 * @version $Id: SaxCompiler.java,v 1.3 2006/11/18 00:05:24 hsivonen Exp $
 * @author hsivonen
 */
public class SaxCompiler implements ContentHandler {

    private StringBuilder sb = new StringBuilder();

    private Writer w;

    private int start = 0;

    private int state = 0;

    // 0 initial
    // 1 package written
    // 2 class written
    // 3 method written

    private boolean omitRoot = false;

    private int level = 0;

    /**
     * Instantiates a <code>SaxCompiler</code>
     * 
     * @param w
     *            the <code>Writer</code> to which generated code is written
     */
    public SaxCompiler(Writer w) {
        this.w = w;
    }

    /**
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator arg0) {
    }

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        try {
            w.write("/"
                    + "* This code was generated by nu.validator.tools.SaxCompiler. Please regenerate instead of editing. *"
                    + "/\n");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        try {
            if (!omitRoot) {
                w.write("} finally {\ncontentHandler.endDocument();\n}\n");
            }
            w.write("}\n");
            w.write("private static final char[] __chars__ = ");
            w.write(StringLiteralUtil.charArrayLiteral(sb));
            w.write(";\n}\n");
            w.flush();
            w.close();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
     *      java.lang.String)
     */
    public void startPrefixMapping(String arg0, String arg1)
            throws SAXException {
        ensureState();
        try {
            w.write("contentHandler.startPrefixMapping(");
            w.write(StringLiteralUtil.stringLiteral(arg0));
            w.write(", ");
            w.write(StringLiteralUtil.stringLiteral(arg1));
            w.write(");\n");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String arg0) throws SAXException {
        try {
            w.write("contentHandler.endPrefixMapping(");
            w.write(StringLiteralUtil.stringLiteral(arg0));
            w.write(");\n");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String arg0, String arg1, String arg2,
            Attributes attrs) throws SAXException {
        ensureState();
        level++;
        if (omitRoot && level == 1) {
            return;
        }
        try {
            w.write("__attrs__.clear();\n");
            for (int i = 0; i < attrs.getLength(); i++) {
                w.write("__attrs__.addAttribute(");
                w.write(StringLiteralUtil.stringLiteral(attrs.getURI(i)));
                w.write(", ");
                w.write(StringLiteralUtil.stringLiteral(attrs.getLocalName(i)));
                w.write(", ");
                w.write(StringLiteralUtil.stringLiteral(attrs.getQName(i)));
                w.write(", ");
                w.write(StringLiteralUtil.stringLiteral(attrs.getType(i)));
                w.write(", ");
                w.write(StringLiteralUtil.stringLiteral(attrs.getValue(i)));
                w.write(");\n");
            }
            w.write("contentHandler.startElement(");
            w.write(StringLiteralUtil.stringLiteral(arg0));
            w.write(", ");
            w.write(StringLiteralUtil.stringLiteral(arg1));
            w.write(", ");
            w.write(StringLiteralUtil.stringLiteral(arg2));
            w.write(", __attrs__);\n");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String arg0, String arg1, String arg2)
            throws SAXException {
        if (omitRoot && level == 1) {
            return;
        }
        level--;
        try {
            w.write("contentHandler.endElement(");
            w.write(StringLiteralUtil.stringLiteral(arg0));
            w.write(", ");
            w.write(StringLiteralUtil.stringLiteral(arg1));
            w.write(", ");
            w.write(StringLiteralUtil.stringLiteral(arg2));
            w.write(");\n");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] buf, int offset, int length)
            throws SAXException {
        sb.append(buf, offset, length);
        try {
            w.write("contentHandler.characters(__chars__, ");
            w.write("" + start);
            w.write(", ");
            w.write("" + length);
            w.write(");\n");
        } catch (IOException e) {
            throw new SAXException(e);
        }
        start += length;
    }

    /**
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
            throws SAXException {
    }

    /**
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
     *      java.lang.String)
     */
    public void processingInstruction(String target, String data)
            throws SAXException {
        try {
            if ("SaxCompiler-package".equals(target)) {
                assertState(0);
                w.write("package ");
                w.write(data);
                w.write(";\n");
                state = 1;
            } else if ("SaxCompiler-class".equals(target)) {
                assertStateLEQ(1);
                w.write("public final class ");
                w.write(data);
                w.write(" {\n");
                w.write("private ");
                w.write(data);
                w.write("() {}\n");
                state = 2;
            } else if ("SaxCompiler-args".equals(target)) {
                assertState(2);
                w.write("public static void emit(org.xml.sax.ContentHandler contentHandler, ");
                w.write(data);
                w.write(") throws org.xml.sax.SAXException {\n");
                state = 3;
                writeStart();
            } else if ("SaxCompiler-omitRoot".equals(target)) {
                assertStateLEQ(2);
                omitRoot = true;
            } else if ("SaxCompiler-code".equals(target)) {
                ensureState();
                w.write(data);
                w.write("\n");
            } else {
                ensureState();
                w.write("contentHandler.processingInstruction(");
                w.write(StringLiteralUtil.stringLiteral(target));
                w.write(", ");
                w.write(StringLiteralUtil.stringLiteral(data));
                w.write(");\n");
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String arg0) throws SAXException {
        throw new SAXException("skippedEntity not supported");
    }

    private void assertState(int s) throws SAXException {
        if (state != s) {
            throw new SAXException("Illegal state.");
        }
    }

    private void assertStateLEQ(int s) throws SAXException {
        if (state > s) {
            throw new SAXException("Illegal state.");
        }
    }

    private void writeStart() throws SAXException {
        try {
            w.write("org.xml.sax.helpers.AttributesImpl __attrs__ = new org.xml.sax.helpers.AttributesImpl();\n");
            if (!omitRoot) {
                w.write("try {\n");
                w.write("contentHandler.startDocument();\n");
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }

    }

    private void ensureState() throws SAXException {
        if (state == 2) {
            try {
                w.write("public static void emit(org.xml.sax.ContentHandler contentHandler) throws org.xml.sax.SAXException {\n");
                writeStart();
            } catch (IOException e) {
                throw new SAXException(e);
            }
            state = 3;
        } else if (state != 3) {
            throw new SAXException("Illegal state.");
        }
    }

    public static void main(String[] args) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            XMLReader reader = factory.newSAXParser().getXMLReader();
            InputSource in = new InputSource(new FileInputStream(args[0]));
            SaxCompiler sc = new SaxCompiler(new OutputStreamWriter(
                    new FileOutputStream(args[1]), "UTF-8"));
            reader.setContentHandler(sc);
            reader.parse(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}