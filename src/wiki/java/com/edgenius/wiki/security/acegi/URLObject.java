/* 
 * =============================================================
 * Copyright (C) 2007-2011 Edgenius (http://www.edgenius.com)
 * =============================================================
 * License Information: http://www.edgenius.com/licensing/edgenius/2.0/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * http://www.gnu.org/licenses/gpl.txt
 *  
 * ****************************************************************
 */
package com.edgenius.wiki.security.acegi;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 	 
 * Construct a URLObject according to string value. URI fields is string before "?". ref field is string 
 * after "#". Parameter will parse by "key=value" pair between "?" and "#"
 * @author Dapeng.Ni
 */
public class URLObject {
	private Map<String, String[]> parameterMap = new HashMap<String, String[]>();
	private String URI;
	private String ref;
	
	public URLObject(String url){
		this(url,"UTF8");
	}
	 
	public URLObject(String url, String encoding){
		int len;
		if((len=url.indexOf("?")) != -1){
			URI = url.substring(0,len);
			String query = url.substring(len+1);
			if((len=query.indexOf("#")) != -1){
				ref = query.substring(len+1);
				query = query.substring(0,len);
			}
			try {
				parseParameters(parameterMap, query.getBytes(), encoding);
			} catch (UnsupportedEncodingException e) {
			}
		}else
			URI = url;
	}

	private void parseParameters(Map<String, String[]> map, byte[] data, String encoding)
        throws UnsupportedEncodingException {

        if (data != null && data.length > 0) {
            int    ix = 0;
            int    ox = 0;
            String key = null;
            String value = null;
            while (ix < data.length) {
                byte c = data[ix++];
                switch ((char) c) {
                case '&':
                    value = new String(data, 0, ox, encoding);
                    if (key != null) {
                        putMapEntry(map, key, value);
                        key = null;
                    }
                    ox = 0;
                    break;
                case '=':
                    key = new String(data, 0, ox, encoding);
                    ox = 0;
                    break;
                case '+':
                    data[ox++] = (byte)' ';
                    break;
                case '%':
                    data[ox++] = (byte)((convertHexDigit(data[ix++]) << 4)
                                    + convertHexDigit(data[ix++]));
                    break;
                default:
                    data[ox++] = c;
                }
            }
            //The last value does not end in '&'.  So save it now.
            if (key != null) {
                value = new String(data, 0, ox, encoding);
                putMapEntry(map, key, value);
            }
        }
    }
     private byte convertHexDigit(byte b) {
		if ((b >= '0') && (b <= '9'))
			return (byte) (b - '0');
		if ((b >= 'a') && (b <= 'f'))
			return (byte) (b - 'a' + 10);
		if ((b >= 'A') && (b <= 'F'))
			return (byte) (b - 'A' + 10);
		return 0;
	}

	/**
	 * Put name and value pair in map.  When name already exist, add value
	 * to array of values.
	 * @param b the character value byte
	 */
	private void putMapEntry(Map<String, String[]> map, String name, String value) {
		String[] newValues = null;
		String[] oldValues = map.get(name);
		if (oldValues == null) {
			newValues = new String[1];
			newValues[0] = value;
		} else {
			newValues = new String[oldValues.length + 1];
			System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
			newValues[oldValues.length] = value;
		}
		map.put(name, newValues);
	}
	
	public Map<String, String[]> getParameterMap() {
		return parameterMap;
	}

	public String getRef() {
		return ref;
	}

	public String getURI() {
		return URI;
	}


}
