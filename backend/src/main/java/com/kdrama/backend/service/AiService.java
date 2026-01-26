package com.kdrama.backend.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AiService(ChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generate a simple response using Gemini
     */
    public String generateResponse(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * Search for dramas based on user criteria and return top 3 results
     * Returns a JSON array with drama titles and summaries
     */
    public ArrayNode searchDramasByPrompt(String prompt) {
        try {
            // Build a prompt to ask LLM for top 3 dramas with summaries
            String searchPrompt = prompt + "\n\n請列出 TOP 3 符合條件的韓劇。" +
                    "以 JSON 陣列格式回覆，包含：title（劇名），summary（20字左右簡介）。" +
                    "回覆格式：[{\"title\": \"劇名1\", \"summary\": \"簡介1\"}, {\"title\": \"劇名2\", \"summary\": \"簡介2\"}, {\"title\": \"劇名3\", \"summary\": \"簡介3\"}]" +
                    "只回覆 JSON 陣列，不需要其他文字。";

            String response = generateResponse(searchPrompt);

            // Try to parse the response as JSON
            ArrayNode results = parseAndExtractDramas(response);
            
            // If parsing fails or we get fewer than 3 results, try again with clearer instructions
            if (results.size() == 0) {
                results = fallbackParseDramas(response);
            }

            // Ensure we have at most 3 results
            ArrayNode limitedResults = objectMapper.createArrayNode();
            for (int i = 0; i < Math.min(results.size(), 3); i++) {
                limitedResults.add(results.get(i));
            }

            return limitedResults;

        } catch (Exception e) {
            System.err.println("Exception occurred during drama search: " + e.getMessage());
            e.printStackTrace();
            return objectMapper.createArrayNode();
        }
    }

    /**
     * Parse JSON response from LLM
     */
    private ArrayNode parseAndExtractDramas(String response) {
        try {
            // Find JSON array pattern
            Pattern pattern = Pattern.compile("\\[\\s*\\{.*?\\}\\s*\\]", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(response);

            if (matcher.find()) {
                String jsonStr = matcher.group();
                JsonNode parsed = objectMapper.readTree(jsonStr);
                if (parsed.isArray()) {
                    return (ArrayNode) parsed;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse JSON: " + e.getMessage());
        }
        return objectMapper.createArrayNode();
    }

    /**
     * Fallback parsing if JSON extraction fails
     * Extracts drama titles and summaries from natural language response
     */
    private ArrayNode fallbackParseDramas(String response) {
        ArrayNode results = objectMapper.createArrayNode();
        
        try {
            // Split by numbers (1., 2., 3.) to extract results
            String[] parts = response.split("(?=\\d\\.\\s)");
            
            int count = 0;
            for (int i = 1; i < parts.length && count < 3; i++) {
                String part = parts[i].trim();
                
                // Extract title (text after number, before first dash or longer description)
                Pattern titlePattern = Pattern.compile("^\\d\\.\\s*([^\\-—\n]+?)(?:\\s*[\\-—]|$)");
                Matcher titleMatcher = titlePattern.matcher(part);
                
                if (titleMatcher.find()) {
                    String title = titleMatcher.group(1).trim();
                    
                    // Extract summary (next ~20 chars after title)
                    String summary = part.substring(titleMatcher.end()).trim();
                    if (summary.length() > 50) {
                        summary = summary.substring(0, 50) + "...";
                    }
                    
                    ObjectNode drama = objectMapper.createObjectNode();
                    drama.put("title", title);
                    drama.put("summary", summary);
                    results.add(drama);
                    count++;
                }
            }
        } catch (Exception e) {
            System.err.println("Fallback parsing failed: " + e.getMessage());
        }
        
        return results;
    }
}

