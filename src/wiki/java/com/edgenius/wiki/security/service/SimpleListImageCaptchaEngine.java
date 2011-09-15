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
package com.edgenius.wiki.security.service;

import java.awt.Color;
import java.util.Locale;

import org.apache.commons.lang.RandomStringUtils;

import com.octo.captcha.component.image.backgroundgenerator.BackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.UniColorBackgroundGenerator;
import com.octo.captcha.component.image.fontgenerator.FontGenerator;
import com.octo.captcha.component.image.fontgenerator.TwistedAndShearedRandomFontGenerator;
import com.octo.captcha.component.image.textpaster.RandomTextPaster;
import com.octo.captcha.component.image.textpaster.TextPaster;
import com.octo.captcha.component.image.wordtoimage.ComposedWordToImage;
import com.octo.captcha.component.image.wordtoimage.WordToImage;
import com.octo.captcha.component.word.wordgenerator.WordGenerator;
import com.octo.captcha.engine.image.ListImageCaptchaEngine;
import com.octo.captcha.image.gimpy.GimpyFactory;

/**
 * @author Dapeng.Ni
 */
public class SimpleListImageCaptchaEngine extends  ListImageCaptchaEngine{

	@Override
	protected void buildInitialFactories() {

        TextPaster textPaster = new RandomTextPaster(Integer.valueOf(6), Integer.valueOf(7), Color.black);

        BackgroundGenerator backgroundGenerator = new UniColorBackgroundGenerator(Integer.valueOf(140), Integer.valueOf(50), Color.white);

        FontGenerator fontGenerator = new TwistedAndShearedRandomFontGenerator(Integer.valueOf(20), Integer.valueOf(25));

        WordToImage wordToImage = new ComposedWordToImage(fontGenerator, backgroundGenerator, textPaster);

        this.addFactory(new GimpyFactory(new RandomWordGenerator(), wordToImage));
		
	}

	private class RandomWordGenerator implements WordGenerator{

		public String getWord(Integer in) {
			String wd =  RandomStringUtils.randomAlphabetic(6);
			return wd.toLowerCase();
			
		}
		public String getWord(Integer in, Locale local) {
			String wd =  RandomStringUtils.randomAlphabetic(6);
			return wd.toLowerCase();
		}
		
	}
}
