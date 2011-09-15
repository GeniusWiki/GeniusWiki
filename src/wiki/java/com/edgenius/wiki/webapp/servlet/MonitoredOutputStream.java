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
package com.edgenius.wiki.webapp.servlet;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Dapeng.Ni
 */
public class MonitoredOutputStream extends OutputStream {
	private OutputStream target;

	private OutputStreamListener listener;

	public MonitoredOutputStream(OutputStream target, OutputStreamListener listener, String filename) {
		this.target = target;
		this.listener = listener;
		this.listener.start(filename);
	}

	public void write(byte b[], int off, int len) throws IOException {
		target.write(b, off, len);
		listener.bytesRead(len - off);
	}

	public void write(byte b[]) throws IOException {
		target.write(b);
		listener.bytesRead(b.length);
	}

	public void write(int b) throws IOException {
		target.write(b);
		listener.bytesRead(1);
	}

	public void close() throws IOException {
		target.close();
		listener.done();
	}

	public void flush() throws IOException {
		target.flush();
	}
}
