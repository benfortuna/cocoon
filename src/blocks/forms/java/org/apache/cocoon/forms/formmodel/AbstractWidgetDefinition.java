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
package org.apache.cocoon.forms.formmodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.event.CreateEvent;
import org.apache.cocoon.forms.event.CreateListener;
import org.apache.cocoon.forms.event.WidgetEventMulticaster;
import org.apache.cocoon.forms.validation.WidgetValidator;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Provides functionality that is common across many WidgetDefinition implementations.
 * 
 * @version $Id$
 */
public abstract class AbstractWidgetDefinition implements WidgetDefinition {
    private FormDefinition formDefinition;
    protected WidgetDefinition parent;
    
    //TODO consider final on these
    private String location = null;
    private String id;
    private Map displayData;
    private List validators;

    protected CreateListener createListener;

    public FormDefinition getFormDefinition() {
        if (this.formDefinition == null) {
            if (this instanceof FormDefinition) {
                this.formDefinition = (FormDefinition)this;
            } else {
                this.formDefinition = this.parent.getFormDefinition();
            }
        }
        return this.formDefinition;
    }

    /**
     * Sets the parent of this definition
     */
    public void setParent(WidgetDefinition definition) {
        this.parent = definition;
    }
    
    /**
     * Gets the parent of this definition.
     * This method returns null for the root definition.
     */
    public WidgetDefinition getParent() {
        return this.parent;
    }

    protected void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    protected void addCreateListener(CreateListener listener) {
    		this.createListener = WidgetEventMulticaster.add(this.createListener, listener);
    }
    
    public void fireCreateEvent(CreateEvent event) {
        if (this.createListener != null) {
        	    this.createListener.widgetCreated(event);
        }
    }

    public void generateLabel(ContentHandler contentHandler) throws SAXException {
        generateDisplayData("label", contentHandler);
    }

    /**
     * Sets the various display data for this widget. This includes the label, hint and help.
     * They must all be objects implementing the XMLizable interface. This approach
     * allows to have mixed content in these data.
     * 
     * @param displayData an association of {name, sax fragment}
     */
    public void setDisplayData(Map displayData) {
        this.displayData = displayData;
    }
    
    public void addValidator(WidgetValidator validator) {
        if (this.validators == null) {
            this.validators = new ArrayList();
        }
        
        this.validators.add(validator);
    }
    
    public void generateDisplayData(String name, ContentHandler contentHandler) throws SAXException {
        Object data = this.displayData.get(name);
        if (data != null) {
            ((XMLizable)data).toSAX(contentHandler);
        } else if (!this.displayData.containsKey(name)) {
            throw new IllegalArgumentException("Unknown display data name '" + name + "'");
        }
    }
    
    public void generateDisplayData(ContentHandler contentHandler) throws SAXException {
        // Output all non-null display data
        Iterator iter = this.displayData.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            if (entry.getValue() != null) {
                String name = (String)entry.getKey();
                
                // Enclose the data into a "wi:{name}" element
                contentHandler.startElement(Constants.INSTANCE_NS, name, Constants.INSTANCE_PREFIX_COLON + name, XMLUtils.EMPTY_ATTRIBUTES);

                ((XMLizable)entry.getValue()).toSAX(contentHandler);

                contentHandler.endElement(Constants.INSTANCE_NS, name, Constants.INSTANCE_PREFIX_COLON + name);
            }
        }   
    }
    
    public boolean validate(Widget widget) {
        if (this.validators == null) {
            // No validators
            return true;
            
        } else {
            Iterator iter = this.validators.iterator();
            while(iter.hasNext()) {
                WidgetValidator validator = (WidgetValidator)iter.next();
                if (! validator.validate(widget)) {
                    // Stop at the first validator that fails
                    return false;
                }
            }
            // All validators were sucessful
            return true;
        }
    }
}
