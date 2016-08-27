package org.arrowhead.wp5.core.entities;

/*-
 * #%L
 * ARROWHEAD::WP5::Core Data Structures
 * %%
 * Copyright (C) 2016 The ARROWHEAD Consortium
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.arrowhead.wp5.core.interfaces.DERProviderIf;

/**
 * @version 1.0
 * @created 19-Kov-2014 11:41:17
 */
@XmlRootElement
public abstract class AbstractDER implements DERProviderIf, Serializable {
    private static final long serialVersionUID = -8899409979845372732L;
    /* A DER Id */
    protected String id = "0";
    /* A DER type description */
    protected String type = "Abstract DER Type";
    /* A DER instance name */
    protected String name = "No-name DER";
    /* A DER management URL */
    protected String url = null;

    public AbstractDER() {}

    public AbstractDER(String type) {
        this.type = type;
    }
    
    public AbstractDER(String type, String name) {
        this(type);
        this.name = name;
    }

    @XmlElement(name = "id")
    public String getId() {
        return id;
    }

    @XmlElement(name = "url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /* Abstract methods */
    public abstract void generateFlexOffer();

}
