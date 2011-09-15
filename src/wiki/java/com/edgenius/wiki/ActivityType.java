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
package com.edgenius.wiki;


/**
 * @author Dapeng.Ni
 */
public class ActivityType {

	
	public static enum Type {
		SYSTEM_EVENT(-1), 
		USER_EVENT(1), 
		PAGE_EVENT(20), 
		SPACE_EVENT(50), 
		COMMENT_EVENT(55),
		ATTACHMENT_EVENT(60)
		;

		int code;

		private Type(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public static Type valueOfCode(int code) {
			Type[] values = values();
			for (Type type : values) {
				if (type.code == code) {
					return type;
				}
			}
			return null;
		}
		@Override public String toString() {
			return super.toString().toLowerCase();
		}

	}
	
	public static enum SubType {
		CREATE(1), //for user, it is new user sign-up
		UPDATE(2), //for user, it is status update
		DELETE(3),
		RESTORE(4),
		PERMANENT_DELETE(5),
		COPY(6),
		MOVE(7),
		FOLLOW(8),
		UNFOLLOW(9),
		REVERT(10),
		VERSION_PING(11), 
		REBUILD_INDEX(12),
		REDEPLOY_SHELL(13),
		;

		int code;

		private SubType(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public static SubType valueOfCode(int code) {
			SubType[] values = values();
			for (SubType type : values) {
				if (type.code == code) {
					return type;
				}
			}
			return null;
		}
		
		@Override public String toString() {
			return super.toString().toLowerCase();
		}
	}

}
