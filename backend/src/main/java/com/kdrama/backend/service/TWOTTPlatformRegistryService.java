package com.kdrama.backend.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.kdrama.backend.config.TWOTTPlatformConfig;

@Component
public class TWOTTPlatformRegistryService {
    private final Map<String, TWOTTPlatformConfig> platforms = new HashMap<>();

    public TWOTTPlatformRegistryService() {
        // Now only ones that can be scraped directly are here
        // If there are changes in the platform websites' design, the css selectors may have to be updated
        platforms.put("friDay影音-drama", new TWOTTPlatformConfig("friDay影音-drama", "https://video.friday.tw/drama/filter/all/all/all/korea", ".filmcase-name-bottom h4", ".filmcase a", "https://video.friday.tw", ".common-paging > a.active ~ a.arrowBtn", false));
        platforms.put("friDay影音-movie", new TWOTTPlatformConfig("friDay影音-movie", "https://video.friday.tw/movie/filter/all/all/all/korea", ".filmcase-name-bottom h4", ".filmcase a", "https://video.friday.tw", ".common-paging > a.active ~ a.arrowBtn", false));
        platforms.put("中華電信Hami Video-drama", new TWOTTPlatformConfig("中華電信Hami Video-drama", "https://hamivideo.hinet.net/%E5%BD%B1%E5%8A%87%E9%A4%A8%E2%81%BA/%E6%88%B2%E5%8A%87/%E9%9F%93%E5%8A%87.do", ".list_item .title h3", ".list_item a", "https://hamivideo.hinet.net", "#result_more", false));
        // For Hami Video-movie, due to the website design, there are no filters -> every single movie regardless of country of origin will be fetched; do not fetch it too frequently
        platforms.put("中華電信Hami Video-movie", new TWOTTPlatformConfig("中華電信Hami Video-movie", "https://hamivideo.hinet.net/more.do?type=card_vod_horizontal&key=1611&menuId=171&filterType=new&getStr=0&isNew=1&isPopular=1&isScore=1&filter=1&mn=%E5%BD%B1%E5%8A%87%E9%A4%A8%E2%81%BA&submn=%E9%9B%BB%E5%BD%B1&lastmn=%E6%8E%A8%E8%96%A6&title=%E6%9C%AC%E9%80%B1%E4%B8%8A%E6%9E%B6&back=https%3A%2F%2Fhamivideo.hinet.net%2F%25E5%25BD%25B1%25E5%258A%2587%25E9%25A4%25A8%25E2%2581%25BA%2F%25E9%259B%25BB%25E5%25BD%25B1%2F%25E6%258E%25A8%25E8%2596%25A6.do?bookmark=bookmark0", ".list_item .title h3", ".list_item a", "https://hamivideo.hinet.net", "#result_more", false));
        platforms.put("MyVideo-movie", new TWOTTPlatformConfig("MyVideo-movie", "https://www.myvideo.net.tw/movie/genre/all/all/%E9%9F%93%E5%9C%8B/all/?orderBy=1", "#vidioListUl li h3 a", "#vidioListUl li h3 a", "https://www.myvideo.net.tw", ".more", false));
        // For LINE TV, it is relatively harder to scrape as sometimes the next button cannot be fetched normally; need to take a few tries or help Chrome driver click the buttons
        platforms.put("LINE TV-drama", new TWOTTPlatformConfig("LINE TV-drama", "https://www.linetv.tw/channel/1/genre/44?genre_token=44&sort=VIEW_COUNT_LAST_7_DAYS&source=CHANNEL_PREDEFINED_FILTER&source_channel_id=1&source_feed_id=13", "[class^=ChannelFilterResult__DramaGridItemTitle]", "[class^=ChannelFilterResult__DramaGrid] a", "https://www.linetv.tw", "li.Pagination-next a.Pagination-nextLink", false));
    }

    public Map<String, TWOTTPlatformConfig> getPlatforms() {
        return platforms;
    }

    public TWOTTPlatformConfig getPlatform(String name) {
        return platforms.get(name);
    }
}
