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
package org.apache.cocoon.woody.formmodel;

import org.apache.cocoon.woody.event.ActionEvent;
import org.apache.cocoon.woody.event.ActionListener;

/**
 * The definition for a repeater action that adds a row to a sibling repeater.
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: AddRowActionDefinition.java,v 1.4 2004/03/09 13:53:56 reinhard Exp $
 */
public class AddRowActionDefinition extends RepeaterActionDefinition {
    
    public AddRowActionDefinition(String repeaterName) {
        super(repeaterName);
        
        this.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Repeater repeater = ((RepeaterAction)event.getSource()).getRepeater();
                repeater.addRow();
            }
        });
    }
}
