/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.reading.imageop;

import java.awt.geom.AffineTransform;

import java.awt.image.AffineTransformOp;
import java.awt.image.WritableRaster;

import org.apache.avalon.framework.parameters.Parameters;

public class ShearOperation
    implements ImageOperation
{
    private String  m_Prefix;
    private boolean m_Enabled;
    private float   m_ShearX;
    private float   m_ShearY;
    
    public void setPrefix( String prefix )
    {
        m_Prefix = prefix;
    }
    
    public void setup( Parameters params )
    {
        m_Enabled = params.getParameterAsBoolean( m_Prefix + "enabled", true);
        m_ShearX = params.getParameterAsFloat( m_Prefix + "shear-x", 0.0f );
        m_ShearY = params.getParameterAsFloat( m_Prefix + "shear-y", 0.0f );
    }
    
    public WritableRaster apply( WritableRaster image )
    {
        if( ! m_Enabled )
            return image;
        AffineTransform shear = AffineTransform.getShearInstance( m_ShearX, m_ShearY );
        AffineTransformOp op = new AffineTransformOp( shear, AffineTransformOp.TYPE_BILINEAR );
        WritableRaster scaledRaster = op.filter( image, null );
        return scaledRaster;
    }

    public String getKey()
    {
        return "shear:" 
               + ( m_Enabled ? "enable" : "disable" )
               + ":" + m_ShearX
               + ":" + m_ShearY
               + ":" + m_Prefix;
    }
} 
