/*
 * (C) Copyright 2015 Boni Garcia (http://bonigarcia.github.io/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package io.github.bonigarcia.wdm.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Generic test logic (browsing Wikipedia).
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.0.0
 */
public class ManagerTest {

	private static final int TIMEOUT = 30; // seconds

	protected WebDriver driver;

	public void browseWikipedia() {
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
		driver.get("http://en.wikipedia.org/wiki/Main_Page");

		WebElement searchInput = driver.findElement(By.id("searchInput"));
		searchInput.sendKeys("Software");

		// wait.until(ExpectedConditions.elementToBeClickable(By
		// .id("searchButton")));

		WebElement searchButton = driver.findElement(By.id("searchButton"));
		searchButton.click();

		wait.until(ExpectedConditions.textToBePresentInElementLocated(
				By.tagName("body"), "Computer software or simply software"));
	}
}
