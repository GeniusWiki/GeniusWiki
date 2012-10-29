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
package com.edgenius.wiki.webapp.admin.action;

import com.edgenius.wiki.model.Space;

/**
 * @author Dapeng.Ni
 */
public class SpaceDTO {
	private Space space;
	private long totalPages;
	private String lastUpdatePageTitle;
	private String lastUpdatePageModifiedDate;
	private String createdDate;
	private String smallLogoUrl;
	private int delayRemoveHours;
	private String privateSpace;
	private String largeLogoUrl;
	private String quota;
	private long quotaNum;
	private boolean globalAdSense;
	private boolean spaceAdSense;


	public long getQuotaNum() {
		return quotaNum;
	}
	public void setQuotaNum(long quotaNum) {
		this.quotaNum = quotaNum;
	}
	public String getPrivateSpace() {
		return privateSpace;
	}
	public void setPrivateSpace(String privateSpace) {
		this.privateSpace = privateSpace;
	}
	public int getDelayRemoveHours() {
		return delayRemoveHours;
	}
	public void setDelayRemoveHours(int delayRemoveHours) {
		this.delayRemoveHours = delayRemoveHours;
	}
	public String getSmallLogoUrl() {
		return smallLogoUrl;
	}
	public void setSmallLogoUrl(String smallLogoUrl) {
		this.smallLogoUrl = smallLogoUrl;
	}
	public String getLargeLogoUrl() {
		return largeLogoUrl;
	}
	public void setLargeLogoUrl(String largeLogoUrl) {
		this.largeLogoUrl = largeLogoUrl;
	}
	public String getQuota() {
		return quota;
	}

	public void setQuota(String quota) {
		this.quota = quota;
	}
	public String getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	public Space getSpace() {
		return space;
	}
	public void setSpace(Space space) {
		this.space = space;
	}
	public long getTotalPages() {
		return totalPages;
	}
	public void setTotalPages(long totalPages) {
		this.totalPages = totalPages;
	}
	public String getLastUpdatePageTitle() {
		return lastUpdatePageTitle;
	}
	public void setLastUpdatePageTitle(String lastUpdatePageTitle) {
		this.lastUpdatePageTitle = lastUpdatePageTitle;
	}
	public String getLastUpdatePageModifiedDate() {
		return lastUpdatePageModifiedDate;
	}
	public void setLastUpdatePageModifiedDate(String lastUpdatePageModifiedDate) {
		this.lastUpdatePageModifiedDate = lastUpdatePageModifiedDate;
	}
	public boolean isGlobalAdSense() {
		return globalAdSense;
	}
	public void setGlobalAdSense(boolean globalAdSense) {
		this.globalAdSense = globalAdSense;
	}
	public boolean isSpaceAdSense() {
		return spaceAdSense;
	}
	public void setSpaceAdSense(boolean spaceAdSense) {
		this.spaceAdSense = spaceAdSense;
	}
}
