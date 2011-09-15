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
package com.edgenius.core.util;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
/**
 * 
 * @author Dapeng.Ni
 */
public class ScaleImage {
	/**
	 * Resize image to given width by original width/height ratio.
	 * @param src
	 * @param width
	 * @param dest
	 * @throws FileUtilException 
	 * @throws IOException 
	 */
	public static File scale(File src, String fileExtension, int width) throws ImageProcessException {
		
		
		return scale(src,fileExtension, width, -1, fileExtension);
	}


	/**
	 * Resize image to give width and height. Note, this maybe leads resized
	 * image twist if given width and height is not match original image
	 * width/height ratio. Please just give height as -1 (or call
	 * scale(String,int,String) method) to keep image resize by ratio.
	 * 
	 * @param src
	 * @param width
	 * @param height
	 * @throws IOException
	 * @throws FileUtilException 
	 */
	public static File scale(File src, String ext, int width, int height, String fileExtension) throws ImageProcessException{
		try {
			
			File destFile = FileUtil.createTempFile("." + fileExtension);
			BufferedImage bsrc = ImageIO.read(src);
			int srcW = bsrc.getWidth();
			int srcH = bsrc.getHeight();
			
			if(srcW <= width){
				//don't need scale, just return source
				return src;
			}
			if (height == -1)
				height = (int) (((float) width / (float) srcW) * (float) srcH);
			
			BufferedImage bdest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = bdest.createGraphics();
			AffineTransform at = AffineTransform.getScaleInstance((double) width / bsrc.getWidth(), (double) height
					/ bsrc.getHeight());
			g.drawRenderedImage(bsrc, at);
			ImageIO.write(bdest, ext, destFile);
			return destFile;
		} catch (Exception e) {
			throw new ImageProcessException(e);
		}
	}

	public static void main(String[] args) throws ImageProcessException {
		File out = scale(new File("c:\\temp\\n.jpg"), "jpg", 50);
		System.out.println(out.getAbsolutePath());
	}
}
