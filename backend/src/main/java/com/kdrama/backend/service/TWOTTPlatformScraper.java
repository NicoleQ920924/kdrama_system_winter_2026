package com.kdrama.backend.service;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kdrama.backend.config.TWOTTPlatformConfig;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Service
public class TWOTTPlatformScraper {
    @Autowired
    private TWOTTPlatformRegistryService platformService;

    @Autowired
    private CacheManager cacheManager;

    @Scheduled(cron = "0 0 0/3 * * *") // it is recommended to use @Scheduled(cron = "0 0 0/3 * * *") to preload productions every three hours; the project has to be in running state to work, and if the timing is missed, one has to wait until the next one
    public void preloadProductionsOnTWOTTPlatforms() {
        TWOTTPlatformConfig config = new TWOTTPlatformConfig(null, null, null, null, null, null, false);
        Map<String, String> productionMap = new HashMap<>();

        System.out.println("Fetching Taiwanese OTT Productions...");

        Map<String, TWOTTPlatformConfig> allPlatforms = platformService.getPlatforms();

        for (Map.Entry<String, TWOTTPlatformConfig> e : allPlatforms.entrySet()) {
            config = platformService.getPlatforms().get(e.getKey());
            delay(3000);
            productionMap = startPlatformScraper(config);

            if (productionMap != null) {
                cacheManager.saveCache(e.getValue().getName(), productionMap);
                System.out.println("Fetching for this platform is complete, " + productionMap.size() + " works in total");
            } else {
                System.out.println("Failed to fetch works on " + e.getKey());
            }
        }

        System.out.println("Taiwanese OTT Productions Fetching is complete.");
    }
    
    public HashMap<String, String> getWorkTWOTTPlatformInfo(String workChineseName, String workType) {
        Set<String> allPlatformNames = platformService.getPlatforms().keySet();

        // HashMap to be returned
        HashMap<String, String> platformMapOfThisWork = new HashMap<>();

        for (String platformName : allPlatformNames) {
            Map<String, String> allProductionsOnPlatform = cacheManager.loadCache(platformName);

            if (workType.equals("drama") && platformName.contains("-movie")) {
                // Skip movie caches if the search target is a drama
                continue;
            }
            else if (workType.equals("movie") && platformName.contains("-drama")) {
                // Skip drama caches if the search target is a movie
                continue;
            }
            else if (allProductionsOnPlatform.containsKey(workChineseName)) {
                // This work can be watched on this platform; add to map
                String platformNameToSave = platformName.split("-")[0];
                platformMapOfThisWork.put(platformNameToSave, allProductionsOnPlatform.get(workChineseName));
            }
        }
        
        return platformMapOfThisWork;
    }

    public Map<String, String> startPlatformScraper(TWOTTPlatformConfig platformConfig) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        String url = platformConfig.getSearchUrl();
        Set<String> seenTitles = new HashSet<>();
        Map<String, String> allWorksOnPlatform = new HashMap<>();

        try {
            driver.get(url);

            while (true) {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(platformConfig.gettitleElementSelector())));
                List<WebElement> titleElements = driver.findElements(By.cssSelector(platformConfig.gettitleElementSelector()));
                List<WebElement> watchUrlElements = driver.findElements(By.cssSelector(platformConfig.getWatchUrlElementSelector()));

                for (int i = 0; i < titleElements.size(); i++) {
                    // Note that findElement() throws NoSuchElementException while findElements() do not (return an empty list instead)
                    try {
                        String title = titleElements.get(i).getText().trim();
                        WebElement linkElement = watchUrlElements.get(i);
                        String link = linkElement.getDomAttribute("href");

                        // Skip elements without work titles
                        if (title.isEmpty()) continue;

                        // Ignore already seen ones
                        if (!seenTitles.contains(title)) {
                            seenTitles.add(title);
                            allWorksOnPlatform.put(title, platformConfig.getWatchUrlPrefix() + link);
                            System.out.println("Fetched work: " + title + " → " + link);
                        }

                    } catch (NoSuchElementException e) {
                        // Ignore other elements
                    }
                }

                // Click on the "next page" button
                delay(5000);
                List<WebElement> nextButtons = driver.findElements(By.cssSelector(platformConfig.getNextPageBtnSelector()));
                if (nextButtons.isEmpty()) {
                    System.out.println("No more next page buttons. Fetching for this platform ends.");
                    break;
                }

                WebElement nextButton = nextButtons.get(0);

                if (nextButton.getDomAttribute("aria-disabled") != null) {
                    if (nextButton.getDomAttribute("aria-disabled").equals("true")) {
                        System.out.println("Next page button is not clickable. Fetching ends.");
                        break;
                    }
                }

                try {
                    // Check if the button is clickable
                    if (nextButton.isDisplayed() && nextButton.isEnabled()) {

                        // Scroll and click the button via JavascriptExecutor
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nextButton);
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextButton);

                        // Wait until new titles are loaded
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(platformConfig.gettitleElementSelector())));
                        delay(5000); // Wait

                    } else {
                        System.out.println("Next page button is not clickable. Fetching ends.");
                        break;
                    }

                } catch (StaleElementReferenceException e) {
                    System.out.println("Stale element, retry fetching next page...");
                    continue; // Try to fetch the next button again
                } catch (Exception e) {
                    System.out.println("Failed to click next page button: " + e.getMessage());
                    break;
                }
            }

            System.out.println("No. of fetched works: " + allWorksOnPlatform.size() + "!");
            return allWorksOnPlatform;

        } catch (Exception e) {
            e.printStackTrace();
            return null;

        } finally {
            driver.quit();
        }
    }

    // Custom delay function
    private void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
